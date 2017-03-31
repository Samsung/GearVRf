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
import java.lang.reflect.*;

import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.GVRContext;
import org.gearvrf.utility.Log;

/**
 * Manages custom material shaders, for rendering scene objects.
 *
 * Get the singleton from {@link GVRContext#getMaterialShaderManager()}.
 *
 * This class will be deprecated in future releases and
 * there will be no difference between material
 * and post-effect shaders.
 * Now the preferred method of authoring custom
 * shaders is to derive from {@link GVRShaderTemplate}.
 */
    public class GVRMaterialShaderManager extends
        GVRBaseShaderManager implements GVRShaderManagers {

    private final Map<GVRShaderId, GVRMaterialMap> materialMaps = new HashMap<GVRShaderId, GVRMaterialMap>();

    GVRMaterialShaderManager(GVRContext gvrContext) {
        super(gvrContext, NativeShaderManager.ctor());
    }

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code.
     *
     * @param vertexShader
     *            GLSL source code for a vertex shader.
     * @param fragmentShader
     *            GLSL source code for a fragment shader.
     * @return An opaque type that you can pass to {@link #getShaderMap(GVRCustomMaterialShaderId)},
     *         or to the {@link GVRMaterial} constructor and {@code setShader} methods.
     */
    public GVRCustomMaterialShaderId addShader(String vertexShader, String fragmentShader)
    {
        synchronized (materialMaps)
        {
            final int shaderId = NativeShaderManager.addCustomShader(getNative(),
                    vertexShader, fragmentShader);
            GVRCustomMaterialShaderId result = new GVRCustomMaterialShaderId(shaderId);
            materialMaps.put(result, retrieveShaderMap(result));
            return result;
        }
    }

    @Override
    public GVRShaderId newShader(String vertexShader, String fragmentShader)
    {
        synchronized (materialMaps)
        {
            final int shaderId = NativeShaderManager.addCustomShader(getNative(),
                    vertexShader, fragmentShader);
            GVRCustomMaterialShaderId result = new GVRCustomMaterialShaderId(shaderId);
            GVRMaterialMap mmap = retrieveShaderMap(result);
            materialMaps.put(result, mmap);
            return result;
        }
    }

    /**
     * Builds a shader program from the supplied vertex and fragment shader
     * code from resources in res/raw.
     *
     * @param vertexShader_resRaw
     *            R.raw id, for a file containing a vertex shader
     * @param fragmentShader_resRaw
     *            R.raw id, for a file containing a fragment shader
     * @return An opaque type that you can pass to {@link #getShaderMap(GVRCustomMaterialShaderId)}
     *         or to the {@link GVRMaterial} constructor and {@code setShader} methods.
     */
    public GVRCustomMaterialShaderId addShader(int vertexShader_resRaw, int fragmentShader_resRaw) {
        return (GVRCustomMaterialShaderId) newShader(vertexShader_resRaw, fragmentShader_resRaw);
    }

    @Override
    public GVRShaderMaps getShaderMapping(GVRShaderId id) {
        synchronized (materialMaps)
        {
            return materialMaps.get(id);
        }
    }

    /**
     * Get a name mapping object for the custom shader program.
     *
     * @param id
     *            Opaque type from {@link #newShader(String, String)}
     * @return A name mapping object
     */
    public GVRMaterialMap getShaderMap(GVRCustomMaterialShaderId id) {
        synchronized (materialMaps)
        {
            return materialMaps.get(id);
        }
    }

    @SuppressWarnings("resource")
    private GVRMaterialMap retrieveShaderMap(GVRShaderId id) {
        long ptr = NativeShaderManager.getCustomShader(getNative(), id.ID);
        return ptr == 0 ? null : new GVRMaterialMap(getGVRContext(), ptr);
    }
}

class NativeShaderManager {
    static native long ctor();

    static native int addCustomShader(long shaderManager, String vertexShader,
            String fragmentShader);

    static native long getCustomShader(long shaderManager, int id);
}
