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

public class ScreenShader {

    public static final String SCREEN_KEY = "screen";

    private static final String VERTEX_SHADER = "" //
            + "attribute vec4 a_position;\n"
            + "attribute vec4 a_tex_coord;\n" //
            + "uniform mat4 u_mvp;\n"
            + "varying vec2 v_tex_coord;\n" //
            + "void main() {\n" //
            + "  v_tex_coord = a_tex_coord.xy;\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES u_screen;\n"
            + "uniform int u_right_eye;\n"
            + "varying vec2 v_tex_coord;\n"
            + "void main() {\n"
            + "  float offset = 0.0; if(u_right_eye==1) offset = 0.5;\n"
            + "  vec2 coord = vec2( offset + 0.5*v_tex_coord.x, v_tex_coord.y );\n"
            + "  gl_FragColor = texture2D(u_screen, coord);\n" //
            + "}\n";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public ScreenShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addTextureKey("u_screen", SCREEN_KEY);
    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
