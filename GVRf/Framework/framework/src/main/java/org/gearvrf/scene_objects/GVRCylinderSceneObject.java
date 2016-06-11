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

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Log;

public class GVRCylinderSceneObject extends GVRSceneObject {
    public static class CylinderParams {
        public float       Height = 1.0f;
        public float       TopRadius = 0.5f;
        public float       BottomRadius = 0.5f;
        public int         StackNumber = 10;
        public int         SliceNumber = 36;
        public boolean     FacingOut = true;
        public boolean     HasTopCap = true;
        public boolean     HasBottomCap = true;
        public GVRMaterial Material = null;
    };
    
    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(GVRCylinderSceneObject.class);

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
     * Constructs a cylinder scene object based on a set of parameters.
     *  Height          height of cylinder (default 1).
     *  TopRadius       radius of cylinder top (default 0.5).
     *  BottomRadius    radius of cylinder bottom (default 0.5).
     *  StackNumber     number of stacks in cylinder body (default 10).
     *  SliceNumber     number of slices in cylinder caps (default 36).
     *  FacingOut       true for normals facing out, false for normals facing in (default true).
     *  HasTopCap       true to include top cap, false to omit it (default true).
     *  HasBottomCap    true to include bottom cap, false to omit it (default true).
     *  Material        GVRMaterial to apply to cylinder (default null).
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param params    CylinderParams object with cylinder construction parameters.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, CylinderParams params) {
        super(gvrContext);
        generateCylinderObject(gvrContext, params);
    }
    
    /**
     * Constructs a cylinder scene object with a height of 1, radius of 0.5, 10
     * stacks, and 36 slices.
     * 
     * The cylinder's triangles and normals are facing out and the same texture
     * will be applied to top, bottom, and side of the cylinder.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRCylinderSceneObject(GVRContext gvrContext) {
        super(gvrContext);
        CylinderParams params = new CylinderParams();
        params.Material = new GVRMaterial(gvrContext);
        generateCylinderObject(gvrContext, params);
    }

    /**
     * Constructs a cylinder scene object with a height of 1, radius of 0.5, 10
     * stacks, and 36 slices.
     * 
     * The cylinder's triangles and normals are facing either in or out and the
     * same texture will be applied to top, bottom, and side of the cylinder.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, boolean facingOut) {
        super(gvrContext);
        CylinderParams params = new CylinderParams();
        params.Material = new GVRMaterial(gvrContext);
        params.FacingOut = facingOut;
        generateCylinderObject(gvrContext, params);
    }

    /**
     * Constructs a cylinder scene object with a height of 1, radius of 0.5, 10
     * stacks, and 36 slices.
     * 
     * The cylinder's triangles and normals are facing either in or out and the
     * same texture will be applied to top, bottom, and side of the cylinder.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param futureTexture
     *            the texture for the cylinder. {@code Future<GVRTexture>} is
     *            used here for asynchronously loading the texture.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, boolean facingOut,
            Future<GVRTexture> futureTexture) {
        super(gvrContext);
        CylinderParams params = new CylinderParams();
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(futureTexture);
        params.Material = material;
        params.FacingOut = facingOut;
        generateCylinderObject(gvrContext, params);
    }

    /**
     * Constructs a cylinder scene object with a height of 1, radius of 0.5, 10
     * stacks, and 36 slices.
     * 
     * The cylinder's triangles and normals are facing either in or out and the
     * same material will be applied to top, bottom, and side of the cylinder.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param material
     *            the material for the cylinder.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, boolean facingOut,
            GVRMaterial material) {
        super(gvrContext);
        CylinderParams params = new CylinderParams();
        params.Material = material;
        params.FacingOut = facingOut;
        generateCylinderObject(gvrContext, params);
    }

    /**
     * Constructs a cylinder scene object with a height of 1, radius of 0.5, 10
     * stacks, and 36 slices.
     * 
     * The cylinder's triangles and normals are facing either in or out. The top, bottom, and side of the cylinder each has its own texture.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param futureTextureList
     *            the list of three textures for the cylinder. {@code Future<GVRTexture>} is used here for asynchronously loading
     *            the texture. The six textures are for top, side, and bottom faces respectively.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, boolean facingOut,
            ArrayList<Future<GVRTexture>> futureTextureList) {
        super(gvrContext);
        CylinderParams params = new CylinderParams();
      
        // assert length of futureTextureList is 3
        if (futureTextureList.size() != 3) {
            throw new IllegalArgumentException(
                    "The length of futureTextureList is not 3.");
        }

        generateCylinderObjectThreeMeshes(gvrContext, params, futureTextureList);
    }

    /**
     * Constructs a cylinder scene object with user-specified height, top-radius, bottom-radius, 
     * stacks, slices.
     * 
     * The cylinder's triangles and normals are facing either in or out and the
     * same material will be applied to top, bottom, and side of the cylinder.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     *            
     * @param bottomRadius
     *            radius for the bottom of the cylinder
     *            
     * @param topRadius
     *            radius for the top of the cylinder
     *            
     * @param height
     *            height of the cylinder
     *            
     * @param stackNumber
     *            number of quads high to make the cylinder.
     *            
     * @param sliceNumber
     *            number of quads around to make the cylinder.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, float bottomRadius,
            float topRadius, float height, int stackNumber, int sliceNumber,
            boolean facingOut) {
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
        CylinderParams params = new CylinderParams();
        params.BottomRadius = bottomRadius;
        params.TopRadius = topRadius;
        params.Height = height;
        params.SliceNumber = sliceNumber;
        params.StackNumber = stackNumber;
        params.FacingOut = facingOut;
        params.Material = new GVRMaterial(gvrContext);
        generateCylinderObject(gvrContext, params);
    }

    /**
     * Constructs a cylinder scene object with user-specified height, top-radius, bottom-radius, 
     * stacks, slices.
     * 
     * The cylinder's triangles and normals are facing either in or out and the
     * same material will be applied to top, bottom, and side of the cylinder.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     *            
     * @param bottomRadius
     *            radius for the bottom of the cylinder
     *            
     * @param topRadius
     *            radius for the top of the cylinder
     *            
     * @param height
     *            height of the cylinder
     *            
     * @param stackNumber
     *            number of quads high to make the cylinder.
     *            
     * @param sliceNumber
     *            number of quads around to make the cylinder.
     *            
     * @param material
     *            the material for the cylinder.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, float bottomRadius,
            float topRadius, float height, int stackNumber, int sliceNumber,
            boolean facingOut, GVRMaterial material) {
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
        CylinderParams params = new CylinderParams();
        params.BottomRadius = bottomRadius;
        params.TopRadius = topRadius;
        params.Height = height;
        params.SliceNumber = sliceNumber;
        params.StackNumber = stackNumber;
        params.FacingOut = facingOut;
        params.Material = material;
        generateCylinderObject(gvrContext, params);
    }

    /**
     * Constructs a cylinder scene object with user-specified height, top-radius, bottom-radius, 
     * stacks, slices.
     * 
     * The cylinder's triangles and normals are facing either in or out. The top, bottom, and side of the cylinder each has its own texture.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     *            
     * @param bottomRadius
     *            radius for the bottom of the cylinder
     *            
     * @param topRadius
     *            radius for the top of the cylinder
     *            
     * @param height
     *            height of the cylinder
     *            
     * @param stackNumber
     *            number of quads high to make the cylinder.
     *            
     * @param sliceNumber
     *            number of quads around to make the cylinder.
     *            
     * @param futureTextureList
     *            the list of three textures for the cylinder. {@code Future<GVRTexture>} is used here for asynchronously loading
     *            the texture. The six textures are for top, side, and bottom faces respectively.
     */
    public GVRCylinderSceneObject(GVRContext gvrContext, float bottomRadius,
            float topRadius, float height, int stackNumber, int sliceNumber,
            boolean facingOut, ArrayList<Future<GVRTexture>> futureTextureList) {
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
        CylinderParams params = new CylinderParams();
        params.BottomRadius = bottomRadius;
        params.TopRadius = topRadius;
        params.Height = height;
        params.SliceNumber = sliceNumber;
        params.StackNumber = stackNumber;
        params.FacingOut = facingOut;

        // assert length of futureTextureList is 3
        if (futureTextureList.size() != 3) {
            throw new IllegalArgumentException(
                    "The length of futureTextureList is not 3.");
        }

        generateCylinderObjectThreeMeshes(gvrContext, params, futureTextureList);
    }

    /**
     * Constructs a cylinder scene object with user-specified height, top-radius, bottom-radius, 
     * stacks, slices. The sphere is subdivided into MxN meshes, where M=sliceSegmengNumber and N=(stackSegmentNumber+2) are specified by user.
     * 
     * The cylinder's triangles and normals are facing either in or out. The top, bottom, and side of the cylinder each has its own texture.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     *            
     * @param bottomRadius
     *            radius for the bottom of the cylinder
     *            
     * @param topRadius
     *            radius for the top of the cylinder
     *            
     * @param height
     *            height of the cylinder
     *            
     * @param stackNumber
     *            number of quads high to make the cylinder.
     *            
     * @param sliceNumber
     *            number of quads around to make the cylinder.
     *            
     * @param futureTextureList
     *            the list of three textures for the cylinder. {@code Future<
     *            {@code GVRTexture}>} is used here for asynchronously loading
     *            the texture. The six textures are for top, side, and bottom faces respectively.
     *            
     * @param stackSegmentNumber
     *            the segment number along vertical direction (i.e. stacks).
     *            Note only body is subdivided along
     *            vertical direction. Number of stacks (i.e.
     *            stackNumber) should be divisible by stackSegmentNumber.
     * 
     * @param sliceSegmentNumber
     *            the segment number along horizontal direction (i.e. slices).
     *            Number of slices (i.e. sliceNumber) should be divisible by
     *            sliceSegmentNumber.
    */
    public GVRCylinderSceneObject(GVRContext gvrContext, float bottomRadius,
            float topRadius, float height, int stackNumber, int sliceNumber,
            boolean facingOut, ArrayList<Future<GVRTexture>> futureTextureList, int stackSegmentNumber, int sliceSegmentNumber) {
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

        // assert length of futureTextureList is 3
        if (futureTextureList.size() != 3) {
            throw new IllegalArgumentException(
                    "The length of futureTextureList is not 3.");
        }


        // assert for valid stackSegmentNumber
        if (stackNumber % stackSegmentNumber != 0) {
            throw new IllegalArgumentException(
                    "stackNumber should be divisible by stackSegmentNumber.");
        }

        // assert for valid sliceSegmentNumber
        if (sliceNumber % sliceSegmentNumber != 0) {
            throw new IllegalArgumentException(
                    "sliceNumber should be divisible by sliceSegmentNumber.");
        }
        CylinderParams params = new CylinderParams();
        params.BottomRadius = bottomRadius;
        params.TopRadius = topRadius;
        params.Height = height;
        params.SliceNumber = sliceNumber;
        params.StackNumber = stackNumber;
        params.FacingOut = facingOut;
        generateComplexCylinderObject(gvrContext, params, futureTextureList, stackSegmentNumber, sliceSegmentNumber);
    }

    /**
     * Constructs a cylinder scene object with user-specified height, top-radius, bottom-radius, 
     * stacks, slices. The sphere is subdivided into MxN meshes, where M=sliceSegmengNumber and N=(stackSegmentNumber+2) are specified by user.
     * 
     * The cylinder's triangles and normals are facing either in or out and the
     * same material will be applied to top, bottom, and side of the cylinder.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     *            
     * @param bottomRadius
     *            radius for the bottom of the cylinder
     *            
     * @param topRadius
     *            radius for the top of the cylinder
     *            
     * @param height
     *            height of the cylinder
     *            
     * @param stackNumber
     *            number of quads high to make the cylinder.
     *            
     * @param sliceNumber
     *            number of quads around to make the cylinder.
     *            
     * @param material
     *            the material for the cylinder.
     *            
     * @param stackSegmentNumber
     *            the segment number along vertical direction (i.e. stacks).
     *            Note only body is subdivided along
     *            vertical direction. Number of stacks (i.e.
     *            stackNumber) should be divisible by stackSegmentNumber.
     * 
     * @param sliceSegmentNumber
     *            the segment number along horizontal direction (i.e. slices).
     *            Number of slices (i.e. sliceNumber) should be divisible by
     *            sliceSegmentNumber.
    */
    public GVRCylinderSceneObject(GVRContext gvrContext, float bottomRadius,
            float topRadius, float height, int stackNumber, int sliceNumber,
            boolean facingOut, GVRMaterial material, int stackSegmentNumber, int sliceSegmentNumber) {
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

        // assert for valid stackSegmentNumber
        if (stackNumber % stackSegmentNumber != 0) {
            throw new IllegalArgumentException(
                    "stackNumber should be divisible by stackSegmentNumber.");
        }

        // assert for valid sliceSegmentNumber
        if (sliceNumber % sliceSegmentNumber != 0) {
            throw new IllegalArgumentException(
                    "sliceNumber should be divisible by sliceSegmentNumber.");
        }
        CylinderParams params = new CylinderParams();
        params.BottomRadius = bottomRadius;
        params.TopRadius = topRadius;
        params.Height = height;
        params.SliceNumber = sliceNumber;
        params.StackNumber = stackNumber;
        params.FacingOut = facingOut;
        params.Material = new GVRMaterial(gvrContext);
        
        generateComplexCylinderObject(gvrContext, params, stackSegmentNumber, sliceSegmentNumber);
    }

    private void generateCylinderObject(GVRContext gvrContext, CylinderParams params) {
        generateCylinder(params);

        GVRMesh mesh = new GVRMesh(gvrContext);
        GVRMaterial material = params.Material;
        
        if (material == null)
            material = new GVRMaterial(gvrContext);
        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setTriangles(indices);

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
        renderData.setMaterial(material);
    }

    private void generateCylinderObjectThreeMeshes(GVRContext gvrContext,
            CylinderParams params,
            ArrayList<Future<GVRTexture>> futureTextureList) {
        float halfHeight = params.Height / 2.0f;

        // top cap
        if (params.TopRadius > 0) {
            createCapMesh(gvrContext, params.TopRadius, halfHeight, params.SliceNumber,
                    1.0f, params.FacingOut, futureTextureList.get(0));       
        }
        
        // cylinder body
        createBodyMesh(gvrContext, params, futureTextureList.get(1));
        
        // bottom cap
        if (params.BottomRadius > 0) {
            createCapMesh(gvrContext, params.BottomRadius, -halfHeight, params.SliceNumber,
                    -1.0f, params.FacingOut, futureTextureList.get(2));       
        }

        // attached an empty renderData for parent object, so that we can set some common properties
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
    }

    private void generateCylinder(CylinderParams params) {

        int capNumber = 2;
        if (params.BottomRadius == 0) {
            capNumber--;
        }

        if (params.TopRadius == 0) {
            capNumber--;
        }

        int capVertexNumber = 3 * params.SliceNumber;
        int bodyVertexNumber = 4 * params.SliceNumber * params.StackNumber;
        int vertexNumber = (capNumber * capVertexNumber) + bodyVertexNumber;
        int triangleNumber = (capNumber * capVertexNumber)
                + (6 * params.SliceNumber * params.StackNumber);
        float halfHeight = params.Height / 2.0f;

        vertices = new float[3 * vertexNumber];
        normals = new float[3 * vertexNumber];
        texCoords = new float[2 * triangleNumber];
        indices = new char[triangleNumber];

        // top cap
        // 3 * numSlices
        if (params.HasTopCap && (params.TopRadius > 0)) {
            createCap(params.TopRadius, halfHeight, params.SliceNumber, 1.0f, params.FacingOut);
        }

        // cylinder body
        // 4 * numSlices * numStacks
        createBody(params);

        // bottom cap
        // 3 * numSlices
        if (params.BottomRadius > 0) {
            createCap(params.BottomRadius, -halfHeight, params.SliceNumber, -1.0f, params.FacingOut);
        }
    }
    
    private void createCap(float radius, float height, int sliceNumber,
            float normalDirection, boolean facingOut) {
        if (!facingOut) {
            normalDirection = -normalDirection;
        }
        for (int slice = 0; slice < sliceNumber; slice++) {
            double theta0 = ((double) (slice) / sliceNumber) * 2.0 * Math.PI;
            double theta1 = ((double) (slice + 1) / sliceNumber) * 2.0
                    * Math.PI;

            float y = height;
            float x0 = (float) (radius * Math.cos(theta0));
            float z0 = (float) (radius * Math.sin(theta0));
            float x1 = (float) (radius * Math.cos(theta1));
            float z1 = (float) (radius * Math.sin(theta1));

            float s0, s1;
            if (normalDirection > 0) {
                s0 = (float) (slice) / sliceNumber;
                s1 = (float) (slice + 1) / sliceNumber;
            } else {
                s0 = 1.0f - (float) (slice) / sliceNumber;
                s1 = 1.0f - (float) (slice + 1) / sliceNumber;
            }
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

    private void createBody(CylinderParams params) {
        float difference = params.BottomRadius - params.TopRadius;
        float length = (float) Math.sqrt(difference*difference + params.Height * params.Height);
        float ratio = params.Height / length;
        float halfHeight = params.Height / 2.0f;

        for (int stack = 0; stack < params.StackNumber; stack++) {

            float stackPercentage0 = ((float) (stack) / params.StackNumber);
            float stackPercentage1 = ((float) (stack + 1) / params.StackNumber);
            float radius0 = (params.BottomRadius - (difference * stackPercentage0));
            float radius1 = (params.BottomRadius - (difference * stackPercentage1));

            float t0 = 1.0f - stackPercentage0;
            float t1 = 1.0f - stackPercentage1;
            float y0 = -halfHeight + (stackPercentage0 * params.Height);
            float y1 = -halfHeight + (stackPercentage1 * params.Height);

            for (int slice = 0; slice < params.SliceNumber; slice++) {
                float slicePercentage0 = ((float) (slice) / params.SliceNumber);
                float slicePercentage1 = ((float) (slice + 1) / params.SliceNumber);
                double theta0 = slicePercentage0 * 2.0 * Math.PI;
                double theta1 = slicePercentage1 * 2.0 * Math.PI;
                double cosTheta0 = Math.cos(theta0);
                double sinTheta0 = Math.sin(theta0);
                double cosTheta1 = Math.cos(theta1);
                double sinTheta1 = Math.sin(theta1);

                float x0 = (float) (radius0 * cosTheta0);
                float z0 = (float) (-radius0 * sinTheta0);
                float x1 = (float) (radius0 * cosTheta1);
                float z1 = (float) (-radius0 * sinTheta1);

                float x2 = (float) (radius1 * cosTheta0);
                float z2 = (float) (-radius1 * sinTheta0);
                float x3 = (float) (radius1 * cosTheta1);
                float z3 = (float) (-radius1 * sinTheta1);

                float s0, s1;
                if (params.FacingOut) {
                    s0 = slicePercentage0;
                    s1 = slicePercentage1;
                } else {
                    s0 = 1.0f - slicePercentage0;
                    s1 = 1.0f - slicePercentage1;
                }

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
                float nx0 = (float) (ratio * cosTheta0);
                float nx1 = (float) (ratio * cosTheta1);
                float ny = difference / length;
                float nz0 = (float) (-ratio * sinTheta0);
                float nz1 = (float) (-ratio * sinTheta0);
                
                normals[vertexCount + 0] = nx0;
                normals[vertexCount + 1] = ny;
                normals[vertexCount + 2] = nz0;
                
                normals[vertexCount + 3] = nx1;
                normals[vertexCount + 4] = ny;
                normals[vertexCount + 5] = nz1;
                
                normals[vertexCount + 6] = nx0;
                normals[vertexCount + 7] = ny;
                normals[vertexCount + 8] = nz0;
                
                normals[vertexCount + 9] = nx1;
                normals[vertexCount + 10] = ny;
                normals[vertexCount + 11] = nz1;

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
                // t1   2-----3
                //  |   |     |
                //  v   |     |
                // t0   0-----1
                //
                //     s0 --> s1
                //     
                // @formatter:on
                //
                // Note that tex_coord t increase from top to bottom because the
                // texture image is loaded upside down.
                if (params.FacingOut) {
                    indices[indexCount + 0] = (char) (triangleCount + 0); // 0
                    indices[indexCount + 1] = (char) (triangleCount + 1); // 1
                    indices[indexCount + 2] = (char) (triangleCount + 2); // 2

                    indices[indexCount + 3] = (char) (triangleCount + 2); // 2
                    indices[indexCount + 4] = (char) (triangleCount + 1); // 1
                    indices[indexCount + 5] = (char) (triangleCount + 3); // 3
                } else {
                    indices[indexCount + 0] = (char) (triangleCount + 0); // 0
                    indices[indexCount + 1] = (char) (triangleCount + 2); // 2
                    indices[indexCount + 2] = (char) (triangleCount + 1); // 1

                    indices[indexCount + 3] = (char) (triangleCount + 2); // 2
                    indices[indexCount + 4] = (char) (triangleCount + 3); // 3
                    indices[indexCount + 5] = (char) (triangleCount + 1); // 1
                }

                vertexCount += 12;
                texCoordCount += 8;
                indexCount += 6;
                triangleCount += 4;
            }
        }
    }

    private void createCapMesh(GVRContext gvrContext, float radius, float height, int sliceNumber,
            float normalDirection, boolean facingOut, Future<GVRTexture> futureTexture) {      
        int capVertexNumber = 3 * sliceNumber;
        vertices = new float[3 * capVertexNumber];
        normals = new float[3 * capVertexNumber];
        texCoords = new float[2 * capVertexNumber];
        indices = new char[capVertexNumber];

        vertexCount = 0;
        texCoordCount = 0;
        indexCount = 0;
        triangleCount = 0;

        createCap(radius, height, sliceNumber, normalDirection, facingOut);

        GVRMesh mesh = new GVRMesh(gvrContext);
        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setTriangles(indices);

        GVRSceneObject child = new GVRSceneObject(gvrContext,
                new FutureWrapper<GVRMesh>(mesh),
                futureTexture);
        addChildObject(child);        
   }

    private void createBodyMesh(GVRContext gvrContext, CylinderParams params, Future<GVRTexture> futureTexture) {
        int bodyVertexNumber = 4 * params.SliceNumber * params.StackNumber;
        int triangleNumber = 6 * params.SliceNumber * params.StackNumber;
        
        vertices = new float[3 * bodyVertexNumber];
        normals = new float[3 * bodyVertexNumber];
        texCoords = new float[2 * triangleNumber];
        indices = new char[triangleNumber];

        vertexCount = 0;
        texCoordCount = 0;
        indexCount = 0;
        triangleCount = 0;

        createBody(params);

        GVRMesh mesh = new GVRMesh(gvrContext);
        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setTriangles(indices);

        GVRSceneObject child = new GVRSceneObject(gvrContext,
                new FutureWrapper<GVRMesh>(mesh),
                futureTexture);
        addChildObject(child);        
    }

    private void generateComplexCylinderObject(GVRContext gvrContext,
            CylinderParams params,
            ArrayList<Future<GVRTexture>> futureTextureList, int stackSegmentNumber, int sliceSegmentNumber) {
        float halfHeight = params.Height / 2.0f;

        GVRMaterial material;
        // top cap
        if (params.HasTopCap && (params.TopRadius > 0)) {
            material = new GVRMaterial(gvrContext);
            material.setMainTexture(futureTextureList.get(0));
            createComplexCap(gvrContext, params.TopRadius, halfHeight, params.SliceNumber,
                    1.0f, params.FacingOut, material, params.SliceNumber);       
        }
        
        // cylinder body
        material = new GVRMaterial(gvrContext);
        material.setMainTexture(futureTextureList.get(1));
        createComplexBody(gvrContext, params, stackSegmentNumber, sliceSegmentNumber);
        
        // bottom cap
        if (params.HasBottomCap && (params.BottomRadius > 0)) {
            material = new GVRMaterial(gvrContext);
            material.setMainTexture(futureTextureList.get(2));
            createComplexCap(gvrContext, params.BottomRadius, -halfHeight, params.SliceNumber,
                    -1.0f, params.FacingOut, material, params.SliceNumber);       
        }

        // attached an empty renderData for parent object, so that we can set some common properties
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
    }

    private void generateComplexCylinderObject(GVRContext gvrContext,
            CylinderParams params,
            int stackSegmentNumber, int sliceSegmentNumber) {
        float halfHeight = params.Height / 2.0f;

        // top cap
        if (params.HasTopCap && (params.TopRadius > 0)) {
            createComplexCap(gvrContext, params.TopRadius, halfHeight, params.SliceNumber,
                    1.0f, params.FacingOut, params.Material, params.SliceNumber);       
        }
        
        // cylinder body
        createComplexBody(gvrContext, params, stackSegmentNumber, sliceSegmentNumber);
        
        // bottom cap
        if (params.HasBottomCap && (params.BottomRadius > 0)) {
            createComplexCap(gvrContext, params.BottomRadius, -halfHeight, params.SliceNumber,
                    -1.0f, params.FacingOut, params.Material, params.SliceNumber);       
        }

        // attached an empty renderData for parent object, so that we can set some common properties
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
    }

    private void createComplexCap(GVRContext gvrContext, float radius, float height, int sliceNumber,
            float normalDirection, boolean facingOut, GVRMaterial material, int sliceSegmentNumber) {
        if (!facingOut) {
            normalDirection = -normalDirection;
        }

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
        
        for (int slice = 0; slice < sliceNumber; slice++) {
            double theta0 = ((double) (slice) / sliceNumber) * 2.0 * Math.PI;
            double theta1 = ((double) (slice + 1) / sliceNumber) * 2.0
                    * Math.PI;

            float y = height;
            float x0 = (float) (radius * Math.cos(theta0));
            float z0 = (float) (radius * Math.sin(theta0));
            float x1 = (float) (radius * Math.cos(theta1));
            float z1 = (float) (radius * Math.sin(theta1));

            float s0, s1;
            if (normalDirection > 0) {
                s0 = (float) (slice) / sliceNumber;
                s1 = (float) (slice + 1) / sliceNumber;
            } else {
                s0 = 1.0f - (float) (slice) / sliceNumber;
                s1 = 1.0f - (float) (slice + 1) / sliceNumber;
            }
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

    private void createComplexBody(GVRContext gvrContext, CylinderParams params, int stackSegmentNumber, int sliceSegmentNumber) {
        float difference = params.BottomRadius - params.TopRadius;
        float length = (float) Math.sqrt(difference*difference + params.Height * params.Height);
        float ratio = params.Height / length;
        float halfHeight = params.Height / 2.0f;

        int stackPerSegment = params.StackNumber / stackSegmentNumber;
        int slicePerSegment = params.SliceNumber / sliceSegmentNumber;
        
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
                for (int stack = stackSegment * stackPerSegment; stack < (stackSegment+1) * stackPerSegment; stack++) {
                    float stackPercentage0 = ((float) (stack) / params.StackNumber);
                    float stackPercentage1 = ((float) (stack + 1) / params.StackNumber);
        
                    float t0 = 1.0f - stackPercentage0;
                    float t1 = 1.0f - stackPercentage1;
                    float y0 = -halfHeight + (stackPercentage0 * params.Height);
                    float y1 = -halfHeight + (stackPercentage1 * params.Height);
    
                    for (int slice = sliceSegment * slicePerSegment; slice < (sliceSegment+1) * slicePerSegment; slice++) {
                        float slicePercentage0 = ((float) (slice) / params.SliceNumber);
                        float slicePercentage1 = ((float) (slice + 1) / params.SliceNumber);
                        double theta0 = slicePercentage0 * 2.0 * Math.PI;
                        double theta1 = slicePercentage1 * 2.0 * Math.PI;
                        double cosTheta0 = Math.cos(theta0);
                        double sinTheta0 = Math.sin(theta0);
                        double cosTheta1 = Math.cos(theta1);
                        double sinTheta1 = Math.sin(theta1);
        
                        float radius = (params.BottomRadius - (difference * stackPercentage0));
                        float x0 = (float) (radius * cosTheta0);
                        float z0 = (float) (-radius * sinTheta0);
                        float x1 = (float) (radius * cosTheta1);
                        float z1 = (float) (-radius * sinTheta1);
        
                        radius = (params.BottomRadius - (difference * stackPercentage1));
                        float x2 = (float) (radius * cosTheta0);
                        float z2 = (float) (-radius * sinTheta0);
                        float x3 = (float) (radius * cosTheta1);
                        float z3 = (float) (-radius * sinTheta1);
        
                        float s0, s1;
                        if (params.FacingOut) {
                            s0 = slicePercentage0;
                            s1 = slicePercentage1;
                        } else {
                            s0 = 1.0f - slicePercentage0;
                            s1 = 1.0f - slicePercentage1;
                        }
        
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
                        float nx0 = (float) (ratio * cosTheta0);
                        float nx1 = (float) (ratio * cosTheta1);
                        float ny = difference / length;
                        float nz0 = (float) (-ratio * sinTheta0);
                        float nz1 = (float) (-ratio * sinTheta0);
                        
                        normals[vertexCount + 0] = nx0;
                        normals[vertexCount + 1] = ny;
                        normals[vertexCount + 2] = nz0;
                        
                        normals[vertexCount + 3] = nx1;
                        normals[vertexCount + 4] = ny;
                        normals[vertexCount + 5] = nz1;
                        
                        normals[vertexCount + 6] = nx0;
                        normals[vertexCount + 7] = ny;
                        normals[vertexCount + 8] = nz0;
                        
                        normals[vertexCount + 9] = nx1;
                        normals[vertexCount + 10] = ny;
                        normals[vertexCount + 11] = nz1;
        
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
                        // t1   2-----3
                        //  |   |     |
                        //  v   |     |
                        // t0   0-----1
                        //
                        //     s0 --> s1
                        //     
                        // @formatter:on
                        //
                        // Note that tex_coord t increase from top to bottom because the
                        // texture image is loaded upside down.
                        if (params.FacingOut) {
                            indices[indexCount + 0] = (char) (triangleCount + 0); // 0
                            indices[indexCount + 1] = (char) (triangleCount + 1); // 1
                            indices[indexCount + 2] = (char) (triangleCount + 2); // 2
        
                            indices[indexCount + 3] = (char) (triangleCount + 2); // 2
                            indices[indexCount + 4] = (char) (triangleCount + 1); // 1
                            indices[indexCount + 5] = (char) (triangleCount + 3); // 3
                        } else {
                            indices[indexCount + 0] = (char) (triangleCount + 0); // 0
                            indices[indexCount + 1] = (char) (triangleCount + 2); // 2
                            indices[indexCount + 2] = (char) (triangleCount + 1); // 1
        
                            indices[indexCount + 3] = (char) (triangleCount + 2); // 2
                            indices[indexCount + 4] = (char) (triangleCount + 3); // 3
                            indices[indexCount + 5] = (char) (triangleCount + 1); // 1
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
                childObject.getRenderData().setMaterial(params.Material);
                addChildObject(childObject);

                vertexCount = 0;
                texCoordCount = 0;
                indexCount = 0;
                triangleCount = 0;
            }
        }
    }
}
