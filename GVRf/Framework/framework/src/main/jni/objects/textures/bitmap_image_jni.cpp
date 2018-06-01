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
#include "bitmap_image.h"
#include "util/gvr_jni.h"
#include "util/gvr_java_stack_trace.h"
#include "android/asset_manager_jni.h"


namespace gvr
{
    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeBitmapImage_constructor(JNIEnv *env, jobject obj,
                                                   int imageType, int pixelFormat);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_setFileName(JNIEnv *env, jobject obj,
                                                   jlong jtexture, jstring jfile);

    JNIEXPORT jstring JNICALL
    Java_org_gearvrf_NativeBitmapImage_getFileName(JNIEnv *env, jobject obj, jlong jtexture);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_updateFromMemory(JNIEnv *env, jobject obj,
                                                        jlong jtexture, jint width,
                                                        jint height, jbyteArray jdata);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_updateCompressed(JNIEnv *env, jobject obj,
                                                        jlong jtexture, jint width, int height, jint imageSize,
                                                        jbyteArray jdata, jint levels, jintArray offset);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_updateFromBitmap(JNIEnv *env, jobject obj,
                                                        jlong jtexture, jobject jbitmap,
                                                        jboolean hasAlpha, jstring format);
    }

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeBitmapImage_constructor(JNIEnv *env, jobject obj, jint type, jint format)
    {
        Image *image = Renderer::getInstance()->createImage(type, format);
        jlong result = reinterpret_cast<jlong>(image);
        return result;
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_updateFromMemory(JNIEnv *env, jobject obj,
                                                        jlong jtexture, jint width, jint height,
                                                        jbyteArray jdata)
    {
        jobject keep = env->NewLocalRef(jdata);
        BitmapImage *texture = reinterpret_cast<BitmapImage *>(jtexture);
        texture->update(env, width, height, jdata);
        env->DeleteLocalRef(keep);
    }


    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_updateFromBitmap(JNIEnv *env, jobject obj,
                                                        jlong jtexture, jobject jbitmap,
                                                        jboolean hasAlpha, jstring format)
    {
        BitmapImage *texture = reinterpret_cast<BitmapImage *>(jtexture);
        const char* format_name = env->GetStringUTFChars(format, 0);

        int form = GL_RGBA;

        if(strcmp(format_name, "ALPHA_8") == 0)
            form = GL_R8;
        else if (strcmp(format_name, "RGB_565") == 0)
            form = GL_RGB565;
        else if (strcmp(format_name, "ARGB_4444") == 0)
            form = GL_RGBA4;
        else if (strcmp(format_name, "ARGB_8888") == 0)
            form = GL_RGBA8;
        else if (strcmp(format_name, "RGBA_F16") == 0)
            form = GL_RGBA16F;

        texture->update(env, jbitmap, static_cast<bool>(hasAlpha), form );
        env->ReleaseStringUTFChars(format, format_name);

    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_updateFromBuffer(JNIEnv *env, jobject obj,
                                                        jlong jtexture, jint xoffset, jint yoffset,
                                                        jint width, jint height,
                                                        jint format, jint type, jobject jbuffer)
    {
        BitmapImage *texture = reinterpret_cast<BitmapImage *>(jtexture);
        texture->update(env, xoffset, yoffset, width, height, format, type, jbuffer);
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_updateCompressed(JNIEnv *env, jobject obj,
                                                        jlong jtexture, jint width, jint height, jint imageSize,
                                                        jbyteArray jdata, jint levels, jintArray joffsets)
    {
        LOGV("updateCompressed: data = %p, offsets = %p", jdata, joffsets);
        jobject keep1 = env->NewLocalRef(jdata);
        jobject keep2 = env->NewLocalRef(joffsets);
        BitmapImage *texture = reinterpret_cast<BitmapImage *>(jtexture);
        jintArray array = static_cast<jintArray>(env->NewLocalRef(joffsets));
        int* offsets = env->GetIntArrayElements(array, 0);
        texture->update(env, width, height, imageSize, jdata, levels, offsets);
        env->ReleaseIntArrayElements(array, offsets, 0);
        env->DeleteLocalRef(keep1);
        env->DeleteLocalRef(keep2);
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBitmapImage_setFileName(JNIEnv *env, jobject obj,
                                                   jlong jtexture, jstring jfile)
    {
        BitmapImage* bmap = reinterpret_cast<BitmapImage *>(jtexture);
        const char* char_name = env->GetStringUTFChars(jfile, 0);
        bmap->setFileName(char_name);
        env->ReleaseStringUTFChars(jfile, char_name);
    }

    JNIEXPORT jstring JNICALL
    Java_org_gearvrf_NativeBitmapImage_getFileName(JNIEnv *env, jobject obj, jlong jtexture)
    {
        BitmapImage* bmap = reinterpret_cast<BitmapImage *>(jtexture);
        const char* fname = bmap->getFileName();
        return env->NewStringUTF(fname);
    }
}