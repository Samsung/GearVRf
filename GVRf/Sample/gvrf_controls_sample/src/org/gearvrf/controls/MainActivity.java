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

package org.gearvrf.controls;

import android.os.Bundle;
import android.view.InputDevice;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.controls.input.GamepadInput;
import org.gearvrf.controls.input.TouchPadInput;
import org.gearvrf.controls.util.VRSamplesTouchPadGesturesDetector;
import org.gearvrf.controls.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

public class MainActivity extends GVRActivity implements
        VRSamplesTouchPadGesturesDetector.OnTouchPadGestureListener {

    private VRSamplesTouchPadGesturesDetector mDetector = null;
    private MainScript mainScript;
    private static final int TAP_INTERVAL = 300;
    private long mLatestTap = 0;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mainScript = new MainScript();
        setScript(mainScript, "gvr_note4.xml");
        mDetector = new VRSamplesTouchPadGesturesDetector(this, this);
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        boolean handled = false;

        GamepadInput.input(event);

        if (handled) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {

        boolean handled = false;

        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                && event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            GamepadInput.input(event);

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                GamepadInput.processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            handled = GamepadInput.processJoystickInput(event, -1);

            if (handled) {
                return true;
            } else {
                return super.dispatchGenericMotionEvent(event);
            }
        }
        return handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDetector == null) {
            return false;
        }
        TouchPadInput.input(event);
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
            mLatestTap = System.currentTimeMillis();
            TouchPadInput.onSingleTap();
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        TouchPadInput.onLongPress();
    }

    @Override
    public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection, float velocityX,
            float velocityY) {
        TouchPadInput.onSwipe(swipeDirection);

        return false;
    }

    @Override
    public void onSwiping(MotionEvent e, MotionEvent e2, float velocityX, float velocityY,
            SwipeDirection swipeDirection) {
    }

    @Override
    public void onSwipeOppositeLastDirection() {
    }
}
