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

#include "unlit_fbo_shader.h"

#include "gl/gl_program.h"
#include "objects/material.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include "objects/textures/texture.h"
#include "util/gvr_gl.h"
#include "engine/renderer/renderer.h"

namespace gvr {
static const char VERTEX_SHADER[] = "attribute vec4 a_position;\n"
        "attribute vec4 a_tex_coord;\n"
        "uniform mat4 u_mvp;\n"
        "varying vec2 v_tex_coord;\n"
        "void main() {\n"
        "  v_tex_coord.x = a_tex_coord.x;\n"
        "  v_tex_coord.y = 1.0 - a_tex_coord.y;\n"
        "  gl_Position = u_mvp * a_position;\n"
        "}\n";

static const char FRAGMENT_SHADER[] =
        "precision highp float;\n"
                "uniform sampler2D u_texture;\n"
                "uniform vec3 u_color;\n"
                "uniform float u_opacity;\n"
                "varying vec2 v_tex_coord;\n"
                "void main()\n"
                "{\n"
                "  vec4 color = texture2D(u_texture, v_tex_coord);"
                "  gl_FragColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);\n"
                "}\n";

UnlitFboShader::UnlitFboShader() :
         u_mvp_(0), u_texture_(0), u_color_(0), u_opacity_(0) {
    program_ = new GLProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
    u_color_ = glGetUniformLocation(program_->id(), "u_color");
    u_opacity_ = glGetUniformLocation(program_->id(), "u_opacity");
}

UnlitFboShader::~UnlitFboShader() {
    if (program_ != 0) {
        recycle();
    }
}

void UnlitFboShader::recycle() {
    delete program_;
    program_ = 0;
}

void UnlitFboShader::render(RenderState* rstate,
        RenderData* render_data, Material* material) {
    Texture* texture = material->getTexture("main_texture");
    glm::vec3 color = material->getVec3("color");
    float opacity = material->getFloat("opacity");

    if (texture->getTarget() != GL_TEXTURE_2D) {
        std::string error = "UnlitFboShader::render : texture with wrong target";
        throw error;
    }

    glUseProgram(program_->id());

    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp));
    glActiveTexture (GL_TEXTURE0);
    glBindTexture(texture->getTarget(), texture->getId());
    glUniform1i(u_texture_, 0);
    glUniform3f(u_color_, color.r, color.g, color.b);
    glUniform1f(u_opacity_, opacity);
    checkGlError("UnlitFboShader::render");
}

}
;
