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

package org.gearvrf.io;

import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

class GVRGazeCursorController extends GVRBaseController implements
        GVRDrawFrameListener {
    private static final String TAG = GVRGazeCursorController.class
            .getSimpleName();
    private static final int SET_KEY_DOWN = 1;
    private static final int TAP_TIMEOUT = 160;
    private static float TOUCH_SQUARE = 8.0f * 8.0f;
    private static final float DEPTH_SENSITIVITY = 0.1f;
    private final KeyEvent BUTTON_GAZE_DOWN = new KeyEvent(
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_1);
    private final KeyEvent BUTTON_GAZE_UP = new KeyEvent(KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_BUTTON_1);
    private final GVRContext context;
    private int referenceCount = 0;
    private boolean keyEventSent = false;
    private float actionDownX;
    private float actionDownY;
    private float actionDownZ;

    // Used to calculate the absolute position that the controller reports to
    // the user.
    private final Vector3f gazePosition;
    private Object lock = new Object();

    // Saves the relative position of the cursor with respect to the camera.
    private final Vector3f setPosition;
    private GestureHandler handler;

    public GVRGazeCursorController(GVRContext context,
                                   GVRControllerType controllerType, String name, int vendorId,
                                   int productId) {
        super(controllerType, name, vendorId, productId);
        this.context = context;
        gazePosition = new Vector3f();
        setPosition = new Vector3f();
        handler = new GestureHandler();
    }

    // Use a handler like the one used in the gesture detector.
    private class GestureHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_KEY_DOWN:
                    keyEventSent = true;
                    setKeyEvent(BUTTON_GAZE_DOWN);
                    break;
                default:
                    Log.e(TAG, "Unknown message type");
            }
        }
    }

    /*
     * The increment the reference count to let the cursor controller know how
     * many input devices are using this controller.
     */
    void incrementReferenceCount() {
        referenceCount++;
        if (referenceCount == 1) {
            context.registerDrawFrameListener(this);
        }
    }

    /*
     * The decrement the reference count to let the cursor controller know how
     * many input devices are using this controller.
     */
    void decrementReferenceCount() {
        referenceCount--;
        // no more devices
        if (referenceCount == 0) {
            context.unregisterDrawFrameListener(this);
        }
    }

    @Override
    boolean dispatchKeyEvent(KeyEvent event) {
        setKeyEvent(event);
        return true;
    }

    @Override
    boolean dispatchMotionEvent(MotionEvent event) {
        MotionEvent clone = MotionEvent.obtain(event);
        float eventX = event.getX();
        float eventY = event.getY();
        int action = clone.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            actionDownX = eventX;
            actionDownY = eventY;
            actionDownZ = setPosition.z;
            // report ACTION_DOWN as a button
            handler.sendEmptyMessageAtTime(SET_KEY_DOWN, event.getDownTime()
                    + TAP_TIMEOUT);
        } else if (action == MotionEvent.ACTION_UP) {
            // report ACTION_UP as a button
            handler.removeMessages(SET_KEY_DOWN);
            if (keyEventSent) {
                setKeyEvent(BUTTON_GAZE_UP);
                keyEventSent = false;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            float deltaX = eventX - actionDownX;
            float deltaY = eventY - actionDownY;
            float eventZ = actionDownZ + (deltaX * DEPTH_SENSITIVITY);

            if (eventZ >= getNearDepth()) {
                eventZ = getNearDepth();
            }
            if (eventZ <= getFarDepth()) {
                eventZ = getFarDepth();
            }

            synchronized (lock) {
                setPosition.z = eventZ;
            }
            float distance = (deltaX * deltaX) + (deltaY * deltaY);
            if (distance > TOUCH_SQUARE) {
                handler.removeMessages(SET_KEY_DOWN);
            }
        }
        setMotionEvent(clone);
        return true;
    }

    @Override
    public void setPosition(float x, float y, float z) {
        setPosition.set(x, y, z);
        super.setPosition(x, y, z);
    }

    @Override
    public void onDrawFrame(float frameTime) {
        synchronized (lock) {
            setPosition.mulPoint(context.getMainScene().getMainCameraRig()
                    .getHeadTransform().getModelMatrix4f(), gazePosition);
        }
        super.setPosition(gazePosition.x, gazePosition.y, gazePosition.z);
    }

    void close() {
        // unregister the draw frame listener
        if (referenceCount > 0) {
            context.unregisterDrawFrameListener(this);
        }
        referenceCount = 0;
    }
}