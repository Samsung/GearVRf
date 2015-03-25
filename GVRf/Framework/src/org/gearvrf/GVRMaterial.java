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

import org.gearvrf.utility.Colors;

import android.graphics.Color;

/**
 * This is one of the key GVRF classes: it holds shaders with textures.
 * 
 * You can have invisible {@linkplain GVRSceneObject scene objects:} these have
 * a location and a set of child objects. This can be useful, to move a set of
 * scene objects as a unit, preserving their relative geometry. Invisible scene
 * objects don't need any {@linkplain GVRSceneObject#getRenderData() render
 * data.}
 * 
 * <p>
 * Visible scene objects must have render data
 * {@linkplain GVRSceneObject#attachRenderData(GVRRenderData) attached.} Each
 * {@link GVRRenderData} has a {@link GVRMesh GL mesh} that defines its
 * geometry, and a {@link GVRMaterial} that defines its surface.
 * 
 * <p>
 * Each {@link GVRMaterial} contains two main things:
 * <ul>
 * <li>The id of a (stock or custom) shader, which is used to draw the mesh. See
 * {@link GVRShaderType} and {@link GVRContext#getMaterialShaderManager()}.
 * <li>Data to pass to the shader. This usually - but not always - means a
 * {@link GVRTexture} and can include other named values to pass to the shader.
 * </ul>
 */
public class GVRMaterial extends GVRHybridObject implements
        GVRShaders<GVRMaterialShaderId> {

    /** Pre-built shader ids. */
    public abstract static class GVRShaderType {

        public abstract static class Unlit {
            public static final GVRMaterialShaderId ID = new GVRStockMaterialShaderId(
                    0);
        }

        public abstract static class UnlitHorizontalStereo {
            public static final GVRMaterialShaderId ID = new GVRStockMaterialShaderId(
                    1);
        }

        public abstract static class UnlitVerticalStereo {
            public static final GVRMaterialShaderId ID = new GVRStockMaterialShaderId(
                    2);
        }

        public abstract static class OES {
            public static final GVRMaterialShaderId ID = new GVRStockMaterialShaderId(
                    3);
        }

        public abstract static class OESHorizontalStereo {
            public static final GVRMaterialShaderId ID = new GVRStockMaterialShaderId(
                    4);
        }

        public abstract static class OESVerticalStereo {
            public static final GVRMaterialShaderId ID = new GVRStockMaterialShaderId(
                    5);
        }
    };

    /**
     * A new holder for a shader's uniforms.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param shaderType
     *            Id of a {@linkplain GVRShaderType stock} or
     *            {@linkplain GVRMaterialShaderManager custom} shader.
     */
    public GVRMaterial(GVRContext gvrContext, GVRMaterialShaderId shaderType) {
        super(gvrContext, NativeMaterial.ctor(shaderType.ID));
    }

    GVRMaterial(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    public GVRMaterialShaderId getShaderType() {
        final int shaderType = NativeMaterial.getShaderType(getPtr());
        return GVRMaterialShaderId.get(shaderType);
    }

    /**
     * Set shader id
     * 
     * @param shaderId
     *            The new shader id.
     */
    public void setShaderType(GVRMaterialShaderId shaderId) {
        NativeMaterial.setShaderType(getPtr(), shaderId.ID);
    }

    public GVRTexture getMainTexture() {
        return getTexture(MAIN_TEXTURE);
    }

    public void setMainTexture(GVRTexture texture) {
        setTexture(MAIN_TEXTURE, texture);
    }

    /**
     * Get the {@code color} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec3} uniform named
     * {@code color}. With the common {@linkplain GVRShaderType.Unlit 'unlit'
     * shader,} this allows you to add an overlay color on top of the texture.
     * 
     * @return The current {@code vec3 color} as a three-element array
     */
    public float[] getColor() {
        return getVec3("color");
    }

    /**
     * A convenience method that wraps {@link #getColor()} and returns an
     * Android {@link Color}
     * 
     * @return An Android {@link Color}
     */
    public int getRgbColor() {
        return Colors.toColor(getColor());
    }

    /**
     * Set the {@code color} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec3} uniform named
     * {@code color}. With the common {@linkplain GVRShaderType.Unlit 'unlit'
     * shader,} this allows you to add an overlay color on top of the texture.
     * Values are between {@code 0.0f} and {@code 1.0f}, inclusive. .
     * 
     * @param r
     *            Red
     * @param g
     *            Green
     * @param b
     *            Blue
     */
    public void setColor(float r, float g, float b) {
        setVec3("color", r, g, b);
    }

    /**
     * A convenience overload of {@link #setColor(float, float, float)} that
     * lets you use familiar Android {@link Color} values.
     * 
     * @param color
     *            Any Android {@link Color}; the alpha byte is ignored.
     */
    public void setColor(int color) {
        setColor(Colors.byteToGl(Color.red(color)), //
                Colors.byteToGl(Color.green(color)), //
                Colors.byteToGl(Color.blue(color)));
    }

    /**
     * Get the {@code opacity} uniform.
     * 
     * By convention, GVRF shaders can use a {@code float} uniform named
     * {@code opacity}. With the default {@linkplain GVRShaderType.Unlit 'unlit'
     * shader,} this controls the opacity of the whole material.
     * 
     * @return The current {@code opacity} uniform
     */
    public float getOpacity() {
        return getFloat("opacity");
    }

    /**
     * Set the {@code opacity} uniform.
     * 
     * By convention, GVRF shaders can use a {@code float} uniform named
     * {@code opacity}. With the default {@linkplain GVRShaderType.Unlit 'unlit'
     * shader,} this controls the opacity of the whole material.
     * 
     * @param opacity
     *            Value between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public void setOpacity(float opacity) {
        setFloat("opacity", opacity);
    }

    public GVRTexture getTexture(String key) {
        long ptr = NativeMaterial.getTexture(getPtr(), key);
        if (ptr == 0) {
            return null;
        } else {
            return GVRTexture.factory(getGVRContext(), ptr);
        }
    }

    public void setTexture(String key, GVRTexture texture) {
        NativeMaterial.setTexture(getPtr(), key, texture.getPtr());
    }

    public float getFloat(String key) {
        return NativeMaterial.getFloat(getPtr(), key);
    }

    public void setFloat(String key, float value) {
        NativeMaterial.setFloat(getPtr(), key, value);
    }

    public float[] getVec2(String key) {
        return NativeMaterial.getVec2(getPtr(), key);
    }

    public void setVec2(String key, float x, float y) {
        NativeMaterial.setVec2(getPtr(), key, x, y);
    }

    public float[] getVec3(String key) {
        return NativeMaterial.getVec3(getPtr(), key);
    }

    public void setVec3(String key, float x, float y, float z) {
        NativeMaterial.setVec3(getPtr(), key, x, y, z);
    }

    public float[] getVec4(String key) {
        return NativeMaterial.getVec4(getPtr(), key);
    }

    public void setVec4(String key, float x, float y, float z, float w) {
        NativeMaterial.setVec4(getPtr(), key, x, y, z, w);
    }

    /**
     * Bind a {@code mat4} to the shader uniform {@code key}.
     * 
     * @param key
     *            Name of the shader uniform
     */
    public void setMat4(String key, float x1, float y1, float z1, float w1,
            float x2, float y2, float z2, float w2, float x3, float y3,
            float z3, float w3, float x4, float y4, float z4, float w4) {
        NativeMaterial.setMat4(getPtr(), key, x1, y1, z1, w1, x2, y2, z2, w2,
                x3, y3, z3, w3, x4, y4, z4, w4);
    }
}

class NativeMaterial {
    public static native long ctor(int shaderType);

    public static native int getShaderType(long material);

    public static native void setShaderType(long material, long shaderType);

    public static native long getTexture(long material, String key);

    public static native void setTexture(long material, String key, long texture);

    public static native float getFloat(long material, String key);

    public static native void setFloat(long material, String key, float value);

    public static native float[] getVec2(long material, String key);

    public static native void setVec2(long material, String key, float x,
            float y);

    public static native float[] getVec3(long material, String key);

    public static native void setVec3(long material, String key, float x,
            float y, float z);

    public static native float[] getVec4(long material, String key);

    public static native void setVec4(long material, String key, float x,
            float y, float z, float w);

    public static native void setMat4(long material, String key, float x1,
            float y1, float z1, float w1, float x2, float y2, float z2,
            float w2, float x3, float y3, float z3, float w3, float x4,
            float y4, float z4, float w4);
}
