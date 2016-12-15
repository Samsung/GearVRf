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

#ifndef TEXTURE_SHADER_H_
#define TEXTURE_SHADER_H_

#include "shaderbase.h"
#include <vector>
#include <unordered_map>
namespace gvr {
class GLProgram;

class TextureShader: public ShaderBase {
public:
    TextureShader();
    virtual ~TextureShader();

    virtual void render(RenderState* rstate, RenderData* render_data, Material* material);
    void render_batch(const std::vector<glm::mat4>& model_matrix,
              RenderData* render_data,  RenderState& rstate, unsigned int, int);

private:
    TextureShader(const TextureShader& texture_shader);
    TextureShader(TextureShader&& texture_shader);
    TextureShader& operator=(const TextureShader& texture_shader);
    TextureShader& operator=(TextureShader&& texture_shader);

private:

    std::unordered_map<int, GLProgram*>program_object_map_;
    struct uniforms{
        GLuint u_model;
        GLuint u_texture;
        GLuint u_color;
        GLuint u_opacity;
        GLuint u_view;
        GLuint u_proj;
        GLuint u_light_pos;
        GLuint u_material_ambient_color_;
        GLuint u_material_diffuse_color_;
        GLuint u_material_specular_color_;
        GLuint u_material_specular_exponent_;
        GLuint u_light_ambient_intensity_;
        GLuint u_light_diffuse_intensity_;
        GLuint u_light_specular_intensity_;
    };
    std::unordered_map<int,uniforms> uniform_loc;


public:
    void initUniforms(int, GLuint ,uniforms& );
    void programInit(RenderState* rstate, RenderData* rdata, Material* material, const std::vector<glm::mat4>& model_matrix
            ,int drawcount, bool batching);
};

}

#endif
