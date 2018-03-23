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
#include <cmath>
#include "vulkan/vk_imagebase.h"
#include "engine/renderer/vulkan_renderer.h"

namespace gvr {
int getComponentsNumber(VkFormat format){
    switch (format){
        case VK_FORMAT_R8G8B8A8_UNORM:
            return 4;
        case VK_FORMAT_D16_UNORM:
            return 2;
        case VK_FORMAT_D32_SFLOAT:
            return 4;
        case VK_FORMAT_D24_UNORM_S8_UINT:
            return 4;
        default:
            FAIL("format not found");
    }
    return 0;
}

    vkImageBase::~vkImageBase(){
        VulkanCore * instance = VulkanCore::getInstance();
        VkDevice device = instance->getDevice();
        vkDestroyImageView(device, imageView, nullptr);
        vkDestroyImage(device, image, nullptr);

        if(host_memory != 0)
            vkFreeMemory(device, host_memory, nullptr);

        if(hostBuffer != 0)
            vkDestroyBuffer(device, hostBuffer, nullptr);


        if(host_accessible_) {
            vkDestroyBuffer(device, *outBuffer, nullptr);
            vkFreeMemory(device, dev_memory, nullptr);
        }

      }

void vkImageBase::createImageView(bool host_accessible) {
    host_accessible_ = host_accessible;

    VkResult ret = VK_SUCCESS;
    VulkanRenderer *vk_renderer = static_cast<VulkanRenderer *>(Renderer::getInstance());
    VkDevice device = vk_renderer->getDevice();
    bool pass;
    VkMemoryRequirements mem_reqs;
    uint32_t memoryTypeIndex;

    ret = vkCreateImage(
            device,
            gvr::ImageCreateInfo(VK_IMAGE_TYPE_2D, format_, width_,
                                 height_, depth_, 1, mLayers,
                                 tiling_,
                                 usage_flags_, 0, getVKSampleBit(mSampleCount),
                                 imageLayout),
            nullptr, &image
    );
    GVR_VK_CHECK(!ret);

    ret = vkCreateBuffer(device,
                         gvr::BufferCreateInfo(width_ * height_ * getComponentsNumber(format_) * mLayers * sizeof(uint8_t),
                                               usage_flags_), nullptr,
                         &hostBuffer);
    GVR_VK_CHECK(!ret);

    // discover what memory requirements are for this image.
    vkGetImageMemoryRequirements(device, image, &mem_reqs);

    pass = vk_renderer->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                    &memoryTypeIndex);
    GVR_VK_CHECK(pass);
    size = mem_reqs.size;

    ret = vkAllocateMemory(device,
                           gvr::MemoryAllocateInfo(mem_reqs.size, memoryTypeIndex), nullptr,
                           &host_memory);
    GVR_VK_CHECK(!ret);

    // Bind memory to the image
    ret = vkBindImageMemory(device, image, host_memory, 0);
    GVR_VK_CHECK(!ret);

    ret = vkBindBufferMemory(device, hostBuffer, host_memory, 0);
    GVR_VK_CHECK(!ret);

    VkImageAspectFlagBits aspectFlag = VK_IMAGE_ASPECT_COLOR_BIT;
    if (usage_flags_ == VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
        aspectFlag = static_cast<VkImageAspectFlagBits>(VK_IMAGE_ASPECT_DEPTH_BIT | VK_IMAGE_ASPECT_STENCIL_BIT);
    ret = vkCreateImageView(
            device,
            gvr::ImageViewCreateInfo(image, imageType,
                                     format_, 1, mLayers,
                                     aspectFlag),
            nullptr, &imageView
    );
    GVR_VK_CHECK(!ret);

    if(host_accessible) {

        ret = vkCreateBuffer(device,
                             gvr::BufferCreateInfo(width_ * height_ *  getComponentsNumber(format_) * mLayers  * sizeof(uint8_t),
                                                   VK_BUFFER_USAGE_TRANSFER_DST_BIT), nullptr,
                             outBuffer.get());
        GVR_VK_CHECK(!ret);

        // Obtain the memory requirements for this buffer.
        vkGetBufferMemoryRequirements(device, *outBuffer, &mem_reqs);
        GVR_VK_CHECK(!ret);

        pass = vk_renderer->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                                                        &memoryTypeIndex);
        GVR_VK_CHECK(pass);

        ret = vkAllocateMemory(device,
                               gvr::MemoryAllocateInfo(mem_reqs.size, memoryTypeIndex), nullptr,
                               &dev_memory);
        GVR_VK_CHECK(!ret);

        ret = vkBindBufferMemory(device, *outBuffer, dev_memory, 0);
        GVR_VK_CHECK(!ret);
    }
}

void vkImageBase::updateMipVkImage(uint64_t texSize, std::vector<void *> &pixels,
                              std::vector<ImageInfo> &bitmapInfos,
                              std::vector<VkBufferImageCopy> &bufferCopyRegions,
                              VkImageViewType target, VkFormat internalFormat,
                              int mipLevels,
                              VkImageCreateFlags flags) {

    VkResult err;
    bool pass;
    VulkanRenderer *vk_renderer = static_cast<VulkanRenderer *>(Renderer::getInstance());
    VkDevice device = vk_renderer->getDevice();
    VkFormatProperties formatProperties;
    vkGetPhysicalDeviceFormatProperties(vk_renderer->getPhysicalDevice(), internalFormat,
                                        &formatProperties);
    assert(formatProperties.optimalTilingFeatures & VK_FORMAT_FEATURE_BLIT_SRC_BIT);
    assert(formatProperties.optimalTilingFeatures & VK_FORMAT_FEATURE_BLIT_DST_BIT);

    VkBuffer texBuffer;
    VkDeviceMemory texMemory;

    VkMemoryAllocateInfo memoryAllocateInfo = {};
    memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
    memoryAllocateInfo.pNext = NULL;
    memoryAllocateInfo.allocationSize = 0;
    memoryAllocateInfo.memoryTypeIndex = 0;

    err = vkCreateBuffer(device,
                         gvr::BufferCreateInfo(texSize,
                                               VK_BUFFER_USAGE_TRANSFER_SRC_BIT),
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
    size = mem_reqs.size;
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
                                                     bitmapInfos[0].height, 1, mipLevels, pixels.size(),
                                                     VK_IMAGE_TILING_OPTIMAL,
                                                     VK_IMAGE_USAGE_TRANSFER_DST_BIT |
                                                     VK_IMAGE_USAGE_TRANSFER_SRC_BIT |
                                                     VK_IMAGE_USAGE_SAMPLED_BIT,
                                                     flags,
                                                     getVKSampleBit(mSampleCount),
                                                     VK_IMAGE_LAYOUT_UNDEFINED), NULL,
                        &image);
    assert(!err);

    vkGetImageMemoryRequirements(device, image, &mem_reqs);

    memoryAllocateInfo.allocationSize = mem_reqs.size;

    pass = vk_renderer->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                    &memoryAllocateInfo.memoryTypeIndex);
    assert(pass);

    /* allocate memory */
    err = vkAllocateMemory(device, &memoryAllocateInfo, NULL, &host_memory);
    assert(!err);

    /* bind memory */
    err = vkBindImageMemory(device, image, host_memory, 0);
    assert(!err);

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
    imageMemoryBarrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    imageMemoryBarrier.subresourceRange.baseMipLevel = 0;
    imageMemoryBarrier.subresourceRange.levelCount = 1;
    imageMemoryBarrier.subresourceRange.baseArrayLayer = 0;
    imageMemoryBarrier.subresourceRange.layerCount = pixels.size();
    imageMemoryBarrier.srcAccessMask = 0;
    imageMemoryBarrier.dstAccessMask =
            VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_INPUT_ATTACHMENT_READ_BIT;

    // Optimal image will be used as destination for the copy, so we must transfer from our initial undefined image layout to the transfer destination layout
    setImageLayout(imageMemoryBarrier, textureCmdBuffer, image, VK_IMAGE_ASPECT_COLOR_BIT,
                   VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                   imageMemoryBarrier.subresourceRange);

    vkCmdCopyBufferToImage(
            textureCmdBuffer,
            texBuffer,
            image,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            static_cast<uint32_t>(bufferCopyRegions.size()),
            bufferCopyRegions.data());

    imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

    setImageLayout(imageMemoryBarrier, textureCmdBuffer, image, VK_IMAGE_ASPECT_COLOR_BIT,
                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
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

    vkFreeMemory(device, texMemory, nullptr);
    vkDestroyBuffer(device, texBuffer, nullptr);

    VkCommandBuffer blitCmd;
    vk_renderer->initCmdBuffer(VK_COMMAND_BUFFER_LEVEL_PRIMARY, blitCmd);

    vkResetCommandBuffer(blitCmd, 0);

    // Begin recording to the command buffer.
    vkBeginCommandBuffer(blitCmd, &setupCmdsBeginInfo);

    // Copy down mips from n-1 to n
    for(int j=0; j< bufferCopyRegions.size(); j++) {

        for (int32_t i = 1; i < mipLevels; i++)
        {

            VkImageBlit imageBlit{};

            // Source
            imageBlit.srcSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            imageBlit.srcSubresource.layerCount = 1;
            imageBlit.srcSubresource.mipLevel = i-1;
            imageBlit.srcSubresource.baseArrayLayer = j;
            imageBlit.srcOffsets[1].x = int32_t(bitmapInfos[j].width >> (i - 1)) == 0 ? 1 : int32_t(bitmapInfos[j].width >> (i - 1));
            imageBlit.srcOffsets[1].y = int32_t(bitmapInfos[j].height >> (i - 1)) == 0 ? 1 : int32_t(bitmapInfos[j].height >> (i - 1));

            imageBlit.srcOffsets[1].z = 1;

            // Destination
            imageBlit.dstSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            imageBlit.dstSubresource.layerCount = 1;
            imageBlit.dstSubresource.baseArrayLayer = j;
            imageBlit.dstSubresource.mipLevel = i;
            imageBlit.dstOffsets[1].x = int32_t(bitmapInfos[j].width >> i) == 0 ? 1 : int32_t(bitmapInfos[j].width >> i);
            imageBlit.dstOffsets[1].y = int32_t(bitmapInfos[j].height >> i) == 0 ? 1 : int32_t(bitmapInfos[j].height >> i);
            imageBlit.dstOffsets[1].z = 1;

            VkImageMemoryBarrier imageMemoryBarrier = {};
            imageMemoryBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
            imageMemoryBarrier.pNext = NULL;
            imageMemoryBarrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            imageMemoryBarrier.subresourceRange.baseMipLevel = i;
            imageMemoryBarrier.subresourceRange.levelCount = 1;
            imageMemoryBarrier.subresourceRange.baseArrayLayer = j;
            imageMemoryBarrier.subresourceRange.layerCount = 1;

            // change layout of current mip level to transfer dest
            setImageLayout(imageMemoryBarrier,
                           blitCmd,
                           image,
                           VK_IMAGE_ASPECT_COLOR_BIT,
                           VK_IMAGE_LAYOUT_UNDEFINED,
                           VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, imageMemoryBarrier.subresourceRange,
                           VK_PIPELINE_STAGE_TRANSFER_BIT,
                           VK_PIPELINE_STAGE_HOST_BIT);

            // Do blit operation from previous mip level
            vkCmdBlitImage(
                    blitCmd,
                    image,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    image,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    1,
                    &imageBlit,
                    VK_FILTER_LINEAR);

            // change layout of current mip level to source for next iteration
            setImageLayout(imageMemoryBarrier,
                           blitCmd,
                           image,
                           VK_IMAGE_ASPECT_COLOR_BIT,
                           VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                           VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, imageMemoryBarrier.subresourceRange,
                           VK_PIPELINE_STAGE_HOST_BIT,
                           VK_PIPELINE_STAGE_TRANSFER_BIT);
        }
    }
    // Change layout of all mip levels to shader read
    imageMemoryBarrier.subresourceRange.levelCount = mipLevels;
    setImageLayout(imageMemoryBarrier,
                   blitCmd,
                   image,
                   VK_IMAGE_ASPECT_COLOR_BIT,
                   VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                   imageLayout,
                   imageMemoryBarrier.subresourceRange);
    // We are finished recording operations.
    vkEndCommandBuffer(blitCmd);
    buffers[0] = blitCmd;

    submit_info.pCommandBuffers = &buffers[0];

    // Submit to our shared graphics queue.
    err = vkQueueSubmit(queue, 1, &submit_info, VK_NULL_HANDLE);
    assert(!err);

    // Wait for the queue to become idle.
    err = vkQueueWaitIdle(queue);
    assert(!err);
    err = vkCreateImageView(device, gvr::ImageViewCreateInfo(image,
                                                             target,
                                                             internalFormat, mipLevels,
                                                             pixels.size(),
                                                             VK_IMAGE_ASPECT_COLOR_BIT), NULL,
                            &imageView);
    assert(!err);
    
}

}