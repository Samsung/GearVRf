/***************************************************************************
 * installable external renderer
 ***************************************************************************/

#ifndef EXTERNAL_RENDERER_SHADER_H_
#define EXTERNAL_RENDERER_SHADER_H_

#include "shaderbase.h"

typedef void (*GVRF_ExternalRenderer)(long data,
                                      const float* bounding_volume, int vcount,
                                      const float* projection, int pcount,
                                      const float* texCoords, int tcount,
                                      float opacity);
extern "C" void GVRF_installExternalRenderer(GVRF_ExternalRenderer renderer);

namespace gvr {

class ExternalRendererShader : public ShaderBase {
public:
    ExternalRendererShader() {}
    virtual void render(RenderState* rstate, RenderData* render_data, Material* material);

private:
    ExternalRendererShader(
            const ExternalRendererShader& shader);
    ExternalRendererShader(
            ExternalRendererShader&& shader);
    ExternalRendererShader& operator=(
            const ExternalRendererShader& shader);
    ExternalRendererShader& operator=(
            ExternalRendererShader&& shader);

    float scratchBuffer[6];
};

}

#endif
