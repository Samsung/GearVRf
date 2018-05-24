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
#include "gl/gl_headers.h"
#include "gl_image.h"

namespace gvr {
    class GLBitmapImage : public GLImage, public BitmapImage
    {
    public:
        explicit GLBitmapImage(int format) :
                BitmapImage(format), GLImage(GL_TEXTURE_2D)
        { }

        virtual ~GLBitmapImage() {}

        static int updateFromBitmap(JNIEnv *env, int target, jobject bitmap, bool, int internalFormat);

        virtual int getId() { return mId; }

        virtual bool isReady()
        {
            return updateGPU() && checkForUpdate(mId);
        }

        virtual void texParamsChanged(const TextureParameters& texparams)
        {
            if (mTexParams != texparams)
            {
                mTexParams = texparams;
                mTexParamsDirty = true;
            }
        }

        void updateTexParams() {
            int min_filter = mTexParams.getMinFilter();

            if(mIsCompressed && mLevels <= 1 && min_filter >= TextureParameters::NEAREST_MIPMAP_NEAREST)
                mTexParams.setMinFilter(GL_LINEAR);

            GLImage::updateTexParams(mTexParams);
        }
    protected:
        virtual void update(int texid);
        void updateFromMemory(int texid);
        void updateFromBitmap(int texid);
        void loadCompressedMipMaps(jbyte *data, int format);

    private:
        void updateFromBuffer(JNIEnv *env, int target, jobject bitmap);
    };

}
#endif
