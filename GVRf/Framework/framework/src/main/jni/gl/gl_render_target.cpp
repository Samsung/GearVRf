//
// Created by roshan on 9/22/17.
//


#include "gl_render_target.h"
#include "../objects/textures/render_texture.h"
#include "../engine/renderer/renderer.h"
#include "../util/gvr_log.h"

namespace gvr{

void GLRenderTarget::beginRendering(Renderer *renderer) {
    RenderTarget::beginRendering(renderer);
}
}