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
import java.util.List;

import org.gearvrf.utility.TextFile;

import android.content.Context;

import org.gearvrf.R;

   /**
    * Manages a set of variants on vertex and fragment shaders from the same source
    * code.
    */
   public class GVRPhongShader extends GVRShaderTemplate
   {
       private static String fragTemplate = null;
       private static String vtxTemplate = null;
       private static String surfaceShader = null;
       private static String addLight = null;
       private static String vtxShader = null;
       private static String normalShader = null;
       private static String skinShader = null;

       public GVRPhongShader(GVRContext gvrcontext)
       {
           super("float4 ambient_color; float4 diffuse_color; float4 specular_color; float4 emissive_color; float specular_exponent");
           if (fragTemplate == null) {
               Context context = gvrcontext.getContext();
               fragTemplate = TextFile.readTextFile(context, R.raw.fragment_template);
               vtxTemplate = TextFile.readTextFile(context, R.raw.vertex_template);
               surfaceShader = TextFile.readTextFile(context, R.raw.phong_surface);
               vtxShader = TextFile.readTextFile(context, R.raw.pos_norm_tex);
               normalShader = TextFile.readTextFile(context, R.raw.normalmap);
               skinShader = TextFile.readTextFile(context, R.raw.vertexskinning);
               addLight = TextFile.readTextFile(context, R.raw.addlight);
           }
           setSegment("FragmentTemplate", fragTemplate);
           setSegment("VertexTemplate", vtxTemplate);
           setSegment("FragmentSurface", surfaceShader);
           setSegment("FragmentAddLight", addLight);
           setSegment("VertexShader", vtxShader);
           setSegment("VertexNormalShader", normalShader);
           setSegment("VertexSkinShader", skinShader);
       }
       
       public HashMap<String, Integer> getRenderDefines(GVRRenderData rdata, GVRLightBase[] lights) {
           HashMap<String, Integer> defines = super.getRenderDefines(rdata, lights);
           
           if (!rdata.isLightMapEnabled())
               defines.put("lightMapTexture", 0);
           return defines;
       }       
   }

