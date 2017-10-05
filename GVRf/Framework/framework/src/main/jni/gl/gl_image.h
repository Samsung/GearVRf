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

#ifndef GL_IMAGE_H_
#define GL_IMAGE_H_

#ifndef GL_EXT_texture_filter_anisotropic
#define GL_EXT_texture_filter_anisotropic 1
#define GL_TEXTURE_MAX_ANISOTROPY_EXT     0x84FE
#define GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT 0x84FF
#endif /* GL_EXT_texture_filter_anisotropic */

#include <cstdlib>
#include "objects/textures/image.h"
#include "objects/textures/texture.h"
#include "gl/gl_headers.h"

namespace gvr {
class GLImage
{
public:
    explicit GLImage(GLenum target)
            : mId(0),
              mGLTarget(target),
              mTexParamsDirty(true)
    { }
    explicit GLImage(GLenum target, int id)
            : mId(id),
              mGLTarget(target),
              mTexParamsDirty(true)
    { }

    GLImage()
            : mGLTarget(0),
              mId(0),
              mTexParamsDirty(true)
    { }

    virtual ~GLImage();

    int             id() const { return mId; }
    virtual int     getId() { return mId; }
    virtual bool    updateGPU();
    void            updateTexParams(const TextureParameters&);
    GLenum          getTarget() const { return mGLTarget; }
    void            setTexId(GLuint id) { mId = id; }
    void            setTexParamsDirty(bool flag) {mTexParamsDirty = flag; }
private:
    GLImage(const GLImage& gl_texture);
    GLImage(GLImage&& gl_texture);
    GLImage& operator=(const GLImage& gl_texture);
    GLImage& operator=(GLImage&& gl_texture);

protected:

    virtual GLuint  createTexture();

    static GLenum MapWrap[];
    static GLenum MapFilter[];
    bool    mTexParamsDirty;
    GLenum  mGLTarget;
    GLuint  mId;
    TextureParameters mTexParams;
};

}
#endif
