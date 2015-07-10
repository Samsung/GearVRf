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

package org.gearvrf.gvroutlinesample;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;

public class OutlineScript extends GVRScript {

    private GVRContext mGVRContext;
    private GVRSceneObject mCube;

    private static final float ROTATION_SPEED = 1.0f;
    private static final float SIZE = 0.5f;

    private float[] vertices = { -SIZE, -SIZE, SIZE, // 0
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

    // Smoothed Normals
    private float[] normals = { -0.57735f, -0.57735f, 0.57735f, // 0
            0.57735f, -0.57735f, 0.57735f, // 1
            -0.57735f, 0.57735f, 0.57735f, // 2
            0.57735f, 0.57735f, 0.57735f, // 3
            0.57735f, -0.57735f, 0.57735f, // 4
            0.57735f, -0.57735f, -0.57735f, // 5
            0.57735f, 0.57735f, 0.57735f, // 6
            0.57735f, 0.57735f, -0.57735f, // 7
            0.57735f, -0.57735f, -0.57735f, // 8
            -0.57735f, -0.57735f, -0.57735f, // 9
            0.57735f, 0.57735f, -0.57735f, // 10
            -0.57735f, 0.57735f, -0.57735f, // 11
            -0.57735f, -0.57735f, -0.57735f, // 12
            -0.57735f, -0.57735f, 0.57735f, // 13
            -0.57735f, 0.57735f, -0.57735f, // 14
            -0.57735f, 0.57735f, 0.57735f, // 15
            -0.57735f, 0.57735f, 0.57735f, // 16
            0.57735f, 0.57735f, 0.57735f, // 17
            -0.57735f, 0.57735f, -0.57735f, // 18
            0.57735f, 0.57735f, -0.57735f, // 19
            -0.57735f, -0.57735f, -0.57735f, // 20
            0.57735f, -0.57735f, -0.57735f, // 21
            -0.57735f, -0.57735f, 0.57735f, // 22
            0.57735f, -0.57735f, 0.57735f }; // 23

    private float[] texCoords = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, // front
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // right
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // back
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // left
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // bottom
    };

    private char[] indices = { 0, 1, 2, // front
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

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        GVRScene outlineScene = gvrContext.getNextMainScene();

        // Create Mesh Manually - We do so because we need smooth normals
        // ---------------------------------------------------------------
        GVRMesh cubeMesh = new GVRMesh(mGVRContext);

        cubeMesh.setVertices(vertices);
        cubeMesh.setTexCoords(texCoords);
        cubeMesh.setNormals(normals);
        cubeMesh.setTriangles(indices);

        mCube = new GVRSceneObject(mGVRContext, cubeMesh);
        mCube.getTransform().setPosition(0.0f, 0.0f, -5.0f);

        // Create Base Material Pass
        // ---------------------------------------------------------------
        OutlineShader outlineShader = new OutlineShader(mGVRContext);
        GVRMaterial outlineMaterial = new GVRMaterial(mGVRContext,
                outlineShader.getShaderId());

        // Brown-ish outline color
        outlineMaterial.setVec4(OutlineShader.COLOR_KEY, 0.4f, 0.1725f,
                0.1725f, 1.0f);
        outlineMaterial.setFloat(OutlineShader.THICKNESS_KEY, 0.2f);

        // For outline we want to cull front faces
        mCube.getRenderData().setMaterial(outlineMaterial);
        mCube.getRenderData().setCullFace(GVRCullFaceEnum.Front);

        // Create Additional Pass
        // ----------------------------------------------------------------
        // load texture
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gearvr_logo));

        GVRMaterial material = new GVRMaterial(mGVRContext,
                GVRShaderType.Texture.ID);
        material.setMainTexture(texture);

        GVRRenderPass pass = new GVRRenderPass(mGVRContext);
        pass.setMaterial(material);
        pass.setCullFace(GVRCullFaceEnum.Back);
        
        mCube.getRenderData().addPass(pass);
        
        // Finally Add Cube to Scene
        outlineScene.addSceneObject(mCube);
    }

    @Override
    public void onStep() {
        if (mCube != null) {
            mCube.getTransform().rotateByAxis(ROTATION_SPEED, 0.7071f, 0.7071f, 0.0f);
        }
    }

}
