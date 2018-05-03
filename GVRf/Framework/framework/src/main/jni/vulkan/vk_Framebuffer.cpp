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

#include "vulkan/vk_framebuffer.h"
#include "vulkanCore.h"

namespace gvr {

    VKFramebuffer::~VKFramebuffer() {

        if(mAttachments[COLOR_IMAGE]!= 0)
            delete mAttachments[COLOR_IMAGE];

        if(mAttachments[DEPTH_IMAGE]!= 0)
            delete mAttachments[DEPTH_IMAGE];

        if(mAttachments[MULTISAMPLED_IMAGE]!= 0)
            delete mAttachments[MULTISAMPLED_IMAGE];

        cleanup();

    }

    void VKFramebuffer::cleanup() {

        VulkanCore * instance = VulkanCore::getInstance();
        VkDevice device = instance->getDevice();

        if(mFramebuffer != 0) {
            vkDestroyFramebuffer(device, mFramebuffer, nullptr);
            mFramebuffer = 0;
        }

        if(mRenderpass != 0) {
            vkDestroyRenderPass(device, mRenderpass, nullptr);
            mRenderpass = 0;
        }
    }
}