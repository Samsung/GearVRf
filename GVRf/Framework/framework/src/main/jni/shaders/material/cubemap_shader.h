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

#ifndef CUBEMAP_SHADER_H_
#define CUBEMAP_SHADER_H_

#include "shaderbase.h"

namespace gvr {


class CubemapShader: public ShaderBase {
public:
    CubemapShader();
    virtual ~CubemapShader();

    virtual void render(RenderState* rstate, RenderData* render_data, Material* material);

private:
    void programInit(RenderState* rstate);
    CubemapShader(const CubemapShader& cubemap_shader);
    CubemapShader(CubemapShader&& cubemap_shader);
    CubemapShader& operator=(const CubemapShader& cubemap_shader);
    CubemapShader& operator=(CubemapShader&& cubemap_shader);

private:
    GLint  u_render_mask_;
    GLint u_model_;
    GLint u_mvp_;
    GLint u_texture_;
    GLint u_color_;
    GLint u_opacity_;
};

}

#endif
