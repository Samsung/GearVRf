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


package org.gearvrf.video;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRCustomMaterialShaderId;

public class AdditiveShader {

    public static final String TEXTURE_KEY = "texture";
    public static final String WEIGHT_KEY = "weight";
    public static final String FADE_KEY = "fade";

    private static final String VERTEX_SHADER = "" //
            + "precision highp float;\n"
            + "attribute vec4 a_position;\n" //
            + "attribute vec2 a_tex_coord;\n"
            + "uniform mat4 u_mvp;\n" //
            + "varying vec2 coord;\n"
            + "void main() {\n" //
            + "  coord = a_tex_coord;\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision highp float;\n"
            + "varying vec2  coord;\n" //
            + "uniform sampler2D texture;\n"
            + "uniform float u_weight;\n" //
            + "uniform float u_fade;\n"
            + "void main() {\n"
            + "  vec3 color1 = texture2D(texture, coord).rgb;\n"
            + "  vec3 color2 = vec3(0.0);\n"
            + "  vec3 color  = color1*(1.0-u_weight)+color2*u_weight;\n"
            + "  float alpha = length(color);\n"
            + "  gl_FragColor = vec4( u_fade*color, alpha );\n" //
            + "}\n";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public AdditiveShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addTextureKey("texture", TEXTURE_KEY);
        mCustomShader.addUniformFloatKey("u_weight", WEIGHT_KEY);
        mCustomShader.addUniformFloatKey("u_fade", FADE_KEY);
    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
