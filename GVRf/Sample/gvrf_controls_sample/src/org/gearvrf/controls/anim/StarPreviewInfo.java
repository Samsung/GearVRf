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

import org.gearvrf.GVRSceneObject;

public class StarPreviewInfo {

    public enum Direction {
        right, left
    }

    public static GVRSceneObject star;
    private static float rotation = 0;
    static Direction direction;
    private static float evPositionZ;
    private static float evPositionX;
    private static float evPositionY;
    private static float evRotationW;

    public static void putStarReference(GVRSceneObject star) {
        StarPreviewInfo.star = star;

        evPositionX = star.getTransform().getRotationX();
        evPositionY = star.getTransform().getRotationY();
        evPositionZ = star.getTransform().getRotationZ();
        evRotationW = star.getTransform().getRotationW();
    }

    public static void restartRotation() {
        rotation = 0;
        star.getTransform().setRotation(evRotationW, evPositionX, evPositionY, evPositionZ);
    }

    public static void changeRotationFactor(Direction direction) {

        /**
         * if the factor is different from accumulator then resets the
         * accumulator before accumulate new values
         */
        if (StarPreviewInfo.direction != direction) {
            rotation = 0;
        }

        if (direction == Direction.left) {

            if (rotation <= 360) {
                rotation += 1.51f;
            }

        } else {

            if (rotation >= -360) {
                rotation -= 1.51f;
            }
        }

        StarPreviewInfo.direction = direction;
    }

    public static float getRotation() {
        return rotation;
    }
}
