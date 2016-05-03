/***************************************************************************
 * installable external renderer
 ***************************************************************************/

#ifndef EXTERNAL_RENDERER_SHADER_H_
#define EXTERNAL_RENDERER_SHADER_H_

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/hybrid_object.h"

typedef void (*GVRF_ExternalRenderer)(long data,
                                      const float* bounding_volume, int vcount,
                                      const float* projection, int pcount,
                                      const float* texCoords, int tcount,
                                      float opacity);
extern "C" void GVRF_installExternalRenderer(GVRF_ExternalRenderer renderer);

namespace gvr {
class RenderData;

class ExternalRendererShader : public HybridObject {
public:
    ExternalRendererShader() {}
    void render(const glm::mat4& mv_matrix, const glm::mat4& mv_it_matrix,
                const glm::mat4& mvp_matrix, RenderData* render_data);

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
