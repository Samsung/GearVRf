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
#include "glm/gtc/matrix_access.hpp"
#include "engine/renderer/renderer.h"
#include "objects/components/camera.h"

namespace gvr {
const int Light::MAX_SHADOW_MAPS = 2;
const int Light::SHADOW_MAP_SIZE = 1024;
GLTexture* Light::depth_texture_ = NULL;
GLTexture* Light::color_texture_ = NULL;

static const bool LOG_LIGHT = false;

class LightCamera : public Camera
{
    Light* mLight = NULL;
    glm::mat4 mProj;

public:
    LightCamera(Light* light) : Camera()
    {
        mLight = light;
        set_owner_object(light->owner_object());
    }

    virtual glm::mat4 getProjectionMatrix() const
    {
        glm::mat4& proj = (glm::mat4&) mProj;
        if (mLight->getMat4(std::string("projMatrix"), proj))
            return proj;
        return glm::perspective(glm::radians(100.0f), 1.0f, 0.1f, 1000.0f);
    }
};

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
     * If this light implements shadow casting,
     * set the shadow map index.
     */
    if (floats_.find("shadow_map_index") != floats_.end()) {
        floats_["shadow_map_index"] = (float) shadowMapIndex_;
        if (LOG_LIGHT)
            LOGD("LIGHT: %s set shadow map index %d\n", lightID_.c_str(), shadowMapIndex_);
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
        if (LOG_LIGHT)
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
            if (LOG_LIGHT)
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
            if (LOG_LIGHT)
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
            if (LOG_LIGHT)
                LOGD("LIGHT: %s\n", key.c_str());
        }
    }
}

/**
 * If this light casts shadows and has a shadow map,
 * bind the shadow map texture to the shader and
 * set the shadow_map_index with the index of the map.
 */
void Light::bindShadowMap(int program, int texIndex) {
    GLTexture* shadowmap = depth_texture_;
    int loc = glGetUniformLocation(program, "u_shadow_maps");

    if ((loc >= 0) && depth_texture_) {
        //LOGD("LIGHT: found shadow map in shader %d\n", loc);
        if (shadowmap == NULL) {
            std::string error = " Depth Map is not created ";
            throw error;
        }
        else {
            glActiveTexture(GL_TEXTURE0 + texIndex);
            glBindTexture(GL_TEXTURE_2D_ARRAY, depth_texture_->id());
            glPixelStorei(GL_PACK_ALIGNMENT, 1);
        }
        glUniform1i(loc, texIndex);
    }
}

void Light::generateFBO() {

	glGenFramebuffers(1, &fboId_); 
    glBindFramebuffer(GL_FRAMEBUFFER, fboId_);  
    glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                depth_texture_->id(), 0, shadowMapIndex_);

    ////////// Check FrameBuffer was created with success ///
    int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Could not create FBO: %d", fboStatus);
        switch(fboStatus){
        case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT :
        	LOGE("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			break;
        case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
        	LOGE("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
        	break;

        case GL_FRAMEBUFFER_UNSUPPORTED:
        	LOGE("GL_FRAMEBUFFER_UNSUPPORTED");
        	break;
        }
        throw std::exception(); // "Could not create FBO: " + fboStatus
    }
    if (LOG_LIGHT)
        LOGD("LIGHT: %s create shadow map framebuffer %d\n", lightID_.c_str(), shadowMapIndex_);

    ////////// Release bind for texture and FrameBuffer /////
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

}

void Light::createDepthTexture(int width, int height, int depth){
    depth_texture_ = new GLTexture(GL_TEXTURE_2D_ARRAY);
    glBindTexture(GL_TEXTURE_2D_ARRAY, depth_texture_->id());
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
 //   glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGB8, width,height,depth,0,GL_RGB, GL_UNSIGNED_BYTE,NULL);
 //   glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_R16F, width,height,depth,0,GL_RED, GL_HALF_FLOAT,NULL);  // it does not for S6 edge
    glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGB10_A2, width,height,depth,0,GL_RGBA, GL_UNSIGNED_INT_2_10_10_10_REV,NULL);
    glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
}

/**
 * Renders the shadow map for this light.
 * @param scene             Scene to use for rendering
 * @param shader_manager    ShaderManager to use
 * @param texIndex          texture index for shadow map
 * @param scene_objects     temporary storage for culling
 */
bool Light::makeShadowMap(Scene* scene, ShaderManager* shader_manager, int texIndex, std::vector<SceneObject*>& scene_objects, int viewport_width, int viewport_height) {

    int framebufferId = 0;  // TODO: get a valid FB ID
    if (shadowMaterial_ == nullptr)
        return false;

    if (nullptr == depth_texture_) {
    	createDepthTexture(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 4);
    }

    if (-1 == fboId_) {
        shadowMapIndex_ = texIndex;
    	generateFBO();
    }

    LightCamera lightcam(this);
    RenderState rstate;
    rstate.viewportX = 0;
    rstate.viewportY = 0;
    rstate.viewportWidth = viewport_width;
    rstate.viewportHeight = viewport_height;
    rstate.scene = scene;
    rstate.material_override = shadowMaterial_;
    rstate.shader_manager = shader_manager;
    rstate.uniforms.u_view = lightcam.getViewMatrix();
    rstate.uniforms.u_proj = lightcam.getProjectionMatrix();
    rstate.render_mask = 1;
    Renderer::renderShadowMap(rstate, &lightcam, fboId_, scene_objects);
    return true;
}
}
