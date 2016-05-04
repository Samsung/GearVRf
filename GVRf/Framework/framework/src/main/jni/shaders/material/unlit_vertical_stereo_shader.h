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
 * Renders a vertically split texture without light.
 ***************************************************************************/

#ifndef UNLIT_VERTICAL_STEREO_SHADER_H_
#define UNLIT_VERTICAL_STEREO_SHADER_H_

#include "shaderbase.h"
#include "objects/eye_type.h"

namespace gvr {


class UnlitVerticalStereoShader: public ShaderBase {
public:
    UnlitVerticalStereoShader();
    virtual ~UnlitVerticalStereoShader();

    virtual void render(RenderState* rstate, RenderData* render_data, Material* material);

private:
    UnlitVerticalStereoShader(const UnlitVerticalStereoShader& unlit_shader);
    UnlitVerticalStereoShader(UnlitVerticalStereoShader&& unlit_shader);
    UnlitVerticalStereoShader& operator=(
            const UnlitVerticalStereoShader& unlit_shader);
    UnlitVerticalStereoShader& operator=(
            UnlitVerticalStereoShader&& unlit_shader);

private:
    GLuint u_mvp_;
    GLuint u_texture_;
    GLuint u_color_;
    GLuint u_opacity_;
    GLuint u_right_;
};

}

#endif
