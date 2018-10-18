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


#ifndef RENDER_TARGET_H_
#define RENDER_TARGET_H_

#include <vector>

#include "component.h"
#include "camera.h"
#include "engine/renderer/renderer.h"


namespace gvr {
class ShaderManager;
class Scene;
class RenderTexture;
class GLTexture;
class Renderer;
class RenderSorter;

/**
 * A render target is a component which allows the scene to be rendered
 * into a texture from the viewpoint of a particular scene object.
 * A render target may have a custom camera to allow control
 * over the projection matrix.
 * @see RenderTexture
 * @see ShadowMap
 */
class RenderTarget : public Component
{
public:
    explicit RenderTarget(RenderTexture*, bool is_multiview, bool is_stereo);
    explicit RenderTarget(Scene*, bool is_stereo);
    explicit RenderTarget(RenderTexture*, const RenderTarget* source);
    virtual ~RenderTarget() { };

    static long long getComponentType()         { return COMPONENT_TYPE_RENDER_TARGET; }
    RenderTarget*   getNextRenderTarget()       { return mNextRenderTarget; }
    RenderSorter*   getRenderSorter() const     { return mRenderSorter; }
    bool            isStereo() const            { return mRenderState.is_stereo; }
    void            setStereo(bool flag)        { mRenderState.is_stereo = flag; }
    void            setMainScene(Scene* scene)  { mRenderState.scene = scene;}
    void            setCamera(Camera* cam)      { mRenderState.camera= cam; }
    Camera*         getCamera() const           { return mRenderState.camera; }
    bool            hasTexture() const          { return (mRenderTexture != nullptr); }
    RenderTexture*  getTexture()                { return mRenderTexture; }
    RenderState&    getRenderState()            { return mRenderState; }
    void attachNextRenderTarget(RenderTarget* rt) { mNextRenderTarget = rt; }

    void            setTexture(RenderTexture* texture);
    void            setRenderSorter(RenderSorter* sorter);
    virtual void    beginRendering();
    virtual void    endRendering();
    virtual void    render();
    virtual void    cullFromCamera(Scene*, jobject javaSceneObject, Camera* camera, ShaderManager* shader_manager);

private:
    RenderTarget(const RenderTarget& render_texture) = delete;
    RenderTarget(RenderTarget&& render_texture) = delete;
    RenderTarget& operator=(const RenderTarget& render_texture) = delete;
    RenderTarget& operator=(RenderTarget&& render_texture) = delete;

protected:
    RenderTarget*   mNextRenderTarget;
    RenderState     mRenderState;
    RenderTexture*  mRenderTexture = nullptr;
    RenderSorter*   mRenderSorter;
};

}
#endif
