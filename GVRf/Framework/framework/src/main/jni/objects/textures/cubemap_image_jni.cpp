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
#include "cubemap_image.h"
#include "util/gvr_jni.h"
#include "util/gvr_java_stack_trace.h"
#include "android/asset_manager_jni.h"


namespace gvr {
    extern "C" {
    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeCubemapImage_update(JNIEnv * env, jobject obj, jlong jcubemap, jobjectArray bitmapArray);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeCubemapImage_updateCompressed(JNIEnv * env, jobject obj, jlong jcubemap,
                                               jint width, jint height, jint imageSize, jobjectArray textureArray, jintArray joffsetArray);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCubemapImage_update(JNIEnv * env,
                                           jobject obj, jlong jcubemap, jobjectArray bitmapArray)
{
    jobject keep = env->NewLocalRef(bitmapArray);
    CubemapImage* cubemap = reinterpret_cast<CubemapImage*>(jcubemap);
    cubemap->update(env, bitmapArray);
    env->DeleteLocalRef(keep);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCubemapImage_updateCompressed(JNIEnv* env,
                                           jobject obj, jlong jcubemap,
                                           jint width, jint height, jint imageSize,
                                           jobjectArray textureArray, jintArray joffsetArray)
{
    jobject keep2 = env->NewLocalRef(joffsetArray);
    if (env->GetArrayLength(joffsetArray) != 6)
    {
        LOGE("CubeMapImage: cannot create: Texture offset list's length is not 6.");
        env->DeleteLocalRef(keep2);
        return;
    }
    jobject keep1 = env->NewLocalRef(textureArray);
    jint* offsets = env->GetIntArrayElements(joffsetArray, 0);
    CubemapImage* cubemap = reinterpret_cast<CubemapImage*>(jcubemap);
    cubemap->update(env, width, height, imageSize, textureArray, offsets);
    env->ReleaseIntArrayElements(joffsetArray, offsets, 0);
    env->DeleteLocalRef(keep1);
    env->DeleteLocalRef(keep2);
}

}
