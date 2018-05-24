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
 * Represents a constraint that forces two {@linkplain GVRRigidBody rigid bodies} to keep same
 * distance and same rotation in respect to each other.
 */
public class GVRFixedConstraint extends GVRConstraint {

    /**
     * Constructs new instance of fixed constraint.
     *
     * @param gvrContext the context of the app
     * @param rigidBodyA the first rigid body in this constraints
     * @param rigidBodyB the second rigid body in this constraint
     */
    public GVRFixedConstraint(GVRContext gvrContext, GVRRigidBody rigidBodyA,
                              GVRRigidBody rigidBodyB) {
        super(gvrContext, rigidBodyA, rigidBodyB,
                Native3DFixedConstraint.ctor(rigidBodyA.getNative(), rigidBodyB.getNative()));
    }

    /** Used only by {@link GVRPhysicsLoader} */
    GVRFixedConstraint(GVRContext gvrContext, GVRRigidBody rigidBodyA, GVRRigidBody rigidBodyB,
                       long nativeConstraint) {
        super(gvrContext, rigidBodyA, rigidBodyB, nativeConstraint);
    }
}

class Native3DFixedConstraint {
    static native long ctor(long rbA, long rbB);
}
