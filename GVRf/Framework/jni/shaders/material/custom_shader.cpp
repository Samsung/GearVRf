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
 * A shader which an user can add in run-time.
 ***************************************************************************/

#include "custom_shader.h"
#include "engine/renderer/renderer.h"
#include "gl/gl_program.h"
#include "objects/material.h"
#include "objects/mesh.h"
#include "objects/textures/texture.h"
#include "objects/components/render_data.h"
#include "util/gvr_gl.h"

#include <sys/time.h>

namespace gvr {
CustomShader::CustomShader(std::string vertex_shader,
        std::string fragment_shader) :
        program_(0), u_mvp_(0), u_mv_(0), u_right_(0), u_view_(0),
        texture_keys_(), attribute_float_keys_(),
        attribute_vec2_keys_(), attribute_vec3_keys_(), attribute_vec4_keys_(),
        uniform_float_keys_(), uniform_vec2_keys_(), uniform_vec3_keys_(),
        uniform_vec4_keys_(), uniform_mat4_keys_() {
    program_ = new GLProgram(vertex_shader.c_str(), fragment_shader.c_str());
    u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    u_view_ = glGetUniformLocation(program_->id(), "u_view");
    u_mv_ = glGetUniformLocation(program_->id(), "u_mv");
    u_mv_it_ = glGetUniformLocation(program_->id(), "u_mv_it");
    u_right_ = glGetUniformLocation(program_->id(), "u_right");
}

CustomShader::~CustomShader() {
    delete program_;
}

void CustomShader::addTextureKey(std::string variable_name, std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    texture_keys_[location] = key;
}

void CustomShader::addAttributeFloatKey(std::string variable_name,
        std::string key) {
    int location = glGetAttribLocation(program_->id(), variable_name.c_str());
    attribute_float_keys_[location] = key;
}

void CustomShader::addAttributeVec2Key(std::string variable_name,
        std::string key) {
    int location = glGetAttribLocation(program_->id(), variable_name.c_str());
    attribute_vec2_keys_[location] = key;
}

void CustomShader::addAttributeVec3Key(std::string variable_name,
        std::string key) {
    int location = glGetAttribLocation(program_->id(), variable_name.c_str());
    attribute_vec3_keys_[location] = key;
}

void CustomShader::addAttributeVec4Key(std::string variable_name,
        std::string key) {
    int location = glGetAttribLocation(program_->id(), variable_name.c_str());
    attribute_vec4_keys_[location] = key;
}

void CustomShader::addUniformFloatKey(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    uniform_float_keys_[location] = key;
}

void CustomShader::addUniformVec2Key(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    uniform_vec2_keys_[location] = key;
}

void CustomShader::addUniformVec3Key(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    uniform_vec3_keys_[location] = key;
}

void CustomShader::addUniformVec4Key(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    uniform_vec4_keys_[location] = key;
}

void CustomShader::addUniformMat4Key(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    uniform_mat4_keys_[location] = key;
}

void CustomShader::render(const ShaderUniformsPerObject& uniforms, RenderData* render_data,
        const std::vector<Light*> lightList, Material* material) {
    for (auto it = texture_keys_.begin(); it != texture_keys_.end(); ++it) {
        Texture* texture = material->getTextureNoError(it->second);
        // If any texture is not ready, do not render the material at all
        if (texture == NULL || !texture->isReady()) {
             return;
        }
    }

    Mesh* mesh = render_data->mesh();

    glUseProgram(program_->id());

    /*
     * Update the uniforms for the lights
     */
    for (auto it = lightList.begin();
         it != lightList.end();
         ++it) {
        Light* light = (*it);
         if (light != NULL)
            light->render(program_->id());
    }
    /*
     * Update the bone matrices
     */
    int a_bone_indices = glGetAttribLocation(program_->id(), "a_bone_indices");
    int a_bone_weights = glGetAttribLocation(program_->id(), "a_bone_weights");
    int u_bone_matrices = glGetUniformLocation(program_->id(), "u_bone_matrix[0]");
    if ((a_bone_indices >= 0) ||
        (a_bone_weights >= 0) ||
        (u_bone_matrices >= 0)) {
        glm::mat4 finalTransform;
        mesh->setBoneLoc(a_bone_indices, a_bone_weights);
        mesh->generateBoneArrayBuffers();
        int nBones = mesh->getVertexBoneData().getNumBones();
        if (nBones > MAX_BONES)
            nBones = MAX_BONES;
        for (int i = 0; i < nBones; ++i) {
            finalTransform = mesh->getVertexBoneData().getFinalBoneTransform(i);
            glUniformMatrix4fv(u_bone_matrices + i, 1, GL_FALSE, glm::value_ptr(finalTransform));
        }
    }
    /*
     * Update vertex information
     */
    if (mesh->isVaoDirty()) {
        mesh->bindVertexAttributes(program_->id());
		mesh->unSetVaoDirty();
    }
    mesh->generateVAO();  // setup VAO

    ///////////// uniform /////////
    for (auto it = uniform_float_keys_.begin(); it != uniform_float_keys_.end();
            ++it) {
        glUniform1f(it->first, material->getFloat(it->second));
    }

    if (u_mvp_ != -1) {
        glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(uniforms.u_mvp));
    }
    if (u_view_ != -1) {
        glUniformMatrix4fv(u_view_, 1, GL_FALSE, glm::value_ptr(uniforms.u_view));
    }
    if (u_mv_ != -1) {
        glUniformMatrix4fv(u_mv_, 1, GL_FALSE, glm::value_ptr(uniforms.u_mv));
    }
    if (u_mv_it_ != -1) {
        glUniformMatrix4fv(u_mv_it_, 1, GL_FALSE, glm::value_ptr(uniforms.u_mv_it));
    }
    if (u_right_ != 0) {
        glUniform1i(u_right_, uniforms.u_right ? 1 : 0);
    }

    int texture_index = 0;
    for (auto it = texture_keys_.begin(); it != texture_keys_.end(); ++it) {
        glActiveTexture(getGLTexture(texture_index));
        Texture* texture = material->getTexture(it->second);
        glBindTexture(texture->getTarget(), texture->getId());
        glUniform1i(it->first, texture_index++);
    }

    for (auto it = uniform_vec2_keys_.begin(); it != uniform_vec2_keys_.end();
            ++it) {
        glm::vec2 v = material->getVec2(it->second);
        glUniform2f(it->first, v.x, v.y);
    }

    for (auto it = uniform_vec3_keys_.begin(); it != uniform_vec3_keys_.end();
            ++it) {
        glm::vec3 v = material->getVec3(it->second);
        glUniform3f(it->first, v.x, v.y, v.z);
    }

    for (auto it = uniform_vec4_keys_.begin(); it != uniform_vec4_keys_.end();
            ++it) {
        glm::vec4 v = material->getVec4(it->second);
        glUniform4f(it->first, v.x, v.y, v.z, v.w);
    }

    for (auto it = uniform_mat4_keys_.begin(); it != uniform_mat4_keys_.end();
            ++it) {
        glm::mat4 m = material->getMat4(it->second);
        glUniformMatrix4fv(it->first, 1, GL_FALSE, glm::value_ptr(m));
    }

    glBindVertexArray(mesh->getVAOId());
    glDrawElements(render_data->draw_mode(), mesh->indices().size(), GL_UNSIGNED_SHORT, 0);
    glBindVertexArray(0);

    checkGlError("CustomShader::render");
}

int CustomShader::getGLTexture(int n) {
    switch (n) {
    case 0:
        return GL_TEXTURE0;
    case 1:
        return GL_TEXTURE1;
    case 2:
        return GL_TEXTURE2;
    case 3:
        return GL_TEXTURE3;
    case 4:
        return GL_TEXTURE4;
    case 5:
        return GL_TEXTURE5;
    case 6:
        return GL_TEXTURE6;
    case 7:
        return GL_TEXTURE7;
    case 8:
        return GL_TEXTURE8;
    case 9:
        return GL_TEXTURE9;
    case 10:
        return GL_TEXTURE10;
    default:
        return GL_TEXTURE0;
    }
}

} /* namespace gvr */
