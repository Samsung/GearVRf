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
 * RAII class for GL frame buffers.
 ***************************************************************************/

#ifndef GL_FRAME_BUFFER_H_
#define GL_FRAME_BUFFER_H_

#include "GLES3/gl3.h"

#include "engine/memory/gl_delete.h"

namespace gvr {

class GLFrameBuffer {
public:
    GLFrameBuffer() {
        glGenFramebuffers(1, &id_);
    }

    ~GLFrameBuffer() {
        gl_delete.queueFrameBuffer(id_);
    }

    GLuint id() const {
        return id_;
    }

private:
    GLFrameBuffer(const GLFrameBuffer& gl_frame_buffer);
    GLFrameBuffer(GLFrameBuffer&& gl_frame_buffer);
    GLFrameBuffer& operator=(const GLFrameBuffer& gl_frame_buffer);
    GLFrameBuffer& operator=(GLFrameBuffer&& gl_frame_buffer);

private:
    GLuint id_;
};

}

#endif
