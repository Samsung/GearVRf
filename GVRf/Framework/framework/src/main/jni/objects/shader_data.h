/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/***************************************************************************
 * Data for doing a post effect on the scene.
 ***************************************************************************/

#ifndef SHADER_DATA_H_
#define SHADER_DATA_H_

#include <map>
#include <memory>
#include <string>
#include <shaders/shader.h>

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "helpers.h"

#include "objects/hybrid_object.h"
#include "objects/textures/texture.h"
#include "objects/uniform_block.h"
#include "shaders/shader.h"

namespace gvr {

class Texture;

class ShaderData : public HybridObject
{
public:
    ShaderData(const std::string& descriptor) :
            native_shader_(0), textures_(), lock_() { }

    virtual ~ShaderData() { }

    int getNativeShader()
    {
        return native_shader_;
    }

    void setNativeShader(int shader)
    {
        native_shader_ = shader;
        dirty(NATIVE_SHADER);
    }

    Texture* getTexture(const std::string& key) const
    {
        auto it = textures_.find(key);
        if (it != textures_.end())
        {
            return it->second;
        }
        return NULL;
    }

    void setTexture(const std::string& key, Texture* texture)
    {
        std::lock_guard<std::mutex> lock(lock_);
        if(textures_.find(key) != textures_.end()){
            dirty(MOD_TEXTURE);
        }
        else {
            dirty(NEW_TEXTURE);
        }
        textures_[key] = texture;
        //By the time the texture is being set to its attaching material, it is ready
        //This is guaranteed by upper java layer scheduling
    }

    int getByteSize(const std::string& name) const
    {
        return uniforms().getByteSize(name);
    }

    const std::string& getDescriptor() const
    {
        return uniforms().getDescriptor();
    }

    bool getFloat(const std::string& name, float& v) const
    {
        return uniforms().getFloat(name, v);
    }

    bool   getInt(const std::string& name, int& v) const
    {
        return uniforms().getInt(name, v);
    }

    bool  setInt(const std::string& name, int val)
    {
        std::lock_guard<std::mutex> lock(lock_);
        dirty(MAT_DATA);
        return uniforms().setInt(name, val);
    }

    bool  setFloat(const std::string& name, float val)
    {
        std::lock_guard<std::mutex> lock(lock_);
        dirty(MAT_DATA);
        return uniforms().setFloat(name, val);
    }

    bool  setIntVec(const std::string& name, const int* val, int n)
    {
        std::lock_guard<std::mutex> lock(lock_);
        dirty(MAT_DATA);
        return uniforms().setIntVec(name, val, n);
    }

    bool  setFloatVec(const std::string& name, const float* val, int n)
    {
        std::lock_guard<std::mutex> lock(lock_);
        dirty(MAT_DATA);
        return uniforms().setFloatVec(name, val, n);
    }

    bool  getFloatVec(const std::string& name, float* val, int n)
    {
        return uniforms().getFloatVec(name, val, n);
    }

    bool  getIntVec(const std::string& name, int* val, int n)
    {
        return uniforms().getIntVec(name, val, n);
    }

    bool  setVec2(const std::string& name, const glm::vec2& v)
    {
        std::lock_guard<std::mutex> lock(lock_);
        return uniforms().setVec2(name, v);
    }

    bool  setVec3(const std::string& name, const glm::vec3& v)
    {
        std::lock_guard<std::mutex> lock(lock_);
        return uniforms().setVec3(name, v);
    }

    bool  setVec4(const std::string& name, const glm::vec4& v)
    {
        std::lock_guard<std::mutex> lock(lock_);
        return uniforms().setVec4(name, v);
    }

    bool  setMat4(const std::string& name, const glm::mat4& m)
    {
        std::lock_guard<std::mutex> lock(lock_);
        return uniforms().setMat4(name, m);
    }

    const std::map<std::string, Texture*>& getAllTextures()
    {
        return textures_;
    }

    void add_dirty_flag(const std::shared_ptr<u_short>& dirty_flag)
    {
        dirty_flags_.insert(dirty_flag);
    }

    void add_dirty_flags(const std::unordered_set<std::shared_ptr<u_short>>& dirty_flags)
    {
        dirty_flags_.insert(dirty_flags.begin(), dirty_flags.end());
    }

    void dirty(DIRTY_BITS bit)
    {
        dirtyImpl(dirty_flags_,bit);
    }

    bool hasTexture(const std::string& key) const
    {
        auto it = textures_.find(key);
        return (it != textures_.end());
    }

    bool hasUniform(const std::string& key) const
    {
        return uniforms().hasUniform(key);
    }

    virtual int updateGPU(Renderer* renderer, Shader* shader)
    {
        std::lock_guard<std::mutex> lock(lock_);
        const std::string& textureDescriptor = shader->getTextureDescriptor();
        const std::string& uniformDescriptor = shader->getUniformDescriptor();

        if (!textureDescriptor.empty())
        {
            for (auto it = textures_.begin(); it != textures_.end(); ++it)
            {
                Texture *tex = it->second;
                if (tex == NULL)
                {
                    LOGV("Texture: ShaderData::areTexturesReady %s is null", it->first.c_str());
                    return -1;
                }
                bool ready = tex->isReady();
                if (Shader::LOG_SHADER)
                {
                    Image* image = tex->getImage();
                    const char* fname = image ? image->getFileName() : "";
                    if (!ready) LOGD("Texture: %s not ready", fname);
                    //LOGV("Texture: ShaderData::areTexturesReady %s %s is %s",
                    //     it->first.c_str(), fname, ready ? "ready" : "not ready");
                }
                if ((tex == NULL) || !ready)
                {
                    return -1;
                }
            }
        }
        if (!uniformDescriptor.empty() && uniforms().updateGPU(renderer))
        {
            return 1;
        }
        return 0;
    }
    u_int32_t getNumberOfTextures(){
        return textures_.size();
    }

    virtual UniformBlock&   uniforms() = 0;
    virtual const UniformBlock& uniforms() const = 0;

private:
    ShaderData(const ShaderData&);
    ShaderData(ShaderData&&);
    ShaderData& operator=(const ShaderData&);
    ShaderData& operator=(ShaderData&&);

protected:
    int native_shader_;
    std::map<std::string, Texture*> textures_;
    std::mutex lock_;
    std::unordered_set<std::shared_ptr<u_short>> dirty_flags_;
};

}
#endif
