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
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectData_getTexture(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key);

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
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectData_ctor(JNIEnv * env,
        jobject obj, jint shader_type) {
    return reinterpret_cast<jlong>(new std::shared_ptr<PostEffectData>(
            new PostEffectData(
                    static_cast<PostEffectData::ShaderType>(shader_type))));
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativePostEffectData_getShaderType(
        JNIEnv * env, jobject obj, jlong jpost_effect_data) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    return static_cast<jint>(post_effect_data->shader_type());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setShaderType(
        JNIEnv * env, jobject obj, jlong jpost_effect_data, jint shader_type) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    return post_effect_data->set_shader_type(
            static_cast<PostEffectData::ShaderType>(shader_type));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativePostEffectData_getTexture(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    try {
        std::shared_ptr<Texture> texture = post_effect_data->getTexture(
                native_key);
        env->ReleaseStringUTFChars(key, char_key);
        return reinterpret_cast<jlong>(new std::shared_ptr<Texture>(texture));
    } catch (std::string e) {
        LOGE("%s", e.c_str());
        env->ReleaseStringUTFChars(key, char_key);
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setTexture(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jlong jtexture) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    std::shared_ptr<Texture> texture =
            *reinterpret_cast<std::shared_ptr<Texture>*>(jtexture);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setTexture(native_key, texture);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePostEffectData_getFloat(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    env->ReleaseStringUTFChars(key, char_key);
    return static_cast<jfloat>(post_effect_data->getFloat(native_key));

}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativePostEffectData_setFloat(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key, jfloat value) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setFloat(native_key, value);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec2(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
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
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setVec2(native_key, glm::vec2(x, y));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec3(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
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
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setVec3(native_key, glm::vec3(x, y, z));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativePostEffectData_getVec4(JNIEnv * env,
        jobject obj, jlong jpost_effect_data, jstring key) {
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
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
    std::shared_ptr<PostEffectData> post_effect_data =
            *reinterpret_cast<std::shared_ptr<PostEffectData>*>(jpost_effect_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    post_effect_data->setVec4(native_key, glm::vec4(x, y, z, w));
    env->ReleaseStringUTFChars(key, char_key);
}

}
