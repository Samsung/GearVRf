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
 * A frame buffer object.
 ***************************************************************************/

#ifndef RENDER_TEXTURE_H_
#define RENDER_TEXTURE_H_

#include "util/gvr_parameters.h"
#include "objects/textures/texture.h"

namespace gvr {

class RenderTexture : public Texture
{
public:
    RenderTexture(int sample_count = 0)
    : Texture(TextureType::TEXTURE_2D),
      mSampleCount(sample_count),
      readback_started_(false),
      mUseStencil(false),
      mBackColor{0, 0, 0, 1}
    { }

    RenderTexture(Texture::TextureType textype, int sample_count = 0)
    : Texture(textype),
      mSampleCount(sample_count),
      readback_started_(false),
      mUseStencil(false),
      mBackColor{0, 0, 0, 1}
    { }

    virtual ~RenderTexture() { }
    virtual int width() const { mImage->getWidth(); }
    virtual int height() const { return mImage->getHeight(); }
    virtual unsigned int getFrameBufferId() const = 0;
    virtual unsigned int getDepthBufferId() const = 0;
    virtual void bind() = 0;
    virtual void beginRendering() = 0;
    virtual void endRendering() = 0;

    void setBackgroundColor(float r, float g, float b, float a)
    {
        mBackColor[0] = r;
        mBackColor[1] = g;
        mBackColor[2] = b;
        mBackColor[3] = a;
    }

    void useStencil(bool useFlag)   { mUseStencil = useFlag; }

    // Start to read back texture in the background. It can be optionally called before
    // readRenderResult() to read pixels asynchronously. This function returns immediately.
    virtual void startReadBack() = 0;

    // Copy data in pixel buffer to client memory. This function is synchronous. When
    // it returns, the pixels have been copied to PBO and then to the client memory.
    virtual bool readRenderResult(unsigned int *readback_buffer, long capacity) = 0;

private:
    RenderTexture(const RenderTexture&);
    RenderTexture(RenderTexture&&);
    RenderTexture& operator=(const RenderTexture&);
    RenderTexture& operator=(RenderTexture&&);

protected:
    bool    mUseStencil;
    float   mBackColor[4];
    int     mSampleCount;
    bool    readback_started_;  // set by startReadBack()
};

}
#endif
