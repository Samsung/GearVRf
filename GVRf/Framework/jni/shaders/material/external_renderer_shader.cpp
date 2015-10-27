/***************************************************************************
 * installable external renderer
 ***************************************************************************/

#include "external_renderer_shader.h"

#include "objects/material.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include "objects/textures/texture.h"
#include "objects/textures/external_renderer_texture.h"
#include "util/gvr_gl.h"
#include "util/gvr_log.h"

static GVRF_ExternalRenderer externalRenderer = NULL;

void GVRF_installExternalRenderer(GVRF_ExternalRenderer fct) {
    externalRenderer = fct;
}

namespace gvr {

void ExternalRendererShader::render(const glm::mat4& mvp_matrix, RenderData* render_data) {
    if (externalRenderer == NULL) {
        LOGE("External renderer not installed");
        return;
    }

    Material* material = render_data->pass(0)->material();
    if (material == NULL) {
        LOGE("No material");
        return;
    }

    Texture *texture = material->getTexture("main_texture");
    if (texture->getTarget() != ExternalRendererTexture::TARGET) {
        LOGE("External renderer only takes external renderer textures");
        return;
    }

    Mesh* mesh = render_data->mesh();
    if (mesh == NULL) {
        LOGE("No mesh!?");
        return;
    }

    {
        const glm::vec3& min_corner = mesh->getBoundingVolume().min_corner();
        scratchBuffer[0] = min_corner[0];
        scratchBuffer[1] = min_corner[1];
        scratchBuffer[2] = min_corner[2];
        const glm::vec3& max_corner = mesh->getBoundingVolume().max_corner();
        scratchBuffer[3] = max_corner[0];
        scratchBuffer[4] = max_corner[1];
        scratchBuffer[5] = max_corner[2];
    }

    externalRenderer(reinterpret_cast<ExternalRendererTexture*>(texture)->getData(),
                     scratchBuffer, 6,
                     glm::value_ptr(mvp_matrix), 16,
                     glm::value_ptr(*mesh->tex_coords().data()), mesh->tex_coords().size() * 2,
                     material->getFloat("opacity"));

    checkGlError("ExternalRendererShader::render");
}

}

