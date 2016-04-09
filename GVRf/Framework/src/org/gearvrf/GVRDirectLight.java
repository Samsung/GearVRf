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

import static org.gearvrf.utility.Assert.*;

import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.utility.TextFile;

/**
 * Illuminates object in the scene with a directional light source.
 * 
 * The direction of the light is the forward orientation of the scene object
 * the light is attached to. Light is emitted in that direction
 * from infinitely far away.
 *
 * The intensity of the light remains constant and does not fall
 * off with distance from the light.
 *
 * Point light uniforms:
 * {@literal
 *   world_direction       direction of light in world coordinates
 *                         derived from scene object orientation
 *   ambient_intensity     intensity of ambient light emitted
 *   diffuse_intensity     intensity of diffuse light emitted
 *   specular_intensity    intensity of specular light emitted
 * }
 * @see GVRPointLight
 * @see GVRSpotLight
 * @see GVRLightBase
 */
public class GVRDirectLight extends GVRLightBase {
    protected static String mPhongLightShaderSource = null;
    
    public GVRDirectLight(GVRContext gvrContext) {
        this(gvrContext, null);
     }

    public GVRDirectLight(GVRContext gvrContext, GVRSceneObject parent) {
        super(gvrContext, parent);
        uniformDescriptor += " float4 diffuse_intensity"
                + " float4 ambient_intensity"
                + " float4 specular_intensity";
         if (mPhongLightShaderSource == null) {
            mPhongLightShaderSource = TextFile.readTextFile(gvrContext.getContext(), R.raw.directlight);
        }
        setAmbientIntensity(0.0f, 0.0f, 0.0f, 1.0f);
        setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setSpecularIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setShaderSource(mPhongLightShaderSource);
    }
    
    /**
     * Get the light position uniform.
     * 
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec3} uniform named
     * {@code world_position} to control the position of the light in world space.
     * It is computed from the scene object the light is attached to.
     * 
     * @return the world position of the light as a 3 element array
     */
    public float[] getPosition() {
        return getVec3("world_position");
    }

    /**
     * Set the world position of the light.
     * 
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec3} uniform named
     * {@code world_position} to control the position of the light in world space.
     * It is computed from the scene object the light is attached to.
     * 
     * @param x
     *            x-coordinate in world coordinate system
     * @param y
     *            y-coordinate in world coordinate system
     * @param z
     *            z-coordinate in world coordinate system
     */
    public void setPosition(float x, float y, float z) {
        setVec3("world_position", x, y, z);
    }

    /**
     * Get the ambient light intensity.
     * 
     * This designates the color of the ambient reflection.
     * It is multiplied by the material ambient color to derive
     * the hue of the ambient reflection for that material.
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
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
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
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
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
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
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
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
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
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
     * The built-in phong shader {@link GVRPhongSurface} uses a {@code vec4} uniform named
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
}
