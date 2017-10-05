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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader;
import org.gearvrf.utility.Exceptions;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.Threads;

import static org.gearvrf.utility.Assert.checkFloatNotNaNOrInfinity;
import static org.gearvrf.utility.Assert.checkStringNotNullOrEmpty;

/**
 * GVRShaderData encapculates data to be sent to a vertex or fragment shader.
 * It contains a list of key / value pairs which can specify arbitrary length
 * float or int vectors. It also has key / value pairs for the texture
 * samplers to be used with the shader.
 *
 * GVRShaderData can be used for a "post-effect" to update the
 * framebuffer after main rendering but before lens distortion.
 * A post effect shader can, for example, apply the same shader to each
 * eye, using different parameters for each eye.
 *
 * It is important to fully initialize your material before
 * @see GVRMaterial
 */
public class GVRShaderData extends GVRHybridObject
{
    private static final String TAG = Log.tag(GVRShaderData.class);

    protected GVRShaderId mShaderId;
    protected String mUniformDescriptor = null;
    protected String mTextureDescriptor = null;

    protected static class TextureInfo
    {
        public GVRTexture Texture;
        public String TexCoordAttr;
        public String ShaderVar;
    }

    final protected Map<String, GVRTexture> textures = new HashMap();

    /**
     * Initialize a post effect, with a shader id.
     *
     * @param gvrContext Current {@link GVRContext}
     * @param shaderId   Shader ID from {@link org.gearvrf.GVRMaterial.GVRShaderType} or
     *                   {@link GVRContext#getMaterialShaderManager()}.
     */
    public GVRShaderData(GVRContext gvrContext, GVRShaderId shaderId)
    {
        super(gvrContext, NativeShaderData.ctor(shaderId.getUniformDescriptor(gvrContext),
                                                shaderId.getTextureDescriptor(gvrContext)));
        GVRShader shader = shaderId.getTemplate(gvrContext);
        GVRShaderManager shaderManager = gvrContext.getMaterialShaderManager();
        mShaderId = shaderManager.getShaderType(shaderId.ID);
        mUniformDescriptor = shader.getUniformDescriptor();
        mTextureDescriptor = shader.getTextureDescriptor();
        shader.setMaterialDefaults(this);
    }

    protected GVRShaderData(GVRContext gvrContext, GVRShaderId shaderId, long constructor)
    {
        super(gvrContext, constructor);
        GVRShader shader = shaderId.getTemplate(gvrContext);
        GVRShaderManager shaderManager = gvrContext.getMaterialShaderManager();
        mShaderId = shaderManager.getShaderType(shaderId.ID);
        mUniformDescriptor = shader.getUniformDescriptor();
        mTextureDescriptor = shader.getTextureDescriptor();
        shader.setMaterialDefaults(this);
    }

    /**
     * Get the shader type for this material.
     * @return GVRShaderId designating the shader this material uses.
     */
    public GVRShaderId getShaderType()
    {
        return mShaderId;
    }

    /**
     * Gets the string describing the uniforms allowed in this material.
     * @return string with uniform descriptor from shader
     */
    public String getUniformDescriptor() { return mUniformDescriptor; }

    /**
     * Gets the string describing the textures allowed in this material.
     * @return string with texture descriptor from shader
     */
    public String getTextureDescriptor() { return mTextureDescriptor; }

    /**
     * Determine whether a named uniform is defined
     * by this material.
     *
     * @param name of uniform in shader and material
     * @return true if uniform defined, else false
     */
    public boolean hasUniform(String name)
    {
        return NativeShaderData.hasUniform(getNative(), name);
    }

    /**
     * Determine whether a named texture has been set.
     * This function will return true if the texture
     * has been set even if it is NULL.
     *
     * @param name of texture
     * @return true if texture has been set, else false
     * @see #getTexture
     * @see #hasUniform
     */
    public boolean hasTexture(String name)
    {
        return textures.containsKey(name);
    }

    /**
     * Return the names of all the textures used by this post effect.
     *
     * @return list of texture names
     */
    public Set<String> getTextureNames()
    {
        Set<String> texNames = textures.keySet();
        return texNames;
    }

    /**
     * Get the {@link GVRTexture texture} currently bound to the shader uniform {@code key}.
     *
     * @param key   name of texture to find
     * @return The current {@link GVRTexture texture}.
     */
    public GVRTexture getTexture(String key)
    {
        return textures.get(key);
    }

    /**
     * Bind a {@link GVRTexture texture} to the shader uniform {@code key}.
     *
     * @param key       Name of the shader uniform to bind the texture to.
     * @param texture   The {@link GVRTexture texture} to bind.
     */
    public void setTexture(String key, GVRTexture texture)
    {
        checkStringNotNullOrEmpty("key", key);
        synchronized (textures)
        {
            textures.put(key, texture);
            if (texture != null)
            {
                NativeShaderData.setTexture(getNative(), key, texture.getNative());
            }
        }
    }


    /**
     * Get the {@code float} bound to the shader uniform {@code key}.
     *
     * @param key Name of the shader uniform
     * @return The bound {@code float} value, returns 0.0 if key does not exist.
     */
    public float getFloat(String key)
    {
        return NativeShaderData.getFloat(getNative(), key);
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
        NativeShaderData.setFloat(getNative(), key, value);
    }

    /**
     * Get the {@code int} bound to the shader uniform {@code key}.
     *
     * @param key   Name of the shader uniform
     * @return The bound {@code int} value, 0 if key does not exist.
     */
    public int getInt(String key)
    {
        return NativeShaderData.getInt(getNative(), key);
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
        NativeShaderData.setInt(getNative(), key, value);
    }

    public float[] getFloatVec(String key)
    {
        float[] vec = NativeShaderData.getFloatVec(getNative(), key);
        if (vec == null)
            throw new IllegalArgumentException("key " + key + " not found in material");
        return vec;
    }

    public int[] getIntVec(String key)
    {
        int[] vec = NativeShaderData.getIntVec(getNative(), key);
        if (vec == null)
            throw new IllegalArgumentException("key " + key + " not found in material");
        return vec;
    }

    public float[] getVec2(String key)
    {
        return getFloatVec(key);
    }

    public void setVec2(String key, float x, float y)
    {
        checkKeyIsUniform(key);
        NativeShaderData.setVec2(getNative(), key, x, y);
    }

    public float[] getVec3(String key)
    {
        return getFloatVec(key);
    }

    public void setVec3(String key, float x, float y, float z)
    {
        checkKeyIsUniform(key);
        NativeShaderData.setVec3(getNative(), key, x, y, z);
    }

    public float[] getVec4(String key)
    {
        return getFloatVec(key);
    }

    public void setVec4(String key, float x, float y, float z, float w)
    {
        checkKeyIsUniform(key);
        NativeShaderData.setVec4(getNative(), key, x, y, z, w);
    }

    public void setMat4(String key, float x1, float y1, float z1, float w1,
                        float x2, float y2, float z2, float w2, float x3, float y3,
                        float z3, float w3, float x4, float y4, float z4, float w4)
    {
        checkKeyIsUniform(key);
        NativeShaderData.setMat4(getNative(), key, x1, y1, z1, w1, x2, y2,
                                 z2, w2, x3, y3, z3, w3, x4, y4, z4, w4);
    }

    public void setFloatArray(String key, float val[])
    {
        checkKeyIsUniform(key);
        NativeShaderData.setFloatVec(getNative(), key, val, val.length);
    }

    public void setIntArray(String key, int val[])
    {
        checkKeyIsUniform(key);
        NativeShaderData.setIntVec(getNative(), key, val, val.length);
    }

    private void checkKeyIsTexture(String key)
    {
        checkStringNotNullOrEmpty("key", key);
        if (!mTextureDescriptor.contains(key))
        {
            throw Exceptions.IllegalArgument("key " + key + " not in material");
        }
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
     * Designate the vertex attribute and shader variable for the texture coordinates
     * associated with the named texture.
     *
     * @param texName       name of texture
     * @param texCoordAttr  name of vertex attribute with texture coordinates.
     * @param shaderVarName name of shader variable to get texture coordinates.
     */
    public void setTexCoord(String texName, String texCoordAttr, String shaderVarName)
    {
        synchronized (textures)
        {
            GVRTexture tex = textures.get(texName);
            if (tex != null)
            {
                tex.setTexCoord(texCoordAttr, shaderVarName);
            }
        }
    }

    /**
     * Gets the name of the vertex attribute containing the texture
     * coordinates for the named texture.
     *
     * @param texName name of texture
     * @return name of texture coordinate vertex attribute
     */
    public String getTexCoordAttr(String texName)
    {
        GVRTexture tex = textures.get(texName);
        if (tex != null)
        {
            return tex.getTexCoordAttr();
        }
        return null;
    }

    /**
     * Gets the name of the shader variable to get the texture
     * coordinates for the named texture.
     *
     * @param texName name of texture
     * @return name of shader variable
     */
    public String getTexCoordShaderVar(String texName)
    {
        GVRTexture tex = textures.get(texName);
        if (tex != null)
        {
            return tex.getTexCoordShaderVar();
        }
        return null;
    }

    /**
     * Construct a string describing the shader layout of this material.
     * @return shader layout string
     */
    String makeShaderLayout() { return NativeShaderData.makeShaderLayout(getNative()); }
    void useGpuBuffer(boolean flag) { NativeShaderData.useGpuBuffer(getNative(), flag);}
}

class NativeShaderData {
    static native long ctor(String uniformDesc, String textureDesc);
    static native void useGpuBuffer(long shaderData, boolean flag);

    static native boolean hasUniform(long shaderData, String key);

    static native boolean hasTexture(long shaderData, String key);

    static native void setTexture(long shaderData, String key, long texture);

    static native float getFloat(long shaderData, String key);

    static native void setFloat(long shaderData, String key, float value);

    static native int getInt(long shaderData, String key);

    static native void setInt(long shaderData, String key, int value);

    static native float[] getFloatVec(long shaderData, String key);
    static native void setFloatVec(long shaderData, String key, float[] val, int n);
    static native void setIntVec(long shaderData, String key, int[] val, int n);

    static native int[] getIntVec(long shaderData, String key);

    static native void setVec2(long shaderData, String key, float x, float y);

    static native void setVec3(long shaderData, String key, float x,
            float y, float z);

    static native void setVec4(long shaderData, String key, float x,
            float y, float z, float w);

    static native float[] getMat4(long shaderData, String key);

    static native void setMat4(long shaderData, String key, float x1,
            float y1, float z1, float w1, float x2, float y2, float z2,
            float w2, float x3, float y3, float z3, float w3, float x4,
            float y4, float z4, float w4);

    static native String makeShaderLayout(long shaderData);
}