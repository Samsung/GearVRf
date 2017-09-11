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
 * Renders a horizontally split texture without light.
 ***************************************************************************/

#ifndef UNLIT_HORIZONTAL_STEREO_SHADER_H_
#define UNLIT_HORIZONTAL_STEREO_SHADER_H_

#include "shaderbase.h"
#include "objects/eye_type.h"

namespace gvr {


class UnlitHorizontalStereoShader: public ShaderBase {
public:
    UnlitHorizontalStereoShader();
    virtual ~UnlitHorizontalStereoShader();

    virtual void render(RenderState* rstate, RenderData* render_data, Material* material);

private:
    UnlitHorizontalStereoShader(
            const UnlitHorizontalStereoShader& unlit_shader);
    UnlitHorizontalStereoShader(UnlitHorizontalStereoShader&& unlit_shader);
    UnlitHorizontalStereoShader& operator=(
            const UnlitHorizontalStereoShader& unlit_shader);
    UnlitHorizontalStereoShader& operator=(
            UnlitHorizontalStereoShader&& unlit_shader);

private:
    GLint u_mvp_;
    GLint u_texture_;
    GLint u_color_;
    GLint u_opacity_;
    GLint u_right_;
};

}

#endif
