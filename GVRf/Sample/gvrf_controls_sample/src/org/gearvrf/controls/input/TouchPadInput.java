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

package org.gearvrf.controls.input;

import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.controls.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

public class TouchPadInput {

    private static TouchPadInputMap newTouchPadMap = new TouchPadInputMap();
    private static TouchPadInputMap oldTouchPadMap = new TouchPadInputMap();

    public static void process() {
        newTouchPadMap.resetIntermadiateState();

        Button oldButton = oldTouchPadMap.buttonState;
        Button newButton = newTouchPadMap.buttonState;

        updateIntermediateStates(newButton, oldButton);

    }

    public static TouchPadInputMap getCurrent() {

        return newTouchPadMap;
    }

    private static void updateIntermediateStates(Button newButton, Button oldButton) {

        if (oldButton.pressed != newButton.pressed) {
            newButton.down = newButton.pressed;
            newButton.up = !newButton.pressed;
        }

        updateSwipe();

        updateLongPress();

        updateTap();

        oldButton.replicateValues(newButton);
    }

    private static void updateLongPress() {
        if (newTouchPadMap.buttonState.longPressed == oldTouchPadMap.buttonState.longPressed) {
            if (newTouchPadMap.buttonState.longPressed) {

                newTouchPadMap.buttonState.longPressed = false;
            }

        }
        oldTouchPadMap.buttonState.longPressed = newTouchPadMap.buttonState.longPressed;
    }

    private static void updateTap() {
        if (newTouchPadMap.buttonState.singleTaped == oldTouchPadMap.buttonState.singleTaped) {
            if (newTouchPadMap.buttonState.singleTaped) {

                newTouchPadMap.buttonState.singleTaped = false;
            }

        }
        oldTouchPadMap.buttonState.singleTaped = newTouchPadMap.buttonState.singleTaped;
    }

    private static void updateSwipe() {
        if (newTouchPadMap.swipeDirection == oldTouchPadMap.swipeDirection) {
            if (newTouchPadMap.swipeDirection != SwipeDirection.Ignore) {

                newTouchPadMap.swipeDirection = SwipeDirection.Ignore;
            }

        }
        oldTouchPadMap.swipeDirection = newTouchPadMap.swipeDirection;

    }

    public static void input(MotionEvent event) {

        // https://github.com/Samsung/GearVRf/issues/231
        newTouchPadMap.axisX = event.getAxisValue(MotionEvent.AXIS_X);
        newTouchPadMap.axisY = event.getAxisValue(MotionEvent.AXIS_Y);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            newTouchPadMap.buttonState.pressed = true;

        }
        if (event.getAction() == KeyEvent.ACTION_UP) {
            newTouchPadMap.buttonState.pressed = false;

        }

    }

    public static void onSwipe(SwipeDirection swipeDirection) {
        newTouchPadMap.swipeDirection = swipeDirection;

    }

    public static void onSingleTap() {
        newTouchPadMap.buttonState.singleTaped = true;

    }

    public static void onLongPress() {
        newTouchPadMap.buttonState.longPressed = true;

    }

}
