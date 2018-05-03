/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "../engine/renderer/vulkan_renderer.h"
#include "../vulkan/vk_render_target.h"
#include "../vulkan/vk_render_to_texture.h"



namespace gvr{
VkCommandBuffer& VkRenderTarget::getCommandBuffer(){
    return static_cast<VkRenderTexture*>(mRenderTexture)->getCommandBuffer();
}
 void VkRenderTarget::beginRendering(Renderer* renderer){
     mRenderTexture->bind();
     RenderTarget::beginRendering(renderer);
     mRenderTexture->beginRendering(renderer);
 }

VkRenderTarget::VkRenderTarget(RenderTexture* renderTexture, bool is_multiview): RenderTarget(renderTexture, is_multiview){
    static_cast<VkRenderTexture*>(mRenderTexture)->initVkData();
}

VkRenderTarget::VkRenderTarget(Scene* scene): RenderTarget(scene){
    static_cast<VkRenderTexture*>(mRenderTexture)->initVkData();
}
VkRenderTarget::VkRenderTarget(RenderTexture* renderTexture, const RenderTarget* source): RenderTarget(renderTexture, source){
    static_cast<VkRenderTexture*>(mRenderTexture)->initVkData();
}


VkRenderTexture* VkRenderTarget :: getTexture() {
    VkFence fence =  static_cast<VkRenderTexture*>(mRenderTexture)->getFenceObject();
    VkResult err;

    VulkanCore * core = VulkanCore::getInstance();
    if(!core)
    {
        return NULL;
    }

    VkDevice device = core->getDevice();
    err = vkGetFenceStatus(device, fence);
    /* Commenting out the code of sending an older image to oculus, if the current one is not yet complete.
     * Reason for commenting : 1. Even though the FPS is 60 the visuals lag.
     *                         2. FPS is not affected with or without this logic
     */
/*
    bool found = false;
    VkResult status;

    if (err != VK_SUCCESS) {
        renderTarget1 = static_cast<VkRenderTarget*>(renderTarget->getNextRenderTarget());
        while (renderTarget1!= nullptr && renderTarget1 != renderTarget) {
            VkFence fence1 = static_cast<VkRenderTexture*>(renderTarget1->getTexture())->getFenceObject();
            status = vkGetFenceStatus(m_device, fence1);
            if (VK_SUCCESS == status) {
                found = true;
                break;
            }
            renderTarget1 = static_cast<VkRenderTarget*>(renderTarget1->getNextRenderTarget());
        }
         if (!found) {
             renderTarget1 = static_cast<VkRenderTarget*>(renderTarget->getNextRenderTarget());
             VkFence fence1 = static_cast<VkRenderTexture*>(renderTarget1->getTexture())->getFenceObject();
            err = vkWaitForFences(m_device, 1, &fence1 , VK_TRUE,
                              4294967295U);
         }
    }
*/

    err = vkWaitForFences(device, 1, &fence , VK_TRUE, 4294967295U);

    if (err != VK_SUCCESS) {
        return NULL;
    }

    return static_cast<VkRenderTexture*>(mRenderTexture);
}

}
