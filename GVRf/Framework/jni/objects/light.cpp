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
 * JNI
 ***************************************************************************/
#include <assert.h>
#include "light.h"
#include "util/gvr_gl.h"
#include "util/gvr_log.h"
#include <string>
#include <sstream>
#include <vector>
#include "glm/gtc/type_ptr.hpp"
#include "engine/renderer/renderer.h"
#include "shaders/posteffect/shadow_shader.h"

namespace gvr {


/*
 * Loads the uniforms associated with this light
 * into the GPU if they have changed.
 */
void Light::render(int program) {
    auto it = dirty_.find(program);

    if (it != dirty_.end() && !it->second)
        return;
    if (lightID_.empty()) {
        return;
    }
    dirty_[program] = false;
    std::string key;
    std::string lname = lightID_ + ".";
    std::stringstream ss;
    int offset;

    for (auto it = floats_.begin(); it != floats_.end(); ++it) {
        key = lname + it->first;
        offset = getOffset(it->first, program);
        if (offset <= 0) {
            offset = glGetUniformLocation(program, key.c_str());
            offsets_[it->first][program] = offset;
        }
        if (offset >= 0)
            glUniform1f(offset, it->second);
            LOGD("LIGHT: %s = %f\n", key.c_str(), it->second);
    }

    for (auto it = vec3s_.begin();
         it != vec3s_.end(); ++it) {
        offset = getOffset(it->first, program);
        key = lname + it->first;
        if (offset <= 0) {
            offset = glGetUniformLocation(program, key.c_str());
            offsets_[it->first][program] = offset;
          }
        if (offset >= 0) {
            glm::vec3 v = it->second;
            glUniform3f(offset, v.x, v.y, v.z);
            LOGD("LIGHT: %s = %f, %f, %f\n", key.c_str(), v.x, v.y, v.z);
        }
    }

    for (auto it = vec4s_.begin();
            it != vec4s_.end(); ++it) {
        offset = getOffset(it->first, program);
        key = lname + it->first;
        if (offset <= 0) {
            offset = glGetUniformLocation(program, key.c_str());
            offsets_[it->first][program] = offset;
        }
        if (offset >= 0) {
            glm::vec4 v = it->second;
            glUniform4f(offset, v.x, v.y, v.z, v.w);
            LOGD("LIGHT: %s = %f, %f, %f, %f\n", key.c_str(), v.x, v.y, v.z, v.w);
        }
    }
    for (auto it = mat4s_.begin();
            it != mat4s_.end(); ++it) {
        offset = getOffset(it->first, program);
        key = lname + it->first;
        if (offset <= 0) {
            offset = glGetUniformLocation(program, key.c_str());
            offsets_[it->first][program] = offset;
        }
        if (offset >= 0) {
            glm::mat4 v = it->second;
            glUniformMatrix4fv(offset, 1, GL_FALSE, glm::value_ptr(v));
        }
    }
}

// This is the code from renderer.cpp that renders the shadow maps
// shader_manager->getShadowShader()->setCameraLight(cameraLight);
// shader_manager->getShadowShader()->updateViewportInfo(viewportWidth, viewportHeight);
// renderCamera(scene, camera,
//         shader_manager->getShadowShader()->getFBOFromLight(),
//         0, 0, viewportWidth, viewportHeight, shader_manager,
//         post_effect_shader_manager, post_effect_render_texture_a,
//         post_effect_render_texture_b, ShadowShader::RENDER_FROM_LIGHT);

// This function should replace the code above
void Light::makeShadowMap(Scene* scene, ShaderManager* shader_manager) {
    RenderState rstate;
    int framebufferId = 0;  // TODO: get a valid FB ID

    if (shadowMaterial_ == NULL)
        return;
    rstate.render_mask;
    // TODO: set viewport based on framebuffer size
    //rstate.viewportX;
    //rstate.viewportY;
    //rstate.viewportWidth;
    //rstate.viewportHeight;
    rstate.scene = scene;
    rstate.material_override = shadowMaterial_;
    rstate.shader_manager = shader_manager;
    rstate.render_mode = ShadowShader::RENDER_FROM_LIGHT;
    // TODO: set up matrix from viewpoint of light
    //rstate.uniforms.u_view = camera->getViewMatrix();
    //rstate.uniforms.u_proj = camera->getProjectionMatrix();

    Renderer::renderShadowMap(rstate, framebufferId);
}
}
