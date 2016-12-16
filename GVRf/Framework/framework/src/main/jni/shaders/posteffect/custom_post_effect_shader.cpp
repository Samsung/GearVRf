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


namespace gvr {
CustomPostEffectShader::CustomPostEffectShader(const char* vertex_shader, const char* fragment_shader) :
        program_(0),
        a_position_(0),
        a_tex_coord_(0),
        u_texture_(0),
        vaoID_(0),
        vertex_shader_(vertex_shader),
        fragment_shader_(fragment_shader) {
}

CustomPostEffectShader::~CustomPostEffectShader() {
    delete program_;

    if (vaoID_ != 0) {
        GL(glDeleteVertexArrays(1, &vaoID_));
    }
}

void CustomPostEffectShader::addTextureKey(const std::string& variable_name, const std::string& key) {
    auto pair = std::make_pair(variable_name, key);
    std::lock_guard<std::mutex> lock(lock_);
    texture_keys_[pair] = 0;
}

void CustomPostEffectShader::addFloatKey(const std::string& variable_name, const std::string& key) {
    auto pair = std::make_pair(variable_name, key);
    std::lock_guard<std::mutex> lock(lock_);
    float_keys_[pair] = 0;
}
void CustomPostEffectShader::addVec2Key(const std::string& variable_name, const std::string& key) {
    auto pair = std::make_pair(variable_name, key);
    std::lock_guard<std::mutex> lock(lock_);
    vec2_keys_[pair] = 0;
}

void CustomPostEffectShader::addVec3Key(const std::string& variable_name, const std::string& key) {
    auto pair = std::make_pair(variable_name, key);
    std::lock_guard<std::mutex> lock(lock_);
    vec3_keys_[pair] = 0;
}

void CustomPostEffectShader::addVec4Key(const std::string& variable_name, const std::string& key) {
    auto pair = std::make_pair(variable_name, key);
    std::lock_guard<std::mutex> lock(lock_);
    vec4_keys_[pair] = 0;
}

void CustomPostEffectShader::addMat4Key(const std::string& variable_name, const std::string& key) {
    auto pair = std::make_pair(variable_name, key);
    std::lock_guard<std::mutex> lock(lock_);
    mat4_keys_[pair] = 0;
}

void CustomPostEffectShader::render(Camera* camera,
        RenderTexture* render_texture,
        PostEffectData* post_effect_data,
        std::vector<glm::vec3>& vertices, std::vector<glm::vec2>& tex_coords,
        std::vector<unsigned short>& triangles) {

    if (0 == program_) {
        program_ = new GLProgram(vertex_shader_.c_str(), fragment_shader_.c_str());
        vertex_shader_.empty();
        fragment_shader_.empty();

        a_position_ = glGetAttribLocation(program_->id(), "a_position");
        a_tex_coord_ = glGetAttribLocation(program_->id(), "a_texcoord");
        u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
        u_projection_matrix_ = glGetUniformLocation(program_->id(), "u_projection_matrix");
        u_right_eye_ = glGetUniformLocation(program_->id(), "u_right_eye");
    }
    if (0 == program_->id()) {
        LOGE("CustomPostEffectShader not rendering due to shader-related error");
        return;
    }

    glUseProgram(program_->id());

    if(vaoID_ == 0)
    {
        GLuint tmpID;

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

    lock_.lock();
    for (auto it = texture_keys_.begin(); it != texture_keys_.end(); ++it) {
        glActiveTexture(getGLTexture(texture_index));

        const std::string& variable = it->first.first;
        const std::string& key = it->first.second;
        Texture* texture = post_effect_data->getTexture(key);
        glBindTexture(texture->getTarget(), texture->getId());

        if (0 == it->second) {
            it->second = glGetUniformLocation(program_->id(), variable.c_str());
        }
        glUniform1i(it->second, texture_index++);
    }

    for (auto it = float_keys_.begin(); it != float_keys_.end(); ++it) {
        const std::string& variable = it->first.first;
        const std::string& key = it->first.second;

        if (0 == it->second) {
            it->second = glGetUniformLocation(program_->id(), variable.c_str());
        }
        glUniform1f(it->second, post_effect_data->getFloat(key));
    }

    for (auto it = vec2_keys_.begin(); it != vec2_keys_.end(); ++it) {
        const std::string& variable = it->first.first;
        const std::string& key = it->first.second;

        const glm::vec2& v = post_effect_data->getVec2(key);

        if (0 == it->second) {
            it->second = glGetUniformLocation(program_->id(), variable.c_str());
        }
        glUniform2f(it->second, v.x, v.y);
    }

    for (auto it = vec3_keys_.begin(); it != vec3_keys_.end(); ++it) {
        const std::string& variable = it->first.first;
        const std::string& key = it->first.second;

        const glm::vec3& v = post_effect_data->getVec3(key);

        if (0 == it->second) {
            it->second = glGetUniformLocation(program_->id(), variable.c_str());
        }
        glUniform3f(it->second, v.x, v.y, v.z);
    }

    for (auto it = vec4_keys_.begin(); it != vec4_keys_.end(); ++it) {
        const std::string& variable = it->first.first;
        const std::string& key = it->first.second;

        const glm::vec4& v = post_effect_data->getVec4(key);

        if (0 == it->second) {
            it->second = glGetUniformLocation(program_->id(), variable.c_str());
        }
        glUniform4f(it->second, v.x, v.y, v.z, v.w);
    }

    for (auto it = mat4_keys_.begin(); it != mat4_keys_.end(); ++it) {
        const std::string& variable = it->first.first;
        const std::string& key = it->first.second;

        const glm::mat4& m = post_effect_data->getMat4(key);

        if (0 == it->second) {
            it->second = glGetUniformLocation(program_->id(), variable.c_str());
        }
        glUniformMatrix4fv(it->second, 1, GL_FALSE, glm::value_ptr(m));
    }
    lock_.unlock();

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
