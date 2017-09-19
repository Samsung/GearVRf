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
#include <functional>

#include "helpers.h"

#include "objects/hybrid_object.h"
#include "objects/textures/texture.h"
#include "objects/uniform_block.h"


namespace gvr {

class Texture;
class RenderData;

class ShaderData : public HybridObject
{
public:
    ShaderData(const char* texture_desc);

    virtual ~ShaderData() { }

    const char* getUniformDescriptor() const;
    const char* getTextureDescriptor() const;
    Texture* getTexture(const char* key) const;
    void    setTexture(const char* key, Texture* texture);
    void    forEachTexture(std::function< void(const char* texname, Texture* tex) > func) const;
    int     getByteSize(const char* name) const;
    bool    getFloat(const char* name, float& v) const;
    bool    getInt(const char* name, int& v) const;
    bool    setInt(const char* name, int val);
    bool    setFloat(const char* name, float val);
    bool    setIntVec(const char* name, const int* val, int n);
    bool    setFloatVec(const char* name, const float* val, int n);
    bool    getFloatVec(const char* name, float* val, int n);
    bool    getIntVec(const char* name, int* val, int n);
    bool    setVec2(const char* name, const glm::vec2& v);
    bool    setVec3(const char* name, const glm::vec3& v);
    bool    setVec4(const char* name, const glm::vec4& v);
    bool    setMat4(const char* name, const glm::mat4& m);
    void    makeDirty(DIRTY_BITS bit);
    void    clearDirty();
    bool    hasTexture(const char* key) const;
    bool    hasUniform(const char* key) const;
    virtual int updateGPU(Renderer* rendere, RenderData* rdata);
    std::string makeShaderLayout();
    u_int32_t getNumTextures() const { return mTextures.size(); }
    virtual UniformBlock&   uniforms() = 0;
    virtual const UniformBlock& uniforms() const = 0;
    virtual void useGPUBuffer(bool flag) = 0;
private:
    ShaderData(const ShaderData&);
    ShaderData(ShaderData&&);
    ShaderData& operator=(const ShaderData&);
    ShaderData& operator=(ShaderData&&);

protected:
    int mNativeShader;
    std::string mTextureDesc;
    std::vector<std::string> mTextureNames;
    std::vector<Texture*> mTextures;
    mutable std::mutex mLock;
    DIRTY_BITS mDirty;
};

}
#endif
