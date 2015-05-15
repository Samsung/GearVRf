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

public class GVRCylinderSceneObject extends GVRSceneObject {

    private static final String TAG = "GVRCylinderSceneObject";
    private static final int NUM_STACKS = 2;
    private static final int NUM_SLICES = 36;
    private static final float BASE_RADIUS = 0.5f;
    private static final float TOP_RADIUS = 0.5f;
    private static final float HEIGHT = 1.0f;

    private float[] vertices;
    private float[] normals;
    private float[] texCoords;
    private char[] indices;
    private int vertexCount = 0;
    private int texCoordCount = 0;
    private char indexCount = 0;
    private int triangleCount = 0;

    /**
     * Constructs a cylinder scene object with a height of 1, radius of 0.5,2
     * stacks, and 36 slices.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRCylinderSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        generateCylinder(BASE_RADIUS, TOP_RADIUS, HEIGHT, NUM_STACKS,
                NUM_SLICES);

        GVRMesh mesh = new GVRMesh(gvrContext);
        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setTriangles(indices);

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

    /**
     * Constructs a cylinder scene object.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param bottomRadius
     *            radius for the bottom of the cylinder
     * @param topRadius
     *            radius for the top of the cylinder
     * @param height
     *            height of the cylinder
     * @param numStacks
     *            number of quads high to make the cylinder.
     * @param numSlices
     *            number of quads around to make the cylinder.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, float bottomRadius,
            float topRadius, float height, int numStacks, int numSlices) {
        super(gvrContext);
        // assert height, numStacks, numSlices > 0
        if (height <= 0 || numStacks <= 0 || numSlices <= 0) {
            throw new IllegalArgumentException(
                    "height, numStacks, and numSlices must be > 0.  Values passed were: height="
                            + height + ", numStacks=" + numStacks
                            + ", numSlices=" + numSlices);
        }

        // assert numCaps > 0
        if (bottomRadius <= 0 && topRadius <= 0) {
            throw new IllegalArgumentException(
                    "bottomRadius and topRadius must be >= 0 and at least one of bottomRadius or topRadius must be > 0.  Values passed were: bottomRadius="
                            + bottomRadius + ", topRadius=" + topRadius);
        }

        generateCylinder(bottomRadius, topRadius, height, numStacks, numSlices);

        GVRMesh mesh = new GVRMesh(gvrContext);
        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setTriangles(indices);

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

    private void generateCylinder(float bottomRadius, float topRadius,
            float height, int numStacks, int numSlices) {

        int numCaps = 2;
        if (bottomRadius == 0) {
            numCaps--;
        }

        if (topRadius == 0) {
            numCaps--;
        }

        int capNumVertices = 3 * numSlices;
        int bodyNumVertices = 4 * numSlices * numStacks;
        int numVertices = (numCaps * capNumVertices) + bodyNumVertices;
        int numTriangles = (numCaps * capNumVertices)
                + (6 * numSlices * numStacks);
        float halfHeight = height / 2.0f;

        vertices = new float[3 * numVertices];
        normals = new float[3 * numVertices];
        texCoords = new float[2 * numVertices];
        indices = new char[numTriangles];

        // top cap
        // 3 * numSlices
        if (topRadius > 0) {
            createCap(topRadius, halfHeight, numSlices, 1.0f);
        }

        // cylinder body
        // 4 * numSlices * numStacks
        createBody(bottomRadius, topRadius, height, numStacks, numSlices);

        // bottom cap
        // 3 * numSlices
        if (bottomRadius > 0) {
            createCap(bottomRadius, -halfHeight, numSlices, -1.0f);
        }

    }

    private void createCap(float radius, float height, int numSlices,
            float normalDir) {
        for (int slice = 0; slice < numSlices; slice++) {
            float theta0 = ((float) (slice) / numSlices) * 2.0f
                    * (float) Math.PI;
            float theta1 = ((float) (slice + 1) / numSlices) * 2.0f
                    * (float) Math.PI;

            float y = height;
            float x0 = radius * (float) Math.cos(theta0);
            float z0 = radius * (float) Math.sin(theta0);
            float x1 = radius * (float) Math.cos(theta1);
            float z1 = radius * (float) Math.sin(theta1);

            float s0 = 1.0f - ((float) (slice) / numSlices);
            float s1 = 1.0f - ((float) (slice + 1) / numSlices);
            float s2 = (s0 + s1) / 2.0f;

            vertices[vertexCount + 0] = x0;
            vertices[vertexCount + 1] = y;
            vertices[vertexCount + 2] = z0;
            vertices[vertexCount + 3] = x1;
            vertices[vertexCount + 4] = y;
            vertices[vertexCount + 5] = z1;
            vertices[vertexCount + 6] = 0.0f;
            vertices[vertexCount + 7] = y;
            vertices[vertexCount + 8] = 0.0f;

            normals[vertexCount + 0] = 0.0f;
            normals[vertexCount + 1] = normalDir;
            normals[vertexCount + 2] = 0.0f;
            normals[vertexCount + 3] = 0.0f;
            normals[vertexCount + 4] = normalDir;
            normals[vertexCount + 5] = 0.0f;
            normals[vertexCount + 6] = 0.0f;
            normals[vertexCount + 7] = normalDir;
            normals[vertexCount + 8] = 0.0f;

            texCoords[texCoordCount + 0] = s0;
            texCoords[texCoordCount + 1] = 0.0f;

            texCoords[texCoordCount + 2] = s1;
            texCoords[texCoordCount + 3] = 0.0f;

            texCoords[texCoordCount + 4] = s2;
            texCoords[texCoordCount + 5] = 1.0f;

            if (normalDir > 0) {
                indices[indexCount + 0] = (char) (triangleCount + 1);
                indices[indexCount + 1] = (char) (triangleCount + 0);
                indices[indexCount + 2] = (char) (triangleCount + 2);
            } else {
                indices[indexCount + 0] = (char) (triangleCount + 0);
                indices[indexCount + 1] = (char) (triangleCount + 1);
                indices[indexCount + 2] = (char) (triangleCount + 2);
            }

            vertexCount += 9;
            texCoordCount += 6;
            indexCount += 3;
            triangleCount += 3;
        }
    }

    private void createBody(float bottomRadius, float topRadius, float height, int numStacks, int numSlices) {
        float difference = bottomRadius - topRadius;
        float halfHeight = height / 2.0f;

        for(int stack = 0; stack < numStacks; stack++) {
            float stackPercentage0 = ((float)(stack)/numStacks);
            float stackPercentage1 = ((float)(stack+1)/numStacks);

            float t0 = 1.0f - stackPercentage0;
            float t1 = 1.0f - stackPercentage1;
            float y0 = -halfHeight + (stackPercentage0 * height);
            float y1 = -halfHeight + (stackPercentage1 * height);

            for(int slice = 0; slice < numSlices; slice++) {
                float slicePercentage0 = ((float)(slice)/numSlices);
                float slicePercentage1 = ((float)(slice+1)/numSlices);
                float theta0 = slicePercentage0*2.0f*(float)Math.PI;
                float theta1 = slicePercentage1*2.0f*(float)Math.PI;
                float cosTheta0 = (float)Math.cos(theta0);
                float sinTheta0 = (float)Math.sin(theta0);
                float cosTheta1 = (float)Math.cos(theta1);
                float sinTheta1 = (float)Math.sin(theta1);

                float radius = (bottomRadius - (difference * stackPercentage0));
                float x0 = radius * cosTheta0;
                float z0 = -radius * sinTheta0;
                float x1 = radius * cosTheta1;
                float z1 = -radius * sinTheta1;

                radius = (bottomRadius - (difference * stackPercentage1));
                float x2 = radius * cosTheta0;
                float z2 = -radius * sinTheta0;
                float x3 = radius * cosTheta1;
                float z3 = -radius * sinTheta1;

                float s0 = slicePercentage0;
                float s1 = slicePercentage1;
                
                vertices[vertexCount+0] = x0;
                vertices[vertexCount+1] = y0;
                vertices[vertexCount+2] = z0;

                vertices[vertexCount+3] = x1;
                vertices[vertexCount+4] = y0;
                vertices[vertexCount+5] = z1;

                vertices[vertexCount+6] = x2;
                vertices[vertexCount+7] = y1;
                vertices[vertexCount+8] = z2;

                vertices[vertexCount+9]  = x3;
                vertices[vertexCount+10] = y1;
                vertices[vertexCount+11] = z3;

                // calculate normal
                float length = (float) Math.sqrt(difference*difference + height*height);
                float ratio = height / length;
                float nx = (float) (ratio * sinTheta0);
                float ny = (float) (ratio * cosTheta0);
                float nz = difference / length;
                normals[vertexCount+0] = nx; 
                normals[vertexCount+1] = ny; 
                normals[vertexCount+2] = nz;
                normals[vertexCount+3] = nx;
                normals[vertexCount+4] = ny; 
                normals[vertexCount+5] = nz;
                normals[vertexCount+6] = nx;
                normals[vertexCount+7] = ny; 
                normals[vertexCount+8] = nz;
                normals[vertexCount+9] = nx;
                normals[vertexCount+10] = ny;
                normals[vertexCount+11] = nz;

                texCoords[texCoordCount+0] = s0;
                texCoords[texCoordCount+1] = t0;

                texCoords[texCoordCount+2] = s1;
                texCoords[texCoordCount+3] = t0;

                texCoords[texCoordCount+4] = s0;
                texCoords[texCoordCount+5] = t1;

                texCoords[texCoordCount+6] = s1;
                texCoords[texCoordCount+7] = t1;

                indices[indexCount+0] = (char) (triangleCount+0); // 0 
                indices[indexCount+1] = (char) (triangleCount+1); // 1
                indices[indexCount+2] = (char) (triangleCount+2); // 2

                indices[indexCount+3] = (char) (triangleCount+2); // 2
                indices[indexCount+4] = (char) (triangleCount+1); // 1
                indices[indexCount+5] = (char) (triangleCount+3); // 3

                vertexCount += 12;
                texCoordCount += 8;
                indexCount += 6;
                triangleCount += 4;
            }
        }
    }
}
