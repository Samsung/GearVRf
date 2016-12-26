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

#ifndef GL_BITMAP_IMAGE_H
#define GL_BITMAP_IMAGE_H

#include "objects/textures/bitmap_image.h"

namespace gvr {
class GLBitmapImage : public BitmapImage
{
public:

    explicit GLBitmapImage(JNIEnv* env, int width, int height, jbyteArray data) :
            BitmapImage(env, width, height, data)
    { }

    explicit GLBitmapImage(JNIEnv* env, int format, jobject bitmap) :
            BitmapImage(env, format, bitmap)
    { }

protected:
    virtual void update(int texid)
    {
        if (mBitmap != NULL)
        {
            updateFromBitmap(texid);
        }
        else if (mData != NULL)
        {
            updateFromMemory(texid);
        }
        else
        {
            return;
        }
        clearData(getCurrentEnv(mJava));
        mPendingUpdate = false;
    }

    void updateFromMemory(int texid)
    {
        JNIEnv *env = getCurrentEnv(mJava);
        jbyteArray array = static_cast<jbyteArray>(env->NewLocalRef(mData));
        jbyte* pixels = env->GetByteArrayElements(array, 0);
        glBindTexture(mType, texid);
        glTexImage2D(mType, 0, GL_LUMINANCE, mWidth, mHeight, 0, GL_LUMINANCE,
                     GL_UNSIGNED_BYTE, pixels);
        glGenerateMipmap(mType);
        env->ReleaseByteArrayElements(array, pixels, 0);
        env->DeleteLocalRef(array);
        clearData(env);
    }

    void updateFromBitmap(int texid)
    {
        JNIEnv *env = getCurrentEnv(mJava);
        jobject bitmap = env->NewLocalRef(mBitmap);
        glBindTexture(mType, texid);
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
            LOGE("BitmapImage::updateFromBitmap AndroidBitmap_lockPixels() failed! error = %d", ret);
        }
        else
        {
            mWidth = info.width;
            mHeight = info.height;
            mImageSize = info.height * info.stride;
            int dataFormat = GL_UNSIGNED_BYTE;
            int pixelFormat = GL_RGBA;
            int internalFormat = GL_RGBA;
            switch (info.format)
            {
                case ANDROID_BITMAP_FORMAT_RGB_565:
                    internalFormat = GL_RGB565;
                    dataFormat = GL_UNSIGNED_SHORT_5_6_5;
                    pixelFormat = GL_RGB;
                    break;

                case ANDROID_BITMAP_FORMAT_RGBA_4444:
                    internalFormat = GL_RGBA4;
                    dataFormat = GL_UNSIGNED_SHORT_4_4_4_4;
                    break;

                case ANDROID_BITMAP_FORMAT_A_8:
                    dataFormat = GL_R8;
                    pixelFormat = GL_RED;
                    break;
            }
            mFormat = dataFormat;
            glTexImage2D(mType, 0, internalFormat,
                         info.width, info.height,
                         0, pixelFormat, dataFormat, pixels);
            AndroidBitmap_unlockPixels(env, bitmap);
        }
        env->DeleteLocalRef(bitmap);
    }
};

}
#endif
