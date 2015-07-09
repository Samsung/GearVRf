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
 * Class containing light source parameters.
 ***************************************************************************/

#ifndef LIGHT_H_
#define LIGHT_H_

#include <map>
#include <memory>
#include <string>

#include "glm/glm.hpp"

#include "objects/hybrid_object.h"

namespace gvr {
class Color;

class Light: public HybridObject {
public:
    explicit Light() : enabled_(true) {
    }

    ~Light() {
    }

    bool enabled() {
        return enabled_;
    }

    void enable() {
        enabled_ = true;
    }

    void disable() {
        enabled_ = false;
    }

    float getFloat(std::string key) {
        auto it = floats_.find(key);
        if (it != floats_.end()) {
            return it->second;
        } else {
            std::string error = "Light::getFloat() : " + key + " not found";
            throw error;
        }
    }
    void setFloat(std::string key, float value) {
        floats_[key] = value;
    }

    glm::vec3 getVec3(std::string key) {
        auto it = vec3s_.find(key);
        if (it != vec3s_.end()) {
            return it->second;
        } else {
            std::string error = "Light::getVec3() : " + key + " not found";
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
            std::string error = "Light::getVec4() : " + key + " not found";
            throw error;
        }
    }

    void setVec4(std::string key, glm::vec4 vector) {
        vec4s_[key] = vector;
    }

private:
    Light(const Light& light);
    Light(Light&& light);
    Light& operator=(const Light& light);
    Light& operator=(Light&& light);

private:
    bool enabled_;
    std::map<std::string, float> floats_;
    std::map<std::string, glm::vec3> vec3s_;
    std::map<std::string, glm::vec4> vec4s_;
};
}
#endif
