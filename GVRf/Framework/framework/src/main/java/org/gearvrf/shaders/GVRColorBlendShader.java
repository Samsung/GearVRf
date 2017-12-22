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

import android.content.Context;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

/**
 * Shader which blends between a color and a texture.
 * This shader ignores light sources.
 * @<code>
 *     a_position   position vertex attribute
 *     a_texcoord   texture coordinate vertex attribute
 *     u_texture    texture to blend
 *     u_color      color to blend
 *     u_factor     blend factor (0 to 1)
 * </code>
 */
public class GVRColorBlendShader extends GVRShader
{
    public GVRColorBlendShader(GVRContext ctx)
    {
        super("float3 u_color float u_factor", "sampler2D u_texture", "float3 a_position float2 a_texcoord", GLSLESVersion.VULKAN);
        Context context = ctx.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.color_blend_frag));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.color_blend_vert));
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec3("u_color", 1, 1, 1);
        material.setFloat("u_factor", 0);
    }
}