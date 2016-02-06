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
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.utility.Log;

import android.opengl.Matrix;
import android.view.KeyEvent;
import android.view.MotionEvent;

class GVRGazeCursorController extends GVRBaseController
        implements GVRDrawFrameListener {
    private static final String TAG = GVRGazeCursorController.class
            .getSimpleName();
    private boolean isActive;
    private float x, y, z;
    private GVRContext context;

    public GVRGazeCursorController(GVRContext context,
            GVRCursorType cursorType) {
        super(cursorType);
        this.context = context;
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
        this.x = x;
        this.y = y;
        this.z = z;
        super.setPosition(x, y, z);
    }

    @Override
    public void onDrawFrame(float frameTime) {
        float[] position = new float[] { x, y, z, 1.0f };
        float[] modelMatrix = context.getMainScene().getMainCameraRig()
                .getHeadTransform().getModelMatrix();
        Matrix.multiplyMV(position, 0, modelMatrix, 0, position, 0);
        super.setPosition(position[0], position[1], position[2]);
    }
    
    void close(){
        context.unregisterDrawFrameListener(this);
    }
}