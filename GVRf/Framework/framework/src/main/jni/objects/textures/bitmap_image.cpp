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
BitmapImage::BitmapImage(JNIEnv* env, int width, int height, jbyteArray data) :
            Image(GL_TEXTURE_2D, width, height, width * height, GL_LUMINANCE, 1),
            mData(NULL), mBitmap(NULL)
{
    if (JNI_OK != env->GetJavaVM(&mJava))
    {
        FAIL("GetJavaVM failed");
    }
    update(env, width, height, data);
}

BitmapImage::BitmapImage(JNIEnv* env, int target, int width, int height, int imagesize, int format, int levels) :
            Image(target, width, height, imagesize, format, levels)
{
    if (JNI_OK != env->GetJavaVM(&mJava))
    {
        FAIL("GetJavaVM failed");
    }
}

BitmapImage::BitmapImage(JNIEnv* env, jobject bitmap) :
        Image(GL_TEXTURE_2D, 0, 0, 0, GL_LUMINANCE, 1),
        mData(NULL), mBitmap(NULL)
{
    if (JNI_OK != env->GetJavaVM(&mJava))
    {
        FAIL("GetJavaVM failed");
    }
    update(env, bitmap);
}

BitmapImage::~BitmapImage()
{
    clearData(getCurrentEnv(mJava));
}

void BitmapImage::update(JNIEnv* env, int width, int height, jbyteArray data)
{
    clearData(env);
    mWidth = width;
    mHeight = height;
    if (data != NULL)
    {
        mData = static_cast<jbyteArray>(env->NewWeakGlobalRef(data));
        mPendingUpdate = true;
    }
}

void BitmapImage::update(JNIEnv* env, jobject bitmap)
{
    clearData(env);
    if (bitmap != NULL)
    {
        mBitmap = static_cast<jbyteArray>(env->NewWeakGlobalRef(bitmap));
        mPendingUpdate = true;
    }
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

