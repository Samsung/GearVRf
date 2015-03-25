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
                0), gl_render_buffer_(new GLRenderBuffer()), gl_frame_buffer_(
                new GLFrameBuffer()) {
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

}
