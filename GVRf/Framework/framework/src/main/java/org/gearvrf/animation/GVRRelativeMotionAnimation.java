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

package org.gearvrf.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;

/** Animate an object's position. */
public class GVRRelativeMotionAnimation extends GVRTransformAnimation {

    private final float mStartX, mDeltaX, mStartY, mDeltaY, mStartZ, mDeltaZ;

    /**
     * Animate a move by delta x/y/z.
     * 
     * @param target
     *            {@link GVRTransform} to animate
     * @param duration
     *            The animation duration, in seconds.
     * @param deltaX
     *            The value to add to x
     * @param deltaY
     *            The value to add to y
     * @param deltaZ
     *            The value to add to z
     */
    public GVRRelativeMotionAnimation(GVRTransform target, float duration,
            float deltaX, float deltaY, float deltaZ) {
        super(target, duration);

        mStartX = mTransform.getPositionX();
        mStartY = mTransform.getPositionY();
        mStartZ = mTransform.getPositionZ();

        mDeltaX = deltaX;
        mDeltaY = deltaY;
        mDeltaZ = deltaZ;
    }

    /**
     * Animate a move by delta x/y/z.
     * 
     * @param target
     *            {@link GVRSceneObject} containing a {@link GVRTransform}
     * @param duration
     *            The animation duration, in seconds.
     * @param deltaX
     *            The value to add to x
     * @param deltaY
     *            The value to add to y
     * @param deltaZ
     *            The value to add to z
     */
    public GVRRelativeMotionAnimation(GVRSceneObject target, float duration,
            float deltaX, float deltaY, float deltaZ) {
        this(getTransform(target), duration, deltaX, deltaY, deltaZ);
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio) {
        mTransform.setPosition(mStartX + mDeltaX * ratio, //
                mStartY + mDeltaY * ratio, //
                mStartZ + mDeltaZ * ratio);
    }
}
