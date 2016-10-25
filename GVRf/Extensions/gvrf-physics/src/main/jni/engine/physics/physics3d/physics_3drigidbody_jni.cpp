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

#include "../bullet/bullet_rigidbody.h"

#include "glm/gtc/type_ptr.hpp"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_ctor(JNIEnv * env, jobject obj);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getComponentType(JNIEnv * env, jobject obj);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getMass(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setMass(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat mass);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_applyCentralForce(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_applyTorque(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_onAttach(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_onDetach(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getCenterX(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getCenterY(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getCenterZ(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setCenter(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getRotationW(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getRotationX(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getRotationY(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getRotationZ(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setRotation(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat w, jfloat x, jfloat y, jfloat z);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getScaleX(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getScaleY(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getScaleZ(JNIEnv * env, jobject obj,
            jlong jrigid_body);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setScale(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setGravity(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setDamping(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat linear, jfloat angular);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setLinearVelocity(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setAngularVelocity(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setAngularFactor(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setLinearFactor(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat x, jfloat y, jfloat z);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setFriction(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat n);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setRestitution(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat n);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setSleepingThresholds(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat linear, jfloat angular);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setCcdMotionThreshold(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat n);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setContactProcessingThreshold(JNIEnv * env, jobject obj,
            jlong jrigid_body, jfloat n);

    JNIEXPORT void   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_setIgnoreCollisionCheck(JNIEnv * env, jobject obj,
            jlong jrigid_body, jobject collisionObj, jboolean ignore);

    JNIEXPORT jfloatArray   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getGravity(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloatArray   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getLinearVelocity(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloatArray   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getAngularVelocity(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloatArray   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getAngularFactor(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloatArray   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getLinearFactor(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloatArray   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getDamping(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloat   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getFriction(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloat   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getRestitution(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloat   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getCcdMotionThreshold(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;

    JNIEXPORT jfloat   JNICALL
    Java_org_gearvrf_physics_Native3DRigidBody_getContactProcessingThreshold(JNIEnv * env, jobject obj,
            jlong jrigid_body) ;
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_ctor(JNIEnv * env, jobject obj) {
    return reinterpret_cast<jlong>(new BulletRigidBody());
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getComponentType(JNIEnv * env, jobject obj) {
    return BulletRigidBody::getComponentType();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getMass(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->getMass();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setMass(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat mass) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setMass(mass);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_applyCentralForce(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody *rigid_body = reinterpret_cast<BulletRigidBody *>(jrigid_body);

    rigid_body->applyCentralForce(x, y, z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_applyTorque(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody *rigid_body = reinterpret_cast<BulletRigidBody *>(jrigid_body);

    rigid_body->applyTorque(x, y, z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_onAttach(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->onAttach();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_onDetach(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->onDetach();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getCenterX(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->center_x();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getCenterY(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->center_y();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getCenterZ(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->center_z();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setCenter(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->set_center(x, y, z);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getRotationW(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->rotation_w();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getRotationX(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->rotation_x();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getRotationY(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->rotation_y();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getRotationZ(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->rotation_z();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setRotation(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat w, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->set_rotation(w, x, y, z);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getScaleX(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->scale_x();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getScaleY(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->scale_y();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getScaleZ(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->scale_z();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setScale(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->set_scale(x, y, z);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setGravity(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setGravity(x, y, z);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setDamping(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat linear, jfloat angular) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setDamping(linear, angular);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setLinearVelocity(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setLinearVelocity(x, y, z);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setAngularVelocity(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setAngularVelocity(x, y, z);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setAngularFactor(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setAngularFactor(x, y, z);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setLinearFactor(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat x, jfloat y, jfloat z) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setLinearFactor(x, y, z);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setFriction(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat n) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setFriction(n);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setRestitution(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat n) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setRestitution(n);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setSleepingThresholds(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat linear, jfloat angular) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setSleepingThresholds(linear, angular);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setCcdMotionThreshold(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat n) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setCcdMotionThreshold(n);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setContactProcessingThreshold(JNIEnv * env, jobject obj,
        jlong jrigid_body, jfloat n) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setContactProcessingThreshold(n);
}

JNIEXPORT void   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_setIgnoreCollisionCheck(JNIEnv * env, jobject obj,
        jlong jrigid_body, jobject collisionObj, jboolean ignore) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    rigid_body->setIgnoreCollisionCheck(reinterpret_cast<BulletRigidBody*>(jrigid_body), ignore);
}

JNIEXPORT jfloatArray   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getGravity(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    jfloat temp[3];

    rigid_body->getGravity(temp);

    jfloatArray result = env->NewFloatArray(3);

    env->SetFloatArrayRegion(result, 0, 3, temp);

    return result;
}

JNIEXPORT jfloatArray   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getLinearVelocity(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    jfloat temp[3];

    rigid_body->getLinearVelocity(temp);

    jfloatArray result = env->NewFloatArray(3);

    env->SetFloatArrayRegion(result, 0, 3, temp);

    return result;
}

JNIEXPORT jfloatArray   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getAngularVelocity(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    jfloat temp[3];

    rigid_body->getAngularVelocity(temp);

    jfloatArray result = env->NewFloatArray(3);

    env->SetFloatArrayRegion(result, 0, 3, temp);

    return result;
}

JNIEXPORT jfloatArray   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getAngularFactor(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    jfloat temp[3];

    rigid_body->getAngularFactor(temp);

    jfloatArray result = env->NewFloatArray(3);

    env->SetFloatArrayRegion(result, 0, 3, temp);

    return result;
}

JNIEXPORT jfloatArray   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getLinearFactor(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    jfloat temp[3];

    rigid_body->getLinearFactor(temp);

    jfloatArray result = env->NewFloatArray(3);

    env->SetFloatArrayRegion(result, 0, 3, temp);

    return result;
}

JNIEXPORT jfloatArray   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getDamping(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    jfloat temp[2];

    rigid_body->getDamping(temp[0], temp[1]);

    jfloatArray result = env->NewFloatArray(2);

    env->SetFloatArrayRegion(result, 0, 2, temp);

    return result;
}

JNIEXPORT jfloat   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getFriction(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->getFriction();
}

JNIEXPORT jfloat   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getRestitution(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->getRestitution();
}

JNIEXPORT jfloat   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getCcdMotionThreshold(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->getCcdMotionThreshold();
}

JNIEXPORT jfloat   JNICALL
Java_org_gearvrf_physics_Native3DRigidBody_getContactProcessingThreshold(JNIEnv * env, jobject obj,
        jlong jrigid_body) {
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    return rigid_body->getContactProcessingThreshold();
}
}
