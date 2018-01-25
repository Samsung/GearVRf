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
 * {@code 1 / (attenuation_constant + attenuation_linear * D * attenuation_quadratic * D ** 2)}
 * <p>
 * <b>Point light uniforms:</b>
 * <table>
 * <tr><td>enabled</td><td>1 = light is enabled, 0 = light is disabled</td></tr>
 * <tr><td>world_position</td><td>position of spot light in world coordinates/td></tr>
 *  derived from scene object position</td></tr>
 * <tr><td>ambient_intensity</td><td>intensity of ambient light emitted/td></tr>
 * <tr><td>diffuse_intensity</td><td>intensity of diffuse light emitted/td></tr>
 * <tr><td>specular_intensity</td><td>intensity of specular light emitted</td></tr>
 * <tr><td>attenuation_constant</td><td>constant attenuation factor</td></tr>
 * <tr><td>attenuation_linear</td><td>linear attenuation factor</td></tr>
 * <tr><td>attenuation_quadratic</td><td>quadratic attenuation factor</td></tr>
 * </table>
 * 
 * Point lights currently cannot cast shadows. Enabling shadows for
 * this light type will waste resources.
 * 
 * @see GVRDirectLight
 * @see GVRSpotLight
 * @see GVRLightBase
 */
public class GVRPointLight extends GVRLightBase
{
    protected final static String POINT_UNIFORM_DESC = UNIFORM_DESC
            + " float4 diffuse_intensity"
            + " float4 ambient_intensity"
            + " float4 specular_intensity"
            + " float attenuation_constant"
            + " float attenuation_linear"
            + " float attenuation_quadratic"
            + " float ppad1";
    private static String shaderSource = null;

    public GVRPointLight(GVRContext ctx)
    {
        this(ctx, POINT_UNIFORM_DESC, null);
        setLightClass(getClass().getSimpleName());
        if (shaderSource == null)
        {
            shaderSource = TextFile.readTextFile(ctx.getContext(), R.raw.pointlight);
        }
        mFragmentShaderSource = shaderSource;
    }

    protected GVRPointLight(GVRContext ctx, String uniformDesc, String vertexDesc)
    {
        super(ctx, uniformDesc, vertexDesc);
        setAmbientIntensity(0.0f, 0.0f, 0.0f, 1.0f);
        setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setSpecularIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setFloat("attenuation_constant", 1);
        setFloat("attenuation_linear", 0);
        setFloat("attenuation_quadratic", 0);
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
   public void setDiffuseIntensity(float r, float g, float b, float a) {
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
   public void setSpecularIntensity(float r, float g, float b, float a) {
       setVec4("specular_intensity", r, g, b, a);
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
     * {@code 1 / (attenuation_constant + attenuation_linear * D * attenuation_quadratic * D ** 2)}
     * @param constant  constant attenuation factor
     * @param linear    linear attenuation factor
     * @param quadratic quadratic attenuation factor
     */
    public void setAttenuation(float constant, float linear, float quadratic) {
        setFloat("attenuation_constant", constant);
        setFloat("attenuation_linear", linear);
        setFloat("attenuation_quadratic", quadratic);
    }

    @Override
    public void setCastShadow(boolean flag)
    {
        if ((getClass() == GVRPointLight.class) && flag) {
            throw new UnsupportedOperationException("GVRPointLight cannot cast shadows");
        }
        super.setCastShadow(flag);
    }
}
