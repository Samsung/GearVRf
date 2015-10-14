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

package org.gearvrf.controls.menu.motion;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRInterpolator;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.controls.anim.AnimationsTime;
import org.gearvrf.controls.model.Apple;
import org.gearvrf.controls.shaders.ColorSwapShader;

public class MenuPreview extends GVRSceneObject {

    private final float APPLE_SCALE = 0.5f;
    private final float APPLE_INITIAL_POSITION_Y = 0.2f;
    private final float Y_ANIMATION_DELTA = -0.7f;

    private Apple apple;
    private GVRAnimation appleAnimation;
    private GVRInterpolator animationInterpolator = null;

    public MenuPreview(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);

        apple = new Apple(gvrContext);
        apple.getRenderData().getMaterial().setOpacity(0f);
        apple.getTransform().setPositionY(APPLE_INITIAL_POSITION_Y);
        apple.getTransform().setScale(APPLE_SCALE, APPLE_SCALE, APPLE_SCALE);
        addChildObject(apple);
    }

    public void show() {
        startAppleAnimation();
    }

    public void startAppleAnimation() {
        apple.getTransform().setPositionY(APPLE_INITIAL_POSITION_Y);
        apple.getRenderData().getMaterial().setOpacity(1f);

        appleAnimation = new GVRRelativeMotionAnimation(apple, AnimationsTime.getDropTime(), 0,
                Y_ANIMATION_DELTA, 0)
                .setInterpolator(animationInterpolator)
                .setRepeatMode(GVRRepeatMode.REPEATED)
                .setRepeatCount(-1)
                .start(getGVRContext()
                        .getAnimationEngine());
    }

    public void changeColorTo(float[] color) {

        apple.getRenderData().getMaterial()
                .setVec4(ColorSwapShader.COLOR, color[0], color[1], color[2], 1);

    }

    public void changeInterpolatorTo(GVRInterpolator interpolator) {
        animationInterpolator = interpolator;
        getGVRContext().getAnimationEngine().stop(appleAnimation);
        startAppleAnimation();
    }

    public void hide() {
        apple.getTransform().setPositionY(APPLE_INITIAL_POSITION_Y);
        apple.getRenderData().getMaterial().setOpacity(0f);
        getGVRContext().getAnimationEngine().stop(appleAnimation);
    }
    
    public void animationsTime(){
        getGVRContext().getAnimationEngine().stop(appleAnimation);
        startAppleAnimation();
    }
}
