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

#ifndef LIGHTMAP_SHADER_H_
#define LIGHTMAP_SHADER_H_

#include "shaderbase.h"

namespace gvr {

class LightMapShader: public ShaderBase {
public:
    LightMapShader();
    virtual ~LightMapShader();

    virtual void render(RenderState* rstate, RenderData* render_data, Material* material);

private:
    LightMapShader(const LightMapShader& lightmap_shader);
    LightMapShader(LightMapShader&& lightmap_shader);
    LightMapShader& operator=(const LightMapShader& lightmap_shader);
    LightMapShader& operator=(LightMapShader&& lightmap_shader);

private:
    GLuint u_mvp_;
    GLuint u_texture_;
    GLuint u_lightmap_texture_;
    GLuint u_lightmap_offset_;
    GLuint u_lightmap_scale_;
};

}

#endif
