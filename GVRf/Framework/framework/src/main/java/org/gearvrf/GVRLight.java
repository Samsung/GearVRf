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

import org.gearvrf.utility.Exceptions;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Base class for defining light sources.
 *
 * Lights are implemented by the fragment shader. Each different light
 * implementation corresponds to a subclass of GVRLight which is
 * responsible for supplying the shader source code for the light. GearVRF
 * aggregates all of the light source implementations into a single fragment
 * shader.
 *
 * Each subclass of GVRLight is a different light implementation and has
 * different shader source. The uniform descriptor is a string which gives the
 * name and type of all uniforms expected in the shader source. It is supplied
 * when a light is created to describe the expected shader input.
 *
 * GearVRF will automatically compute shadow maps for a light if shadow casting
 * is enabled. The light vertex and fragment shader must be implemented to take
 * advantage of these shadow maps.
 *
 * @see GVRShaderTemplate
 * @see GVRLight#setCastShadow(boolean)
 */
public class GVRLight extends GVRJavaComponent implements GVRDrawFrameListener
{
    protected final static String UNIFORM_DESC = "float enabled float shadow_map_index float pad1 float pad2 float4 world_position float4 world_direction ";
    protected Matrix4f mLightRot;
    protected Vector3f mOldDir;
    protected Vector3f mOldPos;
    protected Vector3f mNewDir;
    protected Vector3f mNewPos;
    protected Quaternionf mDefaultDir = new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
    protected String mFragmentShaderSource = null;
    protected String mVertexShaderSource = null;
    protected String mUniformDescriptor = null;
    protected String mVertexDescriptor = null;
    protected boolean mCastShadow = false;

    protected GVRLight(GVRContext gvrContext, String uniformDesc, String vertexDesc)
    {
        super(gvrContext, NativeLight.ctor(uniformDesc));
        mUniformDescriptor = uniformDesc;
        mVertexDescriptor = vertexDesc;
        mLightRot = new Matrix4f();
        mOldDir = new Vector3f();
        mOldPos = new Vector3f();
        mNewPos = new Vector3f();
        mNewDir = new Vector3f(0.0f, 0.0f, -1.0f);
        setFloat("enabled", 1.0f);
        setVec4("world_position", 0.0f, 0.0f, 0.0f, 10.0f);
        setVec4("world_direction", 0.0f, 0.0f, 1.0f, 0.0f);
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
        if (enableFlag)
        {
            throw new UnsupportedOperationException("This light cannot cast shadows");
        }
    }

    public void setShadowRange(float near, float far)
    {
        throw new UnsupportedOperationException("This light cannot cast shadows");
    }

    /**
     * Determines if this light is currently casting shadows.
     * @return true if shadow casting enabled, else false
     */
    public boolean getCastShadow()
    {
        return mCastShadow;
    }

    public void setOwnerObject(GVRSceneObject newOwner)
    {
        if (owner == newOwner)
            return;
        if (newOwner != null)
        {
            if (owner == null)
            {
                getGVRContext().registerDrawFrameListener(this);
                super.setOwnerObject(newOwner);
                if (mCastShadow)
                {
                    GVRShadowMap shadowMap = (GVRShadowMap) getComponent(GVRRenderTarget.getComponentType());
                    if (shadowMap == null)
                    {
                        setCastShadow(true);
                    }
                }
            }
        }
        else if (owner != null)
        {
            getGVRContext().unregisterDrawFrameListener(this);
            super.setOwnerObject(newOwner);
        }
    }


    /**
     * Gets the shadow material used in constructing shadow maps.
     * <p>
     * The shadow map is constructed using a depth map rendered
     * from the viewpoint of the light. This global material
     * does not currently contain any settable properties.
     * @return shadow map material
     */
    public static GVRMaterial getShadowMaterial(GVRContext ctx)
    {
        return GVRShadowMap.getShadowMaterial(ctx);
    }

    /**
     * Enable the light.
     */
    @Override
    public void onEnable()
    {
        setFloat("enabled", 1.0f);
    }

    /**
     * Disable the light.
     */
    @Override
    public void onDisable()
    {
        setFloat("enabled", 0.0f);
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
        return getVec4("world_position");
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
        setVec4("world_position", x, y, z, 1.0f);
    }

    /**
     * Get the light Class.
     *
     * This is a string that identifies the type of light and is generated by
     * GearVRF when it is added to the scene. It is used to generate shader code.
     */
    public String getLightClass()
    {
        return NativeLight.getLightClass(getNative());
    }

    /**
     * Get the light Name.
     *
     * This is a string that uniquely identifies the light and is generated by
     * GearVRF when it is added to the scene. It is used to generate shader code.
     */
    public String getLightName()
    {
        return NativeLight.getLightName(getNative());
    }

    /**
     * Access the fragment shader source code implementing this light.
     *
     * The shader code defines a function which computes the
     * color contributed by this light. It takes a structure of uniforms and a
     * Surface structure as input and outputs a Radiance structure. The
     * contents of the uniform structure is defined by the uniform descriptor. The fragment
     * shader is responsible for computing the surface color and integrating the
     * contribution of each light to the final fragment color. It defines the
     * format of the Radiance and Surface structures.
     * @see GVRShaderTemplate
     * @see GVRLight#getUniformDescriptor()
     *
     * @return string with source for light fragment shader
     */
    public String getFragmentShaderSource()
    {
        return mFragmentShaderSource;
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
     * @see GVRLight#getVertexDescriptor()
     *
     * @return string with source for light vertex shader
     */
    public String getVertexShaderSource()
    {
        return mVertexShaderSource;
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
     * @see GVRLight#getFragmentShaderSource()
     *
     * @return String describing light shader uniforms
     */
    public String getUniformDescriptor()
    {
        return mUniformDescriptor;
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
        return mVertexDescriptor;
    }


    /**
     * Get the {@code float} bound to the shader uniform {@code key}.
     *
     * @param key Name of the shader uniform
     * @return The bound {@code float} value, returns 0.0 if key does not exist.
     */
    public float getFloat(String key)
    {
        return NativeLight.getFloat(getNative(), key);
    }

    /**
     * Bind a {@code float} to the shader uniform {@code key}.
     * Throws an exception of the key is not found.
     *
     * @param key       Name of the shader uniform
     * @param value     New data
     */
    public void setFloat(String key, float value)
    {
        checkKeyIsUniform(key);
        checkFloatNotNaNOrInfinity("value", value);
        NativeLight.setFloat(getNative(), key, value);
    }

    /**
     * Get the {@code int} bound to the shader uniform {@code key}.
     *
     * @param key   Name of the shader uniform
     * @return The bound {@code int} value, 0 if key does not exist.
     */
    public int getInt(String key)
    {
        return NativeLight.getInt(getNative(), key);
    }

    /**
     * Bind an {@code int} to the shader uniform {@code key}.
     * Throws an exception of the key does not exist.
     * @param key       Name of the shader uniform
     * @param value     New data
     */
    public void setInt(String key, int value)
    {
        checkKeyIsUniform(key);
        NativeLight.setInt(getNative(), key, value);
    }

    /**
     * Get the value for a floating point uniform vector.
     * @param key name of uniform to get.
     * @return float array with value of named uniform.
     * @throws IllegalArgumentException if key is not in uniform descriptor.
     */
    public float[] getFloatVec(String key)
    {
        float[] vec = NativeLight.getFloatVec(getNative(), key);
        if (vec == null)
            throw new IllegalArgumentException("key " + key + " not found in light");
        return vec;
    }

    /**
     * Get the value for an integer uniform vector.
     * @param key name of uniform to get.
     * @return int array with value of named uniform.
     * @throws IllegalArgumentException if key is not in uniform descriptor.
     */
    public int[] getIntVec(String key)
    {
        int[] vec = NativeLight.getIntVec(getNative(), key);
        if (vec == null)
            throw new IllegalArgumentException("key " + key + " not found in light");
        return vec;
    }

    /**
     * Get the value for a floating point vector of length 2 (type float2).
     * @param key name of uniform to get.
     * @return float array with two values
     * @throws IllegalArgumentException if key is not in uniform descriptor.
     */
    public float[] getVec2(String key)
    {
        return getFloatVec(key);
    }

    /**
     * Set the value for a floating point vector of length 2.
     * @param key name of uniform to set.
     * @param x new X value
     * @param y new Y value
     * @see #getVec2
     * @see #getFloatVec(String)
     */
    public void setVec2(String key, float x, float y)
    {
        checkKeyIsUniform(key);
        NativeLight.setVec2(getNative(), key, x, y);
    }

    /**
     * Get the value for a floating point vector of length 3 (type float3).
     * @param key name of uniform to get.
     * @return float array with three values
     * @throws IllegalArgumentException if key is not in uniform descriptor.
     */
    public float[] getVec3(String key)
    {
        return getFloatVec(key);
    }

    /**
     * Set the value for a floating point vector of length 3.
     * @param key name of uniform to set.
     * @param x new X value
     * @param y new Y value
     * @param z new Z value
     * @see #getVec3
     * @see #getFloatVec(String)
     */
    public void setVec3(String key, float x, float y, float z)
    {
        checkKeyIsUniform(key);
        NativeLight.setVec3(getNative(), key, x, y, z);
    }

    /**
     * Get the value for a floating point vector of length 4 (type float4).
     * @param key name of uniform to get.
     * @return float array with four values
     * @throws IllegalArgumentException if key is not in uniform descriptor.
     */
    public float[] getVec4(String key)
    {
        return getFloatVec(key);
    }

    /**
     * Set the value for a floating point vector of length 4.
     * @param key name of uniform to set.
     * @param x new X value
     * @param y new Y value
     * @param z new Z value
     * @param w new W value
     * @see #getVec4
     * @see #getFloatVec(String)
     */
    public void setVec4(String key, float x, float y, float z, float w)
    {
        checkKeyIsUniform(key);
        NativeLight.setVec4(getNative(), key, x, y, z, w);
    }



    /**
     * Set the value for a floating point 4x4 matrix.
     * @param key name of uniform to set.
     * @see #getFloatVec(String)
     */
    public void setMat4(String key, float x1, float y1, float z1, float w1,
                        float x2, float y2, float z2, float w2, float x3, float y3,
                        float z3, float w3, float x4, float y4, float z4, float w4)
    {
        checkKeyIsUniform(key);
        NativeLight.setMat4(getNative(), key, x1, y1, z1, w1, x2, y2,
                            z2, w2, x3, y3, z3, w3, x4, y4, z4, w4);
    }

    /**
     * Set the value for a floating point vector uniform.
     * <p>
     * @param key name of uniform to set.
     * @param val floating point array with new data. The size of the array must be
     *            at least as large as the uniform being updated.
     * @throws IllegalArgumentException if key is not in uniform descriptor or array is wrong length.
     * @see #getFloatVec(String)
     */
    public void setFloatArray(String key, float val[])
    {
        checkKeyIsUniform(key);
        NativeLight.setFloatVec(getNative(), key, val, val.length);
    }

    /**
     * Set the value for an integer vector uniform.
     * <p>
     * @param key name of uniform to set.
     * @param val integer array with new data. The size of the array must be
     *            at least as large as the uniform being updated.
     * @throws IllegalArgumentException if key is not in uniform descriptor or array is wrong length.
     * @see #getIntVec(String)
     */
    public void setIntArray(String key, int val[])
    {
        checkKeyIsUniform(key);
        NativeLight.setIntVec(getNative(), key, val, val.length);
    }

    private void checkKeyIsUniform(String key)
    {
        checkStringNotNullOrEmpty("key", key);
        if (!mUniformDescriptor.contains(key))
        {
            throw Exceptions.IllegalArgument("key " + key + " not in material");
        }
    }

    /**
     * Get the default orientation of the light when there is no transformation
     * applied.
     */
    public Quaternionf getDefaultOrientation()
    {
        return mDefaultDir;
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
        mDefaultDir = orientation;
        mDefaultDir.get(mLightRot);
    }


    public int getLightIndex()
    {
        return NativeLight.getLightIndex(getNative());
    }

    protected void setLightClass(String className)
    {
        NativeLight.setLightClass(getNative(), className);
    }

    String getShaderType(String name)
    {
        return NativeLight.getShaderType(getNative(), name);
    }

    String makeShaderLayout()
    {
        return NativeLight.makeShaderLayout(getNative());
    }

    static String makeShaderBlock(GVRScene scene)
    {
        return NativeLight.makeShaderBlock(scene.getNative());
    }

    /**
     * Updates the position and direction of this light from the transform of
     * scene object that owns it.
     */
    public void onDrawFrame(float frameTime)
    {
        if (!isEnabled() || (owner == null) || (getFloat("enabled") <= 0.0f))
        {
            return;
        }
        float[] odir = getVec3("world_direction");
        float[] opos = getVec3("world_position");
        GVRSceneObject parent = owner;
        Matrix4f worldmtx = parent.getTransform().getModelMatrix4f();

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
        if ((mOldDir.x != mNewDir.x) || (mOldDir.y != mNewDir.y) || (mOldDir.z != mNewDir.z))
        {
            setVec4("world_direction", mNewDir.x, mNewDir.y, mNewDir.z, 0);
        }
        if ((mOldPos.x != mNewPos.x) || (mOldPos.y != mNewPos.y) || (mOldPos.z != mNewPos.z))
        {
            setPosition(mNewPos.x, mNewPos.y, mNewPos.z);
        }
    }

}

class NativeLight
{
    static native long ctor(String uniformDesc);

    static native long getComponentType();

    static native String getLightName(long light);

    static native String getLightClass(long light);

    static native void setLightClass(long light, String id);

    static native int getLightIndex(long light);

    static native boolean hasUniform(long light, String key);

    static native boolean hasTexture(long light, String key);

    static native void setTexture(long light, String key, long texture);

    static native float getFloat(long light, String key);

    static native void setFloat(long light, String key, float value);

    static native int getInt(long light, String key);

    static native void setInt(long light, String key, int value);

    static native float[] getFloatVec(long light, String key);

    static native void setFloatVec(long light, String key, float[] val, int n);

    static native void setIntVec(long light, String key, int[] val, int n);

    static native int[] getIntVec(long light, String key);

    static native void setVec2(long light, String key, float x, float y);

    static native void setVec3(long light, String key, float x,
                               float y, float z);

    static native void setVec4(long light, String key, float x,
                               float y, float z, float w);

    static native float[] getMat4(long light, String key);

    static native void setMat4(long light, String key, float x1,
                               float y1, float z1, float w1, float x2, float y2, float z2,
                               float w2, float x3, float y3, float z3, float w3, float x4,
                               float y4, float z4, float w4);

    static native String getShaderType(long light, String name);

    static native String makeShaderBlock(long scene);

    static native String makeShaderLayout(long light);
}