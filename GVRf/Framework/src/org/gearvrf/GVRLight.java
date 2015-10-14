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

public class GVRLight extends GVRHybridObject {

    public GVRLight(GVRContext gvrContext) {
        super(gvrContext, NativeLight.ctor());
        // set light parameters to OpenGL default values
        setPosition(0.0f, 0.0f, 1.0f);
        setAmbientIntensity(0.0f, 0.0f, 0.0f, 1.0f);
        setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        setSpecularIntensity(1.0f, 1.0f, 1.0f, 1.0f);
        isEnabled = true;
    }

    /**
     * Get the {@code light_pos} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec3} uniform named
     * {@code light_pos}. With the {@linkplain GVRShaderType.Lit 'lit' shader,}
     * this allows you to add an overlay color on top of the texture.
     * 
     * @return The current {@code vec4 light_pos} as a three-element array
     */
    public float[] getPosition() {
        return getVec3("position");
    }

    /**
     * Set the {@code light_pos} uniform for light.
     * 
     * By convention, GVRF shaders can use a {@code vec3} uniform named
     * {@code light_pos}. With the {@linkplain GVRShaderType.Lit 'lit' shader,}
     * this allows you to add an overlay lighting color on top of the texture.
     * The position is in the camera coordinate system. The user is responsible
     * to use the correct coordinate system.
     * 
     * @param x
     *            x-coordinate in camera coordinate system
     * @param y
     *            y-coordinate in camera coordinate system
     * @param z
     *            z-coordinate in camera coordinate system
     */
    public void setPosition(float x, float y, float z) {
        setVec3("position", x, y, z);
    }

    /**
     * Get the {@code lightAmbientIntensity} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code lightAmbientIntensity}. With the {@linkplain GVRShaderType.Lit 
     * 'lit' shader,} this allows you to add an overlay color on top of the
     * texture.
     * 
     * @return The current {@code vec4 lightAmbientIntensity} as a four-element
     *         array
     */
    public float[] getAmbientIntensity() {
        return getVec4("ambient_intensity");
    }

    /**
     * Set the {@code lightAmbientIntensity} uniform for lighting.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code lightAmbientIntensity}. With the {@linkplain GVRShaderType.Lit 
     * 'lit' shader,} this allows you to add an overlay ambient light intensity
     * on top of the texture. Values are between {@code 0.0f} and {@code 1.0f},
     * inclusive.
     * 
     * @param r
     *            Red
     * @param g
     *            Green
     * @param b
     *            Blue
     * @param a
     *            Alpha
     */
    public void setAmbientIntensity(float r, float g, float b, float a) {
        setVec4("ambient_intensity", r, g, b, a);
    }

    /**
     * Get the {@code lightDiffuseIntensity} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code lightDiffuseIntensity}. With the {@linkplain GVRShaderType.Lit 
     * 'lit' shader,} this allows you to add an overlay color on top of the
     * texture.
     * 
     * @return The current {@code vec4 lightDiffuseIntensity} as a four-element
     *         array
     */
    public float[] getDiffuseIntensity() {
        return getVec4("diffuse_intensity");
    }

    /**
     * Set the {@code lightDiffuseIntensity} uniform for lighting.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code lightDiffuseIntensity}. With the {@linkplain GVRShaderType.Lit 
     * 'lit' shader,} this allows you to add an overlay diffuse light intensity
     * on top of the texture. Values are between {@code 0.0f} and {@code 1.0f},
     * inclusive.
     * 
     * @param r
     *            Red
     * @param g
     *            Green
     * @param b
     *            Blue
     * @param a
     *            Alpha
     */
    public void setDiffuseIntensity(float r, float g, float b, float a) {
        setVec4("diffuse_intensity", r, g, b, a);
    }

    /**
     * Get the {@code lightSpecularIntensity} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code lightSpecularIntensity}. With the {@linkplain GVRShaderType.Lit 
     * 'lit' shader,} this allows you to add an overlay color on top of the
     * texture.
     * 
     * @return The current {@code vec4 lightSpecularIntensity} as a four-element
     *         array
     */
    public float[] getSpecularIntensity() {
        return getVec4("specular_intensity");
    }

    /**
     * Set the {@code lightSpecularIntensity} uniform for lighting.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code lightSpecularIntensity}. With the {@linkplain GVRShaderType.Lit 
     * 'lit' shader,} this allows you to add an overlay specular light intensity
     * on top of the texture. Values are between {@code 0.0f} and {@code 1.0f},
     * inclusive.
     * 
     * @param r
     *            Red
     * @param g
     *            Green
     * @param b
     *            Blue
     * @param a
     *            Alpha
     */
    public void setSpecularIntensity(float r, float g, float b, float a) {
        setVec4("specular_intensity", r, g, b, a);
    }

    /**
     * Enable the light.
     */
    public void enable() {
        NativeLight.enable(getNative());
        isEnabled = true;
    }

    /**
     * Disable the light.
     */
    public void disable() {
        NativeLight.disable(getNative());
        isEnabled = false;
    }

    /**
     * Get the enable/disable status for the light.
     * 
     * @return true if light is enabled, false if light is disabled.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    // for future use
    @SuppressWarnings("unused")
    private float getFloat(String key) {
        return NativeLight.getFloat(getNative(), key);
    }

    // for future use
    @SuppressWarnings("unused")
    private void setFloat(String key, float value) {
        checkStringNotNullOrEmpty("key", key);
        checkFloatNotNaNOrInfinity("value", value);
        NativeLight.setFloat(getNative(), key, value);
    }

    private float[] getVec3(String key) {
        return NativeLight.getVec3(getNative(), key);
    }

    private void setVec3(String key, float x, float y, float z) {
        checkStringNotNullOrEmpty("key", key);
        NativeLight.setVec3(getNative(), key, x, y, z);
    }

    private float[] getVec4(String key) {
        return NativeLight.getVec4(getNative(), key);
    }

    private void setVec4(String key, float x, float y, float z, float w) {
        checkStringNotNullOrEmpty("key", key);
        NativeLight.setVec4(getNative(), key, x, y, z, w);
    }

    private boolean isEnabled;
}

class NativeLight {
    static native long ctor();

    static native void enable(long light);

    static native void disable(long light);

    static native float getFloat(long light, String key);

    static native void setFloat(long light, String key, float value);

    static native float[] getVec3(long light, String key);

    static native void setVec3(long light, String key, float x, float y, float z);

    static native float[] getVec4(long light, String key);

    static native void setVec4(long light, String key, float x, float y,
            float z, float w);
}
