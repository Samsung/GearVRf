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

package org.gearvrf.controls.focus;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;
import org.gearvrf.controls.cursor.ControlGazeController;
import org.gearvrf.controls.input.GamepadInput;
import org.gearvrf.controls.input.GamepadMap;
import org.gearvrf.controls.input.TouchPadInput;
import org.gearvrf.controls.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

import java.util.ArrayList;

public class ControlSceneObjectBehavior {

    public static ArrayList<ControlSceneObject> interactiveObjects = new ArrayList<ControlSceneObject>();

    public static void process(GVRContext context) {

        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(context.getMainScene());
        ArrayList<ControlSceneObject> needToDisableFocus = new ArrayList<ControlSceneObject>();
        for (ControlSceneObject obj : interactiveObjects) {
            obj.onStep();
            needToDisableFocus.add(obj);
        }

        if (eyePointeeHolders.length == 0) {
            ControlGazeController.disableInteractiveCursor();
        } else {
            for (GVREyePointeeHolder eph : eyePointeeHolders) {
                for (ControlSceneObject object : interactiveObjects) {
                    if (eph.getOwnerObject().equals(object)) {
                        object.setFocus(true);
                        needToDisableFocus.remove(object);
                    }
                }
            }
        }

        for (ControlSceneObject obj : needToDisableFocus) {
            obj.setFocus(false);
        }

        processTap(context);
    }

    private static void processTap(GVRContext context) {

        for (ControlSceneObject object : interactiveObjects) {
            if (object.hasFocus()) {

                checkInput(object);
            }
        }
    }

    private static void checkInput(ControlSceneObject object) {
        if (TouchPadInput.getCurrent().buttonState.isSingleTap()) {
            object.singleTap();

        }
        handleTouchPad(object);
        handleGamePad(object);
    }

    private static void handleGamePad(ControlSceneObject object) {
        if (object.gamepadActionButtonslistener != null) {
            hadleGamepadPressed(object);
            hadleGamepadUp(object);
            hadleGamepadDown(object);
        }
    }

    private static void hadleGamepadDown(ControlSceneObject object) {
        if (GamepadInput.getKeyDown(GamepadMap.KEYCODE_BUTTON_A)) {
            object.gamepadActionButtonslistener.down(GamepadMap.KEYCODE_BUTTON_A);
        }
        if (GamepadInput.getKeyDown(GamepadMap.KEYCODE_BUTTON_B)) {
            object.gamepadActionButtonslistener.down(GamepadMap.KEYCODE_BUTTON_B);
        }
        if (GamepadInput.getKeyDown(GamepadMap.KEYCODE_BUTTON_X)) {
            object.gamepadActionButtonslistener.down(GamepadMap.KEYCODE_BUTTON_X);
        }
        if (GamepadInput.getKeyDown(GamepadMap.KEYCODE_BUTTON_Y)) {
            object.gamepadActionButtonslistener.down(GamepadMap.KEYCODE_BUTTON_Y);
        }

    }

    private static void hadleGamepadUp(ControlSceneObject object) {
        if (GamepadInput.getKeyUp(GamepadMap.KEYCODE_BUTTON_A)) {
            object.gamepadActionButtonslistener.up(GamepadMap.KEYCODE_BUTTON_A);
        }
        if (GamepadInput.getKeyUp(GamepadMap.KEYCODE_BUTTON_B)) {
            object.gamepadActionButtonslistener.up(GamepadMap.KEYCODE_BUTTON_B);
        }
        if (GamepadInput.getKeyUp(GamepadMap.KEYCODE_BUTTON_X)) {
            object.gamepadActionButtonslistener.up(GamepadMap.KEYCODE_BUTTON_X);
        }
        if (GamepadInput.getKeyUp(GamepadMap.KEYCODE_BUTTON_Y)) {
            object.gamepadActionButtonslistener.up(GamepadMap.KEYCODE_BUTTON_Y);
        }

    }

    private static void hadleGamepadPressed(ControlSceneObject object) {

        if (GamepadInput.getKey(GamepadMap.KEYCODE_BUTTON_A)) {
            object.gamepadActionButtonslistener.pressed(GamepadMap.KEYCODE_BUTTON_A);
        }
        if (GamepadInput.getKey(GamepadMap.KEYCODE_BUTTON_B)) {
            object.gamepadActionButtonslistener.pressed(GamepadMap.KEYCODE_BUTTON_B);
        }
        if (GamepadInput.getKey(GamepadMap.KEYCODE_BUTTON_X)) {
            object.gamepadActionButtonslistener.pressed(GamepadMap.KEYCODE_BUTTON_X);
        }
        if (GamepadInput.getKey(GamepadMap.KEYCODE_BUTTON_Y)) {
            object.gamepadActionButtonslistener.pressed(GamepadMap.KEYCODE_BUTTON_Y);
        }

    }

    private static void handleTouchPad(ControlSceneObject object) {
        if (object.touchAndGesturelistener != null) {

            if (TouchPadInput.getCurrent().buttonState.isLongPressed()) {
                object.touchAndGesturelistener.longPressed();
            }
            if (TouchPadInput.getCurrent().buttonState.isDown()) {
                object.touchAndGesturelistener.down();
            }
            if (TouchPadInput.getCurrent().buttonState.isUp()) {
                object.touchAndGesturelistener.up();
            }
            if (TouchPadInput.getCurrent().buttonState.isPressed()) {
                object.touchAndGesturelistener.pressed();
            }
            if (TouchPadInput.getCurrent().swipeDirection != SwipeDirection.Ignore) {
                object.touchAndGesturelistener.swipe(TouchPadInput.getCurrent().swipeDirection);
            }
        }

    }
}
