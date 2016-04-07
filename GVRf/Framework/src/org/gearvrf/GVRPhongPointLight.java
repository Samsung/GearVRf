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
 * Illuminates object in the scene with a point light source.
 * 
 * The position of the light is the position of the scene object
 * the light is attached to. Light is emitted in all directions
 * from that point.
 *
 * The intensity of the light diminishes with distance from the
 * light. Three attenuation factors are provided to specify how
 * the intensity of the light falls off with distance:
 * {@code 1 / (attenuation_constant + attenuation_linear * D * attenuation_quadratic * D ** 2)
 *
 * Point light uniforms:
 * {@literal
 *   world_position        position of light in world coordinates
 *                         derived from scene object position
 *   ambient_intensity     intensity of ambient light emitted
 *   diffuse_intensity     intensity of diffuse light emitted
 *   specular_intensity    intensity of specular light emitted
 *   attenuation_constant  constant attenuation factor
 *   attenuation_linear    linear attenuation factor
 *   attenuation_quadratic quadratic attenuation factor
 * }
 * @see GVRPhongDirectLight
 * @see GVRPhongSpotLight
 * @see GVRLightTemplate
 */
public class GVRPhongPointLight extends GVRPhongLight
{
    protected static String mPointLightShaderSource = null;
    public GVRPhongPointLight(GVRContext gvrContext, GVRSceneObject owner) {
        super(gvrContext, owner);
        uniformDescriptor += " float attenuation_constant"
                + " float attenuation_linear"
                + " float attenuation_quadratic";

        if (mPointLightShaderSource == null) {
            mPointLightShaderSource = TextFile.readTextFile(gvrContext.getContext(), R.raw.pointlight);
        }
        setShaderSource(mPointLightShaderSource);
        setFloat("attenuation_constant", 1);
        setFloat("attenuation_linear", 0);
        setFloat("attenuation_quadratic", 0);
    }
    
    public GVRPhongPointLight(GVRContext gvrContext) {
        this(gvrContext, null);
    }
    
    /**
     * Get the constant attenuation factor.
     * This dims the light by a constant amount: {@code 1 / attenuation_constant}.
     * @return constant attenuation
     */
    public float getAttenuationConstant() {
       return getFloat("attenuation_constant");
    }

    /**
     * Get the constant attenuation factor.
     * This dims the light linearly based on distance from the light:
     * {@code 1 / attenuation_linear * distance}.
     * @return linear attenuation
     */
    public float getAttenuationLinear() {
        return getFloat("attenuation_linear");
    }

    /**
     * Get the quadratic attenuation factor.
     * This dims the light quadratically based on distance from the light:
     * {@code 1 / attenuation_quadratic * distance * distance}.
     * @return quadratic attenuation
     */
    public float getAttenuationQuadratic() {
        return getFloat("attenuation_quadratic");
    }
    
    /**
     * Set the constant attenuation factor.
     * This dims the light by a constant amount: {@code 1 / attenuation_constant}.
     * @param v constant attenuation value
     */
    public void setAttenuationConstant(float v) {
        setFloat("attenuation_constant", v);
    }

    /**
     * Set the linear attenuation factor.
     * This dims the light linearly based on distance from the light:
     * {@code 1 / attenuation_linear * distance}.
     * param v linear attenuation value
     */
    public void setAttenuationLinear(float v) {
        setFloat("attenuation_linear", v);
    }

    /**
     * Set the quadratic attenuation factor.
     * This dims the light quadratically based on distance from the light:
     * {@code 1 / attenuation_quadratic * distance * distance}.
     * @param v quadratic attenuation value
     */
    public void setAttenuationQuadratic(float v) {
        setFloat("attenuation_quadratic", v);
    }
    
    /**
     * Set the three attenuation constants to control how
     * light falls off based on distance from the light source.
     * {@code 1 / (attenuation_constant + attenuation_linear * D * attenuation_quadratic * D ** 2)
     * @param constant  constant attenuation factor
     * @param linear    linear attenuation factor
     * @param quadratic quadratic attenuation factor
     */
    public void setAttenuation(float constant, float linear, float quadratic) {
        setFloat("attenuation_constant", constant);
        setFloat("attenuation_linear", constant);
        setFloat("attenuation_quadratic", quadratic);
    }
}
