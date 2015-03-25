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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;

public class SampleViewManager extends GVRScript {
    private GVRContext mGVRContext = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        GVRMesh mesh = mGVRContext.loadMesh("cylinder.obj");

        GVRSceneObject leftScreen = new GVRSceneObject(gvrContext, mesh,
                gvrContext.loadTexture("sample_20140509_l.bmp"));
        GVRSceneObject rightScreen = new GVRSceneObject(gvrContext, mesh,
                gvrContext.loadTexture("sample_20140509_r.bmp"));

        mGVRContext.getMainScene().addSceneObject(leftScreen);
        mGVRContext.getMainScene().addSceneObject(rightScreen);
    }

    @Override
    public void onStep() {
    }

}
