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

#include "GLES3/gl3.h"
#include "util/gvr_log.h"

#include "engine/memory/gl_delete.h"

namespace gvr {
class GLTexture {
public:
    explicit GLTexture(GLenum target) :
            target_(target) {
        glGenTextures(1, &id_);
        glBindTexture(target, id_);
        glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(target, 0);
    }

    explicit GLTexture(GLenum target, int* texture_parameters) :
            target_(target) {
        // Sets the new MIN FILTER
        GLenum min_filter_type_ = texture_parameters[0];

        // Sets the MAG FILTER
        GLenum mag_filter_type_ = texture_parameters[1];

        // Sets the wrap parameter for texture coordinate S
        GLenum wrap_s_type_ = texture_parameters[3];

        // Sets the wrap parameter for texture coordinate S
        GLenum wrap_t_type_ = texture_parameters[4];

        glGenTextures(1, &id_);
        glBindTexture(target, id_);

        // Sets the anisotropic filtering if the value provided is greater than 1 because 1 is the default value
        if (texture_parameters[2] > 1.0f) {
            glTexParameterf(target, GL_TEXTURE_MAX_ANISOTROPY_EXT,
                    (float) texture_parameters[2]);
        }

        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrap_s_type_);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrap_t_type_);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, min_filter_type_);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, mag_filter_type_);
        glBindTexture(target, 0);
    }

    ~GLTexture() {
        gl_delete.queueTexture(id_);
    }

    GLuint id() const {
        return id_;
    }

    GLenum target() const {
        return target_;
    }

private:
    GLTexture(const GLTexture& gl_texture);
    GLTexture(GLTexture&& gl_texture);
    GLTexture& operator=(const GLTexture& gl_texture);
    GLTexture& operator=(GLTexture&& gl_texture);

private:
    GLuint id_;
    GLenum target_;
};

}
#endif
