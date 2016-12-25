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

#ifndef GL_COMPRESSED_IMAGE_H
#define GL_COMPRESSED_IMAGE_H

#include <objects/textures/compressed_image.h>

namespace gvr
{
class GLCompressedImage : public CompressedImage
{
public:

    // The constructor to use when loading a single-level texture
    explicit GLCompressedImage(JNIEnv *env, int target, int format,
                               int width, int height, jbyteArray bytes,
                               int levels, const int *dataOffsets) :
            CompressedImage(env, target, format, width, height, bytes, levels, dataOffsets)
    {
        CompressedImage::update(env, width, height, bytes, levels, dataOffsets);
    }


    virtual void update(int texid)
    {
        JNIEnv *env = getCurrentEnv(mJava);
        jbyte *data = env->GetByteArrayElements(mData, 0);
        glBindTexture(mType, texid);
        if (mLevels > 1)
        {
            loadCompressedMipMaps(data);
        }
        else
        {
            glCompressedTexImage2D(mType, 0, mFormat, mWidth, mHeight, 0,
                                   mImageSize, data + getDataOffset(0));
            glGenerateMipmap(GL_TEXTURE_2D);
        }
        env->ReleaseByteArrayElements(mData, data, 0);
        env->DeleteGlobalRef(mData);
        mPendingUpdate = false;
    }

private:
    GLCompressedImage(const GLCompressedImage&) = delete;
    GLCompressedImage(GLCompressedImage&&) = delete;
    GLCompressedImage &operator=(const GLCompressedImage&) = delete;
    GLCompressedImage &operator=(GLCompressedImage&&) = delete;

    void loadCompressedMipMaps(jbyte *data)
    {
        for (int level = 0; level < mLevels; ++level) {
            int levelOffset = getDataOffset(level);
            int levelSize = getDataOffset(level + 1) - levelOffset;
            int width = mWidth >> level;
            int height = mHeight >> level;
            if (width < 1) width = 1;
            if (height < 1) height = 1;
            glCompressedTexImage2D(mType, level, mFormat, width, height, levelOffset, levelSize,
                                   data);
        }
    }
};
}
#endif
