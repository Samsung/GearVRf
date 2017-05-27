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

#include "gl/gl_render_texture.h"
#include "gl_imagetex.h"
#include "gl_render_image.h"

namespace gvr {

GLRenderTexture::GLRenderTexture(int width, int height, Image* image) :
        RenderTexture(0),
        renderTexture_gl_render_buffer_(nullptr),
        renderTexture_gl_frame_buffer_(nullptr)
{
    mImage = image;
    initialize();
}

GLRenderTexture::GLRenderTexture(int width, int height)
        : RenderTexture(0),
          renderTexture_gl_render_buffer_(new GLRenderBuffer()),
          renderTexture_gl_frame_buffer_ (new GLFrameBuffer())
{
    GLRenderImage* colorbuffer = new GLRenderImage(width, height);
    mImage = colorbuffer;
    initialize();
    glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorbuffer->getTarget(), colorbuffer->getId(), 0);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
}

GLRenderTexture::GLRenderTexture(int width, int height, int sample_count) :
        RenderTexture(sample_count),
        renderTexture_gl_render_buffer_(new GLRenderBuffer()),
        renderTexture_gl_frame_buffer_(new GLFrameBuffer())
{
    GLRenderImage* colorbuffer = new GLRenderImage(width, height);
    mImage = colorbuffer;
    glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
    MSAA::glRenderbufferStorageMultisampleIMG(GL_RENDERBUFFER, sample_count,
            GL_DEPTH_COMPONENT16, width, height);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    MSAA::glFramebufferTexture2DMultisample(GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0, colorbuffer->getTarget(), colorbuffer->getId(), 0, sample_count);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
            GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
}

GLRenderTexture::GLRenderTexture(int width, int height, int sample_count,
        int jcolor_format, int jdepth_format, bool resolve_depth,
        const TextureParameters* texparams)
        : RenderTexture(sample_count),
          renderTexture_gl_frame_buffer_(new GLFrameBuffer())
{
    GLRenderImage* colorbuffer = new GLRenderImage(width, height);
    mImage = colorbuffer;
    if (texparams)
    {
        mImage->texParamsChanged(mTexParams);
    }
    initialize();
    GLenum depth_format;

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
        GLenum attachment = DepthFormat::DEPTH_24_STENCIL_8 == jdepth_format ? GL_DEPTH_STENCIL_ATTACHMENT : GL_DEPTH_ATTACHMENT;
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
    }

    glScissor(0, 0, width, height);
    glViewport(0, 0, width, height);
    glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT);

    if (resolve_depth && sample_count > 1) {
        delete renderTexture_gl_resolve_buffer_;
        renderTexture_gl_resolve_buffer_ = new GLFrameBuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_resolve_buffer_->id());
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                colorbuffer->getTarget(), colorbuffer->getId(), 0);
        GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            LOGE("resolve FBO %i is not complete: 0x%x",
                 renderTexture_gl_resolve_buffer_->id(), status);
        }
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

GLRenderTexture::~GLRenderTexture()
{
    delete renderTexture_gl_render_buffer_;
    delete renderTexture_gl_frame_buffer_;
    delete renderTexture_gl_color_buffer_;
    delete renderTexture_gl_resolve_buffer_;

    if (0 != renderTexture_gl_pbo_)
    {
        glDeleteBuffers(1, &renderTexture_gl_pbo_);
    }
}

void GLRenderTexture::initialize()
{
    glGenBuffers(1, &renderTexture_gl_pbo_);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, renderTexture_gl_pbo_);
    glBufferData(GL_PIXEL_PACK_BUFFER, mImage->getWidth() * mImage->getHeight() * 4, 0, GL_DYNAMIC_READ);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
}

void GLRenderTexture::generateRenderTextureNoMultiSampling(int jdepth_format,
        GLenum depth_format, int width, int height) {
    if (jdepth_format != DepthFormat::DEPTH_0) {
        delete renderTexture_gl_render_buffer_;
        renderTexture_gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
        glRenderbufferStorage(GL_RENDERBUFFER, depth_format, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }
    GLRenderImage* image = static_cast<GLRenderImage*>(mImage);
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, image->getTarget(), image->getId(), 0);
}

void GLRenderTexture::generateRenderTextureEXT(int sample_count,
        int jdepth_format, GLenum depth_format, int width, int height) {
    if (jdepth_format != DepthFormat::DEPTH_0) {
        delete renderTexture_gl_render_buffer_;
        renderTexture_gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
        MSAA::glRenderbufferStorageMultisampleIMG(GL_RENDERBUFFER, sample_count,
                depth_format, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }
    GLRenderImage* image = static_cast<GLRenderImage*>(mImage);
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    MSAA::glFramebufferTexture2DMultisample(GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0, image->getTarget(), image->getId(), 0, sample_count);
}

void GLRenderTexture::generateRenderTexture(int sample_count, int jdepth_format,
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

void GLRenderTexture::bind()
{
    if (renderTexture_gl_frame_buffer_ == nullptr)
    {
        renderTexture_gl_frame_buffer_ = new GLFrameBuffer();
    }
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    isReady();
}

void GLRenderTexture::beginRendering() {
    const int width = mImage->getWidth();
    const int height = mImage->getHeight();

    bind();
    glViewport(0, 0, width, height);
    glScissor(0, 0, width, height);
    glDepthMask(GL_TRUE);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    invalidateFrameBuffer(GL_FRAMEBUFFER, true, true, true);

    int mask = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;
    if ((mBackColor[0] + mBackColor[1] + mBackColor[2]) != 0)
    {
        glClearColor(mBackColor[0], mBackColor[1], mBackColor[2], mBackColor[3]);
    }
    if (mUseStencil)
    {
        mask |= GL_STENCIL_BUFFER_BIT;
        glStencilMask(~0);
    }
    glClear(mask);
}

void GLRenderTexture::endRendering() {
    const int width = mImage->getWidth();
    const int height = mImage->getHeight();
    invalidateFrameBuffer(GL_DRAW_FRAMEBUFFER, true, false, true);
    if (renderTexture_gl_resolve_buffer_ && mSampleCount > 1) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, renderTexture_gl_resolve_buffer_->id());
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
        invalidateFrameBuffer(GL_READ_FRAMEBUFFER, true, true, false);
    }
}

void GLRenderTexture::invalidateFrameBuffer(GLenum target, bool is_fbo, const bool color_buffer, const bool depth_buffer) {
    const int offset = (int) !color_buffer;
    const int count = (int) color_buffer + ((int) depth_buffer) * 2;
    const GLenum fboAttachments[3] = { GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT };
    const GLenum attachments[3] = { GL_COLOR_EXT, GL_DEPTH_EXT, GL_STENCIL_EXT };
    glInvalidateFramebuffer(target, count, (is_fbo ? fboAttachments : attachments) + offset);
}

void GLRenderTexture::startReadBack() {
    GLRenderImage* image = static_cast<GLRenderImage*>(mImage);
    glBindFramebuffer(GL_READ_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    image->setupReadback(renderTexture_gl_pbo_);
    glReadPixels(0, 0, image->getWidth(), image->getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, 0);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    readback_started_ = true;
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
}

bool GLRenderTexture::readRenderResult(uint32_t *readback_buffer, long capacity) {
    long neededCapacity = mImage->getWidth() * mImage->getHeight();
    if (!readback_buffer) {
        LOGE("GLRenderTexture::readRenderResult: readback_buffer is null");
        return false;
    }

    if (capacity < neededCapacity) {
        LOGE("GLRenderTexture::readRenderResult: buffer capacity too small "
             "(capacity %ld, needed %ld)", capacity, neededCapacity);
        return false;
    }

    GLRenderImage* image = static_cast<GLRenderImage*>(mImage);
    image->setupReadback(renderTexture_gl_pbo_);

    if (!readback_started_) {
        glReadPixels(0, 0, mImage->getWidth(), mImage->getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, 0);
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
