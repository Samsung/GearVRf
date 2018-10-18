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

#ifndef SHADOW_SORTER_H_
#define SHADOW_SORTER_H_

#include "render_sorter.h"

namespace gvr {

/**
 * RenderSorter variant used for shadow mapping.
 * Renderables are sorted based on whether they are skinned or not.
 * One of two shaders is used for creating shadow maps.
 * The material, shader and render modes from the RenderPass
 * objects are ignored.
 */
class ShadowRenderSorter : public RenderSorter
{
public:
    ShadowRenderSorter(ShaderData& material,
                       Renderer& renderer,
                       int numMatrices = 0,
                       bool forceTransformBlock = false)
    : RenderSorter(renderer, "ShadowSorter", numMatrices, false),
      mShadowMaterial(material)
    {
        mDepthShader[0] = nullptr;
        mDepthShader[1] = nullptr;
        mShadowRenderMode.init();
        mShadowRenderMode.setAlphaBlend(false);
        mShadowRenderMode.setUseLights(false);
    }

protected:
    virtual Shader* selectShader(const RenderState& rstate, Renderable& r);
    virtual bool    isValid(RenderState& rstate, Renderable& r);

private:
    ShadowRenderSorter(const ShadowRenderSorter&) = delete;
    ShadowRenderSorter(ShadowRenderSorter&&) = delete;
    ShadowRenderSorter& operator=(const ShadowRenderSorter& render_engine) = delete;
    ShadowRenderSorter& operator=(ShadowRenderSorter&& render_engine) = delete;
    ShaderData& mShadowMaterial;
    Shader*     mDepthShader[2];
    RenderModes mShadowRenderMode;
};

}
#endif
