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

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * The API shared by {@link GVRMaterialShaderManager} and
 * {@link GVRPostEffectShaderManager}.
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
 * 
 * @param <MAP>
 *            The name mapping object, {@link GVRMaterialMap} or
 *            {@link GVRPostEffectMap}.
 * @param <ID>
 *            The opaque types {@link GVRCustomMaterialShaderId} and
 *            {@link GVRCustomPostEffectShaderId} that keep the 'object' and
 *            'scene' shader's namespaces separate, and eliminate the
 *            possibility that you could use an object shader id where you mean
 *            to use a scene shader id, and vice versa. You can pass an
 *            {@code ID} instance to {@link #getShaderMap(Object)
 *            getShaderMap(ID)}; additionally
 *            <ul>
 *            <li>You can pass an object shader {@code ID} to
 *            {@link GVRMaterial#GVRMaterial(GVRContext, GVRMaterialShaderId)}
 *            or {@link GVRMaterial#setShaderType(GVRMaterialShaderId)}.
 * 
 *            <li>You can pass an scene shader {@code ID} to
 *            {@link GVRPostEffect#GVRPostEffect(GVRContext, GVRPostEffectShaderId)}
 *            or {@link GVRPostEffect#setShaderType(GVRPostEffectShaderId)}.
 * 
 *            </ul>
 */
public interface GVRShaderManagers<MAP, ID> {

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code.
     * 
     * @param vertexShader
     *            GLSL source code for a vertex shader.
     * @param fragmentShader
     *            GLSL source code for a fragment shader.
     * @return An opaque type that you can pass to {@link #getShaderMap(Object)
     *         getShaderMap(ID)}, or to the {@link GVRMaterial} and
     *         {@link GVRPostEffect} constructors and {@code setShader} methods.
     */
    public ID addShader(String vertexShader, String fragmentShader);

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code.
     * 
     * The main point of this API is 'discovery,' where you scan the asset
     * directory (or, more typically, some particular subdirectory) looking for
     * shader pairs that you didn't know about at compile time. Secondarily,
     * this can be useful if you have a lot of shader pairs, and would prefer to
     * create a folder <i>per</i> pair and use names like
     * "thisShader/vertex.vsh" and "thisShader/fragment.fsh" rather than
     * "thisShader_vertex.vsh" and "thisShader_fragment.fsh". If neither of this
     * points applies to you, you should use {@code res/raw} and the
     * {@link #addShader(int, int)} overload, as that presents no dangers of
     * {@link FileNotFoundException file not found exceptions}.
     * 
     * @param pathPrefix
     *            Optional (may be {@code null}) prefix for shader filenames. If
     *            present, will be prepended to {@code vertexShader} and
     *            {@code fragmentShader}, thus allowing you to build a tree of
     *            shaders in your assets directory, where each node contains
     *            vertex and fragment shaders with the same filename. If
     *            {@code null}, {@code vertexShader} and {@code fragmentShader}
     *            are the path names of files in the assets directory.
     * 
     *            <p>
     *            To be clear: the path names can contain '/' characters,
     *            whether or not {@code pathPrefix} is {@code null}.
     * 
     *            <pre>
     * 
     * pathPrefix = "foo"; fragmentShader = "bar/fragment.fsh"
     * // Asset-relative name is "foo/bar/fragment.fsh" 
     * 
     * pathPrefix = null; fragmentShader = "bar/fragment.fsh"
     * // Asset-relative name is "bar/fragment.fsh"
     * </pre>
     * @param vertexShader_asset
     *            Filename of a vertex shader, relative to the assets directory
     * @param fragmentShader_asset
     *            Filename of a fragment shader, relative to the assets
     *            directory
     * @return An opaque type that you can pass to {@link #getShaderMap(Object)
     *         getShaderMap(ID)}, or to the {@link GVRMaterial} and
     *         {@link GVRPostEffect} constructors and {@code setShader} methods
     *         ... or {@code null} on any exception, which typically would mean
     *         that one or both of the files does not exist or is not readable.
     */
    /*
     * Realistically, the pathPrefix is mostly here to distinguish this from the
     * addShader(String, String) form ... but the functionality is not
     * worthless.
     */
    public ID addShader(String pathPrefix, String vertexShader_asset,
            String fragmentShader_asset);

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code.
     * 
     * If you are not doing 'discovery' (scanning the asset directory for shader
     * pairs that you don't know about at compile time) this API is preferable
     * to {@link #addShader(String, String, String)}, as Android tools can
     * guarantee that the res/raw file exists.
     * 
     * @param vertexShader_resRaw
     *            R.raw id, for a file containing a vertex shader
     * @param fragmentShader_resRaw
     *            R.raw id, for a file containing a fragment shader
     * @return An opaque type that you can pass to {@link #getShaderMap(Object)
     *         getShaderMap(ID)}, or to the {@link GVRMaterial} and
     *         {@link GVRPostEffect} constructors and {@code setShader} methods.
     */
    public ID addShader(int vertexShader_resRaw, int fragmentShader_resRaw);

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code.
     * 
     * This overload is used inside of {@link #addShader(int, int)} and
     * {@link #addShader(String, String)}. It may not be incredibly useful to
     * application code, but has been exposed because there's no real reason to
     * keep it hidden.
     * 
     * @param vertexShader_stream
     *            GLSL source code for a vertex shader. Stream will be closed
     *            when method returns.
     * @param fragmentShader_stream
     *            GLSL source code for a fragment shader. Stream will be closed
     *            when method returns.
     * @return An opaque type that you can pass to {@link #getShaderMap(Object)
     *         getShaderMap(ID)}, or to the {@link GVRMaterial} and
     *         {@link GVRPostEffect} constructors and {@code setShader} methods.
     */
    public ID addShader(InputStream vertexShader_stream,
            InputStream fragmentShader_stream);

    /**
     * Get a name mapping object for the custom shader program.
     * 
     * @param id
     *            Opaque type from {@link #addShader(String, String)}
     * @return A name mapping object
     */
    public MAP getShaderMap(ID id);

}