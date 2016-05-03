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
 * Directional light for shadow rendering.
 ***************************************************************************/

#ifndef DIRECTIONAL_LIGHT_H_
#define DIRECTIONAL_LIGHT_H_

#include "objects/components/orthogonal_camera.h"
#include "glm/glm.hpp"

namespace gvr {

class DirectionalLight {

public:

    enum LightRenderMode {
        PERSPECTIVE = 0, ORTOGONAL = 1,
    };

    enum ShadowMapHandlerMode {
        HIDE = 0, SHOW = 1, GRADIENT = 2,
    };

    enum ShadowMapHandlerEdges {
        HANDLER_EDGES_NONE = 0, HANDLER_EDGES_DARK = 1, HANDLER_EDGES_LIGHT = 2,
    };

    DirectionalLight() {
        light_position_ = glm::vec3(0.0f);
        light_direction_ = glm::vec3(0.0f);
    }

    ~DirectionalLight() {
    }

    glm::vec3 getLightPosition() const {
        return light_position_;
    }

    void setLightPosition(glm::vec3 light_position) {
        light_position_ = light_position;
    }

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

    LightRenderMode getRenderMode() const {
        return mode;
    }

    void setRenderMode(LightRenderMode type) {
        mode = type;
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
    glm::vec3 light_direction_;

    bool overwriteTextureRender = false;
    float spotangle = 100;
    LightRenderMode mode = PERSPECTIVE;
    ShadowMapHandlerMode shadowmap_handler = HIDE;
    ShadowMapHandlerEdges shadowmap_handler_edges = HANDLER_EDGES_NONE;
    float shadow_smooth_size;
    float shadowmap_edges_length = 0.01;
    float light_ambient_on_shadow = 0.1;
    float shadow_gradient_center = 0.0;
    float u_lighting_shade = 0.0;
    float u_stratified_sampling;
    float u_shadow_smooth_distance;
    float bias;
};
}

#endif
