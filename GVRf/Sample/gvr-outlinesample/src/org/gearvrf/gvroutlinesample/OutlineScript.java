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

import java.io.IOException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;

import android.util.Log;

public class OutlineScript extends GVRScript {

	private static final String TAG = "OutlineSample";
	
    private GVRContext mGVRContext = null;
    private GVRActivity mActivity = null;
    private GVRSceneObject mCube = null;
    
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

    private float[] normals = { 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f };

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

    OutlineScript(GVRActivity activity) {
        mActivity = activity;
    }
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        GVRScene outlineScene = gvrContext.getNextMainScene();

        // load texture
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gearvr_logo));

        GVRMaterial material = new GVRMaterial(mGVRContext);
        material.setMainTexture(texture);
        
        OutlineShader outlineShader = new OutlineShader(mGVRContext);
        GVRMaterial outlineMaterial = new GVRMaterial(mGVRContext, outlineShader.getShaderId());
        outlineMaterial.setVec4(OutlineShader.COLOR_KEY, 1.0f,  0.0f, 0.0f, 1.0f);
        outlineMaterial.setFloat(OutlineShader.THICKNESS_KEY, 0.3f);
        
        // Loading Mesh from FBX
        // ---------------------------------------------------------------
        //GVRSceneObject cube = new GVRSceneObject(mGVRContext);
        //GVRRenderData cubeRenderData = new GVRRenderData(mGVRContext);
            
        //GVRMesh fbxMesh = null;
        //try
        //{
        //	GVRAndroidResource resource = new GVRAndroidResource(mGVRContext, "box_groups.fbx");
        //	fbxMesh = mGVRContext.loadMesh(resource);
        //}
        //catch (IOException e)
        //{
        //	e.printStackTrace();
        //    mActivity.finish();
        //    Log.e(TAG, "Assets were not loaded. Stopping application!");
        //}
        
        //cubeRenderData.setMesh(cubeMesh);
        //cubeRenderData.setMaterial(material);
        //cube.attachRenderData(cubeRenderData);
        //outlineScene.addSceneObject(cube);
        
        
        // Creating Mesh Manually
        // ---------------------------------------------------------------
        GVRMesh cubeMesh = new GVRMesh(mGVRContext);
        
        cubeMesh.setVertices(vertices);
		cubeMesh.setTexCoords(texCoords);
		cubeMesh.setNormals(normals);
		cubeMesh.setTriangles(indices);
		
		mCube = new GVRSceneObject(mGVRContext, cubeMesh);
		mCube.getRenderData().setMaterial(material);
		//mCube.getRenderData().addPass(outlineMaterial);
		
		mCube.getTransform().setPosition(0.0f, 0.0f, -3.0f);
		outlineScene.addSceneObject(mCube);
    }

    @Override
    public void onStep() {
    	if (mCube != null) {
    		mCube.getTransform().rotateByAxis(ROTATION_SPEED, 1.0f, 1.0f, 0.0f);
    	}
    }

}
