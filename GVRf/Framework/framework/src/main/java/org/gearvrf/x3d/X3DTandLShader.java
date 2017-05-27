/* Copyright 2016 Samsung Electronics Co., LTD
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


package org.gearvrf.x3d;

/* shader to test x3d functionality */

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;

/**
 * 
 * @author m1.williams
 * implements shader for X3D developers who will eventually
 * set their own values, which X3D allows.
 */
public class X3DTandLShader extends GVRShader
{

    public final String TEXTURECENTER_KEY = "u_texturecenter";
    public final String TEXTUREROTATION_KEY = "u_texturerotation";
    public final String TEXTURESCALE_KEY = "u_texturescale";
    public final String TEXTURETRANSLATION_KEY = "u_texturetranslation";

    public final String MODELMATRIX_KEY = "modelmatrix";

    public final String DIFFUSECOLOR_KEY = "u_diffusecolor";
    public final String EMISSIVECOLOR_KEY = "u_emissivecolor";
    public final String SPECULARCOLOR_KEY = "u_specularcolor";
    public final String SHININESS_KEY = "u_shininess";
    
    public static final String LIGHT_KEY = "light";
    public static final String EYE_KEY = "eye";
    public final String TEXTURE_KEY = "u_texture";
    public static final String CENTER_KEY = "u_center";
    public static final String ROTATION_KEY = "u_rotation";
    public static final String SCALE_KEY = "u_scale";
    public static final String TRANSLATION_KEY = "u_translation";

    public static final String MAT1_KEY = "u_mat1";
    public static final String MAT2_KEY = "u_mat2";
    public static final String MAT3_KEY = "u_mat3";
    public static final String MAT4_KEY = "u_mat4";

    private final String VERTEX_SHADER = "" //
            + "attribute vec4 a_position;\n"
            + "attribute vec3 a_normal;\n" //
            + "attribute vec2 a_texcoord;\n"
            + "uniform mat4 u_mvp;\n" //
            + "uniform mat4 u_modelmatrix;\n" //
            + "uniform vec2 u_texturecenter;\n" //
            + "uniform float u_texturerotation;\n" //
            + "uniform vec2 u_texturescale;\n" //
            + "uniform vec2 u_texturetranslation;\n" //
            + "varying vec3 v_normal;\n"
            + "varying vec3 v_position;\n"
            + "varying vec2 texturecoord;\n" //
            
            
            + "void main() {\n"
            // texture transform
            + "  mat3 texturenegcentermatrix = mat3(1.0, 0.0, 0.0,   0.0, 1.0, 0.0,   -u_texturecenter.s, -u_texturecenter.t, 1.0);\n" //
            + "  mat3 texturescalematrix = mat3(u_texturescale.s, 0.0, 0.0,   0.0, u_texturescale.t, 0.0,   0, 0, 1.0);\n" //
            + "  mat3 texturerotationmatrix = mat3(cos(u_texturerotation), sin(u_texturerotation), 0.0,  -sin(u_texturerotation), cos(u_texturerotation), 0.0,  0.0, 0.0, 1.0);\n" //
            + "  mat3 texturecentermatrix = mat3(1.0, 0.0, 0.0,   0.0, 1.0, 0.0,   u_texturecenter.s, u_texturecenter.t, 1.0);\n" //
            + "  mat3 texturetranslationmatrix = mat3(1.0, 0.0, 0.0,   0.0, 1.0, 0.0,   u_texturetranslation.s, u_texturetranslation.t, 1.0);\n" //
            + "  mat3 texturematrix = mat3(1.0, 0.0, 0.0,  0.0, 1.0, 0.0,  0.0, 0.0, 1.0);\n" // 
            + " texturematrix = texturenegcentermatrix * texturescalematrix * texturerotationmatrix * texturecentermatrix * texturetranslationmatrix;\n" //
            + " texturecoord = vec2((texturematrix * vec3(a_texcoord, 1.0)).xy);\n" //
             
     		+ "  v_normal = (u_modelmatrix * vec4(a_normal, 0.0)).xyz;\n"
            + "  v_position = vec3((u_modelmatrix * a_position).xyz);\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private final String FRAGMENT_SHADER_Avars = "" //
            + "precision mediump float;\n"
            + "uniform vec3  u_diffusecolor;\n" //
            + "uniform vec3  u_emissivecolor;\n" //
            + "uniform vec3  u_specularcolor;\n" //
            + "uniform float  u_shininess;\n" //
            + "varying vec2  texturecoord;\n"
            + "varying vec3  v_normal;\n" //
            + "varying vec3  v_position;\n" //
            + "uniform sampler2D texture;\n";

    private final String FRAGMENT_SHADER_BspecularlightContribution = "" //
            + "float specularlightCalculation(vec3 pixelNormal, vec3 vectorLightToPixel) {\n"
            + "  vec3 eyeDirection = normalize(v_position);\n"
            + "  vec3 halfwayVector = normalize(eyeDirection + vectorLightToPixel);\n"
            + "  float specularValue = max(dot(-halfwayVector, pixelNormal), 0.0);\n"
            + "  float specularlightContribution = pow(specularValue, u_shininess*128.0);\n"
            + "  return specularlightContribution;\n"         
            + "}\n";         

    private final String FRAGMENT_SHADER_Cdirectionallight = "" //
            + "vec3 directionallight(vec3 pixelColor, vec3 pixelNormal, vec3 lightDirection, float intensity, vec3 lightColor) {\n"
            + "  vec3 vectorLightDirection = normalize(lightDirection);\n"
            + "  pixelColor *= max(dot(-vectorLightDirection, pixelNormal), 0.0) * intensity * lightColor;\n"
            + "  pixelColor += specularlightCalculation(pixelNormal, vectorLightDirection) * u_specularcolor;\n"
            + "  pixelColor = min(pixelColor, 1.0);\n"
            + "  return pixelColor;\n"         
            + "}\n";         

    private final String FRAGMENT_SHADER_Cpointlight = "" //
            + "vec3 pointlight(vec3 v_position, vec3 pixelColor, vec3 pixelNormal, vec3 lightPos, vec3 attenuation, float lightRadius, float intensity, vec3 lightColor) {\n"
            + "  vec3 distanceLightToPixel = vec3(v_position - lightPos);\n"
            + "  float lightContribution = 0.0;\n"
            + "  float lengthLightToPixel = length(distanceLightToPixel);\n"
            + "  vec3 vectorLightToPixel = vec3(0.0, 0.0, 0.0);\n"            
            + "  vec3 endColor = vec3(0.0, 0.0, 0.0);\n"     
            + "  if ( lengthLightToPixel < lightRadius) {\n"
            + "     vectorLightToPixel = normalize(distanceLightToPixel);\n"
            + "     float attenuationValue = 1.0/max((attenuation[0] + attenuation[1]*lengthLightToPixel + attenuation[2]*lengthLightToPixel*lengthLightToPixel), 1.0);\n"         
            + "  	lightContribution = max(dot(-vectorLightToPixel, pixelNormal) * attenuationValue, 0.0);\n"
            + "     endColor = pixelColor * lightContribution * intensity * lightColor;\n"
            + "     endColor += specularlightCalculation(pixelNormal, vectorLightToPixel) * u_specularcolor;\n"
            + "     endColor = min(endColor, 1.0);\n"
             + "  }\n"
            + "  return endColor;\n"         
            + "}\n";         

    private final String FRAGMENT_SHADER_Cspotlight = "" //
            + "vec3 spotlight(vec3 v_position, vec3 pixelColor, vec3 pixelNormal, vec3 lightPos, vec3 lightDirection, vec3 attenuation, float lightRadius, float intensity, float beamWidth, float cutOffAngle, vec3 lightColor) {\n"
            + "  vec3 distanceLightToPixel = vec3(v_position - lightPos);\n"
            + "  float lightContribution = 0.0;\n"
            + "  float lengthLightToPixel = length(distanceLightToPixel);\n"
            + "  vec3 vectorLightToPixel = vec3(0.0, 0.0, 0.0);\n"     
            + "  vec3 endColor = vec3(0.0, 0.0, 0.0);\n"     
            + "  if ( lengthLightToPixel < lightRadius) {\n"
            + "     vectorLightToPixel = normalize(distanceLightToPixel);\n"
            + "  	float angleLightToNormal = max(dot(-vectorLightToPixel, pixelNormal), 0.0);\n"
            + "     float angleLightDirectionToPixel  = dot( vectorLightToPixel, normalize(lightDirection) );\n"
            + "     float beamWidthCos = cos(beamWidth);\n"
            + "     float cutOffAngleCos = cos(cutOffAngle);\n"
            + "     float attenuationValue = 1.0/max((attenuation[0] + attenuation[1]*lengthLightToPixel + attenuation[2]*lengthLightToPixel*lengthLightToPixel), 1.0);\n"         
            	    //note, these are the cosines of the angles, not actual angles
            + "  	if ( angleLightDirectionToPixel >= beamWidthCos ) {\n"
            + "  	   lightContribution = angleLightToNormal * attenuationValue;\n"
            + "        endColor = pixelColor * lightContribution * intensity * lightColor;\n"
            + "        endColor += specularlightCalculation(pixelNormal, vectorLightToPixel) * u_specularcolor;\n"
            + "  	}\n"
            + "  	else if ( (angleLightDirectionToPixel >= cutOffAngleCos) && (beamWidthCos > cutOffAngleCos) ) {\n"
            + "  	   lightContribution = angleLightToNormal * (angleLightDirectionToPixel - cutOffAngleCos) / (beamWidthCos - cutOffAngleCos) * attenuationValue;\n"
            + "        endColor = pixelColor * lightContribution * intensity * lightColor;\n"
            + "        endColor += specularlightCalculation(pixelNormal, vectorLightToPixel) * u_specularcolor;\n"
            + "  	}\n"
            + "    endColor = min(endColor, 1.0);\n"
            + "  }\n"
            + "  return endColor;\n"         
            + "}\n";         

    private final String FRAGMENT_SHADER_DmainBgn = "" //
            + "void main() {\n" //
            + "  vec3  lightdir = normalize(v_position);\n"
            + "  vec3  pixelnormal = normalize(v_normal);\n"
            + "  vec3 pixelcolor = texture2D(texture, texturecoord).rgb * u_diffusecolor;\n"
            + "  vec3 lightContribution = vec3(0.0, 0.0, 0.0);\n"
            + "  bool lightsPresent = false;\n"
    		+ "  vec3 fragmentcolor = vec3(0.0, 0.0, 0.0);\n";

    private final String FRAGMENT_SHADER_DmainEnd = "" //
            + "  if (!lightsPresent) fragmentcolor = texture2D(texture, texturecoord).rgb;\n" //
            + "  else fragmentcolor = min(fragmentcolor + u_emissivecolor, 1.0);\n" //
            + "  gl_FragColor = vec4( fragmentcolor, 1.0 );\n" //
            + "}\n";

    public String FRAGMENT_SHADER_Lights = ""; //

    public X3DTandLShader(GVRContext gvrContext) {
        super("float2 u_texturecenter, float u_texturerotation, float2 u_texturescale, float2 u_texturetranslation "
              + "float3 u_diffusecolor, float3 u_emissivecolor, float3 u_specularcolor, float  u_shininess",
              "sampler2D u_texture",
              "float3 a_position float2 a_texcoord, float3 a_normal", 300);
    }

    public void appendFragmentShaderLights(String lightString) {
    	FRAGMENT_SHADER_Lights += lightString;
    }

    public void setCustomShader() {
 
        String FRAGMENT_SHADER_Lights_Present = "lightsPresent = true;\n"; //
        
    	if (FRAGMENT_SHADER_Lights.length() == 0) {
    		FRAGMENT_SHADER_Lights_Present = "lightsPresent = false;\n";
    	}

        String FRAGMENT_SHADER = "" //
        		+ FRAGMENT_SHADER_Avars
        		+ FRAGMENT_SHADER_BspecularlightContribution
        		+ FRAGMENT_SHADER_Cdirectionallight
        		+ FRAGMENT_SHADER_Cpointlight
        		+ FRAGMENT_SHADER_Cspotlight
        		+ FRAGMENT_SHADER_DmainBgn
        		+ FRAGMENT_SHADER_Lights_Present
        		+ FRAGMENT_SHADER_Lights
                + FRAGMENT_SHADER_DmainEnd;
        
        /*  if we want to print out the shader
        Log.e(" -- Vtx", VERTEX_SHADER);
        Log.e(" -- Frag", FRAGMENT_SHADER_Avars);
        Log.e(" -- Frag", FRAGMENT_SHADER_BspecularlightContribution);
        Log.e(" -- Frag", FRAGMENT_SHADER_Cdirectionallight);
        Log.e(" -- Frag", FRAGMENT_SHADER_Cpointlight);
        Log.e(" -- Frag", FRAGMENT_SHADER_Cspotlight);
        Log.e(" -- Frag", FRAGMENT_SHADER_DmainBgn);
        Log.e(" -- Frag", FRAGMENT_SHADER_Lights);
        Log.e(" -- Frag", FRAGMENT_SHADER_DmainEnd);
        */

    }
}
