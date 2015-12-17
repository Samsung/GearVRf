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

package org.gearvrf.complexScene;

import java.io.IOException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.debug.GVRConsole;
import org.gearvrf.utility.Log;

import android.graphics.Color;

public class SampleViewManager extends GVRScript {

    private GVRContext mGVRContext;

    private GVRConsole console;
    
    private ColorShader mColorShader = null;

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;
        mColorShader = new ColorShader(mGVRContext);

        // set background color
        GVRScene scene = gvrContext.getMainScene();
        scene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(Color.WHITE);
        scene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(Color.WHITE);

        
        try {

            GVRMesh mesh = mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext,
                    "bunny.obj"));
            
            final int OBJECTS_CNT = 5;
            for (int x=-OBJECTS_CNT; x<=OBJECTS_CNT; ++x) {
                for (int y=-OBJECTS_CNT; y<=OBJECTS_CNT; ++y) {
                    GVRSceneObject sceneObject = getColorMesh(1.0f, mesh);
                    sceneObject.getTransform().setPosition(1.0f*x, 1.0f*y, -5.0f);
                    sceneObject.getTransform().setScale(0.5f, 0.5f, 1.0f);
                    scene.addSceneObject(sceneObject); 
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        

    }

    public boolean processKeyEvent(int keyCode) {

        return false;
    }

    @Override
    public void onStep() {
    }

    private GVRSceneObject getColorMesh(float scale, GVRMesh mesh) {
        GVRMaterial material = new GVRMaterial(mGVRContext,
                mColorShader.getShaderId());
        material.setVec4(ColorShader.COLOR_KEY, 1.0f,
                0.0f, 1.0f, 1.0f);

        GVRSceneObject meshObject = null;
        meshObject = new GVRSceneObject(mGVRContext, mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);

        return meshObject;
    }
    
}


