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

package org.gearvrf.physics;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * Created by c.bozzetto on 19/05/2017.
 */

/**
 * Represents a constraint that restricts translation of two {@linkplain GVRRigidBody rigid bodies}
 * to keep fixed distance from a local pivot.
 */
public class GVRPoint2PointConstraint extends GVRConstraint {

    /**
     * Constructs new instance of point-to-point constraint.
     *
     * @param gvrContext the context of the app
     * @param rigidBodyB the second rigid body (not the owner) in this constraint
     * @param pivotInA the pivot point (x, y and z coordinates) related to body A (the owner)
     * @param pivotInB the pivot point related to body B
     */
    public GVRPoint2PointConstraint(GVRContext gvrContext, GVRRigidBody rigidBodyB,
                                    float pivotInA[], float pivotInB[]) {
        this(gvrContext,
                Native3DPoint2PointConstraint.ctor(rigidBodyB.getNative(), pivotInA, pivotInB));

        mBodyB = rigidBodyB;
    }

    /** Used only by {@link GVRPhysicsLoader} */
    GVRPoint2PointConstraint(GVRContext gvrContext, long nativeConstraint) {
        super(gvrContext, nativeConstraint);
    }

    /**
     * Get the pivot for body A
     *
     * @return an array containing x, y and z coordinates of pivot
     */
    public float[] getPivotInA() {
        return Native3DPoint2PointConstraint.getPivotInA(getNative());
    }

    /**
     * Set a new pivot for body A
     *
     * @param pivot an array containing x, y and z coordinates of new pivot
     */
    public void setPivotInA(final float pivot[]) {
        Native3DPoint2PointConstraint.setPivotInA(getNative(), pivot[0], pivot[1], pivot[2]);
    }

    /**
     * Get the pivot for body B
     *
     * @return an array containing x, y and z coordinates of pivot
     */
    public float[] getPivotInB() {
        return Native3DPoint2PointConstraint.getPivotInB(getNative());
    }

    /**
     * Set a new pivot for body B
     *
     * @param pivot an array containing x, y and z coordinates of new pivot
     */
    public void setPivotInB(final float pivot[]) {
        Native3DPoint2PointConstraint.setPivotInB(getNative(), pivot[0], pivot[1], pivot[2]);
    }
}

class Native3DPoint2PointConstraint {
    static native long ctor(long rbB, float pivotInA[], float pivotInB[]);

    static native void setPivotInA(long nativeConstraint, float x, float y, float z);

    static native float[] getPivotInA(long nativeConstraint);

    static native void setPivotInB(long nativeConstraint, float x, float y, float z);

    static native float[] getPivotInB(long nativeConstraint);
}