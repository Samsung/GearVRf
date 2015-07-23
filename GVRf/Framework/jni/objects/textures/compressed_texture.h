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
 * Texture from a (Java-loaded) byte stream containing a compressed texture
 ***************************************************************************/

#ifndef compressed_texture_H_
#define compressed_texture_H_

//#include <GLES3/gl3.h>
#include "objects/textures/texture.h"
#include "util/gvr_log.h"

namespace gvr {
class CompressedTexture: public Texture {
public:

    // The constructor to use when loading a mipmap chain, from Java
    explicit CompressedTexture(GLenum target) :
            Texture(new GLTexture(target)), target(target) {
        glBindTexture(target, gl_texture_->id());
    }

    // The constructor to use when loading a single-level texture
    explicit CompressedTexture(GLenum target, GLenum internalFormat,
            GLsizei width, GLsizei height, GLsizei imageSize, const void* data,
            int* texture_parameters) :
            Texture(new GLTexture(target, texture_parameters)), target(target) {
        glBindTexture(target, gl_texture_->id());
        glCompressedTexImage2D(target, 0, internalFormat, width, height, 0,
                imageSize, data);
    }

    GLenum getTarget() const {
        return target;
    }

private:
    CompressedTexture(const CompressedTexture& compressed_texture);
    CompressedTexture(CompressedTexture&& compressed_texture);
    CompressedTexture& operator=(const CompressedTexture& compressed_texture);
    CompressedTexture& operator=(CompressedTexture&& compressed_texture);

private:
    GLenum const target;
};

}
#endif
