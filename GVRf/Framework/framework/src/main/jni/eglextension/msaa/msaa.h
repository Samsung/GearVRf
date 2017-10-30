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
 * Multi Sampling Anti Aliasing
 ***************************************************************************/

#ifndef MSAA_H_
#define MSAA_H_

#include "gl/gl_headers.h"

namespace gvr {

    using PFNGLRENDERBUFFERSTORAGEMULTISAMPLEIMG = void (*) (GLenum target, GLsizei samples, GLenum internalformat, GLsizei width, GLsizei height);
    using PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEIMG = void (*) (GLenum target, GLenum attachment, GLenum textarget, GLuint texture, GLint level, GLsizei samples);
    using PFNGLRENDERBUFFERSTORAGEMULTISAMPLE = void (GL_APIENTRYP)(GLenum target,
                                                                    GLsizei samples, GLenum internalformat, GLsizei width, GLsizei height);

    namespace MSAA {
        static int getMaxSampleCount() {
            GLint max_sample_count;
            const int MAX_SAMPLES_EXT = 0x8D57;
            glGetIntegerv(MAX_SAMPLES_EXT, &max_sample_count);
            return max_sample_count;
        }

        static void glRenderbufferStorageMultisampleIMG(GLenum target, GLsizei samples,
                GLenum internalformat, GLsizei width, GLsizei height) {
            PFNGLRENDERBUFFERSTORAGEMULTISAMPLEIMG pGlRenderbufferStorageMultisampleIMG =
                    reinterpret_cast<PFNGLRENDERBUFFERSTORAGEMULTISAMPLEIMG>(eglGetProcAddress(
                            "glRenderbufferStorageMultisampleEXT"));
            pGlRenderbufferStorageMultisampleIMG(target, samples, internalformat,
                                                 width, height);
        }

        static void glRenderbufferStorageMultisample(GLenum target,
                GLsizei samples, GLenum internalformat, GLsizei width,
                GLsizei height) {
            PFNGLRENDERBUFFERSTORAGEMULTISAMPLE pGlRenderBufferStorageMultisample =
                    reinterpret_cast<PFNGLRENDERBUFFERSTORAGEMULTISAMPLE>(eglGetProcAddress(
                            "glRenderbufferStorageMultisample"));
            pGlRenderBufferStorageMultisample(target, samples, internalformat, width,
                                              height);
        }

        static void glFramebufferTexture2DMultisample(GLenum target,
                GLenum attachment, GLenum textarget, GLuint texture, GLint level,
                GLsizei samples) {
            PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEIMG pGlFramebufferTexture2DMultisampleIMG =
                    reinterpret_cast<PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEIMG>(eglGetProcAddress(
                            "glFramebufferTexture2DMultisampleEXT"));
            pGlFramebufferTexture2DMultisampleIMG(target, attachment, textarget,
                                                  texture, level, samples);
        }
    }
}

#endif
