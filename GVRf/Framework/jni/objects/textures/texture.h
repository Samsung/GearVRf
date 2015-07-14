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
 * Textures.
 ***************************************************************************/

#ifndef TEXTURE_H_
#define TEXTURE_H_

#include "gl/gl_texture.h"
#include "objects/recyclable_object.h"

namespace gvr {

class Texture: public RecyclableObject {
public:
    virtual ~Texture() {
        recycle();
    }

    virtual void recycle() {
        if (gl_texture_ != 0) {
            delete gl_texture_;
            gl_texture_ = 0;
        }
    }

    virtual GLuint getId() const {
        if (gl_texture_ == 0) {
            // must be recycled already. The caller will handle error.
            return 0;
        }
        return gl_texture_->id();
    }

    virtual void updateTextureParameters(float* texture_parameters) {

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

        glBindTexture(target, getId());

        // Sets the anisotropic filtering if the value provided is greater than 1 because 1 is the default value
        if (texture_parameters[2] > 1.0f) {
            glTexParameterf(target, GL_TEXTURE_MAX_ANISOTROPY_EXT,
                    texture_parameters[2]);
        }

        LOGE("Updated Texture Parameters");
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

    virtual GLenum getTarget() const = 0;

protected:
    Texture(GLTexture* gl_texture) :
            RecyclableObject() {
        gl_texture_ = gl_texture;
    }

    const GLTexture* gl_texture_;

private:
    Texture(const Texture& texture);
    Texture(Texture&& texture);
    Texture& operator=(const Texture& texture);
    Texture& operator=(Texture&& texture);

private:
    static const GLenum target = GL_TEXTURE_2D;
};

}

#endif
