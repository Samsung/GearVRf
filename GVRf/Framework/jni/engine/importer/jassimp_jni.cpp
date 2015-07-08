/*
 ---------------------------------------------------------------------------
 Open Asset Import Library
 ---------------------------------------------------------------------------
 Copyright (c) 2006-2012, assimp team
 All rights reserved.
 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:
 * Redistributions of source code must retain the above
 copyright notice, this list of conditions and the
 following disclaimer.
 * Redistributions in binary form must reproduce the above
 copyright notice, this list of conditions and the
 following disclaimer in the documentation and/or other
 materials provided with the distribution.
 * Neither the name of the assimp team, nor the names of its
 contributors may be used to endorse or promote products
 derived from this software without specific prior
 written permission of the assimp team.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ---------------------------------------------------------------------------
 */

#include "assimp_importer.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getVKeysize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getQKeysize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getV3Dsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getfloatsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getintsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getuintsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getdoublesize(JNIEnv *env,
        jclass cls);
JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getlongsize(JNIEnv *env,
        jclass cls);
JNIEXPORT jstring JNICALL
Java_org_gearvrf_jassimp_Jassimp_getErrorString(
        JNIEnv *env, jclass cls);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getVKeysize(JNIEnv *env,
    jclass cls) {
    const int res = sizeof(aiVectorKey);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getQKeysize(JNIEnv *env,
    jclass cls) {
    const int res = sizeof(aiQuatKey);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getV3Dsize(JNIEnv *env,
    jclass cls) {
    const int res = sizeof(aiVector3D);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getfloatsize(JNIEnv *env,
    jclass cls) {
    const int res = sizeof(float);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getintsize(JNIEnv *env,
    jclass cls) {
    const int res = sizeof(int);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getuintsize(JNIEnv *env,
    jclass cls) {
    const int res = sizeof(unsigned int);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getdoublesize(JNIEnv *env,
    jclass cls) {
    const int res = sizeof(double);
    return res;
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_jassimp_Jassimp_getlongsize(JNIEnv *env,
    jclass cls) {
    const int res = sizeof(long);
    return res;
}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_jassimp_Jassimp_getErrorString(
    JNIEnv *env, jclass cls) {
    const char *err = aiGetErrorString();

    if (NULL == err) {
        return env->NewStringUTF("");
    }

    return env->NewStringUTF(err);
}
}
