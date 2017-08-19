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

package org.gearvrf.particlesystem;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.utility.TextFile;

public class ParticleShader extends GVRShaderTemplate
{
    private static String fragTemplate;
    private static String vtxTemplate;

    public ParticleShader(GVRContext context)
    {
        super("float4 u_color; float u_particle_age; float3 u_acceleration; float u_size_change_rate; " +
                "float u_time; float u_particle_size; float u_fade; float u_noise_factor");
        fragTemplate = TextFile.readTextFile(context.getContext(), R.raw.particle_frag);
        vtxTemplate = TextFile.readTextFile(context.getContext(), R.raw.particle_vert);

        setSegment("VertexTemplate", vtxTemplate);
        setSegment("FragmentTemplate", fragTemplate);
    }

}
