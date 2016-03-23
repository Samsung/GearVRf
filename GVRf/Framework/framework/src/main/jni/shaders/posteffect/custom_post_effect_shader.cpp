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
 * A user-made shader for a post effects.
 ***************************************************************************/

#include "custom_post_effect_shader.h"

#include "gl/gl_program.h"
#include "objects/post_effect_data.h"
#include "objects/components/render_data.h"
#include "objects/textures/render_texture.h"
#include "util/gvr_gl.h"


namespace gvr {
CustomPostEffectShader::CustomPostEffectShader(std::string vertex_shader,
        std::string fragment_shader) :
        program_(0), a_position_(0), a_tex_coord_(0), u_texture_(0), texture_keys_(), float_keys_(), vec2_keys_(), vec3_keys_(), vec4_keys_(), mat4_keys_() {
    deleter_ = getDeleterForThisThread();

    program_ = new GLProgram(vertex_shader.c_str(), fragment_shader.c_str());
    a_position_ = glGetAttribLocation(program_->id(), "a_position");
    checkGlError("glGetAttribLocation");
    a_tex_coord_ = glGetAttribLocation(program_->id(), "a_tex_coord");
    checkGlError("glGetAttribLocation");
    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
    checkGlError("glGetUniformLocation");
    u_projection_matrix_ = glGetUniformLocation(program_->id(), "u_projection_matrix");
    checkGlError("glGetUniformLocation");
    u_right_eye_ = glGetUniformLocation(program_->id(), "u_right_eye");
    checkGlError("glGetUniformLocation");

    vaoID_ = 0;

}

CustomPostEffectShader::~CustomPostEffectShader() {
    delete program_;

    if (vaoID_ != 0) {
        deleter_->queueVertexArray(vaoID_);
    }
}

void CustomPostEffectShader::addTextureKey(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    texture_keys_[location] = key;
}

void CustomPostEffectShader::addFloatKey(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    float_keys_[location] = key;
}
void CustomPostEffectShader::addVec2Key(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    vec2_keys_[location] = key;
}

void CustomPostEffectShader::addVec3Key(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    vec3_keys_[location] = key;
}

void CustomPostEffectShader::addVec4Key(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    vec4_keys_[location] = key;
}

void CustomPostEffectShader::addMat4Key(std::string variable_name,
        std::string key) {
    int location = glGetUniformLocation(program_->id(), variable_name.c_str());
    mat4_keys_[location] = key;
}

void CustomPostEffectShader::render(Camera* camera,
        RenderTexture* render_texture,
        PostEffectData* post_effect_data,
        std::vector<glm::vec3>& vertices, std::vector<glm::vec2>& tex_coords,
        std::vector<unsigned short>& triangles) {
    glUseProgram(program_->id());

    GLuint tmpID;

    if(vaoID_ == 0)
    {
        glGenVertexArrays(1, &vaoID_);
        glBindVertexArray(vaoID_);

        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(unsigned short)*triangles.size(), &triangles[0], GL_STATIC_DRAW);

        if (vertices.size())
        {
            glGenBuffers(1, &tmpID);
            glBindBuffer(GL_ARRAY_BUFFER, tmpID);
            glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec3)*vertices.size(), &vertices[0], GL_STATIC_DRAW);
            glEnableVertexAttribArray(a_position_);
            glVertexAttribPointer(a_position_, 3, GL_FLOAT, 0, 0, 0);
        }

        if (tex_coords.size())
        {
            glGenBuffers(1, &tmpID);
            glBindBuffer(GL_ARRAY_BUFFER, tmpID);
            glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec2)*tex_coords.size(), &tex_coords[0], GL_STATIC_DRAW);
            glEnableVertexAttribArray(a_tex_coord_);
            glVertexAttribPointer(a_tex_coord_, 2, GL_FLOAT, 0, 0, 0);
        }
    }

    int texture_index = 0;
    if (u_texture_ != -1) {
        glActiveTexture(getGLTexture(texture_index));
        glBindTexture(GL_TEXTURE_2D, render_texture->getId());
        glUniform1i(u_texture_, texture_index++);
    }

    if (u_projection_matrix_ != -1) {
        glm::mat4 view = camera->getViewMatrix();
        glUniformMatrix4fv(u_projection_matrix_, 1, GL_TRUE, glm::value_ptr(view));
    }

    if (u_right_eye_ != -1) {
        bool right = camera->render_mask() & RenderData::RenderMaskBit::Right;
        glUniform1i(u_right_eye_, right ? 1 : 0);
    }

    for (auto it = texture_keys_.begin(); it != texture_keys_.end(); ++it) {
        glActiveTexture(getGLTexture(texture_index));
        Texture* texture = post_effect_data->getTexture(it->second);
        glBindTexture(texture->getTarget(), texture->getId());
        glUniform1i(it->first, texture_index++);
    }

    for (auto it = float_keys_.begin(); it != float_keys_.end(); ++it) {
        glUniform1f(it->first, post_effect_data->getFloat(it->second));
    }

    for (auto it = vec2_keys_.begin(); it != vec2_keys_.end(); ++it) {
        glm::vec2 v = post_effect_data->getVec2(it->second);
        glUniform2f(it->first, v.x, v.y);
    }

    for (auto it = vec3_keys_.begin(); it != vec3_keys_.end(); ++it) {
        glm::vec3 v = post_effect_data->getVec3(it->second);
        glUniform3f(it->first, v.x, v.y, v.z);
    }

    for (auto it = vec4_keys_.begin(); it != vec4_keys_.end(); ++it) {
        glm::vec4 v = post_effect_data->getVec4(it->second);
        glUniform4f(it->first, v.x, v.y, v.z, v.w);
    }

    for (auto it = mat4_keys_.begin(); it != mat4_keys_.end(); ++it) {
        glm::mat4 m = post_effect_data->getMat4(it->second);
        glUniformMatrix4fv(it->first, 1, GL_FALSE, glm::value_ptr(m));
    }

    glBindVertexArray(vaoID_);
    glDrawElements(GL_TRIANGLES, triangles.size(), GL_UNSIGNED_SHORT, 0);
    glBindVertexArray(0);
}

int CustomPostEffectShader::getGLTexture(int n) {
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

}
