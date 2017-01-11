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
#include "vk_image.h"
#include "../engine/renderer/vulkan_renderer.h"
//#include "../vulkan/vulkanInfoWrapper.h"
//#include "vulkanCore.h"
#include "../util/gvr_log.h"
#include "assert.h"

namespace gvr {
    size_t getSize(VkFormat format, int width, int height) {

        switch (format) {
            case VK_FORMAT_R8G8B8A8_UNORM:
                return width * height * 4;

            case VK_FORMAT_R5G6B5_UNORM_PACK16  :
            case VK_FORMAT_R4G4B4A4_UNORM_PACK16:
                return width * height * 2;

            case VK_FORMAT_R8_UNORM:
                return width * height;
            case VK_FORMAT_R16G16_SFLOAT:
                return width * height * 2 * 4;
            default:
                LOGE("Image format not supported");
                return 0;
        }
    }

    int vkImage::updateVkImage(size_t texSize, std::vector<void *> &pixels,
                               std::vector<ImageInfo> &bitmapInfos,
                               std::vector<VkBufferImageCopy> &bufferCopyRegions,
                               VkImageViewType target, VkFormat internalFormat,
                               VkImageCreateFlags flags) {


        VkResult err;
        bool pass;
        VulkanRenderer *vk_renderer = static_cast<VulkanRenderer *>(Renderer::getInstance());
        VkDevice device = vk_renderer->getDevice();
        VkBuffer texBuffer;
        VkDeviceMemory texMemory;
        VkBufferCreateInfo bufferCreateInfo = {};
        bufferCreateInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
        bufferCreateInfo.pNext = nullptr;
        bufferCreateInfo.size = texSize;
        bufferCreateInfo.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
        bufferCreateInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
        bufferCreateInfo.flags = 0;

        VkMemoryAllocateInfo memoryAllocateInfo = {};
        memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
        memoryAllocateInfo.pNext = NULL;
        memoryAllocateInfo.allocationSize = 0;
        memoryAllocateInfo.memoryTypeIndex = 0;

        err = vkCreateBuffer(device,
                             gvr::BufferCreateInfo(texSize, VK_BUFFER_USAGE_TRANSFER_DST_BIT),
                             nullptr, &texBuffer);
        GVR_VK_CHECK(!err);

        // Obtain the requirements on memory for this buffer
        VkMemoryRequirements mem_reqs;
        vkGetBufferMemoryRequirements(device, texBuffer, &mem_reqs);
        assert(!err);

        memoryAllocateInfo.allocationSize = mem_reqs.size;

        pass = vk_renderer->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                                                        &memoryAllocateInfo.memoryTypeIndex);
        assert(pass);

        err = vkAllocateMemory(device, gvr::MemoryAllocateInfo(mem_reqs.size,
                                                               memoryAllocateInfo.memoryTypeIndex),
                               NULL, &texMemory);


        unsigned char *texData;
        err = vkMapMemory(device, texMemory, 0,
                          memoryAllocateInfo.allocationSize, 0, (void **) &texData);
        assert(!err);
        int i = 0;
        for (auto &buffer_copy_region: bufferCopyRegions) {
            memcpy(texData + buffer_copy_region.bufferOffset, pixels[i],
                   bitmapInfos[i].size);
            i++;
        }
        vkUnmapMemory(device, texMemory);

        // Bind our buffer to the memory
        err = vkBindBufferMemory(device, texBuffer, texMemory, 0);
        assert(!err);

        err = vkCreateImage(device, gvr::ImageCreateInfo(VK_IMAGE_TYPE_2D,
                                                         internalFormat,
                                                         bitmapInfos[0].width,
                                                         bitmapInfos[0].height, 1, 1, pixels.size(),
                                                         VK_IMAGE_TILING_LINEAR,
                                                         VK_IMAGE_USAGE_SAMPLED_BIT,
                                                         flags,
                                                         VK_SAMPLE_COUNT_1_BIT,
                                                         VK_IMAGE_LAYOUT_UNDEFINED), NULL,
                            &image);
        assert(!err);

        vkGetImageMemoryRequirements(device, image, &mem_reqs);

        memoryAllocateInfo.allocationSize = mem_reqs.size;

        pass = vk_renderer->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                                                        &memoryAllocateInfo.memoryTypeIndex);
        assert(pass);

        /* allocate memory */
        err = vkAllocateMemory(device, &memoryAllocateInfo, NULL, &dev_memory);
        assert(!err);

        /* bind memory */
        err = vkBindImageMemory(device, image, dev_memory, 0);
        assert(!err);

        imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

        // We use a shared command buffer for setup operations to change layout.
        // Reset the setup command buffer
        VkCommandBuffer textureCmdBuffer;
        vk_renderer->initCmdBuffer(VK_COMMAND_BUFFER_LEVEL_PRIMARY, textureCmdBuffer);

        vkResetCommandBuffer(textureCmdBuffer, 0);

        VkCommandBufferInheritanceInfo commandBufferInheritanceInfo = {};
        commandBufferInheritanceInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
        commandBufferInheritanceInfo.pNext = NULL;
        commandBufferInheritanceInfo.renderPass = VK_NULL_HANDLE;
        commandBufferInheritanceInfo.subpass = 0;
        commandBufferInheritanceInfo.framebuffer = VK_NULL_HANDLE;
        commandBufferInheritanceInfo.occlusionQueryEnable = VK_FALSE;
        commandBufferInheritanceInfo.queryFlags = 0;
        commandBufferInheritanceInfo.pipelineStatistics = 0;

        VkCommandBufferBeginInfo setupCmdsBeginInfo;
        setupCmdsBeginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        setupCmdsBeginInfo.pNext = NULL;
        setupCmdsBeginInfo.flags = 0;
        setupCmdsBeginInfo.pInheritanceInfo = &commandBufferInheritanceInfo;

        // Begin recording to the command buffer.
        vkBeginCommandBuffer(textureCmdBuffer, &setupCmdsBeginInfo);

        VkImageMemoryBarrier imageMemoryBarrier = {};
        imageMemoryBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
        imageMemoryBarrier.pNext = NULL;
        imageMemoryBarrier.oldLayout = VK_IMAGE_LAYOUT_UNDEFINED;
        imageMemoryBarrier.newLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
        imageMemoryBarrier.image = image;
        imageMemoryBarrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        imageMemoryBarrier.subresourceRange.baseMipLevel = 0;
        imageMemoryBarrier.subresourceRange.levelCount = 1;
        imageMemoryBarrier.subresourceRange.baseArrayLayer = 0;
        imageMemoryBarrier.subresourceRange.layerCount = pixels.size();
        imageMemoryBarrier.srcAccessMask = 0;
        imageMemoryBarrier.dstAccessMask =
                VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_INPUT_ATTACHMENT_READ_BIT;

        setImageLayout(imageMemoryBarrier,
                       textureCmdBuffer,
                       image,
                       VK_IMAGE_ASPECT_COLOR_BIT,
                       VK_IMAGE_LAYOUT_UNDEFINED,
                       VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                       imageMemoryBarrier.subresourceRange);

        vkCmdCopyBufferToImage(
                textureCmdBuffer,
                texBuffer,
                image,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                static_cast<uint32_t>(bufferCopyRegions.size()),
                bufferCopyRegions.data());

        setImageLayout(imageMemoryBarrier,
                       textureCmdBuffer,
                       image,
                       VK_IMAGE_ASPECT_COLOR_BIT,
                       VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                       VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                       imageMemoryBarrier.subresourceRange);


        // We are finished recording operations.
        vkEndCommandBuffer(textureCmdBuffer);

        VkCommandBuffer buffers[1];
        buffers[0] = textureCmdBuffer;

        VkSubmitInfo submit_info;
        submit_info.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        submit_info.pNext = NULL;
        submit_info.waitSemaphoreCount = 0;
        submit_info.pWaitSemaphores = NULL;
        submit_info.pWaitDstStageMask = NULL;
        submit_info.commandBufferCount = 1;
        submit_info.pCommandBuffers = &buffers[0];
        submit_info.signalSemaphoreCount = 0;
        submit_info.pSignalSemaphores = NULL;
        VkQueue queue = vk_renderer->getQueue();

        // Submit to our shared graphics queue.
        err = vkQueueSubmit(queue, 1, &submit_info, VK_NULL_HANDLE);
        assert(!err);

        // Wait for the queue to become idle.
        err = vkQueueWaitIdle(queue);
        assert(!err);

        err = vkCreateImageView(device, gvr::ImageViewCreateInfo(image,
                                                                 target,
                                                                 internalFormat, 1, pixels.size(),
                                                                 VK_IMAGE_ASPECT_COLOR_BIT), NULL,
                                &imageView);
        assert(!err);
        return internalFormat;

    }
}