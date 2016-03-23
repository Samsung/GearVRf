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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRSceneObject;

public class GVRLight extends GVRHybridObject {

    public GVRLight(GVRContext gvrContext, GVRSceneObject parent) {
        super(gvrContext, NativeLight.ctor());
        isEnabled = true;
        if (parent != null) {
            NativeLight.setParent(getNative(), parent.getNative());
        }
        sceneParent = parent;
        lightID = -1;
    }

    public GVRLight(GVRContext gvrContext) {
        super(gvrContext, NativeLight.ctor());
        isEnabled = true;
        sceneParent = null;
        lightID = -1;
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
    
    /**
     * Get the unique light ID.
     * 
     * This is a number which is different for every light
     * and is assigned by GearVRF when the light is added
     * to the scene.
     */
    public int getLightID() {
        return lightID;
    }
    
    /**
     * Get the GVRSceneObject this light is attached to.
     * 
     * If a light is attached to a scene object, its position
     * and direction are obtained from the scene object's transform.
     */
    public GVRSceneObject getParent() {
        return sceneParent;
    }
    
    /**
     * Access the shader source code implementing this light.
     *
     * The shader code for each light defines a function which
     * computes the color contributed by this light.
     * It takes a structure of uniforms and a <Surface>
     * structure as input and outputs a <Radiance> structure.
     * The format of the uniforms is defined by the shader descriptor.
     * The fragment shader is responsible for computing
     * the surface color and integrating the contribution
     * of each light to the final fragment color. It defines
     * the format of the <Radiance> and <Surface> structures.
     * {@link GVRShader }
     * {@link getShaderDescriptor}
     * @return
     */
    public String getShaderSource() {
    	return shaderSource;
    }
    
    /**
     * Access the descriptor defining the shader uniforms used by this light.
     *
     * Defines the GLSL structure representing the uniform data
     * passed to this light. These must match the structure
     * defined in the shader source code. Each light object
     * maintains a copy of these values and sends them to
     * the shader when they are updated.
     * the format of the <Radiance> and <Surface> structures.
     * {@link GVRShader }
     * {@link getShaderSource}
     * @return String describing light shader uniforms
     */
    public String getUniformDescriptor() {
        return uniformDescriptor;
    }
    
    // for future use
    @SuppressWarnings("unused")
    public float getFloat(String key) {
        return NativeLight.getFloat(getNative(), key);
    }

    // for future use
    @SuppressWarnings("unused")
    public void setFloat(String key, float value) {
        checkStringNotNullOrEmpty("key", key);
        checkFloatNotNaNOrInfinity("value", value);
        NativeLight.setFloat(getNative(), key, value);
    }

    public float[] getVec3(String key) {
        return NativeLight.getVec3(getNative(), key);
    }

    public void setVec3(String key, float x, float y, float z) {
        checkStringNotNullOrEmpty("key", key);
        NativeLight.setVec3(getNative(), key, x, y, z);
    }

    public float[] getVec4(String key) {
        return NativeLight.getVec4(getNative(), key);
    }

    public void setVec4(String key, float x, float y, float z, float w) {
        checkStringNotNullOrEmpty("key", key);
        NativeLight.setVec4(getNative(), key, x, y, z, w);
    }

    /**
     * Get the GLSL structure describing this light.
     * The name of the structure is derived from the light class name.
     * It is assumed that each different type of light will be
     * represented by a different Java class.
     *
     * @return String with shader structure description.
     */
    public String getShaderStruct() {
        Pattern pattern = Pattern.compile("[ ]*([fFiI][loatn]+)([0-9]*)[ ]+([A-Za-z0-9_]+)[,;:]*");
        Matcher matcher = pattern.matcher(uniformDescriptor);
        String structDesc = "struct Data" + getClass().getSimpleName() + " {\n";
        while (matcher.find())
        {
            String name = matcher.group(3);
            String size = matcher.group(2);
            String type = matcher.group(1);

            if (size.length() > 0) {
                if (type.toLowerCase().startsWith("i"))
                    structDesc += "   ivec" + size;
                else
                    structDesc += "   vec" + size;
                }
            else 
                structDesc += type;
            structDesc += " " + name + ";\n";
        }
        structDesc += "};\n";
        return structDesc;
    }
    
    /**
     * Define the shader source to compute the illumination from this light.
     * 
     * This source must be defined at the time the light is constructed.
     * Typically, different subclasses of GVRLight will have different
     * illumination functions.
     * @param source String with shader source code.
     */
    protected void setShaderSource(String source) {
        shaderSource = source;
    }
    
    private boolean isEnabled;
    protected GVRSceneObject sceneParent = null;
    protected int  lightID;
    protected String shaderSource = null;
    protected String uniformDescriptor = null;
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
    
    static native void setParent(long light, long sceneobj);
}
