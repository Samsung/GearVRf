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

#include "sphere_collider.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C"
{
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeSphereCollider_ctor(JNIEnv * env, jobject obj);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeSphereCollider_setRadius(JNIEnv * env,
            jobject obj, jlong jcollider, jfloat radius);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_NativeSphereCollider_getRadius(JNIEnv * env,
            jobject obj, jlong jcollider);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSphereCollider_ctor(JNIEnv * env, jobject obj)
{
    return reinterpret_cast<jlong>(new SphereCollider());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSphereCollider_setRadius(JNIEnv * env,
        jobject obj, jlong jcollider, jfloat radius)
{
    SphereCollider* collider = reinterpret_cast<SphereCollider*>(jcollider);
    collider->set_radius(radius);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeSphereCollider_getRadius(JNIEnv * env,
        jobject obj, jlong jcollider)
{
    SphereCollider* collider = reinterpret_cast<SphereCollider*>(jcollider);
    return collider->get_radius();
}

}
