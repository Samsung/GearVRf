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
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.interpolator.InterpolatorExpoEaseIn;
import org.gearvrf.keyboard.interpolator.InterpolatorExpoEaseOut;
import org.gearvrf.keyboard.mic.model.MicItem;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class MicGroupProgress extends GVRSceneObject {
    MicItem mProgress;
    boolean isVisibleByOpacity;
    GVRAnimation mOpacityAnimation;
    GVRAnimation mRotationIn;
    GVRAnimation mRotationWork;
    GVRAnimation mRotationOut;
    public static float TIME_ANIMATION_ROTATION_IN = 1f;
    public static float TIME_ANIMATION_ROTATION_WORK = 0.5f;
    public static float TIME_ANIMATION_ROTATION_OUT = 3f;
    public static float ROTATION = -360;

    public MicGroupProgress(GVRContext gvrContext) {
        super(gvrContext);
        setName(SceneObjectNames.MIC_GROUP_PROGRESS);
        mProgress = new MicItem(gvrContext, R.drawable.mic_loading);
        mProgress.getRenderData().getMaterial().setOpacity(0);
        mProgress.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_PROGRESS);
        this.addChildObject(mProgress);
    }

    public void show() {
        workIn();

    }

    private void workIn() {
        stop(mRotationOut);
        stop(mRotationWork);
        animateOpacity(1, TIME_ANIMATION_ROTATION_IN);
        animateRotationIn();
    }

    public void hide() {
        workOut();

    }

    private void workOut() {
        stop(mRotationIn);
        stop(mRotationWork);
        animateOpacity(0, TIME_ANIMATION_ROTATION_OUT);
        animateRotationOut();
    }

    private void work() {

        stop(mRotationIn);

        animateRotationWork();
    }

    private void animateRotationIn() {

        mRotationIn = new GVRRotationByAxisAnimation(mProgress, TIME_ANIMATION_ROTATION_IN,
                ROTATION,
                0, 0, 1);
        mRotationIn.setInterpolator(new InterpolatorExpoEaseIn());
        mRotationIn.setOnFinish(new GVROnFinish() {

            @Override
            public void finished(GVRAnimation arg0) {
                work();

            }
        });
        mRotationIn.start(this.getGVRContext().getAnimationEngine());

    }

    private void animateRotationOut() {

        mRotationOut = new GVRRotationByAxisAnimation(mProgress, TIME_ANIMATION_ROTATION_OUT,
                ROTATION * 3,
                0, 0, 1);
        mRotationOut.setInterpolator(new InterpolatorExpoEaseOut());
        mRotationOut.start(this.getGVRContext().getAnimationEngine());

    }

    private void animateOpacity(int alpha, float time) {

        stop(mOpacityAnimation);
        mOpacityAnimation = new GVROpacityAnimation(mProgress, time, alpha);
        mOpacityAnimation.start(this.getGVRContext().getAnimationEngine());

    }

    private void animateRotationWork() {

        mRotationWork = new GVRRotationByAxisAnimation(mProgress, TIME_ANIMATION_ROTATION_WORK,
                ROTATION, 0, 0, 1);
        mRotationWork.setRepeatMode(GVRRepeatMode.REPEATED);
        mRotationWork.setRepeatCount(-1);
        mRotationWork.start(this.getGVRContext().getAnimationEngine());

    }

    private void stop(GVRAnimation animation) {
        if (animation != null) {
            this.getGVRContext().getAnimationEngine().stop(animation);

        }

    }

}
