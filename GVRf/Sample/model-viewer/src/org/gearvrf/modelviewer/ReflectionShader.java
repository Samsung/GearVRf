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

public class ReflectionShader {

    public static final String COLOR_KEY = "color";
    public static final String LIGHT_KEY = "light";
    public static final String EYE_KEY = "eye";
    public static final String RADIUS_KEY = "radius";
    public static final String TEXTURE_KEY = "texture";

    public static final String MAT1_KEY = "u_mat1";
    public static final String MAT2_KEY = "u_mat2";
    public static final String MAT3_KEY = "u_mat3";
    public static final String MAT4_KEY = "u_mat4";

    private static final String VERTEX_SHADER = "" //
            + "#version 300 es\n"
            + "precision mediump float;\n"
            + "in vec4 a_position;\n"
            + "in vec3 a_normal;\n"
            + "in vec2 a_tex_coord;\n"
            + "uniform mat4 u_mvp;\n"
            + "uniform vec4 u_mat1;\n"
            + "uniform vec4 u_mat2;\n"
            + "uniform vec4 u_mat3;\n"
            + "uniform vec4 u_mat4;\n"
            + "uniform vec3 u_eye;\n"
            + "uniform vec3 u_light;\n"
            + "out vec3  n;\n"
            + "out vec3  v;\n"
            + "out vec3  l;\n"
            + "uniform float u_radius;\n"
            + "out vec2 coord;\n"
            + "out vec2 reflect_coord;\n"
            + "void main() {\n"
            + "  mat4 model;\n"
            + "  model[0] = u_mat1;\n"
            + "  model[1] = u_mat2;\n"
            + "  model[2] = u_mat3;\n"
            + "  model[3] = u_mat4;\n"
            + "  model = inverse(model);\n"
            + "  vec4 pos = model*a_position;\n"
            + "  vec4 nrm = model*vec4(a_normal,1.0);\n"
            + "  n = normalize(nrm.xyz);\n"
            + "  v = normalize(u_eye-pos.xyz);\n"
            + "  l = normalize(u_light-pos.xyz);\n"
            + "  vec3  p = pos.xyz;\n"
            + "  vec3  r = normalize(reflect(v,n));\n"
            // + "  vec3  r = normalize(refract(v,n,0.86));\n"
            + "  float b = dot(r,p);\n"
            + "  float c = dot(p,p)-u_radius*u_radius;\n"
            + "  float t = sqrt(b*b-c);\n"
            + "  if( -b + t > 0.0 ) t = -b + t;\n"
            + "  else               t = -b - t;\n"
            + "  vec3 ray = normalize(p+t*r);\n"
            + "  ray.z = ray.z/sqrt(ray.x*ray.x+ray.z*ray.z);\n"
            + "  if( ray.x > 0.0 ) reflect_coord.x =  ray.z + 1.0;\n"
            + "  else              reflect_coord.x = -ray.z - 1.0;\n"
            + "  reflect_coord.x /= 2.0;\n"
            + "  reflect_coord.y  = ray.y;\n"
            + "  reflect_coord.x = 0.5 + 0.6*asin(reflect_coord.x)/1.57079632675;\n"
            + "  reflect_coord.y = 0.5 + 0.6*asin(reflect_coord.y)/1.57079632675;\n"
            + "  coord = a_tex_coord;\n"
            + "  gl_Position = u_mvp*a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "#version 300 es\n"
            + "precision mediump float;\n"
            + "in vec2  coord;\n" //
            + "uniform vec4  u_color;\n"
            + "uniform float u_radius;\n" //
            + "in vec3  n;\n"
            + "in vec3  v;\n" //
            + "in vec3  l;\n"
            + "in vec2 reflect_coord;\n" //
            + "uniform sampler2D intexture;\n"
            + "out vec4 FragColor;\n"
            + "void main() {\n"
            + "  vec3 color = texture(intexture, reflect_coord).rgb;\n"
            + "  vec3  h = normalize(v+l);\n"
            + "  float viewing  = max ( dot(v,n), 0.0 );\n"
            + "  float diffuse  = max ( dot(l,n), 0.0 );\n"
            + "  float specular = max ( dot(h,n), 0.0 );\n"
            + "  specular = pow (specular, 100.0);\n"
            + "  color *= pow(diffuse,0.2);\n" //
            + "  color *= u_color.rgb;\n"
            + "  color += 1.5*(1.0- color)*specular;\n"
            + "  FragColor = vec4( color, 0.7-0.3*viewing );\n"
            + "  FragColor.rgb *= FragColor.a;\n" //
            + "}\n";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public ReflectionShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformVec4Key("u_color", COLOR_KEY);
        mCustomShader.addUniformVec3Key("u_light", LIGHT_KEY);
        mCustomShader.addUniformVec3Key("u_eye", EYE_KEY);
        mCustomShader.addUniformFloatKey("u_radius", RADIUS_KEY);
        mCustomShader.addTextureKey("intexture", TEXTURE_KEY);

        mCustomShader.addUniformVec4Key("u_mat1", MAT1_KEY);
        mCustomShader.addUniformVec4Key("u_mat2", MAT2_KEY);
        mCustomShader.addUniformVec4Key("u_mat3", MAT3_KEY);
        mCustomShader.addUniformVec4Key("u_mat4", MAT4_KEY);

    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
