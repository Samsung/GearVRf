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

package org.gearvrf.testcube;

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.testcube.R;
import org.gearvrf.scene_objects.GVRCubeSceneObject;

import android.util.Log;

public class SampleCubeScript extends GVRScript {

    private static final float CUBE_WIDTH = 20.0f;
    private GVRContext mGVRContext = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getNextMainScene();

        ArrayList<Future<GVRTexture>> futureTextureList = new ArrayList<Future<GVRTexture>>(
                6);
        futureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.back)));
        futureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.right)));
        futureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.front)));
        futureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.left)));
        futureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.top)));
        futureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.bottom)));
        GVRSceneObject mCube = new GVRCubeSceneObject(gvrContext,
                false, futureTextureList);
        mCube.setName("cube");
        mCube.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                CUBE_WIDTH);
        scene.addSceneObject(mCube);

        for (GVRSceneObject so : scene.getWholeSceneObjects()) {
            Log.v("", "scene object name : " + so.getName());
        }
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }
}
