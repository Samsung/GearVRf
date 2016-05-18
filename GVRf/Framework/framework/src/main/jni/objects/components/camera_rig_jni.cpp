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

#include "camera_rig.h"

#include "glm/gtc/type_ptr.hpp"

#include "util/gvr_jni.h"

#include "util/gvr_java_stack_trace.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCameraRig_ctor(JNIEnv * env,
        jobject obj);

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCameraRig_getComponentType(JNIEnv * env, jobject obj);

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeCameraRig_getCameraRigType(JNIEnv * env,
jobject obj);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setCameraRigType(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jint camera_rig_type);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCameraRig_getDefaultCameraSeparationDistance(
        JNIEnv * env, jobject obj);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setDefaultCameraSeparationDistance(
        JNIEnv * env, jobject obj, jfloat distance);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCameraRig_getCameraSeparationDistance(
        JNIEnv * env, jobject obj, jlong jcamera_rig);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setCameraSeparationDistance(
        JNIEnv * env, jobject obj, jlong jcamera_rig, jfloat distance);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCameraRig_getFloat(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setFloat(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key, jfloat value);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeCameraRig_getVec2(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setVec2(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key, jfloat x, jfloat y);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeCameraRig_getVec3(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setVec3(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key, jfloat x, jfloat y,
        jfloat z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setVec4(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key, jfloat x, jfloat y,
        jfloat z, jfloat w);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeCameraRig_getVec4(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_attachLeftCamera(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_attachRightCamera(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_attachCenterCamera(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_reset(JNIEnv * env,
        jobject obj, jlong jcamera_rig);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_resetYaw(JNIEnv * env,
        jobject obj, jlong jcamera_rig);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_resetYawPitch(JNIEnv * env,
        jobject obj, jlong jcamera_rig);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setRotationSensorData(
        JNIEnv * env, jobject obj, jlong jcamera_rig, jlong time_stamp,
        jfloat w, jfloat x, jfloat y, jfloat z, jfloat gyro_x, jfloat gyro_y,
        jfloat gyro_z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_predict(JNIEnv * env, jobject obj, jlong jcamera_rig, jfloat time);

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeCameraRig_getLookAt(JNIEnv * env,
        jobject obj, jlong jcamera_rig);

}; // extern "C"

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCameraRig_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new CameraRig());
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCameraRig_getComponentType(JNIEnv * env, jobject obj) {
    return CameraRig::getComponentType();
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeCameraRig_getCameraRigType(JNIEnv * env,
        jobject obj, jlong jcamera_rig) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    return static_cast<jint>(camera_rig->camera_rig_type());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setCameraRigType(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jint camera_rig_type) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    camera_rig->set_camera_rig_type(
            static_cast<CameraRig::CameraRigType>(camera_rig_type));
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCameraRig_getDefaultCameraSeparationDistance(
        JNIEnv * env, jobject obj) {
    return CameraRig::default_camera_separation_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setDefaultCameraSeparationDistance(
        JNIEnv * env, jobject obj, jfloat distance) {
    CameraRig::set_default_camera_separation_distance(distance);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCameraRig_getCameraSeparationDistance(
        JNIEnv * env, jobject obj, jlong jcamera_rig) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    return camera_rig->camera_separation_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setCameraSeparationDistance(
        JNIEnv * env, jobject obj, jlong jcamera_rig, jfloat distance) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    camera_rig->set_camera_separation_distance(distance);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCameraRig_getFloat(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    jfloat camera_rig_float = camera_rig->getFloat(native_key);
    env->ReleaseStringUTFChars(key, char_key);
    return camera_rig_float;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setFloat(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key, jfloat value) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    camera_rig->setFloat(native_key, value);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeCameraRig_getVec2(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    try {
        glm::vec2 camera_rig_vec2 = camera_rig->getVec2(native_key);
        jfloatArray jvec2 = env->NewFloatArray(2);
        env->SetFloatArrayRegion(jvec2, 0, 2,
                reinterpret_cast<jfloat*>(&camera_rig_vec2));
        env->ReleaseStringUTFChars(key, char_key);
        return jvec2;
    } catch (const std::string & err) {
        env->ReleaseStringUTFChars(key, char_key);
        printJavaCallStack(env, err);
        throw err;
    }
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setVec2(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key, jfloat x, jfloat y) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    camera_rig->setVec2(native_key, glm::vec2(x, y));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeCameraRig_getVec3(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    try {
        glm::vec3 camera_rig_vec3 = camera_rig->getVec3(native_key);
        jfloatArray jvec3 = env->NewFloatArray(3);
        env->SetFloatArrayRegion(jvec3, 0, 3,
                reinterpret_cast<jfloat*>(&camera_rig_vec3));
        env->ReleaseStringUTFChars(key, char_key);
        return jvec3;
    } catch (const std::string & err) {
        env->ReleaseStringUTFChars(key, char_key);
        printJavaCallStack(env, err);
        throw err;
    }
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setVec3(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key, jfloat x, jfloat y,
        jfloat z) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    camera_rig->setVec3(native_key, glm::vec3(x, y, z));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeCameraRig_getVec4(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    try {
        glm::vec4 camera_rig_vec4 = camera_rig->getVec4(native_key);
        jfloatArray jvec4 = env->NewFloatArray(4);
        env->SetFloatArrayRegion(jvec4, 0, 4,
                reinterpret_cast<jfloat*>(&camera_rig_vec4));
        env->ReleaseStringUTFChars(key, char_key);
        return jvec4;
    } catch (const std::string & err) {
        env->ReleaseStringUTFChars(key, char_key);
        printJavaCallStack(env, err);
        throw err;
    }
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setVec4(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jstring key, jfloat x, jfloat y,
        jfloat z, jfloat w) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    camera_rig->setVec4(native_key, glm::vec4(x, y, z, w));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_attachLeftCamera(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jlong jcamera) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    camera_rig->attachLeftCamera(camera);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_attachRightCamera(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jlong jcamera) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    camera_rig->attachRightCamera(camera);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_attachCenterCamera(JNIEnv * env,
        jobject obj, jlong jcamera_rig, jlong jcamera) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    PerspectiveCamera* camera = reinterpret_cast<PerspectiveCamera*>(jcamera);
    camera_rig->attachCenterCamera(camera);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_reset(JNIEnv * env,
        jobject obj, jlong jcamera_rig) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    camera_rig->reset();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_resetYaw(JNIEnv * env,
        jobject obj, jlong jcamera_rig) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    camera_rig->resetYaw();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_resetYawPitch(JNIEnv * env,
        jobject obj, jlong jcamera_rig) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    camera_rig->resetYawPitch();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_setRotationSensorData(
        JNIEnv * env, jobject obj, jlong jcamera_rig, jlong time_stamp,
        jfloat w, jfloat x, jfloat y, jfloat z, jfloat gyro_x, jfloat gyro_y,
        jfloat gyro_z) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    camera_rig->setRotationSensorData(time_stamp, w, x, y, z, gyro_x, gyro_y,
            gyro_z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCameraRig_predict(JNIEnv * env, jobject obj, jlong jcamera_rig, jfloat time) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    camera_rig->predict(time);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeCameraRig_getLookAt(JNIEnv * env,
        jobject obj, jlong jcamera_rig) {
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    glm::vec3 look_at_vector = camera_rig->getLookAt();
    jsize size = sizeof(glm::vec3) / sizeof(jfloat);
    if (size != 3) {
        LOGE("sizeof(glm::vec3) / sizeof(jfloat) != 3");
        throw "sizeof(glm::vec3) / sizeof(jfloat) != 3";
    }
    jfloatArray look_at_array = env->NewFloatArray(size);
    env->SetFloatArrayRegion(look_at_array, 0, size,
            glm::value_ptr(look_at_vector));
    return look_at_array;
}

}
