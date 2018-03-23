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
#ifndef FRAMEWORK_VK_FRAMEBUFFER_H
#define FRAMEWORK_VK_FRAMEBUFFER_H
#include "vulkan/vk_imagebase.h"
#include <unordered_map>

namespace gvr {

class VKFramebuffer final{
    vkImageBase *mAttachments[3];

    VkRenderPass mRenderpass;
    int mWidth;
    int mHeight;
    VkFramebuffer mFramebuffer;
public:

    ~VKFramebuffer();

    void setImage(ImageType type, vkImageBase* image){
        mAttachments[type] = image;
    }
    VKFramebuffer(int width, int height) : mRenderpass(0), mWidth(width), mHeight(height), mAttachments{
            nullptr, nullptr, nullptr} {}
    int getWidth() {
        return mWidth;
    }

    int getHeight() {
        return mHeight;
    }

    void addRenderPass(VkRenderPass renderpass) {
        mRenderpass = renderpass;
    }

    VkRenderPass getRenderPass() {
        return mRenderpass;
    }

    const VkImage &getImage(ImageType type) {
        return mAttachments[type]->getVkImage();
    }

    const VkImageView &getImageView(ImageType type) {
        return mAttachments[type]->getVkImageView();
    }

    const VkImageLayout& getImageLayout(ImageType type){
        return mAttachments[type]->getImageLayout();
    }

    VkDeviceMemory getDeviceMemory(ImageType type) {
        return mAttachments[type]->getDeviceMemory();
    }

    const VkBuffer *getImageBuffer(ImageType type) {
        return mAttachments[type]->getBuffer();
    }

    VkDeviceSize getImageSize(ImageType type) {
        return mAttachments[type]->getSize();
    }

    void createFrameBuffer(VkDevice &, int, int sample_count = 1, bool monoscopic = false);
    void createFramebuffer(VkDevice& device);
    const VkFramebuffer &getFramebuffer() {
        return mFramebuffer;
    }

};
}
#endif //FRAMEWORK_VK_FRAMEBUFFER_H