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

public class TransparentButtonShaderTwoStates  extends TransparentButtonShaderBase{

    public static final String TEXTURE_KEY = "texture";
    public static final String TEXTURE_HOVER_KEY = "textureHover";
    
    public static final String TEXTURE_TEXT_KEY = "textTexture";
    public static final String TEXTURE_TEXT_HOVER_KEY = "textHoverTexture";

    public static final String TEXTURE_TEXT_SPECIAL_KEY = "textSpecialTexture";
    public static final String TEXTURE_TEXT_HOVER_SPECIAL_KEY = "textHoverSpecialTexture";
    
    public static final String TEXTURE_SWITCH = "textureSwitch";
    public static final String OPACITY = "opacity";

    private static final String VERTEX_SHADER = "" //
            + "attribute vec4 a_position;\n"
            + "attribute vec3 a_normal;\n" //
            + "attribute vec2 a_tex_coord;\n"
            + "uniform mat4 u_mvp;\n" //
            + "varying vec3 normal;\n"
            + "varying vec2 coord;\n" //
            + "void main() {\n"
            + "  coord = a_tex_coord;\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision mediump float;\n"
            + "varying vec2  coord;\n"
            + "uniform sampler2D "+ TEXTURE_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_HOVER_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_HOVER_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_SPECIAL_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_HOVER_SPECIAL_KEY + ";\n"
            + "uniform float opacity;\n"
            + "uniform float " + TEXTURE_SWITCH + ";\n"
            + "void main() {\n" // 
            + "  vec4 color = texture2D(texture, coord);\n"
            + "  vec4 text = vec4(0.0, 0.0, 0.0, 1.0);\n"
            + " if(" + TEXTURE_SWITCH + " == 0.0){"
            + "  text = texture2D("+ TEXTURE_TEXT_KEY + ", coord);\n"
            + "  vec4 color = texture2D("+ TEXTURE_KEY + ", coord);\n"
            + " }"
            + " if(" + TEXTURE_SWITCH + " == 1.0){"
            + "  text = texture2D("+ TEXTURE_TEXT_HOVER_KEY + ", coord);\n"
            + "  color = texture2D("+ TEXTURE_HOVER_KEY + ", coord);\n"
            + " }"
            + " if(" + TEXTURE_SWITCH + " == 4.0){"
            + "  text = texture2D("+ TEXTURE_TEXT_SPECIAL_KEY + ", coord);\n"
            + "  color = texture2D("+ TEXTURE_KEY + ", coord);\n"
            + " }"
            + " if(" + TEXTURE_SWITCH + " == 5.0){"
            + "  text = texture2D("+ TEXTURE_TEXT_HOVER_SPECIAL_KEY + ", coord);\n"
            + "  color = texture2D("+ TEXTURE_HOVER_KEY + ", coord);\n"
            + " }"
            + "  color = color + text;\n"
            + "  color = color * opacity;\n"
            + "  gl_FragColor = vec4(color);\n" //
            + "}\n";

   // private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public TransparentButtonShaderTwoStates(GVRContext gvrContext) {
        
        final GVRMaterialShaderManager shaderManager = gvrContext.getMaterialShaderManager();
        
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addTextureKey("texture", TEXTURE_KEY);
        
        mCustomShader.addTextureKey("textureHover", TEXTURE_HOVER_KEY);
        mCustomShader.addTextureKey("textHoverTexture", TEXTURE_TEXT_HOVER_KEY);
        
        mCustomShader.addTextureKey(TEXTURE_TEXT_SPECIAL_KEY, TEXTURE_TEXT_SPECIAL_KEY);
        mCustomShader.addTextureKey(TEXTURE_TEXT_HOVER_SPECIAL_KEY, TEXTURE_TEXT_HOVER_SPECIAL_KEY);
        
        mCustomShader.addTextureKey("textTexture", TEXTURE_TEXT_KEY);
        mCustomShader.addUniformFloatKey("opacity", OPACITY);
        mCustomShader.addUniformFloatKey("textureSwitch", TEXTURE_SWITCH);
    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}