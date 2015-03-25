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
        return gl_texture_->id();
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
};

}

#endif
