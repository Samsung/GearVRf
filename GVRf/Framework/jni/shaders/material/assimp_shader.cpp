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
 * Shader for model loaded with Assimp
 ***************************************************************************/

#include "assimp_shader.h"

#include "gl/gl_program.h"
#include "objects/material.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include "objects/textures/texture.h"
#include "util/gvr_gl.h"

#include "util/gvr_log.h"

namespace gvr {

#define AS_TOTAL_SHADER_STRINGS_COUNT    AS_TOTAL_FEATURE_COUNT + 1

static const char DIFFUSE_TEXTURE[] = "#define AS_DIFFUSE_TEXTURE\n";
static const char NO_DIFFUSE_TEXTURE[] = "#undef AS_DIFFUSE_TEXTURE\n";

static const char SKINNING[] = "#define AS_SKINNING\n";
static const char NO_SKINNING[] = "#undef AS_SKINNING\n";

static const char SPECULAR_TEXTURE[] = "#define AS_SPECULAR_TEXTURE\n";
static const char NO_SPECULAR_TEXTURE[] = "#undef AS_SPECULAR_TEXTURE\n";

static const char VERTEX_SHADER[] =
                "precision highp float;\n"
                "\n"
                "attribute vec4 a_position;\n"
                "uniform mat4 u_mvp;\n"
                "\n"
                "#ifdef AS_DIFFUSE_TEXTURE\n"
                "attribute vec4 a_tex_coord;\n"
                "varying vec2 v_tex_coord;\n"
                "#endif\n"
                "\n"

                // Skinning
                "#ifdef AS_SKINNING\n"
                "uniform mat4 u_bone_matrix[100];\n" // MAX_BONE_NUM = 100
                "attribute vec4 a_bone_indices;\n"
                "attribute vec4 a_bone_weights;\n"
                "#endif\n"
                "\n"

                "void main() {\n"
                "#ifdef AS_DIFFUSE_TEXTURE\n"
                "  v_tex_coord = a_tex_coord.xy;\n"
                "#endif\n"

                "#ifdef AS_SKINNING\n"
                "  lowp vec4 weights = a_bone_weights; \n"
                "  weights.w = 1.0 - dot( weights.xyz, vec3(1.0)); \n" // normalize weights
                "  vec4 local_pos = a_position; \n"
                "  vec4 obj_pos = (u_bone_matrix[int(a_bone_indices[0])] * local_pos) * weights[0]; \n"
                "  obj_pos += (u_bone_matrix[int(a_bone_indices[1])] * local_pos) * weights[1]; \n"
                "  obj_pos += (u_bone_matrix[int(a_bone_indices[2])] * local_pos) * weights[2]; \n"
                "  obj_pos += (u_bone_matrix[int(a_bone_indices[3])] * local_pos) * weights[3]; \n"
                "  gl_Position = u_mvp * obj_pos;\n"
                "#else\n"
                "  gl_Position = u_mvp * a_position;\n"
                "#endif\n"

                "}\n";

static const char FRAGMENT_SHADER[] =
                "precision highp float;\n"
                "\n"
                "#ifdef AS_DIFFUSE_TEXTURE\n"
                "varying vec2 v_tex_coord;\n"
                "uniform sampler2D u_texture;\n"
                "#else\n"
                "uniform vec4 u_diffuse_color;\n"
                "uniform vec4 u_ambient_color;\n"
                "#endif\n"
                "\n"
                "uniform vec3 u_color;\n"
                "uniform float u_opacity;\n"
                "\n"
                "void main()\n"
                "{\n"
                "#ifdef AS_DIFFUSE_TEXTURE\n"
                "  vec4 color;\n"
                "  color = texture2D(u_texture, v_tex_coord);\n"
                "  gl_FragColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);\n"
                "#else\n"
                "  gl_FragColor = (u_diffuse_color * u_opacity) + u_ambient_color;\n"
                "#endif\n"
                "}\n";

AssimpShader::AssimpShader() :
        program_(0), u_mvp_(0), u_diffuse_color_(0), u_ambient_color_(
                0), u_texture_(0), u_color_(0), u_opacity_(
                0), program_list_(0) {
    program_list_ = new GLProgram*[AS_TOTAL_GL_PROGRAM_COUNT];

    const char* vertex_shader_strings[AS_TOTAL_SHADER_STRINGS_COUNT];
    GLint vertex_shader_string_lengths[AS_TOTAL_SHADER_STRINGS_COUNT];
    const char* fragment_shader_strings[AS_TOTAL_SHADER_STRINGS_COUNT];
    GLint fragment_shader_string_lengths[AS_TOTAL_SHADER_STRINGS_COUNT];

    for (int i = 0; i < AS_TOTAL_GL_PROGRAM_COUNT; i++) {
        int counter = 0;

        // TODO: remove duplicate code
        if (ISSET(i, AS_DIFFUSE_TEXTURE)) {
            vertex_shader_strings[counter] =  DIFFUSE_TEXTURE;
            vertex_shader_string_lengths[counter] = (GLint) strlen(DIFFUSE_TEXTURE);
            fragment_shader_strings[counter] = DIFFUSE_TEXTURE;
            fragment_shader_string_lengths[counter] = (GLint) strlen(DIFFUSE_TEXTURE);
            counter++;
        } else {
            vertex_shader_strings[counter] =  NO_DIFFUSE_TEXTURE;
            vertex_shader_string_lengths[counter] = (GLint) strlen(NO_DIFFUSE_TEXTURE);
            fragment_shader_strings[counter] = NO_DIFFUSE_TEXTURE;
            fragment_shader_string_lengths[counter] = (GLint) strlen(NO_DIFFUSE_TEXTURE);
            counter++;
        }

        if (ISSET(i, AS_SPECULAR_TEXTURE)) {
            vertex_shader_strings[counter] =  SPECULAR_TEXTURE;
            vertex_shader_string_lengths[counter] = (GLint) strlen(SPECULAR_TEXTURE);
            fragment_shader_strings[counter] = SPECULAR_TEXTURE;
            fragment_shader_string_lengths[counter] = (GLint) strlen(SPECULAR_TEXTURE);
            counter++;
        } else {
            vertex_shader_strings[counter] =  NO_SPECULAR_TEXTURE;
            vertex_shader_string_lengths[counter] = (GLint) strlen(NO_SPECULAR_TEXTURE);
            fragment_shader_strings[counter] = NO_SPECULAR_TEXTURE;
            fragment_shader_string_lengths[counter] = (GLint) strlen(NO_SPECULAR_TEXTURE);
            counter++;
        }

        if (ISSET(i, AS_SKINNING)) {
            vertex_shader_strings[counter] =  SKINNING;
            vertex_shader_string_lengths[counter] = (GLint) strlen(SKINNING);
            fragment_shader_strings[counter] = SKINNING;
            fragment_shader_string_lengths[counter] = (GLint) strlen(SKINNING);
            counter++;
        } else {
            vertex_shader_strings[counter] =  NO_SKINNING;
            vertex_shader_string_lengths[counter] = (GLint) strlen(NO_SKINNING);
            fragment_shader_strings[counter] = NO_SKINNING;
            fragment_shader_string_lengths[counter] = (GLint) strlen(NO_SKINNING);
            counter++;
        }

        /* Shader should be added in the last */
        vertex_shader_strings[counter] = VERTEX_SHADER;
        vertex_shader_string_lengths[counter] = (GLint) strlen(VERTEX_SHADER);
        fragment_shader_strings[counter] = FRAGMENT_SHADER;
        fragment_shader_string_lengths[counter] = (GLint) strlen(FRAGMENT_SHADER);
        counter++;

        program_list_[i] = new GLProgram(vertex_shader_strings,
                    vertex_shader_string_lengths, fragment_shader_strings,
                    fragment_shader_string_lengths, counter);
    }
}

AssimpShader::~AssimpShader() {
    if (program_list_ != 0) {
        recycle();
    }
}

void AssimpShader::recycle() {
    if (program_list_ != 0) {
        for (int i = 0; i < AS_TOTAL_GL_PROGRAM_COUNT; i++) {
            if (program_list_[i] != 0) {
                delete program_list_[i];
            }
        }
        delete program_list_;
        program_list_ = 0;
        program_ = 0;
    }
}

void AssimpShader::render(const glm::mat4& mv_matrix,
        const glm::mat4& mv_it_matrix, const glm::mat4& mvp_matrix,
        RenderData* render_data, Material* material) {
    Mesh* mesh = render_data->mesh();
    Texture* texture;
    int feature_set = material->get_shader_feature_set();

    /* Get the texture only diffuse texture is set */
    if (ISSET(feature_set, AS_DIFFUSE_TEXTURE)) {
        texture = material->getTexture("main_texture");
        if (texture->getTarget() != GL_TEXTURE_2D) {
            std::string error =
                    "TextureShader::render : texture with wrong target.";
            throw error;
        }
    }

    /* Based on feature set get the shader program, feature set cannot exceed program count */
    program_ = program_list_[feature_set & (AS_TOTAL_GL_PROGRAM_COUNT - 1)];

    u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
    u_diffuse_color_ = glGetUniformLocation(program_->id(), "u_diffuse_color");
    u_ambient_color_ = glGetUniformLocation(program_->id(), "u_ambient_color");
    u_color_ = glGetUniformLocation(program_->id(), "u_color");
    u_opacity_ = glGetUniformLocation(program_->id(), "u_opacity");

    /* Get common attributes and uniforms from material */
    glm::vec3 color = material->getVec3("color");
    float opacity = material->getFloat("opacity");

#if _GVRF_USE_GLES3_
    mesh->generateVAO();

    glUseProgram(program_->id());
    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(mvp_matrix));

    if (ISSET(feature_set, AS_DIFFUSE_TEXTURE)) {
        glActiveTexture (GL_TEXTURE0);
        glBindTexture(texture->getTarget(), texture->getId());
        glUniform1i(u_texture_, 0);
    } else {
        glm::vec4 diffuse_color = material->getVec4("diffuse_color");
        glm::vec4 ambient_color = material->getVec4("ambient_color");
        glUniform4f(u_diffuse_color_, diffuse_color.r, diffuse_color.g,
                diffuse_color.b, diffuse_color.a);
        glUniform4f(u_ambient_color_, ambient_color.r, ambient_color.g,
                ambient_color.b, ambient_color.a);
    }

    /* Set up bones if AS_SKINNING is set */
    if (ISSET(feature_set, AS_SKINNING)) {
        a_bone_indices_ = glGetAttribLocation(program_->id(), "a_bone_indices");
        a_bone_weights_ = glGetAttribLocation(program_->id(), "a_bone_weights");
        u_bone_matrix_ = glGetUniformLocation(program_->id(), "u_bone_matrix");

        mesh->setBoneLoc(a_bone_indices_, a_bone_weights_, u_bone_matrix_);
        mesh->generateBoneArrayBuffers(Material::ASSIMP_SHADER);

        glUniformMatrix4fv(u_bone_matrix_, mesh->getVertexBoneData().getNumBones(),
                GL_FALSE, glm::value_ptr(mesh->getVertexBoneData().boneMatrices[0]));
    }

    glUniform3f(u_color_, color.r, color.g, color.b);
    glUniform1f(u_opacity_, opacity);

    glBindVertexArray(mesh->getVAOId(Material::ASSIMP_SHADER));
    glDrawElements(render_data->draw_mode(), mesh->indices().size(), GL_UNSIGNED_SHORT,
            0);
    glBindVertexArray(0);
#else
    glUseProgram(program_->id());

    glVertexAttribPointer(a_position_, 3, GL_FLOAT, GL_FALSE, 0,
            mesh->vertices().data());
    glEnableVertexAttribArray(a_position_);

    glVertexAttribPointer(a_tex_coord_, 2, GL_FLOAT, GL_FALSE, 0,
            mesh->tex_coords().data());
    glEnableVertexAttribArray(a_tex_coord_);

    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(mvp_matrix));

    if (ISSET(feature_set, AS_DIFFUSE_TEXTURE)) {
        glActiveTexture (GL_TEXTURE0);
        glBindTexture(texture->getTarget(), texture->getId());
        glUniform1i(u_texture_, 0);
    } else {
        glm::vec4 diffuse_color = material->getVec4("diffuse_color");
        glm::vec4 ambient_color = material->getVec4("ambient_color");
        glUniform4f(u_diffuse_color_, diffuse_color.x, diffuse_color.y, diffuse_color.z, diffuse_color.w);
        glUniform4f(u_ambient_color_, ambient_color.x, ambient_color.y, ambient_color.z, ambient_color.w);
    }

    glUniform3f(u_color_, color.r, color.g, color.b);
    glUniform1f(u_opacity_, opacity);

    glDrawElements(render_data->draw_mode(), mesh->indices().size(), GL_UNSIGNED_SHORT,
            mesh->indices().data());
#endif

    checkGlError("AssimpShader::render");
}

}
;
