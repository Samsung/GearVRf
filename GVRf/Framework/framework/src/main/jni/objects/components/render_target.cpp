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

#include "render_target.h"
#include "engine/renderer/render_sorter.h"
#include "objects/textures/render_texture.h"
#include "objects/light.h" // for DEBUG_LIGHT
#include "objects/scene.h"

namespace gvr {

/**
 * Constructs a render target component which renders to a designated texture.
 * The scene will be rendered from the viewpoint of the scene object
 * the RenderTarget is attached to. Nothing will be rendered if
 * the render target is not attached to a scene object or
 * if it does not have a texture.
 *
 * If a RenderTarget is actually a ShadowMap, it is rendered
 * automatically by the lighting code. Otherwise, the
 * Java application is responsible for initiating rendering.
 *
 * @param texture RenderTexture to render to
 */
RenderTarget::RenderTarget(RenderTexture* tex, bool is_multiview, bool is_stereo)
:   Component(RenderTarget::getComponentType()),
    mNextRenderTarget(nullptr),
    mRenderTexture(tex)
{
    mRenderState.is_shadow = false;
    mRenderState.shadow_map = nullptr;
    mRenderState.is_stereo = is_stereo;
    mRenderState.is_multiview = is_multiview;
    if (nullptr != mRenderTexture)
    {
        mRenderState.sampleCount = mRenderTexture->getSampleCount();
    }
}

RenderTarget::RenderTarget(Scene* scene, bool is_stereo)
  :   Component(RenderTarget::getComponentType()),
      mNextRenderTarget(nullptr),
      mRenderTexture(nullptr)
{
    mRenderState.is_shadow = false;
    mRenderState.shadow_map = nullptr;
    mRenderState.is_multiview = false;
    mRenderState.is_stereo = is_stereo;
}

RenderTarget::RenderTarget(RenderTexture* tex, const RenderTarget* source)
    : Component(RenderTarget::getComponentType()),
      mNextRenderTarget(nullptr),
      mRenderTexture(tex)
{
    mRenderState.is_shadow = false;
    mRenderState.shadow_map = nullptr;
    mRenderState.is_stereo = source->mRenderState.is_stereo;
    mRenderState.is_multiview = source->mRenderState.is_multiview;
    mRenderState.sampleCount = mRenderTexture->getSampleCount();
}

void RenderTarget::cullFromCamera(Scene* scene, jobject javaSceneObject, Camera* camera, ShaderManager* shader_manager)
{
    checkGLError("RenderTarget::cullFromCamera");
    mRenderState.camera = camera;
    mRenderState.scene = scene;
    mRenderState.javaSceneObject = javaSceneObject;
    mRenderState.shader_manager = shader_manager;
    // TODO: lock scene graph matrices around cull
    mRenderSorter->cull(mRenderState);
    // TODO: can unlock matrices here - they are copied
    mRenderSorter->sort(mRenderState);
    mRenderState.javaSceneObject = nullptr;
}

void RenderTarget::beginRendering()
{
    if (mRenderTexture == nullptr)
    {
        return;
    }
    Renderer& renderer = mRenderSorter->getRenderer();
    mRenderTexture->useStencil(renderer.useStencilBuffer());
    mRenderState.sampleCount = mRenderTexture->getSampleCount();
    if (-1 != mRenderState.camera->background_color_r())
    {
        mRenderTexture->setBackgroundColor(mRenderState.camera->background_color_r(),
                                           mRenderState.camera->background_color_g(),
                                           mRenderState.camera->background_color_b(), mRenderState.camera->background_color_a());
    }
    checkGLError("RenderTarget::beginRendering");
    mRenderTexture->beginRendering(&renderer);
}

void RenderTarget::endRendering()
{
    if (mRenderTexture)
    {
        mRenderTexture->endRendering(&(mRenderSorter->getRenderer()));
        checkGLError("RenderTarget::endRendering");
    }
}

void RenderTarget::render()
{
    mRenderSorter->render(mRenderState);
}

void RenderTarget::setRenderSorter(RenderSorter* sorter)
{
    mRenderSorter = sorter;
}

/**
 * Designates the RenderTexture this RenderTarget should render to.
 * @param RenderTexture to render to
 * @see #getTexture()
 * @see RenderTexture
 */
void RenderTarget::setTexture(RenderTexture* texture)
{
    mRenderTexture = texture;
    mRenderState.sampleCount = mRenderTexture->getSampleCount();
}

}
