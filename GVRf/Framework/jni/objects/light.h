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

    // ========== LIGHT parameters for shadow calculations ==========
    // TODO: Ported from previous DirectionalLight class for compatibility reasons.
    //       In the future, this will be moved to the appropriate class.
    //

    enum LightType {
        POINT = 0, SPOT = 1, DIRECTIONAL = 2
    };

    enum ShadowMapHandlerMode {
        HIDE = 0, SHOW = 1, GRADIENT = 2,
    };

    enum ShadowMapHandlerEdges {
        HANDLER_EDGES_NONE = 0, HANDLER_EDGES_DARK = 1, HANDLER_EDGES_LIGHT = 2,
    };

    glm::vec3 getLightDirection() const {
        return light_direction_;
    }

    void setLightDirection(glm::vec3 light_direction) {
        light_direction_ = light_direction;
    }

    void setSpotangle(float angle) {
        spotangle = angle;
    }

    float getSpotangle() const {
        return spotangle;
    }

    void setOverwriteTextureRender(bool enable) {
        overwriteTextureRender = enable;
    }

    float isOverwriteTextureRender() const {
        return overwriteTextureRender;
    }

    int getLightType() const {
        return type;
    }

    void setLightType(int light_type) {
        type = light_type;
    }

    void setShadowMapHandlerMode(ShadowMapHandlerMode mode) {
        shadowmap_handler = mode;
    }

    ShadowMapHandlerMode getShadowMapHandlerMode() const {
        return shadowmap_handler;
    }

    float getShadowSmoothSize() const {
        return shadow_smooth_size;
    }

    void setShadowSmoothSize(float size) {
        shadow_smooth_size = size;
    }

    void setShadowMapHandlerEdges(ShadowMapHandlerEdges mode) {
        shadowmap_handler_edges = mode;
    }

    ShadowMapHandlerEdges getShadowMapHandlerEdges() const {
        return shadowmap_handler_edges;
    }

    float getShadowMapEdgesLength() const {
        return shadowmap_edges_length;
    }

    void setShadowMapEdgesLength(float value) {
        shadowmap_edges_length = value;
    }

    float getLightAmbientOnShadow() const {
        return light_ambient_on_shadow;
    }

    void setLightAmbientOnShadow(float value) {
        light_ambient_on_shadow = value;
    }

    float getShadowGradientCenter() const {
        return shadow_gradient_center;
    }

    void setShadowGradientCenter(float value) {
        shadow_gradient_center = value;
    }

    float getLightingShade() const {
        return u_lighting_shade;
    }

    void setLightingShade(float value) {
        u_lighting_shade = value;
    }

    float getBoardStratifiedSampling() const {
        return u_stratified_sampling;
    }

    void setBoardStratifiedSampling(float value) {
        u_stratified_sampling = value;
    }

    float getShadowSmoothDistance() const {
        return u_shadow_smooth_distance;
    }

    void setShadowSmoothDistance(float value) {
        u_shadow_smooth_distance = value;
    }

    float getBias() const {
        return bias;
    }

    void setBias(float value) {
        bias = value;
    }

private:
    glm::vec3 light_position_;
    glm::vec3 light_direction_ = glm::vec3(0);

    int type = POINT;

    bool overwriteTextureRender = false;
    float spotangle = 100;
    ShadowMapHandlerMode shadowmap_handler = HIDE;
    ShadowMapHandlerEdges shadowmap_handler_edges = HANDLER_EDGES_DARK;
    float shadow_smooth_size = 0.0;
    float shadowmap_edges_length = 0.1;
    float light_ambient_on_shadow = 0.1;
    float shadow_gradient_center = 2.0;
    float u_lighting_shade = 0.0;
    float u_stratified_sampling = 1.0;
    float u_shadow_smooth_distance = 1.0;
    float bias = 0.001;
    // ========== LIGHT parameters END ==========

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
