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
package org.gearvrf;

import android.content.Context;

import org.gearvrf.utility.TextFile;

/**
 * Shader which samples from either the top or bottom half of a texture.
 * This shader does not use light sources.
 *
 * @<code>
 *    a_position    position vertex attribute
 *    a_texcoord    texture coordinate vertex attribute
 *    u_color       color to modulate texture
 *    u_opacity     opacity
 *    u_right       1 = right eye, 0 = left eye
 *    u_texture     texture
 * </code>
 */
public class GVRUnlitVerticalStereoShader extends GVRShader
{
    private String vertexShader =
            "attribute vec3 a_position;\n" +
            "attribute vec2 a_texcoord;\n" +
            "uniform mat4 u_mvp;\n" +
            "varying vec2 diffuse_coord;\n" +
            "void main() {\n" +
            "  diffuse_coord = a_texcoord.xy;\n" +
            "  gl_Position = u_mvp *  vec4(a_position, 1.0);\n" +
            "}\n";

    private String fragmentShader =
            "precision highp float;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform vec3 u_color;\n" +
            "uniform float u_opacity;\n" +
            "uniform int u_right;\n" +
            "varying vec2 diffuse_coord;\n" +
            "void main()\n" +
            "{\n" +
            "  vec2 tex_coord = vec2(diffuse_coord.x, 0.5 * (diffuse_coord.y + float(u_right)));\n" +
            "  vec4 color = texture2D(u_texture, tex_coord);\n" +
            "  gl_FragColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);\n" +
            "}\n";

    public GVRUnlitVerticalStereoShader(GVRContext gvrContext)
    {
        super("float3 u_color float u_opacity int u_right", "sampler2D u_texture", "float3 a_position float2 a_texcoord");
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context,R.raw.unlit_vertical_frag));
        setSegment("VertexTemplate",TextFile.readTextFile(context,R.raw.unlit_vertical_vert));
    }
}


