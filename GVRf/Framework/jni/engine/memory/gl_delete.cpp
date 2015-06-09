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

//#include "util/gvr_log.h"
#include "gl_delete.h"

namespace gvr {

GlDelete gl_delete;

void GlDelete::queueBuffer(GLuint buffer) {
    lock();
    buffers_.push_back(buffer);
//    LOGD("queueBuffer(%d) buffers_.size() = %d", buffer, buffers_.size());
    dirty = true;
    unlock();
}

void GlDelete::queueFrameBuffer(GLuint buffer) {
    lock();
    frame_buffers_.push_back(buffer);
//    LOGD("queueFrameBuffer(%d) frame_buffers_.size() = %d", buffer,
//            frame_buffers_.size());
    dirty = true;
    unlock();
}

void GlDelete::queueProgram(GLuint program) {
    lock();
    programs_.push_back(program);
//    LOGD("queueProgram(%d) programs_.size() = %d", program, programs_.size());
    dirty = true;
    unlock();
}

void GlDelete::queueRenderBuffer(GLuint buffer) {
    lock();
    render_buffers_.push_back(buffer);
//    LOGD("queueRenderBuffer(%d) render_buffers_.size() = %d", buffer,
//            render_buffers_.size());
    dirty = true;
    unlock();
}

void GlDelete::queueShader(GLuint shader) {
    lock();
    shaders_.push_back(shader);
//    LOGD("queueShader(%d) shaders_.size() = %d", shader, shaders_.size());
    dirty = true;
    unlock();
}

void GlDelete::queueTexture(GLuint texture) {
    lock();
    textures_.push_back(texture);
//    LOGD("queueTexture(%d) textures_.size() = %d", texture, textures_.size());
    dirty = true;
    unlock();
}

void GlDelete::queueVertexArray(GLuint vertex_array) {
    lock();
    vertex_arrays_.push_back(vertex_array);
//    LOGD("queueVertexArray(%d) vertex_arrays_.size() = %d", vertex_array,
//            vertex_arrays_.size());
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
//    LOGD("GlDelete::processQueues()");
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
