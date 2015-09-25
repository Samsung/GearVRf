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

package org.gearvrf.controls.model;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.controls.R;
import org.gearvrf.controls.util.MathUtils;

public class Star extends GVRSceneObject {

    private static final float ANIMATION_DURATION = 5;
    private static final float OPACITY_ANIMATION_DURATION = 4;
    private static final float Y_ANIMATION_DELTA = 10;
    private static final float STAR_SCALE = 0.75f;

    public Star(GVRContext gvrContext) {
        super(gvrContext, gvrContext.loadMesh(new GVRAndroidResource(gvrContext,
                R.raw.star)),
                gvrContext
                        .loadTexture(new GVRAndroidResource(gvrContext, R.drawable.star_diffuse)));
        this.getRenderData().getMaterial().setOpacity(0);
        this.getTransform().setScale(STAR_SCALE, STAR_SCALE, STAR_SCALE);

    }

    public void playMoveAnimation(GVRContext gvrContext, GVRSceneObject returnTarget) {
        getTransform().setPosition(returnTarget.getTransform().getPositionX(),
                returnTarget.getTransform().getPositionY(),
                returnTarget.getTransform().getPositionZ());
        getTransform().setRotationByAxis(
                0, 1, 1, 1);
        getTransform().rotateByAxis(
                MathUtils.getYRotationAngle(this, gvrContext.getMainScene().getMainCameraRig()), 0,
                1, 0);
        GVRAnimation anim = new GVRRelativeMotionAnimation(this, ANIMATION_DURATION, 0,
                Y_ANIMATION_DELTA, 0);
        anim.start(gvrContext.getAnimationEngine());
        playOpacityAnimation(gvrContext);

    }

    public void playOpacityAnimation(GVRContext gvrContext) {

        this.getRenderData().getMaterial().setOpacity(1);
        GVRAnimation anim = new GVROpacityAnimation(this, OPACITY_ANIMATION_DURATION, 0);

        anim.start(gvrContext.getAnimationEngine());

    }

}
