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
package org.gearvrf.shaders;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

/**
 * Error shader used by GearVRF after a rendering error.
 * It only accesses the "a_position" vertex attribute
 * and does not rely on any uniform data. The mesh
 * will be rendered in SOLID RED.
 */
public class GVRErrorShader extends GVRShaderTemplate
{
    private String fragmentShader =
            "#extension GL_ARB_separate_shader_objects : enable\n" +
                    "#extension GL_ARB_shading_language_420pack : enable\n"+
                    "layout(location = 0) out vec4 outColor; "+
    "void main() { outColor = vec4(1, 0, 0, 1); }\n";

    public GVRErrorShader(GVRContext ctx)
    {
        super("", "", "float3 a_position",GLSLESVersion.VULKAN);
        setSegment("FragmentTemplate", fragmentShader);
        setSegment("VertexTemplate", TextFile.readTextFile(ctx.getContext(), R.raw.pos_ubo));
    }
}
