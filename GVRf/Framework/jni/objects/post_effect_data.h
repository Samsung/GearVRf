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

#ifndef COLOR_BLEND_POST_EFFECT_H_
#define COLOR_BLEND_POST_EFFECT_H_

#include <map>
#include <memory>
#include <string>

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/hybrid_object.h"

namespace gvr {
class Texture;

class PostEffectData: public HybridObject {
public:
    enum ShaderType {
        COLOR_BLEND_SHADER = 0, HORIZONTAL_FLIP_SHADER = 1,
    };

    PostEffectData(ShaderType shader_type) :
            shader_type_(shader_type), textures_(), floats_(), vec2s_(), vec3s_(), vec4s_(), mat4s_() {
        switch (shader_type) {
        case COLOR_BLEND_SHADER:
            floats_["r"] = 0.0f;
            floats_["g"] = 0.0f;
            floats_["b"] = 0.0f;
            floats_["factor"] = 0.0f;
            break;
        }
    }

    ~PostEffectData() {
    }

    ShaderType shader_type() {
        return shader_type_;
    }

    void set_shader_type(ShaderType shader_type) {
        shader_type_ = shader_type;
    }

    Texture* getTexture(std::string key) const {
        auto it = textures_.find(key);
        if (it != textures_.end()) {
            return it->second;
        } else {
            std::string error = "PostEffectData::getTexture() : " + key
                    + " not found";
            throw error;
        }
    }

    void setTexture(std::string key, Texture* texture) {
        textures_[key] = texture;
    }

    float getFloat(std::string key) {
        auto it = floats_.find(key);
        if (it != floats_.end()) {
            return it->second;
        } else {
            std::string error = "PostEffectData::getFloat() : " + key
                    + " not found";
            throw error;
        }
    }

    void setFloat(std::string key, float value) {
        floats_[key] = value;
    }

    glm::vec2 getVec2(std::string key) {
        auto it = vec2s_.find(key);
        if (it != vec2s_.end()) {
            return it->second;
        } else {
            std::string error = "PostEffectData::getVec2() : " + key
                    + " not found";
            throw error;
        }
    }

    void setVec2(std::string key, glm::vec2 vector) {
        vec2s_[key] = vector;
    }

    glm::vec3 getVec3(std::string key) {
        auto it = vec3s_.find(key);
        if (it != vec3s_.end()) {
            return it->second;
        } else {
            std::string error = "PostEffectData::getVec3() : " + key
                    + " not found";
            throw error;
        }
    }

    void setVec3(std::string key, glm::vec3 vector) {
        vec3s_[key] = vector;
    }

    glm::vec4 getVec4(std::string key) {
        auto it = vec4s_.find(key);
        if (it != vec4s_.end()) {
            return it->second;
        } else {
            std::string error = "PostEffectData::getVec4() : " + key
                    + " not found";
            throw error;
        }
    }

    void setVec4(std::string key, glm::vec4 vector) {
        vec4s_[key] = vector;
    }

    glm::mat4 getMat4(std::string key) {
        auto it = mat4s_.find(key);
        if (it != mat4s_.end()) {
            return it->second;
        } else {
            std::string error = "PostEffectData::getMat4() : " + key
                    + " not found";
            throw error;
        }
    }

    void setMat4(std::string key, glm::mat4 mat) {
        mat4s_[key] = mat;
    }

private:
    PostEffectData(const PostEffectData& post_effect_data);
    PostEffectData(PostEffectData&& post_effect_data);
    PostEffectData& operator=(const PostEffectData& post_effect_data);
    PostEffectData& operator=(PostEffectData&& post_effect_data);

private:
    ShaderType shader_type_;
    std::map<std::string, Texture*> textures_;
    std::map<std::string, float> floats_;
    std::map<std::string, glm::vec2> vec2s_;
    std::map<std::string, glm::vec3> vec3s_;
    std::map<std::string, glm::vec4> vec4s_;
    std::map<std::string, glm::mat4> mat4s_;
};

}
#endif
