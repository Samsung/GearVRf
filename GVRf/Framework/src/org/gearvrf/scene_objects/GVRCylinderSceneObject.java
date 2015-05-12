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
    private static final int NUM_STACKS = 180;
    private static final int NUM_SLICES = 360;
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

    /**
     * Constructs a cylinder scene object.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRCylinderSceneObject(GVRContext gvrContext) {
        super(gvrContext);
        
        generateCylinder(BASE_RADIUS, TOP_RADIUS, HEIGHT, NUM_STACKS, NUM_SLICES);

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
    public GVRCylinderSceneObject(GVRContext gvrContext, float bottomRadius, float topRadius, float height, int numStacks, int numSlices) {
        super(gvrContext);
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

    private void generateCylinder(float bottomRadius, float topRadius, float height, int numStacks, int numSlices) {
        int capNumVertices = 3 * numSlices;
        int bodyNumVertices = 4 * numSlices * numStacks;
        int numVertices = (2 * capNumVertices) + bodyNumVertices;
        vertices = new float[3*numVertices];
        normals = new float[3*numVertices];
        texCoords = new float[2*numVertices];
        indices = new char[numVertices];
        float halfHeight = height / 2.0f;

        // top cap
        // 3 * numSlices
        createCap(numSlices, topRadius, halfHeight, 1.0f);

        // cylinder body
        // 4 * numSlices * numStacks
        createBody(numSlices, numStacks, -halfHeight, topRadius, bottomRadius);
        
        // bottom cap
        // 3 * numSlices
        createCap(numSlices, bottomRadius, -halfHeight, -1.0f);
    }

    private void createCap(int numSlices, float radius, float height, float normalDir) {
        for(int slice = 0; slice < numSlices; slice++) {
            float theta0 = ((float)(slice)/numSlices)*2.0f*(float)Math.PI;
            float theta1 = ((float)(slice+1)/numSlices)*2.0f*(float)Math.PI;

            float y = height;
            float x0 = radius * (float)Math.cos(theta0);
            float z0 = radius * (float)Math.sin(theta0);
            float x1 = radius * (float)Math.cos(theta1);
            float z1 = radius * (float)Math.sin(theta1);

            float s0 = 1.0f - ((float)(slice)/numSlices);
            float s1 = 1.0f - ((float)(slice+1)/numSlices);
            float s2 = (s0 + s1) / 2.0f;

            vertices[vertexCount+0] = x0;
            vertices[vertexCount+1] = y;
            vertices[vertexCount+2] = z0;
            vertices[vertexCount+3] = x1;
            vertices[vertexCount+4] = y;
            vertices[vertexCount+5] = z1;
            vertices[vertexCount+6] = 0.0f;
            vertices[vertexCount+7] = y;
            vertices[vertexCount+8] = 0.0f;

            normals[vertexCount+0] = 0.0f;
            normals[vertexCount+1] = normalDir; 
            normals[vertexCount+2] = 0.0f;
            normals[vertexCount+3] = 0.0f;
            normals[vertexCount+4] = normalDir; 
            normals[vertexCount+5] = 0.0f;
            normals[vertexCount+6] = 0.0f;
            normals[vertexCount+7] = normalDir; 
            normals[vertexCount+8] = 0.0f;

            texCoords[texCoordCount+0] = s0;
            texCoords[texCoordCount+1] = 0.0f;

            texCoords[texCoordCount+2] = s1;
            texCoords[texCoordCount+3] = 0.0f;

            texCoords[texCoordCount+4] = s2;
            texCoords[texCoordCount+5] = 1.0f;

            if(normalDir > 0) {
                indices[indexCount+0] = (char)(indexCount+0);
                indices[indexCount+1] = (char)(indexCount+1);
                indices[indexCount+2] = (char)(indexCount+2);
            } else {
                indices[indexCount+0] = (char)(indexCount+1);
                indices[indexCount+1] = (char)(indexCount+0);
                indices[indexCount+2] = (char)(indexCount+2);
            }

            vertexCount += 9;
            texCoordCount += 6;
            indexCount += 3;

        }
    }

    private void createBody(int numSlices, int numStacks, float height, float topRadius, float bottomRadius) {
        float difference = bottomRadius - topRadius;

        for(int stack = 0; stack < numStacks; stack++) {
            float t0 = ((float)(stack)/numStacks);
            float t1 = ((float)(stack+1)/numStacks);
            float y0 = -height + (t0 * 2*height);
            float y1 = -height + (t1 * 2*height);

            for(int slice = 0; slice < numSlices; slice++) {
                float theta0 = ((float)(slice)/numSlices)*2.0f*(float)Math.PI;
                float theta1 = ((float)(slice+1)/numSlices)*2.0f*(float)Math.PI;

                float radius = (bottomRadius - (difference * t0));
                float x0 = radius * (float)Math.cos(theta0);
                float z0 = radius * (float)Math.sin(theta0);
                float x1 = radius * (float)Math.cos(theta1);
                float z1 = radius * (float)Math.sin(theta1);
                
                radius = (bottomRadius - (difference * t1));
                float x2 = radius * (float)Math.cos(theta0);
                float z2 = radius * (float)Math.sin(theta0);
                float x3 = radius * (float)Math.cos(theta1);
                float z3 = radius * (float)Math.sin(theta1);

                float s0 = 1.0f - ((float)(slice)/numSlices);
                float s1 = 1.0f - ((float)(slice+1)/numSlices);

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
                float height2 = 2*height;
                float length = (float) Math.sqrt(difference*difference + height2*height2);
                float ratio = height2 / length;
                float nx = (float) (ratio * Math.sin(theta0));
                float ny = (float) (ratio * Math.cos(theta0));
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

                texCoords[texCoordCount+0] = s0;
                texCoords[texCoordCount+1] = t0;

                texCoords[texCoordCount+2] = s1;
                texCoords[texCoordCount+3] = t0;

                texCoords[texCoordCount+4] = s0;
                texCoords[texCoordCount+5] = t1;

                texCoords[texCoordCount+6] = s1;
                texCoords[texCoordCount+7] = t1;

                indices[indexCount+0] = (char) (indexCount+0);
                indices[indexCount+1] = (char) (indexCount+1);
                indices[indexCount+2] = (char) (indexCount+2);

                indices[indexCount+3] = (char) (indexCount+2);
                indices[indexCount+4] = (char) (indexCount+1);
                indices[indexCount+5] = (char) (indexCount+3);

                vertexCount += 12;
                texCoordCount += 8;
                indexCount += 6;

            }
        }
    }

 }

