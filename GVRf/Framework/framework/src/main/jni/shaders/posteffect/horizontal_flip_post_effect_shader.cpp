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
 * Horizontally flips the scene
 ***************************************************************************/

#include "horizontal_flip_post_effect_shader.h"

#include "gl/gl_program.h"
#include "objects/post_effect_data.h"
#include "objects/textures/render_texture.h"
#include "util/gvr_gl.h"

namespace gvr {
static const char VERTEX_SHADER[] = "attribute vec4 a_position;\n"
        "attribute vec4 a_tex_coord;\n"
        "varying vec2 v_tex_coord;\n"
        "void main() {\n"
        "  v_tex_coord = vec2(a_tex_coord.x, 1.0 - a_tex_coord.y);\n"
        "  gl_Position = a_position;\n"
        "}\n";

static const char FRAGMENT_SHADER[] = "precision highp float;\n"
        "uniform sampler2D u_texture;\n"
        "varying vec2 v_tex_coord;\n"
        "void main() {\n"
        "  gl_FragColor = texture2D(u_texture, v_tex_coord);\n"
        "}\n";

HorizontalFlipPostEffectShader::HorizontalFlipPostEffectShader() :
        program_(0), a_position_(0), a_tex_coord_(0), u_texture_(0) {
    deleter_ = getDeleterForThisThread();

    program_ = new GLProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    a_position_ = glGetAttribLocation(program_->id(), "a_position");
    a_tex_coord_ = glGetAttribLocation(program_->id(), "a_tex_coord");
    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
    vaoID_ = 0;
}

HorizontalFlipPostEffectShader::~HorizontalFlipPostEffectShader() {
    delete program_;

    if (vaoID_ != 0) {
        deleter_->queueVertexArray(vaoID_);
    }
}

void HorizontalFlipPostEffectShader::render(
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

    glActiveTexture (GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, render_texture->getId());
    glUniform1i(u_texture_, 0);

    glBindVertexArray(vaoID_);
    glDrawElements(GL_TRIANGLES, triangles.size(), GL_UNSIGNED_SHORT, 0);
    glBindVertexArray(0);

    checkGlError("HorizontalFlipPostEffectShader::render");
}
}
