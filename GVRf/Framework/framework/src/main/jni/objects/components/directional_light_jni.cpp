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

#include "directional_light.h"

#include "util/gvr_jni.h"

namespace gvr {

extern "C" {
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightDirection(JNIEnv * env,
        jobject obj, jlong ref, jfloat jx, jfloat jy, jfloat jz);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightPosition(JNIEnv * env,
        jobject obj, jlong ref, jfloat jx, jfloat jy, jfloat jz);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setSpotangle(JNIEnv * env, jobject obj,
        jlong ref, jfloat angle);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowSmoothSize(JNIEnv * env,
        jobject obj, jlong ref, jfloat size);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightAmbientOnShadow(JNIEnv * env,
        jobject obj, jlong ref, jfloat value);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowGradientCenter(JNIEnv * env,
        jobject obj, jlong ref, jfloat value);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightingShade(JNIEnv * env,
        jobject obj, jlong ref, jfloat value);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setBoardStratifiedSampling(JNIEnv * env,
        jobject obj, jlong ref, jfloat value);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowSmoothDistance(JNIEnv * env,
        jobject obj, jlong ref, jfloat value);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setBias(JNIEnv * env, jobject obj,
        jlong ref, jfloat value);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowMapEdgesLength(JNIEnv * env,
        jobject obj, jlong ref, jfloat value);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightRenderMode(JNIEnv * env,
        jobject obj, jlong ref, jint mode);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowMapHandlerMode(JNIEnv * env,
        jobject obj, jlong ref, jint mode);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowMapHandlerEdges(JNIEnv * env,
        jobject obj, jlong ref, jint mode);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeDirectionalLight_getNewDirectionalLight(JNIEnv * env,
        jobject obj);

}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightDirection(JNIEnv * env,
        jobject obj, jlong ref, jfloat jx, jfloat jy, jfloat jz) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setLightDirection(glm::vec3(jx, jy, jz));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightPosition(JNIEnv * env,
        jobject obj, jlong ref, jfloat jx, jfloat jy, jfloat jz) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    //LOGI(" Render Camera DirectionalLight x %f y %f z %f ", jx, jy, jz);
    directionalLight->setLightPosition(glm::vec3(jx, jy, jz));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setSpotangle(JNIEnv * env, jobject obj,
        jlong ref, jfloat angle) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setSpotangle(angle);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowSmoothSize(JNIEnv * env,
        jobject obj, jlong ref, jfloat size) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setShadowSmoothSize(size);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightAmbientOnShadow(JNIEnv * env,
        jobject obj, jlong ref, jfloat value) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setLightAmbientOnShadow(value);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowGradientCenter(JNIEnv * env,
        jobject obj, jlong ref, jfloat value) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setShadowGradientCenter(value);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightingShade(JNIEnv * env,
        jobject obj, jlong ref, jfloat value) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setLightingShade(value);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setBoardStratifiedSampling(JNIEnv * env,
        jobject obj, jlong ref, jfloat value) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setBoardStratifiedSampling(value);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowSmoothDistance(JNIEnv * env,
        jobject obj, jlong ref, jfloat value) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setShadowSmoothDistance(value);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setBias(JNIEnv * env, jobject obj,
        jlong ref, jfloat value) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setBias(value);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowMapEdgesLength(JNIEnv * env,
        jobject obj, jlong ref, jfloat value) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    directionalLight->setShadowMapEdgesLength(value);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setLightRenderMode(JNIEnv * env,
        jobject obj, jlong ref, jint mode) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    if (mode == 0) {
        directionalLight->setRenderMode(DirectionalLight::PERSPECTIVE);
    } else {
        directionalLight->setRenderMode(DirectionalLight::ORTOGONAL);
    }
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowMapHandlerEdges(JNIEnv * env,
        jobject obj, jlong ref, jint mode) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    switch (mode) {
    case 0:
        directionalLight->setShadowMapHandlerEdges(
                DirectionalLight::HANDLER_EDGES_NONE);
        break;
    case 1:
        directionalLight->setShadowMapHandlerEdges(
                DirectionalLight::HANDLER_EDGES_DARK);
        break;
    case 2:
        directionalLight->setShadowMapHandlerEdges(
                DirectionalLight::HANDLER_EDGES_LIGHT);
        break;
    }
}
;

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeDirectionalLight_setShadowMapHandlerMode(JNIEnv * env,
        jobject obj, jlong ref, jint mode) {

    DirectionalLight* directionalLight =
            reinterpret_cast<DirectionalLight*>(ref);
    switch (mode) {
    case 0:
        directionalLight->setShadowMapHandlerMode(DirectionalLight::HIDE);
        break;
    case 1:
        directionalLight->setShadowMapHandlerMode(DirectionalLight::SHOW);
        break;
    case 2:
        directionalLight->setShadowMapHandlerMode(DirectionalLight::GRADIENT);
        break;
    }
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeDirectionalLight_getNewDirectionalLight(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new DirectionalLight());
}

}
