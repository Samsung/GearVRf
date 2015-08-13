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
package org.gearvrf.keyboard.mic.model;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.interpolator.InterpolatorExpoEaseInOut;
import org.gearvrf.keyboard.model.AudioClip;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class ExceptionFeedback extends GVRSceneObject {

    private GVRSceneObject blurObject;
    private GVRSceneObject iconObject;
    private GVRSceneObject ringObject;
    private GVRAnimation blurScale, ringScale, blurOpacity, ringOpacity, iconOpacity;

    private static final float ANIMATION_TIME = 0.3f;

    public ExceptionFeedback(GVRContext context) {
        super(context);
        setName(SceneObjectNames.EXCEPTION_FEEDBACK);

        GVRAndroidResource mResourceGlow = new GVRAndroidResource(context,
                R.drawable.exception_glow);
        GVRAndroidResource mResourceIcon = new GVRAndroidResource(context,
                R.drawable.exception_icon);
        GVRAndroidResource mResourceRing = new GVRAndroidResource(context,
                R.drawable.exception_ring);

        blurObject = new GVRSceneObject(context, 2.8f, 2.8f, context.loadTexture(mResourceGlow));
        iconObject = new GVRSceneObject(context, 0.1f, 0.1f, context.loadTexture(mResourceIcon));
        ringObject = new GVRSceneObject(context, 1.4f, 1.4f, context.loadTexture(mResourceRing));

        this.addChildObject(blurObject);
        this.addChildObject(iconObject);
        this.addChildObject(ringObject);

        iconObject.getTransform().setPositionZ(-1.1f);
        ringObject.getTransform().setPositionZ(-1.2f);
        blurObject.getTransform().setPositionZ(-1.3f);

        blurObject.getTransform().setScale(1.2f, 1.2f, 1.2f);
        ringObject.getTransform().setScale(1.2f, 1.2f, 1.2f);

        blurObject.getRenderData().getMaterial().setOpacity(0);
        iconObject.getRenderData().getMaterial().setOpacity(0);
        ringObject.getRenderData().getMaterial().setOpacity(0);

        blurObject.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_EXCEPTION_BLUR);
        ringObject.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_EXCEPTION_RING);
        iconObject.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_EXCEPTION_ICON);

    }

    public void show() {

        AnimateScale(1);
        AnimateOpacity(1);
        getGVRContext().getPeriodicEngine().runAfter(new Runnable() {

            @Override
            public void run() {

                hide();

            }

        }, 2.0f);

        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getExceptionSoundID(), 1.0f, 1.0f);

    }

    private void hide() {

        AnimateScale(1.3f);
        AnimateOpacity(0);
    }

    private void AnimateScale(float scale) {

        blurScale = new GVRScaleAnimation(blurObject, ANIMATION_TIME, scale);
        blurScale.start(this.getGVRContext().getAnimationEngine());
        blurScale.setInterpolator(new InterpolatorExpoEaseInOut());
        ringScale = new GVRScaleAnimation(ringObject, ANIMATION_TIME, scale);
        ringScale.start(this.getGVRContext().getAnimationEngine());
        ringScale.setInterpolator(new InterpolatorExpoEaseInOut());
    }

    private void AnimateOpacity(float opacity) {

        blurOpacity = new GVROpacityAnimation(blurObject, ANIMATION_TIME, opacity);
        blurOpacity.start(this.getGVRContext().getAnimationEngine());
        blurOpacity.setInterpolator(new InterpolatorExpoEaseInOut());

        ringOpacity = new GVROpacityAnimation(ringObject, ANIMATION_TIME, opacity);
        ringOpacity.start(this.getGVRContext().getAnimationEngine());
        ringOpacity.setInterpolator(new InterpolatorExpoEaseInOut());

        iconOpacity = new GVROpacityAnimation(iconObject, ANIMATION_TIME, opacity);
        iconOpacity.start(this.getGVRContext().getAnimationEngine());
        iconOpacity.setInterpolator(new InterpolatorExpoEaseInOut());

    }

}
