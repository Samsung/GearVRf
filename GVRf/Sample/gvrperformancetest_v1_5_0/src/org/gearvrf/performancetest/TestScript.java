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

package org.gearvrf.performancetest;

import java.io.IOException;
import java.util.Random;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;

import android.util.Log;

public class TestScript extends GVRScript {

    private static final String TAG = "TestScript";

    private static final int numberOfBunnies = 20;
    private static final String[] textureNames = { "texture1.jpg",
            "texture2.jpg", "texture3.jpg", "texture4.jpg", "texture5.jpg" };
    private GVRScene mMainScene = null;

    GVRAnimationEngine mAnimationEngine;

    private GVRActivity mActivity;

    TestScript(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {

        mAnimationEngine = gvrContext.getAnimationEngine();

        mMainScene = gvrContext.getNextMainScene();

        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();

        GVRCamera leftCamera = mainCameraRig.getLeftCamera();
        GVRCamera rightCamera = mainCameraRig.getRightCamera();

        leftCamera.setBackgroundColorR(0.2f);
        leftCamera.setBackgroundColorG(0.2f);
        leftCamera.setBackgroundColorB(0.2f);
        leftCamera.setBackgroundColorA(1.0f);

        rightCamera.setBackgroundColorR(0.2f);
        rightCamera.setBackgroundColorG(0.2f);
        rightCamera.setBackgroundColorB(0.2f);
        rightCamera.setBackgroundColorA(1.0f);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        for (int i = 0; i < numberOfBunnies; ++i) {

            GVRSceneObject bunny = null;
            try {
                // we assume that the mesh and the textures are valid
                bunny = new GVRSceneObject(gvrContext,
                        gvrContext.loadMesh(new GVRAndroidResource(gvrContext,
                                "bunny.obj")),
                        gvrContext.loadTexture(new GVRAndroidResource(
                                gvrContext, textureNames[i
                                        % textureNames.length])));
            } catch (IOException e) {
                e.printStackTrace();
                mActivity.finish();
                Log.e(TAG,
                        "Mesh or texture were not loaded. Stopping application!");
            }

            Random random = new Random();

            bunny.getTransform().setPosition(0.0f, 0.0f,
                    random.nextFloat() * 3.0f + 2.0f);
            bunny.getTransform().rotateByAxisWithPivot(
                    random.nextFloat() * 360.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f);
            bunny.getTransform().rotateByAxisWithPivot(
                    random.nextFloat() * 360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f);
            bunny.getTransform().rotateByAxisWithPivot(
                    random.nextFloat() * 360.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                    0.0f);
            bunny.getTransform().translate(0.0f, 0.0f, -10.0f);
            mainCameraRig.addChildObject(bunny);

            float x = random.nextFloat() - 0.5f;
            float y = random.nextFloat() - 0.5f;
            float z = random.nextFloat() - 0.5f;
            float length = (float) Math.sqrt(x * x + y * y + z * z);
            x /= length;
            y /= length;
            z /= length;

            new GVRRotationByAxisWithPivotAnimation(bunny, //
                    5.0f + random.nextFloat() * 25.0f, //
                    360.0f, //
                    x, y, z, //
                    0.0f, 0.0f, -10.0f) //
                    .setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1) //
                    .start(mAnimationEngine);
        }
    }

    @Override
    public void onStep() {
    }

}
