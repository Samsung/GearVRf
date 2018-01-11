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
#include "engine/renderer/renderer.h"
#include "util/gvr_jni.h"

namespace gvr
{
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderData_ctor(JNIEnv* env, jobject obj, jstring udesc, jstring tdesc);


JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_useGpuBuffer(JNIEnv* env,
                                               jobject obj, jlong jshader_data, jboolean  flag);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setTexture(JNIEnv* env,
                                             jobject obj, jlong jshader_data, jstring key,
                                             jlong jtexture);

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeShaderData_getFloat(JNIEnv* env,
                                           jobject obj, jlong jshader_data, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setFloat(JNIEnv* env,
                                           jobject obj, jlong jshader_data, jstring key,
                                           jfloat value);

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderData_getInt(JNIEnv* env,
                                         jobject obj, jlong jshader_data, jstring key);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setInt(JNIEnv* env,
                                         jobject obj, jlong jshader_data, jstring key,
                                         jint value);

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeShaderData_getFloatVec(JNIEnv* env,
                                              jobject obj, jlong jshader_data, jstring key);

JNIEXPORT jintArray JNICALL
Java_org_gearvrf_NativeShaderData_getIntVec(JNIEnv* env,
                                            jobject obj, jlong jshader_data, jstring key);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setFloatVec(JNIEnv* env, jobject obj,
                                              jlong jshader_data, jstring key, jfloatArray jvec,
                                              jint size);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setIntVec(JNIEnv* env, jobject obj,
                                            jlong jshader_data, jstring key, jintArray jvec,
                                            jint size);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setVec2(JNIEnv* env,
                                          jobject obj, jlong jshader_data, jstring key,
                                          jfloat x, jfloat y);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setVec3(JNIEnv* env,
                                          jobject obj, jlong jshader_data, jstring key,
                                          jfloat x, jfloat y, jfloat z);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setVec4(JNIEnv* env,
                                          jobject obj, jlong jshader_data, jstring key,
                                          jfloat x, jfloat y,
                                          jfloat z, jfloat w);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setMat4(JNIEnv* env,
                                          jobject obj, jlong jshader_data, jstring key,
                                          jfloat x1, jfloat y1, jfloat z1, jfloat w1,
                                          jfloat x2, jfloat y2, jfloat z2, jfloat w2,
                                          jfloat x3, jfloat y3, jfloat z3, jfloat w3,
                                          jfloat x4, jfloat y4, jfloat z4, jfloat w4);

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeShaderData_getMat4(JNIEnv* env,
                                          jobject obj, jlong jshader_data, jstring key);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_hasUniform(JNIEnv*, jobject, jlong, jstring);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_hasTexture(JNIEnv*, jobject, jlong, jstring);

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeShaderData_makeShaderLayout(JNIEnv*, jobject, jlong shader_data);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_copyUniforms(JNIEnv* env,
                                             jobject obj, jlong jdest, jlong jsrc);

};


JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_useGpuBuffer(JNIEnv* env,
                                               jobject obj, jlong jshader_data, jboolean  flag){
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    shader_data->useGPUBuffer(flag);
}
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderData_ctor(JNIEnv* env, jobject obj, jstring junidesc, jstring jtexdesc)
{
    const char* uni_desc = env->GetStringUTFChars(junidesc, 0);
    const char* tex_desc = env->GetStringUTFChars(jtexdesc, 0);
    Renderer* renderer = Renderer::getInstance();
    ShaderData* shaderData = renderer->createMaterial(uni_desc, tex_desc);
    env->ReleaseStringUTFChars(junidesc, uni_desc);
    env->ReleaseStringUTFChars(jtexdesc, tex_desc);
    return reinterpret_cast<jlong>(shaderData);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setTexture(JNIEnv* env, jobject obj,
                                             jlong jshader_data, jstring key, jlong jtexture)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    Texture* texture = reinterpret_cast<Texture*>(jtexture);
    const char* char_key = env->GetStringUTFChars(key, 0);
    shader_data->setTexture(char_key, texture);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setIntVec(JNIEnv* env, jobject obj,
                                            jlong jshader_data, jstring key, jintArray jvec,
                                            jint size)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    int* elems = env->GetIntArrayElements(jvec, 0);
    bool rc = shader_data->setIntVec(char_key, elems, size);
    env->ReleaseStringUTFChars(key, char_key);
    env->ReleaseIntArrayElements(jvec, elems, 0);
    return rc;
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setFloatVec(JNIEnv* env, jobject obj,
                                              jlong jshader_data, jstring key, jfloatArray jvec,
                                              jint size)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    float* elems = env->GetFloatArrayElements(jvec, 0);
    bool rc = shader_data->setFloatVec(char_key, elems, size);
    env->ReleaseStringUTFChars(key, char_key);
    env->ReleaseFloatArrayElements(jvec, elems, 0);
    return rc;
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeShaderData_getFloatVec(JNIEnv* env, jobject obj,
                                              jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    int size = shader_data->getByteSize(char_key) / sizeof(float);
    if (size > 0)
    {
        jfloatArray jvec = env->NewFloatArray(size);
        float* elems = env->GetFloatArrayElements(jvec, 0);
        shader_data->getFloatVec(char_key, elems, size);
        env->ReleaseFloatArrayElements(jvec, elems, 0);
        env->ReleaseStringUTFChars(key, char_key);
        return jvec;
    }
    env->ReleaseStringUTFChars(key, char_key);
    return NULL;
}

JNIEXPORT jintArray JNICALL
Java_org_gearvrf_NativeShaderData_getIntVec(JNIEnv* env, jobject obj,
                                            jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    int size = shader_data->getByteSize(char_key) / sizeof(int);
    if (size > 0)
    {
        jintArray jvec = env->NewIntArray(size);
        int* elems = env->GetIntArrayElements(jvec, 0);
        shader_data->getIntVec(char_key, elems, size);
        env->ReleaseStringUTFChars(key, char_key);
        env->ReleaseIntArrayElements(jvec, elems, 0);
        return jvec;
    }
    env->ReleaseStringUTFChars(key, char_key);
    return NULL;
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeShaderData_getFloat(JNIEnv* env, jobject obj,
                                           jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    float f = 0.0f;
    shader_data->getFloat(char_key, f);
    env->ReleaseStringUTFChars(key, char_key);
    return static_cast<jfloat>(f);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderData_setFloat(JNIEnv* env, jobject obj, jlong jshader_data,
                                           jstring key, jfloat value)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    shader_data->setFloat(char_key, value);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderData_getInt(JNIEnv* env, jobject obj,
                                         jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    int i = 0;
    shader_data->getInt(char_key, i);
    env->ReleaseStringUTFChars(key, char_key);
    return static_cast<jint>(i);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setInt(JNIEnv* env, jobject obj,
                                         jlong jshader_data, jstring key, jint value)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool rc = shader_data->setInt(char_key, value);
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}


JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setVec2(JNIEnv* env, jobject obj,
                                          jlong jshader_data, jstring key, jfloat x, jfloat y)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool rc = shader_data->setVec2(char_key, glm::vec2(x, y));
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}


JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setVec3(JNIEnv* env, jobject obj,
                                          jlong jshader_data, jstring key, jfloat x, jfloat y,
                                          jfloat z)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool rc = shader_data->setVec3(char_key, glm::vec3(x, y, z));
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}


JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setVec4(JNIEnv* env, jobject obj,
                                          jlong jshader_data, jstring key, jfloat x, jfloat y,
                                          jfloat z, jfloat w)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool rc = shader_data->setVec4(char_key, glm::vec4(x, y, z, w));
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_setMat4(JNIEnv* env,
                                          jobject obj, jlong jshader_data, jstring key,
                                          jfloat x1, jfloat y1, jfloat z1, jfloat w1,
                                          jfloat x2, jfloat y2, jfloat z2, jfloat w2,
                                          jfloat x3, jfloat y3, jfloat z3, jfloat w3,
                                          jfloat x4, jfloat y4, jfloat z4, jfloat w4)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    float m[16] = {x1, y1, z1, w1, x2, y2, z2, w2, x3, y3, z3, w3, x4, y4, z4, w4};
    bool rc = shader_data->setFloatVec(char_key, m, 16);
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeShaderData_getMat4(JNIEnv* env,
                                          jobject obj, jlong jshader_data, jstring key)
{
    return Java_org_gearvrf_NativeShaderData_getFloatVec(env, obj, jshader_data, key);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_hasTexture(JNIEnv* env, jobject obj,
                                             jlong jshader_data, jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool hasKey = shader_data->hasTexture(char_key);
    env->ReleaseStringUTFChars(key, char_key);
    return (jboolean) hasKey;
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_hasUniform(JNIEnv* env, jobject obj, jlong jshader_data,
                                             jstring key)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool hasUniform = shader_data->hasUniform(char_key);
    env->ReleaseStringUTFChars(key, char_key);
    return (jboolean) hasUniform;

}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeShaderData_makeShaderLayout(JNIEnv* env, jobject, jlong jshader_data)
{
    ShaderData* shader_data = reinterpret_cast<ShaderData*>(jshader_data);
    const std::string& layout = shader_data->makeShaderLayout();
    return env->NewStringUTF(layout.c_str());
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeShaderData_copyUniforms(JNIEnv* env,
                                               jobject obj, jlong jdest, jlong jsrc)
{
    const ShaderData* src = reinterpret_cast<ShaderData*>(jsrc);
    ShaderData* dest = reinterpret_cast<ShaderData*>(jdest);

    return dest->copyUniforms(src);
}



}

