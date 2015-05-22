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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;

public class GVRCylinderSceneObject extends GVRSceneObject {

    private static final String TAG = "GVRCylinderSceneObject";
    private static final int STACK_NUMBER = 10;
    private static final int SLICE_NUMBER = 36;
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

        generateCylinder(BASE_RADIUS, TOP_RADIUS, HEIGHT, STACK_NUMBER,
                SLICE_NUMBER);

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
     * @param stackNumber
     *            number of quads high to make the cylinder.
     * @param sliceNumber
     *            number of quads around to make the cylinder.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, float bottomRadius,
            float topRadius, float height, int stackNumber, int sliceNumber) {
        super(gvrContext);
        // assert height, numStacks, numSlices > 0
        if (height <= 0 || stackNumber <= 0 || sliceNumber <= 0) {
            throw new IllegalArgumentException(
                    "height, numStacks, and numSlices must be > 0.  Values passed were: height="
                            + height + ", numStacks=" + stackNumber
                            + ", numSlices=" + sliceNumber);
        }

        // assert numCaps > 0
        if (bottomRadius <= 0 && topRadius <= 0) {
            throw new IllegalArgumentException(
                    "bottomRadius and topRadius must be >= 0 and at least one of bottomRadius or topRadius must be > 0.  Values passed were: bottomRadius="
                            + bottomRadius + ", topRadius=" + topRadius);
        }

        generateCylinder(bottomRadius, topRadius, height, stackNumber,
                sliceNumber);

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
            float height, int stackNumber, int sliceNumber) {

        int capNumber = 2;
        if (bottomRadius == 0) {
            capNumber--;
        }

        if (topRadius == 0) {
            capNumber--;
        }

        int capVertexNumber = 3 * sliceNumber;
        int bodyVertexNumber = 4 * sliceNumber * stackNumber;
        int vertexNumber = (capNumber * capVertexNumber) + bodyVertexNumber;
        int triangleNumber = (capNumber * capVertexNumber)
                + (6 * sliceNumber * stackNumber);
        float halfHeight = height / 2.0f;

        vertices = new float[3 * vertexNumber];
        normals = new float[3 * vertexNumber];
        texCoords = new float[2 * triangleNumber];
        indices = new char[triangleNumber];

        // top cap
        // 3 * numSlices
        if (topRadius > 0) {
            createCap(topRadius, halfHeight, sliceNumber, 1.0f);
        }

        // cylinder body
        // 4 * numSlices * numStacks
        createBody(bottomRadius, topRadius, height, stackNumber, sliceNumber);

        // bottom cap
        // 3 * numSlices
        if (bottomRadius > 0) {
            createCap(bottomRadius, -halfHeight, sliceNumber, -1.0f);
        }

    }

    private void createCap(float radius, float height, int sliceNumber,
            float normalDirection) {
        for (int slice = 0; slice < sliceNumber; slice++) {
            double theta0 = ((slice) / sliceNumber) * 2.0 * Math.PI;
            double theta1 = ((slice + 1) / sliceNumber) * 2.0 * Math.PI;

            float y = height;
            float x0 = (float) (radius * Math.cos(theta0));
            float z0 = (float) (radius * Math.sin(theta0));
            float x1 = (float) (radius * Math.cos(theta1));
            float z1 = (float) (radius * Math.sin(theta1));

            float s0 = 1.0f - ((float) (slice) / sliceNumber);
            float s1 = 1.0f - ((float) (slice + 1) / sliceNumber);
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
            normals[vertexCount + 1] = normalDirection;
            normals[vertexCount + 2] = 0.0f;
            normals[vertexCount + 3] = 0.0f;
            normals[vertexCount + 4] = normalDirection;
            normals[vertexCount + 5] = 0.0f;
            normals[vertexCount + 6] = 0.0f;
            normals[vertexCount + 7] = normalDirection;
            normals[vertexCount + 8] = 0.0f;

            texCoords[texCoordCount + 0] = s0;
            texCoords[texCoordCount + 1] = 0.0f;

            texCoords[texCoordCount + 2] = s1;
            texCoords[texCoordCount + 3] = 0.0f;

            texCoords[texCoordCount + 4] = s2;
            texCoords[texCoordCount + 5] = 1.0f;

            if (normalDirection > 0) {
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

    private void createBody(float bottomRadius, float topRadius, float height,
            int stackNumber, int sliceNumber) {
        float difference = bottomRadius - topRadius;
        float halfHeight = height / 2.0f;

        for (int stack = 0; stack < stackNumber; stack++) {

            int initVertexCount = vertexCount;

            float stackPercentage0 = ((float) (stack) / stackNumber);
            float stackPercentage1 = ((float) (stack + 1) / stackNumber);

            float t0 = 1.0f - stackPercentage0;
            float t1 = 1.0f - stackPercentage1;
            float y0 = -halfHeight + (stackPercentage0 * height);
            float y1 = -halfHeight + (stackPercentage1 * height);

            float nx, ny, nz;
            for (int slice = 0; slice < sliceNumber; slice++) {
                float slicePercentage0 = ((float) (slice) / sliceNumber);
                float slicePercentage1 = ((float) (slice + 1) / sliceNumber);
                double theta0 = slicePercentage0 * 2.0 * Math.PI;
                double theta1 = slicePercentage1 * 2.0 * Math.PI;
                double cosTheta0 = Math.cos(theta0);
                double sinTheta0 = Math.sin(theta0);
                double cosTheta1 = Math.cos(theta1);
                double sinTheta1 = Math.sin(theta1);

                float radius = (bottomRadius - (difference * stackPercentage0));
                float x0 = (float) (radius * cosTheta0);
                float z0 = (float) (-radius * sinTheta0);
                float x1 = (float) (radius * cosTheta1);
                float z1 = (float) (-radius * sinTheta1);

                radius = (bottomRadius - (difference * stackPercentage1));
                float x2 = (float) (radius * cosTheta0);
                float z2 = (float) (-radius * sinTheta0);
                float x3 = (float) (radius * cosTheta1);
                float z3 = (float) (-radius * sinTheta1);

                float s0 = slicePercentage0;
                float s1 = slicePercentage1;

                vertices[vertexCount + 0] = x0;
                vertices[vertexCount + 1] = y0;
                vertices[vertexCount + 2] = z0;

                vertices[vertexCount + 3] = x1;
                vertices[vertexCount + 4] = y0;
                vertices[vertexCount + 5] = z1;

                vertices[vertexCount + 6] = x2;
                vertices[vertexCount + 7] = y1;
                vertices[vertexCount + 8] = z2;

                vertices[vertexCount + 9] = x3;
                vertices[vertexCount + 10] = y1;
                vertices[vertexCount + 11] = z3;

                // calculate normal
                Vector3D v1 = new Vector3D(x1 - x0, 0, z1 - z0);
                Vector3D v2 = new Vector3D(x2 - x0, y1 - y0, z2 - z0);
                Vector3D v3 = v1.crossProduct(v2).normalize();

                nx = (float) v3.getX();
                ny = (float) v3.getY();
                nz = (float) v3.getZ();
                normals[vertexCount + 0] = nx;
                normals[vertexCount + 1] = ny;
                normals[vertexCount + 2] = nz;
                normals[vertexCount + 3] = nx;
                normals[vertexCount + 4] = ny;
                normals[vertexCount + 5] = nz;
                normals[vertexCount + 6] = nx;
                normals[vertexCount + 7] = ny;
                normals[vertexCount + 8] = nz;
                normals[vertexCount + 9] = nx;
                normals[vertexCount + 10] = ny;
                normals[vertexCount + 11] = nz;

                texCoords[texCoordCount + 0] = s0;
                texCoords[texCoordCount + 1] = t0;

                texCoords[texCoordCount + 2] = s1;
                texCoords[texCoordCount + 3] = t0;

                texCoords[texCoordCount + 4] = s0;
                texCoords[texCoordCount + 5] = t1;

                texCoords[texCoordCount + 6] = s1;
                texCoords[texCoordCount + 7] = t1;

                indices[indexCount + 0] = (char) (triangleCount + 0); // 0
                indices[indexCount + 1] = (char) (triangleCount + 1); // 1
                indices[indexCount + 2] = (char) (triangleCount + 2); // 2

                indices[indexCount + 3] = (char) (triangleCount + 2); // 2
                indices[indexCount + 4] = (char) (triangleCount + 1); // 1
                indices[indexCount + 5] = (char) (triangleCount + 3); // 3

                vertexCount += 12;
                texCoordCount += 8;
                indexCount += 6;
                triangleCount += 4;
            }

            for (int i = initVertexCount; i < vertexCount - 12; i += 12) {
                Vector3D v1 = new Vector3D(normals[i + 3], normals[i + 4],
                        normals[i + 5]);
                Vector3D v2 = new Vector3D(normals[i + 12], normals[i + 13],
                        normals[i + 14]);
                Vector3D v3 = v1.add(v2).normalize();
                nx = (float) v3.getX();
                ny = (float) v3.getY();
                nz = (float) v3.getZ();
                normals[i + 3] = nx;
                normals[i + 4] = ny;
                normals[i + 5] = nz;
                normals[i + 12] = nx;
                normals[i + 13] = ny;
                normals[i + 14] = nz;

                v1 = new Vector3D(normals[i + 9], normals[i + 10],
                        normals[i + 11]);
                v2 = new Vector3D(normals[i + 18], normals[i + 19],
                        normals[i + 20]);
                v3 = v1.add(v2).normalize();
                nx = (float) v3.getX();
                ny = (float) v3.getY();
                nz = (float) v3.getZ();
                normals[i + 9] = nx;
                normals[i + 10] = ny;
                normals[i + 11] = nz;
                normals[i + 18] = nx;
                normals[i + 19] = ny;
                normals[i + 20] = nz;
            }
            int i1 = vertexCount - 12;
            Vector3D v1 = new Vector3D(normals[i1 + 3], normals[i1 + 4],
                    normals[i1 + 5]);
            int i2 = initVertexCount;
            Vector3D v2 = new Vector3D(normals[i2 + 0], normals[i2 + 1],
                    normals[i2 + 2]);
            Vector3D v3 = v1.add(v2).normalize();
            nx = (float) v3.getX();
            ny = (float) v3.getY();
            nz = (float) v3.getZ();
            normals[i1 + 3] = nx;
            normals[i1 + 4] = ny;
            normals[i1 + 5] = nz;
            normals[i2 + 0] = nx;
            normals[i2 + 1] = ny;
            normals[i2 + 2] = nz;

            v1 = new Vector3D(normals[i1 + 9], normals[i1 + 10],
                    normals[i1 + 11]);
            v2 = new Vector3D(normals[i2 + 6], normals[i2 + 7], normals[i2 + 8]);
            v3 = v1.add(v2).normalize();
            nx = (float) v3.getX();
            ny = (float) v3.getY();
            nz = (float) v3.getZ();
            normals[i1 + 9] = nx;
            normals[i1 + 10] = ny;
            normals[i1 + 11] = nz;
            normals[i2 + 6] = nx;
            normals[i2 + 7] = ny;
            normals[i2 + 8] = nz;
        }
    }
}
