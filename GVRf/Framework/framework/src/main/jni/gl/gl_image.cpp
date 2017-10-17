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

#include "gl/gl_image.h"

namespace gvr {
GLenum GLImage::MapWrap[3] = { GL_CLAMP_TO_EDGE, GL_REPEAT, GL_MIRRORED_REPEAT };
GLenum GLImage::MapFilter[6] = { GL_NEAREST, GL_LINEAR, GL_NEAREST_MIPMAP_NEAREST,
                        GL_NEAREST_MIPMAP_LINEAR, GL_LINEAR_MIPMAP_NEAREST, GL_LINEAR_MIPMAP_LINEAR };

GLImage::~GLImage()
{
    if (0 != mId)
    {
        glDeleteTextures(1,&mId);
    }
}

bool GLImage::updateGPU()
{
    if (mGLTarget == 0)
    {
        return false;
    }
    if (mId == 0)
    {
        mId = createTexture();
    }
    if (mId != 0)
    {
        glBindTexture(mGLTarget, mId);
        checkGLError("GLImage::bindTexture");
    }
    else
    {
        glBindTexture(mGLTarget, 0);
        return false;
    }
    if (mId && mTexParamsDirty)
    {
        mTexParamsDirty = false;
        updateTexParams();
    }
    return (mId != 0);
}


GLuint GLImage::createTexture()
{
    GLuint id;
    glGenTextures(1, &id);
    LOGV("GLImage: texture id created is %d", id);
    checkGLError("GLImage::createTexture");
    return id;
}


void GLImage::updateTexParams(const TextureParameters& texparams)
{
    // Sets the new MIN FILTER
    GLenum min_filter_type_ = MapFilter[texparams.getMinFilter()];

    // Sets the MAG FILTER
    GLenum mag_filter_type_ = MapFilter[texparams.getMagFilter()];

    // Sets the wrap parameter for texture coordinate S
    GLenum wrap_s_type_ = MapWrap[texparams.getWrapU()];

    // Sets the wrap parameter for texture coordinate S
    GLenum wrap_t_type_ = MapWrap[texparams.getWrapV()];

    // Sets the anisotropic filtering if the value provided is greater than 1 because 1 is the default value
    if (texparams.getMaxAnisotropy() > 1.0f)
    {
        glTexParameterf(mGLTarget, GL_TEXTURE_MAX_ANISOTROPY_EXT, texparams.getMaxAnisotropy());
    }

    glTexParameteri(mGLTarget, GL_TEXTURE_WRAP_S, wrap_s_type_);
    glTexParameteri(mGLTarget, GL_TEXTURE_WRAP_T, wrap_t_type_);
    glTexParameteri(mGLTarget, GL_TEXTURE_MIN_FILTER, min_filter_type_);
    glTexParameteri(mGLTarget, GL_TEXTURE_MAG_FILTER, mag_filter_type_);
    LOGV("GLImage: update texparams for %d", mId);
    checkGLError("GLImage::updateTexParams");
}

}
