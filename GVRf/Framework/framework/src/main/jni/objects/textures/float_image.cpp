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

#include "float_image.h"

namespace gvr {
    FloatImage::FloatImage() : Image(ImageType::FLOAT_BITMAP, GL_RG),
                               mJava(NULL), mData(NULL)
    {
    }


    FloatImage::~FloatImage()
    {
        if (mJava != NULL)
        {
            std::lock_guard<std::mutex> lock(mUpdateLock);
            clearData(getCurrentEnv(mJava));
        }
    }

    void FloatImage::update(JNIEnv* env, int width, int height, jfloatArray data)
    {
        std::lock_guard<std::mutex> lock(mUpdateLock);
        env->GetJavaVM(&mJava);
        clearData(env);
        mWidth = width;
        mHeight = height;
        if (data != NULL)
        {
            mData = static_cast<jfloatArray>(env->NewGlobalRef(data));
            signalUpdate();
        }
    }


    void FloatImage::clearData(JNIEnv* env)
    {
        if (mData != NULL)
        {
            env->DeleteGlobalRef(mData);
            mData = NULL;
        }
    }

}
