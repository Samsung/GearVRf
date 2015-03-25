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

#include "gl/gl_render_buffer.h"
#include "gl/gl_frame_buffer.h"

#include "objects/textures/base_texture.h"

namespace gvr {

class RenderTexture: public Texture {
public:
    explicit RenderTexture(int width, int height);
    explicit RenderTexture(int width, int height, int sample_count);

    ~RenderTexture() {
        if (gl_texture_ != 0 || gl_render_buffer_ != 0
                || gl_frame_buffer_ != 0) {
            recycle();
        }
    }

    void recycle() {
        Texture::recycle();
        delete gl_render_buffer_;
        gl_render_buffer_ = 0;
        delete gl_frame_buffer_;
        gl_frame_buffer_ = 0;
    }

    GLenum getTarget() const {
        return TARGET;
    }

    GLuint getFrameBufferId() const {
        return gl_frame_buffer_->id();
    }

    int width() const {
        return width_;
    }

    int height() const {
        return height_;
    }

private:
    RenderTexture(const RenderTexture& render_texture);
    RenderTexture(RenderTexture&& render_texture);
    RenderTexture& operator=(const RenderTexture& render_texture);
    RenderTexture& operator=(RenderTexture&& render_texture);

private:
    static const GLenum TARGET = GL_TEXTURE_2D;
    int width_;
    int height_;
    int sample_count_;
    GLRenderBuffer* gl_render_buffer_;
    GLFrameBuffer* gl_frame_buffer_;
};
}
#endif
