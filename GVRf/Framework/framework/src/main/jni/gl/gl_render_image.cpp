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
 * A frame buffer object.
 ***************************************************************************/

#include "gl_render_image.h"
#include "gl_imagetex.h"
#include "gl_headers.h"

namespace gvr {


GLRenderImage::GLRenderImage(int width, int height, int layers)
        : GLImage((layers > 1) ? GL_TEXTURE_2D_ARRAY : GL_TEXTURE_2D)
{
    mWidth = width;
    mHeight = height;
    mDepth = layers;
    mType = (layers > 1) ? Image::ImageType::ARRAY : Image::ImageType::BITMAP;
    mState = HAS_DATA;
}

GLRenderImage::GLRenderImage(int width, int height, int color_format, const TextureParameters* texparams)
    : GLImage(GL_TEXTURE_2D)
{
    GLenum target = GLImage::getTarget();
    mWidth = width;
    mHeight = height;
    mDepth = 1;
    mType = Image::ImageType::BITMAP;
    mState = HAS_DATA;

    if (texparams)
    {
        updateTexParams(mTexParams);
    }
    updateGPU();

    switch (color_format)
    {
        case ColorFormat::COLOR_565:
        glTexImage2D(target, 0, GL_RGB, width, height, 0, GL_RGB,
                GL_UNSIGNED_SHORT_5_6_5, 0);
        break;

        case ColorFormat::COLOR_5551:
        glTexImage2D(target, 0, GL_RGB5_A1, width, height, 0, GL_RGBA,
                GL_UNSIGNED_SHORT_5_5_5_1, 0);
        break;

        case ColorFormat::COLOR_4444:
        glTexImage2D(target, 0, GL_RGBA, width, height, 0, GL_RGBA,
                GL_UNSIGNED_SHORT_4_4_4_4, 0);
        break;

        case ColorFormat::COLOR_8888:
        glTexImage2D(target, 0, GL_RGBA8, width, height, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, 0);
        break;

        case ColorFormat::COLOR_8888_sRGB:
        glTexImage2D(target, 0, GL_SRGB8_ALPHA8, width, height, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, 0);
        break;

        default:
        break;
    }
}

GLuint GLRenderImage::createTexture()
{
    GLuint texid = GLImage::createTexture();
    glBindTexture(mGLTarget, texid);
    if (mGLTarget == GL_TEXTURE_2D_ARRAY)
    {
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA8,
                     getWidth(), getHeight(), getDepth(),
                     0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    }
    else
    {
        glTexImage2D(mGLTarget, 0, GL_RGBA8,
                     getWidth(), getHeight(),
                     0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    }
    glBindTexture(mGLTarget, 0);
    checkGLError("GLRenderImage::createTexture");
    return texid;
}


void GLRenderImage::setupReadback(GLuint buffer)
{
    glViewport(0, 0, getWidth(), getHeight());
    glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, getTarget(), getId(), 0);
    glReadBuffer(GL_COLOR_ATTACHMENT0);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, buffer);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);
}


}
