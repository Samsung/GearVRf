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
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderManager_ctor(JNIEnv * env,
    jobject obj) {
    return reinterpret_cast<jlong>(new ShaderManager());
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderManager_addCustomShader(
    JNIEnv * env, jobject obj, jlong jshader_manager, jstring vertex_shader,
    jstring fragment_shader) {
    ShaderManager* shader_manager =
    reinterpret_cast<ShaderManager*>(jshader_manager);
    const char *vertex_str = env->GetStringUTFChars(vertex_shader, 0);
    std::string native_vertex_shader = std::string(vertex_str);
    const char *fragment_str = env->GetStringUTFChars(fragment_shader, 0);
    std::string native_fragment_shader = std::string(fragment_str);
    int id = shader_manager->addCustomShader(native_vertex_shader,
            native_fragment_shader);
LOGE("SHADER: end added custom shader %d\n", id);
    env->ReleaseStringUTFChars(vertex_shader, vertex_str);
    env->ReleaseStringUTFChars(fragment_shader, fragment_str);
    return id;
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderManager_getCustomShader(
    JNIEnv * env, jobject obj, jlong jshader_manager, jint id) {
    ShaderManager* shader_manager =
    reinterpret_cast<ShaderManager*>(jshader_manager);
    try {
        return reinterpret_cast<jlong>(shader_manager->getCustomShader(id));
    } catch (char const *e) {
        return 0;
}
}

}
