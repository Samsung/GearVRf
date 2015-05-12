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

public class GVRCubeSceneObject extends GVRSceneObject {

    //       ----------        ---------7       11--------10
    //     / .       /|      / .       /|      / .       /|
    //    2--.------3 |     ---.------6 |     ---.------- |
    //    |  .      | |     |  .      | |     |  .      | |
    //    |  .......|.|     |  .......|.5     |  9......|.8
    //    | /       |/      | /       |/      | /       |/
    //    0---------1       ----------4       -----------
    //
    //     14----------      18--------19        ---------- 
    //     / .       /|      / .       /|      / .       /|
    //   15--.------- |    16--.-----17 |     ---.------- |
    //    |  .      | |     |  .      | |     |  .      | |
    //    | 12......|.|     |  .......|.|     | 20......|21
    //    | /       |/      | /       |/      | /       |/
    //   13----------       -----------       22-------23
     //
    private static final float SIZE = 0.5f;

    private float[] vertices = {
        -SIZE, -SIZE,  SIZE,  //  0
         SIZE, -SIZE,  SIZE,  //  1 
        -SIZE,  SIZE,  SIZE,  //  2
         SIZE,  SIZE,  SIZE,  //  3

         SIZE, -SIZE,  SIZE,  //  4
         SIZE, -SIZE, -SIZE,  //  5 
         SIZE,  SIZE,  SIZE,  //  6
         SIZE,  SIZE, -SIZE,  //  7

         SIZE, -SIZE, -SIZE,  //  8
        -SIZE, -SIZE, -SIZE,  //  9
         SIZE,  SIZE, -SIZE,  // 10
        -SIZE,  SIZE, -SIZE,  // 11

        -SIZE, -SIZE, -SIZE,  // 12
        -SIZE, -SIZE,  SIZE,  // 13
        -SIZE,  SIZE, -SIZE,  // 14
        -SIZE,  SIZE,  SIZE,  // 15

        -SIZE,  SIZE,  SIZE,  // 16
         SIZE,  SIZE, -SIZE,  // 17
        -SIZE,  SIZE, -SIZE,  // 18
        -SIZE,  SIZE,  SIZE,  // 19

        -SIZE, -SIZE, -SIZE,  // 20
         SIZE, -SIZE, -SIZE,  // 21
        -SIZE, -SIZE,  SIZE,  // 22
         SIZE, -SIZE,  SIZE,  // 23
    };

    private float[] normals = { 
         0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f,
         1.0f,  0.0f,  0.0f, 1.0f,  0.0f,  0.0f, 1.0f,  0.0f,  0.0f, 1.0f,  0.0f,  0.0f,
         0.0f,  0.0f,  1.0f, 0.0f,  0.0f,  1.0f, 0.0f,  0.0f,  1.0f, 0.0f,  0.0f,  1.0f,
        -1.0f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,
         0.0f,  1.0f,  1.0f, 0.0f,  1.0f,  1.0f, 0.0f,  1.0f,  1.0f, 0.0f,  1.0f,  1.0f,
         0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f
    };

    private float[] texCoords = {
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 
    };

    private char[] indices = {
         0,  1,  2,  // front
         2,  1,  3,  

         4,  5,  6,  // right
         6,  5,  7,  

         8,  9, 10,  // back
         10, 9, 11,  

        12, 13, 14,  // left
        14, 13, 15,  // 

        16, 17, 18,  // top
        18, 17, 19,  // 

        20, 21, 22,  // bottom 
        22, 21, 23   // 
    };


    /**
     * Constructs a cube scene object.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRCubeSceneObject(GVRContext gvrContext) {
        super(gvrContext);
        
        GVRMesh mesh = new GVRMesh(gvrContext);
        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texCoords);
        mesh.setTriangles(indices);

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

}

