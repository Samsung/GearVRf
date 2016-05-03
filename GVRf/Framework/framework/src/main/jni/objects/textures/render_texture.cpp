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

#include "render_texture.h"
#include "eglextension/msaa/msaa.h"

namespace gvr {
RenderTexture::RenderTexture(int width, int height) :
        Texture(new GLTexture(TARGET)), width_(width), height_(height), sample_count_(
                0), gl_render_buffer_(new GLRenderBuffer()), gl_frame_buffer_ (
                new GLFrameBuffer()) {
    initialize(width, height);
    glBindTexture(TARGET, gl_texture_->id());
    glTexImage2D(TARGET, 0, GL_RGBA, width_, height_, 0, GL_RGBA,
            GL_UNSIGNED_BYTE, 0);
    glBindTexture(TARGET, 0);

    glBindRenderbuffer(GL_RENDERBUFFER, gl_render_buffer_->id());
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);

    glBindFramebuffer(GL_FRAMEBUFFER, gl_frame_buffer_->id());

    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, TARGET,
            gl_texture_->id(), 0);

    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
            GL_RENDERBUFFER, gl_render_buffer_->id());
}

RenderTexture::RenderTexture(int width, int height, int sample_count) :
        Texture(new GLTexture(TARGET)), width_(width), height_(height), sample_count_(
                sample_count), gl_render_buffer_(new GLRenderBuffer()), gl_frame_buffer_(
                new GLFrameBuffer()) {
    initialize(width, height);
    glBindTexture(TARGET, gl_texture_->id());
    glTexImage2D(TARGET, 0, GL_RGBA, width_, height_, 0, GL_RGBA,
            GL_UNSIGNED_BYTE, 0);
    glBindTexture(TARGET, 0);

    glBindRenderbuffer(GL_RENDERBUFFER, gl_render_buffer_->id());
    MSAA::glRenderbufferStorageMultisample(GL_RENDERBUFFER, sample_count,
            GL_DEPTH_COMPONENT16, width, height);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);

    glBindFramebuffer(GL_FRAMEBUFFER, gl_frame_buffer_->id());

    MSAA::glFramebufferTexture2DMultisample(GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0, TARGET, gl_texture_->id(), 0, sample_count);

    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
            GL_RENDERBUFFER, gl_render_buffer_->id());
}

void RenderTexture::startReadBack() {
    glBindFramebuffer(GL_READ_FRAMEBUFFER, gl_frame_buffer_->id());
    glViewport(0, 0, width_, height_);
    glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, TARGET,
            gl_texture_->id(), 0);
    glReadBuffer(GL_COLOR_ATTACHMENT0);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, gl_pbo_);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);

    glReadPixels(0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE, 0);

    readback_started_ = true;
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
}

bool RenderTexture::readRenderResult(uint32_t *readback_buffer, long capacity) {
    long neededCapacity = width_ * height_;
    if (!readback_buffer) {
        LOGE("RenderTexture::readRenderResult: readback_buffer is null");
        return false;
    }

    if (capacity < neededCapacity) {
        LOGE("RenderTexture::readRenderResult: buffer capacity too small "
             "(capacity %ld, needed %ld)", capacity, neededCapacity);
        return false;
    }

    glBindFramebuffer(GL_READ_FRAMEBUFFER, gl_frame_buffer_->id());
    glViewport(0, 0, width_, height_);
    glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, TARGET,
            gl_texture_->id(), 0);
    glReadBuffer(GL_COLOR_ATTACHMENT0);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, gl_pbo_);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);

    if (!readback_started_) {
        glReadPixels(0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE, 0);
    }

    int *buf = (int *)glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0, neededCapacity * 4,
             GL_MAP_READ_BIT);
    if (buf) {
        memcpy(readback_buffer, buf, neededCapacity * 4);
    }

    readback_started_ = false;

    glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

    return true;
}

}
