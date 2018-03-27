//
// Created by roshan on 9/22/17.
//

#ifndef FRAMEWORK_GL_RENDER_TARGET_H
#define FRAMEWORK_GL_RENDER_TARGET_H

#include "../objects/components/render_target.h"

namespace gvr{
class ShaderManager;
class Scene;
class RenderTexture;
class GLTexture;
class Renderer;
class GLRenderTarget : public RenderTarget
{
public:
    explicit GLRenderTarget(RenderTexture* renderTexture, bool is_multiview): RenderTarget(renderTexture, is_multiview){

    }
    explicit GLRenderTarget(Scene* scene): RenderTarget(scene){
    }
    explicit GLRenderTarget(RenderTexture* renderTexture, const RenderTarget* source): RenderTarget(renderTexture, source){}
    GLRenderTarget(){}
    virtual void beginRendering(Renderer *renderer);
};
}
#endif //FRAMEWORK_GL_RENDER_TARGET_H
