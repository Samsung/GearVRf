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

#include "custom_camera.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCustomCamera_ctor(JNIEnv * env,
        jobject obj);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomCamera_setProjectionMatrix(
        JNIEnv * env, jobject obj, jlong jcustom_camera, jfloat x1, jfloat y1,
        jfloat z1, jfloat w1, jfloat x2, jfloat y2, jfloat z2, jfloat w2,
        jfloat x3, jfloat y3, jfloat z3, jfloat w3, jfloat x4, jfloat y4,
        jfloat z4, jfloat w4);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCustomCamera_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new CustomCamera());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomCamera_setProjectionMatrix(
        JNIEnv * env, jobject obj, jlong jcustom_camera, jfloat x1, jfloat y1,
        jfloat z1, jfloat w1, jfloat x2, jfloat y2, jfloat z2, jfloat w2,
        jfloat x3, jfloat y3, jfloat z3, jfloat w3, jfloat x4, jfloat y4,
        jfloat z4, jfloat w4) {
    CustomCamera* custom_camera = reinterpret_cast<CustomCamera*>(jcustom_camera);
    glm::mat4 mat(x1, y1, z1, w1, x2, y2, z2, w2, x3, y3, z3, w3, x4, y4, z4,
            w4);
    custom_camera->set_projection_matrix(mat);
}
}
