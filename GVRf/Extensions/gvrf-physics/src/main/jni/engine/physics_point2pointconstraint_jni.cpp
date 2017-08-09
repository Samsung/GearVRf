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

#include "physics_point2pointconstraint.h"
#include "physics_rigidbody.h"
#include "bullet/bullet_point2pointconstraint.h"

namespace gvr {
    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_ctor(JNIEnv * env, jobject obj,
        jlong rigidBodyB, jfloatArray pivotInA, jfloatArray pivotInB);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_setPivotInA(JNIEnv * env, jobject obj,
                                                                       jlong jp2p_constraint,
                                                                       jfloat x, jfloat y,
                                                                       jfloat z);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_getPivotInA(JNIEnv * env, jobject obj,
                                                                       jlong jp2p_constraint);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_setPivotInB(JNIEnv * env, jobject obj,
                                                                       jlong jp2p_constraint,
                                                                       jfloat x, jfloat y,
                                                                       jfloat z);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_getPivotInB(JNIEnv * env, jobject obj,
                                                                       jlong jp2p_constraint);


    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_setBreakingImpulse(JNIEnv * env,
                                                                              jobject obj,
                                                                              jlong jp2p_constraint,
                                                                              jfloat impulse);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_getBreakingLimit(JNIEnv * env,
                                                                            jobject obj,
                                                                            jlong jp2p_constraint);
    }

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_ctor(JNIEnv * env, jobject obj, 
        jlong rigidBodyB, jfloatArray pivotInA, jfloatArray pivotInB) {
        jfloat *pA = env->GetFloatArrayElements(pivotInA, 0);
        jfloat *pB = env->GetFloatArrayElements(pivotInB, 0);
        return reinterpret_cast<jlong>(new BulletPoint2PointConstraint(
                    reinterpret_cast<PhysicsRigidBody*>(rigidBodyB), pA, pB));
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_setPivotInA(JNIEnv * env, jobject obj,
                                                                       jlong jp2p_constraint,
                                                                       jfloat x, jfloat y,
                                                                       jfloat z) {
        PhysicsVec3 v(x, y, z);
        reinterpret_cast<PhysicsPoint2pointConstraint*>(jp2p_constraint)->setPivotInA(v);
    }

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_getPivotInA(JNIEnv * env, jobject obj,
                                                                       jlong jp2p_constraint) {
        PhysicsVec3 v =
                reinterpret_cast<PhysicsPoint2pointConstraint*>(jp2p_constraint)->getPivotInA();
        jfloatArray result = env->NewFloatArray(3);
        env->SetFloatArrayRegion(result, 0, 3, v.vec);

        return result;
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_setPivotInB(JNIEnv * env, jobject obj,
                                                                       jlong jp2p_constraint,
                                                                       jfloat x, jfloat y,
                                                                       jfloat z) {
        PhysicsVec3 v(x, y, z);
        reinterpret_cast<PhysicsPoint2pointConstraint*>(jp2p_constraint)->setPivotInB(v);
    }

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_getPivotInB(JNIEnv * env, jobject obj,
                                                                       jlong jp2p_constraint) {
        PhysicsVec3 v =
                reinterpret_cast<PhysicsPoint2pointConstraint*>(jp2p_constraint)->getPivotInB();
        jfloatArray result = env->NewFloatArray(3);
        env->SetFloatArrayRegion(result, 0, 3, v.vec);

        return result;
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_setBreakingImpulse(JNIEnv * env,
                                                                              jobject obj,
                                                                              jlong jp2p_constraint,
                                                                              jfloat impulse) {
        reinterpret_cast<PhysicsConstraint*>(jp2p_constraint)->setBreakingImpulse(impulse);
    }

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DPoint2PointConstraint_getBreakingLimit(JNIEnv * env,
                                                                            jobject obj,
                                                                            jlong jp2p_constraint) {
        return reinterpret_cast<PhysicsConstraint*>(jp2p_constraint)->getBreakingImpulse();
    }

}
