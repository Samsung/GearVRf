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


#include "../engine/renderer/renderer.h"
#include "vk_render_to_texture.h"
#include "../engine/renderer/vulkan_renderer.h"
#include "vk_texture.h"
namespace gvr{
VkRenderTexture::VkRenderTexture(int width, int height, int fboType, int layers, int sample_count):RenderTexture(sample_count), fbo(nullptr),mWidth(width), mHeight(height), mFboType(fboType), mLayers(layers), mSamples(sample_count){
    initVkData();
}

const VkDescriptorImageInfo& VkRenderTexture::getDescriptorImage(ImageType imageType){
    mImageInfo.imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
    mImageInfo.imageView = fbo->getImageView(imageType);
    TextureParameters textureParameters = TextureParameters();
    uint64_t index = textureParameters.getHashCode();
    index = (index << 32) | 1;
    if (getSampler(index) == 0)
        VkTexture::createSampler(textureParameters, 1);
    mImageInfo.sampler = getSampler(index);
    return  mImageInfo;
}

void VkRenderTexture::createRenderPass(){
    VulkanRenderer* vk_renderer= static_cast<VulkanRenderer*>(Renderer::getInstance());
    VkRenderPass renderPass;
    if(mFboType == (DEPTH_IMAGE | COLOR_IMAGE))
        renderPass = vk_renderer->getCore()->createVkRenderPass(NORMAL_RENDERPASS, mSampleCount);
    else
        renderPass = vk_renderer->getCore()->createVkRenderPass(SHADOW_RENDERPASS, mSampleCount);

    clear_values.resize(3);
    fbo->addRenderPass(renderPass);
}

void VkRenderTexture::initVkData(){
    VulkanRenderer* renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());
    mWaitFence = 0;
    mCmdBuffer = renderer->createCommandBuffer(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
    mWaitFence = renderer->createFenceObject();
}

VkRenderPassBeginInfo VkRenderTexture::getRenderPassBeginInfo(){
    VkClearValue clear_color;
    VkClearValue clear_depth;

    clear_color.color.float32[0] = mBackColor[0];
    clear_color.color.float32[1] = mBackColor[1];
    clear_color.color.float32[2] = mBackColor[2];
    clear_color.color.float32[3] = mBackColor[3];

    clear_depth.depthStencil.depth = 1.0f;
    clear_depth.depthStencil.stencil = 0;

    clear_values[0] = clear_color;
    if(mSampleCount > 1) {
        clear_values[1] = clear_color;
        clear_values[2] = clear_depth;
    } else
        clear_values[1] = clear_depth;

    VkRenderPassBeginInfo rp_begin = {};
    rp_begin.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
    rp_begin.pNext = nullptr;
    rp_begin.renderPass = fbo->getRenderPass();
    rp_begin.framebuffer = fbo->getFramebuffer(layer_index_);
    rp_begin.renderArea.offset.x = 0;
    rp_begin.renderArea.offset.y = 0;
    rp_begin.renderArea.extent.width = fbo->getWidth();
    rp_begin.renderArea.extent.height = fbo->getHeight();
    rp_begin.clearValueCount = clear_values.size();
    rp_begin.pClearValues = clear_values.data();

    return rp_begin;
}

void VkRenderTexture::bind() {
    if(fbo == nullptr){
        fbo = new VKFramebuffer(mWidth,mHeight);
        createRenderPass();
        VulkanRenderer* vk_renderer= static_cast<VulkanRenderer*>(Renderer::getInstance());

        fbo->createFrameBuffer(vk_renderer->getDevice(), DEPTH_IMAGE | COLOR_IMAGE, mLayers, mSamples);
    }
}

void VkRenderTexture::beginRendering(Renderer* renderer){
    bind();

    VkRenderPassBeginInfo rp_begin = getRenderPassBeginInfo();
    VkViewport viewport = {};
    viewport.height = (float) height();
    viewport.width = (float) width();
    viewport.minDepth = (float) 0.0f;
    viewport.maxDepth = (float) 1.0f;

    VkRect2D scissor = {};
    scissor.extent.width =(float) width();
    scissor.extent.height =(float) height();
    scissor.offset.x = 0;
    scissor.offset.y = 0;

    vkCmdSetScissor(mCmdBuffer,0,1, &scissor);
    vkCmdSetViewport(mCmdBuffer,0,1,&viewport);
    vkCmdBeginRenderPass(mCmdBuffer, &rp_begin, VK_SUBPASS_CONTENTS_INLINE);
}

/*
 * Bind the framebuffer to the specified layer of the texture array.
 * Create the framebuffer and layered texture if necessary.
 */
    void VkRenderTexture::setLayerIndex(int layerIndex)
    {
        layer_index_ = layerIndex;
    }


bool VkRenderTexture::isReady(){
    VkResult err;
    VulkanRenderer* renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());
    VkDevice device = renderer->getDevice();
    if(mWaitFence != 0) {
        err = vkGetFenceStatus(device,mWaitFence);
        if (err == VK_SUCCESS)
            return true;

        if(VK_SUCCESS != vkWaitForFences(device, 1, &mWaitFence, VK_TRUE,
                                         4294967295U))
            return false;

    }
    return true;
}


bool VkRenderTexture::readRenderResult(uint8_t *readback_buffer){
    //wait for rendering to be complete
    if(!isReady()) {
        LOGE("VkRenderTexture::readRenderResult: error in rendering");
        return false;
    }

    uint8_t *data;
    bool result = accessRenderResult(&data);
    VulkanRenderer* vk_renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());

    /* vulkan has a left handed NDC, with y axis facing downwards. The pointer returned after mapping
     * device memory points to the memory laid out assuming that the top left of the image is the starting
     * point. The image rendered is already "upside down". When we pass it to oculus it assumes the top left
     * to be the bottom left, as is the GL convention, and everything sorts itself out accordingly.
     * But in case of monoscopic, we need to modify the proj mat to make this possible. (see renderRenderTarget in
     * vulkan_renderer.cpp). This makes sure that the rendered result is upright. The pointer returned
     * to the device mem still points to the top left though. Therefore, during taking a screenshot,
     * we need to make sure we copy the bottom most row first into the byte buffer (which is being used to create
     * the bitmap).
     * */
    if(vk_renderer->getCore()->isSwapChainPresent())
    {
        int offset = 0;
        size_t rowSize = sizeof(u_char) * 4 * mWidth;
        u_char  * bytedata = data; u_char * readbackdata = readback_buffer;
        for(int i = mHeight - 1; i >=0 ; i -- )
        {
            memcpy(readbackdata + offset, bytedata + (i * rowSize), rowSize);
            offset += rowSize;
        }
    }else{
        memcpy(readback_buffer, data, mWidth*mHeight*4);
    }
    unmapDeviceMemory();

    return result;
}

    bool VkRenderTexture::accessRenderResult(uint8_t **readback_buffer) {

        if(!fbo)
            return false;

        VkResult err;
        VulkanRenderer* vk_renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());
        VkDevice device = vk_renderer->getDevice();

        err = vkResetFences(device, 1, &mWaitFence);
        vk_renderer->getCore()->beginCmdBuffer(mCmdBuffer);
        VkExtent3D extent3D = {};
        extent3D.width = mWidth;
        extent3D.height = mHeight;
        extent3D.depth = 1;
        VkBufferImageCopy region = {0};
        region.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        region.imageSubresource.layerCount = 1;
        region.imageExtent = extent3D;
        vkCmdCopyImageToBuffer(mCmdBuffer,  fbo->getImage(COLOR_IMAGE),
                               VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                               *(fbo->getImageBuffer(COLOR_IMAGE)), 1, &region);
        vkEndCommandBuffer(mCmdBuffer);

        VkSubmitInfo ssubmitInfo = {};
        ssubmitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        ssubmitInfo.commandBufferCount = 1;
        ssubmitInfo.pCommandBuffers = &mCmdBuffer;

        vkQueueSubmit(vk_renderer->getQueue(), 1, &ssubmitInfo, mWaitFence);

        uint8_t *data;
        err = vkWaitForFences(device, 1, &mWaitFence, VK_TRUE, 4294967295U);

        VkDeviceMemory mem = fbo->getDeviceMemory(COLOR_IMAGE);
        err = vkMapMemory(device, mem, 0,
                          fbo->getImageSize(COLOR_IMAGE), 0, (void **) &data);

        *readback_buffer = data;
        //GVR_VK_CHECK(!err);

        return true;

    }


    void VkRenderTexture::unmapDeviceMemory()
    {
        if(!fbo)
            return;

        VulkanRenderer* vk_renderer = static_cast<VulkanRenderer*>(Renderer::getInstance());
        VkDevice device = vk_renderer->getDevice();
        VkDeviceMemory mem = fbo->getDeviceMemory(COLOR_IMAGE);
        vkUnmapMemory(device, mem);
    }

}