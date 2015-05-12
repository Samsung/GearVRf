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

#include "compressed_texture.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
// Note that "asynchronous_textures" becomes "asynchronous_1textures"
Java_org_gearvrf_asynchronous_NativeCompressedTexture_normalConstructor(JNIEnv * env,
        jobject obj, jint target, jint internalFormat,
        jint width, jint height, jint imageSize, jbyteArray bytes);

JNIEXPORT jlong JNICALL
Java_org_gearvrf_asynchronous_1textures_NativeCompressedTexture_mipmappedConstructor(JNIEnv * env,
        jobject obj, jint target);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_asynchronous_NativeCompressedTexture_normalConstructor(JNIEnv * env,
    jobject obj, jint target, jint internalFormat,
    jint width, jint height, jint imageSize, jbyteArray bytes) {

    jbyte* data = env->GetByteArrayElements(bytes, 0);

    CompressedTexture* texture =
            new CompressedTexture(target, internalFormat, width, height, imageSize, data);

    env->ReleaseByteArrayElements(bytes, data, 0);

    return reinterpret_cast<jlong>(texture);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_asynchronous_1textures_NativeCompressedTexture_mipmappedConstructor(JNIEnv * env,
    jobject obj, jint target) {
    return reinterpret_cast<jlong>(new CompressedTexture(target));
}

}
