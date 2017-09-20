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
    RenderTarget(RenderTexture*, bool is_multiview);
    RenderTarget(Scene*);
    RenderTarget(RenderTexture*, const RenderTarget* source);
    RenderTarget();
    ~RenderTarget();
    void            setMainScene(Scene* scene){mRenderState.scene = scene;}
    void            setCamera(Camera* cam) { mRenderState.camera= cam; }
    Camera*         getCamera() const { return mRenderState.camera; }
    bool            hasTexture() const { return (mRenderTexture != nullptr); }\
    RenderTexture*  getTexture()  { return mRenderTexture; }
    void            setTexture(RenderTexture* texture);
    RenderState&    getRenderState() { return mRenderState; }
    virtual void    beginRendering(Renderer* renderer);
    virtual void    endRendering(Renderer* renderer);
    static long long getComponentType() { return COMPONENT_TYPE_RENDER_TARGET; }
    std::vector<RenderData*>* getRenderDataVector(){
        return mRenderDataVector.get();
    }
    virtual void cullFromCamera(Scene*, Camera* camera, Renderer* renderer, ShaderManager* shader_manager);
private:
    RenderTarget(const RenderTarget& render_texture);
    RenderTarget(RenderTarget&& render_texture);
    RenderTarget& operator=(const RenderTarget& render_texture);
    RenderTarget& operator=(RenderTarget&& render_texture);

protected:
    RenderState     mRenderState;
    RenderTexture*  mRenderTexture;
    std::shared_ptr<std::vector<RenderData*>> mRenderDataVector;
    //Camera*         mCamera;
};

}
#endif
