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

#include "objects/components/component.h"
#include "objects/components/camera.h"
#include "engine/renderer/renderer.h"


namespace gvr {
class ShaderManager;
class Scene;
class RenderTexture;
class GLTexture;

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
    RenderTarget(RenderTexture*);
    RenderTarget(int width, int height, GLTexture* tex);
    RenderTarget();
    virtual ~RenderTarget();

    void            setCamera(Camera* cam) { mCamera = cam; }
    Camera*         getCamera() const { return mCamera; }
    bool            hasTexture() const { return (mRenderTexture != nullptr); }\
    RenderTexture*  getTexture() const { return mRenderTexture; }
    void            setTexture(RenderTexture* texture);
    RenderState&    getRenderState() { return mRenderState; }
    virtual void    beginRendering();
    virtual void    endRendering();
    static long long getComponentType() { return COMPONENT_TYPE_RENDER_TARGET; }

private:
    RenderTarget(const RenderTarget& render_texture);
    RenderTarget(RenderTarget&& render_texture);
    RenderTarget& operator=(const RenderTarget& render_texture);
    RenderTarget& operator=(RenderTarget&& render_texture);

protected:
    RenderState     mRenderState;
    RenderTexture*  mRenderTexture;
    Camera*         mCamera;
};

}
#endif
