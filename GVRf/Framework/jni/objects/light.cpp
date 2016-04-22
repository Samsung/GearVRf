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
const int Light::MAX_SHADOW_MAPS = 2;
const int Light::SHADOW_MAP_SIZE = 1024;
GLTexture* Light::depth_texture_ = NULL;
GLTexture* Light::color_texture_ = NULL;

class LightCamera : public Camera
{
public:
    LightCamera(Light* light) : Camera()
    {
        set_owner_object(light->owner_object());
    }

    virtual glm::mat4 getProjectionMatrix() const
    {
        glm::mat4 ortho;
        float left = -1.0f;
        float right = 1.0f;
        float top = 1.0f;
        float bottom = -1.0f;
        float near = -1.0f;
        float far = 60.0f;

        ortho[0][0]= 2.0 / (right - left);
        ortho[1][1]= 2.0 / (top - bottom);
        ortho[2][2]= -2.0 / (far-near);
        ortho[3][0]= -(right + left)/(right-left);
        ortho[3][1]=-(top+bottom)/(top-bottom);
        ortho[3][2]= -(far+near)/(far-near);
        return ortho;
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

    GLuint loc = glGetUniformLocation(program, "shadow_matrix");
	if (loc != -1 && shadowMaterial_!= nullptr) {
		glm::mat4 shadow_matrix;
		getMat4("shadow_matrix", shadow_matrix);
		glUniformMatrix4fv(loc, 1, GL_FALSE, glm::value_ptr((shadow_matrix)));
	}
    /*
     * If this light casts shadows and has a shadow map,
     * bind the shadow map texture to the shader and
     * set the shadow_map_index with the index of the map.
     */
    if (castShadow()) {
        floats_["shadow_map_index"] = (float) shadowMapIndex_;
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

/*
 * If this light casts shadows and has a shadow map,
 * bind the shadow map texture to the shader and
 * set the shadow_map_index with the index of the map.
 */
void Light::bindShadowMaps(int program, int texIndex) {
    GLTexture* shadowmap = depth_texture_;
    int loc = glGetUniformLocation(program, "u_shadow_maps");
    checkGLError("in bind shadowmaps");
    if (loc >= 0) {
        LOGD("LIGHT: found shadow map in shader %d\n", loc);
        if (shadowmap == NULL) {
        	LOGE("shadowmap null");
            depth_texture_ = shadowmap = new GLTexture(GL_TEXTURE_2D_ARRAY);
            LOGD("LIGHT: create shadow map array id = %d\n", depth_texture_->id());
            glActiveTexture(GL_TEXTURE0 + texIndex);
            glBindTexture(GL_TEXTURE_2D_ARRAY, depth_texture_->id());
            glTexStorage3D(GL_TEXTURE_2D_ARRAY, MAX_SHADOW_MAPS, GL_DEPTH_COMPONENT16, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 1);
            checkGlError("Light::render create shadow map");
        }
        else {

            glActiveTexture(GL_TEXTURE0 + texIndex);
            checkGLError("glBindTexture");
            glBindTexture(GL_TEXTURE_2D, depth_texture_->id());
            checkGLError("glBindTexture");

            glPixelStorei(GL_PACK_ALIGNMENT, 1);
            checkGLError("glBindTexture");

        }

        glUniform1i(loc, texIndex);
        checkGlError("Light::render bind shadow map");

    }
}
/*
void Light::generateFBO() {

	GLTexture* colorTexture = new GLTexture(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, colorTexture->id());
    checkGLError("colorTexture");
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 1024, 1024, 0, GL_RGB,
            GL_UNSIGNED_BYTE, NULL);
    checkGLError("glTexImage2D color");
    glBindTexture(GL_TEXTURE_2D, 0);

GLenum attachments[2]={GL_NONE};
    glGenFramebuffers(1, &fboId_);
    checkGLError("glGenFramebuffers");
    glBindFramebuffer(GL_FRAMEBUFFER, fboId_);
    checkGLError("glBindFramebuffer");
  //  glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
  //                      GL_TEXTURE_2D, colorTexture->id(), 0);


 //   glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
 //           color_texture_->id(), 0, shadowMapIndex_);
    glDrawBuffers(1,attachments);
    checkGLError("colorattachment");
    glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
            depth_texture_->id(), 0, shadowMapIndex_);
    checkGLError("glFramebufferTextureLayer");

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
    LOGD("LIGHT: %s create shadow map framebuffer %d\n", lightID_.c_str(), shadowMapIndex_);
    ////////// Release bind for texture and FrameBuffer /////
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}


void Light::createDepthTexture(int width, int height, int depth){
	LOGE("creating depth texture");
	depth_texture_  = new GLTexture(GL_TEXTURE_2D_ARRAY);
    glBindTexture(GL_TEXTURE_2D_ARRAY, depth_texture_->id());
    checkGLError("glBindTexture");
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    checkGLError("glBindTexture");
 //   glTexStorage3D(GL_TEXTURE_2D_ARRAY, 0, GL_DEPTH_COMPONENT16, width, height, depth);
    glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_DEPTH_COMPONENT16, width,height,depth,0,GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT,NULL);
    checkGlError("Light::render create shadow map");
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_NONE);

       glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    checkGlError("Light::render create shadow map");
    glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_WRAP_S, GL_REPEAT);
    checkGlError("Light::render create shadow map");
    glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_WRAP_T, GL_REPEAT);
    checkGlError("Light::render create shadow map");
    glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

    color_texture_ = new GLTexture(GL_TEXTURE_2D_ARRAY);
    glBindTexture(GL_TEXTURE_2D_ARRAY, color_texture_->id());
    checkGLError("glBindTexture 2");
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    checkGLError("glBindTexture");
    glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGB8, width,height,depth,0,GL_RGB, GL_UNSIGNED_BYTE,NULL);
    checkGlError("Light::render create shadow map");

    glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
}

*/


void Light::generateFBO() {
	LOGE("generate FBO");

    glBindTexture(GL_TEXTURE_2D, 0);
    glGenFramebuffers(1, &fboId_);
    checkGLError("glGenFramebuffers");
    glBindFramebuffer(GL_FRAMEBUFFER, fboId_);
    checkGLError("glBindFramebuffer");
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth_texture_->id(), 0);
    checkGLError("glFramebufferTextureLayer");

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
    LOGD("LIGHT: %s create shadow map framebuffer %d\n", lightID_.c_str(), shadowMapIndex_);
    ////////// Release bind for texture and FrameBuffer /////
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}


void Light::createDepthTexture(int width, int height, int depth){
	LOGE("creating depth texture");
	depth_texture_  = new GLTexture(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, depth_texture_->id());
    checkGLError("glBindTexture");
    glTexImage2D(GL_TEXTURE_2D, 0,GL_DEPTH_COMPONENT, 1024, 1024, 0,GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, 0);
    checkGlError("Light::glTexImage2D");
    glBindTexture(GL_TEXTURE_2D, 0);

}

/**
 * Renders the shadow map for this light
 * @param scene             Scene to use for rendering
 * @param shader_manager    ShaderManager to use
 * @param texIndex          texture index for shadow map
 * @param scene_objects     temporary storage for culling
 */
bool Light::makeShadowMap(Scene* scene, ShaderManager* shader_manager, int texIndex, std::vector<SceneObject*>& scene_objects) {

    int framebufferId = 0;  // TODO: get a valid FB ID
    if (shadowMaterial_ == nullptr)
        return false;

    if(nullptr == depth_texture_){
    	createDepthTexture(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 2);
    }

    if (-1 == fboId_ ) {
        shadowMapIndex_ = texIndex;
    	generateFBO();
    }

    LightCamera lightcam(this);
    RenderState rstate;
    rstate.viewportX = 0;
    rstate.viewportY = 0;
    rstate.viewportWidth = 1024;
    rstate.viewportHeight = 1024;
    rstate.scene = scene;
    rstate.material_override = shadowMaterial_;
    rstate.shader_manager = shader_manager;
    rstate.render_mode = 0;
    rstate.uniforms.u_model = owner_object()->transform()->getModelMatrix();
    rstate.uniforms.u_view = lightcam.getViewMatrix();
    rstate.uniforms.u_proj = lightcam.getProjectionMatrix();
    rstate.render_mask = 0;
    Renderer::renderShadowMap(rstate, &lightcam, fboId_, scene_objects);
    return true;
}
}
