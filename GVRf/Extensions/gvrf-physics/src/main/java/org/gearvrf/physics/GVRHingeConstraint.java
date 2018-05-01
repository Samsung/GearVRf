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

/**
 * Created by c.bozzetto on 30/05/2017.
 */

/**
 * Represents a hinge constraint that restricts two {@linkplain GVRRigidBody rigid bodies} to only
 * rotate around one axis.
 */
public class GVRHingeConstraint extends GVRConstraint {

    /**
     * Constructs a new instance of hinge constraint.
     *
     * @param gvrContext the context of the app
     * @param rigidBodyB the second rigid body (not the owner) in this constraint
     * @param pivotInA the pivot point related to body A (the owner)
     * @param pivotInB the pivot point related to body B
     * @param axisInA the axis around which body A can rotate
     * @param axisInB the axis around which body B can rotate
     */
    public GVRHingeConstraint(GVRContext gvrContext, GVRRigidBody rigidBodyB, float pivotInA[],
                                 float pivotInB[], float axisInA[], float axisInB[]) {
        super(gvrContext, Native3DHingeConstraint.ctor(rigidBodyB.getNative(), pivotInA, pivotInB,
                axisInA, axisInB));
    }

    /** Used only by {@link GVRPhysicsLoader} */
    GVRHingeConstraint(GVRContext gvrContext, long nativeConstraint) {
        super(gvrContext, nativeConstraint);
    }

    /**
     * Set rotation limits (in radians) for the bodies.
     *
     * @param lower lower limit
     * @param upper upper limit
     */
    public void setLimits(float lower, float upper) {
        Native3DHingeConstraint.setLimits(getNative(), lower, upper);
    }

    /**
     * Gets the lower rotation limit for the constraint
     *
     * @return the angular limit in radians
     */
    public float getLowerLimit() {
        return Native3DHingeConstraint.getLowerLimit(getNative());
    }


    /**
     * Gets the upper rotation limit for the constraint
     *
     * @return the angular limit in radians
     */
    public float getUpperLimit() {
        return Native3DHingeConstraint.getUpperLimit(getNative());
    }
}

class Native3DHingeConstraint {
    static native long ctor(long rbB, float pivotInA[], float pivotInB[], float axisInA[],
                            float axisInB[]);

    static native void setLimits(long nativeConstraint, float lower, float upper);

    static native float getLowerLimit(long nativeConstraint);

    static native float getUpperLimit(long nativeConstraint);
}