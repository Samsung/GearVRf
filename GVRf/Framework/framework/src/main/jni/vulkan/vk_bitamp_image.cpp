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

/***************************************************************************
 * Texture from a (Java-loaded) byte stream containing a compressed texture
 ***************************************************************************/

#include <android/bitmap.h>
#include <engine/renderer/renderer.h>
#include <engine/renderer/vulkan_renderer.h>
#include "vulkan/vk_bitamp_image.h"


std::map<int,VkFormat> compressed_formats = {
        { 0x93B0, VK_FORMAT_ASTC_4x4_UNORM_BLOCK},
        { 0x93B1 , VK_FORMAT_ASTC_5x4_UNORM_BLOCK },
        { 0x93B2 , VK_FORMAT_ASTC_5x5_UNORM_BLOCK},
        {0x93B3 , VK_FORMAT_ASTC_6x5_UNORM_BLOCK },
        {0x93B4 , VK_FORMAT_ASTC_6x6_UNORM_BLOCK},
        {0x93B5 , VK_FORMAT_ASTC_8x5_UNORM_BLOCK},
        {0x93B6 , VK_FORMAT_ASTC_8x6_UNORM_BLOCK},
        {0x93B7 , VK_FORMAT_ASTC_8x8_UNORM_BLOCK},
        {0x93B8,VK_FORMAT_ASTC_10x5_UNORM_BLOCK},
        {0x93B9, VK_FORMAT_ASTC_10x6_UNORM_BLOCK},
        {0x93BA, VK_FORMAT_ASTC_10x8_UNORM_BLOCK},
        {0x93BB, VK_FORMAT_ASTC_10x10_UNORM_BLOCK},
        {0x93BC, VK_FORMAT_ASTC_12x10_UNORM_BLOCK},
        {0x93BD, VK_FORMAT_ASTC_12x12_UNORM_BLOCK},
        {GL_COMPRESSED_SIGNED_RG11_EAC, VK_FORMAT_EAC_R11G11_SNORM_BLOCK},
        {GL_COMPRESSED_RG11_EAC, VK_FORMAT_EAC_R11G11_UNORM_BLOCK},
        {GL_COMPRESSED_SIGNED_R11_EAC, VK_FORMAT_EAC_R11_SNORM_BLOCK},
        {GL_COMPRESSED_R11_EAC, VK_FORMAT_EAC_R11_UNORM_BLOCK},
        {GL_COMPRESSED_RGB8_ETC2, VK_FORMAT_ETC2_R8G8B8_UNORM_BLOCK},
        {GL_COMPRESSED_RGBA8_ETC2_EAC, VK_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK},
     //   {GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 }
};

namespace gvr {
    size_t getSize(VkFormat format, int width, int height){

        switch(format){
            case VK_FORMAT_R8G8B8A8_UNORM:
                return width * height * 4;

            case VK_FORMAT_R5G6B5_UNORM_PACK16  :
            case VK_FORMAT_R4G4B4A4_UNORM_PACK16:
                return width * height * 2;

            case VK_FORMAT_R8_UNORM:
                return width * height;

            default:
                LOGE("Image format not supported");
                return 0;
        }
    }



    int VkBitmapImage::updateFromBitmap(JNIEnv *env, VkImageViewType target, jobject bitmap)
    {
        AndroidBitmapInfo info;
        void *pixels;
        int ret;
        int imageFormat = 0;
        std::vector<void*>texData;
        size_t tex_size = 0;
        std::vector<VkBufferImageCopy> bufferCopyRegions;
        std::vector<jobject>bitmaps;
        std::vector<AndroidBitmapInfo> bitmapInfos;

        if (bitmap == NULL)
        {
            LOGE("BitmapImage::updateFromBitmap bitmap is NULL");
        }
        else if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0)
        {
            LOGE("BitmapImage::updateFromBitmap AndroidBitmap_getInfo() failed! error = %d", ret);
        }
        else if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
        {
            LOGE("BitmapImage::updateFromBitmap AndroidBitmap_lockPixels() failed! error = %d", ret);
            return 0;
        }
        else
        {
            VkFormat internalFormat = VK_FORMAT_R8G8B8A8_UNORM;
            switch (info.format)
            {
                case ANDROID_BITMAP_FORMAT_RGB_565:
                    internalFormat = VK_FORMAT_R5G6B5_UNORM_PACK16;
                     break;

                case ANDROID_BITMAP_FORMAT_RGBA_4444:
                    internalFormat = VK_FORMAT_R4G4B4A4_UNORM_PACK16;
                    break;

                case ANDROID_BITMAP_FORMAT_A_8:
                    internalFormat = VK_FORMAT_R8_UNORM;
                    break;
            }
            //updateVkImage(target,internalFormat,info.width,info.height,pixels);
              VkBufferImageCopy bufferCopyRegion = {};
              bufferCopyRegion.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
              bufferCopyRegion.imageSubresource.mipLevel = 0;
              bufferCopyRegion.imageSubresource.baseArrayLayer = 0;
              bufferCopyRegion.imageSubresource.layerCount = 1;
              bufferCopyRegion.imageExtent.width = static_cast<uint32_t>(info.width);
              bufferCopyRegion.imageExtent.height = static_cast<uint32_t>(info.height);
              bufferCopyRegion.imageExtent.depth = 1;
              bufferCopyRegion.bufferOffset = 0;
              bufferCopyRegions.push_back(bufferCopyRegion);
              tex_size = info.height * info.stride;
              texData.push_back(pixels);
              bitmapInfos.push_back(info);

              updateVkImage(tex_size,texData,bitmapInfos,bufferCopyRegions,target,internalFormat);
              AndroidBitmap_unlockPixels(env, bitmap);
              return internalFormat;
          }
          return imageFormat;
      }

#if 1
    int VkBitmapImage::updateVkImage(size_t texSize, std::vector<void*>& pixels,std::vector<AndroidBitmapInfo>& bitmapInfos, std::vector<VkBufferImageCopy>& bufferCopyRegions, VkImageViewType target, VkFormat internalFormat){

        VkResult err;
        bool pass;
        VulkanRenderer* vk_renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());
        VkDevice device = vk_renderer->getDevice();
        VkBuffer texBuffer;
        VkDeviceMemory  texMemory;
        VkBufferCreateInfo bufferCreateInfo = {};
        bufferCreateInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
        bufferCreateInfo.pNext = nullptr;
        bufferCreateInfo.size = texSize ;
        bufferCreateInfo.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
        bufferCreateInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
        bufferCreateInfo.flags = 0;

        VkMemoryAllocateInfo memoryAllocateInfo = {};
        memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
        memoryAllocateInfo.pNext = NULL;
        memoryAllocateInfo.allocationSize = 0;
        memoryAllocateInfo.memoryTypeIndex = 0;

        err = vkCreateBuffer(device, gvr::BufferCreateInfo(texSize , VK_BUFFER_USAGE_TRANSFER_DST_BIT), nullptr, &texBuffer);
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

        err = vkAllocateMemory(device, gvr::MemoryAllocateInfo(mem_reqs.size, memoryAllocateInfo.memoryTypeIndex),
                               NULL, &texMemory);


        unsigned char *texData;
        err = vkMapMemory(device, texMemory, 0,
                          memoryAllocateInfo.allocationSize, 0, (void **)&texData);
        assert(!err);
        int i = 0;
        for(auto &buffer_copy_region: bufferCopyRegions) {
            memcpy(texData + buffer_copy_region.bufferOffset, pixels[i], bitmapInfos[i].height * bitmapInfos[i].stride);
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
                                                         VK_IMAGE_USAGE_SAMPLED_BIT,VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT,
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
        imageMemoryBarrier.subresourceRange.layerCount = 1;
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
                                                                 internalFormat, 1, 1,
                                                                 VK_IMAGE_ASPECT_COLOR_BIT), NULL,
                                &imageView);
        assert(!err);
        return internalFormat;
    }

    int VkBitmapImage::updateVkImage(VkImageViewType target, VkFormat internalFormat, int width, int height, void* pixels){
          VkResult err;
          bool pass;
          VulkanRenderer* vk_renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());
          VkDevice device = vk_renderer->getDevice();
          VkBuffer texBuffer;
          VkDeviceMemory  texMemory;
          size_t texSize = getSize(internalFormat,width,height);
          VkBufferCreateInfo bufferCreateInfo = {};
          bufferCreateInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
          bufferCreateInfo.pNext = nullptr;
          bufferCreateInfo.size = texSize ;//sizeof(vb);//
          bufferCreateInfo.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
          bufferCreateInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
          bufferCreateInfo.flags = 0;

          VkMemoryAllocateInfo memoryAllocateInfo = {};
          memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
          memoryAllocateInfo.pNext = NULL;
          memoryAllocateInfo.allocationSize = 0;
          memoryAllocateInfo.memoryTypeIndex = 0;


          //err = vkCreateBuffer(m_device, &bufferCreateInfo, nullptr, &m_vertices.buf);
          err = vkCreateBuffer(device, gvr::BufferCreateInfo(texSize , VK_BUFFER_USAGE_TRANSFER_DST_BIT), nullptr, &texBuffer);
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

          err = vkAllocateMemory(device, gvr::MemoryAllocateInfo(mem_reqs.size, memoryAllocateInfo.memoryTypeIndex),
                                 NULL, &texMemory);


          void *texData;
          err = vkMapMemory(device, texMemory, 0,
                            memoryAllocateInfo.allocationSize, 0, &texData);
          assert(!err);

          memcpy(texData, pixels, texSize);
          vkUnmapMemory(device, texMemory);

          // Bind our buffer to the memory
          err = vkBindBufferMemory(device, texBuffer, texMemory, 0);
          assert(!err);


          std::vector<VkBufferImageCopy> bufferCopyRegions;
          uint32_t offset = 0;

          for (uint32_t i = 0; i < 1 ; i++)
          {
              VkBufferImageCopy bufferCopyRegion = {};
              bufferCopyRegion.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
              bufferCopyRegion.imageSubresource.mipLevel = i;
              bufferCopyRegion.imageSubresource.baseArrayLayer = 0;
              bufferCopyRegion.imageSubresource.layerCount = 1;
              bufferCopyRegion.imageExtent.width = static_cast<uint32_t>(width);
              bufferCopyRegion.imageExtent.height = static_cast<uint32_t>(height);
              bufferCopyRegion.imageExtent.depth = 1;
              bufferCopyRegion.bufferOffset = offset;

              bufferCopyRegions.push_back(bufferCopyRegion);

              offset += static_cast<uint32_t>(texSize);
          }

          err = vkCreateImage(device, gvr::ImageCreateInfo(VK_IMAGE_TYPE_2D,
                                                           internalFormat,
                                                           width,
                                                           height, 1, 1, 1,
                                                           VK_IMAGE_TILING_LINEAR,
                                                           VK_IMAGE_USAGE_SAMPLED_BIT,0,
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
        imageMemoryBarrier.subresourceRange.layerCount = 1;
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
                                                                 internalFormat, 1, 1,
                                                                 VK_IMAGE_ASPECT_COLOR_BIT), NULL,
                                &imageView);
        assert(!err);

        return internalFormat;
    }
#endif
    void VkBitmapImage::update(int texid)
    {
        if (mJava == NULL)
        {
            return;
        }
        if (mBitmap != NULL)
        {
            updateFromBitmap(texid);
        }
        else if (mData != NULL)
        {
            updateFromMemory(texid);
        }
        else
        {
            return;
        }
        clearData(getCurrentEnv(mJava));
        mPendingUpdate = false;
    }

    void VkBitmapImage::updateFromMemory(int texid)
    {
        JNIEnv *env = getCurrentEnv(mJava);
        jbyteArray array = static_cast<jbyteArray>(env->NewLocalRef(mData));
        if (array == NULL)
        {
            LOGE("BitmapImage::updateFromMemory array is null");
            return;
        }

        jbyte* pixels = env->GetByteArrayElements(array, 0);
        int t = getTarget();

        if (mIsCompressed)
        {
            if (mLevels > 1)
            {
           //     loadCompressedMipMaps(pixels, mFormat);
            }
            else
            {
                VkImageViewType target = static_cast<VkImageViewType>(getTarget());
                //updateVkImage(target,compressed_formats[mFormat],mWidth,mHeight,pixels + getDataOffset(0));
            }
        }
        else
        {
            glTexImage2D(t, 0, GL_LUMINANCE, mWidth, mHeight, 0, GL_LUMINANCE,
                         GL_UNSIGNED_BYTE, pixels);
            glGenerateMipmap(t);
        }
        env->ReleaseByteArrayElements(array, pixels, 0);
        env->DeleteLocalRef(array);
        clearData(env);
    }

    void VkBitmapImage::updateFromBitmap(int texid)
    {
        JNIEnv *env = getCurrentEnv(mJava);
        jobject bitmap = env->NewLocalRef(mBitmap);
        if (bitmap == NULL)
        {
            LOGE("BitmapImage::updateFromBitmap bitmap is null");
            return;
        }
        VkImageViewType t = static_cast<VkImageViewType>(getTarget());

        mFormat = updateFromBitmap(env, t, bitmap);

        env->DeleteLocalRef(bitmap);
    }

    void VkBitmapImage::loadCompressedMipMaps(jbyte *data, int format)
    {
        for (int level = 0; level < mLevels; ++level)
        {
            int levelOffset = getDataOffset(level);
            int levelSize = getDataOffset(level + 1) - levelOffset;
            int width = mWidth >> level;
            int height = mHeight >> level;
            if (width < 1) width = 1;
            if (height < 1) height = 1;
            glCompressedTexImage2D(getTarget(), level, format, width, height, levelOffset, levelSize,
                                   data);
        }
    }
}
