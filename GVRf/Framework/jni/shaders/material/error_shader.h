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
 * GL program for rendering a object with an error.
 ***************************************************************************/

#ifndef SOLID_COLOR_SHADER_H_
#define SOLID_COLOR_SHADER_H_

#include <memory>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/recyclable_object.h"

namespace gvr {
class Color;
class GLProgram;
class RenderData;

class ErrorShader: public RecyclableObject {
public:
    ErrorShader();
    ~ErrorShader();
    void recycle();
    void render(const glm::mat4& mvp_matrix,
            std::shared_ptr<RenderData> render_data);

private:
    ErrorShader(const ErrorShader& error_shader);
    ErrorShader(ErrorShader&& error_shader);
    ErrorShader& operator=(const ErrorShader& error_shader);
    ErrorShader& operator=(ErrorShader&& error_shader);

private:
    GLProgram* program_;
    GLuint a_position_;
    GLuint u_mvp_;
    GLuint u_color_;
};

}
#endif
