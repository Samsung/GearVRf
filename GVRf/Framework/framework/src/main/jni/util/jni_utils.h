/* Copyright 2016 Samsung Electronics Co., LTD
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

#ifndef JNI_UTILS_H
#define JNI_UTILS_H

#include "util/gvr_log.h"
#include <jni.h>

namespace gvr {

static jmethodID GetStaticMethodID(JNIEnv& env, jclass clazz, const char * name,
        const char * signature) {
    jmethodID mid = env.GetStaticMethodID(clazz, name, signature);
    if (!mid) {
        FAIL("unable to find static method %s", name);
    }
    return mid;
}

static jmethodID GetMethodId(JNIEnv& env, const jclass clazz, const char* name, const char* signature) {
    const jmethodID mid = env.GetMethodID(clazz, name, signature);
    if (nullptr == mid) {
        FAIL("unable to find method %s", name);
    }
    return mid;
}

/**
 * @return global reference; caller must delete the global reference
 */
static jclass GetGlobalClassReference(JNIEnv& env, const char * className) {
    jclass lc = env.FindClass(className);
    if (0 == lc) {
        FAIL("unable to find class %s", className);
    }
    // Turn it into a global ref, so we can safely use it in the VR thread
    jclass gc = static_cast<jclass>(env.NewGlobalRef(lc));
    env.DeleteLocalRef(lc);

    return gc;
}

/**
 * Assuming this is called only by threads that are already attached to jni; it is the
 * responsibility of the caller to ensure that.
 */
static JNIEnv* getCurrentEnv(JavaVM* javaVm) {
    JNIEnv* result;
    if (JNI_OK != javaVm->GetEnv(reinterpret_cast<void**>(&result), JNI_VERSION_1_6)) {
        FAIL("GetEnv failed");
    }
    return result;
}

}

#endif
