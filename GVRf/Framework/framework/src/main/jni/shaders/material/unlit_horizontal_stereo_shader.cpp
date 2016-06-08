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
 * Renders a horizontally split texture without light.
 ***************************************************************************/

#include "unlit_horizontal_stereo_shader.h"

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
        "  v_tex_coord = a_tex_coord.xy;\n"
        "  gl_Position = u_mvp * a_position;\n"
        "}\n";

static const char FRAGMENT_SHADER[] =
        "precision highp float;\n"
                "uniform sampler2D u_texture;\n"
                "uniform vec3 u_color;\n"
                "uniform float u_opacity;\n"
                "uniform int u_right;\n"
                "varying vec2 v_tex_coord;\n"
                "void main()\n"
                "{\n"
                "  vec2 tex_coord = vec2(0.5 * (v_tex_coord.x + float(u_right)), v_tex_coord.y);\n"
                "  vec4 color = texture2D(u_texture, tex_coord);\n"
                "  gl_FragColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);\n"
                "}\n";

UnlitHorizontalStereoShader::UnlitHorizontalStereoShader() :
        u_mvp_(0), u_texture_(0), u_color_(
                0), u_opacity_(0), u_right_(0) {
    program_ = new GLProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
    u_color_ = glGetUniformLocation(program_->id(), "u_color");
    u_opacity_ = glGetUniformLocation(program_->id(), "u_opacity");
    u_right_ = glGetUniformLocation(program_->id(), "u_right");
}

UnlitHorizontalStereoShader::~UnlitHorizontalStereoShader() {
    delete program_;
}

void UnlitHorizontalStereoShader::render(RenderState* rstate,
        RenderData* render_data, Material* material) {
    Texture* texture = material->getTexture("main_texture");
    glm::vec3 color = material->getVec3("color");
    float opacity = material->getFloat("opacity");
    bool mono_rendering;

    if (texture->getTarget() != GL_TEXTURE_2D) {
        std::string error =
                "UnlitHorizontalStereoShader::render : texture with wrong target";
        throw error;
    }

    try {
        mono_rendering = material->getFloat("mono_rendering") == 1;
    } catch (std::string& error) {
        mono_rendering  = false;
    }

    glUseProgram(program_->id());

    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp));
    glActiveTexture (GL_TEXTURE0);
    glBindTexture(texture->getTarget(), texture->getId());
    glUniform1i(u_texture_, 0);
    glUniform3f(u_color_, color.r, color.g, color.b);
    glUniform1f(u_opacity_, opacity);
    glUniform1i(u_right_, mono_rendering || rstate->uniforms.u_right ? 1 : 0);
    checkGlError("HorizontalStereoUnlitShader::render");
}

}
