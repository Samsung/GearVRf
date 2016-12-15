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
 * A "post effect" shader is a GL shader which can be inserted into the pipeline
 * between rendering the scene graph and applying lens distortion.
 * 
 * A {@link GVRPostEffect} combines the id of a post effect shader
 * with shader data: you can, for example, apply the same shader to each
 * eye, using different parameters for each eye. It is actually quite similar to
 * {@link GVRMaterial}.
 */
public class GVRPostEffect extends GVRHybridObject implements  GVRShaderData {
    private static final String TAG = Log.tag(GVRPostEffect.class);

    protected GVRShaderId mShaderId;
    protected String mUniformDescriptor = null;
    protected String mTextureDescriptor = null;
    private static class TextureInfo
    {
        public GVRTexture Texture;
        public String     TexCoordAttr;
        public String     ShaderVar;
    };
    final private Map<String, TextureInfo> textures = new HashMap();

    /** Selectors for pre-built post effect shaders. */
    public abstract static class GVRPostEffectShaderType {
        /**
         * Selects a post-effect shader that blends a color across the entire
         * scene.
         */
        public abstract static class ColorBlend {
            public static final GVRShaderId ID = new GVRShaderId(GVRColorBlendShader.class);
            public static final String R = "r";
            public static final String G = "g";
            public static final String B = "b";
            public static final String FACTOR = "factor";
        }
        /** Selects a post-effect shader that flips the scene horizontally. */
        public abstract static class HorizontalFlip {
            public static final GVRShaderId ID = new GVRShaderId(GVRHorizontalFlipShader.class);
        }
    };

    /**
     * Initialize a post effect, with a shader id.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param shaderId
     *            Shader ID from {@link GVRPostEffectShaderType} or
     *            {@link GVRContext#getPostEffectShaderManager()}.
     */
    public GVRPostEffect(GVRContext gvrContext, GVRShaderId shaderId) {
        super(gvrContext, NativeShaderData.ctor());
        GVRShader shader = shaderId.getTemplate(gvrContext);
        GVRPostEffectShaderManager shaderManager = gvrContext.getPostEffectShaderManager();
        mShaderId = shaderManager.getShaderType(shaderId.ID);
        mUniformDescriptor = shader.getUniformDescriptor();
        mTextureDescriptor = shader.getTextureDescriptor();

        //shader.setMaterialDefaults(this);
        NativeShaderData.setNativeShader(getNative(), mShaderId.getNativeShader(gvrContext, shaderManager));
    }

    protected GVRPostEffect(GVRContext gvrContext, GVRShaderId shaderId, long constructor) {
        super(gvrContext, constructor);
    }

    public GVRShaderId getShaderType() {
        return mShaderId;
    }

    /**
     * Determine whether a named uniform is defined
     * by this material.
     * @param name of uniform in shader and material
     * @return true if uniform defined, else false
     */
    public boolean hasUniform(String name) {
        return NativeShaderData.hasUniform(getNative(), name);
    }

    /**
     * Determine whether a named texture has been set.
     * This function will return true if the texture
     * has been set even if it is NULL.
     * @param name of texture
     * @return true if texture has been set, else false
     * @see #getTexture
     * @see #hasUniform
     */
    public boolean hasTexture(String name) {
        return textures.containsKey(name);
    }

    /**
     * Return the names of all the textures used by this post effect.
     * @return list of texture names
     */
    public Set<String> getTextureNames()
    {
        Set<String> texNames = textures.keySet();
        return texNames;
    }

    public GVRTexture getTexture(String key) {
        TextureInfo tinfo = textures.get(key);
        if (tinfo != null)
        {
            return tinfo.Texture;
        }
        return null;
    }


    public void setTexture(String key, GVRTexture texture) {
        checkStringNotNullOrEmpty("key", key);
        TextureInfo tinfo = textures.get(key);
        if (tinfo == null)
        {
            tinfo = new TextureInfo();
            tinfo.Texture = texture;
            textures.put(key, tinfo);
        }
        else
        {
            tinfo.Texture = texture;
        }
        if (texture != null)
            NativeShaderData.setTexture(getNative(), key, texture.getNative());
    }

    public void setTexture(final String key, final Future<GVRTexture> texture) {
        if (texture.isDone()) {
            try {
                setTexture(key, texture.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (texture instanceof GVRAsynchronousResourceLoader.FutureResource<?>) {
                setTexture(key, (GVRTexture) null);
                GVRAndroidResource.TextureCallback callback = new GVRAndroidResource.TextureCallback() {
                    @Override
                    public void loaded(GVRTexture texture,
                                       GVRAndroidResource ignored) {
                        setTexture(key, texture);
                        Log.d(TAG, "Finish loading and setting texture %s",
                                texture);
                    }

                    @Override
                    public void failed(Throwable t,
                                       GVRAndroidResource androidResource) {
                        Log.e(TAG, "Error loading texture %s; exception: %s",
                                texture, t.getMessage());
                    }

                    @Override
                    public boolean stillWanted(GVRAndroidResource androidResource) {
                        return true;
                    }
                };

                getGVRContext().getAssetLoader().loadTexture(
                        ((GVRAsynchronousResourceLoader.FutureResource<GVRTexture>) texture).getResource(), callback);
            } else {
                Threads.spawn(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            setTexture(key, texture.get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }


    public float getFloat(String key) {
        return NativeShaderData.getFloat(getNative(), key);
    }

    public void setFloat(String key, float value) {
        checkKeyIsUniform(key);
        checkFloatNotNaNOrInfinity("value", value);
        NativeShaderData.setFloat(getNative(), key, value);
    }

    public int getInt(String key) {
        return NativeShaderData.getInt(getNative(), key);
    }

    public void setInt(String key, int value) {
        checkKeyIsUniform(key);
        NativeShaderData.setInt(getNative(), key, value);
    }

    public float[] getVec2(String key)
    {
        float[] vec = NativeShaderData.getVec2(getNative(), key);
        if (vec == null)
            throw new IllegalArgumentException("key " + key + " not found in material");
        return vec;
    }

    public void setVec2(String key, float x, float y) {
        checkKeyIsUniform(key);
        NativeShaderData.setVec2(getNative(), key, x, y);
    }

    public float[] getVec3(String key)
    {
        float[] vec = NativeShaderData.getVec3(getNative(), key);
        if (vec == null)
            throw new IllegalArgumentException("key " + key + " not found in material");
        return vec;
    }

    public void setVec3(String key, float x, float y, float z) {
        checkKeyIsUniform(key);
        NativeShaderData.setVec3(getNative(), key, x, y, z);
    }

    public float[] getVec4(String key)
    {
        float[] vec = NativeShaderData.getVec4(getNative(), key);
        if (vec == null)
            throw new IllegalArgumentException("key " + key + " not found in material");
        return vec;
    }

    public void setVec4(String key, float x, float y, float z, float w) {
        checkKeyIsUniform(key);
        NativeShaderData.setVec4(getNative(), key, x, y, z, w);
    }

    public void setMat4(String key, float x1, float y1, float z1, float w1,
            float x2, float y2, float z2, float w2, float x3, float y3,
            float z3, float w3, float x4, float y4, float z4, float w4) {
        checkKeyIsUniform(key);
        NativeShaderData.setMat4(getNative(), key, x1, y1, z1, w1, x2, y2,
                z2, w2, x3, y3, z3, w3, x4, y4, z4, w4);
    }

    private void checkKeyIsTexture(String key)
    {
        checkStringNotNullOrEmpty("key", key);
        if (!mTextureDescriptor.contains(key)) {
            throw Exceptions.IllegalArgument("key " + key + " not in material");
        }
    }

    private void checkKeyIsUniform(String key)
    {
        checkStringNotNullOrEmpty("key", key);
        if (!mUniformDescriptor.contains(key)) {
            throw Exceptions.IllegalArgument("key " + key + " not in material");
        }
    }

    /**
     *  Designate the vertex attribute and shader variable for the texture coordinates
     *  associated with the named texture.
     * @param texName name of texture
     * @param texCoordAttr name of vertex attribute with texture coordinates.
     * @param shaderVarName name of shader variable to get texture coordinates.
     */
    public void setTexCoord(String texName, String texCoordAttr, String shaderVarName)
    {
        GVRPostEffect.TextureInfo tinfo = textures.get(texName);
        if (tinfo == null)
        {
            tinfo = new GVRPostEffect.TextureInfo();
            textures.put(texName, tinfo);
        }
        tinfo.TexCoordAttr = texCoordAttr;
        tinfo.ShaderVar = shaderVarName;
    }

    /**
     * Gets the name of the vertex attribute containing the texture
     * coordinates for the named texture.
     * @param texName name of texture
     * @return name of texture coordinate vertex attribute
     */
    public String getTexCoordAttr(String texName)
    {
        GVRPostEffect.TextureInfo tinfo = textures.get(texName);
        if (tinfo != null)
        {
            return tinfo.TexCoordAttr;
        }
        return null;
    }


    /**
     * Gets the name of the shader variable to get the texture
     * coordinates for the named texture.
     * @param texName name of texture
     * @return name of shader variable
     */
    public String getTexCoordShaderVar(String texName)
    {
        GVRPostEffect.TextureInfo tinfo = textures.get(texName);
        if (tinfo != null)
        {
            return tinfo.ShaderVar;
        }
        return null;
    }
}

class NativeShaderData {
    static native long ctor();

    static native int getNativeShader(long shaderData);

    static native void setNativeShader(long shaderData, int nativeShader);

    static native boolean hasUniform(long shaderData, String key);

    static native boolean hasTexture(long shaderData, String key);

    static native void setTexture(long shaderData, String key, long texture);

    static native float getFloat(long shaderData, String key);

    static native void setFloat(long shaderData, String key, float value);

    static native int getInt(long shaderData, String key);

    static native void setInt(long shaderData, String key, int value);

    static native float[] getVec2(long shaderData, String key);

    static native void setVec2(long shaderData, String key, float x, float y);

    static native float[] getVec3(long shaderData, String key);

    static native void setVec3(long shaderData, String key, float x,
            float y, float z);

    static native float[] getVec4(long shaderData, String key);

    static native void setVec4(long shaderData, String key, float x,
            float y, float z, float w);

    static native void setMat4(long shaderData, String key, float x1,
            float y1, float z1, float w1, float x2, float y2, float z2,
            float w2, float x3, float y3, float z3, float w3, float x4,
            float y4, float z4, float w4);
}