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

package org.gearvrf.keyboard.shader;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCustomMaterialShaderId;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.keyboard.R;

public class SphereShader {

    public static final String LIGHT_KEY = "u_light";
    public static final String EYE_KEY = "u_eye";
    public static final String TRANSITION_COLOR = "trans_color";
    public static final String TEXTURE_KEY = "texture";
    public static final String SECUNDARY_TEXTURE_KEY = "second_texture";
    public static final String ANIM_TEXTURE = "animTexture";
    public static final String BLUR_INTENSITY = "blur";
    public static final String HDRI_TEXTURE_KEY = "hdri_texture";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public SphereShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(R.raw.sphereshader_vertex, R.raw.sphereshader_fragment);

        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformVec3Key(LIGHT_KEY, LIGHT_KEY);
        mCustomShader.addUniformVec3Key(TRANSITION_COLOR, TRANSITION_COLOR);
        mCustomShader.addUniformVec3Key(EYE_KEY, EYE_KEY);
        mCustomShader.addTextureKey("texture", TEXTURE_KEY);
        mCustomShader.addTextureKey(SECUNDARY_TEXTURE_KEY, SECUNDARY_TEXTURE_KEY);
        mCustomShader.addUniformFloatKey(ANIM_TEXTURE, ANIM_TEXTURE);
        mCustomShader.addUniformFloatKey(BLUR_INTENSITY, BLUR_INTENSITY);
        mCustomShader.addTextureKey("hdri_texture", HDRI_TEXTURE_KEY);

    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
