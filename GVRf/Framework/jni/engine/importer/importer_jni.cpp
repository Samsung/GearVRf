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

#include "importer.h"

#include <fstream>

#include "android/asset_manager_jni.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeImporter_readFileFromAssets(JNIEnv * env,
        jobject obj, jobject asset_manager, jstring filename, jint settings);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeImporter_readFileFromSDCard(JNIEnv * env,
        jobject obj, jstring filename, jint settings);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeImporter_readFromByteArray(JNIEnv * env,
        jobject obj, jbyteArray bytes, jstring filename, jint settings);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeImporter_readFileFromAssets(JNIEnv * env,
        jobject obj, jobject asset_manager, jstring filename, jint settings) {
    const char* native_string = env->GetStringUTFChars(filename, 0);
    AAssetManager* mgr = AAssetManager_fromJava(env, asset_manager);
    AAsset* asset = AAssetManager_open(mgr, native_string, AASSET_MODE_UNKNOWN);
    if (NULL == asset) {
        LOGE("_ASSET_NOT_FOUND_");
        return JNI_FALSE;
    }
    long size = AAsset_getLength(asset);
    char* buffer = (char*) malloc(sizeof(char) * size);
    AAsset_read(asset, buffer, size);

    AssimpImporter* assimp_scene = Importer::readFileFromAssets(
            buffer, size, native_string, static_cast<int>(settings));

    AAsset_close(asset);

    free(buffer);

    env->ReleaseStringUTFChars(filename, native_string);

    return reinterpret_cast<jlong>(assimp_scene);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeImporter_readFromByteArray(JNIEnv * env,jobject obj,
        jbyteArray bytes, jstring filename, jint settings) {
    jbyte* data = env->GetByteArrayElements(bytes, 0);
    int length = static_cast<int>(env->GetArrayLength(bytes));
    const char* native_string = env->GetStringUTFChars(filename, 0);

    AssimpImporter* assimp_scene = Importer::readFileFromAssets(
            (char*)data, length, native_string, static_cast<int>(settings));

    env->ReleaseByteArrayElements(bytes, data, 0);
    env->ReleaseStringUTFChars(filename, native_string);

    return reinterpret_cast<jlong>(assimp_scene);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeImporter_readFileFromSDCard(JNIEnv * env,
        jobject obj, jstring filename, jint settings) {
    const char* native_string = env->GetStringUTFChars(filename, 0);
    AssimpImporter* assimp_scene = Importer::readFileFromSDCard(native_string, static_cast<int>(settings));

    env->ReleaseStringUTFChars(filename, native_string);

    return reinterpret_cast<jlong>(assimp_scene);
}

}
