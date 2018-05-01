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

import java.util.List;

/**
 * Base class to represent a constraint for the movement of two
 * {@linkplain GVRRigidBody rigid bodies}.
 * <p>
 * After created anf fully configured a constraint must be attached to a
 * {@linkplain org.gearvrf.GVRSceneObject scene object} containing a rigid body that will become
 * the owner of this constraint (body A).
 */
abstract class GVRConstraint extends GVRPhysicsWorldObject {

    static final int fixedConstraintId = 1;
    static final int point2pointConstraintId = 2;
    static final int sliderConstraintId = 3;
    static final int hingeConstraintId = 4;
    static final int coneTwistConstraintId = 5;
    static final int genericConstraintId = 6;

    protected GVRConstraint(GVRContext gvrContext, long nativePointer) {
        super(gvrContext, nativePointer);
    }

    protected GVRConstraint(GVRContext gvrContext, long nativePointer, List<NativeCleanupHandler> cleanupHandlers) {
        super(gvrContext, nativePointer, cleanupHandlers);
    }

    @Override
    protected void addToWorld(GVRWorld world) {
        if (world != null) {
            world.addConstraint(this);
        }
    }

    @Override
    protected void removeFromWorld(GVRWorld world) {
        if (world != null) {
            world.removeConstraint(this);
        }
    }

    /**
     * Sets the breaking impulse for a constraint.
     *
     * @param impulse the breaking impulse value.
     */
    public void setBreakingImpulse(float impulse) {
        Native3DConstraint.setBreakingImpulse(getNative(), impulse);
    }

    /**
     * Gets the breaking impulse for a constraint.
     *
     * @return the breaking impulse value for the constraint.
     */
    public float getBreakingImpulse() {
        return Native3DConstraint.getBreakingImpulse(getNative());
    }

    static public long getComponentType() {
        return Native3DConstraint.getComponentType();
    }
}

class Native3DConstraint {
    static native long getComponentType();

    static native int getConstraintType(long nativeConstraint);

    static native void setBreakingImpulse(long nativeConstraint, float impulse);

    static native float getBreakingImpulse(long nativeConstraint);
}
