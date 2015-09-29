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

package org.gearvrf.controls.anim;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.controls.MainScript;
import org.gearvrf.controls.util.Util;

public class WormApplyTransformAnims {

    public static void moveWorm(GVRContext gvrContext, float scaleFactor) {
        moveWormPart(gvrContext, MainScript.worm.getHead(), scaleFactor);
        moveWormPart(gvrContext, MainScript.worm.getEnd(), scaleFactor);
    }

    public static void resetScaleWorm(float[] scaleFactor) {

        MainScript.worm.getHead().getTransform()
                .setScale(scaleFactor[0], scaleFactor[0], scaleFactor[0]);
        MainScript.worm.getMiddle().getTransform()
                .setScale(scaleFactor[1], scaleFactor[1], scaleFactor[1]);
        MainScript.worm.getEnd().getTransform()
                .setScale(scaleFactor[2], scaleFactor[2], scaleFactor[2]);
    }

    public static void scaleWorm(GVRContext gvrContext, float scaleFactor) {

        scaleWormPart(gvrContext, MainScript.worm.getHead(), scaleFactor);
        scaleWormPart(gvrContext, MainScript.worm.getMiddle(), scaleFactor);
        scaleWormPart(gvrContext, MainScript.worm.getEnd(), scaleFactor);
    }

    public static void moveWormPart(GVRContext gvrContext, GVRSceneObject part, float scaleFactor) {

        float currentScale = part.getTransform().getScaleX();
        float ratio = (currentScale + scaleFactor) / currentScale;
        float currentPartPositionX = part.getTransform().getPositionX();
        float newPartPositionX = ratio * currentPartPositionX;

        new GVRRelativeMotionAnimation(part, AnimationsTime.getScaleTime(),
                newPartPositionX
                        - currentPartPositionX, 0, 0).start(gvrContext
                .getAnimationEngine());
    }

    public static void moveWormPartToClose(GVRContext gvrContext, GVRSceneObject moveablePart,
            GVRSceneObject basePart) {

        float scaleRatio = ScaleWorm.getWorm().getHead().getTransform().getScaleX()
                / ScaleWorm.getLastSize()[0];

        float distance = (float) Util.distance(basePart, moveablePart) * scaleRatio;
        float[] newPosition = Util
                .calculatePointBetweenTwoObjects(basePart, moveablePart, distance);

        float newX = newPosition[0] - moveablePart.getTransform().getPositionX();
        float newZ = newPosition[2] - moveablePart.getTransform().getPositionZ();

        new GVRRelativeMotionAnimation(moveablePart, AnimationsTime.getScaleTime(), newX, 0, newZ)
                .start(gvrContext.getAnimationEngine());
    }

    private static void scaleWormPart(GVRContext gvrContext, GVRSceneObject part, float scaleFactor) {

        new GVRScaleAnimation(part, AnimationsTime.getScaleTime(), part.getTransform().getScaleX()
                + scaleFactor)
                .start(gvrContext.getAnimationEngine());
    }

    public static GVRRelativeMotionAnimation moveWormPartReset(GVRSceneObject moveablePart,
            GVRSceneObject basePart) {

        float scaleRatio = ScaleWorm.getLastSize()[0] / basePart.getTransform().getScaleX();

        float distance = (float) Util.distance(basePart, moveablePart) * scaleRatio;
        float[] newPosition = Util
                .calculatePointBetweenTwoObjects(basePart, moveablePart, distance);

        float newX = newPosition[0] - moveablePart.getTransform().getPositionX();
        float newZ = newPosition[2] - moveablePart.getTransform().getPositionZ();

        return new GVRRelativeMotionAnimation(moveablePart, 0f, newX, 0, newZ);
    }
}
