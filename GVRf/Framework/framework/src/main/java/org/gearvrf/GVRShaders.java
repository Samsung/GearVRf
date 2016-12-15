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

/**
 * The API shared by {@link GVRMaterial} and {@link GVRPostEffect}.
 *
 * <p>
 * <table border="1">
 * <tr>
 * <td>{@link GVRMaterialShaderManager} {@code implements}
 * {@link GVRShaderManagers}</td>
 * <td>{@link GVRMaterialMap} {@code implements} {@link GVRShaderMaps}</td>
 * <td>{@link GVRMaterial} {@code implements} {@link GVRShaders}</td>
 * </tr>
 * <tr>
 * <td>{@link GVRPostEffectShaderManager} {@code implements}
 * {@link GVRShaderManagers}</td>
 * <td>{@link GVRPostEffectMap} {@code implements} {@link GVRShaderMaps}</td>
 * <td>{@link GVRPostEffect} {@code implements} {@link GVRShaders}</td>
 * </tr>
 * </table>
 * </p>
 */
public interface GVRShaders<ID> {

    static final String MAIN_TEXTURE = "main_texture";

    /** @return The current shader id. */
    public ID getShaderType();

    /**
     * Set shader id
     *
     * @param shaderId
     *            The new shader id. This is an opaque type, used to keep object
     *            and scene shader ids in distinct namespaces.
     */
    public void setShaderType(ID shaderId);

    /**
     * The {@link GVRTexture texture} currently bound to the
     * {@code main_texture} shader uniform.
     * 
     * With most shaders, this is the texture that is actually displayed.
     * 
     * @return The {@linkplain GVRTexture main texture}
     */
    public GVRTexture getMainTexture();

    /**
     * Bind a different {@link GVRTexture texture} to the {@code main_texture}
     * shader uniform.
     * 
     * @param texture
     *            The {@link GVRTexture} to bind.
     */
    public void setMainTexture(GVRTexture texture);

    /**
     * Asynchronously bind a different {@link GVRTexture texture} to the
     * {@code main_texture} shader uniform.
     * 
     * Uses a background thread from the thread pool to wait for the
     * {@code Future.get()} method; unless you are loading dozens of textures
     * asynchronously, the extra overhead should be modest compared to the cost
     * of loading a texture.
     * 
     * @param texture
     *            A future texture, from one of the the
     *            {@link GVRContext#loadFutureTexture(GVRAndroidResource)}
     *            methods
     * 
     * @since 1.6.7
     */
    public void setMainTexture(Future<GVRTexture> texture);

    /**
     * Get the {@link GVRTexture texture} currently bound to the shader uniform
     * {@code key}.
     * 
     * @param key
     *            A texture name
     * @return The current {@link GVRTexture texture}.
     */
    public GVRTexture getTexture(String key);

    /**
     * Bind a {@link GVRTexture texture} to the shader uniform {@code key}.
     * 
     * @param key
     *            Name of the shader uniform to bind the texture to.
     * @param texture
     *            The {@link GVRTexture texture} to bind.
     */
    public void setTexture(String key, GVRTexture texture);

    /**
     * Asynchronously bind a {@link GVRTexture texture} to the shader uniform
     * {@code key}.
     * 
     * Uses a background thread from the thread pool to wait for the
     * {@code Future.get()} method; unless you are loading dozens of textures
     * asynchronously, the extra overhead should be modest compared to the cost
     * of loading a texture.
     * 
     * @param key
     *            Name of the shader uniform to bind the texture to.
     * @param texture
     *            The {@link GVRTexture texture} to bind.
     * 
     * @since 1.6.7
     */
    public void setTexture(String key, Future<GVRTexture> texture);

    /**
     * Get the {@code float} bound to the shader uniform {@code key}.
     * 
     * @param key
     *            Name of the shader uniform
     * @return The bound {@code float} value.
     */
    public float getFloat(String key);

    /**
     * Bind a {@code float} to the shader uniform {@code key}.
     * 
     * @param key
     *            Name of the shader uniform
     * @param value
     *            New data
     */
    public void setFloat(String key, float value);

    /**
     * Get the {@code float[2]} vector bound to the shader uniform {@code key}.
     * 
     * @param key
     *            Name of the shader uniform
     * @return The {@code vec2} as a Java {@code float[2]}
     */
    public float[] getVec2(String key);

    /**
     * Bind a {@code vec2} to the shader uniform {@code key}.
     * 
     * @param key
     *            Name of the shader uniform
     * @param x
     *            First component of the vector.
     * @param y
     *            Second component of the vector.
     */
    public void setVec2(String key, float x, float y);

    /**
     * Get the {@code float[3]} vector bound to the shader uniform {@code key}.
     * 
     * @param key
     *            Name of the shader uniform
     * @return The {@code vec3} as a Java {@code float[3]}
     */
    public float[] getVec3(String key);

    /**
     * Bind a {@code vec3} to the shader uniform {@code key}.
     *
     * @param key
     *            Name of the shader uniform to bind the data to.
     * @param x
     *            First component of the vector.
     * @param y
     *            Second component of the vector.
     * @param z
     *            Third component of the vector.
     */
    public void setVec3(String key, float x, float y, float z);

    /**
     * Get the {@code float[4]} vector bound to the shader uniform {@code key}.
     *
     * @param key
     *            Name of the shader uniform
     * @return The {@code vec4} as a Java {@code float[3]}
     */
    public float[] getVec4(String key);

    /**
     * Bind a {@code vec4} to the shader uniform {@code key}.
     *
     * @param key
     *            Name of the shader uniform to bind the data to.
     * @param x
     *            First component of the vector.
     * @param y
     *            Second component of the vector.
     * @param z
     *            Third component of the vector.
     * @param w
     *            Fourth component of the vector.
     */
    public void setVec4(String key, float x, float y, float z, float w);

    /**
     * Bind a {@code mat4} to the shader uniform {@code key}.
     *
     * @param key
     *            Name of the shader uniform to bind the data to.
     */
    public void setMat4(String key, float x1, float y1, float z1, float w1,
            float x2, float y2, float z2, float w2, float x3, float y3,
            float z3, float w3, float x4, float y4, float z4, float w4);
}
