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
 * Textures targeting GL_TEXTURE_EXTERNAL_OES.
 ***************************************************************************/

#ifndef VIDEO_TEXTURE_H_
#define VIDEO_TEXTURE_H_

#include "gl/gl_texture.h"

namespace gvr {

class GLExternalTexture: public GLTexture
{
public:
    GLExternalTexture() : GLTexture()
    {
        target_ = GL_TEXTURE_EXTERNAL_OES;
    }

private:
    GLExternalTexture(const GLExternalTexture&e);
    GLExternalTexture(GLExternalTexture&&);
    GLExternalTexture& operator=(const GLExternalTexture&);
    GLExternalTexture& operator=(GLExternalTexture&&);
};

}
#endif
