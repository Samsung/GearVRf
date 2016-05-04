/* Copyright 2016 Samsung Electronics Co., LTD
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

#include "framebufferobject.h"
#include "util/gvr_log.h"
#include "VrApi.h"
#include <EGL/egl.h>

namespace gvr {

typedef void (GL_APIENTRY*PFNGLRENDERBUFFERSTORAGEMULTISAMPLEEXTPROC)(GLenum target, GLsizei samples,
        GLenum internalformat, GLsizei width, GLsizei height);
typedef void (GL_APIENTRY*PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEEXTPROC)(GLenum target, GLenum attachment,
        GLenum textarget, GLuint texture, GLint level, GLsizei samples);


void FrameBufferObject::clear() {
    mWidth = 0;
    mHeight = 0;
    mMultisamples = 0;
    mTextureSwapChainLength = 0;
    mTextureSwapChainIndex = 0;
    mColorTextureSwapChain = NULL;
    mDepthBuffers = NULL;
}

bool FrameBufferObject::create(const ovrTextureFormat colorFormat, const int width, const int height,
        const int multisamples, bool resolveDepth, const ovrTextureFormat depthFormat) {
    clearGLError("FrameBufferObject::create: GL error on entry");

    mWidth = width;
    mHeight = height;
    mMultisamples = multisamples;

    mColorTextureSwapChain = vrapi_CreateTextureSwapChain(VRAPI_TEXTURE_TYPE_2D, colorFormat, width, height, 1, true);
    if (nullptr == mColorTextureSwapChain) {
        FAIL("vrapi_CreateTextureSwapChain for mColorTextureSwapChain failed");
    }
    mTextureSwapChainLength = vrapi_GetTextureSwapChainLength(mColorTextureSwapChain);
    mTextureSwapChainIndex = 0;

    PFNGLRENDERBUFFERSTORAGEMULTISAMPLEEXTPROC glRenderbufferStorageMultisampleEXT =
            (PFNGLRENDERBUFFERSTORAGEMULTISAMPLEEXTPROC) eglGetProcAddress("glRenderbufferStorageMultisampleEXT");
    PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEEXTPROC glFramebufferTexture2DMultisampleEXT =
            (PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEEXTPROC) eglGetProcAddress("glFramebufferTexture2DMultisampleEXT");

    enum multisample_t {
        MSAA_OFF, MSAA_RENDER_TO_TEXTURE, MSAA_BLIT
    };

    multisample_t multisampleMode;
    if (multisamples > 1) {
        if (glFramebufferTexture2DMultisampleEXT != NULL && resolveDepth == false) {
            multisampleMode = MSAA_RENDER_TO_TEXTURE;
        } else {
            multisampleMode = MSAA_BLIT;
        }
    } else {
        multisampleMode = MSAA_OFF;
    }
    LOGV("FrameBufferObject::create: multisampleMode: %d, glRenderbufferStorageMultisampleEXT: %p, glFramebufferTexture2DMultisampleEXT: %p",
            multisampleMode, glRenderbufferStorageMultisampleEXT, glFramebufferTexture2DMultisampleEXT);

    if (MSAA_BLIT == multisampleMode) {
        GL( glGenRenderbuffers(1, &mColorBuffer) );
        GL( glBindRenderbuffer(GL_RENDERBUFFER, mColorBuffer));

        GLenum internalFormat;
        switch (colorFormat) {
        case VRAPI_TEXTURE_FORMAT_565:
            internalFormat = GL_RGB565;
            break;
        case VRAPI_TEXTURE_FORMAT_5551:
            internalFormat = GL_RGB5_A1;
            break;
        case VRAPI_TEXTURE_FORMAT_4444:
            internalFormat = GL_RGBA4;
            break;
        case VRAPI_TEXTURE_FORMAT_8888:
            internalFormat = GL_RGBA8;
            break;
        case VRAPI_TEXTURE_FORMAT_8888_sRGB:
            internalFormat = GL_SRGB8_ALPHA8;
            break;
        case VRAPI_TEXTURE_FORMAT_RGBA16F:
            internalFormat = GL_RGBA16F;
            break;
        default:
            FAIL("unknown colorFormat %i", colorFormat);
        }

        GL( glRenderbufferStorageMultisample(GL_RENDERBUFFER, multisamples, internalFormat, width, height) );
        GL( glBindRenderbuffer(GL_RENDERBUFFER, 0) );
    }

    if (depthFormat != VRAPI_TEXTURE_FORMAT_NONE) {
        if (resolveDepth) {
            mDepthTextureSwapChain = vrapi_CreateTextureSwapChain(VRAPI_TEXTURE_TYPE_2D, depthFormat, width, height, 1, true);
            if (nullptr == mDepthTextureSwapChain) {
                FAIL("vrapi_CreateTextureSwapChain for mDepthTextureSwapChain failed");
            }
            mDepthTextureSwapChainLength = vrapi_GetTextureSwapChainLength(mDepthTextureSwapChain);
        } else {
            mDepthTextureSwapChain = nullptr;
            mDepthTextureSwapChainLength = 0;
        }

        if (!resolveDepth || MSAA_BLIT == multisampleMode) {
            GLenum internalFormat;
            switch (depthFormat) {
            case VRAPI_TEXTURE_FORMAT_DEPTH_16:
                internalFormat = GL_DEPTH_COMPONENT16;
                break;
            case VRAPI_TEXTURE_FORMAT_DEPTH_24:
                internalFormat = GL_DEPTH_COMPONENT24;
                break;
            case VRAPI_TEXTURE_FORMAT_DEPTH_24_STENCIL_8:
                internalFormat = GL_DEPTH24_STENCIL8;
                break;
            default:
                FAIL("unknown depthFormat %i", depthFormat);
            }

            mDepthBuffers = new GLuint[mTextureSwapChainLength];

            for (int i = 0; i < mTextureSwapChainLength; ++i) {
                GL( glGenRenderbuffers(1, &mDepthBuffers[i]) );
                GL( glBindRenderbuffer(GL_RENDERBUFFER, mDepthBuffers[i]) );
                if (multisampleMode == MSAA_RENDER_TO_TEXTURE) {
                    GL( glRenderbufferStorageMultisampleEXT(GL_RENDERBUFFER, multisamples, internalFormat, width, height) );
                } else if (multisampleMode == MSAA_BLIT) {
                    GL( glRenderbufferStorageMultisample(GL_RENDERBUFFER, multisamples, internalFormat, width, height) );
                } else {
                    GL( glRenderbufferStorage(GL_RENDERBUFFER, internalFormat, width, height) );
                }
                GL( glBindRenderbuffer(GL_RENDERBUFFER, 0) );
            }
        }
    }

    mRenderFrameBuffers = new GLuint[mTextureSwapChainLength];
    if (MSAA_BLIT == multisampleMode) {
        mResolveFrameBuffers = new GLuint[mTextureSwapChainLength];
    }

    for (int i = 0; i < mTextureSwapChainLength; ++i) {
        const GLuint colorTexture = vrapi_GetTextureSwapChainHandle(mColorTextureSwapChain, i);
        const GLuint depthTexture = (mDepthTextureSwapChain != nullptr)
                ? vrapi_GetTextureSwapChainHandle(mDepthTextureSwapChain, i) : 0;

        GL( glGenFramebuffers(1, &mRenderFrameBuffers[i]) );
        GL( glBindFramebuffer(GL_FRAMEBUFFER, mRenderFrameBuffers[i]) );

        if (MSAA_RENDER_TO_TEXTURE == multisampleMode) {
            GL( glFramebufferTexture2DMultisampleEXT(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                    colorTexture, 0, multisamples) );
            if (VRAPI_TEXTURE_FORMAT_NONE != depthFormat) {
                GL( glFramebufferRenderbuffer( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mDepthBuffers[i]) );
            }

            GLenum renderStatus = GL( glCheckFramebufferStatus(GL_FRAMEBUFFER) );
            if (GL_FRAMEBUFFER_COMPLETE != renderStatus) {
                FAIL("fbo %i not complete: 0x%x", mRenderFrameBuffers[i], renderStatus );
            }
        }
        else if (multisampleMode == MSAA_BLIT) {
            GL( glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, mColorBuffer) );

            if (depthFormat != VRAPI_TEXTURE_FORMAT_NONE) {
                GL( glFramebufferRenderbuffer( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mDepthBuffers[i]) );
            }
            GLenum renderStatus = GL( glCheckFramebufferStatus(GL_FRAMEBUFFER) );
            if (renderStatus != GL_FRAMEBUFFER_COMPLETE) {
                FAIL("fbo %i not complete: 0x%x", mRenderFrameBuffers[i], renderStatus);
            }
            GL( glBindFramebuffer(GL_FRAMEBUFFER, 0) );

            GL( glGenFramebuffers(1, &mResolveFrameBuffers[i]) );
            GL( glBindFramebuffer(GL_FRAMEBUFFER, mResolveFrameBuffers[i]) );
            GL( glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture, 0) );
            if (depthFormat != VRAPI_TEXTURE_FORMAT_NONE && resolveDepth) {
                GL( glFramebufferTexture2D( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0) );
            }
            GLenum resolveStatus = GL( glCheckFramebufferStatus(GL_FRAMEBUFFER) );
            if (resolveStatus != GL_FRAMEBUFFER_COMPLETE) {
                FAIL("fbo %i not complete: 0x%x", mResolveFrameBuffers[i], resolveStatus);
            }
        } else {
            GL( glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture, 0) );
            if (depthFormat != VRAPI_TEXTURE_FORMAT_NONE) {
                if (resolveDepth) {
                    GL( glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0) );
                } else {
                    GL( glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mDepthBuffers[i]) );
                }
            }
            GLenum renderStatus = GL( glCheckFramebufferStatus(GL_FRAMEBUFFER) );
            if (renderStatus != GL_FRAMEBUFFER_COMPLETE) {
                FAIL("fbo %i not complete: 0x%x", mRenderFrameBuffers[i], renderStatus);
            }
        }

        GL( glScissor(0, 0, width, height) );
        GL( glViewport(0, 0, width, height) );
        GL( glBindFramebuffer(GL_FRAMEBUFFER, 0) );
    }

    return true;
}

void FrameBufferObject::destroy() {
    if (nullptr != mRenderFrameBuffers) {
        GL(glDeleteFramebuffers(mTextureSwapChainLength, mRenderFrameBuffers));
        delete[] mRenderFrameBuffers;
        mRenderFrameBuffers = nullptr;
    }

    if (nullptr != mResolveFrameBuffers) {
        GL(glDeleteFramebuffers(mTextureSwapChainLength, mResolveFrameBuffers));
        delete[] mResolveFrameBuffers;
        mResolveFrameBuffers = nullptr;
    }

    if (nullptr != mDepthBuffers) {
        GL(glDeleteRenderbuffers(mTextureSwapChainLength, mDepthBuffers));
        delete[] mDepthBuffers;
        mDepthBuffers = nullptr;
    }

    if (0 != mColorBuffer) {
        GL(glDeleteRenderbuffers(1, &mColorBuffer));
        mColorBuffer = 0;
    }

    if (mDepthTextureSwapChain) {
        vrapi_DestroyTextureSwapChain(mDepthTextureSwapChain);
    }

    vrapi_DestroyTextureSwapChain(mColorTextureSwapChain);

    clear();
}

void FrameBufferObject::bind() {
    GL(glBindFramebuffer(GL_FRAMEBUFFER, mRenderFrameBuffers[mTextureSwapChainIndex]));
}

void FrameBufferObject::unbind() {
    GL(glBindFramebuffer(GL_FRAMEBUFFER, 0));
}

void FrameBufferObject::resolve() {
    if (nullptr == mDepthTextureSwapChain) {
        const GLenum attachments[] = {GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT};
        GL( glInvalidateFramebuffer(GL_FRAMEBUFFER, sizeof(attachments)/sizeof(GLenum), attachments) );
    }

    if (nullptr != mResolveFrameBuffers) {
        GL( glBindFramebuffer(GL_READ_FRAMEBUFFER, mRenderFrameBuffers[mTextureSwapChainIndex]) );
        GL( glBindFramebuffer(GL_DRAW_FRAMEBUFFER, mResolveFrameBuffers[mTextureSwapChainIndex]) );
        GL( glBlitFramebuffer(  0, 0, mWidth, mHeight, 0, 0, mWidth, mHeight,
                            GL_COLOR_BUFFER_BIT | (nullptr != mDepthTextureSwapChain ? GL_DEPTH_BUFFER_BIT : 0 ),
                            GL_NEAREST ) );

        if (nullptr != mDepthTextureSwapChain) {
            const GLenum attachments[] = {GL_COLOR_ATTACHMENT0};
            GL( glInvalidateFramebuffer(GL_FRAMEBUFFER, sizeof(attachments)/sizeof(GLenum), attachments) );
        } else {
            const GLenum attachments[] = {GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT};
            GL( glInvalidateFramebuffer(GL_FRAMEBUFFER, sizeof(attachments)/sizeof(GLenum), attachments) );
        }
    }

    unbind();
}

void FrameBufferObject::advance() {
    mTextureSwapChainIndex = (mTextureSwapChainIndex + 1) % mTextureSwapChainLength;
}

} //namespace gvr
