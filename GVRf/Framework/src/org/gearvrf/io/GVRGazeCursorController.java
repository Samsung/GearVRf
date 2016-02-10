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
import android.view.KeyEvent;
import android.view.MotionEvent;

class GVRGazeCursorController extends GVRBaseController
        implements GVRDrawFrameListener {
    private static final String TAG = GVRGazeCursorController.class
            .getSimpleName();
    private boolean isActive;
    private final GVRContext context;

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
        context.registerDrawFrameListener(this);
    }

    @Override
    boolean dispatchKeyEvent(KeyEvent event) {
        // Not used.
        return false;
    }

    @Override
    boolean dispatchMotionEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && isActive == false) {
            this.isActive = true;
            setActive(isActive);
        } else if (event.getAction() == MotionEvent.ACTION_UP
                && isActive == true) {
            this.isActive = false;
            setActive(isActive);
        }
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
        context.unregisterDrawFrameListener(this);
    }
}