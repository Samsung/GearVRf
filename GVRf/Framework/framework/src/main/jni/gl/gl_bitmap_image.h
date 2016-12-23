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

    explicit GLBitmapImage(JNIEnv* env, int target, int width, int height, int imagesize, int format, int levels) :
            BitmapImage(env, target, width, height, imagesize, format, levels)
    { }

    virtual void update(int texid)
    {
        if (mData != NULL)
        {
            JNIEnv *env = getCurrentEnv(mJava);
            jbyteArray array = static_cast<jbyteArray>(env->NewLocalRef(mData));
            jbyte *data = env->GetByteArrayElements(array, 0);
            env->ReleaseByteArrayElements(mData, data, 0);
            glBindTexture(GL_TEXTURE_2D, texid);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, mWidth, mHeight, 0, GL_LUMINANCE,
                         GL_UNSIGNED_BYTE, mData
            );
            glGenerateMipmap(GL_TEXTURE_2D);
            env->DeleteLocalRef(array);
            clearData(env);
        }
        mPendingUpdate = false;
    }
};

}
#endif
