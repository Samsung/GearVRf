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
#define LIGHT           1
#define NO_LIGHT        2
#define MULTIVIEW       4
#define NO_MULTIVIEW    8
#define BATCHING        16
#define NO_BATCHING     32

namespace gvr {
static const char USE_MULTIVIEW[] = "#define MULTIVIEW\n";
static const char NOT_USE_MULTIVIEW[] = "#undef MULTIVIEW\n";
static const char version[] = "#version 300 es\n";
static const char USE_LIGHT[] = "#define USE_LIGHT\n";
static const char NOT_USE_LIGHT[] = "#undef USE_LIGHT\n";
static const char USE_BATCHING[] = "#define USE_BATCHING\n";
static const char NOT_USE_BATCHING[] ="#undef USE_BATCHING\n";

static const char VERTEX_SHADER[] =
        "#ifdef MULTIVIEW\n"
        "#extension GL_OVR_multiview2 : enable\n"
        "layout(num_views = 2) in;\n"
        //"flat out int id;\n"
        "#endif\n"
        "uniform mat4 u_proj;\n"
        "in vec3 a_position;\n"
        "in vec2 a_tex_coord;\n"
        "#ifdef MULTIVIEW\n"
        "uniform mat4 u_view_[2];\n"
        "#else\n"
         "uniform mat4 u_view;\n"
        "#endif\n"

        "#ifndef USE_BATCHING\n"
        "uniform mat4 u_model;\n"
        "#endif\n"
        "out vec2 v_tex_coord;\n"
        "#ifdef USE_LIGHT\n"

        "in vec3 a_normal;\n"
        "uniform vec3 u_light_pos;\n"
        "out vec3 v_viewspace_normal;\n"
        "out vec3 v_viewspace_light_direction;\n"
        "#endif\n"
        "#ifdef USE_BATCHING\n"
        "in float a_matrix_index;\n"
        "uniform vec4 u_matrices[240];\n"
        "#endif\n"
        "\n"
        "void main() {\n"
        "mat4 mv;\n"
        "mat4 mv_it;\n"
        "mat4 mvp;\n"
        "#ifdef USE_BATCHING\n"
        "int index =int(a_matrix_index);\n"
        "mat4 model_matrix = mat4(u_matrices[index*4],u_matrices[index*4+1],u_matrices[index*4+2],u_matrices[index*4+3]);\n"
        "#else\n"
        "mat4 model_matrix = u_model;\n"
        "#endif\n"
        "#ifdef MULTIVIEW\n"
        "mvp = u_proj * u_view_[gl_ViewID_OVR] * model_matrix; \n"
        "#else\n"
        "mvp = u_proj * u_view * model_matrix; \n"
        "#endif\n"

        // use light
        "#ifdef USE_LIGHT\n"
        "#ifdef MULTIVIEW\n"
        "mv = u_view_[gl_ViewID_OVR] * model_matrix; \n"
        "#else\n"
        "mv = u_view * u_model;\n"
        "#endif\n"
        "mv_it = transpose(inverse(mv));\n"
        "  vec4 v_viewspace_position_vec4 = mv * vec4(a_position,1.0);\n"
        "  vec3 v_viewspace_position = v_viewspace_position_vec4.xyz / v_viewspace_position_vec4.w;\n"
        "  v_viewspace_light_direction = u_light_pos - v_viewspace_position;\n"
        "  v_viewspace_normal = (mv_it * vec4(a_normal, 1.0)).xyz;\n"
        "#endif\n"
        "  v_tex_coord = a_tex_coord.xy;\n"
        "  gl_Position = mvp * vec4(a_position,1.0);\n"
        "}\n";

static const char FRAGMENT_SHADER[] =
        "precision highp float;\n"
        "out vec4 out_color;\n"
        "uniform sampler2D u_texture;\n"
        "uniform vec3 u_color;\n"
        "uniform float u_opacity;\n"
        "in vec2 v_tex_coord;\n"
        "#ifdef USE_LIGHT\n"
        "uniform vec4 materialAmbientColor;\n"
        "uniform vec4 materialDiffuseColor;\n"
        "uniform vec4 materialSpecularColor;\n"
        "uniform float materialSpecularExponent;\n"
        "uniform vec4 lightAmbientIntensity;\n"
        "uniform vec4 lightDiffuseIntensity;\n"
        "uniform vec4 lightSpecularIntensity;\n"
        "in vec3 v_viewspace_normal;\n"
        "in vec3 v_viewspace_light_direction;\n"
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
        "  color *= texture(u_texture, v_tex_coord);\n"
		"\n"
        "  // Specular Light\n"
        "  vec3 reflection = normalize(reflect(-normalize(v_viewspace_light_direction), normalize(v_viewspace_normal)));\n"
        "  float specular = max(0.0, dot(normalize(v_viewspace_normal), reflection));\n"
        "  if(diffuse != 0.0) {\n"
        "    color += pow(specular, materialSpecularExponent) * materialSpecularColor * lightSpecularIntensity;\n"
        "  }\n"
        "#else\n"
        "  color = texture(u_texture, v_tex_coord);\n"
		"#endif\n"
        "\n"
        "  out_color = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);\n"
        "}\n";

TextureShader::TextureShader() {}

TextureShader::~TextureShader() {
    for(auto it= program_object_map_.begin();it!=program_object_map_.end();it++){
        GLProgram* program = it->second;
        delete program;
    }

}
void TextureShader::initUniforms(int feature_set, GLuint program_id,uniforms& locations ){

    locations.u_texture = glGetUniformLocation(program_id, "u_texture");
    locations.u_color = glGetUniformLocation(program_id, "u_color");
    locations.u_opacity = glGetUniformLocation(program_id, "u_opacity");
    locations.u_proj = glGetUniformLocation(program_id, "u_proj");

    if(feature_set & LIGHT){
        locations.u_light_pos = glGetUniformLocation(program_id, "u_light_pos");
        locations.u_material_ambient_color_ = glGetUniformLocation(program_id,
                "materialAmbientColor");
        locations.u_material_diffuse_color_ = glGetUniformLocation(program_id,
                "materialDiffuseColor");
        locations.u_material_specular_color_ = glGetUniformLocation(program_id,
                "materialSpecularColor");
        locations.u_material_specular_exponent_ = glGetUniformLocation(program_id,
                "materialSpecularExponent");
        locations.u_light_ambient_intensity_ = glGetUniformLocation(program_id,
                "lightAmbientIntensity");
        locations.u_light_diffuse_intensity_ = glGetUniformLocation(program_id,
                "lightDiffuseIntensity");
        locations.u_light_specular_intensity_ = glGetUniformLocation(program_id,
                "lightSpecularIntensity");
    }
    if(feature_set & MULTIVIEW)
        locations.u_view = glGetUniformLocation(program_id, "u_view_[0]");
    else
        locations.u_view = glGetUniformLocation(program_id, "u_view");

    if(feature_set & BATCHING)
        locations.u_model = glGetUniformLocation(program_id, "u_matrices[0]");
    else
        locations.u_model = glGetUniformLocation(program_id, "u_model");
}

void TextureShader::programInit(RenderState* rstate, RenderData* render_data, Material* material,
        const std::vector<glm::mat4>& model_matrix,int drawcount, bool batching){

    if(!material->isMainTextureReady())
        return;

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

    bool batching_enabled = batching;
    int feature_set =0;
    feature_set |= (use_light) ? LIGHT : NO_LIGHT;
    feature_set |= (use_multiview) ? MULTIVIEW : NO_MULTIVIEW;
    feature_set |= (batching_enabled) ? BATCHING : NO_BATCHING;

    bool properties [] = {use_light, use_multiview, batching_enabled};
    const char* feature_strings[2][3]={{NOT_USE_LIGHT, NOT_USE_MULTIVIEW, NOT_USE_BATCHING},
            {USE_LIGHT, USE_MULTIVIEW, USE_BATCHING}};

    int feature_string_lengths[2][3]={{strlen(NOT_USE_LIGHT), strlen(NOT_USE_MULTIVIEW), strlen(NOT_USE_BATCHING)},
            {strlen(USE_LIGHT),strlen(USE_MULTIVIEW), strlen(USE_BATCHING)}};

    uniforms uniform_locations;
    GLProgram* prgram = nullptr;
    if(program_object_map_.find(feature_set)==program_object_map_.end()){

        const char* vertex_shader_strings[5];
        GLint vertex_shader_string_lengths[5];
        vertex_shader_strings[0]=version;
        vertex_shader_strings[4]=VERTEX_SHADER;
        vertex_shader_string_lengths[0]= (GLint) strlen(version);
        vertex_shader_string_lengths[4]= (GLint) strlen(VERTEX_SHADER);

        const char* frag_shader_strings[5];
        GLint frag_shader_string_lengths[5];
        frag_shader_strings[0]=version;
        frag_shader_strings[4]=FRAGMENT_SHADER;
        frag_shader_string_lengths [0] = vertex_shader_string_lengths[0];
        frag_shader_string_lengths [4] = (GLint) strlen(FRAGMENT_SHADER);

        int index = 1;
        for(int i=0;i<3; i++){
            vertex_shader_strings[index]= feature_strings[properties[i]][i];
            vertex_shader_string_lengths [index]= feature_string_lengths[properties[i]][i];
            frag_shader_strings[index]=vertex_shader_strings[index];
            frag_shader_string_lengths[index] = vertex_shader_string_lengths [index];
            index++;
        }
        prgram = new GLProgram(vertex_shader_strings,
                vertex_shader_string_lengths, frag_shader_strings,
                frag_shader_string_lengths, 5);
        program_object_map_[feature_set] = prgram;

        initUniforms(feature_set, prgram->id(), uniform_locations);
        uniform_loc[feature_set] = uniform_locations;
    }
    else {
        prgram = program_object_map_[feature_set];
        uniform_locations = uniform_loc[feature_set];
    }

    program_ = prgram;
    GLuint programId = prgram->id();
    //render_data->mesh()->generateVAO(programId);
    GL(glUseProgram(programId));
    GL(glActiveTexture (GL_TEXTURE0));
    GL(glBindTexture(texture->getTarget(), texture->getId()));

    glUniform1i(uniform_locations.u_texture, 0);
    glUniform3f(uniform_locations.u_color, color.r, color.g, color.b);
    glUniform1f(uniform_locations.u_opacity, opacity);

    if(!batching_enabled)
        glUniformMatrix4fv(uniform_locations.u_model, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_model));

    glUniformMatrix4fv(uniform_locations.u_proj, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_proj));
    if (use_light) {
        glm::vec3 light_position = light->getVec3("world_position");
        glm::vec4 light_ambient_intensity = light->getVec4("ambient_intensity");
        glm::vec4 light_diffuse_intensity = light->getVec4("diffuse_intensity");
        glm::vec4 light_specular_intensity = light->getVec4(
                "specular_intensity");

        glUniform3f(uniform_locations.u_light_pos, light_position.x, light_position.y,
                light_position.z);

        glUniform4f(uniform_locations.u_material_ambient_color_, material_ambient_color.r,
                material_ambient_color.g, material_ambient_color.b,
                material_ambient_color.a);
        glUniform4f(uniform_locations.u_material_diffuse_color_, material_diffuse_color.r,
                material_diffuse_color.g, material_diffuse_color.b,
                material_diffuse_color.a);
        glUniform4f(uniform_locations.u_material_specular_color_, material_specular_color.r,
                material_specular_color.g, material_specular_color.b,
                material_specular_color.a);
        glUniform1f(uniform_locations.u_material_specular_exponent_, material_specular_exponent);
        glUniform4f(uniform_locations.u_light_ambient_intensity_, light_ambient_intensity.r,
                light_ambient_intensity.g, light_ambient_intensity.b,
                light_ambient_intensity.a);
        glUniform4f(uniform_locations.u_light_diffuse_intensity_, light_diffuse_intensity.r,
                light_diffuse_intensity.g, light_diffuse_intensity.b,
                light_diffuse_intensity.a);
        glUniform4f(uniform_locations.u_light_specular_intensity_, light_specular_intensity.r,
                light_specular_intensity.g, light_specular_intensity.b,
                light_specular_intensity.a);

    }

    if(use_multiview)
        glUniformMatrix4fv(uniform_locations.u_view, 2, GL_FALSE, glm::value_ptr(rstate->uniforms.u_view_[0]));
    else
        glUniformMatrix4fv(uniform_locations.u_view, 1, GL_FALSE, glm::value_ptr(rstate->uniforms.u_view));


    if(batching){
        glUniform4fv(uniform_locations.u_model, drawcount*4, &model_matrix[0][0][0]);
        glBindVertexArray(render_data->mesh()->getVAOId(programId));
    }


}
void TextureShader::render(RenderState* rstate,
        RenderData* render_data, Material* material) {

   std::vector<glm::mat4> model_matrix;
   model_matrix.push_back(rstate->uniforms.u_model);
   programInit(rstate,render_data,material,model_matrix,1,false);
   checkGlError("TextureShader::render");
}
void TextureShader::render_batch(const std::vector<glm::mat4>& model_matrix,
        RenderData* render_data,  RenderState& rstate, unsigned int indexCount, int drawcount)
{

    uniforms uniform_locations;
    programInit(&rstate,render_data,rstate.material_override,model_matrix,drawcount,true);

    if(use_multiview)
        glDrawElementsInstanced(render_data->draw_mode(),indexCount, GL_UNSIGNED_SHORT, NULL, 2 );
    else
        GL(glDrawElements(render_data->draw_mode(), indexCount, GL_UNSIGNED_SHORT,
            0));

    GL(glBindVertexArray(0));
    checkGlError(" TextureShader::render_batch");

}
}
;
