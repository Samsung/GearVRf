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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.joml.Vector3f;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

class GVRGazeCursorController extends GVRBaseController
        implements GVRDrawFrameListener {
    private static final String TAG = GVRGazeCursorController.class
            .getSimpleName();
    private final KeyEvent BUTTON_GAZE_DOWN = new KeyEvent(KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_BUTTON_1);
    private final KeyEvent BUTTON_GAZE_UP = new KeyEvent(KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_BUTTON_1);

    private final GVRContext context;
    private int referenceCount = 0;

    // Used to calculate the absolute position that the controller reports to
    // the user.
    private final Vector3f gazePosition;

    // Saves the relative position of the cursor with respect to the camera.
    private final Vector3f setPosition;

    public GVRGazeCursorController(GVRContext context,
            GVRCursorType cursorType) {
        super(cursorType);
        this.context = context;
        gazePosition = new Vector3f();
        setPosition = new Vector3f();        
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
        if (clone.getAction() == MotionEvent.ACTION_DOWN) {
            // report ACTION_DOWN as a button
            setKeyEvent(BUTTON_GAZE_DOWN);
        } else if (clone.getAction() == MotionEvent.ACTION_UP) {
            // report ACTION_UP as a button
            setKeyEvent(BUTTON_GAZE_UP);
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
        setPosition.mulPoint(context.getMainScene().getMainCameraRig()
                .getHeadTransform().getModelMatrix4f(), gazePosition);
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