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
 * Renders the textures of light map plus the main texutre.
 ***************************************************************************/

#include "lightmap_shader.h"

#include "gl/gl_program.h"
#include "objects/material.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include "objects/textures/texture.h"
#include "util/gvr_gl.h"
#include "engine/renderer/renderer.h"

namespace gvr {
static const char VERTEX_SHADER[] = "attribute vec4 a_position;\n"
        "attribute vec3 a_normal;\n"
        "attribute vec2 a_tex_coord;\n"
        "uniform mat4 u_mvp;\n"
        "varying vec2 coord;\n"
        "void main() {\n"
        " vec4 pos = u_mvp * a_position;\n"
        " coord = a_tex_coord;\n"
        " gl_Position = pos;\n"
        "}";

static const char FRAGMENT_SHADER[] = "precision mediump float;\n"
        "varying vec2  coord;\n"
        "uniform sampler2D u_main_texture;\n"
        "uniform sampler2D u_lightmap_texture;\n"
        "uniform vec2  u_lightmap_offset;\n"
        "uniform vec2  u_lightmap_scale;\n"
        "void main() {\n"
        " vec4 color;\n"
        " vec4 lightmap_color;\n"
        " vec2 lightmap_coord = (coord * u_lightmap_scale) + u_lightmap_offset;\n"
        // Beast exports the texture with vertical flip
        " lightmap_color = texture2D(u_lightmap_texture, vec2(lightmap_coord.x, 1.0 - lightmap_coord.y));\n"
        " color = texture2D(u_main_texture, coord);\n"
        " gl_FragColor = color * lightmap_color;\n"
        "}";

LightMapShader::LightMapShader() :
        u_mvp_(0), u_texture_(0), u_lightmap_texture_(0),
        u_lightmap_offset_(0), u_lightmap_scale_(0) {
    program_ = new GLProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");

    u_lightmap_texture_ = glGetUniformLocation(program_->id(), "u_lightmap_texture");
    u_lightmap_offset_ = glGetUniformLocation(program_->id(), "u_lightmap_offset");
    u_lightmap_scale_ = glGetUniformLocation(program_->id(), "u_lightmap_scale");
}

LightMapShader::~LightMapShader() {
    delete program_;
}

void LightMapShader::render(RenderState* rstate,
        RenderData* render_data, Material* material) {

    Texture* texture = material->getTexture("main_texture");
    Texture* lightmap_texture = material->getTexture("lightmap_texture");
    glm::vec2 lightmap_offset = material->getVec2("lightmap_offset");
    glm::vec2 lightmap_scale = material->getVec2("lightmap_scale");

    glUseProgram(program_->id());

    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp));

    glActiveTexture (GL_TEXTURE0);
    glBindTexture(texture->getTarget(), texture->getId());
    glUniform1i(u_texture_, 0);

    glActiveTexture (GL_TEXTURE1);
    glBindTexture(lightmap_texture->getTarget(), lightmap_texture->getId());
    glUniform1i(u_lightmap_texture_, 1);

    glUniform2f(u_lightmap_offset_, lightmap_offset.x, lightmap_offset.y);
    glUniform2f(u_lightmap_scale_, lightmap_scale.x, lightmap_scale.y);
    checkGlError("LightMapShader::render");
}

};
