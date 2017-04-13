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

import android.content.res.Resources;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages post-effect shaders, for modifying the texture holding the rendered
 * scene graph, before lens distortion is applied.
 * 
 * Get the singleton from {@link GVRContext#getPostEffectShaderManager()}
 *
 * This class is deprecated. The preferred method of authoring custom
 * shaders is to derive from {@link GVRShaderTemplate}.
 * In future releases there will be no difference between material
 * and post-effect shaders.
 * @deprecated
 */
public class GVRPostEffectShaderManager extends
        GVRBaseShaderManager implements GVRShaderManagers {

    private final Map<GVRShaderId, GVRPostEffectMap> posteffects = new HashMap<GVRShaderId, GVRPostEffectMap>();

    GVRPostEffectShaderManager(GVRContext gvrContext) {
        super(gvrContext, NativePostEffectShaderManager.ctor());
    }

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code.
     *
     * @param vertexShader
     *            GLSL source code for a vertex shader.
     * @param fragmentShader
     *            GLSL source code for a fragment shader.
     * @return An opaque type that you can pass to {@link #getShaderMap(GVRCustomPostEffectShaderId)},
     *         or to the {@link GVRPostEffect} constructor and {@code setShader} methods.
     */
    public GVRCustomPostEffectShaderId addShader(String vertexShader,
            String fragmentShader) {
        final int shaderId = NativePostEffectShaderManager
                .addCustomPostEffectShader(getNative(), vertexShader,
                        fragmentShader);
        GVRCustomPostEffectShaderId result = new GVRCustomPostEffectShaderId(
                shaderId);
        posteffects.put(result, retrieveShaderMap(result));
        return result;
    }

    @Override
    public GVRShaderId newShader(String vertexShader, String fragmentShader)
    {
        final int shaderId = NativePostEffectShaderManager
                .addCustomPostEffectShader(getNative(), vertexShader,
                        fragmentShader);
        GVRCustomPostEffectShaderId result = new GVRCustomPostEffectShaderId(shaderId);
        posteffects.put(result, retrieveShaderMap(result));
        return result;
    }

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code from resources in res/raw. Assumes the shaders are using GLSL ES version 100.
     *
     * @param vertexShader_resRaw
     *            R.raw id, for a file containing a vertex shader
     * @param fragmentShader_resRaw
     *            R.raw id, for a file containing a fragment shader
     * @return An opaque type that you can pass to {@link #getShaderMap(GVRCustomPostEffectShaderId)}
     *         or to the {@link GVRPostEffect} constructor and {@code setShader} methods.
     */
    public GVRCustomPostEffectShaderId addShader(int vertexShader_resRaw, int fragmentShader_resRaw) {
        return (GVRCustomPostEffectShaderId) newShader(vertexShader_resRaw, fragmentShader_resRaw);
    }

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code from resources in res/raw.
     *
     * @param vertexShader_resRaw
     *            R.raw id, for a file containing a vertex shader
     * @param fragmentShader_resRaw
     *            R.raw id, for a file containing a fragment shader
     * @param glslesVersion GLSL ES version the shaders are using
     * @return An opaque type that you can pass to {@link #getShaderMap(GVRCustomPostEffectShaderId)}
     *         or to the {@link GVRPostEffect} constructor and {@code setShader} methods.
     */
    public GVRCustomPostEffectShaderId addShader(int vertexShader_resRaw, int fragmentShader_resRaw, GLSLESVersion glslesVersion) {
        return (GVRCustomPostEffectShaderId) newShader(vertexShader_resRaw, fragmentShader_resRaw, glslesVersion);
    }

    @Override
    public GVRShaderMaps getShaderMapping(GVRShaderId id) {
        return posteffects.get(id);
    }

    /**
     * Get a name mapping object for the custom shader program.
     *
     * @param id
     *            Opaque type from {@link #newShader(String, String)}
     * @return A name mapping object
     */
    public GVRPostEffectMap getShaderMap(GVRCustomPostEffectShaderId id) {
        return posteffects.get(id);
    }

    @SuppressWarnings("resource")
    private GVRPostEffectMap retrieveShaderMap(GVRCustomPostEffectShaderId id) {
        long ptr = NativePostEffectShaderManager.getCustomPostEffectShader(
                getNative(), id.ID);
        return ptr == 0 ? null : new GVRPostEffectMap(getGVRContext(), ptr);
    }

}

class NativePostEffectShaderManager {
    static native long ctor();

    static native long delete(long postEffectShaderManager);

    static native int addCustomPostEffectShader(long postEffectShaderManager,
            String vertexShader, String fragmentShader);

    static native long getCustomPostEffectShader(long postEffectShaderManager,
            int id);
}
