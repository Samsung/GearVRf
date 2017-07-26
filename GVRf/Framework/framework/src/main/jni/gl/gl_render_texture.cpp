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

GLRenderTexture::GLRenderTexture(int width, int height, int sample_count, int layers, int depth_format) :
        RenderTexture(sample_count),
        renderTexture_gl_render_buffer_(nullptr),
        renderTexture_gl_frame_buffer_(nullptr),
        renderTexture_gl_resolve_buffer_(nullptr),
        renderTexture_gl_color_buffer_(nullptr),
        layer_index_(0)
{
    mImage = new GLRenderImage(width, height, layers);
    switch (depth_format)
    {
        case DepthFormat::DEPTH_0:
        depth_format_ = 0;
        break;

        case DepthFormat::DEPTH_24:
        depth_format_ = GL_DEPTH_COMPONENT24_OES;
        break;

        case DepthFormat::DEPTH_24_STENCIL_8:
        depth_format_ = GL_DEPTH24_STENCIL8_OES;
        break;

        default:
        depth_format_ = GL_DEPTH_COMPONENT16;
        break;
    }
}


GLRenderTexture::GLRenderTexture(int width, int height, int sample_count,
        int jcolor_format, int jdepth_format, bool resolve_depth,
        const TextureParameters* texparams)
        : RenderTexture(sample_count),
          renderTexture_gl_render_buffer_(nullptr),
          renderTexture_gl_frame_buffer_(new GLFrameBuffer()),
          renderTexture_gl_resolve_buffer_(nullptr),
          renderTexture_gl_color_buffer_(nullptr),
          layer_index_(0)
{
    GLRenderImage* colorbuffer = new GLRenderImage(width, height, jcolor_format, texparams);
    GLenum depth_format;

    mImage = colorbuffer;
    initialize();
    mImage->isReady();
    switch (jdepth_format)
    {
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
    if (sample_count <= 1)
    {
        generateRenderTextureNoMultiSampling(jdepth_format, depth_format,
                                             width, height);
    }
    else if (resolve_depth)
    {
        generateRenderTexture(sample_count, jdepth_format, depth_format,
                              width, height, jcolor_format);
    }
    else
    {
        generateRenderTextureEXT(sample_count, jdepth_format,
                                 depth_format, width, height);
    }
    if (jdepth_format != DepthFormat::DEPTH_0)
    {
        GLenum attachment = DepthFormat::DEPTH_24_STENCIL_8 == jdepth_format ? GL_DEPTH_STENCIL_ATTACHMENT : GL_DEPTH_ATTACHMENT;
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
    }

    glScissor(0, 0, width, height);
    glViewport(0, 0, width, height);
    glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT);

    if (resolve_depth && sample_count > 1)
    {
        delete renderTexture_gl_resolve_buffer_;
        renderTexture_gl_resolve_buffer_ = new GLFrameBuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_resolve_buffer_->id());
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                               colorbuffer->getTarget(), colorbuffer->getId(), 0);
        GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
        {
            LOGE("resolve FBO %i is not complete: 0x%x",
                 renderTexture_gl_resolve_buffer_->id(), status);
        }
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

GLRenderTexture::~GLRenderTexture()
{
    delete renderTexture_gl_frame_buffer_;
    if (renderTexture_gl_render_buffer_)
        delete renderTexture_gl_render_buffer_;
    if (renderTexture_gl_color_buffer_)
        delete renderTexture_gl_color_buffer_;
    if (renderTexture_gl_resolve_buffer_)
        delete renderTexture_gl_resolve_buffer_;
    if (renderTexture_gl_pbo_)
    {
        glDeleteBuffers(1, &renderTexture_gl_pbo_);
    }
}

bool GLRenderTexture::isReady()
{
    if (!Texture::isReady())
    {
        return false;
    }
    GLRenderImage* colorbuffer = static_cast<GLRenderImage*>(mImage);
    int width = colorbuffer->getWidth();
    int height = colorbuffer->getHeight();

    if (renderTexture_gl_frame_buffer_ == NULL)
    {
        renderTexture_gl_frame_buffer_ = new GLFrameBuffer();
        generateRenderTextureLayer(depth_format_, width, height);
        checkGLError("RenderTexture::isReady generateRenderTextureLayer");
    }
    return true;
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

void GLRenderTexture::generateRenderTextureLayer(GLenum depth_format, int width, int height)
{
    if (depth_format_ && (renderTexture_gl_render_buffer_ == nullptr))
    {
        renderTexture_gl_render_buffer_ = new GLRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, renderTexture_gl_render_buffer_->id());
        glRenderbufferStorage(GL_RENDERBUFFER, depth_format, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }
    GLRenderImage* image = static_cast<GLRenderImage*>(mImage);
    glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, image->getId(), 0, layer_index_);
    checkGLError("RenderTexture::generateRenderTextureLayer");
    int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (fboStatus == GL_FRAMEBUFFER_COMPLETE)
    {
        if (depth_format_)
        {
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, depth_format_, GL_RENDERBUFFER,
                                      renderTexture_gl_render_buffer_->id());
        }
        return;
    }
    LOGE("RenderTexture::generateRenderTextureLayer Could not bind texture %d to framebuffer: %d", image->getId(), fboStatus);
    switch (fboStatus)
    {
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

void GLRenderTexture::beginRendering(Renderer* renderer)
{
    if (!isReady())
    {
        return;
    }
    const int width = mImage->getWidth();
    const int height = mImage->getHeight();

    bind();
    if (mImage->getDepth() > 1)
    {
        LOGV("GLRenderTexture::beginRendering layer=%d", layer_index_);
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, mImage->getId(), 0, layer_index_);
    }
    glViewport(0, 0, width, height);
    glScissor(0, 0, width, height);
    glDepthMask(GL_TRUE);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    invalidateFrameBuffer(GL_FRAMEBUFFER, true, true, renderTexture_gl_render_buffer_ != NULL);
    if ((mBackColor[0] + mBackColor[1] + mBackColor[2] + mUseStencil) != 0)
    {
        int mask = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;
        glClearColor(mBackColor[0], mBackColor[1], mBackColor[2], mBackColor[3]);
        if (mUseStencil && (depth_format_ == GL_DEPTH24_STENCIL8_OES))
        {
            mask |= GL_STENCIL_BUFFER_BIT;
            glStencilMask(~0);
        }
        glClear(mask);
    }
    else
    {
        glClear(GL_DEPTH_BUFFER_BIT);
    }
}

void GLRenderTexture::endRendering(Renderer* renderer)
{
    const int width = mImage->getWidth();
    const int height = mImage->getHeight();
    int fbid = getFrameBufferId();

    invalidateFrameBuffer(GL_DRAW_FRAMEBUFFER, true, false, true);
    if (renderTexture_gl_resolve_buffer_ && mSampleCount > 1)
    {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbid);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, renderTexture_gl_resolve_buffer_->id());
        glBlitFramebuffer(0, 0, width, height,
                          0, 0, width, height,
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

/*
 * Bind the framebuffer to the specified layer of the texture array.
 * Create the framebuffer and layered texture if necessary.
 * This function must be called from the GL thread.
 */
void GLRenderTexture::bindFrameBufferToLayer(int layerIndex)
{
    layer_index_ = layerIndex;
}

bool GLRenderTexture::bindTexture(int gl_location, int texIndex)
{
    GLRenderImage* image = static_cast<GLRenderImage*>(mImage);

    if (image && (gl_location >= 0))
    {
        LOGV("RenderTexture::bindTexture loc=%d texindex=%d", gl_location, texIndex);
        glActiveTexture(GL_TEXTURE0 + texIndex);
        glBindTexture(image->getTarget(), getId());
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glUniform1i(gl_location, texIndex);
    }
}

}
