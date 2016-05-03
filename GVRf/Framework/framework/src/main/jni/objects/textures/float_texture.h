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
 * Textures generated from float point arrays
 ***************************************************************************/

#ifndef FLOAT_TEXTURE_H_
#define FLOAT_TEXTURE_H_

#include "objects/textures/texture.h"
#include "util/gvr_log.h"

namespace gvr {
class FloatTexture: public Texture {
public:
    explicit FloatTexture() :
            Texture(new GLTexture(TARGET)) {
    }

    bool update(int width, int height, float* data) {
        glBindTexture(GL_TEXTURE_2D, gl_texture_->id());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RG32F, width, height, 0,
                GL_RG, GL_FLOAT, data);
        return (glGetError() == 0) ? 1 : 0;
    }

    GLenum getTarget() const {
        return TARGET;
    }

private:
    FloatTexture(const FloatTexture& float_texture);
    FloatTexture(FloatTexture&& float_texture);
    FloatTexture& operator=(const FloatTexture& float_texture);
    FloatTexture& operator=(FloatTexture&& float_texture);

private:
    static const GLenum TARGET = GL_TEXTURE_2D;
};

}
#endif
