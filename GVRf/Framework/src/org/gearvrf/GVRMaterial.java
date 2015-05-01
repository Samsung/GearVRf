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

import java.util.concurrent.Future;

import org.gearvrf.utility.Colors;
import org.gearvrf.utility.Threads;
import static org.gearvrf.utility.Preconditions.*;

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
 * 
 * <li>Data to pass to the shader. This usually - but not always - means a
 * {@link GVRTexture} and can include other named values to pass to the shader.
 * </ul>
 * 
 * <p>
 * The simplest way to create a {@link GVRMaterial} is to call the
 * {@linkplain GVRMaterial#GVRMaterial(GVRContext) constructor that takes only a
 * GVRContext.} Then you just {@link GVRMaterial#setMainTexture(GVRTexture)
 * setMainTexture()} and you're ready to draw with the default shader, which is
 * called 'unlit' because it simply drapes the texture over the mesh, without
 * any lighting or reflection effects.
 * 
 * <pre>
 * // for example
 * GVRMaterial material = new GVRMaterial(gvrContext);
 * material.setMainTexture(texture);
 * </pre>
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

        public abstract static class Cubemap {
            public static final GVRMaterialShaderId ID = new GVRStockMaterialShaderId(
                    6);
        }

        public abstract static class CubemapReflection {
            public static final GVRMaterialShaderId ID = new GVRStockMaterialShaderId(
                    7);
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

    /**
     * A convenience overload: builds a {@link GVRMaterial} that uses the most
     * common stock shader, the {@linkplain GVRShaderType.Unlit 'unlit'} shader.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVRMaterial(GVRContext gvrContext) {
        this(gvrContext, GVRShaderType.Unlit.ID);
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

    public void setMainTexture(Future<GVRTexture> texture) {
        setTexture(MAIN_TEXTURE, texture);
    }

    /**
     * Get the {@code color} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec3} uniform named
     * {@code color}. With the default {@linkplain GVRShaderType.Unlit 'unlit'
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
     * {@code color}. With the default {@linkplain GVRShaderType.Unlit 'unlit'
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
     * Get the opacity.
     * 
     * This method returns the {@code opacity} uniform.
     * 
     * The {@linkplain #setOpacity(float) setOpacity() documentation} explains
     * what the {@code opacity} uniform does.
     * 
     * @return The {@code opacity} uniform used to render this material
     */
    public float getOpacity() {
        return getFloat("opacity");
    }

    /**
     * Set the opacity, in a complicated way.
     * 
     * There are two things you need to know, how opacity is applied, and how
     * opacity is implemented.
     * 
     * <p>
     * First, GVRF does not sort by distance every object it can see, then draw
     * from back to front. Rather, it sorts every object by
     * {@linkplain GVRRenderData#getRenderingOrder() render order,} then draws
     * the {@linkplain GVRScene scene graph} in traversal order. So, if you want
     * to see a scene object through another scene object, you have to
     * explicitly {@linkplain GVRRenderData#setRenderingOrder(int) set the
     * rendering order} so that the translucent object draws after the opaque
     * object. You can use any integer values you like, but GVRF supplies
     * {@linkplain GVRRenderData.GVRRenderingOrder four standard values;} the
     * {@linkplain GVRRenderData#getRenderingOrder() default value} is
     * {@linkplain GVRRenderData.GVRRenderingOrder#GEOMETRY GEOMETRY.}
     * 
     * <p>
     * Second, technically all this method does is set the {@code opacity}
     * uniform. What this does depends on the actual shader. If you don't
     * specify a shader (or you specify the
     * {@linkplain GVRMaterial.GVRShaderType.Unlit#ID unlit} shader) setting
     * {@code opacity} does exactly what you expect; you only have to worry
     * about the render order. However, it is totally up to a custom shader
     * whether or how it will handle opacity.
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
        checkStringNotNullOrEmpty("key", key);
        checkNotNull("texture", texture);
        NativeMaterial.setTexture(getPtr(), key, texture.getPtr());
    }

    public void setTexture(final String key, final Future<GVRTexture> texture) {
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

    public float getFloat(String key) {
        return NativeMaterial.getFloat(getPtr(), key);
    }

    public void setFloat(String key, float value) {
        checkStringNotNullOrEmpty("key", key);
        checkFloatNotNaNOrInfinity("value", value);
        NativeMaterial.setFloat(getPtr(), key, value);
    }

    public float[] getVec2(String key) {
        return NativeMaterial.getVec2(getPtr(), key);
    }

    public void setVec2(String key, float x, float y) {
        checkStringNotNullOrEmpty("key", key);
        NativeMaterial.setVec2(getPtr(), key, x, y);
    }

    public float[] getVec3(String key) {
        return NativeMaterial.getVec3(getPtr(), key);
    }

    public void setVec3(String key, float x, float y, float z) {
        checkStringNotNullOrEmpty("key", key);
        NativeMaterial.setVec3(getPtr(), key, x, y, z);
    }

    public float[] getVec4(String key) {
        return NativeMaterial.getVec4(getPtr(), key);
    }

    public void setVec4(String key, float x, float y, float z, float w) {
        checkStringNotNullOrEmpty("key", key);
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
        checkStringNotNullOrEmpty("key", key);
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
