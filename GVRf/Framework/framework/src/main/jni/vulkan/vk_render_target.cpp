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
 void VkRenderTarget::beginRendering(Renderer* renderer){
     mRenderTexture->bind();
     RenderTarget::beginRendering(renderer);
     VkViewport viewport = {};
     viewport.height = (float) mRenderTexture->height() ;
     viewport.width = (float) mRenderTexture->width();
     viewport.minDepth = (float) 0.0f;
     viewport.maxDepth = (float) 1.0f;

     VkRect2D scissor = {};
     scissor.extent.width = mRenderTexture->width();
     scissor.extent.height = mRenderTexture->height();
     scissor.offset.x = 0;
     scissor.offset.y = 0;

     vkCmdSetScissor(mCmdBuffer,0,1, &scissor);
     vkCmdSetViewport(mCmdBuffer,0,1,&viewport);
     VkRenderPassBeginInfo rp_begin =  (static_cast<VkRenderTexture*>(mRenderTexture))->getRenderPassBeginInfo();
     vkCmdBeginRenderPass(mCmdBuffer, &rp_begin, VK_SUBPASS_CONTENTS_INLINE);
 }
VkRenderTarget::VkRenderTarget(RenderTexture* renderTexture, bool is_multiview): RenderTarget(renderTexture, is_multiview){
    initVkData();
}
void VkRenderTarget::endRendering(Renderer *) {
    vkCmdEndRenderPass(mCmdBuffer);
}

void VkRenderTarget::createCmdBuffer(VkDevice device, VkCommandPool commandPool){
    VkResult ret = VK_SUCCESS;
    ret = vkAllocateCommandBuffers(device, gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, commandPool),
                                   &mCmdBuffer
    );
    GVR_VK_CHECK(!ret);
}
void VkRenderTarget::initVkData() {
    VulkanRenderer* renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());
    VkDevice device = renderer->getDevice();
    VkCommandPool commandPool = renderer->getCore()->getCommandPool();
    createCmdBuffer(device,commandPool);
    static_cast<VkRenderTexture*>(mRenderTexture)->createFenceObject(device);
}
VkRenderTarget::VkRenderTarget(Scene* scene): RenderTarget(scene){
    initVkData();
}
VkRenderTarget::VkRenderTarget(RenderTexture* renderTexture, const RenderTarget* source): RenderTarget(renderTexture, source){
    initVkData();
}
}
