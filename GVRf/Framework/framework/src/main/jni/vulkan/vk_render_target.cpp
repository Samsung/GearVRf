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
}
