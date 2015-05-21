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

#include "GLES3/gl3.h"

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

    ~GLTexture() {
        glDeleteTextures(1, &id_);
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
