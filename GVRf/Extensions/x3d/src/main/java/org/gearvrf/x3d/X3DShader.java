package org.gearvrf.x3d;

import android.content.Context;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.IRenderable;
import org.gearvrf.utility.TextFile;

import java.util.HashMap;


public class X3DShader extends GVRShaderTemplate
{
    private static String fragTemplate = null;
    private static String vtxTemplate = null;
    private static String surfaceShader = null;
    private static String addLight = null;
    private static String vtxShader = null;
    private static String normalShader = null;
    private static String skinShader = null;
    private boolean useMultitex = true;

    public X3DShader(GVRContext gvrcontext)
    {
        super("float4 ambient_color; float4 diffuse_color; float4 specular_color; float4 emissive_color; mat3 texture_matrix; float specular_exponent",
              "sampler2D diffuseTexture",
              "float3 a_position float2 a_texcoord float3 a_normal float4 a_bone_weights int4 a_bone_indices float4 a_tangent float4 a_bitangent",
              GLSLESVersion.VULKAN);

        if (fragTemplate == null)
        {
            Context context = gvrcontext.getContext();
            fragTemplate = TextFile.readTextFile(context, org.gearvrf.R.raw.fragment_template);
            vtxTemplate = TextFile.readTextFile(context, org.gearvrf.x3d.R.raw.x3d_vertex_template);
            surfaceShader = TextFile.readTextFile(context, org.gearvrf.x3d.R.raw.x3d_surface);
            vtxShader = TextFile.readTextFile(context, org.gearvrf.x3d.R.raw.x3d_vertex);
            addLight = TextFile.readTextFile(context, org.gearvrf.R.raw.addlight);
        }
        setSegment("FragmentTemplate", fragTemplate);
        setSegment("VertexTemplate", vtxTemplate);
        setSegment("FragmentSurface", surfaceShader);
        setSegment("FragmentAddLight", addLight);
        setSegment("VertexShader", vtxShader);

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
        material.setVec4("ambient_color", 0.2f, 0.2f, 0.2f, 1.0f);
        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("specular_color", 0.0f, 0.0f, 0.0f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 1.0f);
        material.setFloat("specular_exponent", 0.0f);
    }
}
