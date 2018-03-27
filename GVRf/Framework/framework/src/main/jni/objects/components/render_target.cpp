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
#include "component.inl"
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
RenderTarget::RenderTarget(RenderTexture* tex, bool is_multiview)
: Component(RenderTarget::getComponentType()),mNextRenderTarget(nullptr),
  mRenderTexture(tex),mRenderDataVector(std::make_shared< std::vector<RenderData*>>())
{
    mRenderState.is_shadow = false;
    mRenderState.shadow_map = nullptr;
    mRenderState.material_override = NULL;
    mRenderState.is_multiview = is_multiview;
    if (nullptr != mRenderTexture) {
        mRenderState.sampleCount = mRenderTexture->getSampleCount();
    }
}
void RenderTarget::beginRendering(Renderer *renderer) {
    mRenderTexture->useStencil(renderer->useStencilBuffer());
    mRenderState.viewportWidth = mRenderTexture->width();
    mRenderState.viewportHeight = mRenderTexture->height();
    mRenderState.sampleCount = mRenderTexture->getSampleCount();
    if (-1 != mRenderState.camera->background_color_r())
    {
        mRenderTexture->setBackgroundColor(mRenderState.camera->background_color_r(),
                                           mRenderState.camera->background_color_g(),
                                           mRenderState.camera->background_color_b(), mRenderState.camera->background_color_a());
    }
}
void RenderTarget::endRendering(Renderer *renderer) {
    mRenderTexture->endRendering(renderer);
}
RenderTarget::RenderTarget(Scene* scene)
: Component(RenderTarget::getComponentType()), mNextRenderTarget(nullptr), mRenderTexture(nullptr),mRenderDataVector(std::make_shared< std::vector<RenderData*>>()){
    mRenderState.is_shadow = false;
    mRenderState.shadow_map = nullptr;
    mRenderState.material_override = NULL;
    mRenderState.is_multiview = false;
    mRenderState.scene = scene;

}
RenderTarget::RenderTarget(RenderTexture* tex, const RenderTarget* source)
        : Component(RenderTarget::getComponentType()),mNextRenderTarget(nullptr),
          mRenderTexture(tex), mRenderDataVector(source->mRenderDataVector)
{
    mRenderState.is_shadow = false;
    mRenderState.shadow_map = nullptr;
    mRenderState.material_override = NULL;
    mRenderState.is_multiview = false;
}
/**
 * Constructs an empty render target without a render texture.
 * This component will not render anything until a RenderTexture
 * is provided.
 */
RenderTarget::RenderTarget()
:   Component(RenderTarget::getComponentType()),
    mRenderTexture(nullptr),mNextRenderTarget(nullptr), mRenderDataVector(std::make_shared< std::vector<RenderData*>>())
{
    mRenderState.is_multiview = false;
    mRenderState.shadow_map = nullptr;
    mRenderState.is_shadow = false;
    mRenderState.material_override = NULL;
}

void RenderTarget::cullFromCamera(Scene* scene, jobject javaSceneObject, Camera* camera, Renderer* renderer, ShaderManager* shader_manager){

    renderer->cullFromCamera(scene, javaSceneObject, camera,shader_manager, mRenderDataVector.get(),mRenderState.is_multiview);
    scene->getLights().shadersRebuilt();
    renderer->state_sort(mRenderDataVector.get());
}

RenderTarget::~RenderTarget()
{
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
}

}
