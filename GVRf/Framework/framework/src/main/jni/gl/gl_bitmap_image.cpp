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
#include <gvr_gl.h>
#include "gl/gl_bitmap_image.h"
namespace gvr {

int GLBitmapImage::updateFromBitmap(JNIEnv *env, int target, jobject bitmap, bool mipmap, int internalFormat)
{
    AndroidBitmapInfo info;
    void *pixels;
    int ret;

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
        LOGE("BitmapImage::updateFromBitmap AndroidBitmap_lockPixels() failed! error = %d",
             ret);
        return 0;
    }
    else
    {
        int dataFormat = GL_UNSIGNED_BYTE;
        int pixelFormat = GL_RGBA;
        switch (internalFormat)
        {
            case GL_RGB565:
                dataFormat = GL_UNSIGNED_SHORT_5_6_5;
                pixelFormat = GL_RGB;
                break;

            case GL_RGBA4:
                dataFormat = GL_UNSIGNED_SHORT_4_4_4_4;
                break;

            case GL_R8:
                pixelFormat = GL_RED;
                break;

            case GL_RGBA16F:
                dataFormat = GL_HALF_FLOAT;
                break;
        }
        glTexImage2D(target, 0, internalFormat, info.width, info.height, 0, pixelFormat,
                     dataFormat, pixels);
        if(mipmap)
            glGenerateMipmap(target);

        AndroidBitmap_unlockPixels(env, bitmap);
        return internalFormat;
    }
    return 0;
}

void GLBitmapImage::updateFromBuffer(JNIEnv *env, int target, jobject pixels)
{
    void* directPtr = env->GetDirectBufferAddress(pixels);
    glTexSubImage2D(target, 0, mXOffset, mYOffset, mWidth, mHeight, mFormat, mType, directPtr);
}

void GLBitmapImage::update(int texid)
{
    if (mJava == NULL)
    {
        return;
    }
    if (mBitmap != NULL)
    {
        updateFromBitmap(texid);
        clearData(getCurrentEnv(mJava));
    }
    else if (mData != NULL)
    {
        updateFromMemory(texid);
        clearData(getCurrentEnv(mJava));
    }
}

void GLBitmapImage::updateFromMemory(int texid)
{
    JNIEnv *env = getCurrentEnv(mJava);

    if (mData == NULL)
    {
        LOGE("BitmapImage::updateFromMemory array is null");
        return;
    }
    jbyte* pixels = env->GetByteArrayElements(mData, 0);
    if (mIsCompressed)
    {
        if (mLevels > 1)
        {
            loadCompressedMipMaps(pixels, mFormat);
        }
        else
        {
            glCompressedTexImage2D(mGLTarget, 0, mFormat, mWidth, mHeight, 0,
                                   mImageSize, pixels + getDataOffset(0));
        }
    }
    else
    {
        glTexImage2D(mGLTarget, 0, GL_LUMINANCE, mWidth, mHeight, 0, GL_LUMINANCE,
                     GL_UNSIGNED_BYTE, pixels);
        glGenerateMipmap(mGLTarget);
    }
    checkGLError("GLBitmapImage::updateFromMemory");
    env->ReleaseByteArrayElements(mData, pixels, 0);
    clearData(env);
}

void GLBitmapImage::updateFromBitmap(int texid)
{
    JNIEnv *env = getCurrentEnv(mJava);

    if (mBitmap == NULL)
    {
        LOGE("BitmapImage::updateFromBitmap bitmap is null");
        return;
    }
    if (mIsBuffer)
    {
        updateFromBuffer(env, mGLTarget, mBitmap);
    } else {
        bool mipmap = false;
        if(!mIsCompressed && mTexParams.getMinFilter() >=  TextureParameters::NEAREST_MIPMAP_NEAREST)
            mipmap = true;
        updateFromBitmap(env, mGLTarget, mBitmap, mipmap, mFormat);
    }
    checkGLError("GLBitmapImage::updateFromBitmap");
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
        glCompressedTexImage2D(mGLTarget, level, format, width, height, levelOffset, levelSize,
                               data);
    }
}

}
