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

#ifndef GL_IMAGETEX_H
#define GL_IMAGETEX_H

#include "objects/textures/image.h"
#include "gl/gl_headers.h"
#include "gl_image.h"

namespace gvr {
    class GLImageTex : public GLImage, public Image
    {
    public:
        explicit GLImageTex(int target) :
                Image(ImageType::BITMAP, target),
                GLImage(target)
        { }

        explicit GLImageTex(int target, int id) :
                Image(ImageType::BITMAP, target),
                GLImage(target,id)

        {
            mTexParams.setMinFilter(GL_LINEAR); // if created with external texture
            mState = HAS_DATA;
        }

        virtual ~GLImageTex() {}

        void updateTexParams() {
            int min_filter = mTexParams.getMinFilter();
            if(mIsCompressed && mLevels <= 1 && min_filter >= TextureParameters::NEAREST_MIPMAP_NEAREST)
                mTexParams.setMinFilter(GL_LINEAR);

            GLImage::updateTexParams(mTexParams);
        }
        explicit GLImageTex(ImageType type, int format, short width, short height) :
                Image(type, format),
                GLImage(GL_TEXTURE_2D)
        {
            mWidth = width;
            mHeight = height;
        }

        virtual int getId() { return mId; }

        virtual bool isReady()
        {
            return updateGPU() && checkForUpdate(mId);
        }

        virtual void texParamsChanged(const TextureParameters& texparams)
        {
            mTexParams = texparams;
            mTexParamsDirty = true;
        }
    };

}
#endif
