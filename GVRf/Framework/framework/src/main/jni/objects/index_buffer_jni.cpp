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

#include <engine/renderer/renderer.h>
#include "objects/index_buffer.h"

#include "util/gvr_log.h"
#include "util/gvr_jni.h"

namespace gvr {
    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeIndexBuffer_ctor(JNIEnv* env, jobject obj,
                                            int bytesPerIndex, int vertexCount);
    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeIndexBuffer_getIntVec(JNIEnv* env, jobject obj, jlong jibuf, jobject data);

    JNIEXPORT jintArray JNICALL
    Java_org_gearvrf_NativeIndexBuffer_getIntArray(JNIEnv* env, jobject obj, jlong jibuf);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeIndexBuffer_setIntArray(JNIEnv* env, jobject obj, jlong jibuf, jintArray data);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeIndexBuffer_setIntVec(JNIEnv* env, jobject obj, jlong jibuf, jobject jintbuf);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeIndexBuffer_getShortVec(JNIEnv* env, jobject obj, jlong jibuf, jobject jshortbuf);

    JNIEXPORT jcharArray JNICALL
    Java_org_gearvrf_NativeIndexBuffer_getShortArray(JNIEnv* env, jobject obj, jlong jibuf);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeIndexBuffer_setShortArray(JNIEnv* env, jobject obj, jlong jibuf, jcharArray data);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeIndexBuffer_setShortVec(JNIEnv* env, jobject obj, jlong jibuf, jobject jshortbuf);

    JNIEXPORT int JNICALL
    Java_org_gearvrf_NativeIndexBuffer_getIndexSize(JNIEnv* env, jobject obj, jlong jibuf);

    JNIEXPORT int JNICALL
    Java_org_gearvrf_NativeIndexBuffer_getIndexCount(JNIEnv* env, jobject obj, jlong jibuf);

};

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeIndexBuffer_ctor(JNIEnv* env, jobject obj, int bytesPerVertex, int vertexCount)
{
    IndexBuffer* ibuf = Renderer::getInstance()->createIndexBuffer(bytesPerVertex, vertexCount);
    return reinterpret_cast<jlong>(ibuf);
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeIndexBuffer_getShortVec(JNIEnv * env, jobject obj, jlong jibuf, jobject jshortbuf)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    void* bufptr = env->GetDirectBufferAddress(jshortbuf);
    bool rc = false;
    if (bufptr)
    {
        int capacity = env->GetDirectBufferCapacity(jshortbuf);
        rc = ibuf->getShortVec((unsigned short*) bufptr, capacity);
    }
    return rc;
}


JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeIndexBuffer_getIntVec(JNIEnv * env, jobject obj, jlong jibuf, jobject jdata)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    void* bufptr = env->GetDirectBufferAddress(jdata);
    bool rc = false;
    if (bufptr)
    {
        int capacity = env->GetDirectBufferCapacity(jdata);
        rc = ibuf->getIntVec((unsigned int*) bufptr, capacity);
    }
    return rc;
}

JNIEXPORT jintArray JNICALL
Java_org_gearvrf_NativeIndexBuffer_getIntArray(JNIEnv* env, jobject obj, jlong jibuf)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    int n = ibuf->getIndexCount();
    jintArray jdata = env->NewIntArray(n);
    unsigned int* data = reinterpret_cast<unsigned int*>(env->GetIntArrayElements(jdata, 0));
    ibuf->getIntVec(data, n);
    env->ReleaseIntArrayElements(jdata, reinterpret_cast<jint*>(data), 0);
    return jdata;
}

JNIEXPORT jcharArray JNICALL
Java_org_gearvrf_NativeIndexBuffer_getShortArray(JNIEnv* env, jobject obj, jlong jibuf)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    jchar n = ibuf->getIndexCount();
    jcharArray jdata = env->NewCharArray(n);
    unsigned short* data = reinterpret_cast<unsigned short*>(env->GetCharArrayElements(jdata, 0));
    ibuf->getShortVec(data, n);
    env->ReleaseCharArrayElements(jdata, reinterpret_cast<jchar*>(data), 0);
    return jdata;
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeIndexBuffer_setShortArray(JNIEnv * env, jobject obj, jlong jibuf, jcharArray jdata)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    jchar* data = env->GetCharArrayElements(jdata, 0);
    bool rc = ibuf->setShortVec(data, static_cast<int>(env->GetArrayLength(jdata)));
    env->ReleaseCharArrayElements(jdata, data, 0);
    return rc;
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeIndexBuffer_setShortVec(JNIEnv * env, jobject obj, jlong jibuf, jobject jshortbuf)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    void* bufptr = env->GetDirectBufferAddress(jshortbuf);
    bool rc = false;
    if (bufptr)
    {
        jlong capacity = env->GetDirectBufferCapacity(jshortbuf);
        rc = ibuf->setShortVec((unsigned short*) bufptr, capacity);
    }
    return rc;
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeIndexBuffer_setIntVec(JNIEnv* env, jobject obj, jlong jibuf, jobject jintbuf)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    void* bufptr = env->GetDirectBufferAddress(jintbuf);
    bool rc = false;
    if (bufptr)
    {
        jlong capacity = env->GetDirectBufferCapacity(jintbuf);
        rc = ibuf->setIntVec((unsigned int*) bufptr, capacity);
    }
    return rc;
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeIndexBuffer_setIntArray(JNIEnv * env, jobject obj, jlong jibuf, jintArray jdata)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    unsigned int* data = reinterpret_cast<unsigned int*>(env->GetIntArrayElements(jdata, 0));
    bool rc = ibuf->setIntVec(data, static_cast<int>(env->GetArrayLength(jdata)));
    env->ReleaseIntArrayElements(jdata, reinterpret_cast<jint*>(data), 0);
    return rc;
}


JNIEXPORT int JNICALL
Java_org_gearvrf_NativeIndexBuffer_getIndexCount(JNIEnv* env, jobject obj, jlong jibuf)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    return ibuf->getIndexCount();
}

JNIEXPORT int JNICALL
Java_org_gearvrf_NativeIndexBuffer_getIndexSize(JNIEnv* env, jobject obj, jlong jibuf)
{
    IndexBuffer* ibuf = reinterpret_cast<IndexBuffer*>(jibuf);
    return ibuf->getIndexSize();
}

}
