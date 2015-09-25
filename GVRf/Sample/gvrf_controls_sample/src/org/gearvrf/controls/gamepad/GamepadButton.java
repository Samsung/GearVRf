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

import android.content.res.TypedArray;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTextureParameters.TextureFilterType;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.controls.R;
import org.gearvrf.controls.util.RenderingOrder;

public class GamepadButton extends GVRSceneObject {

    private static final float DOWN_SIMPLE_BUTTON_TIME = 0.01f;
    private static final float DOWN_SIMPLE_BUTTON = 0.02f;
    private GVRTexture buttonTexture;
    private float pivotX;
    private float pivotY;
    private float pivotZ;
    private GVRSceneObject buttonHover;
    private GVRSceneObject buttonNormal;
    private GVROpacityAnimation animOpacity;
    private float evPositionX, evPositionY, evPositionZ, evRotationW;
    private GVRTexture eventTexture;
    private boolean isDown = false;

    public GamepadButton(GVRContext gvrContext, TypedArray array) {
        super(gvrContext);

        setName(array.getString(0));
        GVRTextureParameters parameters = new GVRTextureParameters(getGVRContext());
        parameters.setAnisotropicValue(16);
        parameters.setMinFilterType(TextureFilterType.GL_NEAREST_MIPMAP_NEAREST);
        parameters.setMagFilterType(TextureFilterType.GL_NEAREST_MIPMAP_NEAREST);
        buttonTexture = gvrContext.loadTexture(new GVRAndroidResource(
                gvrContext, R.drawable.gamepad_diffuse), parameters);

        eventTexture = gvrContext.loadTexture(new GVRAndroidResource(
                gvrContext, R.drawable.event_color));

        attachButton(array.getResourceId(1, -0));
        attachEvent(array.getResourceId(2, -0));

        pivotX = array.getFloat(3, -0);
        pivotY = array.getFloat(4, -0);
        pivotZ = array.getFloat(5, -0);

        array.recycle();
    }

    private void attachButton(int drawable) {

        GVRMesh buttonMesh = getGVRContext().loadMesh(new GVRAndroidResource(
                getGVRContext(), drawable));

        buttonNormal = new GVRSceneObject(getGVRContext(), buttonMesh,
                buttonTexture);
        buttonNormal.getRenderData().setRenderingOrder(
                RenderingOrder.ORDER_RENDERING_GAMEPAD_BUTTONS);

        addChildObject(buttonNormal);
    }

    private void attachEvent(int drawable) {

        if (drawable == -0) {
            return;
        }

        GVRMesh dpadEventMesh = getGVRContext().loadMesh(new GVRAndroidResource(
                getGVRContext(), drawable));

        buttonHover = new GVRSceneObject(getGVRContext(), dpadEventMesh, eventTexture);
        buttonHover.getRenderData().getMaterial().setOpacity(0);

        evPositionX = buttonHover.getTransform().getPositionX();
        evPositionY = buttonHover.getTransform().getPositionY();
        evPositionZ = buttonHover.getTransform().getPositionZ();
        evRotationW = buttonHover.getTransform().getRotationW();

        buttonHover.getRenderData().setRenderingOrder(
                RenderingOrder.ORDER_RENDERING_GAMEPAD_BUTTONS_EVENT);

        addChildObject(buttonHover);
    }

    public void moveToPosition(float x, float y, float z) {

        if (x != 0 || y != 0) {
            buttonHover.getRenderData().getMaterial().setOpacity(1);
        } else {
            buttonHover.getRenderData().getMaterial().setOpacity(0);
        }

        buttonHover.getTransform().setPosition(x * 0.14f, y * -0.14f, evPositionZ);
        buttonNormal.getTransform().setPosition(x * 0.02f, y * -0.02f, evPositionZ);
    }

    public void showButtonPressed(float angle) {

        buttonHover.getRenderData().getMaterial().setOpacity(0);

        buttonHover.getTransform().setPosition(evPositionX, evPositionY, evPositionZ);
        buttonHover.getTransform().setRotation(evRotationW, evPositionX, evPositionY, evPositionZ);

        GVRRotationByAxisWithPivotAnimation dpadRotation = new GVRRotationByAxisWithPivotAnimation(
                buttonHover, 0.001f, angle, 0, 0, 1, pivotX, pivotY, pivotZ);
        dpadRotation.setRepeatMode(GVRRepeatMode.ONCE);
        dpadRotation.setRepeatCount(1);
        dpadRotation.start(this.getGVRContext().getAnimationEngine());

        animOpacity = new GVROpacityAnimation(buttonHover, 2, 1);
        animOpacity.setRepeatMode(GVRRepeatMode.ONCE);
        animOpacity.setRepeatCount(1);
        animOpacity.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                buttonHover.getRenderData().getMaterial().setOpacity(0);
            }
        });

        animOpacity.start(getGVRContext().getAnimationEngine());
    }

    public void actionPressedLR(boolean pressed) {

        if (pressed) {

            buttonNormal.getRenderData().getMaterial().setOpacity(0f);
            buttonHover.getRenderData().getMaterial().setOpacity(1f);

        } else {

            buttonNormal.getRenderData().getMaterial().setOpacity(1f);
            buttonHover.getRenderData().getMaterial().setOpacity(0.f);
        }
    }

    public void handlerButtonStates(boolean pressed) {

        if (pressed) {

            buttonHover.getRenderData().getMaterial().setOpacity(0.5f);

            if (!isDown) {

                GVRRelativeMotionAnimation eventDown = new GVRRelativeMotionAnimation(this,
                        DOWN_SIMPLE_BUTTON_TIME,
                        this.getTransform().getPositionX(),
                        this.getTransform().getPositionY(),
                        this.getTransform().getPositionZ() - DOWN_SIMPLE_BUTTON);

                eventDown.setRepeatMode(GVRRepeatMode.ONCE);
                eventDown.setRepeatCount(1);
                eventDown.start(this.getGVRContext().getAnimationEngine());

                isDown = true;
            }

        } else {

            buttonHover.getRenderData().getMaterial().setOpacity(0.f);

            if (isDown) {

                GVRRelativeMotionAnimation evDown = new GVRRelativeMotionAnimation(this,
                        DOWN_SIMPLE_BUTTON_TIME,
                        this.getTransform().getPositionX(),
                        this.getTransform().getPositionY(),
                        evPositionZ - this.getTransform().getPositionZ());

                evDown.setRepeatMode(GVRRepeatMode.ONCE);
                evDown.setRepeatCount(1);
                evDown.start(this.getGVRContext().getAnimationEngine());

                isDown = false;
            }
        }
    }

    public void showEvent() {

        if (animOpacity != null) {
            this.getGVRContext().getAnimationEngine().stop(animOpacity);
        }

        animOpacity = new GVROpacityAnimation(buttonHover, 0.6f, 1);
        animOpacity.setRepeatMode(GVRRepeatMode.ONCE);
        animOpacity.setRepeatCount(1);
        animOpacity.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                buttonHover.getRenderData().getMaterial().setOpacity(0);
            }
        });

        animOpacity.start(getGVRContext().getAnimationEngine());
    }
}
