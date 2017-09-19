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

#include <engine/renderer/renderer.h>
#include "render_texture.h"
#include "gl/gl_render_texture.h"
namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctor(JNIEnv * env, jobject obj, jint width,
        jint height);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorMSAA(JNIEnv * env, jobject obj,
        jint width, jint height, jint sample_count, jint number_views);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorWithParameters(JNIEnv * env,
        jobject obj, jint width, jint height, jint sample_count,
        jint color_format, jint depth_format, jboolean resolve_depth,
        jintArray parameters);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorArray(JNIEnv * env,
        jobject obj, jint width, jint height, jint samples, jint numLayers);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_beginRendering(JNIEnv * env, jobject obj,
                                                    jlong ptr);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_endRendering(JNIEnv * env, jobject obj,
                                                  jlong ptr);
JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeRenderTexture_readRenderResult(JNIEnv * env, jobject obj,
        jlong ptr, jintArray jreadback_buffer);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_bind(JNIEnv * env, jobject obj, jlong ptr);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctor(JNIEnv * env, jobject obj,
                                          jint width, jint height)
{
    RenderTexture* tex = Renderer::getInstance()->createRenderTexture(width, height, 0, 0, DepthFormat::DEPTH_16, 0, NULL,1);
  //  RenderTexture* tex = new GLRenderTexture(width, height, 0, 0, DepthFormat::DEPTH_16, 0, NULL);
    return reinterpret_cast<jlong>(tex);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorMSAA(JNIEnv* env, jobject obj,
                                              jint width, jint height, jint sample_count, jint number_views)
{
    RenderTexture* tex = Renderer::getInstance()->createRenderTexture(width, height, sample_count, 0, DepthFormat::DEPTH_16, 0, NULL, number_views);
    return reinterpret_cast<jlong>(tex);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorWithParameters(JNIEnv* env, jobject obj,
                                                        jint width, jint height,
                                                        jint sample_count, jint color_format,
                                                        jint depth_format, jboolean resolve_depth,
                                                        jintArray j_parameters)
{
    jint* parameters = env->GetIntArrayElements(j_parameters, NULL);
    TextureParameters texparams;
    texparams.setMinFilter(parameters[0]);
    texparams.setMagFilter(parameters[1]);
    texparams.setMaxAnisotropy((float) parameters[2]);
    texparams.setWrapU(parameters[3]);
    texparams.setWrapV(parameters[2]);
    Texture* tex = Renderer::getInstance()->createRenderTexture(width, height, sample_count,
                                                                color_format, depth_format, resolve_depth, &texparams, 1);
    env->ReleaseIntArrayElements(j_parameters, parameters, 0);
    return reinterpret_cast<jlong>(tex);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTexture_ctorArray(JNIEnv * env,
          jobject obj, jint width, jint height, jint samples, jint numLayers)
{
    RenderTexture* texarray = Renderer::getInstance()->createRenderTexture(width, height, samples, numLayers);
    return reinterpret_cast<jlong>(texarray);
}


JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeRenderTexture_readRenderResult(JNIEnv* env, jobject obj,
                                                      jlong ptr, jintArray jreadback_buffer)
{
    RenderTexture *render_texture = reinterpret_cast<RenderTexture*>(ptr);
    jint *readback_buffer = env->GetIntArrayElements(jreadback_buffer, JNI_FALSE);
    jlong buffer_capacity = env->GetArrayLength(jreadback_buffer);

    bool rv = render_texture->readRenderResult((uint8_t*)readback_buffer, buffer_capacity);

    env->ReleaseIntArrayElements(jreadback_buffer, readback_buffer, 0);

    return rv;
}
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_beginRendering(JNIEnv * env, jobject obj,
                                                    jlong ptr) {
    RenderTexture *render_texture = reinterpret_cast<RenderTexture*>(ptr);
    render_texture->beginRendering(gRenderer->getInstance());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_endRendering(JNIEnv * env, jobject obj,
                                                  jlong ptr) {
    RenderTexture *render_texture = reinterpret_cast<RenderTexture*>(ptr);
    render_texture->endRendering(gRenderer->getInstance());
}
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTexture_bind(JNIEnv * env, jobject obj, jlong ptr)
{
    RenderTexture *render_texture = reinterpret_cast<RenderTexture*>(ptr);
    render_texture->bind();
}


}
