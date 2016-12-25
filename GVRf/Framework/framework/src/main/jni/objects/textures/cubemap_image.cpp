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
 * Cube map texture made by six bitmaps.
 ***************************************************************************/

#include "cubemap_image.h"
#include "util/scope_exit.h"
#include "util/jni_utils.h"

namespace gvr {
CubemapImage::CubemapImage(JNIEnv* env, jobjectArray bitmapArray) :
            Image(GL_TEXTURE_CUBE_MAP, GL_RGBA), mBitmaps(NULL), mTextures(NULL)
{
    if (JNI_OK != env->GetJavaVM(&mJava))
    {
        FAIL("GetJavaVM failed");
    }
    if (bitmapArray != NULL)
    {
        mBitmaps = env->NewWeakGlobalRef(bitmapArray);
        mPendingUpdate = true;
    }
}

CubemapImage::CubemapImage(JNIEnv* env, int format, int width, int height, int imageSize,
        jobjectArray textureArray, const int* dataOffsets) :
        Image(GL_TEXTURE_CUBE_MAP, width, height, imageSize, format, 6),
        mBitmaps(NULL), mTextures(NULL)
{
    if (JNI_OK != env->GetJavaVM(&mJava))
    {
        FAIL("GetJavaVM failed");
    }
    setDataOffsets(dataOffsets, 6);
    if (textureArray != NULL)
    {
        mTextures = env->NewWeakGlobalRef(textureArray);
        mPendingUpdate = true;
    }
}

void CubemapImage::update(JNIEnv* env, jobjectArray bitmapArray)
{
    clearData(env);
    mBitmaps = env->NewWeakGlobalRef(bitmapArray);
    mPendingUpdate = true;
}

void CubemapImage::update(JNIEnv* env, int imageSize,
            jobjectArray textureArray, const int* textureOffset)
{
    mImageSize = imageSize;
    setDataOffsets(textureOffset, 6);
    clearData(env);
    mTextures = env->NewWeakGlobalRef(textureArray);
    mPendingUpdate = true;
}

CubemapImage::~CubemapImage()
{
    JNIEnv* env = getCurrentEnv(mJava);
    clearData(env);
}

void CubemapImage::update(int texid)
{
    if (mBitmaps != NULL)
    {
        updateFromBitmap(texid);
        mPendingUpdate = false;
    }
    else if (mTextures != NULL)
    {
        updateFromMemory(texid);
        mPendingUpdate = false;
    }
}

void CubemapImage::clearData(JNIEnv* env)
{
    if (mBitmaps != NULL)
    {
        jobject bmaps = env->NewLocalRef(mBitmaps);

        if (bmaps != NULL)
        {
            LOGV("CubemapImage::ClearData bitmap");
            env->DeleteWeakGlobalRef(mBitmaps);
            env->DeleteLocalRef(bmaps);
        }
        mBitmaps = NULL;
    }
    if (mTextures != NULL)
    {
        jobject textures = env->NewLocalRef(mTextures);
        if (textures != NULL)
        {
            LOGV("CubemapImage::ClearData texture");
            env->DeleteWeakGlobalRef(mTextures);
            env->DeleteLocalRef(textures);
        }
        mTextures = NULL;
    }
}

void CubemapImage::updateFromBitmap(int texid)
{
    JNIEnv *env = getCurrentEnv(mJava);
    jobjectArray bmapArray = static_cast<jobjectArray>(env->NewLocalRef(mBitmaps));
    if (bmapArray == NULL)
    {
        LOGE("CubemapImage::updateFromBitmap bitmap array NULL");
        return;
    }
    // Clean up upon scope exit. The SCOPE_EXIT utility is used
    // to avoid duplicated code in the throw case and normal
    // case.
    SCOPE_EXIT( clearData(env); env->DeleteLocalRef(bmapArray));
    glBindTexture(mType, texid);
    for (int i = 0; i < 6; i++)
    {
        jobject bitmap = env->NewLocalRef(env->GetObjectArrayElement(bmapArray, i));
        AndroidBitmapInfo info;
        void *pixels;
        int ret;

        if (bitmap == NULL)
        {
            LOGE("CubemapImage::updateFromBitmap bitmap %d is NULL", i);
        }
        else if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0)
        {
            LOGE("CubemapImage::updateFromBitmap AndroidBitmap_getInfo() failed! error = %d", ret);
        }
        else if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
        {
            LOGE("CubemapImage::updateFromBitmap AndroidBitmap_lockPixels() failed! error = %d", ret);
        }
        else
        {
            if (i == 0)
            {
                mWidth = info.width;
                mHeight = info.height;
                mImageSize = info.height * info.stride;
            }
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA,
                         info.width, info.height,
                         0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            AndroidBitmap_unlockPixels(env, bitmap);
        }
        env->DeleteLocalRef(bitmap);
    }
}

void CubemapImage::updateFromMemory(int texid)
{
    JNIEnv *env = getCurrentEnv(mJava);
    jobjectArray texArray = static_cast<jobjectArray>(env->NewLocalRef(mTextures));
    if (texArray == NULL)
    {
        LOGE("CubemapImage::updateFromMemory texture array NULL");
        return;
    }

    // Clean up upon scope exit
    SCOPE_EXIT( clearData(env); env->DeleteLocalRef(texArray); );

    glBindTexture(mType, texid);
    for (int i = 0; i < 6; i++)
    {
        jbyteArray byteArray = static_cast<jbyteArray>(env->NewLocalRef(texArray));
        jbyte *textureData = env->GetByteArrayElements(byteArray, 0);

        if (byteArray == NULL)
        {
            LOGE("CubemapImage::updateFromMemory texture %d is NULL", i);
        }
        else
        {
            glCompressedTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, mFormat, mWidth,
                                   mWidth, 0, mImageSize, textureData + getDataOffset(i));
            env->ReleaseByteArrayElements(byteArray, textureData, 0);
        }
        env->DeleteLocalRef(byteArray);
    }
}

}

