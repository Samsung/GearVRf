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

#include <memory>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/recyclable_object.h"

namespace gvr {
class GLProgram;
class RenderData;

class CubemapShader: public RecyclableObject {
public:
	CubemapShader();
    ~CubemapShader();
    void recycle();
    void render(const glm::mat4& model_matrix, const glm::mat4& mvp_matrix,
            std::shared_ptr<RenderData> render_data);

private:
    CubemapShader(const CubemapShader& cubemap_shader);
    CubemapShader(CubemapShader&& cubemap_shader);
    CubemapShader& operator=(const CubemapShader& cubemap_shader);
    CubemapShader& operator=(CubemapShader&& cubemap_shader);

private:
    GLProgram* program_;
    GLuint a_position_;
    GLuint u_model_;
    GLuint u_mvp_;
    GLuint u_texture_;
    GLuint u_color_;
    GLuint u_opacity_;
};

}

#endif
