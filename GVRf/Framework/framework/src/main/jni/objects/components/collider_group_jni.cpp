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
 * JNI
 ***************************************************************************/

#include "collider_group.h"

#include "util/gvr_jni.h"
#include "glm/gtc/type_ptr.hpp"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeColliderGroup_ctor(JNIEnv * env,
            jobject obj);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeColliderGroup_addCollider(JNIEnv * env,
            jobject obj, jlong jgroup, jlong jcollider);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeColliderGroup_removeCollider(
            JNIEnv * env, jobject obj, jlong jgroup,
            jlong jcollider);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeColliderGroup_getHit(JNIEnv * env, jobject obj, jlong jgroup);
}


JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeColliderGroup_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new ColliderGroup());
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeColliderGroup_getHit(JNIEnv * env, jobject obj, jlong jgroup) {
    ColliderGroup* group = reinterpret_cast<ColliderGroup*>(jgroup);
    const glm::vec3& hitpos = group->hit();
    jsize size = sizeof(hitpos) / sizeof(jfloat);
    if (size != 3) {
        LOGE("sizeof(hit) / sizeof(jfloat) != 3");
        throw "sizeof(hit) / sizeof(jfloat) != 3";
    }
    jfloatArray jhit = env->NewFloatArray(size);
    env->SetFloatArrayRegion(jhit, 0, size, glm::value_ptr(hitpos));
    return jhit;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeColliderGroup_addCollider(JNIEnv * env,
        jobject obj, jlong jgroup, jlong jcollider) {
    ColliderGroup* group = reinterpret_cast<ColliderGroup*>(jgroup);
    Collider* collider = reinterpret_cast<Collider*>(jcollider);
    group->addCollider(collider);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeColliderGroup_removeCollider(
        JNIEnv * env, jobject obj, jlong jgroup,
        jlong jcollider) {
    ColliderGroup* group = reinterpret_cast<ColliderGroup*>(jgroup);
    Collider* collider = reinterpret_cast<Collider*>(jcollider);
    group->removeCollider(collider);
}

}
