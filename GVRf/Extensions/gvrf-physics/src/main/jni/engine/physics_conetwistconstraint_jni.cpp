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

//
// Created by c.bozzetto on 06/06/2017.
//

#include "physics_conetwistconstraint.h"
#include "physics_rigidbody.h"
#include "bullet/bullet_conetwistconstraint.h"

namespace gvr {

    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_ctor(JNIEnv *env, jobject obj,
                                                              jlong rigidBodyB, jfloatArray pivot,
                                                              const jfloatArray bodyRotation,
                                                              const jfloatArray coneRotation);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_setSwingLimit(JNIEnv *env, jobject obj,
                                                                       jlong jconstraint,
                                                                       jfloat limit);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_getSwingLimit(JNIEnv *env, jobject obj,
                                                                       jlong jconstraint);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_setTwistLimit(JNIEnv *env, jobject obj,
                                                                       jlong jconstraint,
                                                                       jfloat limit);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_getTwistLimit(JNIEnv *env, jobject obj,
                                                                       jlong jconstraint);

    }

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_ctor(JNIEnv *env, jobject obj,
                                                              jlong rigidBodyB,
                                                              const jfloatArray pivot,
                                                              const jfloatArray bodyRotation,
                                                              const jfloatArray coneRotation) {
        PhysicsVec3 _pivot(env->GetFloatArrayElements(pivot, 0));
        PhysicsMat3x3 _b_rot(env->GetFloatArrayElements(bodyRotation, 0));
        PhysicsMat3x3 _c_rot(env->GetFloatArrayElements(coneRotation, 0));

        return reinterpret_cast<jlong>(new
                BulletConeTwistConstraint(reinterpret_cast<PhysicsRigidBody*>(rigidBodyB),
                                          _pivot, _b_rot, _c_rot));
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_setSwingLimit(JNIEnv *env, jobject obj,
                                                                       jlong jconstraint,
                                                                       jfloat limit) {
        reinterpret_cast<PhysicsConeTwistConstraint*>(jconstraint)->setSwingLimit(limit);
    }

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_getSwingLimit(JNIEnv *env, jobject obj,
                                                                       jlong jconstraint) {
        return reinterpret_cast<PhysicsConeTwistConstraint*>(jconstraint)->getSwingLimit();
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_setTwistLimit(JNIEnv *env, jobject obj,
                                                                       jlong jconstraint,
                                                                       jfloat limit) {
        reinterpret_cast<PhysicsConeTwistConstraint*>(jconstraint)->setTwistLimit(limit);
    }

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DConeTwistConstraint_getTwistLimit(JNIEnv *env, jobject obj,
                                                                       jlong jconstraint) {
        return reinterpret_cast<PhysicsConeTwistConstraint*>(jconstraint)->getTwistLimit();
    }

}
