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


package org.gearvrf.utility;

import org.gearvrf.GVRShaderTemplate;

public class ColorShader extends GVRShaderTemplate
{
    private static final String VERTEX_SHADER = "in vec4 a_position;\n"
            + "uniform mat4 u_mvp;\n"
            + "void main() {\n"
            + "  gl_Position = u_mvp * a_position;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "uniform vec4 u_color;\n"
            + "out vec4 fragColor;\n"
            + "void main() {\n"
            + "  fragColor = u_color;\n"
            + "}\n";

    /**
     * A simple shader that lets you choose a solid color for your scene object.
     * 1. set the shader for your scene object via getRenderData().setShaderTemplate(ColorShader.class).
     * 2. specify the color to be used via getRenderData().getMaterial().setVec4("u_color", R, G, B, A).
     */
    public ColorShader()
    {
        super("float4 u_color", 300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

}
