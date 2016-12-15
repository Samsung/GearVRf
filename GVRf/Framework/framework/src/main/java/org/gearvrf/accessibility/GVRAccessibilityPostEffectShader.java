/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package org.gearvrf.accessibility;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCustomPostEffectShaderId;
import org.gearvrf.GVRPostEffectMap;
import org.gearvrf.GVRPostEffectShaderManager;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

/**
 * Shader to invert colors by post processing rendered image from cameras.
 */
final class GVRAccessibilityPostEffectShader  extends GVRShaderTemplate {
    static String fragmentSource;
    static String vertexSource;

    public GVRAccessibilityPostEffectShader(GVRContext context) {
        super("", "sampler2D u_texture", "float3 a_position float2 a_texcoord");
        if (vertexSource == null)
        {
            vertexSource = TextFile.readTextFile(context.getContext(), R.raw.inverted_colors_vertex);
        }
        if (fragmentSource == null)
        {
            fragmentSource = TextFile.readTextFile(context.getContext(), R.raw.inverted_colors_fragment);
        }
        setSegment("VertexTemplate", vertexSource);
        setSegment("FragmentTemplate", fragmentSource);
    }
}
