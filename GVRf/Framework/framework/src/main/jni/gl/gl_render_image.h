
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
#define GL_RENDER_IMAGE_H_
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
    explicit GLRenderImage(int width, int height, int layers = 1);
    explicit GLRenderImage(int width, int height, int layers, GLuint texId, bool marktexParamsDirty);
    explicit GLRenderImage(int width, int height, int color_format, const TextureParameters* texture_parameters);
    explicit GLRenderImage(int width, int height, int color_format, int layers, const TextureParameters* texture_parameters);
    virtual ~GLRenderImage() {}
    virtual int getId() { return mId; }

    virtual bool isReady()
    {
        return updateGPU();
    }

    virtual void texParamsChanged(const TextureParameters& texparams)
    {
        if (mTexParams != texparams)
        {
            mTexParams = texparams;
            mTexParamsDirty = true;
        }
    }
    virtual void updateTexParams();
    void setupReadback(GLuint buffer, int);

protected:
    virtual GLuint createTexture();

private:
    GLRenderImage(const GLRenderImage&) = delete;
    GLRenderImage(GLRenderImage&&) = delete;
    GLRenderImage& operator=(const GLRenderImage&) = delete;
    GLRenderImage& operator=(GLRenderImage&&) = delete;
};

}
#endif
