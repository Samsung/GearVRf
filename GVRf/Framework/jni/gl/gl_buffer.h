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
 * RAII class for GL buffers.
 ***************************************************************************/

#ifndef GL_BUFFER_H_
#define GL_BUFFER_H_

#include "GLES3/gl3.h"

#include "engine/memory/gl_delete.h"

namespace gvr {
class GLBuffer {
public:
    GLBuffer() {
        glGenBuffers(1, &id_);
    }

    ~GLBuffer() {
        gl_delete.queueBuffer(id_);
    }

    GLuint id() const {
        return id_;
    }

private:
    GLBuffer(const GLBuffer& gl_buffer);
    GLBuffer(GLBuffer&& gl_buffer);
    GLBuffer& operator=(const GLBuffer& gl_buffer);
    GLBuffer& operator=(GLBuffer&& gl_buffer);

private:
    GLuint id_;
};

}

#endif
