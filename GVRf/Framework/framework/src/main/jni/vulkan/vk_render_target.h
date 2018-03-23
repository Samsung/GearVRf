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
#ifndef FRAMEWORK_VK_RENDER_TARGET_H
#define FRAMEWORK_VK_RENDER_TARGET_H
#include "../objects/components/render_target.h"
#include "vulkan_headers.h"

namespace gvr{

class Scene;
class Renderer;
class VkRenderTexture;
class VkRenderTarget: public  RenderTarget
{
public:
    explicit VkRenderTarget(RenderTexture* renderTexture, bool is_multiview);
    explicit VkRenderTarget(Scene* scene);
    explicit VkRenderTarget(RenderTexture* renderTexture, const RenderTarget* source);
    explicit  VkRenderTarget(){}
    virtual ~VkRenderTarget(){}
    virtual void    beginRendering(Renderer* renderer);

    VkRenderTexture* getTexture();
    VkCommandBuffer& getCommandBuffer();
};
}
#endif //FRAMEWORK_VK_RENDER_TARGET_H
