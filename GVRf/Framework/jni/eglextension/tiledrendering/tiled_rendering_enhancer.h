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
 * Enables more efficient tiled rendering when rendering on the front buffer.
 ***************************************************************************/

#ifndef TILED_RENDERING_H_
#define TILED_RENDERING_H_

#define __gl2_h_
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>
#include <GLES3/gl3ext.h>

namespace gvr {

class TiledRenderingEnhancer {
private:
    TiledRenderingEnhancer();

public:
    static void start(GLuint x, GLuint y, GLuint width, GLuint height,
            GLbitfield preserveMask) {
        PFNGLSTARTTILINGQCOMPROC start =
                reinterpret_cast<PFNGLSTARTTILINGQCOMPROC>(eglGetProcAddress(
                        "glStartTilingQCOM"));
        start(x, y, width, height, preserveMask);
    }

    static void end(GLbitfield preserveMask) {
        PFNGLENDTILINGQCOMPROC end =
                reinterpret_cast<PFNGLENDTILINGQCOMPROC>(eglGetProcAddress(
                        "glEndTilingQCOM"));
        end(preserveMask);
    }

    static bool available() {
        PFNGLSTARTTILINGQCOMPROC start =
                reinterpret_cast<PFNGLSTARTTILINGQCOMPROC>(eglGetProcAddress(
                        "glStartTilingQCOM"));
        PFNGLENDTILINGQCOMPROC end =
                reinterpret_cast<PFNGLENDTILINGQCOMPROC>(eglGetProcAddress(
                        "glEndTilingQCOM"));
        return start && end;
    }
};

}

#endif
