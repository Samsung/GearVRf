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
import org.joml.Vector3f;

public class GVRCubeSceneObject extends GVRSceneObject {

    private static final float SIZE = 0.5f;

    // naming convention for arrays:
    // simple - single quad for each face
    // complex - multiple quads for each face
    // inward / outward - normals and triangles facing in or out
    //
    // ----------------------------------
    // Simple - 1 quad per face
    // ----------------------------------
    //@formatter:off
    //       ----------        ---------7       11--------10
    //     / .       /|      / .       /|      / .       /|
    //    2--.------3 |     ---.------6 |     ---.------- |
    //    |  .      | |     |  .      | |     |  .      | |
    //    |  .......|.|     |  .......|.5     |  9......|.8
    //    | /       |/      | /       |/      | /       |/
    //    0---------1       ----------4       ----------
    //       front             right              back
    //
    //     14----------      18--------19        ---------- 
    //     / .       /|      / .       /|      / .       /|
    //   15--.------- |    16--.-----17 |     ---.------- |
    //    |  .      | |     |  .      | |     |  .      | |
    //    | 12......|.|     |  .......|.|     | 20......|21
    //    | /       |/      | /       |/      | /       |/
    //   13---------        ----------        22-------23
    //       left               top              bottom
    //
    //@formatter:on
    //
    // (Note that these name are for outward facing. When looking from inside,
    // "back" face is in front of you, and "front" face is behind you.)

    private static final float[] SIMPLE_VERTICES = { -SIZE, -SIZE, SIZE, // 0
            SIZE, -SIZE, SIZE, // 1
            -SIZE, SIZE, SIZE, // 2
            SIZE, SIZE, SIZE, // 3

            SIZE, -SIZE, SIZE, // 4
            SIZE, -SIZE, -SIZE, // 5
            SIZE, SIZE, SIZE, // 6
            SIZE, SIZE, -SIZE, // 7

            SIZE, -SIZE, -SIZE, // 8
            -SIZE, -SIZE, -SIZE, // 9
            SIZE, SIZE, -SIZE, // 10
            -SIZE, SIZE, -SIZE, // 11

            -SIZE, -SIZE, -SIZE, // 12
            -SIZE, -SIZE, SIZE, // 13
            -SIZE, SIZE, -SIZE, // 14
            -SIZE, SIZE, SIZE, // 15

            -SIZE, SIZE, SIZE, // 16
            SIZE, SIZE, SIZE, // 17
            -SIZE, SIZE, -SIZE, // 18
            SIZE, SIZE, -SIZE, // 19

            -SIZE, -SIZE, -SIZE, // 20
            SIZE, -SIZE, -SIZE, // 21
            -SIZE, -SIZE, SIZE, // 22
            SIZE, -SIZE, SIZE, // 23
    };

    private static final float[] SIMPLE_OUTWARD_NORMALS = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,

            1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.0f,

            0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
            0.0f, -1.0f,

            -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
            0.0f, 0.0f,

            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.0f,

            0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f };

    private static final float[] SIMPLE_INWARD_NORMALS = { 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,

            -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
            0.0f, 0.0f,

            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            1.0f,

            1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.0f,

            0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f,

            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.0f };

    private static final float[] SIMPLE_OUTWARD_TEXCOORDS = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, // front
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // right
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // back
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // left
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f  // bottom
    };

    private static final float[] SIMPLE_INWARD_TEXCOORDS = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
            0.0f, 0.0f, // front
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, // right
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, // back
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, // left
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // top
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f  // bottom
    };

    private static final char[] SIMPLE_OUTWARD_INDICES = { 0, 1, 2, // front
            2, 1, 3,

            4, 5, 6, // right
            6, 5, 7,

            8, 9, 10, // back
            10, 9, 11,

            12, 13, 14, // left
            14, 13, 15, //

            16, 17, 18, // top
            18, 17, 19, //

            20, 21, 22, // bottom
            22, 21, 23 //
    };

    private static final char[] SIMPLE_INWARD_INDICES = { 0, 2, 1, // front
            1, 2, 3,

            4, 6, 5, // right
            5, 6, 7,

            8, 10, 9, // back
            9, 10, 11,

            12, 14, 13, // left
            13, 14, 15, //

            16, 18, 17, // top
            17, 18, 19, //

            20, 22, 21, // bottom
            21, 22, 23 //
    };

    /**
     * Constructs a cube scene object with each side of length 1.
     * 
     * The cube's triangles and normals are facing out and the same texture will
     * be applied to each side of the cube.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRCubeSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        createSimpleCube(gvrContext, true, new GVRMaterial(gvrContext), null);
    }

    /**
     * Constructs a cube scene object with each side of length 1.
     * 
     * The cube's triangles and normals are facing either in or out and the same
     * texture will be applied to each side of the cube.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     */
    public GVRCubeSceneObject(GVRContext gvrContext, boolean facingOut) {
        super(gvrContext);

        createSimpleCube(gvrContext, facingOut, new GVRMaterial(gvrContext), null);
    }

    /**
     * Constructs a cube scene object with each side of length 1.
     * 
     * The cube's triangles and normals are facing either in or out and the same
     * texture will be applied to each side of the cube. All six faces share the
     * same texture.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param futureTexture
     *            the texture for six faces. {@code Future<GVRTexture>}
     *            is used here for asynchronously loading the texture.
     */
    public GVRCubeSceneObject(GVRContext gvrContext, boolean facingOut,
            Future<GVRTexture> futureTexture) {
        super(gvrContext);

        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(futureTexture);
        createSimpleCube(gvrContext, facingOut, material, null);
    }

    /**
     * Constructs a cube scene object with each side of length 1.
     * 
     * The cube's triangles and normals are facing either in or out and the material
     * is applied to the cube.
     *
     * To use the same texture on the six faces, use a material with the shader type
     * {@code GVRMaterial.GVRShaderType.Texture} and a {@code GVRTexture}.
     *
     * To use different textures on different faces, use a material
     * with the shader type {@code GVRMaterial.GVRShaderType.Cubemap}, and a
     * cubemap texture loaded by {@code GVRContext.loadFutureCubemapTexture}.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param material
     *            the material for six faces.
     */
    public GVRCubeSceneObject(GVRContext gvrContext, boolean facingOut,
            GVRMaterial material) {
        super(gvrContext);

        createSimpleCube(gvrContext, facingOut, material, null);
    }

    /**
     * Constructs a box scene object with given dimensions.
     * 
     * The triangles and normals are facing either in or out and the material
     * is applied to the cube.
     *
     * To use the same texture on the six faces, use a material with the shader type
     * {@code GVRMaterial.GVRShaderType.Texture} and a {@code GVRTexture}.
     *
     * To use different textures on different faces, use a material
     * with the shader type {@code GVRMaterial.GVRShaderType.Cubemap}, and a
     * cubemap texture loaded by {@code GVRContext.loadFutureCubemapTexture}.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param material
     *            the material for six faces.
     *            
     * @param dimensions
     *            Vector3f containing X, Y, Z dimensions
     */
    public GVRCubeSceneObject(GVRContext gvrContext, boolean facingOut,
            GVRMaterial material, Vector3f dimensions) {
        super(gvrContext);

        createSimpleCube(gvrContext, facingOut, material, dimensions);
    }
    
    /**
     * Constructs a cube scene object with each side of length 1.
     * 
     * The cube's triangles and normals are facing either in or out. Each face
     * has its own texture.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param futureTextureList
     *            the list of six textures for six faces.
     *            {@code Future<GVRTexture>} is used here for asynchronously
     *            loading the texture. The six textures are for front, right,
     *            back, left, top, and bottom faces respectively.
     */
    public GVRCubeSceneObject(GVRContext gvrContext, boolean facingOut,
            ArrayList<Future<GVRTexture>> futureTextureList) {
        super(gvrContext);

        // assert length of futureTextureList is 6
        if (futureTextureList.size() != 6) {
            throw new IllegalArgumentException(
                    "The length of futureTextureList is not 6.");
        }

        createSimpleCubeSixMeshes(gvrContext, facingOut, futureTextureList);
    }

    /**
     * Constructs a cube scene object with each side of length 1.
     * 
     * Each face is subdivided into NxN quads, where N = segmentNumber is given by user.  
     * 
     * The cube's triangles and normals are facing either in or out. Each face has its own
     * texture.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * 
     * @param facingOut
     *            whether the triangles and normals should be facing in or
     *            facing out.
     * 
     * @param futureTextureList
     *            the list of six textures for six faces. {@code Future<GVRTexture>} is used here for asynchronously loading
     *            the texture. The six textures are for front, right, back,
     *            left, top, and bottom faces respectively.
     *            
     * @param segmetnNumber
     *            the segment number along each axis. 
     *            
     */
    public GVRCubeSceneObject(GVRContext gvrContext, boolean facingOut,
            ArrayList<Future<GVRTexture>> futureTextureList, int segmentNumber) {
        super(gvrContext);

        // assert length of futureTextureList is 6
        if (futureTextureList.size() != 6) {
            throw new IllegalArgumentException(
                    "The length of futureTextureList is not 6.");
        }

        createComplexCube(gvrContext, facingOut, futureTextureList, segmentNumber);
    }

    private void createSimpleCube(GVRContext gvrContext, boolean facingOut,
            GVRMaterial material, Vector3f dimensions) {

        GVRMesh mesh = new GVRMesh(gvrContext);
        float[] vertices = SIMPLE_VERTICES;
        if (dimensions != null) {
            vertices = new float[SIMPLE_VERTICES.length];
            for (int i = 0; i < SIMPLE_VERTICES.length; i += 3) {
               vertices[i] = SIMPLE_VERTICES[i] * dimensions.x;
               vertices[i + 1] = SIMPLE_VERTICES[i + 1] * dimensions.y;
               vertices[i + 2] = SIMPLE_VERTICES[i + 2] * dimensions.z;
            }
         }
        if (facingOut) {
            mesh.setVertices(vertices);
            mesh.setNormals(SIMPLE_OUTWARD_NORMALS);
            mesh.setTexCoords(SIMPLE_OUTWARD_TEXCOORDS);
            mesh.setTriangles(SIMPLE_OUTWARD_INDICES);
        } else {
            mesh.setVertices(vertices);
            mesh.setNormals(SIMPLE_INWARD_NORMALS);
            mesh.setTexCoords(SIMPLE_INWARD_TEXCOORDS);
            mesh.setTriangles(SIMPLE_INWARD_INDICES);
        }

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMaterial(material);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

    private static final char[] SIMPLE_OUTWARD_FRONT_INDICES = { 0, 1, 2, // front
            2, 1, 3 };
    private static final char[] SIMPLE_OUTWARD_RIGHT_INDICES = { 4, 5, 6, // right
            6, 5, 7 };
    private static final char[] SIMPLE_OUTWARD_BACK_INDICES = { 8, 9, 10, // back
            10, 9, 11 };
    private static final char[] SIMPLE_OUTWARD_LEFT_INDICES = { 12, 13, 14, // left
            14, 13, 15 };
    private static final char[] SIMPLE_OUTWARD_TOP_INDICES = { 16, 17, 18, // top
            18, 17, 19 };
    private static final char[] SIMPLE_OUTWARD_BOTTOM_INDICES = { 20, 21, 22, // bottom
            22, 21, 23 };

    private static final char[] SIMPLE_INWARD_FRONT_INDICES = { 0, 2, 1, // front
            1, 2, 3 };
    private static final char[] SIMPLE_INWARD_RIGHT_INDICES = { 4, 6, 5, // right
            5, 6, 7 };
    private static final char[] SIMPLE_INWARD_BACK_INDICES = { 8, 10, 9, // back
            9, 10, 11 };
    private static final char[] SIMPLE_INWARD_LEFT_INDICES = { 12, 14, 13, // left
            13, 14, 15 };
    private static final char[] SIMPLE_INWARD_TOP_INDICES = { 16, 18, 17, // top
            17, 18, 19 };
    private static final char[] SIMPLE_INWARD_BOTTOM_INDICES = { 20, 22, 21, // bottom
            21, 22, 23 };

    private void createSimpleCubeSixMeshes(GVRContext gvrContext,
            boolean facingOut, ArrayList<Future<GVRTexture>> futureTextureList) {

        GVRSceneObject[] children = new GVRSceneObject[6];
        GVRMesh[] meshes = new GVRMesh[6];
        for (int i = 0; i < 6; i++) {
            meshes[i] = new GVRMesh(gvrContext);
        }

        if (facingOut) {
            meshes[0].setTriangles(SIMPLE_OUTWARD_FRONT_INDICES);
            meshes[1].setTriangles(SIMPLE_OUTWARD_RIGHT_INDICES);
            meshes[2].setTriangles(SIMPLE_OUTWARD_BACK_INDICES);
            meshes[3].setTriangles(SIMPLE_OUTWARD_LEFT_INDICES);
            meshes[4].setTriangles(SIMPLE_OUTWARD_TOP_INDICES);
            meshes[5].setTriangles(SIMPLE_OUTWARD_BOTTOM_INDICES);
            for (int i = 0; i < 6; i++) {
                meshes[i].setVertices(SIMPLE_VERTICES);
                meshes[i].setNormals(SIMPLE_OUTWARD_NORMALS);
                meshes[i].setTexCoords(SIMPLE_OUTWARD_TEXCOORDS);
                children[i] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(meshes[i]),
                        futureTextureList.get(i));
                addChildObject(children[i]);
            }
        } else {
            meshes[0].setTriangles(SIMPLE_INWARD_FRONT_INDICES);
            meshes[1].setTriangles(SIMPLE_INWARD_RIGHT_INDICES);
            meshes[2].setTriangles(SIMPLE_INWARD_BACK_INDICES);
            meshes[3].setTriangles(SIMPLE_INWARD_LEFT_INDICES);
            meshes[4].setTriangles(SIMPLE_INWARD_TOP_INDICES);
            meshes[5].setTriangles(SIMPLE_INWARD_BOTTOM_INDICES);
            for (int i = 0; i < 6; i++) {
                meshes[i].setVertices(SIMPLE_VERTICES);
                meshes[i].setNormals(SIMPLE_INWARD_NORMALS);
                meshes[i].setTexCoords(SIMPLE_INWARD_TEXCOORDS);
                children[i] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(meshes[i]),
                        futureTextureList.get(i));
                addChildObject(children[i]);
            }
        }
        
        // attached an empty renderData for parent object, so that we can set some common properties
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
    }

    private float[] vertices;
    private float[] normals;
    private float[] texCoords;
    private char[] indices;

    private void createComplexCube(GVRContext gvrContext,
            boolean facingOut, ArrayList<Future<GVRTexture>> futureTextureList, int segmentNumber) {

        GVRSceneObject[] children = new GVRSceneObject[6];
        for (int i = 0; i < 6; i++) {
            children[i] = new GVRSceneObject(gvrContext);
            addChildObject(children[i]);
        }
        
        int numPerFace = segmentNumber*segmentNumber;
        GVRSceneObject[] grandchildren = new GVRSceneObject[numPerFace];
        GVRMesh[] subMeshes = new GVRMesh[numPerFace];
        
        // 4 vertices (2 triangles) per mesh
        vertices = new float[12];
        normals = new float[12];
        texCoords = new float[8];
        indices = new char[6];
        
        if (facingOut) {
            indices[0] = 0;
            indices[1] = 1;
            indices[2] = 2;

            indices[3] = 1;
            indices[4] = 3;
            indices[5] = 2;
        } else {
            indices[0] = 0;
            indices[1] = 2;
            indices[2] = 1;

            indices[3] = 1;
            indices[4] = 2;
            indices[5] = 3;
        }
        
        float segmentLength = 2.0f * SIZE / segmentNumber;
        float segmentTexCoordLength = 1.0f / segmentNumber;
        
        // front face
        normals[0] = normals[3] = normals[6] = normals[9] = 0.0f;
        normals[1] = normals[4] = normals[7] = normals[10] = 0.0f;
        if (facingOut) {
            normals[2] = normals[5] = normals[8] = normals[11] = 1.0f;
        } else {
            normals[2] = normals[5] = normals[8] = normals[11] = -1.0f;
        }
        for (int col = 0; col<segmentNumber; col++) {
            for (int row = 0; row<segmentNumber; row++) {
                // sub-mesh (col, row)
                int index = row*segmentNumber+col;
                
                float x0 = -SIZE + segmentLength * col;
                float y0 = -SIZE + segmentLength * row;
                float x1 = x0 + segmentLength;
                float y1 = y0 + segmentLength;
                float z = SIZE;
                vertices[0] = x0;
                vertices[1] = y0;
                vertices[2] = z;
                vertices[3] = x1;
                vertices[4] = y0;
                vertices[5] = z;
                vertices[6] = x0;
                vertices[7] = y1;
                vertices[8] = z;
                vertices[9] = x1;
                vertices[10] = y1;
                vertices[11] = z;
                
                float s0, s1;
                if (facingOut) {
                    s0 = col * segmentTexCoordLength;
                    s1 = (col + 1) * segmentTexCoordLength;
                } else {
                    s0 = 1.0f - col * segmentTexCoordLength;
                    s1 = 1.0f - (col + 1) * segmentTexCoordLength;
                }
                float t0 = 1.0f - (row + 1) * segmentTexCoordLength;
                float t1 = 1.0f - row * segmentTexCoordLength;
                texCoords[0] = s0; 
                texCoords[1] = t1; 
                texCoords[2] = s1; 
                texCoords[3] = t1; 
                texCoords[4] = s0; 
                texCoords[5] = t0; 
                texCoords[6] = s1; 
                texCoords[7] = t0;
                
                subMeshes[index] = new GVRMesh(gvrContext);
                subMeshes[index].setVertices(vertices);
                subMeshes[index].setNormals(normals);
                subMeshes[index].setTexCoords(texCoords);
                subMeshes[index].setTriangles(indices);
                grandchildren[index] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(subMeshes[index]),
                        futureTextureList.get(0));
                children[0].addChildObject(grandchildren[index]);
            }
        }

        // right face
        if (facingOut) {
            normals[0] = normals[3] = normals[6] = normals[9] = 1.0f;
        } else {
            normals[0] = normals[3] = normals[6] = normals[9] = -1.0f;
        }
        normals[1] = normals[4] = normals[7] = normals[10] = 0.0f;
        normals[2] = normals[5] = normals[8] = normals[11] = 0.0f;
        for (int col = 0; col<segmentNumber; col++) {
            for (int row = 0; row<segmentNumber; row++) {
                // sub-mesh (col, row)
                int index = row*segmentNumber+col;
                
                float x = SIZE;
                float y0 = -SIZE + segmentLength * row;
                float z0 = SIZE - segmentLength * col;
                float y1 = y0 + segmentLength;
                float z1 = z0 - segmentLength;
                vertices[0] = x;
                vertices[1] = y0;
                vertices[2] = z0;
                vertices[3] = x;
                vertices[4] = y0;
                vertices[5] = z1;
                vertices[6] = x;
                vertices[7] = y1;
                vertices[8] = z0;
                vertices[9] = x;
                vertices[10] = y1;
                vertices[11] = z1;
                
                float s0, s1;
                if (facingOut) {
                    s0 = col * segmentTexCoordLength;
                    s1 = (col + 1) * segmentTexCoordLength;
                } else {
                    s0 = 1.0f - col * segmentTexCoordLength;
                    s1 = 1.0f - (col + 1) * segmentTexCoordLength;
                }
                float t0 = 1.0f - (row + 1) * segmentTexCoordLength;
                float t1 = 1.0f - row * segmentTexCoordLength;
                texCoords[0] = s0; 
                texCoords[1] = t1; 
                texCoords[2] = s1; 
                texCoords[3] = t1; 
                texCoords[4] = s0; 
                texCoords[5] = t0; 
                texCoords[6] = s1; 
                texCoords[7] = t0;
                
                subMeshes[index] = new GVRMesh(gvrContext);
                subMeshes[index].setVertices(vertices);
                subMeshes[index].setNormals(normals);
                subMeshes[index].setTexCoords(texCoords);
                subMeshes[index].setTriangles(indices);
                grandchildren[index] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(subMeshes[index]),
                        futureTextureList.get(1));
                children[1].addChildObject(grandchildren[index]);
            }
        }

        // back face
        normals[0] = normals[3] = normals[6] = normals[9] = 0.0f;
        normals[1] = normals[4] = normals[7] = normals[10] = 0.0f;
        if (facingOut) {
            normals[2] = normals[5] = normals[8] = normals[11] = -1.0f;
        } else {
            normals[2] = normals[5] = normals[8] = normals[11] = 1.0f;
        }
        for (int col = 0; col<segmentNumber; col++) {
            for (int row = 0; row<segmentNumber; row++) {
                // sub-mesh (col, row)
                int index = row*segmentNumber+col;
                
                float x0 = SIZE - segmentLength * col;
                float y0 = -SIZE + segmentLength * row;
                float x1 = x0 - segmentLength;
                float y1 = y0 + segmentLength;
                float z = -SIZE;
                vertices[0] = x0;
                vertices[1] = y0;
                vertices[2] = z;
                vertices[3] = x1;
                vertices[4] = y0;
                vertices[5] = z;
                vertices[6] = x0;
                vertices[7] = y1;
                vertices[8] = z;
                vertices[9] = x1;
                vertices[10] = y1;
                vertices[11] = z;
                
                float s0, s1;
                if (facingOut) {
                    s0 = col * segmentTexCoordLength;
                    s1 = (col + 1) * segmentTexCoordLength;
                } else {
                    s0 = 1.0f - col * segmentTexCoordLength;
                    s1 = 1.0f - (col + 1) * segmentTexCoordLength;
                }
                float t0 = 1.0f - (row + 1) * segmentTexCoordLength;
                float t1 = 1.0f - row * segmentTexCoordLength;
                texCoords[0] = s0; 
                texCoords[1] = t1; 
                texCoords[2] = s1; 
                texCoords[3] = t1; 
                texCoords[4] = s0; 
                texCoords[5] = t0; 
                texCoords[6] = s1; 
                texCoords[7] = t0;
                
                subMeshes[index] = new GVRMesh(gvrContext);
                subMeshes[index].setVertices(vertices);
                subMeshes[index].setNormals(normals);
                subMeshes[index].setTexCoords(texCoords);
                subMeshes[index].setTriangles(indices);
                grandchildren[index] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(subMeshes[index]),
                        futureTextureList.get(2));
                children[2].addChildObject(grandchildren[index]);
            }
        }

        // left face
        if (facingOut) {
            normals[0] = normals[3] = normals[6] = normals[9] = -1.0f;
        } else {
            normals[0] = normals[3] = normals[6] = normals[9] = 1.0f;
        }
        normals[1] = normals[4] = normals[7] = normals[10] = 0.0f;
        normals[2] = normals[5] = normals[8] = normals[11] = 0.0f;
        for (int col = 0; col<segmentNumber; col++) {
            for (int row = 0; row<segmentNumber; row++) {
                // sub-mesh (col, row)
                int index = row*segmentNumber+col;
                
                float x = -SIZE;
                float y0 = -SIZE + segmentLength * row;
                float z0 = -SIZE + segmentLength * col;
                float y1 = y0 + segmentLength;
                float z1 = z0 + segmentLength;
                vertices[0] = x;
                vertices[1] = y0;
                vertices[2] = z0;
                vertices[3] = x;
                vertices[4] = y0;
                vertices[5] = z1;
                vertices[6] = x;
                vertices[7] = y1;
                vertices[8] = z0;
                vertices[9] = x;
                vertices[10] = y1;
                vertices[11] = z1;
                
                float s0, s1;
                if (facingOut) {
                    s0 = col * segmentTexCoordLength;
                    s1 = (col + 1) * segmentTexCoordLength;
                } else {
                    s0 = 1.0f - col * segmentTexCoordLength;
                    s1 = 1.0f - (col + 1) * segmentTexCoordLength;
                }
                float t0 = 1.0f - (row + 1) * segmentTexCoordLength;
                float t1 = 1.0f - row * segmentTexCoordLength;
                texCoords[0] = s0; 
                texCoords[1] = t1; 
                texCoords[2] = s1; 
                texCoords[3] = t1; 
                texCoords[4] = s0; 
                texCoords[5] = t0; 
                texCoords[6] = s1; 
                texCoords[7] = t0;
                
                subMeshes[index] = new GVRMesh(gvrContext);
                subMeshes[index].setVertices(vertices);
                subMeshes[index].setNormals(normals);
                subMeshes[index].setTexCoords(texCoords);
                subMeshes[index].setTriangles(indices);
                grandchildren[index] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(subMeshes[index]),
                        futureTextureList.get(3));
                children[3].addChildObject(grandchildren[index]);
            }
        }

        // top face
        normals[0] = normals[3] = normals[6] = normals[9] = 0.0f;
        if (facingOut) {
            normals[1] = normals[4] = normals[7] = normals[10] = 1.0f;
        } else {
            normals[1] = normals[4] = normals[7] = normals[10] = -1.0f;
        }
        normals[2] = normals[5] = normals[8] = normals[11] = 0.0f;
        for (int col = 0; col<segmentNumber; col++) {
            for (int row = 0; row<segmentNumber; row++) {
                // sub-mesh (col, row)
                int index = row*segmentNumber+col;
                
                float y = SIZE;
                float x0 = -SIZE + segmentLength * col;
                float z0 = SIZE - segmentLength * row;
                float x1 = x0 + segmentLength;
                float z1 = z0 - segmentLength;
                vertices[0] = x0;
                vertices[1] = y;
                vertices[2] = z0;
                vertices[3] = x1;
                vertices[4] = y;
                vertices[5] = z0;
                vertices[6] = x0;
                vertices[7] = y;
                vertices[8] = z1;
                vertices[9] = x1;
                vertices[10] = y;
                vertices[11] = z1;
                
                float s0, s1;
                s0 = col * segmentTexCoordLength;
                s1 = (col + 1) * segmentTexCoordLength;
                float t0 = (row + 1) * segmentTexCoordLength;
                float t1 = row * segmentTexCoordLength;
                texCoords[0] = s0; 
                texCoords[1] = t1; 
                texCoords[2] = s1; 
                texCoords[3] = t1; 
                texCoords[4] = s0; 
                texCoords[5] = t0; 
                texCoords[6] = s1; 
                texCoords[7] = t0;
                
                subMeshes[index] = new GVRMesh(gvrContext);
                subMeshes[index].setVertices(vertices);
                subMeshes[index].setNormals(normals);
                subMeshes[index].setTexCoords(texCoords);
                subMeshes[index].setTriangles(indices);
                grandchildren[index] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(subMeshes[index]),
                        futureTextureList.get(4));
                children[4].addChildObject(grandchildren[index]);
            }
        }

        // bottom face
        normals[0] = normals[3] = normals[6] = normals[9] = 0.0f;
        if (facingOut) {
            normals[1] = normals[4] = normals[7] = normals[10] = -1.0f;
        } else {
            normals[1] = normals[4] = normals[7] = normals[10] = 1.0f;
        }
        normals[2] = normals[5] = normals[8] = normals[11] = 0.0f;
        for (int col = 0; col<segmentNumber; col++) {
            for (int row = 0; row<segmentNumber; row++) {
                // sub-mesh (col, row)
                int index = row*segmentNumber+col;
                
                float y = -SIZE;
                float x0 = -SIZE + segmentLength * col;
                float z0 = -SIZE + segmentLength * row;
                float x1 = x0 + segmentLength;
                float z1 = z0 + segmentLength;
                vertices[0] = x0;
                vertices[1] = y;
                vertices[2] = z0;
                vertices[3] = x1;
                vertices[4] = y;
                vertices[5] = z0;
                vertices[6] = x0;
                vertices[7] = y;
                vertices[8] = z1;
                vertices[9] = x1;
                vertices[10] = y;
                vertices[11] = z1;
                
                float s0, s1;
                s0 = col * segmentTexCoordLength;
                s1 = (col + 1) * segmentTexCoordLength;
                float t0 = 1.0f - (row + 1) * segmentTexCoordLength;
                float t1 = 1.0f - row * segmentTexCoordLength;
                texCoords[0] = s0; 
                texCoords[1] = t1; 
                texCoords[2] = s1; 
                texCoords[3] = t1; 
                texCoords[4] = s0; 
                texCoords[5] = t0; 
                texCoords[6] = s1; 
                texCoords[7] = t0;
                
                subMeshes[index] = new GVRMesh(gvrContext);
                subMeshes[index].setVertices(vertices);
                subMeshes[index].setNormals(normals);
                subMeshes[index].setTexCoords(texCoords);
                subMeshes[index].setTriangles(indices);
                grandchildren[index] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(subMeshes[index]),
                        futureTextureList.get(5));
                children[5].addChildObject(grandchildren[index]);
            }
        }

        // attached an empty renderData for parent object, so that we can set some common properties
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
    }
}
