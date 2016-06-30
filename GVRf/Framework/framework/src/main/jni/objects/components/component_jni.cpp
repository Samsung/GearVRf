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


#include "component.h"
#include "util/gvr_jni.h"
#include "util/gvr_log.h"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeComponent_getType(JNIEnv * env,
            jobject obj, jlong jcomponent);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeComponent_setOwnerObject(JNIEnv * env,
            jobject obj, jlong jcomponent, jlong jowner);

    JNIEXPORT jboolean JNICALL
    Java_org_gearvrf_NativeComponent_isEnabled(JNIEnv * env, jobject obj, jlong jcomponent);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeComponent_setEnable(JNIEnv * env, jobject obj, jlong jlight, jboolean flag);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeComponent_getType(JNIEnv * env,
        jobject obj, jlong jcomponent)
{
    Component* component = reinterpret_cast<Component*>(jcomponent);
    long long type = component->getType();
    return type;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeComponent_setOwnerObject(JNIEnv * env,
        jobject obj, jlong jcomponent, jlong jowner)
{
    Component* component = reinterpret_cast<Component*>(jcomponent);
    SceneObject* owner = reinterpret_cast<SceneObject*>(jowner);
    component->set_owner_object(owner);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeComponent_isEnabled(JNIEnv * env, jobject obj, jlong jcomponent)
{
    Component* component = reinterpret_cast<Component*>(jcomponent);
    return component->enabled();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeComponent_setEnable(JNIEnv * env, jobject obj, jlong jcomponent, jboolean flag)
{
    Component* component = reinterpret_cast<Component*>(jcomponent);
    component->set_enable((bool) flag);
}

}

