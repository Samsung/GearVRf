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
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeCubemapImage_bitmapArrayConstructor(JNIEnv * env,
            jobject obj, jobjectArray bitmapArray);
    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeCubemapImage_update(JNIEnv * env,
             jobject obj, jobject jcubemap, jobjectArray bitmapArray);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeCubemapImage_bitmapArrayConstructor(JNIEnv * env,
    jobject obj, jobjectArray bitmapArray)
{
    if (env->GetArrayLength(bitmapArray) != 6)
    {
        std::string error =
        "new CubemapImage() failed! Input bitmapList's length is not 6.";
        throw error;
    }
    jlong result = 0;
    try
    {
        CubemapImage* image = Renderer::getInstance()->createCubemap(env, bitmapArray);
        result = reinterpret_cast<jlong>(image);
    }
    catch (const std::string &err)
    {
        printJavaCallStack(env, err);
        throw err;
    }
    return result;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeCubemapImage_update(JNIEnv * env,
    jobject obj, jobject jcubemap, jobjectArray bitmapArray)
{
    CubemapImage* cubemap = reinterpret_cast<CubemapImage*>(jcubemap);
    cubemap->update(env, bitmapArray);
}

}
