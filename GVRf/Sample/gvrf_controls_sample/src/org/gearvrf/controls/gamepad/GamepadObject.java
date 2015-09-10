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

package org.gearvrf.controls.gamepad;

import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.controls.input.GamepadInput;


public class GamepadObject extends GVRSceneObject {

    private GamepadVirtual gamepadVirtual;

    public GamepadObject(GVRContext gvrContext) {
        super(gvrContext);

        gamepadVirtual = new GamepadVirtual(gvrContext);
        
        GVRSceneObject mGVRSceneObject = new GVRSceneObject(gvrContext);
        mGVRSceneObject.addChildObject(gamepadVirtual);
        addChildObject(mGVRSceneObject);
    }

    public GamepadVirtual getGamepadVirtual() {
        return gamepadVirtual;
    }
    
    public void inputControl(){
        
        gamepadVirtual.handlerAnalogL(
            GamepadInput.getCenteredAxis(MotionEvent.AXIS_X),
            GamepadInput.getCenteredAxis(MotionEvent.AXIS_Y),
            0);

        gamepadVirtual.handlerAnalogR(
            GamepadInput.getCenteredAxis(MotionEvent.AXIS_RX),
            GamepadInput.getCenteredAxis(MotionEvent.AXIS_RY),
            0);

        gamepadVirtual.dpadTouch(
            GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_X),
            GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_Y));

        gamepadVirtual.handlerLRButtons(
            GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_L1),
            GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_R1));

        gamepadVirtual.buttonsPressed(
            GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_X),
            GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_Y),
            GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_A),
            GamepadInput.getKey(KeyEvent.KEYCODE_BUTTON_B));
    }
}