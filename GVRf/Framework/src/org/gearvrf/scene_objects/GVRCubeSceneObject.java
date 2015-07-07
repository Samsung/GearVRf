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
    //
    //     14----------      18--------19        ---------- 
    //     / .       /|      / .       /|      / .       /|
    //   15--.------- |    16--.-----17 |     ---.------- |
    //    |  .      | |     |  .      | |     |  .      | |
    //    | 12......|.|     |  .......|.|     | 20......|21
    //    | /       |/      | /       |/      | /       |/
    //   13---------        ----------        22-------23
    //@formatter:on

    private float[] simple_vertices = { -SIZE, -SIZE, SIZE, // 0
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

    private float[] simple_outward_normals = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
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

    private float[] simple_inward_normals = { 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
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

    private float[] simple_texCoords = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, // front
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // right
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // back
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // left
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // bottom
    };

    private char[] simple_outward_indices = { 0, 1, 2, // front
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

    private char[] simple_inward_indices = { 0, 2, 1, // front
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

        createSimpleCube(gvrContext, true, new GVRMaterial(gvrContext));
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

        createSimpleCube(gvrContext, facingOut, new GVRMaterial(gvrContext));
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
     *            the texture for six faces. {@code Future<{@code GVRTexture}>}
     *            is used here for asynchronously loading the texture.
     */
    public GVRCubeSceneObject(GVRContext gvrContext, boolean facingOut,
            Future<GVRTexture> futureTexture) {
        super(gvrContext);

        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(futureTexture);
        createSimpleCube(gvrContext, facingOut, material);
    }

    /**
     * Constructs a cube scene object with each side of length 1.
     * 
     * The cube's triangles and normals are facing either in or out and the same
     * texture will be applied to each side of the cube. All six faces share the
     * same material (i.e. same texture and same shader).
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

        createSimpleCube(gvrContext, facingOut, material);
    }

    /**
     * Constructs a cube scene object with each side of length 1.
     * 
     * The cube's triangles and normals are facing either in or out and the same
     * texture will be applied to each side of the cube. Each face has its own
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
     *            the list of six textures for six faces. {@code Future<
     *            {@code GVRTexture}>} is used here for asynchronously loading
     *            the texture. The six textures are for front, right, back,
     *            left, top, and bottom faces respectively.
     */
    public GVRCubeSceneObject(GVRContext gvrContext, boolean facingOut,
            ArrayList<Future<GVRTexture>> futureTextureList) {
        super(gvrContext);

        createSimpleCubeSixMeshes(gvrContext, facingOut, futureTextureList);
    }

    private void createSimpleCube(GVRContext gvrContext, boolean facingOut,
            GVRMaterial material) {

        GVRMesh mesh = new GVRMesh(gvrContext);

        if (facingOut) {
            mesh.setVertices(simple_vertices);
            mesh.setNormals(simple_outward_normals);
            mesh.setTexCoords(simple_texCoords);
            mesh.setTriangles(simple_outward_indices);
        } else {
            mesh.setVertices(simple_vertices);
            mesh.setNormals(simple_inward_normals);
            mesh.setTexCoords(simple_texCoords);
            mesh.setTriangles(simple_inward_indices);
        }

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMaterial(material);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

    private char[] simple_outward_front_indices = { 0, 1, 2, // front
            2, 1, 3 };
    private char[] simple_outward_right_indices = { 4, 5, 6, // right
            6, 5, 7 };
    private char[] simple_outward_back_indices = { 8, 9, 10, // back
            10, 9, 11 };
    private char[] simple_outward_left_indices = { 12, 13, 14, // left
            14, 13, 15 };
    private char[] simple_outward_top_indices = { 16, 17, 18, // top
            18, 17, 19 };
    private char[] simple_outward_bottom_indices = { 20, 21, 22, // bottom
            22, 21, 23 };

    private char[] simple_inward_front_indices = { 0, 2, 1, // front
            1, 2, 3 };
    private char[] simple_inward_right_indices = { 4, 6, 5, // right
            5, 6, 7 };
    private char[] simple_inward_back_indices = { 8, 10, 9, // back
            9, 10, 11 };
    private char[] simple_inward_left_indices = { 12, 14, 13, // left
            13, 14, 15 };
    private char[] simple_inward_top_indices = { 16, 18, 17, // top
            17, 18, 19 };
    private char[] simple_inward_bottom_indices = { 20, 22, 21, // bottom
            21, 22, 23 };

    private void createSimpleCubeSixMeshes(GVRContext gvrContext,
            boolean facingOut, ArrayList<Future<GVRTexture>> futureTextureList) {

        GVRSceneObject[] children = new GVRSceneObject[6];
        GVRMesh[] meshes = new GVRMesh[6];
        for (int i = 0; i < 6; i++) {
            meshes[i] = new GVRMesh(gvrContext);
        }

        if (facingOut) {
            meshes[0].setTriangles(simple_outward_front_indices);
            meshes[1].setTriangles(simple_outward_right_indices);
            meshes[2].setTriangles(simple_outward_back_indices);
            meshes[3].setTriangles(simple_outward_left_indices);
            meshes[4].setTriangles(simple_outward_top_indices);
            meshes[5].setTriangles(simple_outward_bottom_indices);
            for (int i = 0; i < 6; i++) {
                meshes[i].setVertices(simple_vertices);
                meshes[i].setNormals(simple_outward_normals);
                meshes[i].setTexCoords(simple_texCoords);
                children[i] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(meshes[i]),
                        futureTextureList.get(i));
                addChildObject(children[i]);
            }
        } else {
            meshes[0].setTriangles(simple_inward_front_indices);
            meshes[1].setTriangles(simple_inward_right_indices);
            meshes[2].setTriangles(simple_inward_back_indices);
            meshes[3].setTriangles(simple_inward_left_indices);
            meshes[4].setTriangles(simple_inward_top_indices);
            meshes[5].setTriangles(simple_inward_bottom_indices);
            for (int i = 0; i < 6; i++) {
                meshes[i].setVertices(simple_vertices);
                meshes[i].setNormals(simple_inward_normals);
                meshes[i].setTexCoords(simple_texCoords);
                children[i] = new GVRSceneObject(gvrContext,
                        new FutureWrapper<GVRMesh>(meshes[i]),
                        futureTextureList.get(i));
                addChildObject(children[i]);
            }
        }
    }
}
