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

import org.gearvrf.utility.TextFile;
import org.joml.Matrix4f;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Illuminates object in the scene with a cone shaped beam.
 * 
 * The apex of the cone is at the position of the scene object
 * the light is attached to. The direction of the cone is the
 * forward direction of that scene object.
 * 
 * There are two angles for the cone. Beyond the "outer angle"
 * no light is emitted. Inside the "inner angle" the light is
 * at full intensity. Between the two angles the light linearly
 * decreases allowing for soft edges.
 * 
 * The intensity of the light diminishes with distance from the
 * cone apex. Three attenuation factors are provided to specify how
 * the intensity of the light falls off with distance:
 * {@code I = attenuation_constant + attenuation_linear * D * attenuation_quadratic * D ** 2}
 * <p>
 * <b>Spot light uniforms:</b>
 * <table>
 * <tr><td>enabled</td><td>1 = light is enabled, 0 = light is disabled</td></tr>
 * <tr><td>world_position</td><td>position of spot light in world coordinates</td></tr>
 *  derived from scene object position</td></tr>
 * <tr><td>world_direction</td><td>direction of spot light in world coordinates</td></tr>
 *  derived from scene object orientation</td></tr>
 * <tr><td>ambient_intensity</td><td>intensity of ambient light emitted</td></tr>
 * <tr><td>diffuse_intensity</td><td>intensity of diffuse light emitted</td></tr>
 * <tr><td>specular_intensity</td><td>intensity of specular light emitted</td></tr>
 * <tr><td>attenuation_constant</td><td>constant attenuation factor</td></tr>
 * <tr><td>attenuation_linear</td><td>linear attenuation factor</td></tr>
 * <tr><td>attenuation_quadratic</td><td>quadratic attenuation factor</td></tr>
 * <tr><td>inner_cone_angle</td><td>cosine of inner cone angle</td></tr>
 * <tr><td>outer_cone_angle</td><td>cosine of outer cone angle</td></tr>
 * <tr><td>sm0</td><td>shadow matrix column 1</td></tr>
 * <tr><td>sm1</td><td>shadow matrix column 2</td></tr>
 * <tr><td>sm2</td><td>shadow matrix column 3</td></tr>
 * <tr><td>sm3</td><td>shadow matrix column 4</td></tr>
 * </table>
 * Note: some mobile GPU drivers do not correctly pass a mat4 thru so we currently
 * use 4 vec4's instead.
 *  
 * @see GVRPointLight
 * @see GVRLightBase
 */
public class GVRSpotLight extends GVRPointLight
{
    private static String fragmentShader = null;
    private static String vertexShader = null;
    private AtomicBoolean mChanged = new AtomicBoolean();
    protected final static String SPOT_UNIFORM_DESC = POINT_UNIFORM_DESC
            + " float inner_cone_angle; float outer_cone_angle; float spad1; float spad2;"
            + " float4 sm0; float4 sm1; float4 sm2; float4 sm3";

    public GVRSpotLight(GVRContext gvrContext)
    {
        this(gvrContext, SPOT_UNIFORM_DESC, "vec4 shadow_position");
        if (fragmentShader == null)
            fragmentShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.spotshadowlight);
        if (vertexShader == null)
            vertexShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.vertex_shadow);
        mVertexShaderSource = vertexShader;
        mFragmentShaderSource = fragmentShader;
    }
    
    public GVRSpotLight(GVRContext gvrContext, String uniformDesc, String vertexDesc)
    {
        super(gvrContext, uniformDesc, vertexDesc);
        setLightClass(getClass().getSimpleName());
        mChanged.set(true);
        setFloat("shadow_map_index", -1.0f);
        setInnerConeAngle(90.0f);
        setOuterConeAngle(90.0f);
   }

    /**
     * Get the inner angle of the spotlight cone in degrees.
     * 
     * Inside the inner cone angle the light is at full intensity.
     * @see #setInnerConeAngle(float)
     * @see #setOuterConeAngle(float)
     */
    public float getInnerConeAngle()
    {
        return (float) Math.toDegrees(Math.acos(getFloat("inner_cone_angle")));
    }

    /**
     * Set the inner angle of the spotlight cone in degrees.
     * 
     * Inside the inner cone angle the light is at full intensity.
     * The underlying uniform "inner_cone_angle" is the cosine
     * of this input angle. If the inner cone angle is larger than the outer cone angle
     * there will be unexpected results.
     * @see #getInnerConeAngle()
     * @see #getOuterConeAngle()
     */
    public void setInnerConeAngle(float angle)
    {
        setFloat("inner_cone_angle", (float) Math.cos(Math.toRadians(angle)));
    }
    
    /**
     * Get the outer angle of the spotlight cone in degrees.
     * 
     * Beyond the outer cone angle there is no illumination.
     * @see #setInnerConeAngle(float)
     * @see #setOuterConeAngle(float)
     */
    public float getOuterConeAngle()
    {
        return (float) Math.toDegrees(Math.acos(getFloat("outer_cone_angle")));
    }
    
    /**
     * Set the inner angle of the spotlight cone in degrees.
     * 
     * Beyond the outer cone angle there is no illumination.
     * The underlying uniform "outer_cone_angle" is the cosine
     * of this input angle. If the inner cone angle is larger than the outer cone angle
     * there will be unexpected results.
     * @see #setInnerConeAngle(float)
     * @see #getOuterConeAngle()
     */
    public void setOuterConeAngle(float angle)
    {
        setFloat("outer_cone_angle", (float) Math.cos(Math.toRadians(angle)));
        mChanged.set(true);
    }

    /**
     * Enables or disabled shadow casting for a spot light.
     * Enabling shadows attaches a GVRShadowMap component to the
     * GVRSceneObject which owns the light and provides the
     * component with an perspective camera for shadow casting.
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
                    float angle = (float) Math.acos(getFloat("outer_cone_angle")) * 2.0f;
                    GVRCamera shadowCam = GVRShadowMap.makePerspShadowCamera(getGVRContext().getMainScene().getMainCameraRig().getCenterCamera(), angle);
                    shadowMap = new GVRShadowMap(getGVRContext(), shadowCam);
                    owner.attachComponent(shadowMap);
                }
                mChanged.set(true);
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
        GVRPerspectiveCamera shadowCam = null;

        if (owner == null)
        {
            throw new UnsupportedOperationException("Light must have an owner to set the shadow range");
        }
        GVRShadowMap shadowMap = (GVRShadowMap) getComponent(GVRRenderTarget.getComponentType());
        if (shadowMap != null)
        {
            shadowCam = (GVRPerspectiveCamera) shadowMap.getCamera();
            shadowCam.setNearClippingDistance(near);
            shadowCam.setFarClippingDistance(far);
            shadowMap.setEnable(true);
        }
        else
        {
            shadowCam = GVRShadowMap.makePerspShadowCamera(
                    getGVRContext().getMainScene().getMainCameraRig().getCenterCamera(), getOuterConeAngle());
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
        Matrix4f worldmtx = owner.getTransform().getModelMatrix4f();
        boolean changed = mChanged.getAndSet(false);

        mNewDir.x = 0.0f;
        mNewDir.y = 0.0f;
        mNewDir.z = -1.0f;
        worldmtx.getTranslation(mNewPos);
        worldmtx.mul(mLightRot);
        worldmtx.transformDirection(mNewDir);
        mNewDir.normalize();
        if ((mOldDir.x != mNewDir.x) || (mOldDir.y != mNewDir.y) || (mOldDir.z != mNewDir.z))
        {
            changed = true;
            setVec4("world_direction", mNewDir.x, mNewDir.y, mNewDir.z, 0);
            mOldDir.set(mNewDir);
            mChanged.set(false);
        }
        if ((mOldPos.x != mNewPos.x) || (mOldPos.y != mNewPos.y) || (mOldPos.z != mNewPos.z))
        {
            changed = true;
            setPosition(mNewPos.x, mNewPos.y, mNewPos.z);
            mOldPos.set(mNewPos);
            mChanged.set(false);
        }
        GVRShadowMap shadowMap = (GVRShadowMap) getComponent(GVRShadowMap.getComponentType());
        if ((shadowMap != null) && changed && shadowMap.isEnabled())
        {
            shadowMap.setPerspShadowMatrix(worldmtx, this);
        }
    }
}
