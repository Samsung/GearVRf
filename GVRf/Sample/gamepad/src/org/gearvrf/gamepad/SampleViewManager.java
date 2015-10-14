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

import java.util.Arrays;
import java.util.List;

import android.graphics.Color;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class SampleViewManager extends GVRScript {

    GVRScene mScene;
    private int colorIndex = 0;
    private static final String TAG = "SampleViewManager";

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

    // getCenteredAxis() and some other code are referenced from:
    // https://developer.android.com/training/game-controllers/controller-input.html
    private static float getCenteredAxis(MotionEvent event, InputDevice device,
            int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis,
                event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value = historyPos < 0 ? event.getAxisValue(axis)
                    : event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public boolean processJoystickInput(MotionEvent event, int historyPos) {
        if (mScene.getSceneObjects().isEmpty()) {
            return false;
        }

        InputDevice mInputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X,
                historyPos);
        float hatx = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_HAT_X, historyPos);
        
        // Google refers to the second analog x axis as z, 
        // the Samsung Gamepad refers to is as RX.
        float rx = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RX,
                historyPos);

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y,
                historyPos);

        float haty = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_HAT_Y, historyPos);

        // Google refers to the second analog y axis as rz, 
        // the Samsung Gamepad refers to is as RY.
        float ry = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RY,
                historyPos);

        android.util.Log.d(TAG, "x = " + x);
        android.util.Log.d(TAG, "hatx = " + hatx);
        android.util.Log.d(TAG, "rx = " + rx);

        android.util.Log.d(TAG, "y = " + y);
        android.util.Log.d(TAG, "haty = " + haty);
        android.util.Log.d(TAG, "ry = " + ry);

        // Translate the camera
        // the first object of the scene is the cameraRigObject
        final float SCALE = 0.5f;
        GVRTransform transform = mScene.getSceneObjects().get(0).getTransform();
        transform.setPositionX(transform.getPositionX() - SCALE * hatx);
        transform.setPositionY(transform.getPositionY() + SCALE * haty);

        return true;
    }

    public boolean processKeyEvent(int keyCode) {
        GVRCameraRig mainCameraRig = mScene.getMainCameraRig();
        int color = mainCameraRig.getLeftCamera().getBackgroundColor();

        boolean handled = false;

        switch (keyCode) {
        case KeyEvent.KEYCODE_BUTTON_L1:
            android.util.Log.d(TAG, "button L1");
            colorIndex = (colorIndex + 1) % colors.size();
            handled = true;
            break;

        case KeyEvent.KEYCODE_BUTTON_R1:
            android.util.Log.d(TAG, "button R1");
            colorIndex = (colorIndex > 0) ? colorIndex - 1 : colors.size() - 1;
            handled = true;
            break;
        case KeyEvent.KEYCODE_BUTTON_A:
            android.util.Log.d(TAG, "button .");
            handled = true;
            break;
        case KeyEvent.KEYCODE_BUTTON_B:
            android.util.Log.d(TAG, "button ..");
            handled = true;
            break;
        case KeyEvent.KEYCODE_BUTTON_X:
            android.util.Log.d(TAG, "button ...");
            handled = true;
            break;
        case KeyEvent.KEYCODE_BUTTON_Y:
            android.util.Log.d(TAG, "button ....");
            handled = true;
            break;
        case KeyEvent.KEYCODE_BUTTON_START:
            android.util.Log.d(TAG, "button START");
            handled = true;
            break;
        case KeyEvent.KEYCODE_BUTTON_SELECT:
            android.util.Log.d(TAG, "button SELECT");
            handled = true;
            break;
        }

        // change background color
        if (handled) {
            color = colors.get(colorIndex);
            mainCameraRig.getLeftCamera().setBackgroundColor(color);
            mainCameraRig.getRightCamera().setBackgroundColor(color);
            return true;
        }
        return false;
    }

}
