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
#include "eglextension/msaa/msaa.h"

namespace gvr {
typedef void (GL_APIENTRY *PFNGLFRAMEBUFFERTEXTUREMULTIVIEWOVRPROC)(GLenum target,
                                                                    GLenum attachment,
                                                                    GLuint texture, GLint level,
                                                                    GLint baseViewIndex,
                                                                    GLsizei numViews);

typedef void (GL_APIENTRY *PFNGLFRAMEBUFFERTEXTUREMULTISAMPLEMULTIVIEWOVRPROC)(GLenum target,
                                                                               GLenum attachment,
                                                                               GLuint texture,
                                                                               GLint level,
                                                                               GLsizei samples,
                                                                               GLint baseViewIndex,
                                                                               GLsizei numViews);

RenderTexture::RenderTexture(int width, int height, GLTexture *tex) :
        Texture(tex), target_(tex->target()),
        width_(width), height_(height), sample_count_(0),
        back_color_{0, 0, 0}, use_stencil_(0),
        renderTexture_gl_render_buffer_(nullptr),
        renderTexture_gl_frame_buffer_(nullptr) {}

void createArrayTexture(GLuint &texId, int width, int height, GLenum tex_format) {
    glGenTextures(1, &texId);
    glBindTexture(GL_TEXTURE_2D_ARRAY, texId);
    glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, tex_format, width, height, 2);
    glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
}

RenderTexture::RenderTexture(int width, int height, GLenum target) :
        Texture(new GLTexture(target)),
        target_(target),
        width_(width), height_(height), sample_count_(0),
        back_color_{0, 0, 0}, use_stencil_(0),
        renderTexture_gl_render_buffer_(new GLRenderBuffer()),
        renderTexture_gl_frame_buffer_(new GLFrameBuffer()) {
    initialize(width, height);

    if (target == GL_TEXTURE_2D_ARRAY) {
        PFNGLFRAMEBUFFERTEXTUREMULTIVIEWOVRPROC glFramebufferTextureMultiviewOVR =
                (PFNGLFRAMEBUFFERTEXTUREMULTIVIEWOVRPROC) eglGetProcAddress(
                        "glFramebufferTextureMultiviewOVR");
        GL(glBindFramebuffer(GL_FRAMEBUFFER, 0));
        GL(glBindTexture(target_, gl_texture_->id()));
        GL(glTexImage3D(target, 0, GL_RGBA8, width, height, 2, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0));
        GL(glBindTexture(target_, 0));

        createArrayTexture(frameBufferDepthTextureId, width_, height_, GL_DEPTH_COMPONENT16);
        GL(glBindFramebuffer(GL_DRAW_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id()));
        GL(glFramebufferTextureMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                                            gl_texture_->id(), 0, 0, 2));
        GL(glFramebufferTextureMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                                            frameBufferDepthTextureId, 0, 0, 2));
    } else {
        GL(glBindTexture(target_, gl_texture_->id()));
        GL(glTexImage2D(target_, 0, GL_RGBA, width_, height_, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0));
        GL(glBindTexture(target_, 0));

        GL(glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id()));
        GL(glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height));
        GL(glBindRenderbuffer(GL_RENDERBUFFER, 0));

        GL(glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id()));

        GL(glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, target_, gl_texture_->id(),
                                  0));
        GL(glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,
                                     GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id()));
    }
}

RenderTexture::RenderTexture(int width, int height, int sample_count, GLenum target) :
        Texture(new GLTexture(target)), target_(target),
        width_(width), height_(height), sample_count_(sample_count),
        back_color_{0, 0, 0}, use_stencil_(0),
        renderTexture_gl_render_buffer_(new GLRenderBuffer()),
        renderTexture_gl_frame_buffer_(new GLFrameBuffer()) {
    initialize(width, height);
    if (target_ == GL_TEXTURE_2D_ARRAY) {
        PFNGLFRAMEBUFFERTEXTUREMULTISAMPLEMULTIVIEWOVRPROC glFramebufferTextureMultisampleMultiviewOVR =
                (PFNGLFRAMEBUFFERTEXTUREMULTISAMPLEMULTIVIEWOVRPROC) eglGetProcAddress(
                        "glFramebufferTextureMultisampleMultiviewOVR");
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(target_, gl_texture_->id());
        glTexImage3D(target, 0, GL_RGBA8, width, height, 2, 0, GL_RGBA,
                     GL_UNSIGNED_BYTE, 0);
        glBindTexture(target_, 0);
        createArrayTexture(frameBufferDepthTextureId, width_, height_, GL_DEPTH_COMPONENT16);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
        glFramebufferTextureMultisampleMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                                                    gl_texture_->id(), 0, sample_count_, 0, 2);
        glFramebufferTextureMultisampleMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                                                    frameBufferDepthTextureId, 0, sample_count_, 0,
                                                    2);
    } else {
        glBindTexture(target_, gl_texture_->id());
        glTexImage2D(target_, 0, GL_RGBA, width_, height_, 0, GL_RGBA,
                     GL_UNSIGNED_BYTE, 0);
        glBindTexture(target_, 0);

        glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
        MSAA::glRenderbufferStorageMultisampleIMG(GL_RENDERBUFFER, sample_count,
                                                  GL_DEPTH_COMPONENT16, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());

        MSAA::glFramebufferTexture2DMultisample(GL_FRAMEBUFFER,
                                                GL_COLOR_ATTACHMENT0, target_, gl_texture_->id(), 0,
                                                sample_count);

        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                                  GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
    }
}

void texImage3D(int color_format, int width, int height, GLenum target) {
    switch (color_format) {
        case ColorFormat::COLOR_565:
            glTexImage3D(target, 0, GL_RGB, width, height, 2, 0, GL_RGB,
                         GL_UNSIGNED_SHORT_5_6_5, 0);
            break;
        case ColorFormat::COLOR_5551:
            glTexImage3D(target, 0, GL_RGB5_A1, width, height, 2, 0, GL_RGBA,
                         GL_UNSIGNED_SHORT_5_5_5_1, 0);
            break;
        case ColorFormat::COLOR_4444:
            glTexImage3D(target, 0, GL_RGBA, width, height, 2, 0, GL_RGBA,
                         GL_UNSIGNED_SHORT_4_4_4_4, 0);
            break;
        case ColorFormat::COLOR_8888:
            glTexImage3D(target, 0, GL_RGBA8, width, height, 2, 0, GL_RGBA,
                         GL_UNSIGNED_BYTE, 0);
            break;
        case ColorFormat::COLOR_8888_sRGB:
            glTexImage3D(target, 0, GL_SRGB8_ALPHA8, width, height, 2, 0, GL_RGBA,
                         GL_UNSIGNED_BYTE, 0);
            break;
        default:
            break;
    }
}

void
RenderTexture::create2DRenderTexture(int jcolor_format, int jdepth_format, bool resolve_depth) {
    initialize(width_, height_);
    GLenum depth_format;
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glBindTexture(target_, gl_texture_->id());
    switch (jcolor_format) {
        case ColorFormat::COLOR_565:
            glTexImage2D(target_, 0, GL_RGB, width_, height_, 0, GL_RGB,
                         GL_UNSIGNED_SHORT_5_6_5, 0);
            break;
        case ColorFormat::COLOR_5551:
            glTexImage2D(target_, 0, GL_RGB5_A1, width_, height_, 0, GL_RGBA,
                         GL_UNSIGNED_SHORT_5_5_5_1, 0);
            break;
        case ColorFormat::COLOR_4444:
            glTexImage2D(target_, 0, GL_RGBA, width_, height_, 0, GL_RGBA,
                         GL_UNSIGNED_SHORT_4_4_4_4, 0);
            break;
        case ColorFormat::COLOR_8888:
            glTexImage2D(target_, 0, GL_RGBA8, width_, height_, 0, GL_RGBA,
                         GL_UNSIGNED_BYTE, 0);
            break;
        case ColorFormat::COLOR_8888_sRGB:
            glTexImage2D(target_, 0, GL_SRGB8_ALPHA8, width_, height_, 0, GL_RGBA,
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
    if (sample_count_ <= 1) {
        generateRenderTextureNoMultiSampling(jdepth_format, depth_format, width_,
                                             height_);
    } else if (resolve_depth) {
        generateRenderTexture(sample_count_, jdepth_format, depth_format, width_,
                              height_, jcolor_format);
    } else {
        generateRenderTextureEXT(sample_count_, jdepth_format, depth_format,
                                 width_, height_);
    }
    if (jdepth_format != DepthFormat::DEPTH_0) {
        GLenum attachment =
                DepthFormat::DEPTH_24_STENCIL_8 == jdepth_format ? GL_DEPTH_STENCIL_ATTACHMENT
                                                                 : GL_DEPTH_ATTACHMENT;
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER,
                                  renderTexture_gl_render_buffer_->id());

    }
    glScissor(0, 0, width_, height_);
    glViewport(0, 0, width_, height_);
    glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT);

    if (resolve_depth && sample_count_ > 1) {
        delete renderTexture_gl_resolve_buffer_;
        renderTexture_gl_resolve_buffer_ = new GLFrameBuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_resolve_buffer_->id());
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                               GL_TEXTURE_2D, gl_texture_->id(), 0);
        GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            LOGE(
                    "resolve FBO %i is not complete: 0x%x", renderTexture_gl_resolve_buffer_->id(),
                    status);
        }
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

}

void
RenderTexture::createArrayRenderTexture(int jcolor_format, int jdepth_format, bool resolve_depth) {
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    GLenum depth_format;
    glBindTexture(target_, gl_texture_->id());
    texImage3D(jcolor_format, width_, height_, target_);

    switch (jdepth_format) {
        case DepthFormat::DEPTH_24:
            depth_format = GL_DEPTH_COMPONENT24;
            break;
        case DepthFormat::DEPTH_24_STENCIL_8:
            depth_format = GL_DEPTH24_STENCIL8_OES;
            break;
        default:
            depth_format = GL_DEPTH_COMPONENT16;
            break;
    }
    PFNGLFRAMEBUFFERTEXTUREMULTIVIEWOVRPROC glFramebufferTextureMultiviewOVR =
            (PFNGLFRAMEBUFFERTEXTUREMULTIVIEWOVRPROC) eglGetProcAddress(
                    "glFramebufferTextureMultiviewOVR");
    PFNGLFRAMEBUFFERTEXTUREMULTISAMPLEMULTIVIEWOVRPROC glFramebufferTextureMultisampleMultiviewOVR =
            (PFNGLFRAMEBUFFERTEXTUREMULTISAMPLEMULTIVIEWOVRPROC) eglGetProcAddress(
                    "glFramebufferTextureMultisampleMultiviewOVR");

    if (jdepth_format != DepthFormat::DEPTH_0)
        createArrayTexture(frameBufferDepthTextureId, width_, height_, depth_format);

    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());

    if (sample_count_ <= 1 && !resolve_depth) {
        glFramebufferTextureMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                                         gl_texture_->id(), 0, 0, 2);
    }
    if (sample_count_ > 1 && !resolve_depth) {
        if (jdepth_format != DepthFormat::DEPTH_0) {
            glFramebufferTextureMultisampleMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                                                        frameBufferDepthTextureId, 0, sample_count_,
                                                        0, 2);
        }
        glFramebufferTextureMultisampleMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                                                    gl_texture_->id(), 0, sample_count_, 0, 2);
    }
    if (resolve_depth && sample_count_ > 1) {
        if (jdepth_format != DepthFormat::DEPTH_0)
            glFramebufferTextureMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                                             frameBufferDepthTextureId, 0, 0, 2);
        glGenTextures(1, &render_texture_gl_texture_);
        glBindTexture(GL_TEXTURE_2D_ARRAY, render_texture_gl_texture_);
        texImage3D(jcolor_format, width_, height_, GL_TEXTURE_2D_ARRAY);
        glFramebufferTextureMultisampleMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                                                    render_texture_gl_texture_, 0, sample_count_, 0,
                                                    2);
    }

/* Check BO is OK. */
    GLenum result = (glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER));
    if (result != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("RenderTextureArray::bindFrameBuffer Could not bind framebuffer: %d", result);
        switch (result) {
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT :
                LOGE("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
                break;

            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                LOGE("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
                break;

            case GL_FRAMEBUFFER_UNSUPPORTED:
                LOGE("GL_FRAMEBUFFER_UNSUPPORTED");
                break;
        }

        LOGE("Framebuffer incomplete at %s:%i\n", __FILE__, __LINE__);
/* Unbind framebuffer. */
        (glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0));

    }
    checkGLError("glFramebufferTextureMultiviewOVR 2");
    glScissor(0, 0, width_, height_);
    glViewport(0, 0, width_, height_);
    glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT);

    if (resolve_depth && sample_count_ > 1) {
        delete renderTexture_gl_resolve_buffer_;
        renderTexture_gl_resolve_buffer_ = new GLFrameBuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_resolve_buffer_->id());
        glFramebufferTextureMultiviewOVR(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                                         gl_texture_->id(), 0, 0, 2);
        GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            LOGE(
                    "resolve FBO %i is not complete: 0x%x", renderTexture_gl_resolve_buffer_->id(),
                    status);
        }
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);


}

RenderTexture::RenderTexture(int width, int height, GLenum target, int sample_count,
                             int jcolor_format, int jdepth_format, bool resolve_depth,
                             int *texture_parameters) : Texture(
        new GLTexture(target, texture_parameters)), target_(target),
                                                        width_(width), height_(height),
                                                        sample_count_(sample_count),
                                                        back_color_{0, 0, 0}, use_stencil_(0),
                                                        renderTexture_gl_frame_buffer_(
                                                                new GLFrameBuffer()) {

    switch (target) {
        case GL_TEXTURE_2D_ARRAY:
            createArrayRenderTexture(jcolor_format, jdepth_format, resolve_depth);
            return;
        case GL_TEXTURE_2D:
            create2DRenderTexture(jcolor_format, jdepth_format, resolve_depth);
            return;
        default:
            LOGE("invalid Target");
    }

}

void RenderTexture::setBackgroundColor(float r, float g, float b) {
    back_color_[0] = r;
    back_color_[1] = g;
    back_color_[2] = b;
}

void RenderTexture::generateRenderTextureNoMultiSampling(int jdepth_format,
                                                         GLenum depth_format, int width,
                                                         int height) {
    if (jdepth_format != DepthFormat::DEPTH_0) {

        delete renderTexture_gl_render_buffer_;
        renderTexture_gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
        glRenderbufferStorage(GL_RENDERBUFFER, depth_format, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

    }
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());

    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, target_,
                           gl_texture_->id(), 0);

}

void RenderTexture::generateRenderTextureEXT(int sample_count,
                                             int jdepth_format, GLenum depth_format, int width,
                                             int height) {
    if (jdepth_format != DepthFormat::DEPTH_0) {
        delete renderTexture_gl_render_buffer_;
        renderTexture_gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
        MSAA::glRenderbufferStorageMultisampleIMG(GL_RENDERBUFFER, sample_count,
                                                  depth_format, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    MSAA::glFramebufferTexture2DMultisample(GL_FRAMEBUFFER,
                                            GL_COLOR_ATTACHMENT0, target_, gl_texture_->id(), 0,
                                            sample_count);
}

void RenderTexture::generateRenderTexture(int sample_count, int jdepth_format,
                                          GLenum depth_format, int width, int height,
                                          int jcolor_format) {
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
        delete renderTexture_gl_render_buffer_;
        renderTexture_gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
        MSAA::glRenderbufferStorageMultisample(GL_RENDERBUFFER,
                                               sample_count, depth_format, width, height);
    }
    delete renderTexture_gl_color_buffer_;
    renderTexture_gl_color_buffer_ = new GLRenderBuffer();
    glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_color_buffer_->id());
    MSAA::glRenderbufferStorageMultisample(GL_RENDERBUFFER, sample_count,
                                           color_format, width, height);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                              GL_RENDERBUFFER, renderTexture_gl_color_buffer_->id());
}

void RenderTexture::beginRendering() {
    const int width = width_;
    const int height = height_;
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    glViewport(0, 0, width, height);
    glScissor(0, 0, width, height);
    glDepthMask(GL_TRUE);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    invalidateFrameBuffer(GL_FRAMEBUFFER, true, true, true);
    if ((back_color_[0] + back_color_[1] + back_color_[2] + use_stencil_) != 0) {
        int mask = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;
        glClearColor(back_color_[0], back_color_[1], back_color_[2], 1);
        if (use_stencil_) {
            mask |= GL_STENCIL_BUFFER_BIT;
            glStencilMask(~0);
        }
        glClear(mask);
    } else {
        glClear(GL_DEPTH_BUFFER_BIT);
    }
}

void RenderTexture::endRendering() {
    const int width = width_;
    const int height = height_;
    invalidateFrameBuffer(GL_DRAW_FRAMEBUFFER, true, false, true);
    if (renderTexture_gl_resolve_buffer_ && sample_count_ > 1) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, renderTexture_gl_resolve_buffer_->id());
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,
                          GL_COLOR_BUFFER_BIT, GL_NEAREST);
        invalidateFrameBuffer(GL_READ_FRAMEBUFFER, true, true, false);
    }
}

void RenderTexture::invalidateFrameBuffer(GLenum target, bool is_fbo, const bool color_buffer,
                                          const bool depth_buffer) {
    const int offset = (int) !color_buffer;
    const int count = (int) color_buffer + ((int) depth_buffer) * 2;
    const GLenum fboAttachments[3] = {GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT,
                                      GL_STENCIL_ATTACHMENT};
    const GLenum attachments[3] = {GL_COLOR_EXT, GL_DEPTH_EXT, GL_STENCIL_EXT};
    glInvalidateFramebuffer(target, count, (is_fbo ? fboAttachments : attachments) + offset);
}

void RenderTexture::startReadBack(int layer) {

    if (target_ == GL_TEXTURE_2D_ARRAY) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, getReadBufferId());
        glViewport(0, 0, width(), height());
        glFramebufferTextureLayer(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, getId(), 0, layer);
        glReadBuffer(GL_COLOR_ATTACHMENT0);
    } else {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
        glViewport(0, 0, width_, height_);
        glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, target_,
                               gl_texture_->id(), 0);
    }
    glReadBuffer(GL_COLOR_ATTACHMENT0);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, renderTexture_gl_pbo_);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);
    glReadPixels(0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE, 0);

    readback_started_ = true;
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
}

bool RenderTexture::readRenderResult(uint32_t *readback_buffer, long capacity, int eye) {
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
    if (target_ == GL_TEXTURE_2D_ARRAY) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, renderTexture_gl_read_buffer->id());
        glViewport(0, 0, width_, height_);
        glFramebufferTextureLayer(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, gl_texture_->id(), 0,
                                  eye);
    } else {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
        glViewport(0, 0, width_, height_);
        glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, target_,
                               gl_texture_->id(), 0);
    }
    glReadBuffer(GL_COLOR_ATTACHMENT0);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, renderTexture_gl_pbo_);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);

    if (!readback_started_) {
        glReadPixels(0, 0, width_, height_, GL_RGBA, GL_UNSIGNED_BYTE, 0);
    }

    int *buf = (int *) glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0, neededCapacity * 4,
                                        GL_MAP_READ_BIT);
    if (buf) {
        memcpy(readback_buffer, buf, neededCapacity * 4);
    }

    readback_started_ = false;

    glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

    return true;
}


/*
 * Create a RenderTextureArray with a specified number of layers.
 * This is a GL layered textured. Its creation is postponed
 * until bindFrameBuffer is called on the GL thread.
 * The RenderTextureArray constructor may be called from any thread.
 */
RenderTextureArray::RenderTextureArray(int width, int height, int numLayers)
        : RenderTexture(width, height, new GLTexture(GL_TEXTURE_2D_ARRAY)),
          mNumLayers(numLayers) {
}

void RenderTextureArray::beginRendering() {
    const GLenum attachments[3] = {GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT,
                                   GL_STENCIL_ATTACHMENT};

    glViewport(0, 0, width(), height());
    glScissor(0, 0, width(), height());
    glInvalidateFramebuffer(GL_FRAMEBUFFER, 3, attachments);
    glClearColor(0, 0, 0, 1);
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
}


bool RenderTextureArray::bindTexture(int gl_location, int texIndex) {
    if (gl_location >= 0) {
        glActiveTexture(GL_TEXTURE0 + texIndex);
        glBindTexture(getTarget(), getId());
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glUniform1i(gl_location, texIndex);
    }
}

/*
 * Bind the framebuffer to the specified layer of the texture array.
 * Create the framebuffer and layered texture if necessary.
 * This function must be called from the GL thread.
 */
bool RenderTextureArray::bindFrameBuffer(int layerIndex) {
    int fbid = getId();
    if (!isReady()) {
        if (renderTexture_gl_frame_buffer_ == nullptr) {
            renderTexture_gl_frame_buffer_ = new GLFrameBuffer();
        }
        glBindTexture(GL_TEXTURE_2D_ARRAY, fbid);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        //   glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGB8, width,height,depth,0,GL_RGB, GL_UNSIGNED_BYTE,NULL);
        //   glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_R16F, width,height,depth,0,GL_RED, GL_HALF_FLOAT,NULL);  // does not work for S6 edge
        //   glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGB10_A2, width,height,depth,0,GL_RGBA, GL_UNSIGNED_INT_2_10_10_10_REV,NULL);
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA8, width(), height(), mNumLayers, 0, GL_RGBA,
                     GL_UNSIGNED_BYTE, NULL);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        setReady(fbid > 0);
        checkGLError("create RenderTextureArray");
    }
    bind();
    glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                              fbid, 0, layerIndex);
    checkGLError("RenderTextureArray::bindFrameBuffer");
    int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("RenderTextureArray::bindFrameBuffer Could not bind framebuffer: %d", fboStatus);
        switch (fboStatus) {
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT :
                LOGE("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
                break;

            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                LOGE("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
                break;

            case GL_FRAMEBUFFER_UNSUPPORTED:
                LOGE("GL_FRAMEBUFFER_UNSUPPORTED");
                break;
        }
        return false;
    }
    return true;
}


}
