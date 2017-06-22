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

#ifndef GL_RENDER_TEXTURE_H_
#define GL_RENDER_TEXTURE_H_

#include "objects/textures/render_texture.h"
#include "gl/gl_render_buffer.h"
#include "gl/gl_frame_buffer.h"
#include "gl/gl_image.h"
#include "eglextension/msaa/msaa.h"
#include "util/gvr_gl.h"

namespace gvr {
class GLRenderImage;
class Renderer;
class GLRenderTexture : public RenderTexture
{
public:
    explicit GLRenderTexture(int width, int height, int sample_count, int layers, int depth_format);
    explicit GLRenderTexture(int width, int height, int sample_count,
            int jcolor_format, int jdepth_format, bool resolve_depth,
            const TextureParameters* texture_parameters);

    virtual ~GLRenderTexture();

    virtual int getId() { return mImage->getId(); }
    virtual int width() const { return mImage->getWidth(); }
    virtual int height() const { return mImage->getHeight(); }

    virtual unsigned int getFrameBufferId() const {
        return renderTexture_gl_frame_buffer_->id();
    }

    virtual void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, renderTexture_gl_frame_buffer_->id());
    }

    virtual void beginRendering(Renderer*);
    virtual void endRendering(Renderer*);
    virtual bool isReady();

    // Start to read back texture in the background. It can be optionally called before
    // readRenderResult() to read pixels asynchronously. This function returns immediately.
    virtual  void startReadBack();

    // Copy data in pixel buffer to client memory. This function is synchronous. When
    // it returns, the pixels have been copied to PBO and then to the client memory.
    virtual bool readRenderResult(unsigned int *readback_buffer, long capacity);
    void bindFrameBufferToLayer(int layerIndex);
    bool bindTexture(int gl_location, int texIndex);

private:
    GLRenderTexture(const GLRenderTexture&);
    GLRenderTexture(GLRenderTexture&&);
    GLRenderTexture& operator=(const GLRenderTexture&);
    GLRenderTexture& operator=(GLRenderTexture&&);

    void generateRenderTextureNoMultiSampling(int jdepth_format,GLenum depth_format, int width, int height);
    void generateRenderTextureLayer(GLenum depth_format, int width, int height);
    void generateRenderTextureEXT(int sample_count,int jdepth_format,GLenum depth_format, int width, int height);
    void generateRenderTexture(int sample_count, int jdepth_format, GLenum depth_format, int width,
            int height, int jcolor_format);
    void invalidateFrameBuffer(GLenum target, bool is_fbo, const bool color_buffer, const bool depth_buffer);
    void initialize();

private:
    int layer_index_;
    GLenum depth_format_;
    GLRenderBuffer* renderTexture_gl_render_buffer_ = nullptr;// This is actually depth buffer.
    GLFrameBuffer* renderTexture_gl_frame_buffer_ = nullptr;
    GLFrameBuffer* renderTexture_gl_resolve_buffer_ = nullptr;
    GLRenderBuffer* renderTexture_gl_color_buffer_ = nullptr;// This is only for multisampling case
                                     // when resolveDepth is on.
    GLuint renderTexture_gl_pbo_ = 0;
};

}
#endif
