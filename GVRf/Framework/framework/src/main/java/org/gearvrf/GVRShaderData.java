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

import java.util.Set;
import java.util.concurrent.Future;

/**
 * Provides access to the uniform data used with a shader.
 *
 * This interface defines how the Java API accesses the
 * shader uniforms which are kept in the native layer.
 * @see GVRMaterial GVRPostEffect
 */
public interface GVRShaderData {

    public GVRShaderId getShaderType();

    /**
     * Return the names of all the textures used by this material.
     * @return list of texture names
     */
    public Set<String> getTextureNames();

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
     * Determine whether a named uniform has been set.
     * This function does not handle textures.
     * @param name of uniform
     * @return true if uniform has been set, else false
     * @see getTexture
     */
    public boolean hasUniform(String name);

    /**
     * Determine whether a named texture has been set.
     * This function will return true if the texture
     * has been set even if it is NULL.
     * @param name of texture
     * @return true if texture has been set, else false
     * @see getTexture hasUniform
     */
    public boolean hasTexture(String name);

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

    /**
     * Gets the name of the vertex attribute containing the texture
     * coordinates for the named texture.
     * @param texName name of texture
     * @return name of texture coordinate vertex attribute
     */
     public String getTexCoordAttr(String texName);

    /**
     * Gets the name of the shader variable to get the texture
     * coordinates for the named texture.
     * @param texName name of texture
     * @return name of shader variable
     */
    public String getTexCoordShaderVar(String texName);

}
