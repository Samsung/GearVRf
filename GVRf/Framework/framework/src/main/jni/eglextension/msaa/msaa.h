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

#define __gl2_h_
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>
#include <GLES3/gl3ext.h>

namespace gvr {

class MSAA {
private:
    MSAA();

public:
    static int getMaxSampleCount() {
        GLint max_sample_count;
        const int MAX_SAMPLES_EXT = 0x8D57;
        glGetIntegerv(MAX_SAMPLES_EXT, &max_sample_count);
        return max_sample_count;
    }

    static void glRenderbufferStorageMultisample(GLenum target, GLsizei samples,
            GLenum internalformat, GLsizei width, GLsizei height) {
        PFNGLRENDERBUFFERSTORAGEMULTISAMPLEIMG glRenderbufferStorageMultisampleIMG =
                reinterpret_cast<PFNGLRENDERBUFFERSTORAGEMULTISAMPLEIMG>(eglGetProcAddress(
                        "glRenderbufferStorageMultisampleEXT"));
        glRenderbufferStorageMultisampleIMG(target, samples, internalformat,
                width, height);
    }

    static void glFramebufferTexture2DMultisample(GLenum target,
            GLenum attachment, GLenum textarget, GLuint texture, GLint level,
            GLsizei samples) {
        PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEIMG glFramebufferTexture2DMultisampleIMG =
                reinterpret_cast<PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEIMG>(eglGetProcAddress(
                        "glFramebufferTexture2DMultisampleEXT"));
        glFramebufferTexture2DMultisampleIMG(target, attachment, textarget,
                texture, level, samples);
    }
};

}

#endif
