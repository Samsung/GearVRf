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
 * Renders a post effect which blends color to the whole scene.
 ***************************************************************************/

#ifndef COLOR_BLEND_POST_EFFECT_SHADER_H_
#define COLOR_BLEND_POST_EFFECT_SHADER_H_

#include <memory>
#include <vector>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"

#include "objects/hybrid_object.h"
#include "engine/memory/gl_delete.h"

namespace gvr {
class GLProgram;
class RenderTexture;
class PostEffectData;

class ColorBlendPostEffectShader: public HybridObject {
public:
    ColorBlendPostEffectShader();
    virtual ~ColorBlendPostEffectShader();

    void render(RenderTexture* render_texture,
            PostEffectData* post_effect_data,
            std::vector<glm::vec3>& vertices,
            std::vector<glm::vec2>& tex_coords,
            std::vector<unsigned short>& triangles);

private:
    ColorBlendPostEffectShader(
            const ColorBlendPostEffectShader& color_blend_post_effect_shader);
    ColorBlendPostEffectShader(
            ColorBlendPostEffectShader&& color_blend_post_effect_shader);
    ColorBlendPostEffectShader& operator=(
            const ColorBlendPostEffectShader& color_blend_post_effect_shader);
    ColorBlendPostEffectShader& operator=(
            ColorBlendPostEffectShader&& color_blend_post_effect_shader);

private:
    GLProgram* program_;
    GLuint a_position_;
    GLuint a_tex_coord_;
    GLuint u_texture_;
    GLuint u_color_;
    GLuint u_factor_;

    // add vertex array object
    GLuint vaoID_;
    GlDelete* deleter_;
};

}
#endif
