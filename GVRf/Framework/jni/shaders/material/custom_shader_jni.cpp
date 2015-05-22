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

#include "custom_shader.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addTextureKey(JNIEnv * env,
        jobject obj, jlong jcustom_shader, jstring variable_name, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addAttributeFloatKey(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addAttributeVec2Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addAttributeVec3Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addAttributeVec4Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformFloatKey(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformVec2Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformVec3Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformVec4Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformMat4Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key);
}
;

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addTextureKey(JNIEnv * env,
        jobject obj, jlong jcustom_shader, jstring variable_name, jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addTextureKey(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addAttributeFloatKey(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addAttributeFloatKey(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addAttributeVec2Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addAttributeVec2Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addAttributeVec3Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addAttributeVec3Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addAttributeVec4Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addAttributeVec4Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformFloatKey(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addUniformFloatKey(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformVec2Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addUniformVec2Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformVec3Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addUniformVec3Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformVec4Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addUniformVec4Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomShader_addUniformMat4Key(
        JNIEnv * env, jobject obj, jlong jcustom_shader, jstring variable_name,
        jstring key) {
    CustomShader* custom_shader = reinterpret_cast<CustomShader*>(jcustom_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_shader->addUniformMat4Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(key, char_key);
}
}
