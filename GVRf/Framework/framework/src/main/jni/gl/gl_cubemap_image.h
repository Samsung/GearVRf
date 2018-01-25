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

#ifndef GL_CUBEMAP_IMAGE_H_
#define GL_CUBEMAP_IMAGE_H_

#include "objects/textures/cubemap_image.h"
#include "gl_headers.h"
#include "gl_image.h"

namespace gvr {

class GLCubemapImage : public GLImage, public CubemapImage
{
public:
    explicit GLCubemapImage(int format)
            : GLImage(GL_TEXTURE_CUBE_MAP),
              CubemapImage(format)
    { }
    virtual ~GLCubemapImage() {}
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

        if(mIsCompressed && mLevels <=1 && min_filter >= TextureParameters::NEAREST_MIPMAP_NEAREST)
            mTexParams.setMinFilter(GL_LINEAR);

        GLImage::updateTexParams(mTexParams);
    }
protected:
    virtual void update(int texid);

private:
    GLCubemapImage(const GLCubemapImage&) = delete;
    GLCubemapImage(GLCubemapImage&&) = delete;
    GLCubemapImage& operator=(const GLCubemapImage&) = delete;
    GLCubemapImage& operator=(GLCubemapImage&&) = delete;

    void updateFromBitmap(int texid);
    void updateFromMemory(int texid);
};

}
#endif
