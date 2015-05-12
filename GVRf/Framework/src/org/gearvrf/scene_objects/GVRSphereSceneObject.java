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

package org.gearvrf.scene_objects;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;

public class GVRSphereSceneObject extends GVRSceneObject {

    private static final String TAG = "GVRSphereSceneObject";
    private static final int NUM_STACKS = 180;
    private static final int NUM_SLICES = 360;

    private float[] vertices;
    private float[] normals;
    private float[] texCoords;
    private char[] indices;

    /**
     * Constructs a sphere scene object.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRSphereSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        generateSphere(NUM_STACKS, NUM_SLICES);
        
        GVRMesh mesh = new GVRMesh(gvrContext);
        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setTriangles(indices);

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

    private void generateSphere(int numStacks, int numSlices) {
        int numVertices = numStacks * numSlices;
        vertices = new float[3*numVertices];
        normals = new float[3*numVertices];
        texCoords = new float[2*numVertices];
        indices = new char[numVertices];
        float[] x = new float[4];
        float[] y = new float[4];
        float[] z = new float[4];
        float[] s = new float[4];
        float[] t = new float[4];
        float[] nx = new float[4];
        float[] ny = new float[4];
        float[] nz = new float[4];
        int vertexCount = 0;
        int texCoordCount = 0;

        for(int theta = 0; theta < numStacks; theta++) {
            float theta1 = ((float)(theta)/numStacks)*(float)Math.PI;
            float theta2 = ((float)(theta+1)/numStacks)*(float)Math.PI;
            float t0 = 1.0f - ((float)(theta)/numStacks);
            float t1 = 1.0f - ((float)(theta+1)/numStacks);

            for(int phi = 0; phi < numSlices; phi++) {
                float phi1 = ((float)(phi)/numSlices)*2.0f*(float)Math.PI;
                float phi2 = ((float)(phi+1)/numSlices)*2.0f*(float)Math.PI;
                float s0 = 1.0f - ((float)(phi)/numSlices);
                float s1 = 1.0f - ((float)(phi+1)/numSlices);

                //   2-----3
                //   |     |
                //   0-----1
                x[0] = (float)(Math.sin(theta1) * Math.cos(phi2));
                y[0] = (float)(Math.sin(theta1) * Math.sin(phi2));
                z[0] = (float)(Math.cos(theta1));
                nx[0] = (float)(Math.sin(theta1) * Math.cos(phi2));
                ny[0] = (float)(Math.sin(theta1) * Math.sin(phi2));
                nz[0] = (float)(Math.cos(theta1));
                s[0] = s0; t[0] = t1;

                x[1] = (float)(Math.sin(theta2) * Math.cos(phi2));
                y[1] = (float)(Math.sin(theta2) * Math.sin(phi2));
                z[1] = (float)(Math.cos(theta2));
                nx[1] = (float)(Math.sin(theta2) * Math.cos(phi2));
                ny[1] = (float)(Math.sin(theta2) * Math.sin(phi2));
                nz[1] = (float)(Math.cos(theta2));
                s[1] = s1; t[1] = t1;

                x[2] = (float)(Math.sin(theta1) * Math.cos(phi1));
                y[2] = (float)(Math.sin(theta1) * Math.sin(phi1));
                z[2] = (float)(Math.cos(theta1));
                nx[2] = (float)(Math.sin(theta1) * Math.cos(phi1));
                ny[2] = (float)(Math.sin(theta1) * Math.sin(phi1));
                nz[2] = (float)(Math.cos(theta1));
                s[2] = s0; t[2] = t0;

                x[3] = (float)(Math.sin(theta2) * Math.cos(phi1));
                y[3] = (float)(Math.sin(theta2) * Math.sin(phi1));
                z[3] = (float)(Math.cos(theta2));
                nx[3] = (float)(Math.sin(theta2) * Math.cos(phi1));
                ny[3] = (float)(Math.sin(theta2) * Math.sin(phi1));
                nz[3] = (float)(Math.cos(theta2));
                s[3] = s1; t[3] = t0;

                if(theta == 0) { // top
                    // 3, 0, 1
                    vertices[vertexCount+0] = x[3]; vertices[vertexCount+1] = y[3]; vertices[vertexCount+2] = z[3];
                    normals[vertexCount+0] = nx[3]; normals[vertexCount+1] = ny[3]; normals[vertexCount+2] = nz[3];
                    texCoords[texCoordCount+0] =  s[3]; texCoords[texCoordCount+1] =  t[3];

                    vertices[vertexCount+3] = x[0]; vertices[vertexCount+4] = y[0]; vertices[vertexCount+5] = z[0];
                    normals[vertexCount+3] = nx[0]; normals[vertexCount+4] = ny[0]; normals[vertexCount+5] = nz[0];
                    texCoords[texCoordCount+2] =  s[0]; texCoords[texCoordCount+3] =  t[0];

                    vertices[vertexCount+6] = x[1]; vertices[vertexCount+7] = y[1]; vertices[vertexCount+8] = z[1];
                    normals[vertexCount+6] = nx[1]; normals[vertexCount+7] = ny[1]; normals[vertexCount+8] = nz[1];
                    texCoords[texCoordCount+4] =  s[1]; texCoords[texCoordCount+5] =  t[1];
                    vertexCount += 9;
                    texCoordCount += 6;
                } else if(theta+1 == numStacks) { // bottom
                    // 0, 3, 2
                    vertices[vertexCount+3] = x[0]; vertices[vertexCount+4] = y[0]; vertices[vertexCount+5] = z[0];
                    normals[vertexCount+3] = nx[0]; normals[vertexCount+4] = ny[0]; normals[vertexCount+5] = nz[0];
                    texCoords[texCoordCount+0] =  s[0]; texCoords[texCoordCount+1] =  t[0];
                    vertices[vertexCount+0] = x[3]; vertices[vertexCount+1] = y[3]; vertices[vertexCount+2] = z[3];
                    normals[vertexCount+0] = nx[3]; normals[vertexCount+1] = ny[3]; normals[vertexCount+2] = nz[3];
                    texCoords[texCoordCount+2] =  s[3]; texCoords[texCoordCount+3] =  t[3];
                    vertices[vertexCount+6] = x[2]; vertices[vertexCount+7] = y[2]; vertices[vertexCount+8] = z[2];
                    normals[vertexCount+6] = nx[2]; normals[vertexCount+7] = ny[2]; normals[vertexCount+8] = nz[2];
                    texCoords[texCoordCount+4] =  s[2]; texCoords[texCoordCount+5] =  t[2];
                    vertexCount += 9;
                    texCoordCount += 6;
                } else {
                    // 0, 1, 2
                    // 2, 1, 3
                    vertices[vertexCount+0] = x[0]; vertices[vertexCount+1] = y[0]; vertices[vertexCount+2] = z[0];
                    normals[vertexCount+0] = nx[0]; normals[vertexCount+1] = ny[0]; normals[vertexCount+2] = nz[0];
                    texCoords[texCoordCount+0] =  s[0]; texCoords[texCoordCount+1] =  t[0];
                    vertices[vertexCount+3] = x[1]; vertices[vertexCount+4] = y[1]; vertices[vertexCount+5] = z[1];
                    normals[vertexCount+3] = nx[1]; normals[vertexCount+4] = ny[1]; normals[vertexCount+5] = nz[1];
                    texCoords[texCoordCount+2] =  s[1]; texCoords[texCoordCount+3] =  t[1];
                    vertices[vertexCount+6] = x[2]; vertices[vertexCount+7] = y[2]; vertices[vertexCount+8] = z[2];
                    normals[vertexCount+6] = nx[2]; normals[vertexCount+7] = ny[2]; normals[vertexCount+8] = nz[2];
                    texCoords[texCoordCount+4] =  s[2]; texCoords[texCoordCount+5] =  t[2];
                    vertices[vertexCount+9] = x[2]; vertices[vertexCount+10] = y[2]; vertices[vertexCount+11] = z[2];
                    normals[vertexCount+9] = nx[2]; normals[vertexCount+10] = ny[2]; normals[vertexCount+11] = nz[2];
                    texCoords[texCoordCount+6] =  s[2]; texCoords[texCoordCount+7] =  t[2];
                    vertices[vertexCount+12] = x[1]; vertices[vertexCount+13] = y[1]; vertices[vertexCount+14] = z[1];
                    normals[vertexCount+12] = nx[1]; normals[vertexCount+13] = ny[1]; normals[vertexCount+14] = nz[1];
                    texCoords[texCoordCount+8] =  s[1]; texCoords[texCoordCount+9] =  t[1];
                    vertices[vertexCount+15] = x[3]; vertices[vertexCount+16] = y[3]; vertices[vertexCount+17] = z[3];
                    normals[vertexCount+15] = nx[3]; normals[vertexCount+16] = ny[3]; normals[vertexCount+17] = nz[3];
                    texCoords[texCoordCount+10] =  s[3]; texCoords[texCoordCount+11] =  t[3];
                    vertexCount += 18;
                    texCoordCount += 12;
                }
            }
        }
        android.util.Log.d(TAG, "vertexCount = " + vertexCount);
        android.util.Log.d(TAG, "numVertices = " + numVertices);

    }

}




