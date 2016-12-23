
#ifndef EXTERNAL_RENDERER_TEXTURE_H_
#define EXTERNAL_RENDERER_TEXTURE_H_

#include "gl/gl_headers.h"

#include "objects/textures/base_texture.h"
#include "objects/components/texture_capturer.h"

// this is the texture to be used with an external renderer
// the data field can be used to pass data between the gvrf application
// and the external renderer

namespace gvr {

class GLExternalRendererTexture: public GLTexture
{
public:
    GLExternalRendererTexture() : GLTexture(0), mData(0)
    {
        target_ = GL_TEXTURE_EXTERNAL_OES + 1000;
    }

    virtual void setData(long data)
    {
        mData = data;
    }

    virtual long getData() const
    {
        return mData;
    }

private:
    GLExternalRendererTexture(const GLExternalRendererTexture& render_texture);
    GLExternalRendererTexture(GLExternalRendererTexture&& render_texture);
    GLExternalRendererTexture& operator=(const GLExternalRendererTexture& render_texture);
    GLExternalRendererTexture& operator=(GLExternalRendererTexture&& render_texture);

private:
    long mData;
};

}
#endif
