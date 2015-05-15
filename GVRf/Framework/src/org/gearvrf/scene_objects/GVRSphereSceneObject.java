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
    private static final int NUM_STACKS = 18;
    private static final int NUM_SLICES = 36;

    private float[] vertices;
    private float[] normals;
    private float[] texCoords;
    private char[] indices;

    private int vertexCount = 0;
    private int texCoordCount = 0;
    private char indexCount = 0;
    private char triangleCount = 0;

    /**
     * Constructs a sphere scene object with a radius of 1 and 18 stacks, and 36
     * slices.
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
        int capNumVertices = 3 * numSlices;
        int bodyNumVertices = 4 * numSlices * numStacks;
        int numVertices = (2 * capNumVertices) + bodyNumVertices;
        int numTriangles = (2 * capNumVertices) + (6 * numSlices * numStacks);

        vertices = new float[3 * numVertices];
        normals = new float[3 * numVertices];
        texCoords = new float[2 * numVertices];
        indices = new char[numTriangles];

        // bottom cap
        createCap(0, numStacks, numSlices, false);

        // body
        createBody(numStacks, numSlices);

        // top cap
        createCap(numStacks, numStacks, numSlices, true);
    }

    private void createCap(int stack, int numStacks, int numSlices, boolean top) {

        float stackPercentage0;
        float stackPercentage1;

        if (top) {
            stackPercentage0 = ((float) (stack - 1) / numStacks);
            stackPercentage1 = ((float) (stack) / numStacks);

        } else {
            stackPercentage0 = ((float) (stack + 1) / numStacks);
            stackPercentage1 = ((float) (stack) / numStacks);
        }

        float t0 = 1.0f - stackPercentage0;
        float t1 = 1.0f - stackPercentage1;
        float theta1 = stackPercentage0 * (float) Math.PI;
        float theta2 = stackPercentage1 * (float) Math.PI;
        float cosTheta1 = (float) Math.cos(theta1);
        float sinTheta1 = (float) Math.sin(theta1);
        float cosTheta2 = (float) Math.cos(theta2);
        float sinTheta2 = (float) Math.sin(theta2);

        for (int slice = 0; slice < numSlices; slice++) {
            float slicePercentage0 = ((float) (slice) / numSlices);
            float slicePercentage1 = ((float) (slice + 1) / numSlices);
            float phi1 = slicePercentage0 * 2.0f * (float) Math.PI;
            float phi2 = slicePercentage1 * 2.0f * (float) Math.PI;
            float s0 = slicePercentage0;
            float s1 = slicePercentage1;
            float s2 = (s0 + s1) / 2.0f;
            float cosPhi1 = (float) Math.cos(phi1);
            float sinPhi1 = (float) Math.sin(phi1);
            float cosPhi2 = (float) Math.cos(phi2);
            float sinPhi2 = (float) Math.sin(phi2);

            float x0 = sinTheta1 * cosPhi1;
            float y0 = sinTheta1 * sinPhi1;
            float z0 = cosTheta1;

            float x1 = sinTheta1 * cosPhi2;
            float y1 = sinTheta1 * sinPhi2;
            float z1 = cosTheta1;

            float x2 = sinTheta2 * cosPhi1;
            float y2 = sinTheta2 * sinPhi1;
            float z2 = cosTheta2;

            vertices[vertexCount + 0] = x0;
            vertices[vertexCount + 1] = y0;
            vertices[vertexCount + 2] = z0;

            vertices[vertexCount + 3] = x1;
            vertices[vertexCount + 4] = y1;
            vertices[vertexCount + 5] = z1;

            vertices[vertexCount + 6] = x2;
            vertices[vertexCount + 7] = y2;
            vertices[vertexCount + 8] = z2;

            normals[vertexCount + 0] = x0;
            normals[vertexCount + 1] = y0;
            normals[vertexCount + 2] = z0;

            normals[vertexCount + 3] = x1;
            normals[vertexCount + 4] = y1;
            normals[vertexCount + 5] = z1;

            normals[vertexCount + 6] = x2;
            normals[vertexCount + 7] = y2;
            normals[vertexCount + 8] = z2;

            texCoords[texCoordCount + 0] = s0;
            texCoords[texCoordCount + 1] = t0;
            texCoords[texCoordCount + 2] = s1;
            texCoords[texCoordCount + 3] = t0;
            texCoords[texCoordCount + 4] = s2;
            texCoords[texCoordCount + 5] = t1;

            if (top) {
                indices[indexCount + 0] = (char) (triangleCount + 0);
                indices[indexCount + 1] = (char) (triangleCount + 1);
                indices[indexCount + 2] = (char) (triangleCount + 2);
            } else {
                indices[indexCount + 0] = (char) (triangleCount + 1);
                indices[indexCount + 1] = (char) (triangleCount + 0);
                indices[indexCount + 2] = (char) (triangleCount + 2);
            }

            vertexCount += 9;
            texCoordCount += 6;
            indexCount += 3;
            triangleCount += 3;
        }

    }

    private void createBody(int numStacks, int numSlices) {
        for (int stack = 1; stack < numStacks - 1; stack++) {
            float stackPercentage0 = ((float) (stack) / numStacks);
            float stackPercentage1 = ((float) (stack + 1) / numStacks);

            float t0 = 1.0f - stackPercentage0;
            float t1 = 1.0f - stackPercentage1;
            float theta1 = stackPercentage0 * (float) Math.PI;
            float theta2 = stackPercentage1 * (float) Math.PI;
            float cosTheta1 = (float) Math.cos(theta1);
            float sinTheta1 = (float) Math.sin(theta1);
            float cosTheta2 = (float) Math.cos(theta2);
            float sinTheta2 = (float) Math.sin(theta2);

            for (int slice = 0; slice < numSlices; slice++) {
                float slicePercentage0 = ((float) (slice) / numSlices);
                float slicePercentage1 = ((float) (slice + 1) / numSlices);
                float phi1 = slicePercentage0 * 2.0f * (float) Math.PI;
                float phi2 = slicePercentage1 * 2.0f * (float) Math.PI;
                float s0 = slicePercentage0;
                float s1 = slicePercentage1;
                float cosPhi1 = (float) Math.cos(phi1);
                float sinPhi1 = (float) Math.sin(phi1);
                float cosPhi2 = (float) Math.cos(phi2);
                float sinPhi2 = (float) Math.sin(phi2);

                // 2-----3
                // | |
                // 0-----1
                float x0 = sinTheta1 * cosPhi1;
                float y0 = sinTheta1 * sinPhi1;
                float z0 = cosTheta1;

                float x1 = sinTheta1 * cosPhi2;
                float y1 = sinTheta1 * sinPhi2;
                float z1 = cosTheta1;

                float x2 = sinTheta2 * cosPhi1;
                float y2 = sinTheta2 * sinPhi1;
                float z2 = cosTheta2;

                float x3 = sinTheta2 * cosPhi2;
                float y3 = sinTheta2 * sinPhi2;
                float z3 = cosTheta2;

                vertices[vertexCount + 0] = x0;
                vertices[vertexCount + 1] = y0;
                vertices[vertexCount + 2] = z0;

                vertices[vertexCount + 3] = x1;
                vertices[vertexCount + 4] = y1;
                vertices[vertexCount + 5] = z1;

                vertices[vertexCount + 6] = x2;
                vertices[vertexCount + 7] = y2;
                vertices[vertexCount + 8] = z2;

                vertices[vertexCount + 9] = x3;
                vertices[vertexCount + 10] = y3;
                vertices[vertexCount + 11] = z3;

                normals[vertexCount + 0] = x0;
                normals[vertexCount + 1] = y0;
                normals[vertexCount + 2] = z0;

                normals[vertexCount + 3] = x1;
                normals[vertexCount + 4] = y1;
                normals[vertexCount + 5] = z1;

                normals[vertexCount + 6] = x2;
                normals[vertexCount + 7] = y2;
                normals[vertexCount + 8] = z2;

                normals[vertexCount + 9] = x3;
                normals[vertexCount + 10] = y3;
                normals[vertexCount + 11] = z3;

                texCoords[texCoordCount + 0] = s0;
                texCoords[texCoordCount + 1] = t0;
                texCoords[texCoordCount + 2] = s1;
                texCoords[texCoordCount + 3] = t0;
                texCoords[texCoordCount + 4] = s0;
                texCoords[texCoordCount + 5] = t1;
                texCoords[texCoordCount + 6] = s1;
                texCoords[texCoordCount + 7] = t1;

                // 0, 1, 2
                // 2, 1, 3
                indices[indexCount + 0] = (char) (triangleCount + 0);
                indices[indexCount + 1] = (char) (triangleCount + 1);
                indices[indexCount + 2] = (char) (triangleCount + 2);
                indices[indexCount + 3] = (char) (triangleCount + 2);
                indices[indexCount + 4] = (char) (triangleCount + 1);
                indices[indexCount + 5] = (char) (triangleCount + 3);

                vertexCount += 12;
                texCoordCount += 8;
                indexCount += 6;
                triangleCount += 4;
            }
        }

    }

}
