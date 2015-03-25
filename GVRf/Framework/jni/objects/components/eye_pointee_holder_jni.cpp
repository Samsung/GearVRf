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

#include "eye_pointee_holder.h"

#include "util/gvr_jni.h"
#include "glm/gtc/type_ptr.hpp"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_getEnable(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_setEnable(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder, jboolean enable);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_getHit(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_addPointee(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder, jlong jeye_pointee);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_removePointee(
        JNIEnv * env, jobject obj, jlong jeye_pointee_holder,
        jlong jeye_pointee);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_isPointed(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new std::shared_ptr<EyePointeeHolder>(
            new EyePointeeHolder()));
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_getEnable(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder) {
    std::shared_ptr<EyePointeeHolder> eye_pointee_holder =
            *reinterpret_cast<std::shared_ptr<EyePointeeHolder>*>(jeye_pointee_holder);
    return static_cast<jboolean>(eye_pointee_holder->enable());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_setEnable(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder, jboolean enable) {
    std::shared_ptr<EyePointeeHolder> eye_pointee_holder =
            *reinterpret_cast<std::shared_ptr<EyePointeeHolder>*>(jeye_pointee_holder);
    eye_pointee_holder->set_enable(static_cast<jboolean>(enable));
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_getHit(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder) {
    std::shared_ptr<EyePointeeHolder> eye_pointee_holder =
            *reinterpret_cast<std::shared_ptr<EyePointeeHolder>*>(jeye_pointee_holder);
    glm::vec3 hit = eye_pointee_holder->hit();
    jsize size = sizeof(hit) / sizeof(jfloat);
    if (size != 3) {
        LOGE("sizeof(hit) / sizeof(jfloat) != 3");
        throw "sizeof(hit) / sizeof(jfloat) != 3";
    }
    jfloatArray jhit = env->NewFloatArray(size);
    env->SetFloatArrayRegion(jhit, 0, size, glm::value_ptr(hit));
    return jhit;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_addPointee(JNIEnv * env,
        jobject obj, jlong jeye_pointee_holder, jlong jeye_pointee) {
    std::shared_ptr<EyePointeeHolder> eye_pointee_holder =
            *reinterpret_cast<std::shared_ptr<EyePointeeHolder>*>(jeye_pointee_holder);
    std::shared_ptr<EyePointee> eye_pointee = *reinterpret_cast<std::shared_ptr<
            EyePointee>*>(jeye_pointee);
    eye_pointee_holder->addPointee(eye_pointee);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeEyePointeeHolder_removePointee(
        JNIEnv * env, jobject obj, jlong jeye_pointee_holder,
        jlong jeye_pointee) {
    std::shared_ptr<EyePointeeHolder> eye_pointee_holder =
            *reinterpret_cast<std::shared_ptr<EyePointeeHolder>*>(jeye_pointee_holder);
    std::shared_ptr<EyePointee> eye_pointee = *reinterpret_cast<std::shared_ptr<
            EyePointee>*>(jeye_pointee);
    eye_pointee_holder->removePointee(eye_pointee);
}

}
