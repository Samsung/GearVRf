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
BitmapImage::BitmapImage(int type, int format) :
            Image(GL_TEXTURE_2D, GL_RGBA),
            mData(NULL), mBitmap(NULL)
{
}

BitmapImage::~BitmapImage()
{
    mUpdateLock.lock();
    clearData(getCurrentEnv(mJava));
    mUpdateLock.unlock();
}

void BitmapImage::update(JNIEnv* env, int width, int height, jbyteArray data)
{
    mUpdateLock.lock();
    env->GetJavaVM(&mJava);
    clearData(env);
    mWidth = width;
    mHeight = height;
    mFormat = GL_RGBA; // PixelFormat::A8;
    if (data != NULL)
    {
        mData = static_cast<jbyteArray>(env->NewWeakGlobalRef(data));
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
        mBitmap = static_cast<jbyteArray>(env->NewWeakGlobalRef(bitmap));
        mPendingUpdate = true;
    }
    mUpdateLock.unlock();
}

void BitmapImage::clearData(JNIEnv* env)
{
    if (mData != NULL)
    {
        jobject ref = env->NewLocalRef(mData);
        if (ref != NULL)
        {
            env->DeleteWeakGlobalRef(mData);
            env->DeleteLocalRef(ref);
        }
        mData = NULL;
    }
    if (mBitmap != NULL)
    {
        jobject ref = env->NewLocalRef(mBitmap);
        if (ref != NULL)
        {
            env->DeleteWeakGlobalRef(mBitmap);
            env->DeleteLocalRef(ref);
        }
        mBitmap = NULL;
    }
}

}

