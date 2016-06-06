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

#include "transform.h"

#include "util/gvr_jni.h"
#include "util/gvr_log.h"
#include "glm/gtc/type_ptr.hpp"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeTransform_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeTransform_getComponentType(JNIEnv * env, jobject obj);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getPositionX(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getPositionY(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getPositionZ(JNIEnv * env,
        jobject obj, jlong jtransform);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setPosition(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x, jfloat y, jfloat z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setPositionX(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setPositionY(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat y);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setPositionZ(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat z);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationW(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationX(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationY(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationZ(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationYaw(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationPitch(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationRoll(JNIEnv * env,
        jobject obj, jlong jtransform);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setRotation(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat w, jfloat x, jfloat y, jfloat z);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getScaleX(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getScaleY(JNIEnv * env,
        jobject obj, jlong jtransform);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getScaleZ(JNIEnv * env,
        jobject obj, jlong jtransform);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setScale(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x, jfloat y, jfloat z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setScaleX(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setScaleY(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat y);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setScaleZ(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat z);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeTransform_getModelMatrix(JNIEnv * env,
        jobject obj, jlong jtransform);

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeTransform_getLocalModelMatrix(JNIEnv * env,
        jobject obj, jlong jtransform);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setModelMatrix(JNIEnv * env,
        jobject obj, jlong jtransform, jfloatArray mat);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_translate(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x, jfloat y, jfloat z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setRotationByAxis(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat angle, jfloat x, jfloat y,
        jfloat z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_rotate(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat w, jfloat x, jfloat y, jfloat z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_rotateByAxis(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat angle, jfloat x, jfloat y,
        jfloat z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_rotateByAxisWithPivot(
        JNIEnv * env, jobject obj, jlong jtransform, jfloat angle,
        jfloat axis_x, jfloat axis_y, jfloat axis_z, jfloat pivot_x,
        jfloat pivot_y, jfloat pivot_z);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_rotateWithPivot(
        JNIEnv * env, jobject obj, jlong jtransform, jfloat quat_w,
        jfloat quat_x, jfloat quat_y, jfloat quat_z, jfloat pivot_x,
        jfloat pivot_y, jfloat pivot_z);

}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeTransform_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new Transform());
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeTransform_getComponentType(JNIEnv * env, jobject obj) {
    return Transform::getComponentType();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getPositionX(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->position_x();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getPositionY(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->position_y();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getPositionZ(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->position_z();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setPosition(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x, jfloat y, jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_position(x, y, z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setPositionX(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_position_x(x);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setPositionY(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat y) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_position_y(y);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setPositionZ(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_position_z(z);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationW(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->rotation_w();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationX(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->rotation_x();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationY(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->rotation_y();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationZ(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->rotation_z();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationYaw(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->rotation_yaw();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationPitch(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->rotation_pitch();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getRotationRoll(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->rotation_roll();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setRotation(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat w, jfloat x, jfloat y, jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_rotation(w, x, y, z);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getScaleX(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->scale_x();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getScaleY(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->scale_y();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeTransform_getScaleZ(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    return transform->scale_z();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setScale(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x, jfloat y, jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_scale(x, y, z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setScaleX(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_scale_x(x);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setScaleY(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat y) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_scale_y(y);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setScaleZ(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->set_scale_z(z);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeTransform_getModelMatrix(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    glm::mat4 matrix = transform->getModelMatrix();
    jsize size = sizeof(matrix) / sizeof(jfloat);
    if (size != 16) {
        LOGE("sizeof(matrix) / sizeof(jfloat) != 16");
        throw "sizeof(matrix) / sizeof(jfloat) != 16";
    }
    jfloatArray jmatrix = env->NewFloatArray(size);
    env->SetFloatArrayRegion(jmatrix, 0, size, glm::value_ptr(matrix));
    return jmatrix;
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeTransform_getLocalModelMatrix(JNIEnv * env,
        jobject obj, jlong jtransform) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    glm::mat4 matrix = transform->getLocalModelMatrix();
    jsize size = sizeof(matrix) / sizeof(jfloat);
    if (size != 16) {
        LOGE("sizeof(matrix) / sizeof(jfloat) != 16");
        throw "sizeof(matrix) / sizeof(jfloat) != 16";
    }
    jfloatArray jmatrix = env->NewFloatArray(size);
    env->SetFloatArrayRegion(jmatrix, 0, size, glm::value_ptr(matrix));
    return jmatrix;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setModelMatrix(JNIEnv * env,
		jobject obj, jlong jtransform, jfloatArray mat){
	Transform* transform = reinterpret_cast<Transform*>(jtransform);
	jfloat* mat_arr = env->GetFloatArrayElements(mat, 0);
	glm::mat4 matrix = glm::make_mat4x4(mat_arr);
	transform->setModelMatrix(matrix);
	env->ReleaseFloatArrayElements(mat, mat_arr, 0);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_translate(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat x, jfloat y, jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->translate(x, y, z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_setRotationByAxis(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat angle, jfloat x, jfloat y,
        jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->setRotationByAxis(angle, x, y, z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_rotate(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat w, jfloat x, jfloat y, jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->rotate(w, x, y, z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_rotateByAxis(JNIEnv * env,
        jobject obj, jlong jtransform, jfloat angle, jfloat x, jfloat y,
        jfloat z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->rotateByAxis(angle, x, y, z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_rotateByAxisWithPivot(
        JNIEnv * env, jobject obj, jlong jtransform, jfloat angle,
        jfloat axis_x, jfloat axis_y, jfloat axis_z, jfloat pivot_x,
        jfloat pivot_y, jfloat pivot_z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->rotateByAxisWithPivot(angle, axis_x, axis_y, axis_z, pivot_x,
            pivot_y, pivot_z);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeTransform_rotateWithPivot(
        JNIEnv * env, jobject obj, jlong jtransform, jfloat quat_w,
        jfloat quat_x, jfloat quat_y, jfloat quat_z, jfloat pivot_x,
        jfloat pivot_y, jfloat pivot_z) {
    Transform* transform = reinterpret_cast<Transform*>(jtransform);
    transform->rotateWithPivot(quat_w, quat_x, quat_y, quat_z, pivot_x,
            pivot_y, pivot_z);
}


}
