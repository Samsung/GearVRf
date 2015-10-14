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
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.controls.util.RenderingOrder;

public class IndicatorLongPress extends GVRSceneObject {

    private static final float TIME_ANIMATION = 0.1f;
    static final float SCALE_OBJECT = 0.3f;
    private GVROpacityAnimation opacityAnimationOn;
    private GVRScaleAnimation scaleAnimationBiger;
    private GVROpacityAnimation opacityAnimationOff;
    private GVRScaleAnimation scaleAnimationSmaller;

    public IndicatorLongPress(GVRContext gvrContext, float width, float height, GVRTexture texture) {
        super(gvrContext, width, height, texture);

        this.getTransform().setPositionZ(0.1f);
        this.getTransform().setScale(SCALE_OBJECT, SCALE_OBJECT, SCALE_OBJECT);
        this.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_GAMEPAD_BUTTONS_EVENT);
        this.getRenderData().getMaterial().setOpacity(0);
    }

    public void pressed() {

        opacityAnimationOn = new GVROpacityAnimation(this, TIME_ANIMATION, 1);

        scaleAnimationBiger = new GVRScaleAnimation(this, TIME_ANIMATION, SCALE_OBJECT + 0.1f);

        opacityAnimationOn.start(getGVRContext().getAnimationEngine());

        scaleAnimationBiger.start(getGVRContext().getAnimationEngine());

    }

    public void pressedRelese() {
        
        opacityAnimationOff = new GVROpacityAnimation(this, TIME_ANIMATION, 0);

        scaleAnimationSmaller = new GVRScaleAnimation(this, TIME_ANIMATION, SCALE_OBJECT - 0.1f);

        opacityAnimationOff.start(getGVRContext().getAnimationEngine());

        scaleAnimationSmaller.start(getGVRContext().getAnimationEngine());

    }

}
