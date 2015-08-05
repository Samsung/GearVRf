/***************************************************************************
 * installable external renderer
 ***************************************************************************/

#ifndef EXTERNAL_RENDERER_SHADER_H_
#define EXTERNAL_RENDERER_SHADER_H_

#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/recyclable_object.h"

typedef void (*GVRF_ExternalRenderer)(long data, const float* vertices,
                                      int vcount, const float* projection, int pcount);
extern "C" void GVRF_installExternalRenderer(GVRF_ExternalRenderer renderer);

namespace gvr {
class RenderData;

class ExternalRendererShader : public RecyclableObject {
public:
    ExternalRendererShader() {}
    void render(const glm::mat4& mvp_matrix, RenderData* render_data);

private:
    ExternalRendererShader(
            const ExternalRendererShader& shader);
    ExternalRendererShader(
            ExternalRendererShader&& shader);
    ExternalRendererShader& operator=(
            const ExternalRendererShader& shader);
    ExternalRendererShader& operator=(
            ExternalRendererShader&& shader);
};

}

#endif
