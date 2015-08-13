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
package org.gearvrf.keyboard.mic;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.mic.model.MicItem;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class MicGroupHover extends GVRSceneObject {

    private boolean isVisibleByOpacity = false;
    private GVRAnimation mOpacityAnimation;
    private GVRAnimation mScaleAnimation;
    int mOff = 0;
    int mOn = 1;

    MicItem mHover;

    public MicGroupHover(GVRContext gvrContext) {
        super(gvrContext);
        setName(SceneObjectNames.MIC_GROUP_HOVER);
        mHover = new MicItem(gvrContext, R.drawable.mic_hover);
        mHover.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_HOVER);
        mHover.getRenderData().getMaterial().setOpacity(0);
        mHover.getTransform().setScale(0.94f, 0.94f, 0.94f);
        this.addChildObject(mHover);
    }

    public void animateOpacityOff() {

        if (isVisibleByOpacity) {
            isVisibleByOpacity = false;
            stop(mOpacityAnimation);
            mOpacityAnimation = new GVROpacityAnimation(mHover, 1, mOff);
            mOpacityAnimation.start(this.getGVRContext().getAnimationEngine());

        }
    }

    public void animateOpacityOn() {

        if (!isVisibleByOpacity) {
            isVisibleByOpacity = true;
            stop(mOpacityAnimation);
            mOpacityAnimation = new GVROpacityAnimation(mHover, 1, mOn);
            mOpacityAnimation.start(this.getGVRContext().getAnimationEngine());

        }
    }

    // public void animateOpacity(int i) {
    //
    // if ((!isVisibleByOpacity && i == 1) || (isVisibleByOpacity && i == 0)) {
    // isVisibleByOpacity = i == 1 ? true : false;
    // stop(mOpacityAnimation);
    // mOpacityAnimation = new GVROpacityAnimation(mHover, 1, i);
    // mOpacityAnimation.start(this.getGVRContext().getAnimationEngine());
    //
    // }
    // }

    private void stop(GVRAnimation opacityAnimation) {

        if (opacityAnimation != null) {
            this.getGVRContext().getAnimationEngine().stop(opacityAnimation);
        }
    }

    private void animateScale(float scale) {
        mScaleAnimation = new GVRScaleAnimation(mHover, 1, scale);
        mScaleAnimation.start(this.getGVRContext().getAnimationEngine());
    }

    public void show() {
        animateOpacityOn();
        scale();
    }

    public void hide() {
        animateOpacityOff();
        scale();
    }

    private void scale() {
        float scale = !isVisibleByOpacity ? 0.94f : 1f;

        animateScale(scale);
    }
}
