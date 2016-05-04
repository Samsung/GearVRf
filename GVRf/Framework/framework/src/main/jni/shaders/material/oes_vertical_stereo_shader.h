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
 * Renders a vertically split GL_TEXTURE_EXTERNAL_OES texture.
 ***************************************************************************/

#ifndef OES_VERTICAL_STEREO_SHADER_H_
#define OES_VERTICAL_STEREO_SHADER_H_

#include "shaderbase.h"
#include "objects/eye_type.h"


namespace gvr {

class OESVerticalStereoShader: public ShaderBase {
public:
    OESVerticalStereoShader();
    virtual ~OESVerticalStereoShader();

    virtual void render(RenderState* rstate, RenderData* render_data, Material* material);

private:
    OESVerticalStereoShader(
            const OESVerticalStereoShader& oes_vertical_stereo_shader);
    OESVerticalStereoShader(
            OESVerticalStereoShader&& oes_vertical_stereo_shader);
    OESVerticalStereoShader& operator=(
            const OESVerticalStereoShader& oes_vertical_stereo_shader);
    OESVerticalStereoShader& operator=(
            OESVerticalStereoShader&& oes_vertical_stereo_shader);

private:
    GLuint u_mvp_;
    GLuint u_texture_;
    GLuint u_color_;
    GLuint u_opacity_;
    GLuint u_right_;
};

}
#endif
