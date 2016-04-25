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

#include "perspective_camera.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePerspectiveCamera_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getDefaultAspectRatio(
        JNIEnv * env, jobject obj);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setDefaultAspectRatio(
        JNIEnv * env, jobject obj, jfloat aspect_ratio);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getDefaultFovY(
        JNIEnv * env, jobject obj);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setDefaultFovY(
        JNIEnv * env, jobject obj, jfloat fov_y);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getNearClippingDistance(
        JNIEnv * env, jobject obj, jlong jperspective_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setNearClippingDistance(
        JNIEnv * env, jobject obj, jlong jperspective_camera, jfloat near);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getFarClippingDistance(
        JNIEnv * env, jobject obj, jlong jperspective_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setFarClippingDistance(
        JNIEnv * env, jobject obj, jlong jperspective_camera, jfloat far);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getFovY(JNIEnv * env,
        jobject obj, jlong jperspective_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setFovY(JNIEnv * env,
        jobject obj, jlong jperspective_camera, jfloat fov_y);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getAspectRatio(
        JNIEnv * env, jobject obj, jlong jperspective_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setAspectRatio(
        JNIEnv * env, jobject obj, jlong jperspective_camera,
        jfloat aspect_ratio);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePerspectiveCamera_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new PerspectiveCamera());
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getDefaultFovY(
        JNIEnv * env, jobject obj) {
    return PerspectiveCamera::default_fov_y();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setDefaultFovY(
        JNIEnv * env, jobject obj, jfloat fov_y) {
    PerspectiveCamera::set_default_fov_y(fov_y);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getDefaultAspectRatio(
        JNIEnv * env, jobject obj) {
    return PerspectiveCamera::default_aspect_ratio();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setDefaultAspectRatio(
        JNIEnv * env, jobject obj, jfloat aspect_ratio) {
    PerspectiveCamera::set_default_aspect_ratio(aspect_ratio);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getNearClippingDistance(
        JNIEnv * env, jobject obj, jlong jperspective_camera) {
    PerspectiveCamera* perspective_camera =
            reinterpret_cast<PerspectiveCamera*>(jperspective_camera);
    return perspective_camera->near_clipping_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setNearClippingDistance(
        JNIEnv * env, jobject obj, jlong jperspective_camera, jfloat near) {
    PerspectiveCamera* perspective_camera =
            reinterpret_cast<PerspectiveCamera*>(jperspective_camera);
    perspective_camera->set_near_clipping_distance(near);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getFarClippingDistance(
        JNIEnv * env, jobject obj, jlong jperspective_camera) {
    PerspectiveCamera* perspective_camera =
            reinterpret_cast<PerspectiveCamera*>(jperspective_camera);
    return perspective_camera->far_clipping_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setFarClippingDistance(
        JNIEnv * env, jobject obj, jlong jperspective_camera, jfloat far) {
    PerspectiveCamera* perspective_camera =
            reinterpret_cast<PerspectiveCamera*>(jperspective_camera);
    perspective_camera->set_far_clipping_distance(far);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getFovY(JNIEnv * env,
        jobject obj, jlong jperspective_camera) {
    PerspectiveCamera* perspective_camera =
            reinterpret_cast<PerspectiveCamera*>(jperspective_camera);
    return perspective_camera->fov_y();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setFovY(JNIEnv * env,
        jobject obj, jlong jperspective_camera, jfloat fov_y) {
    PerspectiveCamera* perspective_camera =
            reinterpret_cast<PerspectiveCamera*>(jperspective_camera);
    perspective_camera->set_fov_y(fov_y);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePerspectiveCamera_getAspectRatio(
        JNIEnv * env, jobject obj, jlong jperspective_camera) {
    PerspectiveCamera* perspective_camera =
            reinterpret_cast<PerspectiveCamera*>(jperspective_camera);
    return perspective_camera->aspect_ratio();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePerspectiveCamera_setAspectRatio(
        JNIEnv * env, jobject obj, jlong jperspective_camera,
        jfloat aspect_ratio) {
    PerspectiveCamera* perspective_camera =
            reinterpret_cast<PerspectiveCamera*>(jperspective_camera);
    perspective_camera->set_aspect_ratio(aspect_ratio);
}
}
