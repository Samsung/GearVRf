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
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

/**
* Manages a set of variants on vertex and fragment shaders from the same source
* code.
 */
public class GVRDepthShader extends GVRShaderTemplate
{
    private static String fragTemplate = null;
    private static String vtxTemplate = null;
    private static String skinShader = null;

    public GVRDepthShader(GVRContext gvrcontext)
    {
        super("", "", "float3 a_position float4 a_bone_weights int4 a_bone_indices", GLSLESVersion.VULKAN);
        if (fragTemplate == null)
        {
            Context context = gvrcontext.getContext();
            fragTemplate = TextFile.readTextFile(context, R.raw.depth_shader);
            vtxTemplate = TextFile.readTextFile(context, R.raw.vertex_template_depth);
            skinShader = TextFile.readTextFile(context, R.raw.vertexskinning);
        }
        setSegment("FragmentTemplate", fragTemplate);
        setSegment("VertexTemplate", vtxTemplate);
        setSegment("VertexSkinShader", skinShader);
    }       
}