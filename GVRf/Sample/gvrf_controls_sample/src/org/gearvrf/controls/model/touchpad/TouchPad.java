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

package org.gearvrf.controls.model.touchpad;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.controls.R;
import org.gearvrf.controls.input.TouchPadInput;
import org.gearvrf.controls.util.RenderingOrder;

public class TouchPad extends GVRSceneObject {

    GVRSceneObject touchPad;
    IndicatorTap indicator;
    IndicatorLongPress indicatorLongPress;
    Arrow aroowUp;
    Arrow aroowDown;
    Arrow aroowLeft;
    Arrow aroowRight;

    static final float CORRETION_FACTOR_SCALE_INPUT_X = 0.005f;
    static final float CORRETION_FACTOR_SCALE_INPUT_Y = 0.005f;
    static final float CORRETION_FACTOR_CENTER_INPUT_X = 1300f;
    static final float CORRETION_FACTOR_CENTER_INPUT_Y = 700;
    static final float SCALE_OBJECT = 2f;

    public TouchPad(GVRContext gvrContext) {
        super(gvrContext);

        createTouchpad();

        createIndicator();

        createIndicatorLongPress();

        createArrows();

        this.addChildObject(aroowUp);
        this.addChildObject(aroowDown);
        this.addChildObject(aroowLeft);
        this.addChildObject(aroowRight);
        this.addChildObject(touchPad);
        this.addChildObject(indicator);
        this.addChildObject(indicatorLongPress);

    }

    private void createArrows() {

        aroowUp = new Arrow(getGVRContext(), 1f, 1f,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.swipe)), Arrow.UP);
        aroowDown = new Arrow(getGVRContext(), 1f, 1f,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.swipe)), Arrow.DOWN);
        aroowLeft = new Arrow(getGVRContext(), 1f, 1f,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.swipe)), Arrow.LEFT);
        aroowRight = new Arrow(getGVRContext(), 1f, 1f,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.swipe)), Arrow.RIGHT);

    }

    private void createIndicator() {
        indicator = new IndicatorTap(getGVRContext(), 1f, 1f,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.tap)));

    }

    private void createIndicatorLongPress() {
        indicatorLongPress = new IndicatorLongPress(getGVRContext(), 1f, 1f,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.longpress)));

    }

    private void createTouchpad() {
        touchPad = new GVRSceneObject(getGVRContext(),
                getGVRContext().loadMesh(new GVRAndroidResource(getGVRContext(), (R.drawable.gear_vr))),
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.gear_vr_texture)));
        touchPad.getTransform().setPositionZ(-4f);
        touchPad.getTransform().setPositionY(0.1f);
        touchPad.getTransform().setPositionX(-0.895f);
        touchPad.getTransform().rotateByAxis(-90, 0, 1, 0);
        touchPad.getTransform().setScale(SCALE_OBJECT, SCALE_OBJECT, SCALE_OBJECT);
        touchPad.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_GAMEPAD);
        
    }

    public void updateIndicator() {

        updateIndicatorTap();

        updateArrows();

        updateLongPress();

    }

    private void updateIndicatorTap() {
        if (TouchPadInput.getCurrent().buttonState.isSingleTap()) {

            indicator.pressed();

        }
        
    }



    private void updateLongPress() {

        if (TouchPadInput.getCurrent().buttonState.isLongPressed()) {

            indicatorLongPress.pressed();

        }
        
        if (TouchPadInput.getCurrent().buttonState.isUp()) {

            indicatorLongPress.pressedRelese();

        }

    }

    private void updateArrows() {

        switch (TouchPadInput.getCurrent().swipeDirection) {
            case Up:
                aroowUp.animateArrowOn();
                break;
            case Backward:
                aroowLeft.animateArrowOn();
                break;
            case Down:
                aroowDown.animateArrowOn();
                break;
            case Forward:
                aroowRight.animateArrowOn();
                break;
            case Ignore:

                break;

            default:
                break;
        }

    }

}
