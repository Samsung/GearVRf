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

package org.gearvrf.immersivepedia.shader;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCustomMaterialShaderId;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.immersivepedia.R;

public class MenuImageShader {

    public static final String STATE1_TEXTURE = "state1";
    public static final String STATE2_TEXTURE = "state2";
    public static final String TEXTURE_SWITCH = "textureSwitch";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public MenuImageShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(R.raw.menu_image_shader_vertex,
                R.raw.menu_image_shader_fragment);

        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addTextureKey(STATE1_TEXTURE, STATE1_TEXTURE);
        mCustomShader.addTextureKey(STATE2_TEXTURE, STATE2_TEXTURE);
        mCustomShader.addUniformFloatKey(TEXTURE_SWITCH, TEXTURE_SWITCH);
        mCustomShader.addUniformFloatKey("opacity", "opacity");

    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
