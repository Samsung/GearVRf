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
#include "compressed_image.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_asynchronous_NativeCompressedTexture_constructor(JNIEnv *env,
              jobject obj, jint target,
              jint format, jint width,
              jint height, jint levels);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_asynchronous_NativeCompressedTexture_update(JNIEnv *env,
              jobject obj, jobject jtexture,
              jint width, jint height,
              jbyteArray bytes,
              jintArray dataOffsets);
};

JNIEXPORT void JNICALL
Java_org_gearvrf_asynchronous_NativeCompressedTexture_update(JNIEnv * env,
    jobject obj, jobject jtexture,
    jint width, jint height, jbyteArray bytes, jintArray jDataOffsets)
{
    jint* dataOffsets = env->GetIntArrayElements(jDataOffsets,0);
    CompressedImage* texture = reinterpret_cast<CompressedImage*>(jtexture);
    texture->update(env, width, height, bytes, env->GetArrayLength(jDataOffsets), dataOffsets);
    env->ReleaseIntArrayElements(jDataOffsets, dataOffsets, 0);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_asynchronous_NativeCompressedTexture_constructor(JNIEnv * env,
          jobject obj, jint target,
          jint format, jint width,
          jint height, jint levels)
{
    CompressedImage* texture = Renderer::getInstance()->createCompressedBitmap(env, target, format,
                                                       width, height, levels);
    return reinterpret_cast<jlong>(texture);
}
}
