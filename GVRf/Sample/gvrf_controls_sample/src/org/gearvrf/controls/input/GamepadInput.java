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

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;

public final class GamepadInput {

    private static GamepadMap newGamepadMap = new GamepadMap();

    private static GamepadMap oldGamepadMap = new GamepadMap();

    public static void process() {

        newGamepadMap.resetIntermadiateState();

        for (Integer key : oldGamepadMap.buttonsKeyCode) {
            Button oldButton = oldGamepadMap.buttons.get(key);
            Button newButton = newGamepadMap.buttons.get(key);

            updateIntermiteStates(newButton, oldButton);
        }
    }

    private static void updateIntermiteStates(Button newButton, Button oldButton) {

        if (oldButton.pressed != newButton.pressed) {
            newButton.down = newButton.pressed;
            newButton.up = !newButton.pressed;
        }

        oldButton.replicateValues(newButton);
    }

    public static void input(MotionEvent event) {

        newGamepadMap.axisX = event.getAxisValue(MotionEvent.AXIS_X);
        newGamepadMap.axisY = event.getAxisValue(MotionEvent.AXIS_Y);
        newGamepadMap.axisHatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        newGamepadMap.axisHatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        newGamepadMap.axisRX = event.getAxisValue(MotionEvent.AXIS_RX);
        newGamepadMap.axisRY = event.getAxisValue(MotionEvent.AXIS_RY);

    }

    public static void input(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            newGamepadMap.buttons.get(event.getKeyCode()).pressed = true;

        }
        if (event.getAction() == KeyEvent.ACTION_UP) {
            newGamepadMap.buttons.get(event.getKeyCode()).pressed = false;

        }

    }

    public static boolean getKeyDown(int key) {
        return newGamepadMap.buttons.get(key).down;
    }

    public static boolean getKeyUp(int key) {
        return newGamepadMap.buttons.get(key).up;
    }

    public static boolean getKey(int key) {
        return newGamepadMap.buttons.get(key).pressed;
    }

    public static Button getButton(int key) {
        return newGamepadMap.buttons.get(key);
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

    public static boolean processJoystickInput(MotionEvent event, int historyPos) {

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

        newGamepadMap.centeredAxisX = x;
        newGamepadMap.centeredAxisY = y;

        newGamepadMap.centeredAxisHatX = hatx;
        newGamepadMap.centeredAxisHatY = haty;

        newGamepadMap.centeredAxisRX = rx;
        newGamepadMap.centeredAxisRY = ry;

        return true;
    }

    public static float getAxis(int axis) {
        switch (axis) {
            case MotionEvent.AXIS_X:
                return newGamepadMap.axisX;
            case MotionEvent.AXIS_Y:
                return newGamepadMap.axisY;
            case MotionEvent.AXIS_HAT_X:
                return newGamepadMap.axisHatX;
            case MotionEvent.AXIS_HAT_Y:
                return newGamepadMap.axisHatY;
            case MotionEvent.AXIS_RX:
                return newGamepadMap.axisRX;
            case MotionEvent.AXIS_RY:
                return newGamepadMap.axisRY;
            default:
                return 0f;
        }
    }

    public static float getCenteredAxis(int axis) {
        switch (axis) {
            case MotionEvent.AXIS_X:
                return newGamepadMap.centeredAxisX;
            case MotionEvent.AXIS_Y:
                return newGamepadMap.centeredAxisY;
            case MotionEvent.AXIS_HAT_X:
                return newGamepadMap.centeredAxisHatX;
            case MotionEvent.AXIS_HAT_Y:
                return newGamepadMap.centeredAxisHatY;
            case MotionEvent.AXIS_RX:
                return newGamepadMap.centeredAxisRX;
            case MotionEvent.AXIS_RY:
                return newGamepadMap.centeredAxisRY;
            default:
                return 0f;
        }
    }

    public static ArrayList<Integer> getKeyCodeMap() {
        return newGamepadMap.buttonsKeyCode;
    }

}
