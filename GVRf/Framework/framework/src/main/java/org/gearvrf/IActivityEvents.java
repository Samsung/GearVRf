/* Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.io.GVRGearCursorController;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * This interface defines the callback interface of an Android {@code Activity}.
 * User can add a listener to {@code GVRActivity.getEventReceiver()} to handle
 * these events, rather than subclassing {@link GVRActivity}.
 */
public interface IActivityEvents extends IEvents {
    void onPause();

    void onResume();

    void onDestroy();

    void onSetMain(GVRMain script);

    void onWindowFocusChanged(boolean hasFocus);

    void onConfigurationChanged(Configuration config);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onTouchEvent(MotionEvent event);

    /**
     * Invoked every frame with the latest controller position and orientation; the parameters
     * should be copied if they need to be used after the callback returns.
     *
     * @param keys                  one or more CONTROLLER_KEYS
     * @param position              X, Y, Z position of the controller
     * @param orientation           orientation of the controller as a quaternion
     * @param touchpadPoint         X, Y position on the touchpad
     * @param touched               true if pad is touched
     * @param angularAcceleration   angularAcceleration of the controller
     * @param angularVelocity       angularVelocity of the controller
     */
    void onControllerEvent(GVRGearCursorController.CONTROLLER_KEYS[] keys, Vector3f position, Quaternionf orientation, PointF touchpadPoint, boolean touched, Vector3f angularAcceleration,
                           Vector3f angularVelocity);

    void dispatchTouchEvent(MotionEvent event);
    void dispatchKeyEvent(KeyEvent event);
}
