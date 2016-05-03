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

#include "post_effect_shader_manager.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectShaderManager_ctor(
        JNIEnv * env, jobject obj);
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativePostEffectShaderManager_addCustomPostEffectShader(
        JNIEnv * env, jobject obj, jlong jpost_effect_shader_manager,
        jstring vertex_shader, jstring fragment_shader);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectShaderManager_getCustomPostEffectShader(
        JNIEnv * env, jobject obj, jlong jpost_effect_shader_manager, jint id);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectShaderManager_delete(
        JNIEnv * env, jobject obj, jlong jpost_effect_shader_manager);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectShaderManager_ctor(
        JNIEnv * env, jobject obj) {
    return reinterpret_cast<jlong>(new PostEffectShaderManager());
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativePostEffectShaderManager_addCustomPostEffectShader(
        JNIEnv * env, jobject obj, jlong jpost_effect_shader_manager,
        jstring vertex_shader, jstring fragment_shader) {
    PostEffectShaderManager* post_effect_shader_manager =
            reinterpret_cast<PostEffectShaderManager*>(jpost_effect_shader_manager);

    const char *vertex_str = env->GetStringUTFChars(vertex_shader, 0);
    const char *fragment_str = env->GetStringUTFChars(fragment_shader, 0);
    std::string native_vertex_shader = std::string(vertex_str);
    std::string native_fragment_shader = std::string(fragment_str);

    int id = post_effect_shader_manager->addCustomPostEffectShader(
            native_vertex_shader, native_fragment_shader);

    env->ReleaseStringUTFChars(vertex_shader, vertex_str);
    env->ReleaseStringUTFChars(fragment_shader, fragment_str);

    return id;
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectShaderManager_getCustomPostEffectShader(
        JNIEnv * env, jobject obj, jlong jpost_effect_shader_manager, jint id) {
    PostEffectShaderManager* post_effect_shader_manager =
            reinterpret_cast<PostEffectShaderManager*>(jpost_effect_shader_manager);
    try {
        CustomPostEffectShader* custom_post_effect_shader =
                post_effect_shader_manager->getCustomPostEffectShader(id);
        return reinterpret_cast<jlong>(custom_post_effect_shader);
    } catch (char const *c) {
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectShaderManager_delete(
        JNIEnv * env, jobject obj, jlong jpost_effect_shader_manager) {
    delete reinterpret_cast<PostEffectShaderManager*>(jpost_effect_shader_manager);
}

}
