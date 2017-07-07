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
// Created by c.bozzetto on 09/06/2017.
//

#include "physics_genericconstraint.h"
#include "physics_rigidbody.h"
#include "bullet/bullet_generic6dofconstraint.h"

namespace gvr {

    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_ctor(
            JNIEnv *env, jobject obj, jlong rigidBodyB, jfloatArray const joint,
            jfloatArray const rotationA, jfloatArray const rotationB);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_setLinearLowerLimits(
            JNIEnv *env, jobject obj, jlong jconstr, jfloat limitX, jfloat limitY, jfloat limitZ);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_getLinearLowerLimits(
            JNIEnv *env, jobject obj, jlong jconstr);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_setLinearUpperLimits(
            JNIEnv *env, jobject obj, jlong jconstr, jfloat limitX, jfloat limitY, jfloat limitZ);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_getLinearUpperLimits(
            JNIEnv *env, jobject obj, jlong jconstr);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_setAngularLowerLimits(
            JNIEnv *env, jobject obj, jlong jconstr, jfloat limitX, jfloat limitY, jfloat limitZ);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_getAngularLowerLimits(
            JNIEnv *env, jobject obj, jlong jconstr);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_setAngularUpperLimits(
            JNIEnv *env, jobject obj, jlong jconstr, jfloat limitX, jfloat limitY, jfloat limitZ);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_getAngularUpperLimits(
            JNIEnv *env, jobject obj, jlong jconstr);
    }

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_ctor(
            JNIEnv *env, jobject obj, jlong rigidBodyB, jfloatArray const joint,
            jfloatArray const rotationA, jfloatArray const rotationB) {
        PhysicsRigidBody *body = reinterpret_cast<PhysicsRigidBody*>(rigidBodyB);
        float const *_joint = env->GetFloatArrayElements(joint, 0);
        float const *_rotA = env->GetFloatArrayElements(rotationA, 0);
        float const *_rotB = env->GetFloatArrayElements(rotationB, 0);

        return reinterpret_cast<jlong>(new BulletGeneric6dofConstraint(body, _joint, _rotA, _rotB));
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_setLinearLowerLimits(
            JNIEnv *env, jobject obj, jlong jconstr, jfloat limitX, jfloat limitY, jfloat limitZ) {
        reinterpret_cast<PhysicsGenericConstraint*>(jconstr)->setLinearLowerLimits(
                limitX, limitY, limitZ);
    }

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_getLinearLowerLimits(
            JNIEnv *env, jobject obj, jlong jconstr) {
        jfloatArray temp = env->NewFloatArray(3);
        env->SetFloatArrayRegion(temp, 0, 3, reinterpret_cast<PhysicsGenericConstraint*>(jconstr)->
                getLinearLowerLimits().vec);
        return temp;
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_setLinearUpperLimits(
            JNIEnv *env, jobject obj, jlong jconstr, jfloat limitX, jfloat limitY, jfloat limitZ) {
        reinterpret_cast<PhysicsGenericConstraint*>(jconstr)->setLinearUpperLimits(
                limitX, limitY, limitZ);
    }

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_getLinearUpperLimits(
            JNIEnv *env, jobject obj, jlong jconstr) {
        jfloatArray temp = env->NewFloatArray(3);
        env->SetFloatArrayRegion(temp, 0, 3, reinterpret_cast<PhysicsGenericConstraint*>(jconstr)->
                getLinearUpperLimits().vec);
        return temp;
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_setAngularLowerLimits(
            JNIEnv *env, jobject obj, jlong jconstr, jfloat limitX, jfloat limitY, jfloat limitZ) {
        reinterpret_cast<PhysicsGenericConstraint*>(jconstr)->setAngularLowerLimits(
                limitX, limitY, limitZ);
    }

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_getAngularLowerLimits(
            JNIEnv *env, jobject obj, jlong jconstr) {
        jfloatArray temp = env->NewFloatArray(3);
        env->SetFloatArrayRegion(temp, 0, 3, reinterpret_cast<PhysicsGenericConstraint*>(jconstr)->
                getAngularLowerLimits().vec);
        return temp;
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_setAngularUpperLimits(
            JNIEnv *env, jobject obj, jlong jconstr, jfloat limitX, jfloat limitY, jfloat limitZ) {
        reinterpret_cast<PhysicsGenericConstraint*>(jconstr)->setAngularUpperLimits(
                limitX, limitY, limitZ);
    }

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_physics_Native3DGenericConstraint_getAngularUpperLimits(
            JNIEnv *env, jobject obj, jlong jconstr) {
        jfloatArray temp = env->NewFloatArray(3);
        env->SetFloatArrayRegion(temp, 0, 3, reinterpret_cast<PhysicsGenericConstraint*>(jconstr)->
                getAngularUpperLimits().vec);
        return temp;
    }

}