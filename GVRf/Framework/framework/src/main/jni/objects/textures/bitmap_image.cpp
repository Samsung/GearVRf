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

#include "bitmap_image.h"

namespace gvr {
BitmapImage::BitmapImage(int format) :
            Image(Image::BITMAP, format), mIsCompressed(false),
            mData(NULL), mBitmap(NULL), mJava(NULL)
{
}

BitmapImage::~BitmapImage()
{
    if (mJava)
    {
        mUpdateLock.lock();
        clearData(getCurrentEnv(mJava));
        mUpdateLock.unlock();
    }
}

void BitmapImage::update(JNIEnv* env, int width, int height, jbyteArray data)
{
    mUpdateLock.lock();
    env->GetJavaVM(&mJava);
    clearData(env);
    mWidth = width;
    mHeight = height;
    mFormat = GL_RGBA; // PixelFormat::A8;
    mIsCompressed = false;
    if (data != NULL)
    {
        mData = static_cast<jbyteArray>(env->NewGlobalRef(data));
        mPendingUpdate = true;
    }
    mUpdateLock.unlock();
}

void BitmapImage::update(JNIEnv* env, jobject bitmap)
{
    mUpdateLock.lock();
    env->GetJavaVM(&mJava);
    clearData(env);
    if (bitmap != NULL)
    {
        mBitmap = static_cast<jbyteArray>(env->NewGlobalRef(bitmap));
        mPendingUpdate = true;
    }
    mUpdateLock.unlock();
}

void BitmapImage::update(JNIEnv *env, int width, int height, int imageSize,
                         jbyteArray data, int levels, const int* dataOffsets)
{
    mUpdateLock.lock();
    env->GetJavaVM(&mJava);
    clearData(env);
    mWidth = width;
    mHeight = height;
    mLevels = levels;
    mFormat = GL_RGBA;
    mIsCompressed = true;
    mImageSize = imageSize;
    setDataOffsets(dataOffsets, levels);
    if (data != NULL)
    {
        mData = static_cast<jbyteArray>(env->NewGlobalRef(data));
        mPendingUpdate = true;
    }
    mUpdateLock.unlock();
}

void BitmapImage::clearData(JNIEnv* env)
{
    if (mData != NULL)
    {
        env->DeleteGlobalRef(mData);
        mData = NULL;
    }
    if (mBitmap != NULL)
    {
        env->DeleteGlobalRef(mBitmap);
        mBitmap = NULL;
    }
    mIsCompressed = false;
}

}

