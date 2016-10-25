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

/***************************************************************************
 * Represents a physics 3D world
 ***************************************************************************/

#include "../bullet/bullet_world.h"
#include "../bullet/bullet_rigidbody.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_NativePhysics3DWorld_ctor(JNIEnv * env, jobject obj);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_NativePhysics3DWorld_getComponentType(JNIEnv * env, jobject obj);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_NativePhysics3DWorld_addRigidBody(JNIEnv * env, jobject obj,
            jlong jworld, jlong jrigid_body);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_NativePhysics3DWorld_removeRigidBody(JNIEnv * env, jobject obj,
            jlong jworld, jlong jrigid_body);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_NativePhysics3DWorld_step(JNIEnv * env, jobject obj,
            jlong jworld, jfloat jtime_step);

    JNIEXPORT jobjectArray JNICALL
    Java_org_gearvrf_physics_NativePhysics3DWorld_listCollisions(JNIEnv * env, jobject obj,
                                                                    jlong jworld);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_NativePhysics3DWorld_ctor(JNIEnv * env, jobject obj) {
    return reinterpret_cast<jlong>(new BulletWorld());
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_NativePhysics3DWorld_getComponentType(JNIEnv * env, jobject obj) {
    return BulletWorld::getComponentType();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_NativePhysics3DWorld_addRigidBody(JNIEnv * env, jobject obj,
        jlong jworld, jlong jrigid_body) {
    BulletWorld *world = reinterpret_cast<BulletWorld*>(jworld);
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    world->addRigidBody(rigid_body);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_NativePhysics3DWorld_removeRigidBody(JNIEnv * env, jobject obj,
        jlong jworld, jlong jrigid_body) {
    BulletWorld *world = reinterpret_cast<BulletWorld*>(jworld);
    BulletRigidBody* rigid_body = reinterpret_cast<BulletRigidBody*>(jrigid_body);

    world->removeRigidBody(rigid_body);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_NativePhysics3DWorld_step(JNIEnv * env, jobject obj,
        jlong jworld, jfloat jtime_step) {
    BulletWorld *world = reinterpret_cast<BulletWorld*>(jworld);

    world->step((float)jtime_step);
}

JNIEXPORT jobjectArray JNICALL
Java_org_gearvrf_physics_NativePhysics3DWorld_listCollisions(JNIEnv * env, jobject obj, jlong jworld) {

    jclass collisionInfoClass = env->FindClass("org/gearvrf/physics/GVRCollisionInfo");
    jmethodID collisionInfoConstructor = env->GetMethodID(collisionInfoClass, "<init>", "(JJ[FF)V");

    BulletWorld *world = reinterpret_cast <BulletWorld*> (jworld);
    std::vector <ContactPoint> contactPoints;

    world->listCollisions(contactPoints);

    int size = contactPoints.size();
    jobjectArray jnewlist = env->NewObjectArray(size, collisionInfoClass, NULL);

    int i = 0;
    for (auto it = contactPoints.begin(); it != contactPoints.end(); ++it) {
        const ContactPoint& data = *it;

        jfloatArray normal = env->NewFloatArray(3);
        env->SetFloatArrayRegion(normal, 0, 3, data.normal);

        jobject contactObject = env->NewObject(collisionInfoClass, collisionInfoConstructor,
                                               (jlong)data.body0, (jlong)data.body1,
                                               (jfloatArray)normal, (jfloat)data.distance);

        env->SetObjectArrayElement(jnewlist, i++, contactObject);
        env->DeleteLocalRef(contactObject);
    }

    env->DeleteLocalRef(collisionInfoClass);

    return jnewlist;
}

}
