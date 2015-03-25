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
 * Manages post-effect shaders, for modifying the texture holding the rendered
 * scene graph, before lens distortion is applied.
 * 
 * Get the singleton from {@link GVRContext#getPostEffectShaderManager()}
 */
public class GVRPostEffectShaderManager extends
        GVRBaseShaderManager<GVRPostEffectMap, GVRCustomPostEffectShaderId>
        implements
        GVRShaderManagers<GVRPostEffectMap, GVRCustomPostEffectShaderId> {

    GVRPostEffectShaderManager(GVRContext gvrContext) {
        super(gvrContext, NativePostEffectShaderManager.ctor());
    }

    @Override
    public GVRCustomPostEffectShaderId addShader(String vertexShader,
            String fragmentShader) {
        final int shaderId = NativePostEffectShaderManager
                .addCustomPostEffectShader(getPtr(), vertexShader,
                        fragmentShader);
        return new GVRCustomPostEffectShaderId(shaderId);
    }

    @Override
    public GVRPostEffectMap getShaderMap(GVRCustomPostEffectShaderId id) {
        long ptr = NativePostEffectShaderManager.getCustomPostEffectShader(
                getPtr(), id.ID);
        return ptr == 0 ? null : new GVRPostEffectMap(getGVRContext(), ptr);
    }

}

class NativePostEffectShaderManager {
    public static native long ctor();

    public static native int addCustomPostEffectShader(
            long postEffectShaderManager, String vertexShader,
            String fragmentShader);

    public static native long getCustomPostEffectShader(
            long postEffectShaderManager, int id);
}
