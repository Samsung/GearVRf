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

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
 * @see GVRRenderData#bindShader(GVRScene)
 * @see GVRLightBase#setCastShadow(boolean)
 */
public class GVRLightBase extends GVRComponent implements GVRDrawFrameListener
{
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
    static protected GVRMaterial sShadowMaterial = null;

    public GVRLightBase(GVRContext gvrContext, GVRSceneObject parent)
    {
        super(gvrContext, NativeLight.ctor());
        setOwnerObject(parent);
        mUniformDescriptor = "float enabled vec3 world_position vec3 world_direction";
        mVertexDescriptor = null;
        setFloat("enabled", 1.0f);
        mLightRot = new Matrix4f();
        mNewDir = new Vector3f(0.0f, 0.0f, -1.0f);
        mOldDir = new Vector3f();
        mOldPos = new Vector3f();
        mNewPos = new Vector3f();
        setVec3("world_position", 0.0f, 0.0f, 0.0f);
        setVec3("world_direction", 0.0f, 0.0f, 1.0f);
    }

    public GVRLightBase(GVRContext gvrContext)
    {
        super(gvrContext, NativeLight.ctor());
        mUniformDescriptor = "float enabled vec3 world_position vec3 world_direction";
        mVertexDescriptor = null;
        mLightRot = new Matrix4f();
        mOldDir = new Vector3f();
        mOldPos = new Vector3f();
        mNewPos = new Vector3f();
        mNewDir = new Vector3f(0.0f, 0.0f, -1.0f);
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
                    shadowMap = new GVRShadowMap(getGVRContext());
                    owner.attachComponent(shadowMap);
                }
            }
            else if (shadowMap != null)
            {
                shadowMap.setEnable(false);
            }
        }
        mCastShadow = enableFlag;
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
     *
     * The shadow material has several public attributes which affect the shadow
     * map construction:
     *  - shadow_near   near plane of the shadow map camera (default 0.1)
     *  - shadow_far    far plane of the shadow map camera (default 50)
     * The shadow map is constructed using a depth map rendered
     * from the viewpoint of the light. This global material
     * contains the shadow map properties. Modifying the near and far
     * planes change how much of the scene is visible from the light.
     * The shadow map will be more detailed if this range is small.
     * It may be blocky if the range is too large.
     *
     * Note that shadow_near and shadow_far will be deprecated in the next release.
     * The proper way to change the near and far planes of the shadow map
     * camera is to call {@link GVRShadowMap#getCamera } and then call
     * {@link GVRPerspectiveCamera#setNearClippingDistance(float)} and
     * {@link GVRPerspectiveCamera#setFarClippingDistance(float)}}.
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
     * Surface structure as input and outputs a Radiance structure. The
     * contents of the uniform structure is defined by the uniform descriptor. The fragment
     * shader is responsible for computing the surface color and integrating the
     * contribution of each light to the final fragment color. It defines the
     * format of the Radiance and Surface structures.
     * @see GVRShaderTemplate
     * @see GVRLightBase#getUniformDescriptor()
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
     * @see GVRLightBase#getVertexDescriptor()
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
     * @see GVRLightBase#getFragmentShaderSource()
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
     * Gets the value of a floating uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @return floating point value of uniform
     */
    public float getFloat(String key)
    {
        return NativeLight.getFloat(getNative(), key);
    }

    /**
     * Sets the value of a floating uniform based on its name.
     * 
     * @param key
     *            name of uniform to get
     * @param value
     *            floating point value of uniform
     */
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
     * @param x     X coordinate of vector
     * @param y     Y coordinate of vector
     * @param z     Z coordinate of vector
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
     */
    public float[] getVec4(String key)
    {
        return NativeLight.getVec4(getNative(), key);
    }

    /**
     * Sets the value of a vec4 uniform based on its name.
     * 
     * @param key   name of uniform to get
     * @param x     X coordinate of vector
     * @param y     Y coordinate of vector
     * @param z     Z coordinate of vector
     * @param w     W coordinate of vector
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
     * @param matrix
     *            4x4 matrix value of uniform
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


/**
 * Updates the position and direction of this light from the transform of
 * scene object that owns it.
 */
    public void onDrawFrame(float frameTime)
    {     
        if (!isEnabled() || (getFloat("enabled") <= 0.0f) || (owner == null)) { return; }
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
            setVec3("world_direction", mNewDir.x, mNewDir.y, mNewDir.z);
        }
        if ((mOldPos.x != mNewPos.x) || (mOldPos.y != mNewPos.y) || (mOldPos.z != mNewPos.z))
        {
            setVec3("world_position", mNewPos.x, mNewPos.y, mNewPos.z);
        }
    }

}

class NativeLight
{
    static native long ctor();

    static native long getComponentType();

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
}
