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

#include "shader_manager.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderManager_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderManager_addCustomShader(
        JNIEnv * env, jobject obj, jlong jshader_manager, jstring vertex_shader,
        jstring fragment_shader);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderManager_getCustomShader(
        JNIEnv * env, jobject obj, jlong jshader_manager, jint id);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderManager_equals(JNIEnv * env,
        jobject obj, jlong jshader_manager, jlong jother);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderManager_delete(JNIEnv * env,
        jobject obj, jlong jshader_manager);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderManager_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new std::shared_ptr<ShaderManager>(
            new ShaderManager()));
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderManager_addCustomShader(
        JNIEnv * env, jobject obj, jlong jshader_manager, jstring vertex_shader,
        jstring fragment_shader) {
    std::shared_ptr<ShaderManager> shader_manager =
            *reinterpret_cast<std::shared_ptr<ShaderManager>*>(jshader_manager);
    const char *vertex_str = env->GetStringUTFChars(vertex_shader, 0);
    std::string native_vertex_shader = std::string(vertex_str);
    const char *fragment_str = env->GetStringUTFChars(fragment_shader, 0);
    std::string native_fragment_shader = std::string(fragment_str);

    int id = shader_manager->addCustomShader(native_vertex_shader,
            native_fragment_shader);

    env->ReleaseStringUTFChars(vertex_shader, vertex_str);
    env->ReleaseStringUTFChars(fragment_shader, fragment_str);
    return id;
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderManager_getCustomShader(
        JNIEnv * env, jobject obj, jlong jshader_manager, jint id) {
    std::shared_ptr<ShaderManager> shader_manager =
            *reinterpret_cast<std::shared_ptr<ShaderManager>*>(jshader_manager);
    try {
        std::shared_ptr<CustomShader> custom_shader =
                shader_manager->getCustomShader(id);
        return reinterpret_cast<jlong>(new std::shared_ptr<CustomShader>(
                custom_shader));
    } catch (char const *e) {
        return 0;
    }
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderManager_equals(JNIEnv * env,
        jobject obj, jlong jshader_manager, jlong jother) {
    std::shared_ptr<ShaderManager> shader_manager =
            *reinterpret_cast<std::shared_ptr<ShaderManager>*>(jshader_manager);
    std::shared_ptr<ShaderManager> other = *reinterpret_cast<std::shared_ptr<
            ShaderManager>*>(jother);
    return shader_manager.get() == other.get();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderManager_delete(JNIEnv * env,
        jobject obj, jlong jshader_manager) {
    delete reinterpret_cast<std::shared_ptr<ShaderManager>*>(jshader_manager);
}

}
