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
class Renderer;
struct RenderTextureInfo{
    int fdboWidth;
    int fboHeight;
    int multisamples;
    int views;
    GLuint fboId;
    GLuint texId;
    bool useMultiview;
};
class RenderTexture : public Texture
{
public:
    explicit RenderTexture(int sample_count = 1)
            : Texture(TextureType::TEXTURE_2D),
              mSampleCount(sample_count),
              mUseStencil(false),
              mBackColor{ 0, 0, 0, 1.0f},
              readback_started_(false)
    { }

    explicit RenderTexture(Image* image)
    : Texture(image->getType()),
      mSampleCount(1),
      mUseStencil(false),
      mBackColor{ 0, 0, 0, 1.0f},
      readback_started_(false)
    {
        setImage(image);
    }

    virtual ~RenderTexture() { }
    virtual int width() const { return getImage()->getWidth(); }
    virtual int height() const { return getImage()->getHeight(); }
    virtual unsigned int getFrameBufferId() const = 0;
    virtual void bind() = 0;
    virtual void beginRendering(Renderer*) = 0;
    virtual void endRendering(Renderer*) = 0;

    // Start to read back texture in the background. It can be optionally called before
    // readRenderResult() to read pixels asynchronously. This function returns immediately.
    virtual void startReadBack() = 0;

    // Copy data in pixel buffer to client memory. This function is synchronous. When
    // it returns, the pixels have been copied to PBO and then to the client memory.
    virtual bool readRenderResult(uint8_t*readback_buffer, long capacity) = 0;
    // Copy data in pixel buffer to client memory. This function is synchronous. When
    // it returns, the pixels have been copied to PBO and then to the client memory.
    virtual bool readRenderResult(uint8_t *readback_buffer) = 0;
    virtual void setLayerIndex(int layer_index) = 0;
    void useStencil(bool useFlag) { mUseStencil = useFlag; }
    void setBackgroundColor(float r, float g, float b, float a)
    {
        mBackColor[0] = r;
        mBackColor[1] = g;
        mBackColor[2] = b;
        mBackColor[3] = a;

    }

    int getSampleCount(){
        return mSampleCount;
    }

private:
    RenderTexture(const RenderTexture&) = delete;
    RenderTexture(RenderTexture&&) = delete;
    RenderTexture& operator=(const RenderTexture&) = delete;
    RenderTexture& operator=(RenderTexture&&) = delete;

protected:
    float   mBackColor[4];
    bool    mUseStencil;
    int     mSampleCount = 1;
    bool    readback_started_;  // set by startReadBack()
};

}
#endif
