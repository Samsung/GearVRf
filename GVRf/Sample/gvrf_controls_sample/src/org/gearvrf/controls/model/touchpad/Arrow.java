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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.controls.util.RenderingOrder;

public class Arrow extends GVRSceneObject {

    public static final float UP = 270;
    public static final float DOWN = 90;
    public static final float LEFT = 0;
    public static final float RIGHT = 180;

    private static final float TIME_TO_OFF = 0.6f;
    static final float SCALE_ARROW = 0.4f;
    private GVROpacityAnimation opacityAnimationOn;
    private GVROpacityAnimation opacityAnimationOff;
    private boolean finishAnimationOn = true;
    private boolean lockOff;
    private long lastSwip;
    GVRScaleAnimation scaleAnimation;

    public Arrow(GVRContext gvrContext, float width, float height, GVRTexture texture, float angle) {
        super(gvrContext, width, height, texture);
        createAroow(angle);

    }

    private void createAroow(float angle) {

        this.getTransform().setScale(SCALE_ARROW, SCALE_ARROW, SCALE_ARROW);
        this.getTransform().setPosition(-0.35f, 0f, 0.2f);
        this.getTransform().rotateByAxisWithPivot(angle, 0, 0, 1, 0, 0, 0);
        this.getRenderData().getMaterial().setOpacity(0);
        this.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_TOUCHPAD_AROOWS);

    }

    public void animateArrowOn() {

        if (finishAnimationOn && !lockOff) {

            if (opacityAnimationOn != null) {

                getGVRContext().getAnimationEngine().stop(opacityAnimationOn);

            } else {
                opacityAnimationOn = new GVROpacityAnimation(this, 1f, 1);

            }

            executeAnimationOn();
        } else {
            lockOff = true;

            lastSwip = System.currentTimeMillis();

            getGVRContext().getPeriodicEngine().runAfter(getRunnableOff(), TIME_TO_OFF +0.1f);

        }

    }

    private Runnable getRunnableOff() {
        return new Runnable() {

            @Override
            public void run() {

                tryOff();

            }
        };

    }

    private void tryOff() {
        if (System.currentTimeMillis() - lastSwip > (TIME_TO_OFF * 1000)) {

            animateArrowOff();

            lockOff = false;
        }

    }

    private void animateArrowOff() {

        if (opacityAnimationOff != null) {

            getGVRContext().getAnimationEngine().stop(opacityAnimationOff);

        } else {
            opacityAnimationOff = new GVROpacityAnimation(this, TIME_TO_OFF, 0);

        }

        opacityAnimationOff.start(getGVRContext().getAnimationEngine());

    }

    private void executeAnimationOn() {

        finishAnimationOn = false;

        opacityAnimationOn.setOnFinish(getGVROnFinish());

        opacityAnimationOn.start(getGVRContext().getAnimationEngine());

    }

    private GVROnFinish getGVROnFinish() {

        return new GVROnFinish() {

            @Override
            public void finished(GVRAnimation arg0) {
                finishAnimationOn = true;
                if (!lockOff)
                    animateArrowOff();
            }
        };

    }

}
