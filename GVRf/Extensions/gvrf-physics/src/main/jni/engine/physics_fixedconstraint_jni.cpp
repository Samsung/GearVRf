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

#include "bullet/bullet_fixedconstraint.h"
#include "physics_rigidbody.h"

namespace gvr {

    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DFixedConstraint_ctor(JNIEnv * env, jobject obj, jlong rigidBodyB);
    }

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DFixedConstraint_ctor(JNIEnv * env, jobject obj, jlong rigidBodyB) {
        return reinterpret_cast<jlong>(
                new BulletFixedConstraint(reinterpret_cast<PhysicsRigidBody*>(rigidBodyB)));
    }

}
