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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gearvrf.GVRSceneObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import android.util.Log;

/**
 * Base class for defining light sources.
 *
 * Lights are implemented by the fragment shader. Each different light
 * implementation corresponds to a subclass of GVRLightBase which is
 * responsible for supplying the shader source code for the light. GearVRF
 * aggregates all of the light source implementations into a single fragment
 * shader.
 *
 * Each subclass of GVRLightBase is a different light implementation and has
 * different shader source. The uniform descriptor is a string which gives the
 * name and type of all uniforms expected in the shader source. It is supplied
 * when a light is created to describe the expected shader input.
 *
 * GearVRF will automatically compute shadow maps for a light if shadow casting
 * is enabled. The light vertex and fragment shader must be implemented to take
 * advantage of these shadow maps.
 * 
 * @see GVRShaderTemplate
 * @see GVRRenderData.bindShader
 * @see GVRLightBase.setCastShadow
 */
public class GVRLightBase extends GVRComponent implements GVRDrawFrameListener
{
    protected Matrix4f lightrot;
    protected Vector3f olddir;
    protected Vector3f oldpos;
    protected Vector3f newdir;
    protected Vector3f newpos;
    
    public GVRLightBase(GVRContext gvrContext, GVRSceneObject parent)
    {
        super(gvrContext, NativeLight.ctor());
        setOwnerObject(parent);
        uniformDescriptor = "float enabled vec3 world_position vec3 world_direction";
        vertexDescriptor = null;
        setFloat("enabled", 1.0f);
        lightrot = new Matrix4f();
        newdir = new Vector3f(0.0f, 0.0f, -1.0f);
        olddir = new Vector3f();
        oldpos = new Vector3f();
        newpos = new Vector3f();
        setVec3("world_position", 0.0f, 0.0f, 0.0f);
        setVec3("world_direction", 0.0f, 0.0f, 1.0f);
    }

    public GVRLightBase(GVRContext gvrContext)
    {
        super(gvrContext, NativeLight.ctor());
        uniformDescriptor = "float enabled vec3 world_position vec3 world_direction";
        vertexDescriptor = null;
        lightrot = new Matrix4f();
        olddir = new Vector3f();
        oldpos = new Vector3f();
        newpos = new Vector3f();
        newdir = new Vector3f(0.0f, 0.0f, -1.0f);
        setFloat("enabled", 1.0f);
        setVec3("world_position", 0.0f, 0.0f, 0.0f);
        setVec3("world_direction", 0.0f, 0.0f, 1.0f);
    }


    static public long getComponentType() {
        return NativeLight.getComponentType();
    }

    /**
     * Enable or disable shadow casting by this light.
     *
     * If shadow casting is enabled, GearVRF will compute shadow maps
     * for the all of the lights which cast shadows. This is computationally
     * intensive because it requires rendering the entire scene from the viewpoint
     * of the light. It is memory intensive because it requires keeping a framebuffer
     * (shadow map) for each shadow-casting light.
     * 
     * In order for a light to actually produce shadows, it must employ a
     * shader that performs the shadow map calculation. The built-in {@link GVRDirectLight}
     * and {@link GVRSpotLight} can produce shadows. {@link GVRPointLight} does not currently implement
     * shadow casting.
     * 
     * This function will create the material and shader used for making shadow maps
     * if necessary. It will also cause the HAS_SHADOWS symbol to be defined in the
     * shader if shadow casting is enabled.
     *
     * @param enableFlag true to enable shadow casting, false to disable
     */
    public void setCastShadow(boolean enableFlag)
    {
        GVRContext context = getGVRContext();
        if (enableFlag)
        {
            if (mShadowMaterial == null)
            {
                mShadowMaterial = new GVRMaterial(context);
                GVRShaderTemplate depthShader = context.getMaterialShaderManager().retrieveShaderTemplate(GVRDepthShader.class);
                depthShader.bindShader(context, mShadowMaterial);
            }
            NativeLight.setCastShadow(getNative(), mShadowMaterial.getNative());
        }
        else
        {
            NativeLight.setCastShadow(getNative(), 0);            
        }
     }
    
    /**
     * Determines if this light is currently casting shadows.
     * @return true if shadow casting enabled, else false
     */
    public boolean getCastShadow()
    {
        return NativeLight.getCastShadow(getNative());
    }
    
    public void setOwnerObject(GVRSceneObject newOwner)
    {
        if (owner == newOwner)
            return;
        if (newOwner != null)
        {
            if (owner == null)
                getGVRContext().registerDrawFrameListener(this);
        }
        else if (owner != null)
            getGVRContext().unregisterDrawFrameListener(this);
        super.setOwnerObject(newOwner);
    }

    /**
     * Enable the light.
     */
    @Override
    public void enable()
    {
        setFloat("enabled", 1.0f);
        super.enable();
    }

    /**
     * Disable the light.
     */
    @Override
    public void disable()
    {
        setFloat("enabled", 0.0f);
        super.disable();
    }
    
    /**
     * Get the position of the light in world coordinates.
     * 
     * The position is computed from the scene object the light is attached to.
     * It corresponds to the "world_position" uniform for the light.
     * 
     * @return the world position of the light as a 3 element array
     */
    public float[] getPosition() {
        return getVec3("world_position");
    }

    /**
     * Set the world position of the light.
     * 
     * The position is computed from the scene object the light is attached to.
     * It corresponds to the "world_position" uniform for the light.
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
     * Get the light ID.
     * 
     * This is a string that uniquely identifies the light and is generated by
     * GearVRF when it is added to the scene. It is used to generate shader code.
     */
    public String getLightID()
    {
        return NativeLight.getLightID(getNative());
    }

    /**
     * Access the fragment shader source code implementing this light.
     *
     * The shader code defines a function which computes the
     * color contributed by this light. It takes a structure of uniforms and a
     * <Surface> structure as input and outputs a <Radiance> structure. The
     * contents of the uniform structure is defined by the uniform descriptor. The fragment
     * shader is responsible for computing the surface color and integrating the
     * contribution of each light to the final fragment color. It defines the
     * format of the <Radiance> and <Surface> structures.
     * @see GVRShaderTemplate
     * @see GVRLightBase.getUniformDescriptor
     * 
     * @return string with source for light fragment shader
     */
    public String getFragmentShaderSource()
    {
        return fragmentShaderSource;
    }

    /**
     * Access the vertex shader source code implementing this light.
     *
     * The shader code defines a function which computes the per-vertex
     * outputs for this light. The input is a structure of uniforms and the output
     * is a varying structure which is used by the fragment shader.
     * The format of the vertex output for the light is defined
     * by the vertex shader descriptor.
     * 
     * @see GVRShaderTemplate
     * @see GVRLightBase.getVertexDescriptor
     * 
     * @return string with source for light vertex shader
     */
    public String getVertexShaderSource()
    {
        return vertexShaderSource;
    }
    
    /**
     * Access the descriptor defining the shader uniforms used by this light.
     *
     * Describes the uniform data passed to the shader for this light.
     * This string produces the structure defined in the shader source code.
     * Each light object maintains a copy of these values and sends them to the
     * shader when they are updated.
     * 
     * @see GVRShaderTemplate
     * @see GVRLightBase.getFragmentShaderSource
     * 
     * @return String describing light shader uniforms
     */
    public String getUniformDescriptor()
    {
        return uniformDescriptor;
    }

    /**
     * Access the descriptor defining the vertex shader output produced by this light.
     *
     * Defines the GLSL structure representing the varying data for this light.
     * These produce the structure defined in the shader source code.
     * Each light object maintains a copy of these values and sends them to the
     * shader when they are updated.
     * 
     * @return String describing light vertex shader output
     */
    public String getVertexDescriptor()
    {
        return vertexDescriptor;
    }
    
    /**
     * Gets the value of a floating uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @return floating point value of uniform
     * @throws exception
     *             if uniform name not found
     */
    @SuppressWarnings("unused")
    public float getFloat(String key)
    {
        return NativeLight.getFloat(getNative(), key);
    }

    /**
     * Sets the value of a floating uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @param new
     *            floating point value of uniform
     * @throws exception
     *             if uniform name not found
     */
    @SuppressWarnings("unused")
    public void setFloat(String key, float value)
    {
        checkStringNotNullOrEmpty("key", key);
        checkFloatNotNaNOrInfinity("value", value);
        NativeLight.setFloat(getNative(), key, value);
    }

    /**
     * Gets the value of a vec3 floating uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @return vec3 value of uniform
     * @throws exception
     *             if uniform name not found
     */
    public float[] getVec3(String key)
    {
        return NativeLight.getVec3(getNative(), key);
    }

    /**
     * Sets the value of a vec3 uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @param new
     *            vec3 value of uniform
     * @throws exception
     *             if uniform name not found
     */
    public void setVec3(String key, float x, float y, float z)
    {
        checkStringNotNullOrEmpty("key", key);
        NativeLight.setVec3(getNative(), key, x, y, z);
    }

    /**
     * Gets the value of a vec4 floating uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @return vec4 value of uniform
     * @throws exception
     *             if uniform name not found
     */
    public float[] getVec4(String key)
    {
        return NativeLight.getVec4(getNative(), key);
    }

    /**
     * Sets the value of a vec4 uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @param new
     *            vec4 value of uniform
     * @throws exception
     *             if uniform name not found
     */
    public void setVec4(String key, float x, float y, float z, float w)
    {
        checkStringNotNullOrEmpty("key", key);
        NativeLight.setVec4(getNative(), key, x, y, z, w);
    }

    /**
     * Sets the value of a 4x4 matrix uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @param new
     *            4x4 matrix value of uniform
     * @throws exception
     *             if uniform name not found
     */
    public void setMat4(String key, Matrix4f matrix)
    {
        float[] data = new float[16];
        matrix.get(data);
        checkStringNotNullOrEmpty("key", key);
        NativeLight.setMat4(getNative(), key, data);
    }

    /**
     * Gets the value of a 4x4 matrix uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @throws exception
     *             if uniform name not found
     */
    public Matrix4f getMat4(String key)
    {
        float[] data = new float[16];
        checkStringNotNullOrEmpty("key", key);
        NativeLight.getMat4(getNative(), key, data);
        Matrix4f matrix = new Matrix4f();
        matrix.set(data);
        return matrix;
    }

    /**
     * Get the default orientation of the light when there is no transformation
     * applied.
     */
    public Quaternionf getDefaultOrientation()
    {
        return defaultDir;
    }

    /**
     * Set the default orientation of the light when there is no transformation
     * applied.
     * 
     * GearVRF lights default to looking down the positive Z axis with a light
     * direction of (0, 0, 1). This function lets you change the initial forward
     * vector for lights. This orientation is multiplied by the world
     * transformation matrix of the scene object the light is attached to in
     * order to derive the light direction in world space that is passed to the
     * fragment shader.
     * 
     * @param orientation
     *            quaternion with the initial light orientation
     */
    public void setDefaultOrientation(Quaternionf orientation)
    {
        defaultDir = orientation;
    }

    /**
     * Updates the position and direction of this light from the transform of
     * scene object that owns it.
     */
    
    public void onDrawFrame(float frameTime)
    {     
        if ((getFloat("enabled") <= 0.0f) || (owner == null)) { return; }
        float[] odir = getVec3("world_direction");
        float[] opos = getVec3("world_position");
        GVRSceneObject parent = owner;
        Matrix4f worldmtx = parent.getTransform().getModelMatrix4f();
        olddir.x = odir[0];
        olddir.y = odir[1];
        olddir.z = odir[2];
        
        oldpos.x = opos[0];
        oldpos.y = opos[1];
        oldpos.z = opos[2];

        newdir.x = 0.0f;
        newdir.y = 0.0f;
        newdir.z = -1.0f;
        
        lightrot.identity();
        
        defaultDir.get(lightrot);
        worldmtx.getTranslation(newpos);
        worldmtx.mul(lightrot);
        worldmtx.transformDirection(newdir);
        if ((olddir.x != newdir.x) || (olddir.y != newdir.y) || (olddir.z != newdir.z))
        {
            setVec3("world_direction", newdir.x, newdir.y, newdir.z);
        }
        if ((oldpos.x != newpos.x) || (oldpos.y != newpos.y) || (oldpos.z != newpos.z))
        {
            setVec3("world_position", newpos.x, newpos.y, newpos.z);
        }
    }

    protected Quaternionf defaultDir = new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
    protected String fragmentShaderSource = null;
    protected String vertexShaderSource = null;
    protected String uniformDescriptor = null;
    protected String vertexDescriptor = null;
    static protected GVRMaterial mShadowMaterial = null;
}

class NativeLight
{
    static native long ctor();

    static native long getComponentType();

    static native void enable(long light);

    static native void disable(long light);

    static native float getFloat(long light, String key);

    static native void setFloat(long light, String key, float value);

    static native float[] getVec3(long light, String key);

    static native void setVec3(long light, String key, float x, float y, float z);

    static native float[] getVec4(long light, String key);

    static native void setVec4(long light, String key, float x, float y, float z, float w);

    static native String getLightID(long light);

    static native void setLightID(long light, String id);
    
    static native void getMat4(long light, String key, float[] matrix);
    
    static native void setMat4(long light, String key, float[] matrix);
    
    static native void setCastShadow(long light, long material);
    
    static native boolean getCastShadow(long light);
}
