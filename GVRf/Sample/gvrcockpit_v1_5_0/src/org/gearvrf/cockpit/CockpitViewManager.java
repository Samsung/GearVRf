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

package org.gearvrf.cockpit;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class CockpitViewManager extends GVRScript {

    private GVRContext mGVRContext = null;
    private GVRSceneObject mShipSceneObject = null;
    private GVRSceneObject mSpaceSceneObject = null;

    @Override
    public void onInit(GVRContext gvrContext) {

        mGVRContext = gvrContext;
        GVRScene mainScene = mGVRContext.getNextMainScene();

        mainScene.getMainCameraRig().getTransform()
                .setPosition(0.0f, 6.0f, 1.0f);

        GVRMesh shipMesh = mGVRContext.loadMesh(new GVRAndroidResource(
                mGVRContext, R.raw.gvrf_ship_mesh));
        GVRMesh spaceMesh = mGVRContext.loadMesh(new GVRAndroidResource(
                mGVRContext, R.raw.gvrf_space_mesh));

        GVRTexture shipTexture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gvrf_ship_png));
        mShipSceneObject = new GVRSceneObject(gvrContext, shipMesh, shipTexture);
        GVRTexture spaceTexture = gvrContext
                .loadTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.gvrf_space_png));
        mSpaceSceneObject = new GVRSceneObject(gvrContext, spaceMesh,
                spaceTexture);

        mainScene.addSceneObject(mShipSceneObject);
        mainScene.addSceneObject(mSpaceSceneObject);

    }

    @Override
    public void onStep() {
    }

}
