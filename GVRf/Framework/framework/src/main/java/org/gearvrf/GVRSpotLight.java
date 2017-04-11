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
 *
 * Spot light uniforms:
 * {@literal
 *   enabled               1 = light is enabled, 0 = light is disabled
 *   world_position        position of spot light in world coordinates
 *                         derived from scene object position
 *   world_direction       direction of spot light in world coordinates
 *                         derived from scene object orientation
 *   ambient_intensity     intensity of ambient light emitted
 *   diffuse_intensity     intensity of diffuse light emitted
 *   specular_intensity    intensity of specular light emitted
 *   attenuation_constant  constant attenuation factor
 *   attenuation_linear    linear attenuation factor
 *   attenuation_quadratic quadratic attenuation factor
 *   inner_cone_angle      cosine of inner cone angle
 *   outer_cone_angle      cosine of outer cone angle
 *   sm0                   shadow matrix column 1
 *   sm1                   shadow matrix column 2
 *   sm2                   shadow matrix column 3
 *   sm3                   shadow matrix column 4
 * }
 * 
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
    private boolean useShadowShader = true;
    private boolean mChanged = true;

    public GVRSpotLight(GVRContext gvrContext, GVRSceneObject owner) {
        super(gvrContext, owner);
        
        mUniformDescriptor += " float inner_cone_angle; float outer_cone_angle; ";
        if (useShadowShader)
        {
            mUniformDescriptor += " float shadow_map_index; vec4 sm0; vec4 sm1; vec4 sm2; vec4 sm3";
            mVertexDescriptor = "vec4 shadow_position";
            if (fragmentShader == null)
                fragmentShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.spotshadowlight);
            if (vertexShader == null)
                vertexShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.vertex_shadow);
            mVertexShaderSource = vertexShader;
            setFloat("shadow_map_index", -1.0f);
        }
        else if (fragmentShader == null)
            fragmentShader = TextFile.readTextFile(gvrContext.getContext(), R.raw.spotlight);
        mFragmentShaderSource = fragmentShader;
        setInnerConeAngle(90.0f);
        setOuterConeAngle(90.0f);
    }
    
    public GVRSpotLight(GVRContext gvrContext) {
        this(gvrContext, null);
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
        mChanged = true;
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
        super.setCastShadow(enableFlag);
        if (enableFlag && (getOwnerObject() != null))
        {
            GVRShadowMap shadowMap = (GVRShadowMap) getComponent(GVRRenderTarget.getComponentType());
            if ((shadowMap != null) && (shadowMap.getCamera() == null))
            {
                shadowMap.addPerspShadowCamera(getGVRContext().getMainScene().getMainCameraRig().getCenterCamera(),
                                               (float) Math.acos(getFloat("outer_cone_angle")) * 2.0f);
            }
        }
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
        float[] opos = getVec3("world_position");
        Matrix4f worldmtx = owner.getTransform().getModelMatrix4f();
        boolean changed = mChanged;

        mChanged = false;
        mOldDir.x = odir[0];
        mOldDir.y = odir[1];
        mOldDir.z = odir[2];
        mOldPos.x = opos[0];
        mOldPos.y = opos[1];
        mOldPos.z = opos[2];
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
            setVec3("world_direction", mNewDir.x, mNewDir.y, mNewDir.z);
        }
        if ((mOldPos.x != mNewPos.x) || (mOldPos.y != mNewPos.y) || (mOldPos.z != mNewPos.z))
        {
            changed = true;
            setVec3("world_position", mNewPos.x, mNewPos.y, mNewPos.z);
        }
        GVRShadowMap shadowMap = (GVRShadowMap) getComponent(GVRShadowMap.getComponentType());
        if ((shadowMap != null) && changed && shadowMap.isEnabled())
        {
            shadowMap.setPerspShadowMatrix(worldmtx, this);
        }
    }
}
