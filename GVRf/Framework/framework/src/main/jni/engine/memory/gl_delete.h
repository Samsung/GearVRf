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

#include "util/gvr_log.h"
#include "util/gvr_cpp_stack_trace.h"

#include <vector>
#include <pthread.h>
#include <unistd.h>
#ifndef GL_ES_VERSION_3_0
#include "GLES3/gl3.h"
#endif

#define GVR_INVALID 0

namespace gvr {

extern pthread_key_t deleter_key;

class GlDelete {

public:
    /**
     * Before using this class this method must be called once
     * and only once per-process
     */
    static void createTlsKey() {
        //corresponding pthread_key_delete missing intentionally as this is
        //needed for as long as the process is alive
        int err = pthread_key_create(&deleter_key, nullptr);
        if (0 != err) {
            LOGE("fatal error: pthread_key_create failed with %d!", err);
            std::terminate();
        }
    }

    GlDelete() {
        int err = pthread_mutex_init(&mutex, nullptr);
        if (0 != err) {
            LOGE("fatal error: pthread_mutex_init failed with %d!", err);
            std::terminate();
        }
        err = pthread_setspecific(deleter_key, this);
        if (0 != err) {
            LOGE("fatal error: pthread_setspecific failed with %d!", err);
            std::terminate();
        }

        LOGV("GlDelete(): %p tid: %d", this, gettid());
    }

    ~GlDelete() {
        LOGV("~GlDelete(): %p tid: %d", this, gettid());
        int err = pthread_mutex_destroy(&mutex);
        if (0 != err) {
            LOGE("fatal error: pthread_mutex_destroy failed with %d!", err);
            std::terminate();
        }
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

    void logInvalidParameter(const char *msg);

    std::vector<GLuint> buffers_;
    std::vector<GLuint> frame_buffers_;
    std::vector<GLuint> programs_;
    std::vector<GLuint> render_buffers_;
    std::vector<GLuint> shaders_;
    std::vector<GLuint> textures_;
    std::vector<GLuint> vertex_arrays_;
};

/**
 * The assumption is threads that do know they are supposed to have
 * a deleter may only call this method.
 */
static GlDelete* getDeleterForThisThread() {
    GlDelete* deleter = static_cast<GlDelete*>(pthread_getspecific(deleter_key));
    if (nullptr == deleter) {
        printStackTrace();
        LOGE("fatal error: no deleter associated with this thread!");
        std::terminate();
    }
    return deleter;
}

}

#endif
