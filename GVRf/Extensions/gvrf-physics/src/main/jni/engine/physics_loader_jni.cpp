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

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include "physics_world.h"
#include "physics_rigidbody.h"
#include "physics_constraint.h"
#include "physics_loader.h"

#include "bullet/bullet_fileloader.h"

static char tag[] = "PhysLoaderJNI";

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_NativePhysics3DLoader_ctor(JNIEnv* env, jclass clazz,
            jbyteArray byteArr, jint arrLen, jboolean ignoreUpAxis);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_physics_NativePhysics3DLoader_delete(JNIEnv* env, jclass clazz, jlong jloader);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_NativePhysics3DLoader_getNextRigidBody(JNIEnv* env, jclass clazz,
            jlong jloader);

    JNIEXPORT jstring JNICALL
    Java_org_gearvrf_physics_NativePhysics3DLoader_getRigidBodyName(JNIEnv* env, jclass clazz,
            jlong jloader, jlong rigid_body);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_NativePhysics3DLoader_getNextConstraint(JNIEnv* env, jclass clazz,
            jlong jloader);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_NativePhysics3DLoader_getConstraintBodyA(JNIEnv* env, jclass clazz,
            jlong jloader, jlong jconstraint);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_physics_NativePhysics3DLoader_getConstraintBodyB(JNIEnv* env, jclass clazz,
            jlong jloader, jlong jconstraint);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_NativePhysics3DLoader_ctor(JNIEnv* env, jclass clazz,
    jbyteArray byteArr, jint len, jboolean ignoreUpAxis)
{
    jbyte* data = env->GetByteArrayElements(byteArr, NULL);

    if (data == NULL) {
        return 0;
    }

    char *buffer = new char[len];
    memcpy(buffer, data, len);

    env->ReleaseByteArrayElements(byteArr, data, JNI_ABORT);

    PhysicsLoader *loader = new BulletFileLoader(buffer, len, ignoreUpAxis);

    delete[] buffer;

    return reinterpret_cast<jlong>(loader);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_physics_NativePhysics3DLoader_delete(JNIEnv* env, jclass clazz, jlong jloader)
{
    PhysicsLoader *loader = reinterpret_cast<PhysicsLoader*>(jloader);

    delete loader;
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_NativePhysics3DLoader_getNextRigidBody(JNIEnv* env, jclass clazz,
        jlong jloader)
{
    PhysicsLoader *loader = reinterpret_cast<PhysicsLoader*>(jloader);

    return reinterpret_cast<jlong>(loader->getNextRigidBody());
}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_physics_NativePhysics3DLoader_getRigidBodyName(JNIEnv* env, jclass clazz,
        jlong jloader, jlong jrigid_body)
{
    PhysicsLoader *loader = reinterpret_cast<PhysicsLoader*>(jloader);
    PhysicsRigidBody *body = reinterpret_cast<PhysicsRigidBody*>(jrigid_body);

    jstring name = env->NewStringUTF(loader->getRigidBodyName(body));

    return name;
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_NativePhysics3DLoader_getNextConstraint(JNIEnv* env, jclass clazz,
        jlong jloader)
{
    PhysicsLoader *loader = reinterpret_cast<PhysicsLoader*>(jloader);

    return reinterpret_cast<jlong>(loader->getNextConstraint());
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_NativePhysics3DLoader_getConstraintBodyA(JNIEnv* env, jclass clazz,
        jlong jloader, jlong jconstraint)
{
    PhysicsLoader *loader = reinterpret_cast<PhysicsLoader*>(jloader);
    PhysicsConstraint *constraint = reinterpret_cast<PhysicsConstraint*>(jconstraint);

    return reinterpret_cast<jlong >(loader->getConstraintBodyA(constraint));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_physics_NativePhysics3DLoader_getConstraintBodyB(JNIEnv* env, jclass clazz,
                                                                  jlong jloader, jlong jconstraint)
{
    PhysicsLoader *loader = reinterpret_cast<PhysicsLoader*>(jloader);
    PhysicsConstraint *constraint = reinterpret_cast<PhysicsConstraint*>(jconstraint);

    return reinterpret_cast<jlong >(loader->getConstraintBodyB(constraint));
}

}