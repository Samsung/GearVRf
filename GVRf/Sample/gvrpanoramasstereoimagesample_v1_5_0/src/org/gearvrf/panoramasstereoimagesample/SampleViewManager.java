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

package org.gearvrf.panoramasstereoimagesample;

import java.io.IOException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.utility.Log;

public class SampleViewManager extends GVRScript {

    private static final String TAG = "SampleViewManager";

    private GVRContext mGVRContext = null;

    private GVRActivity mActivity;

    SampleViewManager(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRMesh mesh = null;
        GVRSceneObject leftScreen = null;
        GVRSceneObject rightScreen = null;

        /*
         * This sample places its resources in the assets folder, not in
         * res/drawable and res/raw. This means that the named file may not
         * actually exist when we try to load resources, throwing an
         * IOException.
         */
        try {
            // If "cylinder.obj" exists - but is not a valid ASSIMP mesh file -
            // loadMesh() will return null.
            mesh = mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext,
                    "cylinder.obj"));

            leftScreen = new GVRSceneObject(gvrContext, mesh,
                    gvrContext.loadTexture(new GVRAndroidResource(mGVRContext,
                            "sample_20140509_l.png")));
            rightScreen = new GVRSceneObject(gvrContext, mesh,
                    gvrContext.loadTexture(new GVRAndroidResource(mGVRContext,
                            "sample_20140509_r.png")));
        } catch (IOException e) {
            e.printStackTrace();
            leftScreen = null;
            rightScreen = null;
        }

        if (mesh == null || leftScreen == null || rightScreen == null) {
            mActivity.finish();
            Log.e(TAG, "Error loading resources - stopping application!");
        }

        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        mActivity = null;

        GVRScene mainScene = mGVRContext.getNextMainScene();

        mainScene.addSceneObject(leftScreen);
        mainScene.addSceneObject(rightScreen);
    }

    @Override
    public void onStep() {
    }

}
