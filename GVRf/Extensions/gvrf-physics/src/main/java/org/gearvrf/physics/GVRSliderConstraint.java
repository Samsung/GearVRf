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
 * Created by c.bozzetto on 31/05/2017.
 */

/**
 * Represents a constraint that allows two {@linkplain GVRRigidBody rigid bodies} to rotate and
 * translate over a single axis.
 */
public class GVRSliderConstraint extends GVRConstraint {

    /**
     * Constructs a new instance of a slider constraint.
     *
     * @param gvrContext the context of the app
     * @param rigidBody the second rigid body (not the owner) in this constraint.
     */
    public GVRSliderConstraint(GVRContext gvrContext, GVRRigidBody rigidBody) {
        super(gvrContext, Native3DSliderConstraint.ctor(rigidBody.getNative()));
    }

    /** Used only by {@link GVRPhysicsLoader} */
    GVRSliderConstraint(GVRContext gvrContext, long nativeConstraint) {
        super(gvrContext, nativeConstraint);
    }

    /**
     * Sets the lower limit for rotation.
     *
     * @param limit the angular limit in radians.
     */
    public void setAngularLowerLimit(float limit) {
        Native3DSliderConstraint.setAngularLowerLimit(getNative(), limit);
    }

    /**
     * Gets the lower limit for rotation.
     *
     * @return the angular limit in radians.
     */
    public float getAngularLowerLimit() {
        return Native3DSliderConstraint.getAngularLowerLimit(getNative());
    }

    /**
     * Sets the upper limit for rotation.
     *
     * @param limit the angular limit in radians.
     */
    public void setAngularUpperLimit(float limit) {
        Native3DSliderConstraint.setAngularUpperLimit(getNative(), limit);
    }

    /**
     * Gets the upper limit for rotation.
     *
     * @return the angular limit in radians.
     */
    public float getAngularUpperLimit() {
        return Native3DSliderConstraint.getAngularUpperLimit(getNative());
    }

    /**
     * Sets the lower limit for translation.
     *
     * @param limit the linear limit.
     */
    public void setLinearLowerLimit(float limit) {
        Native3DSliderConstraint.setLinearLowerLimit(getNative(), limit);
    }

    /**
     * Gets the lower limit for translation.
     *
     * @return the linear limit in radians.
     */
    public float getLinearLowerLimit() {
        return Native3DSliderConstraint.getLinearLowerLimit(getNative());
    }

    /**
     * Sets the upper limit for translation.
     *
     * @param limit the linear limit.
     */
    public void setLinearUpperLimit(float limit) {
        Native3DSliderConstraint.setLinearUpperLimit(getNative(), limit);
    }

    /**
     * Gets the upper limit for translation.
     *
     * @return the linear limit in radians.
     */
    public float getLinearUpperLimit() {
        return Native3DSliderConstraint.getLinearUpperLimit(getNative());
    }
}


class Native3DSliderConstraint {
    static native long ctor(long rbB);

    static native void setAngularLowerLimit(long nativeConstraint, float limit);

    static native float getAngularLowerLimit(long nativeConstraint);

    static native void setAngularUpperLimit(long nativeConstraint, float limit);

    static native float getAngularUpperLimit(long nativeConstraint);

    static native void setLinearLowerLimit(long nativeConstraint, float limit);

    static native float getLinearLowerLimit(long nativeConstraint);

    static native void setLinearUpperLimit(long nativeConstraint, float limit);

    static native float getLinearUpperLimit(long nativeConstraint);
}
