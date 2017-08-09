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
// Created by c.bozzetto on 31/05/2017.
//

#include "physics_sliderconstraint.h"
#include "physics_rigidbody.h"
#include "bullet/bullet_sliderconstraint.h"

namespace gvr {

    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_ctor(JNIEnv * env, jobject obj,
                                                           jlong rigidBodyB);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_setAngularLowerLimit(JNIEnv * env,
                                                                           jobject obj,
                                                                           jlong jsliderconstraint,
                                                                           jfloat limit);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_getAngularLowerLimit(JNIEnv * env,
                                                                           jobject obj,
                                                                           jlong jsliderconstraint);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_setAngularUpperLimit(JNIEnv * env,
                                                                           jobject obj,
                                                                           jlong jsliderconstraint,
                                                                           jfloat limit);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_getAngularUpperLimit(JNIEnv * env,
                                                                           jobject obj,
                                                                           jlong jsliderconstraint);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_setLinearLowerLimit(JNIEnv * env,
                                                                          jobject obj,
                                                                          jlong jsliderconstraint,
                                                                          jfloat limit);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_getLinearLowerLimit(JNIEnv * env,
                                                                          jobject obj,
                                                                          jlong jsliderconstraint);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_setLinearUpperLimit(JNIEnv * env,
                                                                          jobject obj,
                                                                          jlong jsliderconstraint,
                                                                          jfloat limit);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_getLinearUpperLimit(JNIEnv * env,
                                                                          jobject obj,
                                                                          jlong jsliderconstraint);
    }

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_ctor(JNIEnv * env, jobject obj,
                                                           jlong rigidBodyB) {
        return reinterpret_cast<jlong>(new
                BulletSliderConstraint(reinterpret_cast<PhysicsRigidBody*>(rigidBodyB)));
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_setAngularLowerLimit(JNIEnv * env,
                                                                           jobject obj,
                                                                           jlong jsliderconstraint,
                                                                           jfloat limit) {
        reinterpret_cast<PhysicsSliderConstraint*>(jsliderconstraint)->setAngularLowerLimit(limit);
    }

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_getAngularLowerLimit(JNIEnv * env,
                                                                           jobject obj,
                                                                           jlong jsliderconstraint) {
        return reinterpret_cast<PhysicsSliderConstraint*>(jsliderconstraint)->getAngularLowerLimit();
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_setAngularUpperLimit(JNIEnv * env,
                                                                           jobject obj,
                                                                           jlong jsliderconstraint,
                                                                           jfloat limit) {
        reinterpret_cast<PhysicsSliderConstraint*>(jsliderconstraint)->setAngularUpperLimit(limit);
    }

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_getAngularUpperLimit(JNIEnv * env,
                                                                           jobject obj,
                                                                           jlong jsliderconstraint) {
        return reinterpret_cast<PhysicsSliderConstraint*>(jsliderconstraint)->getAngularUpperLimit();
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_setLinearLowerLimit(JNIEnv * env,
                                                                          jobject obj,
                                                                          jlong jsliderconstraint,
                                                                          jfloat limit) {
        reinterpret_cast<PhysicsSliderConstraint*>(jsliderconstraint)->setLinearLowerLimit(limit);
    }

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_getLinearLowerLimit(JNIEnv * env,
                                                                          jobject obj,
                                                                          jlong jsliderconstraint) {
        return reinterpret_cast<PhysicsSliderConstraint*>(jsliderconstraint)->getLinearLowerLimit();
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_setLinearUpperLimit(JNIEnv * env,
                                                                          jobject obj,
                                                                          jlong jsliderconstraint,
                                                                          jfloat limit) {
        reinterpret_cast<PhysicsSliderConstraint*>(jsliderconstraint)->setLinearUpperLimit(limit);
    }

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DSliderConstraint_getLinearUpperLimit(JNIEnv * env,
                                                                          jobject obj,
                                                                          jlong jsliderconstraint) {
        return reinterpret_cast<PhysicsSliderConstraint*>(jsliderconstraint)->getLinearUpperLimit();
    }

}