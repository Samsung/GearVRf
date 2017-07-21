#ifndef FRAMEWORK_VK_FRAMEBUFFER_H
#define FRAMEWORK_VK_FRAMEBUFFER_H
#include "vulkan/vk_imagebase.h"
#include "vulkan/vulkan.h"
#include <unordered_map>
namespace gvr {

class VKFramebuffer {
    vkImageBase *mAttachments[3];

    VkRenderPass mRenderpass;
    int mWidth;
    int mHeight;
    VkFramebuffer mFramebuffer;
public:

    ~VKFramebuffer() {
        delete mAttachments[COLOR_IMAGE];
        delete mAttachments[DEPTH_IMAGE];
    }
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

    void createFrameBuffer(VkDevice &, int, int sample_count = 1);
    void createFramebuffer(VkDevice& device);
    const VkFramebuffer &getFramebuffer() {
        return mFramebuffer;
    }

};
}
#endif //FRAMEWORK_VK_FRAMEBUFFER_H