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
 * Renders a cube map texture in reflection mode without light.
 ***************************************************************************/

#ifndef CUBEMAP_REFLECTION_SHADER_H_
#define CUBEMAP_REFLECTION_SHADER_H_

#include <memory>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/recyclable_object.h"

namespace gvr {
class GLProgram;
class RenderData;

class CubemapReflectionShader: public RecyclableObject {
public:
	CubemapReflectionShader();
    ~CubemapReflectionShader();
    void recycle();
    void render(const glm::mat4& model_matrix, const glm::mat4& model_it_matrix,
    		const glm::mat4& view_invers_matrix, const glm::mat4& mvp_matrix,
            RenderData* render_data);

private:
    CubemapReflectionShader(const CubemapReflectionShader& cubemap_shader);
    CubemapReflectionShader(CubemapReflectionShader&& cubemap_shader);
    CubemapReflectionShader& operator=(const CubemapReflectionShader& cubemap_shader);
    CubemapReflectionShader& operator=(CubemapReflectionShader&& cubemap_shader);

private:
    GLProgram* program_;
    GLuint a_position_;
    GLuint a_normal_;
    GLuint u_mv_;
    GLuint u_mv_it_;
    GLuint u_mvp_;
    GLuint u_view_i_;
    GLuint u_texture_;
    GLuint u_color_;
    GLuint u_opacity_;
};

}

#endif
