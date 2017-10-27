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
#include "shadow_map.h"

namespace gvr {

ShadowMap::ShadowMap(Material* mtl)
: RenderTarget(nullptr),
  mLayerIndex(-1)
{
    mRenderState.material_override = mtl;
}

ShadowMap::~ShadowMap()
{
    //@todo somebody else owns the render texture..
    mRenderTexture = nullptr;
}

void ShadowMap::setLayerIndex(int layerIndex)
{
    mLayerIndex = layerIndex;
}

void ShadowMap::bindTexture(int loc, int texIndex)
{
    RenderTextureArray* texArray = static_cast<RenderTextureArray*>(mRenderTexture);

    if (texArray)
    {
        texArray->bindTexture(loc, texIndex);
    }
}

void  ShadowMap::beginRendering()
{
    RenderTextureArray* texArray = static_cast<RenderTextureArray*>(mRenderTexture);

    if (texArray && (mLayerIndex >= 0))
    {
        texArray->bindFrameBuffer(mLayerIndex);
    }
    RenderTarget::beginRendering();
    mRenderState.render_mask = 1;
    mRenderState.shadow_map = true;
}

}
