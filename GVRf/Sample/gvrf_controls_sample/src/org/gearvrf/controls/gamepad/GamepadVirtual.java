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

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.controls.R;
import org.gearvrf.controls.util.RenderingOrder;

import java.util.HashMap;

public class GamepadVirtual extends GVRSceneObject {

    private HashMap<GamepadButtons, GamepadButton> gamepadButtons = new HashMap<GamepadButtons, GamepadButton>();
    private Resources res = null;
    private GVRTexture gamepadTexture = null;
    private float angle;
    private boolean isRotated = false;

    public GamepadVirtual(GVRContext gvrContext) {
        super(gvrContext);

        res = gvrContext.getContext().getResources();

        GVRMesh gamepadMesh = gvrContext.loadMesh(new GVRAndroidResource(
                gvrContext, R.drawable.gamepad_dev));

        gamepadTexture = gvrContext.loadTexture(new GVRAndroidResource(
                gvrContext, R.drawable.gamepad_diffuse));

        GVRSceneObject sceneObject = new GVRSceneObject(getGVRContext(), gamepadMesh, gamepadTexture);
        addChildObject(sceneObject);

        sceneObject.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_GAMEPAD);

        attachButtons();
    }

    private void attachButtons() {

        TypedArray gamepad = res.obtainTypedArray(R.array.gamepad);

        int n = gamepad.length();

        for (int i = 0; i < n; i++) {

            int id = gamepad.getResourceId(i, -1);

            TypedArray array = res.obtainTypedArray(id);

            GamepadButton gamepadButton = new GamepadButton(getGVRContext(), array);
            addChildObject(gamepadButton);

            GamepadButtons dd = GamepadButtons.get(i);

            gamepadButtons.put(dd, gamepadButton);
        }
    }

    public void handlerAnalogL(float x, float y, float z) {

        final GamepadButton button = gamepadButtons.get(GamepadButtons.analog_stick_l);
        button.moveToPosition(x, y, z);
    }

    public void handlerAnalogR(float x, float y, float z) {

        final GamepadButton button = gamepadButtons.get(GamepadButtons.analog_stick_r);
        button.moveToPosition(x, y, z);
    }

    public void dpadTouch(float x, float y) {

        final GamepadButton button = gamepadButtons.get(GamepadButtons.dpad);

        if (x == 0 && y == 0) {
            return;
        }

        float angle = getAngle(x, y);

        if (this.angle == angle) {
            button.showEvent();
        } else {
            button.showButtonPressed(angle);
            button.showEvent();
        }

        this.angle = angle;
    }

    private float getAngle(float x, float y) {

        float angle = 0;

        if (x == 1) {
            angle = -90;
        } else if (x == -1) {
            angle = 90;
        } else if (y == 1) {
            angle = 180;
        } else if (y == -1) {
            angle = 0;
        }

        return angle;
    }

    public void handlerLRButtons(boolean left, boolean right) {

        final GamepadButton buttonL = gamepadButtons.get(GamepadButtons.lt);
        final GamepadButton buttonR = gamepadButtons.get(GamepadButtons.rt);

        buttonL.actionPressedLR(left);
        buttonR.actionPressedLR(right);

        handlerRotationGamepad(left, right);
    }

    private void handlerRotationGamepad(boolean left, boolean right) {
        float xRot = this.getParent().getTransform().getRotationPitch();

        if (!left && !right) {

            if (isRotated) {

                rotateBackToNormal(xRot);

                isRotated = false;
            }

        } else {

            if (!isRotated) {

                rotateLayDown(xRot);

                isRotated = true;
            }
        }
    }

    private void rotateLayDown(float xRot) {
        float angle;
        angle = 45 - xRot;

        rotateGamepadOnY(angle);

    }

    private void rotateBackToNormal(float xRot) {
        float angle;
        angle = -xRot;

        rotateGamepadOnY(angle);

    }

    private void rotateGamepadOnY(float angle) {
        GVRRotationByAxisAnimation parentRotation = new GVRRotationByAxisAnimation(this.getParent(), 0.4f, angle, 1, 0, 0);
        parentRotation.setRepeatMode(GVRRepeatMode.ONCE);
        parentRotation.setRepeatCount(1);
        parentRotation.start(this.getGVRContext().getAnimationEngine());
    }

    public void buttonsPressed(boolean button3, boolean button4, boolean button1, boolean button2) {

        final GamepadButton gameButton1 = gamepadButtons.get(GamepadButtons.button1);
        final GamepadButton gameButton2 = gamepadButtons.get(GamepadButtons.button2);
        final GamepadButton gameButton3 = gamepadButtons.get(GamepadButtons.button3);
        final GamepadButton gameButton4 = gamepadButtons.get(GamepadButtons.button4);

        gameButton1.handlerButtonStates(button1);
        gameButton2.handlerButtonStates(button2);
        gameButton3.handlerButtonStates(button3);
        gameButton4.handlerButtonStates(button4);
    }
}