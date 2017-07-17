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
import org.gearvrf.utility.Log;

import android.graphics.Color;

/**
 * Encapsulates the data needed for shading, including textures and shader uniforms.
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
 * <li>The id of a shader, which is used to draw the mesh. See
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
 * {@code}
 * // for example
 * GVRMaterial material = new GVRMaterial(gvrContext);
 * material.setMainTexture(texture);
 * }
 * </pre>
 */
public class GVRMaterial extends  GVRShaderData
{

    private static final String TAG = Log.tag(GVRMaterial.class);

    static final String MAIN_TEXTURE = "u_texture";

    /** Pre-built shader ids. */
    public abstract static class GVRShaderType {
        public abstract static class Color {
            public static final GVRShaderId ID = new GVRShaderId(GVRColorShader.class);
        }

        public abstract static class UnlitHorizontalStereo {
            public static final GVRShaderId ID = new GVRShaderId(GVRUnlitHorizontalStereoShader.class);
        }

        public abstract static class UnlitVerticalStereo {
            public static final GVRShaderId ID = new GVRShaderId(GVRUnlitVerticalStereoShader.class);
        }

        public abstract static class OES {
            public static final GVRShaderId ID = new GVRShaderId(GVROESShader.class);
        }

        public abstract static class OESHorizontalStereo {
            public static final GVRShaderId ID = new GVRShaderId(GVROESHorizontalStereoShader.class);
        }

        public abstract static class OESVerticalStereo {
            public static final GVRShaderId ID = new GVRShaderId(GVROESVerticalStereoShader.class);
        }

        public abstract static class Cubemap {
            public static final GVRShaderId ID = new GVRShaderId(GVRCubemapShader.class);
        }

        public abstract static class CubemapReflection {
            public static final GVRShaderId ID = new GVRShaderId(GVRCubemapReflectionShader.class);
        }

        public abstract static class Texture {
            public static final GVRShaderId ID = new GVRShaderId(GVRTextureShader.class);
        }

        public abstract static class Phong {
            public static final GVRShaderId ID = new GVRShaderId(GVRPhongShader.class);
        }

        public abstract static class UnlitFBO {
            public static final GVRShaderId ID = new GVRShaderId(GVRUnlitFBOShader.class);
        }

        public abstract static class LightMap {
            public static final GVRShaderId ID = new GVRShaderId(GVRLightmapShader.class);
        }

        public abstract static class PhongLayered {
            public static final GVRShaderId ID = new GVRShaderId(GVRPhongLayeredShader.class);
        }

        public abstract static class VerticalFlip {
            public static final GVRShaderId ID = new GVRShaderId(GVRVerticalFlipShader.class);
        }

        public abstract static class HorizontalFlip {
            public static final GVRShaderId ID = new GVRShaderId(GVRHorizontalFlipShader.class);
        }

        public abstract static class ColorBlend {
            public static final GVRShaderId ID = new GVRShaderId(GVRColorBlendShader.class);
        }

    };

    /**
     * A new holder for a shader's uniforms.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param shaderId
     *            Id of a {@linkplain GVRShaderType stock} or
     *            {@linkplain GVRMaterialShaderManager custom} shader.
     */
    public GVRMaterial(GVRContext gvrContext, GVRShaderId shaderId) {
        super(gvrContext, shaderId);
    }

    /**
     * A convenience overload: builds a {@link GVRMaterial} that uses the most
     * common stock shader, the {@linkplain GVRShaderType.Texture 'texture'} shader.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVRMaterial(GVRContext gvrContext) {
        this(gvrContext, GVRShaderType.Texture.ID);
    }

    /**
     * The {@link GVRTexture texture} currently bound to the
     * {@code main_texture} shader uniform.
     *
     * With most shaders, this is the texture that is actually displayed.
     *
     * @return The {@linkplain GVRTexture main texture}
     */
    public GVRTexture getMainTexture()  {
        return getTexture(MAIN_TEXTURE);
    }

    /**
     * Bind a different {@link GVRTexture texture} to the {@code main_texture}
     * shader uniform.
     *
     * @param texture
     *            The {@link GVRTexture} to bind.
     */
    public void setMainTexture(GVRTexture texture)  {
        setTexture(MAIN_TEXTURE, texture);
    }


    /**
     * Set the baked light map texture
     *
     * @param texture
     *            Texture with baked light map
     */
    public void setLightMapTexture(GVRTexture texture) {
        setTexture("lightmap_texture", texture);
    }

    /**
     * Set the light map information(offset and scale) at UV space to
     * map the light map texture to the mesh.
     *
     * @param lightMapInformation
     *            Atlas information object with the offset and scale
     * at UV space necessary to map the light map texture to the mesh.
     */
    public void setLightMapInfo(GVRAtlasInformation lightMapInformation) {
        setTextureAtlasInfo("lightmap", lightMapInformation);
    }

    /**
     * Set the light map information(offset and scale) at UV space to
     * map the light map texture to the mesh.
     *
     * @param key
     *            Prefix name of the uniform at light map shader:
     *            ([key]_texture, [key]_offset and [key]_scale.
     * @param atlasInformation
     *            Atlas information object with the offset and scale
     * at UV space necessary to map the light map texture to the mesh.
     */
    public void setTextureAtlasInfo(String key, GVRAtlasInformation atlasInformation) {
        setTextureAtlasInfo(key, atlasInformation.getOffset(), atlasInformation.getScale());
    }

    /**
     * Set the light map information(offset and scale) at UV space to
     * map the light map texture to the mesh.
     *
     * @param key
     *            Prefix name of the uniform at light map shader:
     *            ([key]_texture, [key]_offset and [key]_scale.
     * @param offset
     *            Array with x and y offset values at UV space
     *            to map the 2D texture to the mesh.
     * @param scale
     *            Array with x and y scale values at UV space
     *            to map the 2D texture to the mesh.
     */
    public void setTextureAtlasInfo(String key, float[] offset, float[] scale) {
        setTextureOffset(key, offset);
        setTextureScale(key, scale);
    }

    /**
     * Returns the placement offset of texture {@code key}}
     * @param key Texture name. A common name is "main",
     *            "lightmap", etc.
     * @return    The vector of x and y at uv space.
     */
    public float[] getTextureOffset(String key) {
        return getVec2(key + "_offset");
    }

    /**
     * Set the placement offset of texture {@code key}}
     * @param key Texture name. A common name is "main",
     *            "lightmap", etc.
     */
    public void setTextureOffset(String key, float[] offset) {
        setVec2(key + "_offset", offset[0], offset[1]);
    }

    /**
     * Returns the placement scale of texture {@code key}}
     * @param key Texture name. A common name is "main",
     *            "lightmap", etc.
     * @return    The vector of x and y at uv space.
     */
    public float[] getTextureScale(String key) {
        return getVec2(key + "_scale");
    }

    /**
     * Set the placement scale of texture {@code key}}
     * @param key Texture name. A common name is "main",
     *            "lightmap", etc.
     */
    public void setTextureScale(String key, float[] scale) {
        setVec2(key + "_scale", scale[0], scale[1]);
    }

    /**
     * Get the {@code color} uniform.
     * 
     * By convention, some of the GVRF shaders can use a {@code vec3} uniform named
     * {@code color}. With the default {@linkplain GVRShaderType.Texture 'texture'
     * shader,} this allows you to modulate the texture with a color.
     * @return The current {@code vec3 color} as a three-element array
     */
    public float[] getColor() {
        return getVec3("u_color");
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
     * {@code color}. With the default {@linkplain GVRShaderType.Texture 'texture'
     * shader,} this allows you to modulate the texture with a color.
     * Values are between {@code 0.0f} and {@code 1.0f}, inclusive.
     * 
     * @param r
     *            Red
     * @param g
     *            Green
     * @param b
     *            Blue
     */
    public void setColor(float r, float g, float b) {
        setVec3("u_color", r, g, b);
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
     * Get the {@code materialAmbientColor} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code materialAmbientColor}. With the default {@linkplain GVRShaderType.Texture 'texture'
     * shader,} this allows you to modulate the texture with a color.
     * 
     * @return The current {@code vec4 materialAmbientColor} as a four-element
     *         array
     */
    public float[] getAmbientColor() {
        return getVec4("ambient_color");
    }

    /**
     * Set the {@code materialAmbientColor} uniform for lighting.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code materialAmbientColor}. With the {@linkplain GVRShaderType.Texture
     * shader,} this allows you to add an overlay ambient light color on
     * top of the texture. Values are between {@code 0.0f} and {@code 1.0f},
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
    public void setAmbientColor(float r, float g, float b, float a) {
        setVec4("ambient_color", r, g, b, a);
    }

    /**
     * Get the {@code materialDiffuseColor} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code materialDiffuseColor}. With the {@linkplain GVRShaderType.Texture
     *  shader,} this allows you to add an overlay color on top of the
     * texture.
     * 
     * @return The current {@code vec4 materialDiffuseColor} as a four-element
     *         array
     */
    public float[] getDiffuseColor() {
        return getVec4("diffuse_color");
    }

    /**
     * Set the {@code materialDiffuseColor} uniform for lighting.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code materialDiffuseColor}. With the {@linkplain GVRShaderType.Texture
     * shader,} this allows you to add an overlay diffuse light color on
     * top of the texture. Values are between {@code 0.0f} and {@code 1.0f},
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
    public void setDiffuseColor(float r, float g, float b, float a) {
        setVec4("diffuse_color", r, g, b, a);
    }

    /**
     * Get the {@code materialSpecularColor} uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code materialSpecularColor}. With the {@linkplain GVRShaderType.Texture
     * shader,} this allows you to add an overlay color on top of the
     * texture.
     * 
     * @return The current {@code vec4 materialSpecularColor} as a four-element
     *         array
     */
    public float[] getSpecularColor() {
        return getVec4("specular_color");
    }

    /**
     * Set the {@code materialSpecularColor} uniform for lighting.
     * 
     * By convention, GVRF shaders can use a {@code vec4} uniform named
     * {@code materialSpecularColor}. With the {@linkplain GVRShaderType.Texture
     * hader,} this allows you to add an overlay specular light color on
     * top of the texture. Values are between {@code 0.0f} and {@code 1.0f},
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
    public void setSpecularColor(float r, float g, float b, float a) {
        setVec4("specular_color", r, g, b, a);
    }

    /**
     * Get the {@code materialSpecularExponent} uniform.
     * 
     * By convention, GVRF shaders can use a {@code float} uniform named
     * {@code materialSpecularExponent}. With the {@linkplain GVRShaderType.Texture
     * shader,} this allows you to add an overlay color on top of the
     * texture.
     * 
     * @return The current {@code vec4 materialSpecularExponent} as a float
     *         value.
     */
    public float getSpecularExponent() {
        return getFloat("specular_exponent");
    }

    /**
     * Set the {@code materialSpecularExponent} uniform for lighting.
     * 
     * By convention, GVRF shaders can use a {@code float} uniform named
     * {@code materialSpecularExponent}. With the {@linkplain GVRShaderType.Texture
     * shader,} this allows you to add an overlay specular light color on
     * top of the texture. Values are between {@code 0.0f} and {@code 128.0f},
     * inclusive.
     * 
     * @param exp
     *            Specular exponent
     */
    public void setSpecularExponent(float exp) {
        setFloat("specular_exponent", exp);
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
        return getFloat("u_opacity");
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
     * {@linkplain GVRMaterial.GVRShaderType.Texture} shader) setting
     * {@code opacity} does exactly what you expect; you only have to worry
     * about the render order. However, it is totally up to a custom shader
     * whether or how it will handle opacity.
     * 
     * @param opacity
     *            Value between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public void setOpacity(float opacity) {
        setFloat("u_opacity", opacity);
    }


    /**
     * Gets the line width for line drawing.
     * 
     * @see GVRRenderData#setDrawMode(int)
     */
    public float getLineWidth() {
        return getFloat("line_width");
    }
    
    /**
     * Sets the line width for line drawing.
     * 
     * By default, the line width is 1. It is applied when the
     * draw mode is GL_LINES, GL_LINE_STRIP or GL_LINE_LOOP.
     * 
     * @param lineWidth new line width.
     * @see GVRRenderData#setDrawMode(int)
     */
    public void setLineWidth(float lineWidth) {
        setFloat("line_width", lineWidth);
    }

}

