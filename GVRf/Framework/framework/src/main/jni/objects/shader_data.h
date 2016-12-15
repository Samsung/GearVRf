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

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/hybrid_object.h"
#include "objects/textures/texture.h"
#include "objects/uniform_block.h"

namespace gvr {

class Texture;

class ShaderData: public HybridObject {
public:
    ShaderData() :
            native_shader_(0), textures_(), floats_(), ints_(), vec2s_(), vec3s_(), vec4s_(), mat4s_() { }

    ~ShaderData() {
    }

    int get_shader() {
        return native_shader_;
    }

    void set_shader(int shader) {
        native_shader_ = shader;
    }

    Texture* getTexture(const std::string& key) const {
        auto it = textures_.find(key);
        if (it != textures_.end()) {
            return it->second;
        } else {
            std::string error = "Material::getTexture() : " + key
                    + " not found";
            LOGE("%s", error.c_str());
            throw error;
        }
    }

    //A new api to return a texture even it is NULL without throwing a error,
    //otherwise it will be captured abruptly by the error handler
    Texture* getTextureNoError(const std::string& key) const {
        auto it = textures_.find(key);
        if (it != textures_.end()) {
            return it->second;
        } else {
            return NULL;
        }
    }

    virtual void setTexture(const std::string& key, Texture* texture) {
        textures_[key] = texture;
        //By the time the texture is being set to its attaching material, it is ready
        //This is guaranteed by upper java layer scheduling
        if (texture != NULL) {
            LOGE("SHADER: texture %s is ready", key.c_str());
            texture->setReady(true);
        }
    }

    bool getFloat(const std::string& key, float& v) {
        auto it = floats_.find(key);
        if (it != floats_.end()) {
            v = it->second;
            return true;
        } else {
            LOGE("Material::getFloat() : %s not found", key.c_str());
            return false;
        }
    }

    virtual void setFloat(const std::string& key, float value) {
        floats_[key] = value;
    }

    bool getInt(const std::string& key, int& v) {
        auto it = ints_.find(key);
        if (it != ints_.end()) {
            v = it->second;
            return true;
        } else {
            LOGE("Material::getInt() : %s not found", key.c_str());
            return false;
        }
    }

    virtual void setInt(const std::string& key, int value) {
        ints_[key] = value;
    }

    bool getVec2(const std::string& key, glm::vec2& v) {
        auto it = vec2s_.find(key);
        if (it != vec2s_.end()) {
            v = it->second;
            return true;
        } else {
            LOGE("Material::getVec2() : %s not found", key.c_str());
            return false;
        }
    }

    virtual void setVec2(const std::string& key, glm::vec2 vector) {
        vec2s_[key] = vector;
    }
    virtual GLUniformBlock* getMatUbo(){}

    virtual const float* getFloatVec(const std::string& key, int numfloats) const
    {
        std::map<std::string, float>::const_iterator it1;
        std::map<std::string, glm::vec2>::const_iterator it2;
        std::map<std::string, glm::vec3>::const_iterator it3;
        std::map<std::string, glm::vec4>::const_iterator it4;
        std::map<std::string, glm::mat4>::const_iterator it5;
        std::map<std::string, int>::const_iterator it6;

        switch (numfloats)
        {
            case 1:
            it1 = floats_.find(key);
            if (it1 != floats_.end())
                return &(it1->second);
            break;

            case 2:
            it2 = vec2s_.find(key);
            if (it2 != vec2s_.end())
                return glm::value_ptr(it2->second);
            break;

            case 3:
            it3 = vec3s_.find(key);
            if (it3 != vec3s_.end())
                return glm::value_ptr(it3->second);
            break;

            case 4:
            it4 = vec4s_.find(key);
            if (it4 != vec4s_.end())
                return glm::value_ptr(it4->second);
            break;

            case 16:
            it5 = mat4s_.find(key);
            if (it5 != mat4s_.end())
                return glm::value_ptr(it5->second);
            break;
        }
        LOGE("SHADER: key %s not found in material %d ", key.c_str(), numfloats);
        return NULL;
    }

    virtual const int* getIntVec(const std::string& key, int numints) const
    {
        if (numints == 1)
        {
            std::map<std::string, int>::const_iterator it1 = ints_.find(key);
            if (it1 != ints_.end())
                return &(it1->second);
        }
        LOGE("SHADER: key %s not found in material %d ", key.c_str(), numints);
        return NULL;
    }

    bool getVec3(const std::string& key, glm::vec3& v) {
        auto it = vec3s_.find(key);
        if (it != vec3s_.end()) {
            v = it->second;
            return true;
        } else {
            LOGE("Material::getVec3() : %s not found", key.c_str());
            return false;
        }
    }

    virtual void setVec3(const std::string& key, glm::vec3 vector) {
        vec3s_[key] = vector;
    }

    bool getVec4(const std::string& key, glm::vec4& v) {
        auto it = vec4s_.find(key);
        if (it != vec4s_.end()) {
            v = it->second;
            return true;
        } else {
            LOGE("Material::getVec4() : %s not found", key.c_str());
            return false;
        }
    }

    virtual void setVec4(const std::string& key, glm::vec4 vector) {
        vec4s_[key] = vector;
    }

    bool getMat4(const std::string& key, glm::mat4 mtx) {
        auto it = mat4s_.find(key);
        if (it != mat4s_.end()) {
            mtx = it->second;
            return true;
        } else {
            LOGE( "Material::getMat4() : %s not found", key.c_str());
            return false;
        }
    }

    const std::map<std::string, Texture*>& getAllTextures()
    {
        return textures_;
    }

    virtual bool hasTexture(const std::string& key) const {
        auto it = textures_.find(key);
        return (it != textures_.end());
    }

    bool hasUniform(const std::string& key) const {
        if (vec3s_.find(key) != vec3s_.end()) {
            return true;
        }
        if (vec2s_.find(key) != vec2s_.end()) {
            return true;
        }
        if (vec4s_.find(key) != vec4s_.end()) {
            return true;
        }
        if (mat4s_.find(key) != mat4s_.end()) {
            return true;
        }
        if (floats_.find(key) != floats_.end()) {
            return true;
        }
        if (ints_.find(key) != ints_.end()) {
            return true;
        }
        return false;
    }

    virtual void setMat4(const std::string& key, glm::mat4 matrix) {
        //LOGE( "Material::setting Mat4() : %s ", key.c_str());
        mat4s_[key] = matrix;
    }

    bool areTexturesReady() {
        for (auto it = textures_.begin(); it != textures_.end(); ++it) {
            Texture* tex = it->second;
            if ((tex == NULL) || !tex->isReady())
            {
                return false;
            }
        }
        return true;
    }

private:
    ShaderData(const ShaderData& post_effect_data);
    ShaderData(ShaderData&& post_effect_data);
    ShaderData& operator=(const ShaderData& post_effect_data);
    ShaderData& operator=(ShaderData&& post_effect_data);

protected:
    int native_shader_;
    std::map<std::string, Texture*> textures_;
    std::map<std::string, float> floats_;
    std::map<std::string, int> ints_;
    std::map<std::string, glm::vec2> vec2s_;
    std::map<std::string, glm::vec3> vec3s_;
    std::map<std::string, glm::vec4> vec4s_;
    std::map<std::string, glm::mat4> mat4s_;
};

}
#endif
