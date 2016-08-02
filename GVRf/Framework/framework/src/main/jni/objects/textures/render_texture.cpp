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
#include "util/gvr_gl_ext.h"
#include <GLES3/gl3.h>
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
    MSAA::glRenderbufferStorageMultisampleIMG(GL_RENDERBUFFER, sample_count,
            GL_DEPTH_COMPONENT16, width, height);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);

    glBindFramebuffer(GL_FRAMEBUFFER, gl_frame_buffer_->id());

    MSAA::glFramebufferTexture2DMultisample(GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0, TARGET, gl_texture_->id(), 0, sample_count);

    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
            GL_RENDERBUFFER, gl_render_buffer_->id());
}

RenderTexture::RenderTexture(int width, int height, int sample_count,
        int jcolor_format, int jdepth_format, bool resolve_depth,
        int* texture_parameters) :
        Texture(new GLTexture(TARGET, texture_parameters)), width_(width), height_(
                height), sample_count_(sample_count), gl_frame_buffer_(new GLFrameBuffer()) {
    initialize(width, height);
    GLenum depth_format;
    glBindTexture(TARGET, gl_texture_->id());
    switch (jcolor_format) {
    case ColorFormat::COLOR_565:
        glTexImage2D(TARGET, 0, GL_RGB, width_, height_, 0, GL_RGB,
                GL_UNSIGNED_SHORT_5_6_5, 0);
        break;
    case ColorFormat::COLOR_5551:
        glTexImage2D(TARGET, 0, GL_RGB5_A1, width_, height_, 0, GL_RGBA,
                GL_UNSIGNED_SHORT_5_5_5_1, 0);
        break;
    case ColorFormat::COLOR_4444:
        glTexImage2D(TARGET, 0, GL_RGBA, width_, height_, 0, GL_RGBA,
                GL_UNSIGNED_SHORT_4_4_4_4, 0);
        break;
    case ColorFormat::COLOR_8888:
        glTexImage2D(TARGET, 0, GL_RGBA8, width_, height_, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, 0);
        break;
    case ColorFormat::COLOR_8888_sRGB:
        glTexImage2D(TARGET, 0, GL_SRGB8_ALPHA8, width_, height_, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, 0);
        break;
    default:
        break;
    }
    switch (jdepth_format) {
    case DepthFormat::DEPTH_24:
        depth_format = GL_DEPTH_COMPONENT24_OES;
        break;
    case DepthFormat::DEPTH_24_STENCIL_8:
        depth_format = GL_DEPTH24_STENCIL8_OES;
        break;
    default:
        depth_format = GL_DEPTH_COMPONENT16;
        break;
    }
    if (sample_count <= 1) {
        generateRenderTextureNoMultiSampling(jdepth_format, depth_format, width,
                height);
    } else if (resolve_depth) {
        generateRenderTexture(sample_count, jdepth_format, depth_format, width,
                height, jcolor_format);
    } else {
        generateRenderTextureEXT(sample_count, jdepth_format, depth_format,
                width, height);
    }
    if (jdepth_format != DepthFormat::DEPTH_0) {
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER, gl_render_buffer_->id());
    }

    glScissor(0, 0, width, height);
    glViewport(0, 0, width, height);
    glClearColor(0, 1, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT);

    if (resolve_depth && sample_count > 1) {
        gl_resolve_buffer_ = new GLFrameBuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, gl_resolve_buffer_->id());
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, gl_texture_->id(), 0);
        GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            LOGE(
                    "resolve FBO %i is not complete: 0x%x", gl_resolve_buffer_->id(), status);
        }
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void RenderTexture::generateRenderTextureNoMultiSampling(int jdepth_format,
        GLenum depth_format, int width, int height) {
    if (jdepth_format != DepthFormat::DEPTH_0) {
        gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, gl_render_buffer_->id());
        glRenderbufferStorage(GL_RENDERBUFFER, depth_format, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }
    glBindFramebuffer(GL_FRAMEBUFFER, gl_frame_buffer_->id());
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, TARGET,
            gl_texture_->id(), 0);
}

void RenderTexture::generateRenderTextureEXT(int sample_count,
        int jdepth_format, GLenum depth_format, int width, int height) {
    if (jdepth_format != DepthFormat::DEPTH_0) {
        gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, gl_render_buffer_->id());
        MSAA::glRenderbufferStorageMultisampleIMG(GL_RENDERBUFFER, sample_count,
                depth_format, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }
    glBindFramebuffer(GL_FRAMEBUFFER, gl_frame_buffer_->id());
    MSAA::glFramebufferTexture2DMultisample(GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0, TARGET, gl_texture_->id(), 0, sample_count);
}

void RenderTexture::generateRenderTexture(int sample_count, int jdepth_format,
        GLenum depth_format, int width, int height, int jcolor_format) {
    GLenum color_format;
    switch (jcolor_format) {
    case ColorFormat::COLOR_565:
        color_format = GL_RGB565;
        break;
    case ColorFormat::COLOR_5551:
        color_format = GL_RGB5_A1;
        break;
    case ColorFormat::COLOR_4444:
        color_format = GL_RGBA4;
        break;
    default:
        color_format = GL_RGBA8;
        break;
    }
    if (jdepth_format != DepthFormat::DEPTH_0) {
        gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, gl_render_buffer_->id());
        MSAA::glRenderbufferStorageMultisample(GL_RENDERBUFFER,
                sample_count, depth_format, width, height);
    }
    gl_color_buffer_ = new GLRenderBuffer();
    glBindRenderbuffer(GL_RENDERBUFFER, gl_color_buffer_->id());
    MSAA::glRenderbufferStorageMultisample(GL_RENDERBUFFER, sample_count,
            color_format, width, height);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, gl_frame_buffer_->id());
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
            GL_RENDERBUFFER, gl_color_buffer_->id());
}

void RenderTexture::beginRendering() {
    const int width = width_;
    const int height = height_;
    glBindFramebuffer(GL_FRAMEBUFFER, gl_frame_buffer_->id());
    glViewport(0, 0, width, height);
    glScissor(0, 0, width, height);
    glDepthMask(GL_TRUE);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    invalidateFrameBuffer(true, true, true);
    glClear(GL_DEPTH_BUFFER_BIT);
}

void RenderTexture::endRendering() {
    const int width = width_;
    const int height = height_;
    invalidateFrameBuffer(true, false, true);
    if (gl_resolve_buffer_ && sample_count_ > 1) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, gl_frame_buffer_->id());
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gl_resolve_buffer_->id());
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
        invalidateFrameBuffer(true, true, false);
    }
}

void RenderTexture::invalidateFrameBuffer(bool is_fbo, const bool color_buffer, const bool depth_buffer) {
    const int offset = (int) !color_buffer;
    const int count = (int) color_buffer + ((int) depth_buffer) * 2;
    const GLenum fboAttachments[3] = { GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT };
    const GLenum attachments[3] = { GL_COLOR_EXT, GL_DEPTH_EXT, GL_STENCIL_EXT };
    glInvalidateFramebuffer(GL_FRAMEBUFFER, count, (is_fbo ? fboAttachments : attachments) + offset);
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
