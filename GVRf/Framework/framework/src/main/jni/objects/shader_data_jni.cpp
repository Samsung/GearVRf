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

#include "shader_data.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeShaderData_ctor(JNIEnv * env, jobject obj);

    JNIEXPORT jint JNICALL
    Java_org_gearvrf_NativeShaderData_getNativeShader(
            JNIEnv * env, jobject obj, jlong jshader_data);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderData_setNativeShader(
            JNIEnv * env, jobject obj, jlong jshader_data, jint shader);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderData_setTexture(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key, jlong jtexture);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_NativeShaderData_getFloat(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderData_setFloat(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key, jfloat value);

    JNIEXPORT jint JNICALL Java_org_gearvrf_NativeShaderData_getInt(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderData_setInt(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key, jint value);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeShaderData_getVec2(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderData_setVec2(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key, jfloat x, jfloat y);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeShaderData_getVec3(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderData_setVec3(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key, jfloat x, jfloat y, jfloat z);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeShaderData_getVec4(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderData_setVec4(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key, jfloat x, jfloat y,
            jfloat z, jfloat w);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderData_setMat4(JNIEnv * env,
            jobject obj, jlong jshader_data, jstring key,
            jfloat x1, jfloat y1, jfloat z1, jfloat w1,
            jfloat x2, jfloat y2, jfloat z2, jfloat w2,
            jfloat x3, jfloat y3, jfloat z3, jfloat w3,
            jfloat x4, jfloat y4, jfloat z4, jfloat w4);

    JNIEXPORT jboolean JNICALL
            Java_org_gearvrf_NativeShaderData_hasUniform(JNIEnv *, jobject, jlong, jstring);

    JNIEXPORT jboolean JNICALL
            Java_org_gearvrf_NativeShaderData_hasTexture(JNIEnv *, jobject, jlong, jstring);
};

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderData_ctor(JNIEnv * env, jobject obj)
{
    return reinterpret_cast<jlong>(new ShaderData());
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderData_getNativeShader(
        JNIEnv * env, jobject obj, jlong jshader_data)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    return static_cast<jint>(shader_data->get_shader());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setNativeShader(JNIEnv *env, jobject obj,
        jlong jshader_data, jint shader)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    shader_data->set_shader(shader);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setTexture(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key, jlong jtexture)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    Texture* texture = reinterpret_cast<Texture*>(jtexture);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    shader_data->setTexture(native_key, texture);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeShaderData_getFloat(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    env->ReleaseStringUTFChars(key, char_key);
    float f = 0.0f;
    shader_data->getFloat(native_key, f);
    return static_cast<jfloat>(f);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setFloat(JNIEnv * env, jobject obj, jlong jshader_data,
        jstring key, jfloat value)
{
    ShaderData *shader_data = reinterpret_cast<ShaderData *>(jshader_data);
    const char *char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    shader_data->setFloat(native_key, value);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderData_getInt(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    env->ReleaseStringUTFChars(key, char_key);
    int i = 0;
    shader_data->getInt(native_key, i);
    return static_cast<jint>(i);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setInt(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key, jint value)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    shader_data->setInt(native_key, value);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeShaderData_getVec2(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    glm::vec2 v;
    jfloatArray jvec2 = 0;
    if (shader_data->getVec2(native_key, v))
    {
        jvec2 = env->NewFloatArray(2);
        env->SetFloatArrayRegion(jvec2, 0, 2, reinterpret_cast<jfloat*>(&v));
    }
    env->ReleaseStringUTFChars(key, char_key);
    return jvec2;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setVec2(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key, jfloat x, jfloat y)
{
    ShaderData* shader_data =reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    shader_data->setVec2(native_key, glm::vec2(x, y));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeShaderData_getVec3(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key) {
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    glm::vec3 v;
    jfloatArray jvec3 = 0;
    if (shader_data->getVec3(native_key, v))
    {
        jvec3 = env->NewFloatArray(3);
        env->SetFloatArrayRegion(jvec3, 0, 3, reinterpret_cast<jfloat*>(&v));
    }
    env->ReleaseStringUTFChars(key, char_key);
    return jvec3;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setVec3(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key, jfloat x, jfloat y, jfloat z)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    shader_data->setVec3(native_key, glm::vec3(x, y, z));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeShaderData_getVec4(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    glm::vec4 v;
    jfloatArray jvec4 = 0;
    if (shader_data->getVec4(native_key, v))
    {
        jvec4 = env->NewFloatArray(4);
        env->SetFloatArrayRegion(jvec4, 0, 4, reinterpret_cast<jfloat*>(&v));
    }
    env->ReleaseStringUTFChars(key, char_key);
    return jvec4;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setVec4(JNIEnv * env, jobject obj,
        jlong jshader_data, jstring key,
        jfloat x, jfloat y, jfloat z, jfloat w)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    shader_data->setVec4(native_key, glm::vec4(x, y, z, w));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setMat4(JNIEnv * env,
        jobject obj, jlong jshader_data, jstring key,
        jfloat x1, jfloat y1, jfloat z1, jfloat w1,
        jfloat x2, jfloat y2, jfloat z2, jfloat w2,
        jfloat x3, jfloat y3, jfloat z3, jfloat w3,
        jfloat x4, jfloat y4, jfloat z4, jfloat w4)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    shader_data->setMat4(native_key, glm::mat4(x1, y1, z1, w1,
                                               x2, y2, z2, w2,
                                               x3, y3, z3, w3,
                                               x4, y4, z4, w4));
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_hasUniform(JNIEnv *env, jobject obj,
        jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    bool hasKey = shader_data->hasUniform(native_key);
    env->ReleaseStringUTFChars(key, char_key);
    return (jboolean) hasKey;
}

JNIEXPORT jboolean JNICALL
        Java_org_gearvrf_NativeShaderData_hasTexture(JNIEnv *env, jobject obj,
        jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    std::string native_key = std::string(char_key);
    bool hasKey = shader_data->hasTexture(native_key);
    env->ReleaseStringUTFChars(key, char_key);
    return (jboolean) hasKey;
}

}
