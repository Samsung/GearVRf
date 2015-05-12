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

#include "custom_post_effect_shader.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addTextureKey(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addFloatKey(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addVec2Key(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addVec3Key(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addVec4Key(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addMat4Key(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key);
}
;

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addTextureKey(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key) {
    CustomPostEffectShader* custom_post_effect_shader =
            reinterpret_cast<CustomPostEffectShader*>(jcustom_post_effect_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_post_effect_shader->addTextureKey(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addFloatKey(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key) {
    CustomPostEffectShader* custom_post_effect_shader =
            reinterpret_cast<CustomPostEffectShader*>(jcustom_post_effect_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_post_effect_shader->addFloatKey(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addVec2Key(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key) {
    CustomPostEffectShader* custom_post_effect_shader =
            reinterpret_cast<CustomPostEffectShader*>(jcustom_post_effect_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_post_effect_shader->addVec2Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addVec3Key(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key) {
    CustomPostEffectShader* custom_post_effect_shader =
            reinterpret_cast<CustomPostEffectShader*>(jcustom_post_effect_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_post_effect_shader->addVec3Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addVec4Key(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key) {
    CustomPostEffectShader* custom_post_effect_shader =
            reinterpret_cast<CustomPostEffectShader*>(jcustom_post_effect_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_post_effect_shader->addVec4Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCustomPostEffectShader_addMat4Key(
        JNIEnv * env, jobject obj, jlong jcustom_post_effect_shader,
        jstring variable_name, jstring key) {
    CustomPostEffectShader* custom_post_effect_shader =
            reinterpret_cast<CustomPostEffectShader*>(jcustom_post_effect_shader);
    const char* char_variable_name = env->GetStringUTFChars(variable_name, 0);
    std::string native_variable_name = std::string(char_variable_name);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    custom_post_effect_shader->addMat4Key(native_variable_name, native_key);
    env->ReleaseStringUTFChars(variable_name, char_variable_name);
    env->ReleaseStringUTFChars(key, char_key);
}
}
