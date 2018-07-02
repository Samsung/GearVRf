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

#include <jni.h>

#include "daydream_renderer.h"

#define JNI_METHOD(return_type, method_name) \
  JNIEXPORT return_type JNICALL              \
      Java_org_gearvrf_DaydreamRenderer_##method_name


namespace {

inline jlong jptr(DaydreamRenderer *native_daydream_renderer) {
  return reinterpret_cast<intptr_t>(native_daydream_renderer);
}

inline DaydreamRenderer *native(jlong ptr) {
  return reinterpret_cast<DaydreamRenderer *>(ptr);
}
}  // anonymous namespace

extern "C" {
JNIEXPORT void JNICALL Java_org_gearvrf_DayDreamControllerReader_setNativeBuffer(JNIEnv * env, jclass clazz, jlong nativeRenderer,
                                                                                jobject jreadback_buffer) {
  float *data = (float *) env->GetDirectBufferAddress(jreadback_buffer);
 DaydreamRenderer* daydreamRenderer = reinterpret_cast<DaydreamRenderer*>(nativeRenderer);
 daydreamRenderer->setFloatBuffer(data);
}
JNIEXPORT void JNICALL Java_org_gearvrf_DayDreamControllerReader_updateHandedness(JNIEnv * env, jclass clazz, jlong nativeRenderer) {
      DaydreamRenderer* daydreamRenderer = reinterpret_cast<DaydreamRenderer*>(nativeRenderer);
      daydreamRenderer->updateHandedness();
}
JNI_METHOD(jlong, nativeCreateRenderer)(JNIEnv *env, jclass clazz,
                                        jlong native_gvr_api) {
  return jptr(
      new DaydreamRenderer(*env, clazz, reinterpret_cast<gvr_context *>(native_gvr_api)));
}

JNI_METHOD(void, nativeDestroyRenderer)
(JNIEnv *env, jclass clazz, jlong native_daydream_renderer) {
  native(native_daydream_renderer)->OnDestroy(*env);
  delete native(native_daydream_renderer);
}

JNI_METHOD(void, nativeInitializeGl)(JNIEnv *env, jobject obj,
                                     jlong native_daydream_renderer) {
  native(native_daydream_renderer)->InitializeGl();
}

JNI_METHOD(void, nativeDrawFrame)(JNIEnv *env, jobject obj,
                                  jlong native_daydream_renderer) {
  native(native_daydream_renderer)->DrawFrame(*env);
}

JNI_METHOD(void, nativeOnPause)(JNIEnv *env, jobject obj,
                                jlong native_daydream_renderer) {
  native(native_daydream_renderer)->OnPause();
}

JNI_METHOD(void, nativeOnResume)(JNIEnv *env, jobject obj,
                                 jlong native_daydream_renderer) {
  native(native_daydream_renderer)->OnResume();
}

JNI_METHOD(void, nativeSetCameraRig)(JNIEnv *env, jobject obj,
        jlong native_daydream_renderer, jlong native_camera) {
native(native_daydream_renderer)->SetCameraRig(native_camera);
}

}  // extern "C"
