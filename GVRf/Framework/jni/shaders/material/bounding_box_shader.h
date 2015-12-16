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
 * Renders a bounding box for occlusion query.
 ***************************************************************************/

#ifndef BOUNDING_BOX_SHADER_H_
#define BOUNDING_BOX_SHADER_H_

#include <memory>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/hybrid_object.h"

namespace gvr {
class GLProgram;
class RenderData;
class Material;

class BoundingBoxShader: public HybridObject {
public:
    BoundingBoxShader();
    virtual ~BoundingBoxShader();

    void render(const glm::mat4& mvp_matrix, RenderData* render_data, Material* material);

private:
    BoundingBoxShader(const BoundingBoxShader& bounding_box_shader);
    BoundingBoxShader(BoundingBoxShader&& bounding_box_shader);
    BoundingBoxShader& operator=(const BoundingBoxShader& bounding_box_shader);
    BoundingBoxShader& operator=(BoundingBoxShader&& bounding_box_shader);

private:
    GLProgram* program_;
    GLuint u_mvp_;
};

}

#endif
