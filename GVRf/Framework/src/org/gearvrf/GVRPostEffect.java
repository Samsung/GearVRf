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

/**
 * A "post effect" shader is a GL shader which can be inserted into the pipeline
 * between rendering the scene graph and applying lens distortion.
 * 
 * A {@link GVRPostEffect} combines the id of a (stock or custom) post effect
 * shader with shader data: you can, for example, apply the same shader to each
 * eye, using different parameters for each eye. It is actually quite similar to
 * {@link GVRMaterial}.
 */
public class GVRPostEffect extends GVRHybridObject implements
        GVRShaders<GVRPostEffectShaderId> {

    /** Selectors for pre-built post effect shaders. */
    public abstract static class GVRPostEffectShaderType {
        /**
         * Selects a post-effect shader that blends a color across the entire
         * scene.
         */
        public abstract static class ColorBlend {
            public static final GVRPostEffectShaderId ID = new GVRStockPostEffectShaderId(
                    0);
            public static final String R = "r";
            public static final String G = "g";
            public static final String B = "b";
            public static final String FACTOR = "factor";
        }

        /** Selects a post-effect shader that flips the scene horizontally. */
        public abstract static class HorizontalFlip {
            public static final GVRPostEffectShaderId ID = new GVRStockPostEffectShaderId(
                    1);
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
    public GVRPostEffect(GVRContext gvrContext, GVRPostEffectShaderId shaderId) {
        super(gvrContext, NativePostEffectData.ctor(shaderId.ID));
    }

    /** @return The post-effect shader id */
    public GVRPostEffectShaderId getShaderType() {
        final int shaderType = NativePostEffectData.getShaderType(getPtr());
        return GVRPostEffectShaderId.get(shaderType);
    }

    /**
     * Select a post-effect shader.
     * 
     * @param shaderId
     *            The new shader.
     */
    public void setShaderType(GVRPostEffectShaderId shaderId) {
        NativePostEffectData.setShaderType(getPtr(), shaderId.ID);
    }

    public GVRTexture getMainTexture() {
        return getTexture(MAIN_TEXTURE);
    }

    public void setMainTexture(GVRTexture texture) {
        setTexture(MAIN_TEXTURE, texture);
    }

    public GVRTexture getTexture(String key) {
        long ptr = NativePostEffectData.getTexture(getPtr(), key);
        if (ptr == 0) {
            return null;
        } else {
            return GVRTexture.factory(getGVRContext(), ptr);
        }
    }

    public void setTexture(String key, GVRTexture texture) {
        NativePostEffectData.setTexture(getPtr(), key, texture.getPtr());
    }

    public float getFloat(String key) {
        return NativePostEffectData.getFloat(getPtr(), key);
    }

    public void setFloat(String key, float value) {
        NativePostEffectData.setFloat(getPtr(), key, value);
    }

    public float[] getVec2(String key) {
        return NativePostEffectData.getVec2(getPtr(), key);
    }

    public void setVec2(String key, float x, float y) {
        NativePostEffectData.setVec2(getPtr(), key, x, y);
    }

    public float[] getVec3(String key) {
        return NativePostEffectData.getVec3(getPtr(), key);
    }

    public void setVec3(String key, float x, float y, float z) {
        NativePostEffectData.setVec3(getPtr(), key, x, y, z);
    }

    public float[] getVec4(String key) {
        return NativePostEffectData.getVec4(getPtr(), key);
    }

    public void setVec4(String key, float x, float y, float z, float w) {
        NativePostEffectData.setVec4(getPtr(), key, x, y, z, w);
    }
}

class NativePostEffectData {
    public static native long ctor(int shaderType);

    public static native int getShaderType(long postEffectData);

    public static native void setShaderType(long postEffectData, long shaderType);

    public static native long getTexture(long postEffectData, String key);

    public static native void setTexture(long postEffectData, String key,
            long texture);

    public static native float getFloat(long postEffectData, String key);

    public static native void setFloat(long postEffectData, String key,
            float value);

    public static native float[] getVec2(long postEffectData, String key);

    public static native void setVec2(long postEffectData, String key, float x,
            float y);

    public static native float[] getVec3(long postEffectData, String key);

    public static native void setVec3(long postEffectData, String key, float x,
            float y, float z);

    public static native float[] getVec4(long postEffectData, String key);

    public static native void setVec4(long postEffectData, String key, float x,
            float y, float z, float w);
}