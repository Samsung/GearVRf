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
 * RAII class for GL textures.
 ***************************************************************************/

#ifndef GL_TEXTURE_H_
#define GL_TEXTURE_H_

#ifndef GL_EXT_texture_filter_anisotropic
#define GL_EXT_texture_filter_anisotropic 1
#define GL_TEXTURE_MAX_ANISOTROPY_EXT     0x84FE
#define GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT 0x84FF
#endif /* GL_EXT_texture_filter_anisotropic */

#ifndef GL_ES_VERSION_3_0
#include "GLES3/gl3.h"
#endif

#include "util/gvr_log.h"
#include <cstdlib>

#include "engine/memory/gl_delete.h"
#include "objects/gl_pending_task.h"

#define MAX_TEXTURE_PARAM_NUM 10

namespace gvr {
class GLTexture : public GLPendingTask {
public:
    explicit GLTexture(GLenum target)
    : target_(target)
    , pending_gl_task_(GL_TASK_NONE)
    {
        pending_gl_task_ = GL_TASK_INIT_NO_PARAM;
    }
    explicit GLTexture(GLenum target, int texture_id) :
            target_(target) {
        id_ = texture_id;
    }
    explicit GLTexture(GLenum target, int* texture_parameters) :
            target_(target) {
        pending_gl_task_ = GL_TASK_INIT_WITH_PARAM;
        memcpy(texture_parameters_, texture_parameters, sizeof(int) * 5);
    }

    virtual ~GLTexture() {
        if (0 != id_ && deleter_) {
            deleter_->queueTexture(id_);
        }
    }

    GLuint id() {
        runPendingGL();
        return id_;
    }

    GLenum target() const {
        return target_;
    }

    virtual void runPendingGL() {
        switch (pending_gl_task_) {
        case GL_TASK_NONE:
            return;

        case GL_TASK_INIT_NO_PARAM: {
            // The deleter needs to be obtained from the GL thread
            deleter_= getDeleterForThisThread();

            glGenTextures(1, &id_);
            glBindTexture(target_, id_);
            glTexParameteri(target_, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(target_, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(target_, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glTexParameteri(target_, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(target_, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glBindTexture(target_, 0);
            break;
        }

        case GL_TASK_INIT_WITH_PARAM: {
            deleter_= getDeleterForThisThread();

            // Sets the new MIN FILTER
            GLenum min_filter_type_ = texture_parameters_[0];

            // Sets the MAG FILTER
            GLenum mag_filter_type_ = texture_parameters_[1];

            // Sets the wrap parameter for texture coordinate S
            GLenum wrap_s_type_ = texture_parameters_[3];

            // Sets the wrap parameter for texture coordinate S
            GLenum wrap_t_type_ = texture_parameters_[4];

            glGenTextures(1, &id_);
            glBindTexture(target_, id_);

            // Sets the anisotropic filtering if the value provided is greater than 1 because 1 is the default value
            if (texture_parameters_[2] > 1.0f) {
                glTexParameterf(target_, GL_TEXTURE_MAX_ANISOTROPY_EXT,
                        (float) texture_parameters_[2]);
            }

            glTexParameteri(target_, GL_TEXTURE_WRAP_S, wrap_s_type_);
            glTexParameteri(target_, GL_TEXTURE_WRAP_T, wrap_t_type_);
            glTexParameteri(target_, GL_TEXTURE_MIN_FILTER, min_filter_type_);
            glTexParameteri(target_, GL_TEXTURE_MAG_FILTER, mag_filter_type_);
            glBindTexture(target_, 0);
            break;
        }

        } // switch

        pending_gl_task_ = GL_TASK_NONE;
    }

private:
    GLTexture(const GLTexture& gl_texture);
    GLTexture(GLTexture&& gl_texture);
    GLTexture& operator=(const GLTexture& gl_texture);
    GLTexture& operator=(GLTexture&& gl_texture);

private:
    GLuint id_ = 0;
    GLenum target_;
    GlDelete* deleter_;

    // Enum for pending GL tasks. Keep a comma with each line
    // for easier merging.
    enum {
        GL_TASK_NONE = 0,
        GL_TASK_INIT_NO_PARAM,
        GL_TASK_INIT_WITH_PARAM,
    };
    int pending_gl_task_;

    int texture_parameters_[MAX_TEXTURE_PARAM_NUM];
};

}
#endif
