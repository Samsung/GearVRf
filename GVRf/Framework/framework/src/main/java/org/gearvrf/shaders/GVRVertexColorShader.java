package org.gearvrf.shaders;

import android.content.Context;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

public class GVRVertexColorShader extends GVRShaderTemplate
{
    private static String fragTemplate = null;
    private static String vtxTemplate = null;

    public GVRVertexColorShader(GVRContext gvrcontext)
    {
        super("float line_width", "", "float3 a_position float4 a_color", GLSLESVersion.VULKAN);
        Context context = gvrcontext.getContext();
        fragTemplate = TextFile.readTextFile(context, R.raw.vcolor_fragment);
        vtxTemplate = TextFile.readTextFile(context, R.raw.vcolor_vertex);
        setSegment("FragmentTemplate", fragTemplate);
        setSegment("VertexTemplate", vtxTemplate);
    }
}
