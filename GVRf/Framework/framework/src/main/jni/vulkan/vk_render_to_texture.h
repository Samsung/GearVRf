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

#ifndef FRAMEWORK_VK_RENDER_TO_TEXTURE_H
#define FRAMEWORK_VK_RENDER_TO_TEXTURE_H


#include "objects/textures/render_texture.h"
#include "vk_framebuffer.h"
#include "vulkan/vk_texture.h"

namespace gvr{
class VkRenderTexture : public RenderTexture
{
    VKFramebuffer* fbo;
    void createRenderPass();
    int mWidth, mHeight;
    std::vector <VkClearValue> clear_values;
    VkFence mWaitFence;
    VkDescriptorImageInfo mImageInfo;
public:
    VkFence& getFenceObject(){
        return mWaitFence;
    }
    virtual const VkDescriptorImageInfo& getDescriptorImage();
    // isReady() for renderTexture is blocking call, we will wait till command buffer rendering is complete
    virtual bool isReady();
    void createFenceObject(VkDevice device);
    VkRenderPassBeginInfo getRenderPassBeginInfo();
    VKFramebuffer* getFBO(){
        return fbo;
    }
    virtual int width() const {
        return mWidth;
    }
    virtual int height() const {
        return mHeight;
    }
    virtual ~VkRenderTexture(){
        delete fbo;
    }
    VkRenderTexture(int width, int height, int sample_count = 1):RenderTexture(sample_count), fbo(nullptr),mWidth(width), mHeight(height){}
    virtual unsigned int getFrameBufferId() const {

    }

    virtual unsigned int getDepthBufferId() const {

    }
    virtual void bind();
    virtual void beginRendering(Renderer* renderer);
    virtual void beginRenderingPE(Renderer* renderer);
    virtual void endRendering(Renderer*){}
    virtual void endRenderingPE(Renderer* renderer);
    // Start to read back texture in the background. It can be optionally called before
    // readRenderResult() to read pixels asynchronously. This function returns immediately.
    virtual void startReadBack() {

    }

    // Copy data in pixel buffer to client memory. This function is synchronous. When
    // it returns, the pixels have been copied to PBO and then to the client memory.
    virtual bool readRenderResult(uint8_t *readback_buffer) {}
    virtual void setLayerIndex(int layer_index) {}
    // Copy data in pixel buffer to client memory. This function is synchronous. When
    // it returns, the pixels have been copied to PBO and then to the client memory.
    virtual bool readRenderResult(uint8_t *readback_buffer, long capacity) {
    }
    bool readVkRenderResult(uint8_t **readback_buffer, VkCommandBuffer& cmd_buffer,VkFence& fence);
    VkRenderPass getRenderPass(){
        bind();
        return fbo->getRenderPass();
    }
};

}
#endif //FRAMEWORK_VK_RENDER_TO_TEXTURE_H