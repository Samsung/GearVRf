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

import java.util.concurrent.Future;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Log;

public class GVRSphereSceneObject extends GVRSceneObject {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(GVRSphereSceneObject.class);

    private static final int STACK_NUMBER = 18;
    private static final int SLICE_NUMBER = 36;

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
     * The sphere's triangles and normals are facing out and the same texture
     * will be applied to each side of the sphere.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRSphereSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        generateSphereObject(gvrContext, STACK_NUMBER, SLICE_NUMBER, true,
                new GVRMaterial(gvrContext));
    }

    /**
     * Constructs a sphere scene object with a radius of 1 and 18 stacks, and 36
     * slices.
     * 
     * The sphere's triangles and normals are facing either in or out and the
     * same texture will be applied to each side of the sphere.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     */
    public GVRSphereSceneObject(GVRContext gvrContext, boolean facingOut) {
        super(gvrContext);

        generateSphereObject(gvrContext, STACK_NUMBER, SLICE_NUMBER, facingOut,
                new GVRMaterial(gvrContext));
    }

    /**
     * Constructs a sphere scene object with a radius of 1 and 18 stacks, and 36
     * slices.
     * 
     * The sphere's triangles and normals are facing either in or out and the
     * same texture will be applied to each side of the sphere.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param futureTexture
     *            the texture for the sphere. {@code Future<GVRTexture>} is used
     *            here for asynchronously loading the texture.
     */
    public GVRSphereSceneObject(GVRContext gvrContext, boolean facingOut,
            Future<GVRTexture> futureTexture) {
        super(gvrContext);

        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(futureTexture);
        generateSphereObject(gvrContext, STACK_NUMBER, SLICE_NUMBER, facingOut,
                material);
    }

    /**
     * Constructs a sphere scene object with a radius of 1 and 18 stacks, and 36
     * slices.
     * 
     * The sphere's triangles and normals are facing either in or out and the
     * same material will be applied to each side of the sphere.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param material
     *            the material for the sphere.
     */
    public GVRSphereSceneObject(GVRContext gvrContext, boolean facingOut,
            GVRMaterial material) {
        super(gvrContext);

        generateSphereObject(gvrContext, STACK_NUMBER, SLICE_NUMBER, facingOut,
                material);
    }

    /**
     * Constructs a sphere scene object with a radius of 1 and user specified
     * stack and slice numbers.
     * 
     * The sphere's triangles and normals are facing either in or out and the
     * same material will be applied to each side of the sphere.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param stackNumber
     *            the number of stacks for the sphere. It should be equal or
     *            greater than 3.
     * 
     * @param sliceNumber
     *            the number of slices for the sphere. It should be equal or
     *            greater than 4.
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param material
     *            the material for the sphere.
     */
    public GVRSphereSceneObject(GVRContext gvrContext, int stackNumber,
            int sliceNumber, boolean facingOut, GVRMaterial material) {
        super(gvrContext);

        // assert sliceNumber>=4
        if (sliceNumber < 4) {
            throw new IllegalArgumentException(
                    "Slice number should be equal or greater than 4.");
        }

        // assert stackNumber>=3
        if (stackNumber < 3) {
            throw new IllegalArgumentException(
                    "Stack number should be equal or greater than 3.");
        }

        generateSphereObject(gvrContext, stackNumber, sliceNumber, facingOut,
                material);
    }

    /**
     * Constructs a sphere scene object with a radius of 1 and user specified
     * stack and slice numbers. The sphere is subdivided into MxN meshes, where M=sliceSegmengNumber and N=(stackSegmentNumber+2) are specified by user.
     * 
     * The sphere's triangles and normals are facing either in or out and the
     * same material will be applied to each side of the sphere.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param stackNumber
     *            the number of stacks for the sphere. It should be equal or
     *            greater than 3.
     * 
     * @param sliceNumber
     *            the number of slices for the sphere. It should be equal or
     *            greater than 4.
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param material
     *            the material for the sphere.
     * 
     * @param stackSegmentNumber
     *            the segment number along vertical direction (i.e. stacks).
     *            Note neither top cap nor bottom cap are subdivided along
     *            vertical direction. So number of stacks in body part (i.e.
     *            stackNumber-2) should be divisible by stackSegmentNumber.
     * 
     * @param sliceSegmentNumber
     *            the segment number along horizontal direction (i.e. slices).
     *            Number of slices (i.e. sliceNumber) should be divisible by
     *            sliceSegmentNumber.
     */
    public GVRSphereSceneObject(GVRContext gvrContext, int stackNumber,
            int sliceNumber, boolean facingOut, GVRMaterial material,
            int stackSegmentNumber, int sliceSegmentNumber) {
        super(gvrContext);

        // assert stackNumber>=3
        if (stackNumber < 3) {
            throw new IllegalArgumentException(
                    "Stack number should be equal or greater than 3.");
        }

        // assert sliceNumber>=4
        if (sliceNumber < 4) {
            throw new IllegalArgumentException(
                    "Slice number should be equal or greater than 4.");
        }

        // assert for valid stackSegmentNumber
        if ((stackNumber - 2) % stackSegmentNumber != 0) {
            throw new IllegalArgumentException(
                    "(stackNumber-2) should be divisible by stackSegmentNumber.");
        }

        // assert for valid sliceSegmentNumber
        if (sliceNumber % sliceSegmentNumber != 0) {
            throw new IllegalArgumentException(
                    "sliceNumber should be divisible by sliceSegmentNumber.");
        }

        generateComplexSphereObject(gvrContext, stackNumber, sliceNumber,
                facingOut, material, stackSegmentNumber, sliceSegmentNumber);
    }

    private void generateSphereObject(GVRContext gvrContext, int stackNumber,
            int sliceNumber, boolean facingOut, GVRMaterial material) {
        generateSphere(stackNumber, sliceNumber, facingOut);

        GVRMesh mesh = new GVRMesh(gvrContext);
        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setTriangles(indices);

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
        renderData.setMaterial(material);
    }

    private void generateSphere(int stackNumber, int sliceNumber,
            boolean facingOut) {
        int capVertexNumber = 3 * sliceNumber;
        int bodyVertexNumber = 4 * sliceNumber * (stackNumber - 2);
        int vertexNumber = (2 * capVertexNumber) + bodyVertexNumber;
        int triangleNumber = (2 * capVertexNumber)
                + (6 * sliceNumber * (stackNumber - 2));

        vertices = new float[3 * vertexNumber];
        normals = new float[3 * vertexNumber];
        texCoords = new float[2 * vertexNumber];
        indices = new char[triangleNumber];

        // bottom cap
        createCap(stackNumber, sliceNumber, false, facingOut);

        // body
        createBody(stackNumber, sliceNumber, facingOut);

        // top cap
        createCap(stackNumber, sliceNumber, true, facingOut);
    }

    private void createCap(int stackNumber, int sliceNumber, boolean top,
            boolean facingOut) {

        float stackPercentage0;
        float stackPercentage1;

        if (!top) {
            stackPercentage0 = ((float) (stackNumber - 1) / stackNumber);
            stackPercentage1 = 1.0f;

        } else {
            stackPercentage0 = (1.0f / stackNumber);
            stackPercentage1 = 0.0f;
        }

        float t0 = stackPercentage0;
        float t1 = stackPercentage1;
        double theta0 = stackPercentage0 * Math.PI;
        double theta1 = stackPercentage1 * Math.PI;
        double cosTheta0 = Math.cos(theta0);
        double sinTheta0 = Math.sin(theta0);
        double cosTheta1 = Math.cos(theta1);
        double sinTheta1 = Math.sin(theta1);

        for (int slice = 0; slice < sliceNumber; slice++) {
            float slicePercentage0 = ((float) (slice) / sliceNumber);
            float slicePercentage1 = ((float) (slice + 1) / sliceNumber);
            double phi0 = slicePercentage0 * 2.0 * Math.PI;
            double phi1 = slicePercentage1 * 2.0 * Math.PI;
            float s0, s1;
            if (facingOut) {
                s0 = 1 - slicePercentage0;
                s1 = 1 - slicePercentage1;
            } else {
                s0 = slicePercentage0;
                s1 = slicePercentage1;
            }
            float s2 = (s0 + s1) / 2.0f;
            double cosPhi0 = Math.cos(phi0);
            double sinPhi0 = Math.sin(phi0);
            double cosPhi1 = Math.cos(phi1);
            double sinPhi1 = Math.sin(phi1);

            float x0 = (float) (sinTheta0 * cosPhi0);
            float y0 = (float) cosTheta0;
            float z0 = (float) (sinTheta0 * sinPhi0);

            float x1 = (float) (sinTheta0 * cosPhi1);
            float y1 = (float) cosTheta0;
            float z1 = (float) (sinTheta0 * sinPhi1);

            float x2 = (float) (sinTheta1 * cosPhi0);
            float y2 = (float) cosTheta1;
            float z2 = (float) (sinTheta1 * sinPhi0);

            vertices[vertexCount + 0] = x0;
            vertices[vertexCount + 1] = y0;
            vertices[vertexCount + 2] = z0;

            vertices[vertexCount + 3] = x1;
            vertices[vertexCount + 4] = y1;
            vertices[vertexCount + 5] = z1;

            vertices[vertexCount + 6] = x2;
            vertices[vertexCount + 7] = y2;
            vertices[vertexCount + 8] = z2;

            if (facingOut) {
                normals[vertexCount + 0] = x0;
                normals[vertexCount + 1] = y0;
                normals[vertexCount + 2] = z0;

                normals[vertexCount + 3] = x1;
                normals[vertexCount + 4] = y1;
                normals[vertexCount + 5] = z1;

                normals[vertexCount + 6] = x2;
                normals[vertexCount + 7] = y2;
                normals[vertexCount + 8] = z2;
            } else {
                normals[vertexCount + 0] = -x0;
                normals[vertexCount + 1] = -y0;
                normals[vertexCount + 2] = -z0;

                normals[vertexCount + 3] = -x1;
                normals[vertexCount + 4] = -y1;
                normals[vertexCount + 5] = -z1;

                normals[vertexCount + 6] = -x2;
                normals[vertexCount + 7] = -y2;
                normals[vertexCount + 8] = -z2;
            }

            texCoords[texCoordCount + 0] = s0;
            texCoords[texCoordCount + 1] = t0;
            texCoords[texCoordCount + 2] = s1;
            texCoords[texCoordCount + 3] = t0;
            texCoords[texCoordCount + 4] = s2;
            texCoords[texCoordCount + 5] = t1;

            if ((facingOut && top) || (!facingOut && !top)) {
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

    private void createBody(int stackNumber, int sliceNumber, boolean facingOut) {
        for (int stack = 1; stack < stackNumber - 1; stack++) {
            float stackPercentage0 = ((float) (stack) / stackNumber);
            float stackPercentage1 = ((float) (stack + 1) / stackNumber);

            float t0 = stackPercentage0;
            float t1 = stackPercentage1;

            double theta0 = stackPercentage0 * Math.PI;
            double theta1 = stackPercentage1 * Math.PI;
            double cosTheta0 = Math.cos(theta0);
            double sinTheta0 = Math.sin(theta0);
            double cosTheta1 = Math.cos(theta1);
            double sinTheta1 = Math.sin(theta1);

            for (int slice = 0; slice < sliceNumber; slice++) {
                float slicePercentage0 = ((float) (slice) / sliceNumber);
                float slicePercentage1 = ((float) (slice + 1) / sliceNumber);
                double phi0 = slicePercentage0 * 2.0 * Math.PI;
                double phi1 = slicePercentage1 * 2.0 * Math.PI;
                float s0, s1;
                if (facingOut) {
                    s0 = 1.0f - slicePercentage0;
                    s1 = 1.0f - slicePercentage1;
                } else {
                    s0 = slicePercentage0;
                    s1 = slicePercentage1;
                }
                double cosPhi0 = Math.cos(phi0);
                double sinPhi0 = Math.sin(phi0);
                double cosPhi1 = Math.cos(phi1);
                double sinPhi1 = Math.sin(phi1);

                float x0 = (float) (sinTheta0 * cosPhi0);
                float y0 = (float) cosTheta0;
                float z0 = (float) (sinTheta0 * sinPhi0);

                float x1 = (float) (sinTheta0 * cosPhi1);
                float y1 = (float) cosTheta0;
                float z1 = (float) (sinTheta0 * sinPhi1);

                float x2 = (float) (sinTheta1 * cosPhi0);
                float y2 = (float) cosTheta1;
                float z2 = (float) (sinTheta1 * sinPhi0);

                float x3 = (float) (sinTheta1 * cosPhi1);
                float y3 = (float) cosTheta1;
                float z3 = (float) (sinTheta1 * sinPhi1);

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

                if (facingOut) {
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
                } else {
                    normals[vertexCount + 0] = -x0;
                    normals[vertexCount + 1] = -y0;
                    normals[vertexCount + 2] = -z0;

                    normals[vertexCount + 3] = -x1;
                    normals[vertexCount + 4] = -y1;
                    normals[vertexCount + 5] = -z1;

                    normals[vertexCount + 6] = -x2;
                    normals[vertexCount + 7] = -y2;
                    normals[vertexCount + 8] = -z2;

                    normals[vertexCount + 9] = -x3;
                    normals[vertexCount + 10] = -y3;
                    normals[vertexCount + 11] = -z3;
                }

                texCoords[texCoordCount + 0] = s0;
                texCoords[texCoordCount + 1] = t0;
                texCoords[texCoordCount + 2] = s1;
                texCoords[texCoordCount + 3] = t0;
                texCoords[texCoordCount + 4] = s0;
                texCoords[texCoordCount + 5] = t1;
                texCoords[texCoordCount + 6] = s1;
                texCoords[texCoordCount + 7] = t1;

                // one quad looking from outside toward center
                //
                // @formatter:off
                //
                //     s1 --> s0
                //
                // t0   1-----0
                //  |   |     |
                //  v   |     |
                // t1   3-----2
                //     
                // @formatter:on
                //
                // Note that tex_coord t increase from top to bottom because the
                // texture image is loaded upside down.
                if (facingOut) {
                    indices[indexCount + 0] = (char) (triangleCount + 0);
                    indices[indexCount + 1] = (char) (triangleCount + 1);
                    indices[indexCount + 2] = (char) (triangleCount + 2);

                    indices[indexCount + 3] = (char) (triangleCount + 2);
                    indices[indexCount + 4] = (char) (triangleCount + 1);
                    indices[indexCount + 5] = (char) (triangleCount + 3);
                } else {
                    indices[indexCount + 0] = (char) (triangleCount + 0);
                    indices[indexCount + 1] = (char) (triangleCount + 2);
                    indices[indexCount + 2] = (char) (triangleCount + 1);

                    indices[indexCount + 3] = (char) (triangleCount + 2);
                    indices[indexCount + 4] = (char) (triangleCount + 3);
                    indices[indexCount + 5] = (char) (triangleCount + 1);
                }

                vertexCount += 12;
                texCoordCount += 8;
                indexCount += 6;
                triangleCount += 4;
            }
        }

    }

    private void generateComplexSphereObject(GVRContext gvrContext,
            int stackNumber, int sliceNumber, boolean facingOut,
            GVRMaterial material, int stackSegmentNumber, int sliceSegmentNumber) {
        // bottom cap
        createComplexCap(gvrContext, stackNumber, sliceNumber, false,
                facingOut, material, sliceSegmentNumber);

        // body
        createComplexBody(gvrContext, stackNumber, sliceNumber, facingOut,
                material, stackSegmentNumber, sliceSegmentNumber);

        // top cap
        createComplexCap(gvrContext, stackNumber, sliceNumber, true, facingOut,
                material, sliceSegmentNumber);

        // attached an empty renderData for parent object, so that we can set
        // some common properties
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMaterial(material);
        attachRenderData(renderData);
    }

    private void createComplexCap(GVRContext gvrContext, int stackNumber,
            int sliceNumber, boolean top, boolean facingOut,
            GVRMaterial material, int sliceSegmentNumber) {
        int slicePerSegment = sliceNumber / sliceSegmentNumber;
        int vertexNumber = 3 * slicePerSegment;
        vertices = new float[3 * vertexNumber];
        normals = new float[3 * vertexNumber];
        texCoords = new float[2 * vertexNumber];
        indices = new char[vertexNumber];

        vertexCount = 0;
        texCoordCount = 0;
        indexCount = 0;
        triangleCount = 0;

        int sliceCounter = 0;

        float stackPercentage0;
        float stackPercentage1;

        if (!top) {
            stackPercentage0 = ((float) (stackNumber - 1) / stackNumber);
            stackPercentage1 = 1.0f;

        } else {
            stackPercentage0 = (1.0f / stackNumber);
            stackPercentage1 = 0.0f;
        }

        float t0 = stackPercentage0;
        float t1 = stackPercentage1;
        double theta0 = stackPercentage0 * Math.PI;
        double theta1 = stackPercentage1 * Math.PI;
        double cosTheta0 = Math.cos(theta0);
        double sinTheta0 = Math.sin(theta0);
        double cosTheta1 = Math.cos(theta1);
        double sinTheta1 = Math.sin(theta1);

        for (int slice = 0; slice < sliceNumber; slice++) {
            float slicePercentage0 = ((float) (slice) / sliceNumber);
            float slicePercentage1 = ((float) (slice + 1) / sliceNumber);
            double phi0 = slicePercentage0 * 2.0 * Math.PI;
            double phi1 = slicePercentage1 * 2.0 * Math.PI;
            float s0, s1;
            if (facingOut) {
                s0 = 1 - slicePercentage0;
                s1 = 1 - slicePercentage1;
            } else {
                s0 = slicePercentage0;
                s1 = slicePercentage1;
            }
            float s2 = (s0 + s1) / 2.0f;
            double cosPhi0 = Math.cos(phi0);
            double sinPhi0 = Math.sin(phi0);
            double cosPhi1 = Math.cos(phi1);
            double sinPhi1 = Math.sin(phi1);

            float x0 = (float) (sinTheta0 * cosPhi0);
            float y0 = (float) cosTheta0;
            float z0 = (float) (sinTheta0 * sinPhi0);

            float x1 = (float) (sinTheta0 * cosPhi1);
            float y1 = (float) cosTheta0;
            float z1 = (float) (sinTheta0 * sinPhi1);

            float x2 = (float) (sinTheta1 * cosPhi0);
            float y2 = (float) cosTheta1;
            float z2 = (float) (sinTheta1 * sinPhi0);

            vertices[vertexCount + 0] = x0;
            vertices[vertexCount + 1] = y0;
            vertices[vertexCount + 2] = z0;

            vertices[vertexCount + 3] = x1;
            vertices[vertexCount + 4] = y1;
            vertices[vertexCount + 5] = z1;

            vertices[vertexCount + 6] = x2;
            vertices[vertexCount + 7] = y2;
            vertices[vertexCount + 8] = z2;

            if (facingOut) {
                normals[vertexCount + 0] = x0;
                normals[vertexCount + 1] = y0;
                normals[vertexCount + 2] = z0;

                normals[vertexCount + 3] = x1;
                normals[vertexCount + 4] = y1;
                normals[vertexCount + 5] = z1;

                normals[vertexCount + 6] = x2;
                normals[vertexCount + 7] = y2;
                normals[vertexCount + 8] = z2;
            } else {
                normals[vertexCount + 0] = -x0;
                normals[vertexCount + 1] = -y0;
                normals[vertexCount + 2] = -z0;

                normals[vertexCount + 3] = -x1;
                normals[vertexCount + 4] = -y1;
                normals[vertexCount + 5] = -z1;

                normals[vertexCount + 6] = -x2;
                normals[vertexCount + 7] = -y2;
                normals[vertexCount + 8] = -z2;
            }

            texCoords[texCoordCount + 0] = s0;
            texCoords[texCoordCount + 1] = t0;
            texCoords[texCoordCount + 2] = s1;
            texCoords[texCoordCount + 3] = t0;
            texCoords[texCoordCount + 4] = s2;
            texCoords[texCoordCount + 5] = t1;

            if ((facingOut && top) || (!facingOut && !top)) {
                indices[indexCount + 0] = (char) (triangleCount + 1);
                indices[indexCount + 1] = (char) (triangleCount + 0);
                indices[indexCount + 2] = (char) (triangleCount + 2);
            } else {
                indices[indexCount + 0] = (char) (triangleCount + 0);
                indices[indexCount + 1] = (char) (triangleCount + 1);
                indices[indexCount + 2] = (char) (triangleCount + 2);
            }

            sliceCounter++;
            if (sliceCounter == slicePerSegment) {
                GVRMesh mesh = new GVRMesh(gvrContext);
                mesh.setVertices(vertices);
                mesh.setNormals(normals);
                mesh.setTexCoords(texCoords);
                mesh.setTriangles(indices);
                GVRSceneObject childObject = new GVRSceneObject(gvrContext,
                        mesh);
                childObject.getRenderData().setMaterial(material);
                addChildObject(childObject);

                sliceCounter = 0;

                vertexCount = 0;
                texCoordCount = 0;
                indexCount = 0;
                triangleCount = 0;
            } else {
                vertexCount += 9;
                texCoordCount += 6;
                indexCount += 3;
                triangleCount += 3;
            }
        }
    }

    private void createComplexBody(GVRContext gvrContext, int stackNumber,
            int sliceNumber, boolean facingOut, GVRMaterial material,
            int stackSegmentNumber, int sliceSegmentNumber) {
        int stackPerSegment = (stackNumber - 2) / stackSegmentNumber;
        int slicePerSegment = sliceNumber / sliceSegmentNumber;

        int vertexNumber = 4 * stackPerSegment * slicePerSegment;
        int triangleNumber = 6 * stackPerSegment * slicePerSegment;
        vertices = new float[3 * vertexNumber];
        normals = new float[3 * vertexNumber];
        texCoords = new float[2 * vertexNumber];
        indices = new char[triangleNumber];

        vertexCount = 0;
        texCoordCount = 0;
        indexCount = 0;
        triangleCount = 0;

        for (int stackSegment = 0; stackSegment < stackSegmentNumber; stackSegment++) {
            for (int sliceSegment = 0; sliceSegment < sliceSegmentNumber; sliceSegment++) {
                for (int stack = stackSegment * stackPerSegment + 1; stack < (stackSegment+1) * stackPerSegment + 1; stack++) {
    
                    float stackPercentage0 = ((float) (stack) / stackNumber);
                    float stackPercentage1 = ((float) (stack + 1) / stackNumber);
        
                    float t0 = stackPercentage0;
                    float t1 = stackPercentage1;
        
                    double theta0 = stackPercentage0 * Math.PI;
                    double theta1 = stackPercentage1 * Math.PI;
                    double cosTheta0 = Math.cos(theta0);
                    double sinTheta0 = Math.sin(theta0);
                    double cosTheta1 = Math.cos(theta1);
                    double sinTheta1 = Math.sin(theta1);
        
                    for (int slice = sliceSegment * slicePerSegment; slice < (sliceSegment+1) * slicePerSegment; slice++) {
                        float slicePercentage0 = ((float) (slice) / sliceNumber);
                        float slicePercentage1 = ((float) (slice + 1) / sliceNumber);
                        double phi0 = slicePercentage0 * 2.0 * Math.PI;
                        double phi1 = slicePercentage1 * 2.0 * Math.PI;
                        float s0, s1;
                        if (facingOut) {
                            s0 = 1.0f - slicePercentage0;
                            s1 = 1.0f - slicePercentage1;
                        } else {
                            s0 = slicePercentage0;
                            s1 = slicePercentage1;
                        }
                        double cosPhi0 = Math.cos(phi0);
                        double sinPhi0 = Math.sin(phi0);
                        double cosPhi1 = Math.cos(phi1);
                        double sinPhi1 = Math.sin(phi1);
        
                        float x0 = (float) (sinTheta0 * cosPhi0);
                        float y0 = (float) cosTheta0;
                        float z0 = (float) (sinTheta0 * sinPhi0);
        
                        float x1 = (float) (sinTheta0 * cosPhi1);
                        float y1 = (float) cosTheta0;
                        float z1 = (float) (sinTheta0 * sinPhi1);
        
                        float x2 = (float) (sinTheta1 * cosPhi0);
                        float y2 = (float) cosTheta1;
                        float z2 = (float) (sinTheta1 * sinPhi0);
        
                        float x3 = (float) (sinTheta1 * cosPhi1);
                        float y3 = (float) cosTheta1;
                        float z3 = (float) (sinTheta1 * sinPhi1);
        
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
        
                        if (facingOut) {
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
                        } else {
                            normals[vertexCount + 0] = -x0;
                            normals[vertexCount + 1] = -y0;
                            normals[vertexCount + 2] = -z0;
        
                            normals[vertexCount + 3] = -x1;
                            normals[vertexCount + 4] = -y1;
                            normals[vertexCount + 5] = -z1;
        
                            normals[vertexCount + 6] = -x2;
                            normals[vertexCount + 7] = -y2;
                            normals[vertexCount + 8] = -z2;
        
                            normals[vertexCount + 9] = -x3;
                            normals[vertexCount + 10] = -y3;
                            normals[vertexCount + 11] = -z3;
                        }
        
                        texCoords[texCoordCount + 0] = s0;
                        texCoords[texCoordCount + 1] = t0;
                        texCoords[texCoordCount + 2] = s1;
                        texCoords[texCoordCount + 3] = t0;
                        texCoords[texCoordCount + 4] = s0;
                        texCoords[texCoordCount + 5] = t1;
                        texCoords[texCoordCount + 6] = s1;
                        texCoords[texCoordCount + 7] = t1;
        
                        // one quad looking from outside toward center
                        //
                        // @formatter:off
                        //
                        //     s1 --> s0
                        //
                        // t0   1-----0
                        //  |   |     |
                        //  v   |     |
                        // t1   3-----2
                        //     
                        // @formatter:on
                        //
                        // Note that tex_coord t increase from top to bottom because the
                        // texture image is loaded upside down.
                        if (facingOut) {
                            indices[indexCount + 0] = (char) (triangleCount + 0);
                            indices[indexCount + 1] = (char) (triangleCount + 1);
                            indices[indexCount + 2] = (char) (triangleCount + 2);
        
                            indices[indexCount + 3] = (char) (triangleCount + 2);
                            indices[indexCount + 4] = (char) (triangleCount + 1);
                            indices[indexCount + 5] = (char) (triangleCount + 3);
                        } else {
                            indices[indexCount + 0] = (char) (triangleCount + 0);
                            indices[indexCount + 1] = (char) (triangleCount + 2);
                            indices[indexCount + 2] = (char) (triangleCount + 1);
        
                            indices[indexCount + 3] = (char) (triangleCount + 2);
                            indices[indexCount + 4] = (char) (triangleCount + 3);
                            indices[indexCount + 5] = (char) (triangleCount + 1);
                        }
        
                        vertexCount += 12;
                        texCoordCount += 8;
                        indexCount += 6;
                        triangleCount += 4;
                    }
                }

                GVRMesh mesh = new GVRMesh(gvrContext);
                mesh.setVertices(vertices);
                mesh.setNormals(normals);
                mesh.setTexCoords(texCoords);
                mesh.setTriangles(indices);
                GVRSceneObject childObject = new GVRSceneObject(gvrContext,
                        mesh);
                childObject.getRenderData().setMaterial(material);
                addChildObject(childObject);

                vertexCount = 0;
                texCoordCount = 0;
                indexCount = 0;
                triangleCount = 0;
            }
        }
    }
}
