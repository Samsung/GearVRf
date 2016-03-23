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

import java.util.HashMap;

import org.gearvrf.utility.TextFile;
import org.gearvrf.R;

   /**
    * Manages a set of variants on vertex and fragment shaders from the same source
    * code.
    */
   public class GVRPhongShader extends org.gearvrf.GVRShader
   {
       public GVRPhongShader(GVRContext context)
       {
           super("PhongShader", "float4 ambientColor; float4 diffuseColor; float4 specularColor; float specularExponent");
           String fragTemplate = TextFile.readTextFile(context.getContext(), R.raw.fragment_template);
           String vtxTemplate = TextFile.readTextFile(context.getContext(), R.raw.vertex_template);
           String surfaceShader = TextFile.readTextFile(context.getContext(), R.raw.phong_surface);
           String vtxShader = TextFile.readTextFile(context.getContext(), R.raw.pos_nmap_tex);
           setSegment("FragmentTemplate", fragTemplate);
           setSegment("VertexTemplate", vtxTemplate);
           setSegment("FragmentSurface", surfaceShader);
           setSegment("VertexShader", vtxShader);
           setSegment("VertexSkinShader", null);
       }
   }

