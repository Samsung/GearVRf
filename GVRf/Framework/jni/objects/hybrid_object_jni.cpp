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


#include "hybrid_object.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeHybridObject_delete(JNIEnv * env,
        jobject obj, jlong jhybrid_object);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeHybridObject_equals(JNIEnv * env,
        jobject obj, jlong jhybrid_object, jlong jother);
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeHybridObject_getUseCount(JNIEnv * env,
        jobject obj, jlong jhybrid_object);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeHybridObject_getNativePointer(
        JNIEnv * env, jobject obj, jlong jhybrid_object);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeHybridObject_delete(JNIEnv * env,
        jobject obj, jlong jhybrid_object) {
    delete reinterpret_cast<std::shared_ptr<HybridObject>*>(jhybrid_object);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeHybridObject_equals(JNIEnv * env,
        jobject obj, jlong jhybrid_object, jlong jother) {
    std::shared_ptr<HybridObject> hybrid_object =
            *reinterpret_cast<std::shared_ptr<HybridObject>*>(jhybrid_object);
    std::shared_ptr<HybridObject> other = *reinterpret_cast<std::shared_ptr<
            HybridObject>*>(jother);
    return static_cast<jboolean>(hybrid_object.get() == other.get());
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeHybridObject_getUseCount(JNIEnv * env,
        jobject obj, jlong jhybrid_object) {
    std::weak_ptr<HybridObject> hybrid_object(
            *reinterpret_cast<std::shared_ptr<HybridObject>*>(jhybrid_object));
    return hybrid_object.use_count();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeHybridObject_getNativePointer(
        JNIEnv* env, jobject obj, jlong jhybrid_object) {
    std::shared_ptr<HybridObject> hybrid_object =
            *reinterpret_cast<std::shared_ptr<HybridObject>*>(jhybrid_object);
    return reinterpret_cast<jlong>(hybrid_object.get());
}
}

