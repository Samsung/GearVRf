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

/**
 * This animation uses
 * {@link GVRTransform#rotateByAxisWithPivot(float, float, float, float, float, float, float) }
 * to do an animated rotation about a specific axis, with a specific pivot
 * point.
 */
public class GVRRotationByAxisWithPivotAnimation extends GVRTransformAnimation {

    private final Orientation mOrientation;
    private final Position mPosition;
    private final float mAngle, //
            mAxisX, mAxisY, mAxisZ, //
            mPivotX, mPivotY, mPivotZ;

    /**
     * Use
     * {@link GVRTransform#rotateByAxisWithPivot(float, float, float, float, float, float, float)}
     * to do an animated rotation about a specific axis with a specific pivot.
     * 
     * @param target
     *            {@link GVRTransform} to animate.
     * @param duration
     *            The animation duration, in seconds.
     * @param angle
     *            the rotation angle, in degrees
     * @param axisX
     *            the normalized axis x component
     * @param axisY
     *            the normalized axis y component
     * @param axisZ
     *            the normalized axis z component
     * @param pivotX
     *            The x-coordinate of the pivot point
     * @param pivotY
     *            The y-coordinate of the pivot point
     * @param pivotZ
     *            The z-coordinate of the pivot point
     */
    public GVRRotationByAxisWithPivotAnimation(GVRTransform target,
            float duration, float angle, float axisX, float axisY, float axisZ,
            float pivotX, float pivotY, float pivotZ) {
        super(target, duration);

        mOrientation = new Orientation();
        mPosition = new Position();

        mAngle = angle;
        mAxisX = axisX;
        mAxisY = axisY;
        mAxisZ = axisZ;
        mPivotX = pivotX;
        mPivotY = pivotY;
        mPivotZ = pivotZ;
    }

    /**
     * Use
     * {@link GVRTransform#rotateByAxisWithPivot(float, float, float, float, float, float, float)}
     * to do an animated rotation about a specific axis with a specific pivot.
     * 
     * @param target
     *            {@link GVRSceneObject} containing a {@link GVRTransform}
     * @param duration
     *            The animation duration, in seconds.
     * @param angle
     *            the rotation angle, in degrees
     * @param axisX
     *            the normalized axis x component
     * @param axisY
     *            the normalized axis y component
     * @param axisZ
     *            the normalized axis z component
     * @param pivotX
     *            The x-coordinate of the pivot point
     * @param pivotY
     *            The y-coordinate of the pivot point
     * @param pivotZ
     *            The z-coordinate of the pivot point
     */
    public GVRRotationByAxisWithPivotAnimation(GVRSceneObject target,
            float duration, float angle, float axisX, float axisY, float axisZ,
            float pivotX, float pivotY, float pivotZ) {
        this(getTransform(target), duration, angle, axisX, axisY, axisZ,
                pivotX, pivotY, pivotZ);
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio) {
        // Reset rotation and position (this is pretty cheap - GVRF uses a 'lazy
        // update' policy on the matrix, so three changes don't cost all that
        // much more than one)
        mOrientation.setOrientation();
        mPosition.setPosition();

        // Rotate with pivot, from start orientation & position
        float angle = ratio * mAngle;
        mTransform.rotateByAxisWithPivot(angle, mAxisX, mAxisY, mAxisZ,
                mPivotX, mPivotY, mPivotZ);
    }
}
