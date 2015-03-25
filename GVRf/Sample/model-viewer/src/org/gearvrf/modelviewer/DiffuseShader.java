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


package org.gearvrf.modelviewer;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRCustomMaterialShaderId;

public class DiffuseShader {

    public static final String COLOR_KEY = "color";
    public static final String LIGHT_KEY = "light";
    public static final String EYE_KEY = "eye";
    public static final String TEXTURE_KEY = "texture";

    public static final String MAT1_KEY = "u_mat1";
    public static final String MAT2_KEY = "u_mat2";
    public static final String MAT3_KEY = "u_mat3";
    public static final String MAT4_KEY = "u_mat4";

    private static final String VERTEX_SHADER = "" //
            + "attribute vec4 a_position;\n"
            + "attribute vec3 a_normal;\n" //
            + "attribute vec2 a_tex_coord;\n"
            + "uniform mat4 u_mvp;\n" //
            + "uniform vec3 u_eye;\n"
            + "uniform vec3 u_light;\n" //
            + "varying vec3 normal;\n"
            + "varying vec3 view;\n" //
            + "varying vec3 light;\n"
            + "varying vec2 coord;\n" //
            + "void main() {\n"
            + "  normal = a_normal;\n" //
            + "  view  = u_eye - a_position.xyz;\n"
            + "  light = u_light - a_position.xyz;\n"
            + "  coord = a_tex_coord;\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision mediump float;\n"
            + "uniform vec4  u_color;\n" //
            + "varying vec2  coord;\n"
            + "varying vec3  normal;\n" //
            + "varying vec3  view;\n"
            + "varying vec3  light;\n" //
            + "uniform sampler2D texture;\n"
            + "void main() {\n" //
            + "  vec3  v = normalize(view);\n"
            + "  vec3  l = normalize(light);\n"
            + "  vec3  n = normalize(normal);\n"
            + "  vec3 color = texture2D(texture, coord).rgb;\n"
            + "  vec3  h = normalize(v+l);\n"
            + "  float diffuse  = max ( dot(l,n), 0.1 );\n"
            + "  float specular = max ( dot(h,n), 0.0 );\n"
            + "  specular = pow (specular, 300.0);\n" //
            + "  color *= diffuse;\n" //
            + "  color *= u_color.rgb;\n"
            + "  color += 0.5*(1.0- color)*specular;\n"
            + "  gl_FragColor = vec4( color, 1.0 );\n" //
            + "}\n";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public DiffuseShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformVec4Key("u_color", COLOR_KEY);
        mCustomShader.addUniformVec3Key("u_light", LIGHT_KEY);
        mCustomShader.addUniformVec3Key("u_eye", EYE_KEY);
        mCustomShader.addTextureKey("texture", TEXTURE_KEY);

    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
