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

#include "base_texture.h"
#include "png_loader.h"
#include "util/gvr_jni.h"
#include "util/gvr_java_stack_trace.h"
#include "android/asset_manager_jni.h"

#include <png.h>

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBaseTexture_bitmapConstructor(JNIEnv * env,
        jobject obj, jobject bitmap);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBaseTexture_fileConstructor(JNIEnv * env,
        jobject obj, jobject asset_manager, jstring filename);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBaseTexture_bareConstructor(JNIEnv * env, jobject obj);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeBaseTexture_update(JNIEnv * env, jobject obj,
        jlong jtexture, jint width, jint height, jbyteArray jdata);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBaseTexture_bitmapConstructor(JNIEnv * env,
        jobject obj, jobject bitmap) {
    try {
        jlong res_texture = reinterpret_cast<jlong>(new std::shared_ptr<
                BaseTexture>(new BaseTexture(env, bitmap)));
        return res_texture;
    } catch (const std::string &err) {
        printJavaCallStack(env, err);
        throw err;
    }
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBaseTexture_fileConstructor(JNIEnv * env,
        jobject obj, jobject asset_manager, jstring filename) {

    const char* native_string = env->GetStringUTFChars(filename, 0);
    AAssetManager* mgr = AAssetManager_fromJava(env, asset_manager);
    AAsset* asset = AAssetManager_open(mgr, native_string, AASSET_MODE_UNKNOWN);
    if (NULL == asset) {
        LOGE("_ASSET_NOT_FOUND_");
        return JNI_FALSE;
    }

    PngLoader loader;
    loader.loadFromAsset(asset);

    AAsset_close(asset);
    env->ReleaseStringUTFChars(filename, native_string);

    if (loader.pOutImage.bits == NULL) {
        LOGE("PNG decoder failed");
        return JNI_FALSE;
    }

    if (loader.pOutImage.format != PngLoader::RGBAFormat) {
        LOGE("Only RGBA format supported");
        return JNI_FALSE;
    }

    int imgW = loader.pOutImage.width;
    int imgH = loader.pOutImage.height;
    unsigned char *pixels = loader.pOutImage.bits;
    return reinterpret_cast<jlong>(new std::shared_ptr<BaseTexture>(
            new BaseTexture(imgW, imgH, pixels)));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBaseTexture_bareConstructor(JNIEnv * env, jobject obj) {
    return reinterpret_cast<jlong>(new std::shared_ptr<BaseTexture>(
            new BaseTexture()));
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeBaseTexture_update(JNIEnv * env, jobject obj,
        jlong jtexture, jint width, jint height, jbyteArray jdata) {
    std::shared_ptr<BaseTexture> texture = *reinterpret_cast<std::shared_ptr<
            BaseTexture>*>(jtexture);
    jbyte* data = env->GetByteArrayElements(jdata, 0);
    jboolean result = texture->update(width, height, data);
    env->ReleaseByteArrayElements(jdata, data, 0);
    return result;
}

}
