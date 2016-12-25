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

class RenderTexture
{
public:
    explicit RenderTexture(int width, int height, int sample_count = 0) : width_(width), height_(height), sample_count_(sample_count) { }
    explicit RenderTexture(int width, int height, int sample_count,
            int jcolor_format, int jdepth_format, bool resolve_depth,
            const int* texture_parameters) : width_(width), height_(height), sample_count_(sample_count) { }

    virtual ~RenderTexture() { }

    virtual void initialize(int width, int height)
    {
        width_ = width;
        height_ = height;
        readback_started_ = false;
    }

    virtual Texture* asTexture() { return NULL; }
    virtual const Texture* asTexture() const { return NULL; }
    virtual unsigned int getFrameBufferId() const = 0;
    virtual void bind() = 0;
    int width() const { return width_; }
    int height() const { return height_; }
    virtual void beginRendering() = 0;
    virtual void endRendering() = 0;

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
    int width_;
    int height_;
    int sample_count_;
    bool readback_started_;          // set by startReadBack()
};

}
#endif
