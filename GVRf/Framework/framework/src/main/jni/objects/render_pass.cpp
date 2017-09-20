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
#include "engine/renderer/renderer.h"
#include "render_pass.h"

namespace gvr {

RenderPass::RenderPass() :
        material_(0), cull_face_(DEFAULT_CULL_FACE), dirty_(true)
{
    memset(shaderID_,0,sizeof(shaderID_));
}


void RenderPass::set_material(ShaderData* material)
{
    if (material != material_)
    {
        material_ = material;
        markDirty();
    }
}

void RenderPass::set_cull_face(int cull_face)
{
    if (cull_face_ != cull_face)
    {
        cull_face_ = cull_face;
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

int RenderPass::isValid(Renderer* renderer, const RenderState& rstate, RenderData* rdata)
{
    ShaderData* mtl = material();
    int shaderID = get_shader(rstate.is_multiview);
    bool dirty = dirty_;

    if ((shaderID <= 0) || dirty_)
    {
        clearDirty();
    }
    if (mtl->updateGPU(renderer, rdata) <= 0)
    {
        return -1;
    }
    return !dirty;
}


}
