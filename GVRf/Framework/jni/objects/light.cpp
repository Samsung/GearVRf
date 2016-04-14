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

GLTexture* Light::depth_texture_ = NULL;

/*
 * Loads the uniforms associated with this light
 * into the GPU if they have changed.
 * @param program   ID of shader program light is bound ot
 * @param texIndex  next available texture location
 */
void Light::render(int program, int texIndex) {
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

    /*
     * If this light casts shadows and has a shadow map,
     * bind the shadow map texture to the shader and
     * set the shadow_map_index with the index of the map.
     */
    if (castShadow()) {
        GLTexture* shadowmap = depth_texture_;
        if (shadowmap == NULL) {
            depth_texture_ = shadowmap = new GLTexture(GL_TEXTURE_2D_ARRAY);
            LOGD("LIGHT: %s create shadow map array\n", lightID_.c_str());
       }
        int loc = glGetUniformLocation(program, "u_shadow_maps");
        glBindTexture(GL_TEXTURE_2D_ARRAY, depth_texture_->id());
        glActiveTexture(GL_TEXTURE0 + texIndex);
        glUniform1i(loc, shadowMapIndex_);
        floats_[lname + "shadow_map_index"] = (float) shadowMapIndex_;
        LOGD("LIGHT: %s bind shadow map %d\n", lightID_.c_str(), shadowMapIndex_);
    }

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
            LOGD("LIGHT: %s\n", key.c_str());
        }
    }
}

void Light::generateFBO() {
    glBindTexture(GL_TEXTURE_2D_ARRAY, depth_texture_->id());
    glTexImage2D(GL_TEXTURE_2D_ARRAY, shadowMapIndex_, GL_DEPTH_COMPONENT, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 0,
            GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, NULL);
    glGenFramebuffers(1, &fboId_);
    glBindFramebuffer(GL_FRAMEBUFFER, fboId_);
    glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D_ARRAY,
            depth_texture_->id(), shadowMapIndex_);
    const GLenum bufs [ ] = {GL_NONE};
    glDrawBuffers(1, bufs);
    ////////// Check FrameBuffer was created with success ///
    int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Could not create FBO: %d", fboStatus);
        throw std::exception(); // "Could not create FBO: " + fboStatus
    }
    LOGD("LIGHT: %s create shadow map framebuffer %d\n", lightID_.c_str(), shadowMapIndex_);
    ////////// Release bind for texture and FrameBuffer /////
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
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
bool Light::makeShadowMap(Scene* scene, ShaderManager* shader_manager, int texIndex) {
    RenderState rstate;
    int framebufferId = 0;  // TODO: get a valid FB ID

    if (-1 == fboId_  && nullptr == depth_texture_ ) {
        shadowMapIndex_ = texIndex;
    	generateFBO();
    }
    if (shadowMaterial_ == nullptr)
        return false;

    rstate.render_mask;
    // TODO: set viewport based on framebuffer size
    rstate.viewportX = 0;
    rstate.viewportY = 0;
    rstate.viewportWidth = 1024;
    rstate.viewportHeight = 1024;
    rstate.scene = scene;
    rstate.material_override = shadowMaterial_;
    rstate.shader_manager = shader_manager;
    rstate.render_mode = 0;
    // TODO: set up matrix from viewpoint of light
    //rstate.uniforms.u_view = camera->getViewMatrix();
    //rstate.uniforms.u_proj = camera->getProjectionMatrix();

    getMat4("shadow_matrix",rstate.uniforms.u_mvp);
    Renderer::renderShadowMap(rstate, fboId_);
    return true;
}
}
