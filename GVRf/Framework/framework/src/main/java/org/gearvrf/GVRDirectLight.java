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

import org.gearvrf.shaders.GVRPhongShader;
import org.gearvrf.utility.TextFile;
import org.joml.Matrix4f;

/**
 * Illuminates object in the scene with a directional light source.
 * 
 * The direction of the light is the forward orientation of the scene object
 * the light is attached to. Light is emitted in that direction
 * from infinitely far away.
 *
 * The intensity of the light remains constant and does not fall
 * off with distance from the light.
 * <p>
 * <b>Dlrect light uniforms:</b>
 * <table>
 * <tr><td>enabled</td><td>1 = light is enabled, 0 = light is disabled/td></tr>
 * <tr><td>world_direction</td><td>direction of spot light in world coordinates</td></tr>
 *  derived from scene object orientation<td></tr>
 * <tr><td>ambient_intensity</td><td>intensity of ambient light emitted</td></tr>
 * <tr><td>diffuse_intensity</td><td>intensity of diffuse light emitted</td></tr>
 * <tr><td>specular_intensity</td><td>intensity of specular light emitted</td></tr>
 * <tr><td>sm0</td><td>shadow matrix column 1</td></tr>
 * <tr><td>sm1</td><td>shadow matrix column 2</td></tr>
 * <tr><td>sm2</td><td>shadow matrix column 3</td></tr>
 * <tr><td>sm3</td><td>shadow matrix column 4</td></tr>
 * </table>
 * 
 * Note: some mobile GPU drivers do not correctly pass a mat4 thru so we currently
 * use 4 vec4's instead.
 * 
 * @see GVRPointLight
 * @see GVRSpotLight
 * @see GVRLight
 */
public class GVRDirectLight extends GVRLight
{
    private static String fragmentShader = null;
    private static String vertexShader = null;
    private boolean useShadowShader = true;
    protected final static String DIRECT_UNIFORM_DESC = UNIFORM_DESC +
        " float4 diffuse_intensity"
        + " float4 ambient_intensity"
        + " float4 specular_intensity"
        + " float4 sm0 float4 sm1 float4 sm2 float4 sm3";

    public GVRDirectLight(GVRContext gvrContext)
    {
        this(gvrContext, DIRECT_UNIFORM_DESC,  "float4 shadow_position");
        if (useShadowShader)
        {
            if (fragmentShader == null)
                fragmentShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.directshadowlight);
            if (vertexShader == null)
                vertexShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.vertex_shadow);
            mVertexShaderSource = vertexShader;
        }
        else if (fragmentShader == null)
        {
            fragmentShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.directlight);
        }
        mFragmentShaderSource = fragmentShader;
    }

    public GVRDirectLight(GVRContext ctx, String uniformDesc, String vertexDesc)
    {
        super(ctx, uniformDesc, vertexDesc);
        setLightClass(getClass().getSimpleName());
        setFloat("shadow_map_index", -1.0f);
        setAmbientIntensity(0.0f, 0.0f, 0.0f, 1.0f);
        setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setSpecularIntensity(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Get the ambient light intensity.
     * 
     * This designates the color of the ambient reflection.
     * It is multiplied by the material ambient color to derive
     * the hue of the ambient reflection for that material.
     * The built-in phong shader {@link GVRPhongShader} uses a {@code vec4} uniform named
     * {@code ambient_intensity} to control the intensity of ambient light reflected.
     * 
     * @return The current {@code vec4 ambient_intensity} as a four-element array
     */
    public float[] getAmbientIntensity() {
        return getVec4("ambient_intensity");
    }

    /**
     * Set the ambient light intensity.
     * 
     * This designates the color of the ambient reflection.
     * It is multiplied by the material ambient color to derive
     * the hue of the ambient reflection for that material.
     * The built-in phong shader {@link GVRPhongShader} uses a {@code vec4} uniform named
     * {@code ambient_intensity} to control the intensity of ambient light reflected.
     * 
     * @param r red component (0 to 1)
     * @param g green component (0 to 1)
     * @param b blue component (0 to 1)
     * @param a alpha component (0 to 1)
     */
    public void setAmbientIntensity(float r, float g, float b, float a) {
        setVec4("ambient_intensity", r, g, b, a);
    }

    /**
     * Get the diffuse light intensity.
     * 
     * This designates the color of the diffuse reflection.
     * It is multiplied by the material diffuse color to derive
     * the hue of the diffuse reflection for that material.
     * The built-in phong shader {@link GVRPhongShader} uses a {@code vec4} uniform named
     * {@code diffuse_intensity} to control the intensity of diffuse light reflected.
     * 
     * @return The current {@code vec4 diffuse_intensity} as a four-element
     *         array
     */
    public float[] getDiffuseIntensity() {
        return getVec4("diffuse_intensity");
    }

    /**
     * Set the diffuse light intensity.
     * 
     * This designates the color of the diffuse reflection.
     * It is multiplied by the material diffuse color to derive
     * the hue of the diffuse reflection for that material.
     * The built-in phong shader {@link GVRPhongShader} uses a {@code vec4} uniform named
     * {@code diffuse_intensity} to control the intensity of diffuse light reflected.
     * 
     * @param r red component (0 to 1)
     * @param g green component (0 to 1)
     * @param b blue component (0 to 1)
     * @param a alpha component (0 to 1)
     */
    public void setDiffuseIntensity(float r, float g, float b, float a)
    {
        setVec4("diffuse_intensity", r, g, b, a);
    }

    /**
     * Get the specular intensity of the light.
     *
     * This designates the color of the specular reflection.
     * It is multiplied by the material specular color to derive
     * the hue of the specular reflection for that material.
     * The built-in phong shader {@link GVRPhongShader} uses a {@code vec4} uniform named
     * {@code specular_intensity} to control the specular intensity.
     *
     * @return The current {@code vec4 specular_intensity} as a four-element array
     */
    public float[] getSpecularIntensity() {
        return getVec4("specular_intensity");
    }

    /**
     * Set the specular intensity of the light.
     * 
     * This designates the color of the specular reflection.
     * It is multiplied by the material specular color to derive
     * the hue of the specular reflection for that material.
     * The built-in phong shader {@link GVRPhongShader} uses a {@code vec4} uniform named
     * {@code specular_intensity} to control the specular intensity.
     * 
     * @param r red component (0 to 1)
     * @param g green component (0 to 1)
     * @param b blue component (0 to 1)
     * @param a alpha component (0 to 1)
     */
    public void setSpecularIntensity(float r, float g, float b, float a)
    {
        setVec4("specular_intensity", r, g, b, a);
    }

    /**
     * Enables or disabled shadow casting for a direct light.
     * Enabling shadows attaches a GVRShadowMap component to the
     * GVRSceneObject which owns the light and provides the
     * component with an orthographic camera for shadow casting.
     * @param enableFlag true to enable shadow casting, false to disable
     */
    public void setCastShadow(boolean enableFlag)
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner != null)
        {
            GVRShadowMap shadowMap = (GVRShadowMap) getComponent(GVRRenderTarget.getComponentType());
            if (enableFlag)
            {
                if (shadowMap != null)
                {
                    shadowMap.setEnable(true);
                }
                else
                {
                    GVRCamera shadowCam = GVRShadowMap.makeOrthoShadowCamera(
                            getGVRContext().getMainScene().getMainCameraRig().getCenterCamera());
                    shadowMap = new GVRShadowMap(getGVRContext(), shadowCam);
                    owner.attachComponent(shadowMap);
                }
            }
            else if (shadowMap != null)
            {
                shadowMap.setEnable(false);
            }
        }
        mCastShadow = enableFlag;
    }

    /**
     * Sets the near and far range of the shadow map camera.
     * <p>
     * This function enables shadow mapping and sets the shadow map
     * camera near and far range, controlling which objects affect
     * the shadow map. To modify other properties of the shadow map
     * camera's projection, you can call {@link GVRShadowMap#getCamera}
     * and update them.
     * @param near near shadow camera clipping plane
     * @param far far shadow camera clipping plane
     * @see GVRShadowMap#getCamera()
     */
    public void setShadowRange(float near, float far)
    {
        GVRSceneObject owner = getOwnerObject();
        GVROrthogonalCamera shadowCam = null;

        if (owner == null)
        {
            throw new UnsupportedOperationException("Light must have an owner to set the shadow range");
        }
        GVRShadowMap shadowMap = (GVRShadowMap) getComponent(GVRRenderTarget.getComponentType());
        if (shadowMap != null)
        {
            shadowCam = (GVROrthogonalCamera) shadowMap.getCamera();
            shadowCam.setNearClippingDistance(near);
            shadowCam.setFarClippingDistance(far);
            shadowMap.setEnable(true);
        }
        else
        {
           shadowCam = GVRShadowMap.makeOrthoShadowCamera(
                    getGVRContext().getMainScene().getMainCameraRig().getCenterCamera());
            shadowCam.setNearClippingDistance(near);
            shadowCam.setFarClippingDistance(far);
            shadowMap = new GVRShadowMap(getGVRContext(), shadowCam);
            owner.attachComponent(shadowMap);
        }
        mCastShadow = true;
    }

    /**
     * Updates the position, direction and shadow matrix
     * of this light from the transform of scene object that owns it.
     * The shadow matrix is the model/view/projection matrix
     * from the point of view of the light.
     */
    public void onDrawFrame(float frameTime)
    {
        if (!isEnabled() || (getFloat("enabled") <= 0.0f) || (owner == null)) { return; }
        float[] odir = getVec3("world_direction");
        boolean changed = false;
        Matrix4f worldmtx = owner.getTransform().getModelMatrix4f();

        mOldDir.x = odir[0];
        mOldDir.y = odir[1];
        mOldDir.z = odir[2];
        mNewDir.x = 0.0f;
        mNewDir.y = 0.0f;
        mNewDir.z = -1.0f;
        worldmtx.mul(mLightRot);
        worldmtx.transformDirection(mNewDir);
        mNewDir.normalize();
        if ((mOldDir.x != mNewDir.x) || (mOldDir.y != mNewDir.y) || (mOldDir.z != mNewDir.z))
        {
            changed = true;
            setVec4("world_direction", mNewDir.x, mNewDir.y, mNewDir.z, 0);
        }
        GVRShadowMap shadowMap = (GVRShadowMap) getComponent(GVRShadowMap.getComponentType());
        if ((shadowMap != null) && changed && shadowMap.isEnabled())
        {
            computePosition();
            worldmtx.setTranslation(mNewPos);
            shadowMap.setOrthoShadowMatrix(worldmtx, this);
        }
    }

    private void computePosition()
    {
        GVRScene scene = getGVRContext().getMainScene();
        GVRSceneObject.BoundingVolume bv = scene.getRoot().getBoundingVolume();
        float far = scene.getMainCameraRig().getFarClippingDistance();

        mNewPos.x = bv.center.x - far * mNewDir.x;
        mNewPos.y = bv.center.y - far * mNewDir.y;
        mNewPos.z = bv.center.z - far * mNewDir.z;
        setPosition(mNewPos.x, mNewPos.y, mNewPos.z);
    }
}
