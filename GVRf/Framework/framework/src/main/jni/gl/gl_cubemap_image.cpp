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

#include "gl/gl_cubemap_image.h"

namespace gvr {
void GLCubemapImage::update(int texid)
{
    if (mJava == NULL)
    {
        return;
    }
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

void GLCubemapImage::updateFromBitmap(int texid)
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
    int t = getTarget();
    glBindTexture(t, texid);
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

void GLCubemapImage::updateFromMemory(int texid)
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
    int t = getTarget();

    glBindTexture(t, texid);
    for (int i = 0; i < 6; i++)
    {
        jbyteArray byteArray = static_cast<jbyteArray>(env->GetObjectArrayElement(texArray, i));
        jbyte* pixels = env->GetByteArrayElements(byteArray, 0);

        if (byteArray == NULL)
        {
            LOGE("CubemapImage::updateFromMemory texture %d is NULL", i);
        }
        else
        {
            glCompressedTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, mFormat, mWidth,
                                   mHeight, 0, mImageSize, pixels + getDataOffset(i));
            env->ReleaseByteArrayElements(byteArray, pixels, 0);
        }
        env->DeleteLocalRef(byteArray);
    }
}

}
