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
 * Renders a cube map texture without light.
 ***************************************************************************/

#include "cubemap_shader.h"
#include "objects/material.h"
#include "util/gvr_log.h"
#include "engine/renderer/renderer.h"

// OpenGL Cube map texture uses coordinate system different to other OpenGL functions:
// Positive x pointing right, positive y pointing up, positive z pointing inward.
// It is a left-handed system, while other OpenGL functions use right-handed system.
// The side faces are also oriented up-side down as illustrated below.
//
// Since the origin of Android bitmap is at top left, and the origin of OpenGL texture
// is at bottom left, when we use Android bitmap to create OpenGL texture, it is already
// up-side down. So we do not need to flip them again.
//
// We do need to flip the z-coordinate to be consistent with the left-handed system.
//    _________
//   /        /|
//  /________/ |
//  |        | |    +y
//  |        | |    |  +z
//  |        | /    | /
//  |________|/     |/___ +x
//
//  Positive x    Positive y    Positive z
//      ______        ______        ______
//     |      |      |      |      |      |
//  -y |      |   +z |      |   -y |      |
//  |  |______|   |  |______|   |  |______|
//  |___ -z       |___ +x       |___ +x
//
//  Negative x    Negative y    Negative z
//      ______        ______        ______
//     |      |      |      |      |      |
//  -y |      |   -z |      |   -y |      |
//  |  |______|   |  |______|   |  |______|
//  |___ +z       |___ +x       |___ -x
//
// (http://www.nvidia.com/object/cube_map_ogl_tutorial.html)
// (http://stackoverflow.com/questions/11685608/convention-of-faces-in-opengl-cubemapping)

namespace gvr {

const std::string VERTEX_SHADER =
        "#ifdef HAS_MULTIVIEW\n"
        "#extension GL_OVR_multiview2 : enable\n"
        "layout(num_views = 2) in;\n"
        "uniform mat4 u_mvp_[2];\n"
        "#else\n"
        "uniform mat4 u_mvp;\n"
        "#endif\n"

        "in vec3 a_position;\n"
        "uniform mat4 u_model;\n"
        "out vec3 v_tex_coord;\n"

        "void main() {\n"
        "  vec4 pos = vec4(a_position, 1.0);\n"
        "  v_tex_coord = normalize((u_model * pos).xyz);\n"
        "  v_tex_coord.z = -v_tex_coord.z;\n"

        "#ifdef HAS_MULTIVIEW\n"
        "  gl_Position = u_mvp_[gl_ViewID_OVR]  * pos;\n"
         "#else\n"
        "  gl_Position = u_mvp  * pos;\n"
        "#endif\n"

        "}\n";

const std::string FRAGMENT_SHADER =
        "precision highp float;\n"

        "uniform samplerCube u_texture;\n"
        "uniform vec3 u_color;\n"
        "uniform float u_opacity;\n"
        "out vec4 outColor;\n"
        "in vec3 v_tex_coord;\n"

        "void main()\n"
        "{\n"
        "  vec4 color = texture(u_texture, v_tex_coord);\n"
        "  outColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);\n"
        "}\n";


void CubemapShader::programInit(RenderState* rstate){

    std::string vertexShaderSource = VERTEX_SHADER;
    std::string fragmentShaderSource = FRAGMENT_SHADER;

    if(rstate->is_multiview){
        vertexShaderSource = "#define HAS_MULTIVIEW\n" + vertexShaderSource;
        fragmentShaderSource =  "#define HAS_MULTIVIEW\n" + fragmentShaderSource;
    }

    vertexShaderSource = "#version 300 es\n" + vertexShaderSource;
    fragmentShaderSource = "#version 300 es\n" + fragmentShaderSource;

    program_ = new GLProgram(vertexShaderSource.c_str(), fragmentShaderSource.c_str());
    u_model_ = glGetUniformLocation(program_->id(), "u_model");

    if(rstate->is_multiview) {
        u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp_[0]");
    } else {
        u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    }

    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
    u_color_ = glGetUniformLocation(program_->id(), "u_color");
    u_opacity_ = glGetUniformLocation(program_->id(), "u_opacity");
}

CubemapShader::CubemapShader() {
}

CubemapShader::~CubemapShader() {
    delete program_;
}

void CubemapShader::render(RenderState* rstate, RenderData*, Material* material) {

    if(program_ == nullptr) {
        programInit(rstate);
    }

    Texture* texture = material->getTexture("main_texture");
    glm::vec3 color = material->getVec3("color");
    float opacity = material->getFloat("opacity");

    if (texture->getTarget() != GL_TEXTURE_CUBE_MAP) {
        std::string error = "CubemapShader::render : texture with wrong target";
        throw error;
    }
    GL(glUseProgram(program_->id()));
    GL(glUniformMatrix4fv(u_model_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_model)));

    if(rstate->is_multiview) {
        GL(glUniformMatrix4fv(u_mvp_, 2, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp_[0])));
    } else {
        GL(glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp)));
    }

    GL(glActiveTexture (GL_TEXTURE0));
    GL(glBindTexture(texture->getTarget(), texture->getId()));
    GL(glUniform1i(u_texture_, 0));
    GL(glUniform3f(u_color_, color.r, color.g, color.b));
    GL(glUniform1f(u_opacity_, opacity));
}

}
