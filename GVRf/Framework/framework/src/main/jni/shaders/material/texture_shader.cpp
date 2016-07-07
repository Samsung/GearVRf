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
 * Renders a texture with light.
 ***************************************************************************/

#include "texture_shader.h"

#include "gl/gl_program.h"
#include "objects/material.h"
#include "objects/light.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include "objects/textures/texture.h"
#include "util/gvr_gl.h"
#include "util/gvr_log.h"
#include "engine/renderer/renderer.h"

namespace gvr {
static const char USE_LIGHT[] = "#define USE_LIGHT\n";
static const char NOT_USE_LIGHT[] = "#undef USE_LIGHT\n";
static const char VERTEX_SHADER[] =
        "attribute vec3 a_position;\n"
                "attribute vec2 a_tex_coord;\n"
                "uniform mat4 u_mvp;\n"
                "varying vec2 v_tex_coord;\n"
                "#ifdef USE_LIGHT\n"
                "attribute vec3 a_normal;\n"
                "uniform mat4 u_mv;\n"
                "uniform mat4 u_mv_it;\n"
                "uniform vec3 u_light_pos;\n"
                "varying vec3 v_viewspace_normal;\n"
                "varying vec3 v_viewspace_light_direction;\n"
                "#endif\n"
                "\n"
                "void main() {\n"
                "vec4 new_pos = vec4(a_position.x, a_position.y, a_position.z, 1.0);\n"
                "#ifdef USE_LIGHT\n"
                "  vec4 v_viewspace_position_vec4 = u_mv * new_pos;\n"
                "  vec3 v_viewspace_position = v_viewspace_position_vec4.xyz / v_viewspace_position_vec4.w;\n"
                "  v_viewspace_light_direction = u_light_pos - v_viewspace_position;\n"
                "  v_viewspace_normal = (u_mv_it * vec4(a_normal, 1.0)).xyz;\n"
                "#endif\n"
                "  v_tex_coord = a_tex_coord.xy;\n"
                "  gl_Position = u_mvp * new_pos;\n"
                "}\n";

static const char FRAGMENT_SHADER[] =
        "precision highp float;\n"
                "uniform sampler2D u_texture;\n"
                "uniform vec3 u_color;\n"
                "uniform float u_opacity;\n"
                "varying vec2 v_tex_coord;\n"
                "#ifdef USE_LIGHT\n"
                "uniform vec4 materialAmbientColor;\n"
                "uniform vec4 materialDiffuseColor;\n"
                "uniform vec4 materialSpecularColor;\n"
                "uniform float materialSpecularExponent;\n"
                "uniform vec4 lightAmbientIntensity;\n"
                "uniform vec4 lightDiffuseIntensity;\n"
                "uniform vec4 lightSpecularIntensity;\n"
                "varying vec3 v_viewspace_normal;\n"
                "varying vec3 v_viewspace_light_direction;\n"
                "#endif\n"
                "\n"
                "void main()\n"
                "{\n"
                "  vec4 color;\n"
                "#ifdef USE_LIGHT\n"
                "  // Dot product gives us diffuse intensity\n"
                "  float diffuse = max(0.0, dot(normalize(v_viewspace_normal), normalize(v_viewspace_light_direction)));\n"
                "\n"
                "  // Multiply intensity by diffuse color, force alpha to 1.0\n"
                "  color = diffuse * materialDiffuseColor * lightDiffuseIntensity;\n"
                "\n"
                "  // Add in ambient light\n"
                "  color += materialAmbientColor * lightAmbientIntensity;\n"
                "\n"
                "  // Modulate in the texture\n"
                "  color *= texture2D(u_texture, v_tex_coord);\n"
                "\n"
                "  // Specular Light\n"
                "  vec3 reflection = normalize(reflect(-normalize(v_viewspace_light_direction), normalize(v_viewspace_normal)));\n"
                "  float specular = max(0.0, dot(normalize(v_viewspace_normal), reflection));\n"
                "  if(diffuse != 0.0) {\n"
                "    color += pow(specular, materialSpecularExponent) * materialSpecularColor * lightSpecularIntensity;\n"
                "  }\n"
                "#else\n"
                "  color = texture2D(u_texture, v_tex_coord);\n"
                "#endif\n"
                "\n"
                "  gl_FragColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);\n"
                "}\n";

TextureShader::TextureShader() :
        program_light_(0), program_no_light_(0),u_mv_(0), u_mv_it_(0), u_mvp_(0), u_light_pos_(
                0), u_texture_(0), u_color_(0), u_opacity_(0), u_material_ambient_color_(
                0), u_material_diffuse_color_(0), u_material_specular_color_(0), u_material_specular_exponent_(
                0), u_light_ambient_intensity_(0), u_light_diffuse_intensity_(
                0), u_light_specular_intensity_(0) {
    const char* vertex_shader_light_strings[2] = { USE_LIGHT, VERTEX_SHADER };
    GLint vertex_shader_light_string_lengths[2] = { (GLint) strlen(USE_LIGHT),
            (GLint) strlen(VERTEX_SHADER) };
    const char* vertex_shader_no_light_strings[2] = { NOT_USE_LIGHT,
            VERTEX_SHADER };
    GLint vertex_shader_no_light_string_lengths[2] = { (GLint) strlen(
            NOT_USE_LIGHT), (GLint) strlen(VERTEX_SHADER) };
    const char* fragment_shader_light_strings[2] =
            { USE_LIGHT, FRAGMENT_SHADER };
    GLint fragment_shader_light_string_lengths[2] = { (GLint) strlen(USE_LIGHT),
            (GLint) strlen(FRAGMENT_SHADER) };
    const char* fragment_shader_no_light_strings[2] = { NOT_USE_LIGHT,
            FRAGMENT_SHADER };
    GLint fragment_shader_no_light_string_lengths[2] = { (GLint) strlen(
            NOT_USE_LIGHT), (GLint) strlen(FRAGMENT_SHADER) };

    program_light_ = new GLProgram(vertex_shader_light_strings,
            vertex_shader_light_string_lengths, fragment_shader_light_strings,
            fragment_shader_light_string_lengths, 2);
    program_no_light_ = new GLProgram(vertex_shader_no_light_strings,
            vertex_shader_no_light_string_lengths,
            fragment_shader_no_light_strings,
            fragment_shader_no_light_string_lengths, 2);

    u_mvp_no_light_ = glGetUniformLocation(program_no_light_->id(), "u_mvp");
    u_texture_no_light_ = glGetUniformLocation(program_no_light_->id(),
            "u_texture");
    u_color_no_light_ = glGetUniformLocation(program_no_light_->id(),
            "u_color");
    u_opacity_no_light_ = glGetUniformLocation(program_no_light_->id(),
            "u_opacity");

    u_mvp_ = glGetUniformLocation(program_light_->id(), "u_mvp");
    u_texture_ = glGetUniformLocation(program_light_->id(), "u_texture");
    u_color_ = glGetUniformLocation(program_light_->id(), "u_color");
    u_opacity_ = glGetUniformLocation(program_light_->id(), "u_opacity");

    u_mv_ = glGetUniformLocation(program_light_->id(), "u_mv");
    u_mv_it_ = glGetUniformLocation(program_light_->id(), "u_mv_it");
    u_light_pos_ = glGetUniformLocation(program_light_->id(), "u_light_pos");
    u_material_ambient_color_ = glGetUniformLocation(program_light_->id(),
            "materialAmbientColor");
    u_material_diffuse_color_ = glGetUniformLocation(program_light_->id(),
            "materialDiffuseColor");
    u_material_specular_color_ = glGetUniformLocation(program_light_->id(),
            "materialSpecularColor");
    u_material_specular_exponent_ = glGetUniformLocation(program_light_->id(),
            "materialSpecularExponent");
    u_light_ambient_intensity_ = glGetUniformLocation(program_light_->id(),
            "lightAmbientIntensity");
    u_light_diffuse_intensity_ = glGetUniformLocation(program_light_->id(),
            "lightDiffuseIntensity");
    u_light_specular_intensity_ = glGetUniformLocation(program_light_->id(),
            "lightSpecularIntensity");
}

TextureShader::~TextureShader() {
    delete program_light_;
    delete program_no_light_;
}

void TextureShader::render(RenderState* rstate,
        RenderData* render_data, Material* material) {
    Texture* texture = material->getTexture("main_texture");
    glm::vec3 color = material->getVec3("color");
    float opacity = material->getFloat("opacity");
    glm::vec4 material_ambient_color = material->getVec4("ambient_color");
    glm::vec4 material_diffuse_color = material->getVec4("diffuse_color");
    glm::vec4 material_specular_color = material->getVec4("specular_color");
    float material_specular_exponent = material->getFloat("specular_exponent");

    if (texture->getTarget() != GL_TEXTURE_2D) {
        std::string error = "TextureShader::render : texture with wrong target.";
        throw error;
    }

    bool use_light = false;
    Light* light;
    if (render_data->light_enabled()) {
        light = render_data->light();
        if (light->enabled()) {
            use_light = true;
        }
    }

    if (use_light) {
        program_ = program_light_;
    } else {
        program_ = program_no_light_;
    }
    GLuint programId = program_->id();
    render_data->mesh()->generateVAO(programId);
    GL(glUseProgram(programId));
    GL(glActiveTexture (GL_TEXTURE0));
    GL(glBindTexture(texture->getTarget(), texture->getId()));

    if (use_light) {
        glm::vec3 light_position = light->getVec3("world_position");
        glm::vec4 light_ambient_intensity = light->getVec4("ambient_intensity");
        glm::vec4 light_diffuse_intensity = light->getVec4("diffuse_intensity");
        glm::vec4 light_specular_intensity = light->getVec4(
                "specular_intensity");

        glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mvp));
        glUniformMatrix4fv(u_mv_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mv));
        glUniformMatrix4fv(u_mv_it_, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_mv_it));
        glUniform3f(u_light_pos_, light_position.x, light_position.y,
                light_position.z);

        glUniform1i(u_texture_, 0);
        glUniform3f(u_color_, color.r, color.g, color.b);
        glUniform1f(u_opacity_, opacity);

        glUniform4f(u_material_ambient_color_, material_ambient_color.r,
                material_ambient_color.g, material_ambient_color.b,
                material_ambient_color.a);
        glUniform4f(u_material_diffuse_color_, material_diffuse_color.r,
                material_diffuse_color.g, material_diffuse_color.b,
                material_diffuse_color.a);
        glUniform4f(u_material_specular_color_, material_specular_color.r,
                material_specular_color.g, material_specular_color.b,
                material_specular_color.a);
        glUniform1f(u_material_specular_exponent_, material_specular_exponent);
        glUniform4f(u_light_ambient_intensity_, light_ambient_intensity.r,
                light_ambient_intensity.g, light_ambient_intensity.b,
                light_ambient_intensity.a);
        glUniform4f(u_light_diffuse_intensity_, light_diffuse_intensity.r,
                light_diffuse_intensity.g, light_diffuse_intensity.b,
                light_diffuse_intensity.a);
        glUniform4f(u_light_specular_intensity_, light_specular_intensity.r,
                light_specular_intensity.g, light_specular_intensity.b,
                light_specular_intensity.a);
    } else {
        glUniformMatrix4fv(u_mvp_no_light_, 1, GL_FALSE,
                glm::value_ptr(rstate->uniforms.u_mvp));

        glUniform1i(u_texture_no_light_, 0);
        glUniform3f(u_color_no_light_, color.r, color.g, color.b);
        glUniform1f(u_opacity_no_light_, opacity);
    }
   checkGlError("TextureShader::render");
}

}
;
