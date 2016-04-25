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

#include "post_effect_data.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectData_ctor(JNIEnv * env,
        jobject obj, jint shader_type);
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativePostEffectData_getShaderType(
        JNIEnv * env, jobject obj, jlong jpost_effect_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setShaderType(
        JNIEnv * env, jobject obj, jlong jpost_effect_data, jint shader_type);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setTexture(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jlong jtexture);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePostEffectData_getFloat(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setFloat(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat value);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec2(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setVec2(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat x, jfloat y);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec3(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setVec3(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat x, jfloat y,
        jfloat z);
JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec4(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setVec4(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat x, jfloat y,
        jfloat z, jfloat w);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setMat4(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key,
        jfloat x1, jfloat y1, jfloat z1, jfloat w1,
        jfloat x2, jfloat y2, jfloat z2, jfloat w2,
        jfloat x3, jfloat y3, jfloat z3, jfloat w3,
        jfloat x4, jfloat y4, jfloat z4, jfloat w4);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectData_ctor(JNIEnv * env,
        jobject obj, jint shader_type) {
    return reinterpret_cast<jlong>(new PostEffectData(
                    static_cast<PostEffectData::ShaderType>(shader_type)));
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativePostEffectData_getShaderType(
        JNIEnv * env, jobject obj, jlong jpost_effect_data) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    return static_cast<jint>(post_effect_data->shader_type());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setShaderType(
        JNIEnv * env, jobject obj, jlong jpost_effect_data, jint shader_type) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    return post_effect_data->set_shader_type(
            static_cast<PostEffectData::ShaderType>(shader_type));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setTexture(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jlong jtexture) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    Texture* texture = reinterpret_cast<Texture*>(jtexture);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setTexture(native_key, texture);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePostEffectData_getFloat(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    env->ReleaseStringUTFChars(key, char_key);
    return static_cast<jfloat>(post_effect_data->getFloat(native_key));

}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setFloat(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat value) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setFloat(native_key, value);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec2(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    glm::vec2 post_effect_data_vec2 = post_effect_data->getVec2(native_key);
    jfloatArray jvec2 = env->NewFloatArray(2);
    env->SetFloatArrayRegion(jvec2, 0, 2,
            reinterpret_cast<jfloat*>(&post_effect_data_vec2));
    env->ReleaseStringUTFChars(key, char_key);
    return jvec2;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setVec2(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat x, jfloat y) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setVec2(native_key, glm::vec2(x, y));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec3(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    glm::vec3 post_effect_data_vec3 = post_effect_data->getVec3(native_key);
    jfloatArray jvec3 = env->NewFloatArray(3);
    env->SetFloatArrayRegion(jvec3, 0, 3,
            reinterpret_cast<jfloat*>(&post_effect_data_vec3));
    env->ReleaseStringUTFChars(key, char_key);
    return jvec3;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setVec3(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat x, jfloat y,
        jfloat z) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setVec3(native_key, glm::vec3(x, y, z));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec4(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    glm::vec4 post_effect_data_vec4 = post_effect_data->getVec4(native_key);
    jfloatArray jvec4 = env->NewFloatArray(4);
    env->SetFloatArrayRegion(jvec4, 0, 4,
            reinterpret_cast<jfloat*>(&post_effect_data_vec4));
    env->ReleaseStringUTFChars(key, char_key);
    return jvec4;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setVec4(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat x, jfloat y,
        jfloat z, jfloat w) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setVec4(native_key, glm::vec4(x, y, z, w));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setMat4(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key,
        jfloat x1, jfloat y1, jfloat z1, jfloat w1,
        jfloat x2, jfloat y2, jfloat z2, jfloat w2,
        jfloat x3, jfloat y3, jfloat z3, jfloat w3,
        jfloat x4, jfloat y4, jfloat z4, jfloat w4) {
    PostEffectData* post_effect_data =
            reinterpret_cast<PostEffectData*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setMat4(native_key, glm::mat4(x1, y1, z1, w1,
                                                    x2, y2, z2, w2,
                                                    x3, y3, z3, w3,
                                                    x4, y4, z4, w4));
    env->ReleaseStringUTFChars(key, char_key);
}
}
