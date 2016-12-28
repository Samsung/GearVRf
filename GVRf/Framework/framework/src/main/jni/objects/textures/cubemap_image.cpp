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
        mTarget = GL_TEXTURE_CUBE_MAP;
    }

    void CubemapImage::update(JNIEnv* env, jobjectArray bitmapArray)
    {
        mUpdateLock.lock();
        env->GetJavaVM(&mJava);
        clearData(env);
        mBitmaps = env->NewGlobalRef(bitmapArray);
        mPendingUpdate = true;
        mUpdateLock.unlock();
    }

    void CubemapImage::update(JNIEnv* env, int width, int height, int imageSize,
                              jobjectArray textureArray, const int* textureOffset)
    {
        mUpdateLock.lock();
        env->GetJavaVM(&mJava);
        mWidth = width;
        mHeight = height;
        mImageSize = imageSize;
        setDataOffsets(textureOffset, 6);
        clearData(env);
        mTextures = env->NewGlobalRef(textureArray);
        mPendingUpdate = true;
        mUpdateLock.unlock();
    }

    CubemapImage::~CubemapImage()
    {
        mUpdateLock.lock();
        JNIEnv* env = getCurrentEnv(mJava);
        clearData(env);
        mUpdateLock.unlock();
    }


    void CubemapImage::clearData(JNIEnv* env)
    {
        if (mBitmaps != NULL)
        {
            jobject bmaps = env->NewLocalRef(mBitmaps);

            if (bmaps != NULL)
            {
                 env->DeleteGlobalRef(mBitmaps);
                env->DeleteLocalRef(bmaps);
            }
            mBitmaps = NULL;
        }
        if (mTextures != NULL)
        {
            jobject textures = env->NewLocalRef(mTextures);
            if (textures != NULL)
            {
                env->DeleteGlobalRef(mTextures);
                env->DeleteLocalRef(textures);
            }
            mTextures = NULL;
        }
    }
}

