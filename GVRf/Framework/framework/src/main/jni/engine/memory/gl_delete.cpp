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

#include "util/gvr_log.h"
#include "gl_delete.h"
#include "util/gvr_cpp_stack_trace.h"

//#define VERBOSE_LOGGING

namespace gvr {

pthread_key_t deleter_key;

void GlDelete::logInvalidParameter(const char *funcName) {
    LOGW("GlDelete::%s is called with an invalid parameter", funcName);
    printStackTrace();
}

void GlDelete::queueBuffer(GLuint buffer) {
    if (buffer == GVR_INVALID) {
        logInvalidParameter(__func__);
        return;
    }

    lock();
    buffers_.push_back(buffer);
#ifdef VERBOSE_LOGGING
    LOGD("queueBuffer(%d) buffers_.size() = %d", buffer, buffers_.size());
#endif
    dirty = true;
    unlock();
}

void GlDelete::queueFrameBuffer(GLuint buffer) {
    if (buffer == GVR_INVALID) {
        logInvalidParameter(__func__);
        return;
    }

    lock();
    frame_buffers_.push_back(buffer);
#ifdef VERBOSE_LOGGING
    LOGD("queueFrameBuffer(%d) frame_buffers_.size() = %d", buffer,
            frame_buffers_.size());
#endif
    dirty = true;
    unlock();
}

void GlDelete::queueProgram(GLuint program) {
    if (program == GVR_INVALID) {
        logInvalidParameter(__func__);
        return;
    }

    lock();
    programs_.push_back(program);
#ifdef VERBOSE_LOGGING
    LOGD("queueProgram(%d) programs_.size() = %d", program, programs_.size());
#endif
    dirty = true;
    unlock();
}

void GlDelete::queueRenderBuffer(GLuint buffer) {
    if (buffer == GVR_INVALID) {
        logInvalidParameter(__func__);
        return;
    }

    lock();
    render_buffers_.push_back(buffer);
#ifdef VERBOSE_LOGGING
    LOGD("queueRenderBuffer(%d) render_buffers_.size() = %d", buffer,
            render_buffers_.size());
#endif
    dirty = true;
    unlock();
}

void GlDelete::queueShader(GLuint shader) {
    if (shader == GVR_INVALID) {
        logInvalidParameter(__func__);
        return;
    }

    lock();
    shaders_.push_back(shader);
#ifdef VERBOSE_LOGGING
    LOGD("queueShader(%d) shaders_.size() = %d", shader, shaders_.size());
#endif
    dirty = true;
    unlock();
}

void GlDelete::queueTexture(GLuint texture) {
    if (texture == GVR_INVALID) {
        logInvalidParameter(__func__);
        return;
    }

    lock();
    textures_.push_back(texture);
#ifdef VERBOSE_LOGGING
    LOGD("queueTexture(%d) textures_.size() = %d", texture, textures_.size());
#endif
    dirty = true;
    unlock();
}

void GlDelete::queueVertexArray(GLuint vertex_array) {
    if (vertex_array == GVR_INVALID) {
        logInvalidParameter(__func__);
        return;
    }

    lock();
    vertex_arrays_.push_back(vertex_array);
#ifdef VERBOSE_LOGGING
    LOGD("queueVertexArray(%d) vertex_arrays_.size() = %d", vertex_array,
            vertex_arrays_.size());
#endif
    dirty = true;
    unlock();
}

void GlDelete::processQueues() {
    /*
     * Do an unsynchronized check of the dirty flag, so that we don't have to
     * call lock() on each and every frame. The consequences of 'just missing'
     * a queue op and leaving a handle on a queue for an extra frame are quite
     * minimal, but locking every frame is not free.
     */
    if (dirty) {
        lock();
#ifdef VERBOSE_LOGGING
        LOGD("GlDelete::processQueues()");
#endif
        if (buffers_.size() > 0) {
            glDeleteBuffers(buffers_.size(), buffers_.data());
            buffers_.clear();
        }
        if (frame_buffers_.size() > 0) {
            glDeleteFramebuffers(frame_buffers_.size(), frame_buffers_.data());
            frame_buffers_.clear();
        }
        if (programs_.size() > 0) {
            for (int index = 0, size = programs_.size(); index < size;
                    ++index) {
                glDeleteProgram(programs_[index]);
            }
            programs_.clear();
        }
        if (render_buffers_.size() > 0) {
            glDeleteRenderbuffers(render_buffers_.size(),
                    render_buffers_.data());
            render_buffers_.clear();
        }
        if (shaders_.size() > 0) {
            for (int index = 0, size = shaders_.size(); index < size; ++index) {
                glDeleteShader(shaders_[index]);
            }
            shaders_.clear();
        }
        if (textures_.size() > 0) {
            glDeleteTextures(textures_.size(), textures_.data());
            textures_.clear();
        }
        if (vertex_arrays_.size() > 0) {
            glDeleteVertexArrays(vertex_arrays_.size(), vertex_arrays_.data());
            vertex_arrays_.clear();
        }
        dirty = false;
        unlock();
    }
}

}
