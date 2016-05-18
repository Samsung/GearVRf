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

#include "camera.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCamera_getComponentType(JNIEnv * env, jobject obj);

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCamera_getBackgroundColorR(JNIEnv * env,
        jobject obj, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setBackgroundColorR(JNIEnv * env,
        jobject obj, jlong jcamera, jfloat r);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCamera_getBackgroundColorG(JNIEnv * env,
        jobject obj, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setBackgroundColorG(JNIEnv * env,
        jobject obj, jlong jcamera, jfloat g);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCamera_getBackgroundColorB(JNIEnv * env,
        jobject obj, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setBackgroundColorB(JNIEnv * env,
        jobject obj, jlong jcamera, jfloat b);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCamera_getBackgroundColorA(JNIEnv * env,
        jobject obj, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setBackgroundColorA(JNIEnv * env,
        jobject obj, jlong jcamera, jfloat a);
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeCamera_getRenderMask(JNIEnv * env,
        jobject obj, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setRenderMask(JNIEnv * env,
        jobject obj, jlong jcamera, jint render_mask);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_addPostEffect(JNIEnv * env,
        jobject obj, jlong jcamera, jlong jpost_effect_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_removePostEffect(JNIEnv * env,
        jobject obj, jlong jcamera, jlong jpost_effect_data);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCamera_getComponentType(JNIEnv * env, jobject obj) {
    return Camera::getComponentType();
}


JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCamera_getBackgroundColorR(JNIEnv * env,
        jobject obj, jlong jcamera) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    return camera->background_color_r();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setBackgroundColorR(JNIEnv * env,
        jobject obj, jlong jcamera, jfloat r) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    camera->set_background_color_r(r);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCamera_getBackgroundColorG(JNIEnv * env,
        jobject obj, jlong jcamera) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    return camera->background_color_g();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setBackgroundColorG(JNIEnv * env,
        jobject obj, jlong jcamera, jfloat g) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    camera->set_background_color_g(g);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCamera_getBackgroundColorB(JNIEnv * env,
        jobject obj, jlong jcamera) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    return camera->background_color_b();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setBackgroundColorB(JNIEnv * env,
        jobject obj, jlong jcamera, jfloat b) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    camera->set_background_color_b(b);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeCamera_getBackgroundColorA(JNIEnv * env,
        jobject obj, jlong jcamera) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    return camera->background_color_a();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setBackgroundColorA(JNIEnv * env,
        jobject obj, jlong jcamera, jfloat a) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    camera->set_background_color_a(a);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeCamera_getRenderMask(JNIEnv * env,
        jobject obj, jlong jcamera) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    return camera->render_mask();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_setRenderMask(JNIEnv * env,
        jobject obj, jlong jcamera, jint render_mask) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    camera->set_render_mask(render_mask);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_addPostEffect(JNIEnv * env,
        jobject obj, jlong jcamera, jlong jpost_effect_data) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    camera->addPostEffect(post_effect_data);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCamera_removePostEffect(JNIEnv * env,
        jobject obj, jlong jcamera, jlong jpost_effect_data) {
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    camera->removePostEffect(post_effect_data);
}
}
