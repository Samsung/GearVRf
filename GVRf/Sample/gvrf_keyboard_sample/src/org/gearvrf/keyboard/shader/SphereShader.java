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

public class SphereShader {

    public static final String LIGHT_KEY = "light";
    public static final String EYE_KEY = "eye";
    public static final String TRANSITION_COLOR = "trans_color";
    public static final String TEXTURE_KEY = "texture";
    public static final String SECUNDARY_TEXTURE_KEY = "second_texture";
    public static final String ANIM_TEXTURE = "animTexture";
    public static final String BLUR_INTENSITY = "blur";

    // TODO: Light treatment
    private static final String VERTEX_SHADER = "" //
            + "attribute vec4 a_position;\n"
            + "attribute vec3 a_normal;\n" //
            + "attribute vec2 a_tex_coord;\n"
            + "uniform mat4 u_mvp;\n" //
            + "uniform vec3 u_eye;\n"
            + "uniform vec3 u_light;\n" //
            + "varying vec3 normal;\n"
            + "varying vec3 view;\n" //
            + "varying vec3 " + LIGHT_KEY + ";\n"
            + "varying vec2 coord;\n" //
            + "void main() {\n"
            // + "  normal = a_normal;\n" //
            // + "  view  = " + EYE_KEY + " - a_position.xyz;\n"
            // + "  u_light = " + LIGHT_KEY + " - a_position.xyz;\n"
            + "  coord = a_tex_coord;\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision mediump float;\n"
            + "varying vec2  coord;\n"
            + "varying vec3  normal;\n" //
            + "varying vec3  view;\n"
            + "varying vec3  "
            + LIGHT_KEY
            + ";\n" //
            + "uniform sampler2D texture;\n"
            + "uniform sampler2D " + SECUNDARY_TEXTURE_KEY
            + ";\n"
            + "uniform vec3 "
            + TRANSITION_COLOR
            + " ;\n"
            + "vec2  animOffset;\n"
            + "uniform float "
            + ANIM_TEXTURE
            + ";\n"
            + "uniform float "
            + BLUR_INTENSITY
            + ";\n"
            + "void main() {\n" //
            // + "  vec3  v = normalize(view);\n"
            // + "  vec3  l = normalize(" + LIGHT_KEY + ");\n"
            // + "  vec3  n = normalize(normal);\n"
            + "  float division = 7.0;\n"
            + "  animOffset = vec2(" + ANIM_TEXTURE + ",0.0) ;\n"
            + "  vec4 color = texture2D(texture, coord) / division ;\n"
            + "  vec3 color2 = texture2D(texture, coord ).rgb ;\n"
            + "  vec4 color3 = texture2D(" + SECUNDARY_TEXTURE_KEY
            + ", coord + animOffset - vec2(1,0.0)) ;\n"
            + "  color += texture2D(texture, (coord * (0.9)) + vec2(0.05,0.05)) / division ;\n"
            + "  color += texture2D(texture, (coord * (0.85)) + vec2(0.075,0.075)) / division ;\n"
            + "  color += texture2D(texture, (coord * (0.8)) + vec2(0.1,0.1)) / division ;\n"
            + "  color += texture2D(texture, (coord * (0.75)) + vec2(0.125,0.125)) / division ;\n"
            + "  color += texture2D(texture, (coord * (0.7)) + vec2(0.15,0.15)) / division ;\n"
            + "  color += texture2D(texture, (coord * (0.65)) + vec2(0.175,0.175)) / division ;\n"
            + "  vec3 finalColor = (color.rgb * " + BLUR_INTENSITY + ") + (color2 * (1.0-"
            + BLUR_INTENSITY + ")); \n"
            + "  if(color3.w == 0.0){ \n"
            // + "  finalColor = max(finalColor, vec3(1.0 - " + ANIM_OFFSET +
            // ",1.0,1.0 - " + ANIM_OFFSET + ")) + color3.rgb; }\n"
            + "  finalColor = (  " + TRANSITION_COLOR + " * " + ANIM_TEXTURE
            + " + (finalColor * (1.0 -" + ANIM_TEXTURE + "))) + color3.rgb; }\n"
            + "  else{ \n"
            + "  finalColor = color3.rgb; }\n"
            // + "  vec3  h = normalize(v+l);\n"
            // + "  float diffuse  = max ( dot(l,n), 0.1 );\n"
            // + "  float specular = max ( dot(h,n), 0.0 );\n"
            // + "  specular = pow (specular, 300.0);\n" //
            // + "  color *= diffuse;\n" //
            // + "  color += 0.5*(1.0- color)*specular;\n"
            + "  gl_FragColor = vec4( finalColor * 1.0 - " + BLUR_INTENSITY + "/2.0, 1.0 );\n" //
            + "  gl_FragColor.a = 1.0 - min(0.9," + BLUR_INTENSITY + ");\n" //
            + "}\n";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public SphereShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformVec3Key(LIGHT_KEY, LIGHT_KEY);
        mCustomShader.addUniformVec3Key(TRANSITION_COLOR, TRANSITION_COLOR);
        mCustomShader.addUniformVec3Key(EYE_KEY, EYE_KEY);
        mCustomShader.addTextureKey("texture", TEXTURE_KEY);
        mCustomShader.addTextureKey(SECUNDARY_TEXTURE_KEY, SECUNDARY_TEXTURE_KEY);
        mCustomShader.addUniformFloatKey(ANIM_TEXTURE, ANIM_TEXTURE);
        mCustomShader.addUniformFloatKey(BLUR_INTENSITY, BLUR_INTENSITY);

    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
