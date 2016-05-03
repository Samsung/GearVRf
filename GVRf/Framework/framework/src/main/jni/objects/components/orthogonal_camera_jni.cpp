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

#include "orthogonal_camera.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getLeftClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setLeftClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat left);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getRightClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setRightClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat right);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getBottomClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setBottomClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat bottom);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getTopClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setTopClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat top);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getNearClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setNearClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat near);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getFarClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setFarClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat far);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new OrthogonalCamera());
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getLeftClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    return orthogonal_camera->left_clipping_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setLeftClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat left) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    orthogonal_camera->set_near_clipping_distance(left);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getRightClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    return orthogonal_camera->right_clipping_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setRightClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat right) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    orthogonal_camera->set_right_clipping_distance(right);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getBottomClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    return orthogonal_camera->bottom_clipping_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setBottomClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat bottom) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    orthogonal_camera->set_bottom_clipping_distance(bottom);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getTopClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    return orthogonal_camera->top_clipping_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setTopClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat top) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    orthogonal_camera->set_top_clipping_distance(top);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getNearClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    return orthogonal_camera->near_clipping_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setNearClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat near) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    orthogonal_camera->set_near_clipping_distance(near);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_getFarClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    return orthogonal_camera->far_clipping_distance();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeOrthogonalCamera_setFarClippingDistance(
        JNIEnv * env, jobject obj, jlong jorthogonal_camera, jfloat far) {
    OrthogonalCamera* orthogonal_camera =
            reinterpret_cast<OrthogonalCamera*>(jorthogonal_camera);
    orthogonal_camera->set_far_clipping_distance(far);
}
}
