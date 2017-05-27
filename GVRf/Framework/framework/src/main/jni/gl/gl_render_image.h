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

#ifndef GL_RENDER_IMAGE_H_
#define GL_RENDER_IMAGE_H
#include "objects/textures/render_texture.h"
#include "gl/gl_render_buffer.h"
#include "gl/gl_frame_buffer.h"
#include "gl/gl_imagetex.h"
#include "eglextension/msaa/msaa.h"
#include "util/gvr_gl.h"

namespace gvr {

class GLRenderImage : public GLImage, public Image
{
public:
    explicit GLRenderImage(int width, int height, GLenum gltarget);
    explicit GLRenderImage(int width, int height);
    GLRenderImage(int width, int height,
            int jcolor_format, int jdepth_format,
            const TextureParameters* texture_parameters);

    virtual int getId() { return mId; }

    virtual bool isReady()
    {
        return updateGPU();
    }

    virtual void texParamsChanged(const TextureParameters& texparams)
    {
        mTexParams = texparams;
        mTexParamsDirty = true;
    }

    void setupReadback(GLuint buffer);

private:
    GLRenderImage(const GLRenderImage&);
    GLRenderImage(GLRenderImage&&);
    GLRenderImage& operator=(const GLRenderImage&);
    GLRenderImage& operator=(GLRenderImage&&);

    void generateRenderTextureNoMultiSampling(int jdepth_format,GLenum depth_format, int width, int height);
    void generateRenderTextureEXT(int sample_count,int jdepth_format,GLenum depth_format, int width, int height);
    void generateRenderTexture(int sample_count, int jdepth_format, GLenum depth_format, int width,
            int height, int jcolor_format);
    void invalidateFrameBuffer(GLenum target, bool is_fbo, const bool color_buffer, const bool depth_buffer);
    void initialize();

};


class GLRenderImageArray : public GLRenderImage
{
public:
    GLRenderImageArray(int width, int height, int numLayers);
    virtual void beginRendering();
    bool bindFrameBuffer(int layerIndex);
    bool bindTexture(int gl_location, int texIndex);
    void genFramebuffer(){
            glGenFramebuffers(1, &fboId_);
    }
    GLuint getFboId(){
        if(fboId_ == 0)
            genFramebuffer();
        return fboId_;
    }
protected:
    virtual GLuint  createTexture();
    int     mNumLayers;
    GLuint fboId_ = 0;
};
}
#endif
