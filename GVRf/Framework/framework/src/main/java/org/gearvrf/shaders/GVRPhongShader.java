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

import java.util.HashMap;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.IRenderable;
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
       private static String morphShader = null;

       public GVRPhongShader(GVRContext gvrcontext)
       {
            super("float4 ambient_color; float4 diffuse_color; float4 specular_color; float4 emissive_color; float3 u_color; float u_opacity; float specular_exponent; float line_width; float2 u_lightmap_offset; float2 u_lightmap_scale; int u_numblendshapes; float u_blendweights[75];",
                   "sampler2D diffuseTexture; sampler2D ambientTexture; sampler2D specularTexture; sampler2D opacityTexture; sampler2D lightmapTexture; sampler2D normalTexture; sampler2D emissiveTexture; sampler2D blendshapeTexture",
                   "float3 a_position float2 a_texcoord float2 a_texcoord1 float2 a_texcoord2 float2 a_texcoord3 float3 a_normal float4 a_bone_weights int4 a_bone_indices float3 a_tangent float3 a_bitangent",
                   GLSLESVersion.VULKAN);

           if (fragTemplate == null)
           {
               Context context = gvrcontext.getContext();
               fragTemplate = TextFile.readTextFile(context, R.raw.fragment_template_multitex);
               vtxTemplate = TextFile.readTextFile(context, R.raw.vertex_template_multitex);
               surfaceShader = TextFile.readTextFile(context, R.raw.phong_surface_multitex);
               vtxShader = TextFile.readTextFile(context, R.raw.pos_norm_multitex);
               normalShader = TextFile.readTextFile(context, R.raw.normalmap);
               skinShader = TextFile.readTextFile(context, R.raw.vertexskinning);
               morphShader = TextFile.readTextFile(context, R.raw.vertexmorph);
               addLight = TextFile.readTextFile(context, R.raw.addlight);
           }
           setSegment("FragmentTemplate", fragTemplate);
           setSegment("VertexTemplate", vtxTemplate);
           setSegment("FragmentSurface", surfaceShader);
           setSegment("FragmentAddLight", addLight);
           setSegment("VertexSkinShader", skinShader);
           setSegment("VertexMorphShader", morphShader);
           setSegment("VertexShader", vtxShader);
           setSegment("VertexNormalShader", normalShader);

           mHasVariants = true;
           mUsesLights = true;
       }
       
       public HashMap<String, Integer> getRenderDefines(IRenderable renderable, GVRScene scene)
       {
           HashMap<String, Integer> defines = super.getRenderDefines(renderable, scene);
           boolean lightMapEnabled  = (renderable instanceof GVRRenderData) ? ((GVRRenderData) renderable).isLightMapEnabled() : false;

           if (!lightMapEnabled)
               defines.put("lightMapTexture", 0);
           if (!defines.containsKey("LIGHTSOURCES") || (defines.get("LIGHTSOURCES") != 1))
           {
               defines.put("a_normal", 0);
           }
           return defines;
       }

       protected void setMaterialDefaults(GVRShaderData material)
       {
           material.setVec4("ambient_color", 0.2f, 0.2f, 0.2f, 1.0f);
           material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
           material.setVec4("specular_color", 0.0f, 0.0f, 0.0f, 1.0f);
           material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 1.0f);
           material.setFloat("specular_exponent", 0.0f);
           material.setFloat("line_width", 1.0f);
           material.setFloat("u_opacity", 0.0f);
       }
   }

