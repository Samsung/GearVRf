
#ifndef EXTERNAL_RENDERER_TEXTURE_H_
#define EXTERNAL_RENDERER_TEXTURE_H_

#include "objects/textures/texture.h"

// this is the texture to be used with an external renderer
// the data field can be used to pass data between the gvrf application
// and the external renderer

namespace gvr {

class ExternalRendererTexture
{
public:
    ExternalRendererTexture() : mData(0)
    { }

    virtual void setData(long data)
    {
        mData = data;
    }

    virtual long getData() const
    {
        return mData;
    }

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
