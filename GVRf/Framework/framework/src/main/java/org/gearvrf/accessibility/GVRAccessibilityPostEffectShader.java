/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package org.gearvrf.accessibility;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCustomPostEffectShaderId;
import org.gearvrf.GVRPostEffectMap;
import org.gearvrf.GVRPostEffectShaderManager;

/**
 * Shader to invert colors by post processing rendered image from cameras.
 */
final class GVRAccessibilityPostEffectShader {

    private final GVRCustomPostEffectShaderId mShaderId;
    private GVRPostEffectMap mCustomShader;

    /**
     * load vertex and fragment shaders from external files.
     * 
     * @param gvrContext
     */
    public GVRAccessibilityPostEffectShader(GVRContext gvrContext) {
        final GVRPostEffectShaderManager shaderManager = gvrContext
                .getPostEffectShaderManager();
        mShaderId = shaderManager.addShader(org.gearvrf.R.raw.inverted_colors_vertex,
                org.gearvrf.R.raw.inverted_colors_fragment);
        mCustomShader = shaderManager.getShaderMap(mShaderId);

    }

    /**
     * Return shader id. It is needed to apply the shader as post effect to a camera
     * 
     * @return
     */
    public GVRCustomPostEffectShaderId getShaderId() {
        return mShaderId;
    }
}
