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
    CubemapImage::CubemapImage(int format) :
            Image(Image::CUBEMAP, format), mJava(NULL), mBitmaps(NULL), mTextures(NULL)
    {
        mFormat = format;
    }

    void CubemapImage::update(JNIEnv* env, jobjectArray bitmapArray)
    {
        std::lock_guard<std::mutex> lock(mUpdateLock);
        env->GetJavaVM(&mJava);
        clearData(env);
        mBitmaps = env->NewGlobalRef(bitmapArray);
        signalUpdate();
    }

    void CubemapImage::update(JNIEnv* env, int width, int height, int imageSize,
                              jobjectArray textureArray, const int* textureOffset)
    {
        std::lock_guard<std::mutex> lock(mUpdateLock);
        env->GetJavaVM(&mJava);
        mWidth = width;
        mHeight = height;
        mImageSize = imageSize;
        mIsCompressed = true;
        setDataOffsets(textureOffset, 6);
        clearData(env);
        mTextures = env->NewGlobalRef(textureArray);
        signalUpdate();
    }

    CubemapImage::~CubemapImage()
    {
        if (mJava != NULL)
        {
            std::lock_guard<std::mutex> lock(mUpdateLock);
            JNIEnv *env = getCurrentEnv(mJava);
            clearData(env);
        }
    }


    void CubemapImage::clearData(JNIEnv* env)
    {
        if (mBitmaps != NULL)
        {
            env->DeleteGlobalRef(mBitmaps);
            mBitmaps = NULL;
        }
        if (mTextures != NULL)
        {
            env->DeleteGlobalRef(mTextures);
            mTextures = NULL;
        }
    }
}

