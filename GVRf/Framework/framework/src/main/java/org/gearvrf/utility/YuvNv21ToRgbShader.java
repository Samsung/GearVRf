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


package org.gearvrf.utility;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;

/**
 * YUV-NV21-to-RGB shader. Adapted from this excellent post: http://stackoverflow.com/a/22456885.
 *
 * First set up the buffers and the scene object where width and height are YUV image's width and height:
 * <pre>
 * ByteBuffer mYBuffer = ByteBuffer.allocateDirect(width*height);
 * ByteBuffer mUvBuffer = ByteBuffer.allocateDirect(width*height/2);
 * myBuffer.order(ByteOrder.nativeOrder());
 * muvBuffer.order(ByteOrder.nativeOrder());
 * GVRTexture mYBufferTexture = new GVRBitmapTexture(context, new GVRTextureParameters());
 * GVRTexture mUVBufferTexture = new GVRBitmapTexture(context, new GVRTextureParameters());
 *
 * GVRSceneObject quad = new GVRSceneObject(context, 3f, 1.5f);
 * GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.BeingGenerated.ID);
 * quad.getRenderData().setMaterial(material); *
 * material.setTexture("y_texture", mYBufferTexture);
 * material.setTexture("uv_texture", mUVBufferTexture);
 * quad.getRenderData().setShaderTemplate(YuvNv21ToRgbShader.class);
 * </pre>
 *
 * Whenever there is new image data, post it:
 * <pre>
 * mYBuffer.put(image, 0, width*height);
 * mYBuffer.position(0);
 * mUvBuffer.put(image, width*height, width*height/2);
 * mUvBuffer.position(0);
 * mYBufferTexture.postBuffer(GLES30.GL_LUMINANCE, width, height, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, mYBuffer);
 * mUVBufferTexture.postBuffer(GLES30.GL_LUMINANCE_ALPHA, width / 2, height / 2, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, mYuvBuffer);
 * </pre>
 */
public class YuvNv21ToRgbShader extends GVRShaderTemplate
{
    private static final String VERTEX_SHADER =
            "in vec4 a_position;\n" +
            "in vec2 a_texcoord;\n" +
            "out vec2 v_texCoord;\n" +
            "uniform mat4 u_mvp;\n" +
            "void main() {\n" +
            "  gl_Position = u_mvp * a_position;\n" +
            "  v_texCoord = a_texcoord;\n" +
            "}\n";

    private static String FRAGMENT_SHADER =
            "precision highp float;\n" +
            "in vec2 v_texCoord;\n" +
            "uniform sampler2D y_texture;\n" +
            "uniform sampler2D uv_texture;\n" +
            "out vec4 fragColor;\n" +
            "  void main (void) {\n" +
            "  float r, g, b, y, u, v;\n" +
            "  y = texture(y_texture, v_texCoord).r;\n" +
            "  vec4 texColor = texture(uv_texture,v_texCoord);\n" +
            "  u = texColor.a - 0.5;\n" +
            "  v = texColor.r - 0.5;\n" +
            "  r = y + 1.13983*v;\n" +
            "  g = y - 0.39465*u - 0.58060*v;\n" +
            "  b = y + 2.03211*u;\n" +
            "  fragColor = vec4(r, g, b, 1.0);\n" +
            "}\n";

    public YuvNv21ToRgbShader(GVRContext gvrContext)
    {
        super("sampler2D y_texture, sampler2D uv_texture", 300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

}
