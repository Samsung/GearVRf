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

package org.gearvrf.gamepad;

import java.io.IOException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTransform;
import org.gearvrf.utility.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Color;

public class SampleViewManager extends GVRScript {

    GVRScene mScene;
    private int colorIndex = 0;

    static List<Integer> colors = Arrays.asList(Color.WHITE, Color.YELLOW,
            Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA,
            Color.GRAY);

    // asynchronic loading of objects
    private GVRSceneObject asyncSceneObject(GVRContext context,
            String textureName) throws IOException {
        return new GVRSceneObject(context, new GVRAndroidResource(context,
                "sphere.obj"), new GVRAndroidResource(context, textureName));
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {

        mScene = gvrContext.getNextMainScene();

        // set background color
        GVRCameraRig mainCameraRig = mScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.WHITE);

        GVRSceneObject venusMeshObject = asyncSceneObject(gvrContext,
                "venusmap.jpg");
        venusMeshObject.getTransform().setPosition(-2.0f, 0.0f, -4.0f);
        venusMeshObject.getTransform().setScale(1.5f, 1.5f, 1.5f);
        mScene.addSceneObject(venusMeshObject);

        GVRSceneObject earthMeshObject = asyncSceneObject(gvrContext,
                "earthmap1k.jpg");
        earthMeshObject.getTransform().setPosition(2.0f, 0.0f, -4.0f);
        earthMeshObject.getTransform().setScale(1.5f, 1.5f, 1.5f);
        mScene.addSceneObject(earthMeshObject);

    }

    @Override
    public void onStep() {
    }

    public void processKeyEvent(int keyCode) {

        GVRCameraRig mainCameraRig = mScene.getMainCameraRig();
        int color = mainCameraRig.getLeftCamera().getBackgroundColor();

        switch (keyCode) {
        case android.view.KeyEvent.KEYCODE_BUTTON_L1:
            colorIndex = (colorIndex + 1) % colors.size();
            break;

        case android.view.KeyEvent.KEYCODE_BUTTON_R1:
            colorIndex = (colorIndex > 0) ? colorIndex - 1 : colors.size() - 1;
            break;
        }

        // change background color
        color = colors.get(colorIndex);
        mainCameraRig.getLeftCamera().setBackgroundColor(color);
        mainCameraRig.getRightCamera().setBackgroundColor(color);

    }

    public void processMotionEvent(float motionX, float motionY) {

        if (mScene.getSceneObjects().isEmpty()) {
            return;
        }

        // Translate the camera
        // the first object of the scene is the cameraRigObject
        final float SCALE = 0.5f;
        GVRTransform transform = mScene.getSceneObjects().get(0).getTransform();
        transform.setPositionX(transform.getPositionX() - SCALE * motionX);
        transform.setPositionY(transform.getPositionY() + SCALE * motionY);

    }

}
