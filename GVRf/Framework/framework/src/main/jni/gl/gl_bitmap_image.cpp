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
 * Texture from a (Java-loaded) byte stream containing a compressed texture
 ***************************************************************************/

#include <android/bitmap.h>
#include "gl/gl_bitmap_image.h"

namespace gvr {
int GLBitmapImage::MapFormat[7] = { GL_RGBA8, GL_LUMINANCE, GL_RGB8, GL_RGBA8, GL_RGB565, GL_RGBA4, GL_RG };

int GLBitmapImage::updateFromBitmap(JNIEnv *env, int target, jobject bitmap)
{
    AndroidBitmapInfo info;
    void *pixels;
    int ret;
    int imageFormat = 0;

    if (bitmap == NULL)
    {
        LOGE("BitmapImage::updateFromBitmap bitmap is NULL");
    }
    else if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0)
    {
        LOGE("BitmapImage::updateFromBitmap AndroidBitmap_getInfo() failed! error = %d", ret);
    }
    else if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
    {
        LOGE("BitmapImage::updateFromBitmap AndroidBitmap_lockPixels() failed! error = %d", ret);
        return 0;
    }
    else
    {
        int dataFormat = GL_UNSIGNED_BYTE;
        int pixelFormat = GL_RGBA;
        int internalFormat = GL_RGBA;
        switch (info.format)
        {
            case ANDROID_BITMAP_FORMAT_RGB_565:
                internalFormat = GL_RGB565;
                dataFormat = GL_UNSIGNED_SHORT_5_6_5;
                pixelFormat = GL_RGB;
                imageFormat = Image::PixelFormat::RGB_5;
                break;

            case ANDROID_BITMAP_FORMAT_RGBA_4444:
                internalFormat = GL_RGBA4;
                dataFormat = GL_UNSIGNED_SHORT_4_4_4_4;
                imageFormat = Image::PixelFormat::RGBA_4;
                break;

            case ANDROID_BITMAP_FORMAT_A_8:
                dataFormat = GL_R8;
                pixelFormat = GL_RED;
                imageFormat = Image::PixelFormat::A8;
                break;
        }
        glTexImage2D(target, 0, internalFormat,
                     info.width, info.height,
                     0, pixelFormat, dataFormat, pixels);
        AndroidBitmap_unlockPixels(env, bitmap);
        return internalFormat;
    }
    return imageFormat;
}


void GLBitmapImage::update(int texid)
{
    LOGE("in update");
    if (mJava == NULL)
    {
        return;
    }
    if (mBitmap != NULL)
    {
        LOGE("in update 1");
        updateFromBitmap(texid);
    }
    else if (mData != NULL)
    {
        LOGE("in update 2" );
        updateFromMemory(texid);
    }
    else
    {
        return;
    }
    clearData(getCurrentEnv(mJava));
    mPendingUpdate = false;
}

void GLBitmapImage::updateFromMemory(int texid)
{
    JNIEnv *env = getCurrentEnv(mJava);
    jbyteArray array = static_cast<jbyteArray>(env->NewLocalRef(mData));
    if (array == NULL)
    {
        LOGE("BitmapImage::updateFromMemory array is null");
        return;
    }
    jbyte* pixels = env->GetByteArrayElements(array, 0);
    int t = getTarget();
    glBindTexture(t, texid);
    LOGE("image size is %d ", mImageSize);
    if (mIsCompressed)
    {
        if (mLevels > 1)
        {
            loadCompressedMipMaps(pixels, mFormat);
        }
        else
        {
            glCompressedTexImage2D(t, 0, mFormat, mWidth, mHeight, 0,
                                   mImageSize, pixels + getDataOffset(0));
            glGenerateMipmap(t);
        }
    }
    else
    {
        glTexImage2D(t, 0, GL_LUMINANCE, mWidth, mHeight, 0, GL_LUMINANCE,
                     GL_UNSIGNED_BYTE, pixels);
        glGenerateMipmap(t);
    }
    env->ReleaseByteArrayElements(array, pixels, 0);
    env->DeleteLocalRef(array);
    clearData(env);
}

void GLBitmapImage::updateFromBitmap(int texid)
{
    JNIEnv *env = getCurrentEnv(mJava);
    jobject bitmap = env->NewLocalRef(mBitmap);
    if (bitmap == NULL)
    {
        LOGE("BitmapImage::updateFromBitmap bitmap is null");
        return;
    }
    int t = getTarget();
    glBindTexture(t, texid);
    mFormat = updateFromBitmap(env, t, bitmap);
    env->DeleteLocalRef(bitmap);
}

void GLBitmapImage::loadCompressedMipMaps(jbyte *data, int format)
{
    for (int level = 0; level < mLevels; ++level)
    {
        int levelOffset = getDataOffset(level);
        int levelSize = getDataOffset(level + 1) - levelOffset;
        int width = mWidth >> level;
        int height = mHeight >> level;
        if (width < 1) width = 1;
        if (height < 1) height = 1;
        glCompressedTexImage2D(getTarget(), level, format, width, height, levelOffset, levelSize,
                               data);
    }
}
}
