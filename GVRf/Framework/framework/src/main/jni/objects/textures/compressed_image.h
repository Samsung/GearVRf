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

#ifndef COMPRESSED_IMAGE_H
#define COMPRESSED_IMAGE_H


#include "bitmap_image.h"

namespace gvr
{
class CompressedImage : public BitmapImage
{
public:
    // The constructor to use when loading a single-level texture
    explicit CompressedImage(JNIEnv *env, int target, int format,
                               int width, int height, jbyteArray bytes,
                               int levels, const int *dataOffsets) :
            BitmapImage(env, target, width, height, env->GetArrayLength(bytes), format, levels)
    {
        update(env, width, height, bytes, levels, dataOffsets);
    }

    void update(JNIEnv *env, int width, int height, jbyteArray bytes, int levels,
                const int* dataOffsets)
    {
        env->DeleteGlobalRef(mData);
        mWidth = width;
        mHeight = height;
        mLevels = levels;
        mImageSize = env->GetArrayLength(bytes);
        mData = static_cast<jbyteArray>(env->NewGlobalRef(bytes));
        setDataOffsets(dataOffsets, mLevels);
        mPendingUpdate = true;
    }

private:
    CompressedImage(const CompressedImage &compressed_texture) = delete;
    CompressedImage(CompressedImage &&compressed_texture) = delete;
    CompressedImage &operator=(const CompressedImage &compressed_texture) = delete;
    CompressedImage &operator=(CompressedImage &&compressed_texture) = delete;
};
}
#endif
