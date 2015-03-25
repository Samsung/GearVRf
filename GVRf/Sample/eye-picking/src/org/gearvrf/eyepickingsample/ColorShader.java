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


package org.gearvrf.eyepickingsample;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRCustomMaterialShaderId;

public class ColorShader {

    public static final String COLOR_KEY = "color";

    private static final String VERTEX_SHADER = "attribute vec4 a_position;\n"
            + "uniform mat4 u_mvp;\n" //
            + "void main() {\n" //
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "uniform vec4 u_color;\n" //
            + "void main() {\n" //
            + "  gl_FragColor = u_color;\n" //
            + "}\n";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public ColorShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformVec4Key("u_color", COLOR_KEY);
    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
