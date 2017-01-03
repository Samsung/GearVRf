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

#include <cstdlib>
#include "objects/textures/image.h"
#include "objects/textures/texture.h"
#include "gl/gl_headers.h"
#include "engine/memory/gl_delete.h"

namespace gvr {
class GLTexture : public Texture
{
public:
    explicit GLTexture() : Texture(),
                           id_(0),
                           deleter_(NULL) { }

    explicit GLTexture(int texture_type) :
            Texture(texture_type),
            id_(0),
            deleter_(NULL) { }

    virtual ~GLTexture();
    virtual bool isReady();
    virtual bool updateGPU();

    virtual int getId()
    {
        isReady();
        return id();
    }

    GLuint id() const
    {
        return id_;
    }

    bool bindTexture(int texIndex, int loc)
    {
        glActiveTexture(GL_TEXTURE0 + texIndex);
        glBindTexture(getTarget(), id());
        glUniform1i(loc, texIndex);
    }

private:
    GLTexture(const GLTexture& gl_texture);
    GLTexture(GLTexture&& gl_texture);
    GLTexture& operator=(const GLTexture& gl_texture);
    GLTexture& operator=(GLTexture&& gl_texture);

    void updateTexParams();
    bool updateImage();

protected:
    static GLenum MapWrap[];
    static GLenum MapFilter[];
    GLuint id_;
    GlDelete* deleter_;
};

}
#endif
