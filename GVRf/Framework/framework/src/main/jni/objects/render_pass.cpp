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

#include <memory>
#include <glslang/Include/Common.h>
#include "engine/renderer/renderer.h"
#include "render_pass.h"
#include <string>
#include <sstream>

template <typename T>
std::string to_string(T value)
{
    std::ostringstream os ;
    os << value ;
    return os.str() ;
}

namespace gvr {

RenderPass::RenderPass() :
        material_(0),  shader_dirty_(true)
{
    shaderID_[0] = -1;
    shaderID_[1] = -1;
    render_modes_.init();
}


void RenderPass::set_material(ShaderData* material)
{
    if (material != material_)
    {
        material_ = material;
        markDirty();
    }
}


void RenderPass::set_shader(int shaderid, bool useMultiview)
{
    if (shaderID_[useMultiview] != shaderid)
    {
        shaderID_[useMultiview] = shaderid;
        markDirty();
    }
}

}
