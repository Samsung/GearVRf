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


/*
 * k_sensor_jni.cpp
 *
 *  Created on: 2014. 8. 4.
 */

#include "k_sensor.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeKSensor_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeKSensor_update(JNIEnv * env,
        jobject obj, jlong jk_sensor);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeKSensor_getTimeStamp(
        JNIEnv * env, jobject obj, jlong jk_sensor);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeKSensor_getData(JNIEnv * env,
        jobject obj, jlong jk_sensor);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeKSensor_close(JNIEnv * env,
        jobject obj, jlong jk_sensor);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeKSensor_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new KSensor());
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeKSensor_update(JNIEnv * env,
        jobject obj, jlong jk_sensor) {
    KSensor* k_sensor = reinterpret_cast<KSensor*>(jk_sensor);
    return k_sensor->update();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeKSensor_getTimeStamp(
        JNIEnv * env, jobject obj, jlong jk_sensor) {
    KSensor* k_sensor = reinterpret_cast<KSensor*>(jk_sensor);
    return k_sensor->getLatestTime();
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeKSensor_getData(JNIEnv * env,
        jobject obj, jlong jk_sensor) {
    KSensor* k_sensor = reinterpret_cast<KSensor*>(jk_sensor);
    Quaternion rotation = k_sensor->getSensorQuaternion();
    vec3 angular_velocity = k_sensor->getAngularVelocity();

    jfloatArray jdata = env->NewFloatArray(7);
    jfloat data[7];
    data[0] = rotation.w;
    data[1] = rotation.x;
    data[2] = rotation.y;
    data[3] = rotation.z;
    data[4] = angular_velocity.x;
    data[5] = angular_velocity.y;
    data[6] = angular_velocity.z;
    env->SetFloatArrayRegion(jdata, 0, 7, data);

    return jdata;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeKSensor_close(JNIEnv * env,
        jobject obj, jlong jk_sensor) {
    KSensor* k_sensor = reinterpret_cast<KSensor*>(jk_sensor);
    k_sensor->closeSensor();
}
}
