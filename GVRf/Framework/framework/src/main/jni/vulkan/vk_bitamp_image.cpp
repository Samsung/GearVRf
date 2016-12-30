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
#include "vulkan/vulkan.h"
#include "vulkan/vulkanInfoWrapper.h"
namespace gvr {
    int getSize(VkFormat format,AndroidBitmapInfo& info){
        if(format == VK_FORMAT_R8G8B8A8_UNORM){
            return info.width * info.height * 4;
        }
        if(format == VK_FORMAT_R8_UNORM)
            return info.width * info.height;
    }

    VkImageViewType VkBitmapImage::MapFormat[]={VK_IMAGE_VIEW_TYPE_1D, VK_IMAGE_VIEW_TYPE_2D,VK_IMAGE_VIEW_TYPE_3D};

    int VkBitmapImage::updateFromBitmap(JNIEnv *env, VkImageType target, jobject bitmap)
    {
        AndroidBitmapInfo info;
        void *pixels;
        int ret;
        int imageFormat = 0;

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
            LOGE("vulkan: format %d ",info.format);
            VkFormat internalFormat = VK_FORMAT_R8G8B8A8_UNORM;
            switch (info.format)
            {
                case ANDROID_BITMAP_FORMAT_RGB_565:
                    LOGE("ANDROID_BITMAP_FORMAT_RGB_565");
                     break;

                case ANDROID_BITMAP_FORMAT_RGBA_4444:
                    LOGE("ANDROID_BITMAP_FORMAT_RGBA_4444");
                    break;

                case ANDROID_BITMAP_FORMAT_A_8:
                    LOGE("ANDROID_BITMAP_FORMAT_A_8");
                    internalFormat = VK_FORMAT_R8_UNORM;
                    break;
            }
            VkResult err;
            bool pass;
            VulkanRenderer* vk_renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());

            VkMemoryAllocateInfo memoryAllocateInfo = {};
            memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
            memoryAllocateInfo.pNext = NULL;
            memoryAllocateInfo.allocationSize = 0;
            memoryAllocateInfo.memoryTypeIndex = 0;

            VkMemoryRequirements mem_reqs;
            VkDevice device = vk_renderer->getDevice();
            LOGE("target = %d, internal %d, width = %d, height = %d", target,internalFormat,info.width,info.height);
            err = vkCreateImage(device, gvr::ImageCreateInfo(target,
                                                                               internalFormat,
                                                                               info.width,
                                                                               info.width, 1, 1, 1,
                                                               VK_IMAGE_TILING_LINEAR,
                                                               VK_IMAGE_USAGE_SAMPLED_BIT,
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

            // Copy source image data into mapped memory
            {
                VkImageSubresource subres;
                subres.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
                subres.mipLevel = 0;
                subres.arrayLayer = 0;

                VkSubresourceLayout layout;
                void *data;

                vkGetImageSubresourceLayout(device, image, &subres, &layout);

                err = vkMapMemory(device, dev_memory, 0, memoryAllocateInfo.allocationSize,
                                  0,  &data);
                assert(!err);

                memcpy(data,pixels,getSize(internalFormat,info));
                vkUnmapMemory( device, dev_memory);
            }
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

            VkPipelineStageFlags src_stages = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            VkPipelineStageFlags dest_stages = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;

            // Barrier on image memory, with correct layouts set.
            vkCmdPipelineBarrier(textureCmdBuffer, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                                 VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 0, 0, NULL, 0, NULL, 1,
                                 &imageMemoryBarrier);

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
                                                                     MapFormat[static_cast<int>(target)],
                                                                       internalFormat, 1, 1,
                                                                       VK_IMAGE_ASPECT_COLOR_BIT), NULL,
                                    &imageView);
            assert(!err);


            AndroidBitmap_unlockPixels(env, bitmap);
            LOGE("imageview created");
            return internalFormat;
        }
        return imageFormat;
    }


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
            LOGE("updatefrom memory ???????");
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
           /* if (mLevels > 1)
            {
                loadCompressedMipMaps(pixels, mFormat);
            }
            else
            {
                glCompressedTexImage2D(t, 0, mFormat, mWidth, mHeight, 0,
                                       mImageSize, pixels + getDataOffset(0));
                glGenerateMipmap(t);
            }*/
            // TODO: Vulkan Compressed textures
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
        VkImageType t = static_cast<VkImageType>(getTarget());

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
