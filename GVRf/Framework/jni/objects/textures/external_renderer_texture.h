
#ifndef EXTERNAL_RENDERER_TEXTURE_H_
#define EXTERNAL_RENDERER_TEXTURE_H_

#define __gl2_h_
#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>

#include "objects/textures/base_texture.h"

// this is the texture to be used with an external renderer
// the data field can be used to pass data between the gvrf application
// and the external renderer

namespace gvr {

class ExternalRendererTexture: public Texture {
public:
    ExternalRendererTexture() : Texture(0), mData(0) {
    }

    GLenum getTarget() const {
        return TARGET;
    }

    virtual void setData(long data) {
        mData = data;
    }

    virtual long getData() const {
        return mData;
    }

    static const GLenum TARGET = GL_TEXTURE_EXTERNAL_OES + 1000;

private:
    ExternalRendererTexture(const ExternalRendererTexture& render_texture);
    ExternalRendererTexture(ExternalRendererTexture&& render_texture);
    ExternalRendererTexture& operator=(const ExternalRendererTexture& render_texture);
    ExternalRendererTexture& operator=(ExternalRendererTexture&& render_texture);

private:
    long mData;
};

}
#endif
