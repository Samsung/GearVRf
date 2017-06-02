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
GLRenderImage::GLRenderImage(int width, int height)
        : GLImage(GL_TEXTURE_2D)
{
    mWidth = width;
    mHeight = height;
    mType = Image::ImageType::BITMAP;
    updateGPU();
    glBindTexture(mGLTarget, id());
    glTexImage2D(mGLTarget, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
    glBindTexture(mGLTarget, 0);
}


GLRenderImage::GLRenderImage(int width, int height, GLenum gltarget)
    : GLImage(gltarget)
{
    mWidth = width;
    mHeight = height;
    mType = Image::ImageType::BITMAP;
}

GLRenderImage::GLRenderImage(int width, int height,
    int jcolor_format, int jdepth_format,
    const TextureParameters* texparams)
    : GLImage(GL_TEXTURE_2D)
{
    GLenum target = GLImage::getTarget();
    mWidth = width;
    mHeight = height;
    mType = Image::ImageType::BITMAP;
    if (texparams)
    {
        updateTexParams(mTexParams);
    }
    updateGPU();

    switch (jcolor_format) {
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

void GLRenderImage::setupReadback(GLuint buffer)
{
    glViewport(0, 0, getWidth(), getHeight());
    glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, getTarget(), getId(), 0);
    glReadBuffer(GL_COLOR_ATTACHMENT0);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, buffer);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);
}

/*
 * Create a GLRenderImageArray with a specified number of layers.
 * This is a GL layered textured. Its creation is postponed
 * until bindFrameBuffer is called on the GL thread.
 * The GLRenderImageArray constructor may be called from any thread.
 */
GLRenderImageArray::GLRenderImageArray(int width, int height, int numLayers)
        : GLRenderImage(width, height, GL_TEXTURE_2D_ARRAY),
          mNumLayers(numLayers)
{
}

GLuint  GLRenderImageArray::createTexture()
{
    GLuint texId = GLRenderImage::createTexture();
    glBindTexture(GL_TEXTURE_2D_ARRAY, texId);
    //   glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGB8, width,height,depth,0,GL_RGB, GL_UNSIGNED_BYTE,NULL);
    //   glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_R16F, width,height,depth,0,GL_RED, GL_HALF_FLOAT,NULL);  // does not work for S6 edge
    //   glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGB10_A2, width,height,depth,0,GL_RGBA, GL_UNSIGNED_INT_2_10_10_10_REV,NULL);
    glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA8, getWidth(), getHeight(), mNumLayers, 0, GL_RGBA,
                 GL_UNSIGNED_BYTE, NULL);
    glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    return texId;
}

void GLRenderImageArray::beginRendering()
{
    const GLenum attachments[3] = { GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT  };

    glViewport(0, 0, getWidth(), getHeight());
    glScissor(0, 0, getWidth(), getHeight());
    glInvalidateFramebuffer(GL_FRAMEBUFFER, 3, attachments);
    glClearColor(0, 0, 0, 1);
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
}


bool GLRenderImageArray::bindTexture(int gl_location, int texIndex)
{
    if (gl_location >= 0)
    {
        glActiveTexture(GL_TEXTURE0 + texIndex);
        glBindTexture(getTarget(), getId());
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glUniform1i(gl_location, texIndex);
    }
}

/*
 * Bind the framebuffer to the specified layer of the texture array.
 * Create the framebuffer and layered texture if necessary.
 * This function must be called from the GL thread.
 */
bool GLRenderImageArray::bindFrameBuffer(int layerIndex)
{
    int texId = getId();
    glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                              texId, 0, layerIndex);
    checkGLError("RenderTextureArray::bindFrameBuffer");
    int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (fboStatus != GL_FRAMEBUFFER_COMPLETE)
    {
        LOGE("RenderTextureArray::bindFrameBuffer Could not bind framebuffer: %d", fboStatus);
        switch (fboStatus)
        {
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT :
            LOGE("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            break;

            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
            LOGE("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            break;

            case GL_FRAMEBUFFER_UNSUPPORTED:
            LOGE("GL_FRAMEBUFFER_UNSUPPORTED");
            break;
        }
        return false;
    }
    return true;
}


}
