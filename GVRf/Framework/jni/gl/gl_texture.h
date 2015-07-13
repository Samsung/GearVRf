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

    explicit GLTexture(GLenum target, float* texture_parameters) :
            target_(target) {
        // Sets the new MIN FILTER
        GLenum min_filter_type_;

        switch ((int) texture_parameters[0]) {
        case 0:
            min_filter_type_ = GL_LINEAR;
            break;
        case 1:
            min_filter_type_ = GL_NEAREST;
            break;
        case 2:
            min_filter_type_ = GL_NEAREST_MIPMAP_NEAREST;
            break;
        case 3:
            min_filter_type_ = GL_NEAREST_MIPMAP_LINEAR;
            break;
        case 4:
            min_filter_type_ = GL_LINEAR_MIPMAP_NEAREST;
            break;
        case 5:
            min_filter_type_ = GL_LINEAR_MIPMAP_LINEAR;
            break;
        default:
            min_filter_type_ = GL_LINEAR;
            break;
        }

        // Sets the MAG FILTER
        GLenum mag_filter_type_;
        switch ((int) texture_parameters[1]) {
        case 0:
            mag_filter_type_ = GL_LINEAR;
            break;
        case 1:
            mag_filter_type_ = GL_NEAREST;
            break;
        default:
            mag_filter_type_ = GL_LINEAR;
            break;
        }

        // Sets the wrap parameter for texture coordinate S
        GLenum wrap_s_type_;
        switch ((int) texture_parameters[3]) {
        case 0:
            wrap_s_type_ = GL_CLAMP_TO_EDGE;
            break;
        case 1:
            wrap_s_type_ = GL_MIRRORED_REPEAT;
            break;
        case 2:
            wrap_s_type_ = GL_REPEAT;
            break;
        default:
            wrap_s_type_ = GL_CLAMP_TO_EDGE;
            break;
        }

        // Sets the wrap parameter for texture coordinate S
        GLenum wrap_t_type_;
        switch ((int) texture_parameters[4]) {
        case 0:
            wrap_t_type_ = GL_CLAMP_TO_EDGE;
            break;
        case 1:
            wrap_t_type_ = GL_MIRRORED_REPEAT;
            break;
        case 2:
            wrap_t_type_ = GL_REPEAT;
            break;
        default:
            wrap_t_type_ = GL_CLAMP_TO_EDGE;
            break;
        }

        glGenTextures(1, &id_);
        glBindTexture(target, id_);

        // Sets the anisotropic filtering if the value provided is greater than 1 because 1 is the default value
        if (texture_parameters[2] > 1.0f) {
            glTexParameterf(target, GL_TEXTURE_MAX_ANISOTROPY_EXT,
                    texture_parameters[2]);
        }

        LOGE("MIN: %d", (int)texture_parameters[0]);
        LOGE("MAG: %d", (int)texture_parameters[1]);
        LOGE("ANISO: %f", texture_parameters[2]);
        LOGE("WRAP S: %d", (int)texture_parameters[3]);
        LOGE("WRAP T: %d", (int)texture_parameters[4]);

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
