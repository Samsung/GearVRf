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
    * Implements the Phong lighting model in GearVRF.
    * <p>
    * The Phong shader is used by content loaded through
    * the GVRAssetLoader class. It supports ambient,
    * diffuse, specular, opacity and emissive material
    * colors and textures. You can blend two textures
    * of the same type together. It also supports multiple light sources.
    * <p>
    * <h3>Vertex Attributes:</h3>
    * <table>
    *     <tr><td>a_position</td><td>x,y,z vertex position</td></tr>
    *     <tr><td>a_normal</td><td>x,y,z, vertex normal</td></tr>
    *     <tr><td>a_texcoord</td><td>u,v first texture coordinate set</td></tr>
    *     <tr><td>a_texcoord1</td><td>u,v second texture coordinate set</td></tr>
    *     <tr><td>a_texcoord2</td><td>u,v third texture coordinate set</td></tr>
    *     <tr><td>a_texcoord3</td><td>u,v fourth texture coordinate set</td></tr>
    * </table>
    * <p>
    *  <h3>Material Uniforms:</h3>
    *  <table>
    *      <tr><td>ambient_color</td><td>r,g,b,a ambient material color</td></tr>
    *      <tr><td>diffuse_color</td><td>r,g,b,a diffuse material color</td></tr>
    *      <tr><td>specular_color</td><td>r,g,b,a specular material color</td></tr>
    *      <tr><td>emissive_color</td><td>r,g,b,a emissive material color</td></tr>
    *      <tr><td>specular_exponent</td><td>Phong specular power</td></tr>
    *      <tr><td>u_lightmap_offset</td><td>offset for lightmap texture coordinates</td></tr>
    *      <tr><td>u_lightmap_scale</td><td>scale for lightmap texture coordinates</td></tr>
    *  </table>
    *  <h3>Textures:</h3>
    *  <table>
    *      <tr><td>ambientTexture</td><td>texture providing ambient color</td></tr>
    *      <tr><td>diffuseTexture</td><td>texture providing diffuse color</td></tr>
    *      <tr><td>specularTexture</td><td>texture providing specular color</td></tr>
    *      <tr><td>opacityTexture</td><td>texture controlling opacity (alpha)</td></tr>
    *      <tr><td>emissiveTexture</td><td>texture providing emissive color</td></tr>
    *      <tr><td>lightmapTexture</td><td>texture providing diffuse illumination</td></tr>
    *      <tr><td>normalTexture</td><td>texture containing normal map</td></tr>
    *  </table>
    *  <h3>Texture Blending:</h3>
    *  To support blending of textures, another set of texture samplers
    *  is supported. These secondary textures are blended
    *  with the previous texture of the same type based on the
    *  blend operation.
    *  <ul>
     *  @see GVRLightBase GVRSpotLight GVRDirectLight GVRPointLight
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
       private boolean useMultitex = true;

       public GVRPhongShader(GVRContext gvrcontext)
       {
           super("float4 ambient_color; float4 diffuse_color; float4 specular_color; float4 emissive_color; float specular_exponent", 300);
           if (fragTemplate == null)
           {
               Context context = gvrcontext.getContext();
               if (useMultitex)
               {
                   fragTemplate = TextFile.readTextFile(context, R.raw.fragment_template_multitex);
                   vtxTemplate = TextFile.readTextFile(context, R.raw.vertex_template_multitex);
                   surfaceShader = TextFile.readTextFile(context, R.raw.phong_surface_multitex);
                   vtxShader = TextFile.readTextFile(context, R.raw.pos_norm_multitex);
               }
               else
               {
                   fragTemplate = TextFile.readTextFile(context, R.raw.fragment_template);
                   vtxTemplate = TextFile.readTextFile(context, R.raw.vertex_template);
                   surfaceShader = TextFile.readTextFile(context, R.raw.phong_surface);
                   vtxShader = TextFile.readTextFile(context, R.raw.pos_norm_tex);
               }
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
       
       public HashMap<String, Integer> getRenderDefines(GVRRenderData rdata, GVRScene scene)
       {
           GVRLightBase[] lights = (scene != null) ? scene.getLightList() : null;
           HashMap<String, Integer> defines = super.getRenderDefines(rdata, scene);
           if (!rdata.isLightMapEnabled())
               defines.put("lightMapTexture", 0);
           return defines;
       }       
   }

