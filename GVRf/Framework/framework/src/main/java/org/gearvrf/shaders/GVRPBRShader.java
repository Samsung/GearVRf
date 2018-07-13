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
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.IRenderable;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

import java.util.HashMap;

/**
 * Manages a set of variants on vertex and fragment shaders from the same source
 * code.
 */
public class GVRPBRShader extends GVRShaderTemplate
{
    private static String fragTemplate = null;
    private static String vtxTemplate = null;
    private static String surfaceShader = null;
    private static String addLight = null;
    private static String vtxShader = null;
    private static String normalShader = null;
    private static String skinShader = null;

    public GVRPBRShader(GVRContext gvrcontext)
    {
         super("float4 diffuse_color; float4 specular_color; float4 emissive_color; float metallic; float roughness; float specular_exponent; float lightmapStrength; float normalScale; float glossinessFactor",
                "sampler2D diffuseTexture; sampler2D metallicRoughnessTexture; sampler2D specularTexture; sampler2D lightmapTexture; sampler2D diffuseTexture1; sampler2D normalTexture; sampler2D emissiveTexture; sampler2D brdfLUTTexture; samplerCube diffuseEnvTex; samplerCube specularEnvTexture",
                "float3 a_position float2 a_texcoord float2 a_texcoord1 float2 a_texcoord2 float2 a_texcoord3 float3 a_normal float4 a_bone_weights int4 a_bone_indices float4 a_tangent float4 a_bitangent",
                GLSLESVersion.VULKAN);

        if (fragTemplate == null)
        {
            Context context = gvrcontext.getContext();
            fragTemplate = TextFile.readTextFile(context, R.raw.fragment_template_multitex);
            vtxTemplate = TextFile.readTextFile(context, R.raw.vertex_template_multitex);
            surfaceShader = TextFile.readTextFile(context, R.raw.pbr_surface);
            vtxShader = TextFile.readTextFile(context, R.raw.pos_norm_multitex);
            normalShader = TextFile.readTextFile(context, R.raw.normalmap);
            skinShader = TextFile.readTextFile(context, R.raw.vertexskinning);
            addLight = TextFile.readTextFile(context, R.raw.pbr_addlight);
        }
        String defines = "#define ambient_coord metallicRoughness_coord\n";
        setSegment("FragmentTemplate", defines + fragTemplate);
        setSegment("VertexTemplate", defines + vtxTemplate);
        setSegment("FragmentSurface", surfaceShader);
        setSegment("FragmentAddLight", addLight);
        setSegment("VertexSkinShader", skinShader);
        setSegment("VertexMorphShader", "");
        setSegment("VertexShader", vtxShader);
        setSegment("VertexNormalShader", normalShader);

        mHasVariants = true;
        mUsesLights = true;
    }

    public HashMap<String, Integer> getRenderDefines(IRenderable renderable, GVRScene scene)
    {
        HashMap<String, Integer> defines = super.getRenderDefines(renderable, scene);

        if (!defines.containsKey("LIGHTSOURCES") || (defines.get("LIGHTSOURCES") != 1))
        {
            defines.put("a_normal", 0);
        }
        return defines;
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 1.0f);
        material.setFloat("normalScale", 1);
        material.setFloat("lightmapStrength", 1);
    }
}

