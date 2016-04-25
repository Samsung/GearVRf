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
 * The API shared by {@link GVRMaterialMap} and {@link GVRPostEffectMap}.
 * 
 * A {@link GVRShaders} implementation specifies a shader <i>via</i> an opaque
 * type, and contains named values. These names are not necessarily the same as
 * the names of the uniforms in the shader program: the methods of this
 * interface let you map names from materials to programs.
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
public interface GVRShaderMaps {
    /**
     * Adds a link from a texture in a {@link GVRShaders} to a GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The texture's key in the {@link GVRShaders}.
     */
    void addTextureKey(String variableName, String key);

    /**
     * Adds a link from a float in a {@link GVRShaders} to a GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The float's key in the post effect data.
     */
    void addUniformFloatKey(String variableName, String key);

    /**
     * Adds a link from a vec2 in a {@link GVRShaders} to a GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The vec2's key in the post effect data.
     */
    void addUniformVec2Key(String variableName, String key);

    /**
     * Adds a link from a vec3 in a {@link GVRShaders} to a GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The vec3's key in the post effect data.
     */
    void addUniformVec3Key(String variableName, String key);

    /**
     * Adds a link from a vec4 in a {@link GVRShaders} to a GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The vec4's key in the post effect data.
     */
    void addUniformVec4Key(String variableName, String key);
}
