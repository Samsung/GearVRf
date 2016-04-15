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
 * }
 * 
 * @see GVRPointLight
 * @see GVRLightBase
 */
public class GVRSpotLight extends GVRPointLight
{
    protected static String mSpotLightShaderSource = null;
    public GVRSpotLight(GVRContext gvrContext, GVRSceneObject owner) {
        super(gvrContext, owner);
        uniformDescriptor += " float inner_cone_angle; float outer_cone_angle; ";
        if (mSpotLightShaderSource == null) {
            mSpotLightShaderSource = TextFile.readTextFile(gvrContext.getContext(), R.raw.spotlight);
        }
        setShaderSource(mSpotLightShaderSource);
        setFloat("inner_cone_angle", 90.0f);
        setFloat("outer_cone_angle", 90.0f);
    }
    
    public GVRSpotLight(GVRContext gvrContext) {
        this(gvrContext, null);
    }

    /**
     * Get the inner angle of the spotlight cone in degrees.
     * 
     * Inside the inner cone angle the light is at full intensity.
     * {@link setInnerConeAngle setOuterConeAngle}
     */
    public float getInnerConeAngle() {
        return getFloat("inner_cone_angle");
    }

    /**
     * Set the inner angle of the spotlight cone in degrees.
     * 
     * Inside the inner cone angle the light is at full intensity.
     * The underlying uniform "inner_cone_angle" is the cosine
     * of this input angle. If the inner cone angle is larger than the outer cone angle
     * there will be unexpected results.
     * {@link getInnerConeAngle setOuterConeAngle}
     */
    public void setInnerConeAngle(float angle) {
        setFloat("inner_cone_angle", (float) Math.cos(Math.toRadians(angle)));
    }
    
    /**
     * Get the outer angle of the spotlight cone in degrees.
     * 
     * Beyond the outer cone angle there is no illumination.
     * {@link setInnerConeAngle setOuterConeAngle}
     */
    public float getOuterConeAngle() {
        return getFloat("outer_cone_angle");
    }
    
    /**
     * Set the inner angle of the spotlight cone in degrees.
     * 
     * Beyond the outer cone angle there is no illumination.
     * The underlying uniform "outer_cone_angle" is the cosine
     * of this input angle. If the inner cone angle is larger than the outer cone angle
     * there will be unexpected results.
     * {@link getInnerConeAngle setOuterConeAngle}
     */
    public void setOuterConeAngle(float angle) {
        setFloat("outer_cone_angle", (float) Math.cos(Math.toRadians(angle)));
    }
}
