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

#include "light.h"

#include "util/gvr_jni.h"
#include "glm/gtc/type_ptr.hpp"
#include "scene.h"
#include "lightlist.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeLight_ctor(JNIEnv * env, jobject obj, jstring juniformDesc);

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeLight_getComponentType(JNIEnv * env, jobject obj);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_enable(JNIEnv * env, jobject obj, jlong jlight);

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_getLightName(JNIEnv * env, jobject obj, jlong jlight);


JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_disable(JNIEnv * env, jobject obj, jlong jlight);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_getCastShadow(JNIEnv * env, jobject obj, jlong jlight);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_setTexture(JNIEnv* env, jobject obj,
                                        jlong jlight, jstring key, jlong jtexture);

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeLight_getFloat(JNIEnv* env, jobject obj,
                                      jlong jlight, jstring key);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_setFloat(JNIEnv* env, jobject obj,
                                      jlong jlight, jstring key, jfloat value);

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeLight_getInt(JNIEnv* env, jobject obj,
                                    jlong jlight, jstring key);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setInt(JNIEnv* env, jobject obj,
                                    jlong jlight, jstring key, jint value);

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeLight_getFloatVec(JNIEnv* env, jobject obj,
                                         jlong jlight, jstring key);

JNIEXPORT jintArray JNICALL
Java_org_gearvrf_NativeLight_getIntVec(JNIEnv* env, jobject obj,
                                       jlong jlight, jstring key);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setFloatVec(JNIEnv* env, jobject obj,
                                         jlong jlight, jstring key,
                                         jfloatArray jvec, jint size);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setIntVec(JNIEnv* env, jobject obj,
                                       jlong jlight, jstring key,
                                       jintArray jvec, jint size);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setVec2(JNIEnv* env, jobject obj,
                                     jlong jlight, jstring key,
                                     jfloat x, jfloat y);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setVec3(JNIEnv* env, jobject obj,
                                     jlong jlight, jstring key,
                                     jfloat x, jfloat y, jfloat z);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setVec4(JNIEnv* env, jobject obj,
                                     jlong jlight, jstring key,
                                     jfloat x, jfloat y, jfloat z, jfloat w);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setMat4(JNIEnv* env, jobject obj,
                                     jlong jlight, jstring key,
                                     jfloat x1, jfloat y1, jfloat z1, jfloat w1,
                                     jfloat x2, jfloat y2, jfloat z2, jfloat w2,
                                     jfloat x3, jfloat y3, jfloat z3, jfloat w3,
                                     jfloat x4, jfloat y4, jfloat z4, jfloat w4);

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeLight_getMat4(JNIEnv* env, jobject obj,
                                     jlong jlight, jstring key);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_hasUniform(JNIEnv*, jobject, jlong, jstring);

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_hasTexture(JNIEnv*, jobject, jlong, jstring);

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_getLightClass(JNIEnv * env,
                                        jobject obj, jlong jlight);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_setLightClass(JNIEnv * env, jobject obj, jlong jlight, jstring lightclass);

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeLight_getLightIndex(JNIEnv * env, jobject obj, jlong jlight);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_setLightIndex(JNIEnv * env, jobject obj, jlong jlight, jint index);

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_getShaderType(JNIEnv* env, jobject, jlong jlight, jstring jtype);

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_makeShaderLayout(JNIEnv * env, jobject obj, jlong jlight);

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_makeShaderBlock(JNIEnv * env, jobject obj, jlong jscene);

}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeLight_ctor(JNIEnv * env, jobject obj, jstring juniformDesc)
{
    const char* uniform_desc = env->GetStringUTFChars(juniformDesc, 0);
    const char* texture_desc = "";
    Renderer* renderer = Renderer::getInstance();
    Light* light = renderer->createLight(uniform_desc, texture_desc);
    jlong ptr = reinterpret_cast<jlong>(light);
    env->ReleaseStringUTFChars(juniformDesc, uniform_desc);
    return ptr;
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeLight_getComponentType(JNIEnv * env, jobject obj)
{
    return Light::getComponentType();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_enable(JNIEnv * env, jobject obj, jlong jlight) 
{
    Light* light = reinterpret_cast<Light*>(jlight);
    light->set_enable(true);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_disable(JNIEnv * env, jobject obj, jlong jlight)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    light->set_enable(false);
}


JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_getLightClass(JNIEnv * env, jobject obj, jlong jlight)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    std::string clz = light->getLightClass();
    return env->NewStringUTF(clz.c_str());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_setLightClass(JNIEnv * env, jobject obj, jlong jlight, jstring jlightClass)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_id = env->GetStringUTFChars(jlightClass, 0);
    light->setLightClass(char_id);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeLight_getLightIndex(JNIEnv * env, jobject obj, jlong jlight)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    return light->getLightIndex();
}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_getLightName(JNIEnv * env, jobject obj, jlong jlight)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    std::string clz = light->getLightName();
    return env->NewStringUTF(clz.c_str());
}


    JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_setLightIndex(JNIEnv * env, jobject obj, jlong jlight, jint index)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    light->setLightIndex(index);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_getCastShadow(JNIEnv * env, jobject obj, jlong jlight)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    unsigned char rc = (unsigned char) light->castShadow();
    return reinterpret_cast<jboolean>(rc);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_setTexture(JNIEnv* env, jobject obj,
                                        jlong jlight, jstring key, jlong jtexture)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    Texture* texture = reinterpret_cast<Texture*>(jtexture);
    const char* char_key = env->GetStringUTFChars(key, 0);
    light->setTexture(char_key, texture);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setIntVec(JNIEnv* env, jobject obj,
                                       jlong jlight, jstring key,
                                       jintArray jvec, jint size)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    int* elems = env->GetIntArrayElements(jvec, 0);
    bool rc = light->setIntVec(char_key, elems, size);
    env->ReleaseStringUTFChars(key, char_key);
    env->ReleaseIntArrayElements(jvec, elems, 0);
    return rc;
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setFloatVec(JNIEnv* env, jobject obj,
                                         jlong jlight, jstring key,
                                         jfloatArray jvec, jint size)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    float* elems = env->GetFloatArrayElements(jvec, 0);
    bool rc = light->setFloatVec(char_key, elems, size);
    env->ReleaseStringUTFChars(key, char_key);
    env->ReleaseFloatArrayElements(jvec, elems, 0);
    return rc;
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeLight_getFloatVec(JNIEnv* env, jobject obj,
                                         jlong jlight, jstring key)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    int size = light->getByteSize(char_key) / sizeof(float);
    if (size > 0)
    {
        jfloatArray jvec = env->NewFloatArray(size);
        float* elems = env->GetFloatArrayElements(jvec, 0);
        light->getFloatVec(char_key, elems, size);
        env->ReleaseFloatArrayElements(jvec, elems, 0);
        env->ReleaseStringUTFChars(key, char_key);
        return jvec;
    }
    env->ReleaseStringUTFChars(key, char_key);
    return NULL;
}

JNIEXPORT jintArray JNICALL
Java_org_gearvrf_NativeLight_getIntVec(JNIEnv* env, jobject obj,
                                            jlong jlight, jstring key)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    int size = light->getByteSize(char_key) / sizeof(int);
    if (size > 0)
    {
        jintArray jvec = env->NewIntArray(size);
        int* elems = env->GetIntArrayElements(jvec, 0);
        light->getIntVec(char_key, elems, size);
        env->ReleaseStringUTFChars(key, char_key);
        env->ReleaseIntArrayElements(jvec, elems, 0);
        return jvec;
    }
    env->ReleaseStringUTFChars(key, char_key);
    return NULL;
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeLight_getFloat(JNIEnv* env, jobject obj,
                                      jlong jlight, jstring key)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    jfloat f = 0.0f;
    light->getFloat(char_key, f);
    env->ReleaseStringUTFChars(key, char_key);
    return f;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeLight_setFloat(JNIEnv* env, jobject obj, jlong jlight,
                                      jstring key, jfloat value)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    light->setFloat(char_key, value);
    env->ReleaseStringUTFChars(key, char_key);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeLight_getInt(JNIEnv* env, jobject obj,
                                    jlong jlight, jstring key)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    int i = 0;
    light->getInt(char_key, i);
    env->ReleaseStringUTFChars(key, char_key);
    return static_cast<jint>(i);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setInt(JNIEnv* env, jobject obj,
                                         jlong jlight, jstring key, jint value)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool rc = light->setInt(char_key, value);
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}


JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setVec2(JNIEnv* env, jobject obj,
                                          jlong jlight, jstring key, jfloat x, jfloat y)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool rc = light->setVec2(char_key, glm::vec2(x, y));
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}


JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setVec3(JNIEnv* env, jobject obj,
                                          jlong jlight, jstring key, jfloat x, jfloat y,
                                          jfloat z)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool rc = light->setVec3(char_key, glm::vec3(x, y, z));
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}


JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setVec4(JNIEnv* env, jobject obj,
                                     jlong jlight, jstring key,
                                     jfloat x, jfloat y, jfloat z, jfloat w)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool rc = light->setVec4(char_key, glm::vec4(x, y, z, w));
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_setMat4(JNIEnv* env, jobject obj,
                                     jlong jlight, jstring key,
                                     jfloat x1, jfloat y1, jfloat z1, jfloat w1,
                                     jfloat x2, jfloat y2, jfloat z2, jfloat w2,
                                     jfloat x3, jfloat y3, jfloat z3, jfloat w3,
                                     jfloat x4, jfloat y4, jfloat z4, jfloat w4)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    float m[16] = {x1, y1, z1, w1, x2, y2, z2, w2, x3, y3, z3, w3, x4, y4, z4, w4};
    bool rc = light->setFloatVec(char_key, m, 16);
    env->ReleaseStringUTFChars(key, char_key);
    return rc;
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeLight_getMat4(JNIEnv* env,
                                          jobject obj, jlong jlight, jstring key)
{
    return Java_org_gearvrf_NativeLight_getFloatVec(env, obj, jlight, key);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_hasTexture(JNIEnv* env, jobject obj,
                                             jlong jlight, jstring key)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool hasKey = (light->getTexture(char_key) != NULL);
    env->ReleaseStringUTFChars(key, char_key);
    return (jboolean) hasKey;
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeLight_hasUniform(JNIEnv* env, jobject obj, jlong jlight, jstring key)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_key = env->GetStringUTFChars(key, 0);
    bool hasUniform = light->hasUniform(char_key);
    env->ReleaseStringUTFChars(key, char_key);
    return (jboolean) hasUniform;

}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_getShaderType(JNIEnv* env, jobject, jlong jlight, jstring jtype)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    const char* char_type = env->GetStringUTFChars(jtype, 0);
    const std::string& type = light->getShaderType(char_type);
    env->ReleaseStringUTFChars(jtype, char_type);
    return env->NewStringUTF(type.c_str());
}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_makeShaderLayout(JNIEnv* env, jobject obj, jlong jlight)
{
    Light* light = reinterpret_cast<Light*>(jlight);
    std::string layout;
    light->makeShaderLayout(layout);
    return env->NewStringUTF(layout.c_str());
}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeLight_makeShaderBlock(JNIEnv* env, jobject obj, jlong jscene)
{
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    const LightList& lights = scene->getLights();
    std::string layout;
    lights.makeShaderBlock(layout);
    return env->NewStringUTF(layout.c_str());
}
}
