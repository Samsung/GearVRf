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

#include "render_texture.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctor(JNIEnv * env, jobject obj, jint width,
        jint height);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorMSAA(JNIEnv * env, jobject obj,
        jint width, jint height, jint sample_count);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorWithParameters(JNIEnv * env,
        jobject obj, jint width, jint height, jint sample_count,
        jint color_format, jint depth_format, jboolean resolve_depth,
        jintArray parameters);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_beginRendering(JNIEnv * env, jobject obj,
        jlong ptr);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_endRendering(JNIEnv * env, jobject obj,
        jlong ptr);
JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeRenderTexture_readRenderResult(JNIEnv * env, jobject obj,
        jlong ptr, jintArray jreadback_buffer);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctor(JNIEnv * env, jobject obj, jint width,
        jint height) {
    return reinterpret_cast<jlong>(new RenderTexture(width, height));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorMSAA(JNIEnv * env, jobject obj,
        jint width, jint height, jint sample_count) {
    return reinterpret_cast<jlong>(new RenderTexture(width, height,
            sample_count));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorWithParameters(JNIEnv * env,
        jobject obj, jint width, jint height, jint sample_count,
        jint color_format, jint depth_format, jboolean resolve_depth,
        jintArray j_parameters) {
    jint* parameters = env->GetIntArrayElements(j_parameters, NULL);
    jlong result =
            reinterpret_cast<jlong>(new RenderTexture(width, height,
                    sample_count, color_format, depth_format, resolve_depth,
                    parameters));
    env->ReleaseIntArrayElements(j_parameters, parameters, 0);
    return result;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_beginRendering(JNIEnv * env, jobject obj,
        jlong ptr) {
    RenderTexture *render_texture = reinterpret_cast<RenderTexture*>(ptr);
    render_texture->beginRendering();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_endRendering(JNIEnv * env, jobject obj,
        jlong ptr) {
    RenderTexture *render_texture = reinterpret_cast<RenderTexture*>(ptr);
    render_texture->endRendering();
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeRenderTexture_readRenderResult(JNIEnv * env, jobject obj,
        jlong ptr, jintArray jreadback_buffer) {
    RenderTexture *render_texture = reinterpret_cast<RenderTexture*>(ptr);
    jint *readback_buffer = env->GetIntArrayElements(jreadback_buffer, JNI_FALSE);
    jlong buffer_capacity = env->GetArrayLength(jreadback_buffer);

    bool rv = render_texture->readRenderResult((uint32_t*)readback_buffer, buffer_capacity);

    env->ReleaseIntArrayElements(jreadback_buffer, readback_buffer, 0);

    return rv;
}

}
