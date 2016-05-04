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

import java.util.HashMap;
import java.util.Map;

/**
 * Manages post-effect shaders, for modifying the texture holding the rendered
 * scene graph, before lens distortion is applied.
 * 
 * Get the singleton from {@link GVRContext#getPostEffectShaderManager()}
 */
public class GVRPostEffectShaderManager extends
        GVRBaseShaderManager<GVRPostEffectMap, GVRCustomPostEffectShaderId>
        implements
        GVRShaderManagers<GVRPostEffectMap, GVRCustomPostEffectShaderId> {

    private final Map<GVRCustomPostEffectShaderId, GVRPostEffectMap> posteffects = new HashMap<GVRCustomPostEffectShaderId, GVRPostEffectMap>();

    GVRPostEffectShaderManager(GVRContext gvrContext) {
        super(gvrContext, NativePostEffectShaderManager.ctor());
    }

    @Override
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
