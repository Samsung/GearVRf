package com.samsung.android.gvr.app.base.misc;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCustomPostEffectShaderId;
import org.gearvrf.GVRPostEffectMap;
import org.gearvrf.GVRPostEffectShaderManager;

public class CustomPostEffectShaderManager {

    private final String VERTEX_SHADER = "" //
            + "attribute vec4 a_position;\n"
            + "attribute vec4 a_tex_coord;\n" //
            + "varying vec2 v_tex_coord;\n"
            + "void main() {\n" //
            + "  v_tex_coord = a_tex_coord.xy;\n"
            + "  gl_Position = a_position;\n" //
            + "}\n";

    private final String FRAGMENT_SHADER = "" //
            + "precision mediump float;\n"
            + "uniform sampler2D u_texture;\n"
            + "uniform vec3 u_ratio_r;\n"
            + "uniform vec3 u_ratio_g;\n"
            + "uniform vec3 u_ratio_b;\n"
            + "varying vec2 v_tex_coord;\n"
            + "void main() {\n"
            + "  vec4 tex = texture2D(u_texture, v_tex_coord);\n"
            + "  float r = tex.r * u_ratio_r.r + tex.g * u_ratio_r.g + tex.b * u_ratio_r.b;\n"
            + "  float g = tex.r * u_ratio_g.r + tex.g * u_ratio_g.g + tex.b * u_ratio_g.b;\n"
            + "  float b = tex.r * u_ratio_b.r + tex.g * u_ratio_b.g + tex.b * u_ratio_b.b;\n"
            + "  vec3 color = vec3(r, g, b);\n"
            + "  gl_FragColor = vec4(color, tex.a);\n" //
            + "}\n";

    private GVRCustomPostEffectShaderId mShaderId;
    private GVRPostEffectMap mCustomShader;

    public CustomPostEffectShaderManager(GVRContext gvrContext) {
        final GVRPostEffectShaderManager shaderManager = gvrContext
                .getPostEffectShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformVec3Key("u_ratio_r", "ratio_r");
        mCustomShader.addUniformVec3Key("u_ratio_g", "ratio_g");
        mCustomShader.addUniformVec3Key("u_ratio_b", "ratio_b");
    }

    public GVRCustomPostEffectShaderId getShaderId() {
        return mShaderId;
    }
}

