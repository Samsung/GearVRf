//
// Created by roshan on 9/22/17.
//

#ifndef FRAMEWORK_VK_RENDER_TARGET_H
#define FRAMEWORK_VK_RENDER_TARGET_H
#include "../objects/components/render_target.h"
#include "vulkan.h"

namespace gvr{

class Scene;
class Renderer;
class VkRenderTexture;
class VkRenderTarget: public  RenderTarget
{
public:
    void createFenceObject(VkDevice device);
    void createCmdBuffer(VkDevice device, VkCommandPool commandPool);
    void initVkData();
    explicit VkRenderTarget(RenderTexture* renderTexture, bool is_multiview);
    explicit VkRenderTarget(Scene* scene);
    explicit VkRenderTarget(RenderTexture* renderTexture, const RenderTarget* source);
    explicit  VkRenderTarget(){}
    ~VkRenderTarget(){}
    virtual void    beginRendering(Renderer* renderer);
    virtual void    endRendering(Renderer*);
    VkCommandBuffer& getCommandBuffer(){
        return mCmdBuffer;
    }
    VkFence& getFenceObject(){
        return mWaitFence;
    }

private:
    VkFence mWaitFence;
    VkCommandBuffer mCmdBuffer;
};
}
#endif //FRAMEWORK_VK_RENDER_TARGET_H
