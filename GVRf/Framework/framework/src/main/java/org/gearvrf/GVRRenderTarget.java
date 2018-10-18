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

package org.gearvrf;

import org.gearvrf.utility.Log;

/**
 * A render target is a component which allows the scene to be rendered
 * into a texture from the viewpoint of a particular scene object.
 * GVRRenderTarget can initiate a DrawFrameListener which causes
 * the scene to be rendered into the texture every frame.
 * To initiate rendering you must call @{link #startListening()}.
 * A render target may also have a custom camera to allow control
 * over the projection matrix.
 * @see GVRRenderTexture
 * @see GVRShadowMap
 */
public class GVRRenderTarget extends GVRBehavior
{
    protected GVRRenderTexture mTexture;
    protected GVRScene  mScene;
    protected GVRCamera mCamera;
    protected boolean   mIsStereo;

    static public long getComponentType()
    {
        return NativeRenderTarget.getComponentType();
    }

    /**
     * Constructs a render target component which renders the main scene to a designated texture.
     * The objects in the scene are rendered from the viewpoint of the scene object
     * the GVRRenderTarget is attached to. Nothing is rendered if
     * the render target is not attached to a scene object.
     *
     * @param texture   GVRRenderTexture to render to.
     */
    public GVRRenderTarget(GVRRenderTexture texture, boolean isStereo)
    {
        super(texture.getGVRContext(),
              NativeRenderTarget.ctorMultiview(texture.getNative(), false, isStereo));
        mScene = texture.getGVRContext().getMainScene();
        mTexture = texture;
        mIsStereo = isStereo;
    }

    /**
     * Constructs a render target component which renders the main scene.
     * The objects in the scene are rendered from the viewpoint of the scene's
     * right and left cameras if stereo is enabled.
     * @param ctx   GVRContext to attach render target to.
     */
    public GVRRenderTarget(GVRContext ctx, boolean isStereo)
    {
        super(ctx,
              NativeRenderTarget.defaultCtr(ctx.getMainScene().getNative(), isStereo));
        mScene = ctx.getMainScene();
        mIsStereo = isStereo;
    }

    /**
     * Constructs a render target component which renders to the same scene
     * as the specified render target. The objects in the scene are rendered
     * from the viewpoint of the scene's camera.
     *
     * @param texture       GVRRenderTexture to render to.
     * @param renderTarget  GVRRenderTarget to share
     */
    public GVRRenderTarget(GVRRenderTexture texture, GVRRenderTarget renderTarget)
    {
        super(texture.getGVRContext(),
              NativeRenderTarget.ctor(texture.getNative(), renderTarget.getNative()));
        setEnable(false);
        mTexture = texture;
        mIsStereo = renderTarget.isStereo();
        setMainScene(renderTarget.mScene);
    }

    public GVRRenderTarget(GVRRenderTexture texture, GVRScene scene)
    {
        this(texture, scene, false);
    }

    public GVRRenderTarget(GVRRenderTexture texture, GVRScene scene, boolean isMultiview)
    {
        super(texture.getGVRContext(),
              NativeRenderTarget.ctorMultiview(texture.getNative(), isMultiview, true));
        setEnable(false);
        mTexture = texture;
        mScene = scene;
        mIsStereo = true;
        setMainScene(scene);
        setCamera(scene.getMainCameraRig().getCenterCamera());
    }

    public boolean isStereo() { return mIsStereo; }

    public void setStereo(boolean flag)
    {
        mIsStereo = flag;
        NativeRenderTarget.setStereo(getNative(), flag);
    }

    public GVRCamera getCamera(){
        return mCamera;
    }

    public void setCamera(GVRCamera camera)
    {
        mCamera = camera;
        NativeRenderTarget.setCamera(getNative(), camera.getNative());
    }

    public void attachRenderTarget(GVRRenderTarget renderTarget){
        NativeRenderTarget.attachRenderTarget(getNative(),renderTarget.getNative());
    }
    public void beginRendering(GVRCamera camera){
        NativeRenderTarget.beginRendering(getNative(), camera.getNative());
    }
    public void endRendering(){
        NativeRenderTarget.endRendering(getNative());
    }
    public void cullFromCamera(GVRScene scene, GVRCamera camera, GVRShaderManager shaderManager)
    {
        NativeRenderTarget.cullFromCamera(scene.getNative(), scene, getNative(), camera.getNative(), shaderManager.getNative());
    }

    public void render(GVRScene scene, GVRCamera camera, GVRShaderManager shaderManager, GVRRenderTexture posteffectRenderTextureA, GVRRenderTexture posteffectRenderTextureB) {
        NativeRenderTarget.render(getNative(), camera.getNative(), shaderManager.getNative(), posteffectRenderTextureA.getNative(), posteffectRenderTextureB.getNative(), scene.getNative(), scene);
    }

    public void setMainScene(GVRScene scene){
        mScene = scene;
        NativeRenderTarget.setMainScene(getNative(),scene.getNative());
    }
    /**
     * Internal constructor for subclasses.
     * @param ctx
     * @param nativePointer
     */
    protected GVRRenderTarget(GVRContext ctx, long nativePointer)
    {
        super(ctx, nativePointer);
    }

    /**
     * Sets the texture this render target will render to.
     * If no texture is provided, the render target will
     * not render anything.
     * @param texture GVRRenderTexture to render to.
     */
    public void setTexture(GVRRenderTexture texture)
    {
        mTexture = texture;
        NativeRenderTarget.setTexture(getNative(), texture.getNative());
    }

    /**
     * Gets the GVRRenderTexture being rendered to by this render target.
     * @return GVRRenderTexture used by the render target or null if none specified.
     * @see #setTexture(GVRRenderTexture)
     */
    public GVRRenderTexture getTexture()
    {
        return mTexture;
    }

    public void onDrawFrame(float frameTime)
    {
        getGVRContext().getActivity().getViewManager().cullAndRender(this, mScene);
    }
}

class NativeRenderTarget
{
    static native long ctor(long texture, long sourceRendertarget);
    static native long ctorMultiview(long texture, boolean isMultiview, boolean isStereo);
    static native long defaultCtr(long scene, boolean isStereo);
    static native long getComponentType();
    static native void setMainScene(long rendertarget, long scene);
    static native void beginRendering(long rendertarget, long camera);
    static native void endRendering(long rendertarget);
    static native void setCamera(long rendertarget, long camera);
    static native void setStereo(long rendertarget, boolean isStereo);
    static native void cullFromCamera(long scene, GVRScene javaSceneObject, long renderTarget, long camera, long shader_manager);
    static native void render(long renderTarget, long camera, long shader_manager, long posteffectrenderTextureA, long posteffectRenderTextureB, long scene, GVRScene javaSceneObject);
    static native void setTexture(long rendertarget, long texture);
    static native void attachRenderTarget(long renderTarget, long nextRenderTarget);
}
