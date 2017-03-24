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
    protected GVRCamera mCamera;
    protected GVRMaterial mMaterial;
    protected GVRRenderTexture mTexture;
    protected GVRScene  mScene;

    static public long getComponentType()
    {
        return NativeRenderTarget.getComponentType();
    }

    /**
     * Constructs a render target component which renders the given scene to a designated texture.
     * The objects in the scene are rendered from the viewpoint of the scene object
     * the GVRRenderTarget is attached to. Nothing is rendered if
     * the render target is not attached to a scene object.
     * You must call @{link #setEnable(true)} to initiate rendering.
     *
     * @param texture   GVRRenderTexture to render to.
     * @param scene     GVRScene to render.
     * @see #setEnable(boolean)
     */
    public GVRRenderTarget(GVRRenderTexture texture, GVRScene scene)
    {
        super(texture.getGVRContext(), NativeRenderTarget.ctor(texture.getNative()));
        setEnable(false);
        mTexture = texture;
        mScene = scene;
        setCamera(scene.getMainCameraRig().getCenterCamera());
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
     * Sets the camera to use for this render target.
     * The position and orientation of the render camera is taken
     * from the transform of the scene object this component is
     * attached to. The projection matrix which defines the
     * camera view volume is taken from this camera.
     * If no camera is provided, the projection matrix from
     * the scene's main camera is used.
     * @param camera GVRCamera to use for rendering this render target.
     * @see #getCamera()
     */
    public void setCamera(GVRCamera camera)
    {
        mCamera = camera;
        NativeRenderTarget.setCamera(getNative(), camera.getNative());
    }

    /**
     * Gets the camera used to render to this render target.
     * @return GVRCamera used for rendering
     * @see #setCamera(GVRCamera)
     */
    public GVRCamera getCamera()
    {
        return mCamera;
    }

    /**
     * Sets the texture this render target will render to.
     * If no texture is provided, the render target will
     * not render anything.
     * @param texture GVRRenderTexture to render to.
     */
    public void setTexture(GVRRenderTexture texture)
    {
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
    static native long getComponentType();

    static native long ctor(long texture);

    static native void setCamera(long rendertarget, long camera);

    static native void setTexture(long rendertarget, long texture);
}
