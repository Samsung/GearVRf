/* Copyright 2017 Samsung Electronics Co., LTD
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
package org.gearvrf.scene_objects;

import android.opengl.GLES30;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.shaders.GVRVertexColorShader;
import org.joml.Vector4f;

/***
 * A {@link GVRSceneObject} representing a line or ray
 */
public class GVRLineSceneObject extends GVRSceneObject {

    /**
     * The simple constructor; creates a line of length 1.
     *
     * @param gvrContext current {@link GVRContext}
     */
    public GVRLineSceneObject(GVRContext gvrContext){
        this(gvrContext, 1.0f);
    }

    /**
     * Creates a line based on the passed {@code length} argument
     *
     * @param gvrContext    current {@link GVRContext}
     * @param length        length of the line/ray
     */
    public GVRLineSceneObject(GVRContext gvrContext, float length)
    {
        super(gvrContext, generateLine(gvrContext, "float3 a_position", length));
        final GVRRenderData renderData = getRenderData().setDrawMode(GLES30.GL_LINES);

        final GVRMaterial material = new GVRMaterial(gvrContext, new GVRShaderId(GVRVertexColorShader.class));
        renderData.disableLight();
        renderData.setMaterial(material);
    }

    /**
     * Creates a line based on the passed {@code length} argument
     * with vertex colors at the endpoints.
     * <p>
     * This line will use the {@link GVRVertexColorShader} to vary
     * the color across the length of the line.
     *
     * @param gvrContext    current {@link GVRContext}
     * @param length        length of the line/ray
     * @param startColor    RGB color for starting point
     * @param endColor      RGB color for ending point
     */
    public GVRLineSceneObject(GVRContext gvrContext, float length, Vector4f startColor, Vector4f endColor)
    {
        super(gvrContext, generateLine(gvrContext, "float3 a_position float4 a_color", length));
        final GVRRenderData renderData = getRenderData().setDrawMode(GLES30.GL_LINES);
        final GVRMaterial material = new GVRMaterial(gvrContext,
                                                     new GVRShaderId(GVRVertexColorShader.class));
        float[] colors = {
            startColor.x, startColor.y, startColor.z, startColor.w,
            endColor.y,    endColor.y,   endColor.z,  endColor.w
        };

        renderData.disableLight();
        renderData.setMaterial(material);
        renderData.getMesh().setFloatArray("a_color", colors);
    }

    private static GVRMesh generateLine(GVRContext gvrContext, String vertexDesc, float length)
    {
        GVRMesh mesh = new GVRMesh(gvrContext, vertexDesc);
        float[] vertices = {
                0,          0,          0,
                0,          0,          -length
        };
        mesh.setVertices(vertices);
        return mesh;
    }
}
