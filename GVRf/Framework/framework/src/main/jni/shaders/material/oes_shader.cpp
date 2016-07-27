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
 * Renders a GL_TEXTURE_EXTERNAL_OES texture.
 ***************************************************************************/

#include "oes_shader.h"

#include "gl/gl_program.h"
#include "objects/material.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include "util/gvr_gl.h"
#include "engine/renderer/renderer.h"

static const char USE_MULTIVIEW[] = "#define MULTIVIEW\n";
static const char NOT_USE_MULTIVIEW[] = "#undef MULTIVIEW\n";
static const char version[] = "#version 300 es\n";

namespace gvr {

static const char VERTEX_SHADER[] =
        "#ifdef MULTIVIEW\n"
        "#extension GL_OVR_multiview2 : enable\n"
        "layout(num_views = 2) in;\n"
        "uniform mat4 u_mvp_[2];\n"
        "#else\n"
        "uniform mat4 u_mvp;\n"
        "#endif\n"

        "in vec3 a_position;\n"
        "in vec2 a_tex_coord;\n"

        "out vec2 v_tex_coord;\n"
        "void main() {\n"
        "  v_tex_coord = a_tex_coord.xy;\n"
        "#ifdef MULTIVIEW\n"
        "  gl_Position = u_mvp_[gl_ViewID_OVR] * vec4(a_position,1.0);\n"
        "#else\n"
        "  gl_Position = u_mvp * vec4(a_position,1.0);\n"
        "#endif\n"
        "}\n";

static const char FRAGMENT_SHADER[] =
        "#extension GL_OES_EGL_image_external : require\n"
                "precision highp float;\n"
                "uniform samplerExternalOES u_texture;\n"
                "uniform vec3 u_color;\n"
                "uniform float u_opacity;\n"
                "in vec2 v_tex_coord;\n"
                "out vec4 out_color;\n"
                "void main()\n"
                "{\n"
                "  vec4 color = texture(u_texture, v_tex_coord);"
                "  out_color = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);\n"
                "}\n";


OESShader::OESShader() :
        u_mvp_(0), u_texture_(0), u_color_(
                0), u_opacity_(0) {
}
void OESShader::programInit(RenderState* rstate){
    const char* frag_shader_strings[3];
    GLint frag_shader_string_lengths[3];

    const char* vertex_shader_strings[3];
    GLint vertex_shader_string_lengths[3];
    vertex_shader_strings[0] = version;
    frag_shader_strings[0] = version;
    vertex_shader_string_lengths [0]=(GLint) strlen(version);
    frag_shader_string_lengths[0] = vertex_shader_string_lengths [0];
    vertex_shader_string_lengths [2] = (GLint) strlen(VERTEX_SHADER);
    frag_shader_string_lengths[2] = (GLint)strlen(FRAGMENT_SHADER);

    if(use_multiview){
        vertex_shader_strings[1] = USE_MULTIVIEW;
        frag_shader_strings[1] = USE_MULTIVIEW;
        vertex_shader_string_lengths [1] =(GLint)strlen(USE_MULTIVIEW);
        frag_shader_string_lengths[1] = vertex_shader_string_lengths [1];
    }
    else {
        vertex_shader_strings[1] = NOT_USE_MULTIVIEW;
        frag_shader_strings[1] = NOT_USE_MULTIVIEW;
        vertex_shader_string_lengths [1] =(GLint)strlen(NOT_USE_MULTIVIEW);
        frag_shader_string_lengths[1] = (GLint)strlen(NOT_USE_MULTIVIEW);
    }
    vertex_shader_strings [2] = VERTEX_SHADER;
    frag_shader_strings [2] = FRAGMENT_SHADER;

    program_ = new GLProgram(vertex_shader_strings,
                    vertex_shader_string_lengths, frag_shader_strings,
                    frag_shader_string_lengths, 3);

    if(use_multiview)
        u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp_[0]");
    else
        u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");

    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
    u_color_ = glGetUniformLocation(program_->id(), "u_color");
    u_opacity_ = glGetUniformLocation(program_->id(), "u_opacity");

}
OESShader::~OESShader() {
    delete program_;
}

void OESShader::render(RenderState* rstate, RenderData* render_data, Material* material) {
    if(!program_)
        programInit(rstate);

    Texture* texture = material->getTexture("main_texture");
    glm::vec3 color = material->getVec3("color");
    float opacity = material->getFloat("opacity");

    if (texture->getTarget() != GL_TEXTURE_EXTERNAL_OES) {
        std::string error = "OESShader::render : texture with wrong target";
        throw error;
    }

    glUseProgram(program_->id());
    if(use_multiview)
        glUniformMatrix4fv(u_mvp_, 2, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp_[0]));
    else
        glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp));

    glActiveTexture (GL_TEXTURE0);
    glBindTexture(texture->getTarget(), texture->getId());
    glUniform1i(u_texture_, 0);
    glUniform3f(u_color_, color.r, color.g, color.b);
    glUniform1f(u_opacity_, opacity);
    checkGlError("OESShader::render");
}

}
;
