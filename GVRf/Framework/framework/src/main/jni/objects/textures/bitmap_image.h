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

#ifndef BITMAP_IMAGE_H
#define BITMAP_IMAGE_H

#include "objects/textures/texture.h"
#include "util/gvr_jni.h"
#include "util/gvr_log.h"
#include "util/jni_utils.h"

namespace gvr {
    class BitmapImage : public Image
    {
    public:

        explicit BitmapImage(JNIEnv* env, int width, int height, jbyteArray data) :
                Image(GL_TEXTURE_2D, width, height, width * height, GL_LUMINANCE, 1),
                mData(NULL)
        {
            if (JNI_OK != env->GetJavaVM(&mJava))
            {
                FAIL("GetJavaVM failed");
            }
            update(env, width, height, data);
        }

        explicit BitmapImage(JNIEnv* env, int target, int width, int height, int imagesize, int format, int levels) :
                Image(target, width, height, imagesize, format, levels)
        {
            if (JNI_OK != env->GetJavaVM(&mJava))
            {
                FAIL("GetJavaVM failed");
            }
        }

        virtual ~BitmapImage()
        {
            clearData(getCurrentEnv(mJava));
        }

        void clearData(JNIEnv* env)
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
        }

        void update(JNIEnv* env, int width, int height, jbyteArray data)
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

    private:
        BitmapImage(const BitmapImage& texture) = delete;
        BitmapImage(BitmapImage&& texture) = delete;
        BitmapImage& operator=(const BitmapImage& texture) = delete;
        BitmapImage& operator=(BitmapImage&& texture) = delete;


    protected:
        JavaVM* mJava;
        jbyteArray mData;
    };

}
#endif
