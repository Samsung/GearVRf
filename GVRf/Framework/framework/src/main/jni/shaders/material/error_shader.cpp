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
 * GL program for rendering a object with an error.
 ***************************************************************************/

#include "error_shader.h"

#include "gl/gl_program.h"
#include "objects/material.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include "util/gvr_gl.h"
#include "engine/renderer/renderer.h"

namespace gvr {
static const char VERTEX_SHADER[] = "attribute vec4 a_position;\n"
        "uniform mat4 u_mvp;\n"
        "void main() {\n"
        "  gl_Position = u_mvp * a_position;\n"
        "}\n";

static const char FRAGMENT_SHADER[] = "precision highp float;\n"
        "uniform vec4 u_color;\n"
        "void main()\n"
        "{\n"
        "  gl_FragColor = u_color;\n"
        "}\n";

ErrorShader::ErrorShader() {
    program_ = new GLProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    u_color_ = glGetUniformLocation(program_->id(), "u_color");
}

ErrorShader::~ErrorShader() {
    delete program_;
}

void ErrorShader::render(RenderState* rstate, RenderData* render_data, Material* mtl_unused) {
    float r = 0.0f;
    float g = 1.0f;
    float b = 0.0f;
    float a = 1.0f;

    glUseProgram(program_->id());
    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp));
    glUniform4f(u_color_, r, g, b, a);
    checkGlError("ErrorShader::render");
}

}
