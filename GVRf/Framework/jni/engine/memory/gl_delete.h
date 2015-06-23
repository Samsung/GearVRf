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

#ifndef GL_DELETE_H_
#define GL_DELETE_H_

#include <vector>
#include <pthread.h>
#include "GLES3/gl3.h"

namespace gvr {
class GlDelete {

public:
    GlDelete() {
        pthread_mutex_init(&mutex, 0);
    }

    ~GlDelete() {
        pthread_mutex_destroy(&mutex);
    }

    void queueBuffer(GLuint buffer);
    void queueFrameBuffer(GLuint buffer);
    void queueProgram(GLuint program);
    void queueRenderBuffer(GLuint buffer);
    void queueShader(GLuint shader);
    void queueTexture(GLuint texture);
    void queueVertexArray(GLuint vertex_array);

    void processQueues();

private:

    pthread_mutex_t mutex;
    bool dirty = false;

    void lock() {
        pthread_mutex_lock(&mutex);
    }
    void unlock() {
        pthread_mutex_unlock(&mutex);
    }

    std::vector<GLuint> buffers_;
    std::vector<GLuint> frame_buffers_;
    std::vector<GLuint> programs_;
    std::vector<GLuint> render_buffers_;
    std::vector<GLuint> shaders_;
    std::vector<GLuint> textures_;
    std::vector<GLuint> vertex_arrays_;
};

extern GlDelete gl_delete;
}

#endif
