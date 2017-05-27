//
// Created by roshan on 5/23/17.
//

#ifndef FRAMEWORK_VK_FRAMEBUFFER_H
#define FRAMEWORK_VK_FRAMEBUFFER_H
#include "vulkan_image.h"
#include <unordered_map>
namespace gvr {

class VKFramebuffer {
    std::unordered_map<int, vkImageBase *> mAttachments;
    VkRenderPass *mRenderpass;
    int mWidth;
    int mHeight;
    VkFramebuffer mFramebuffer;
public:
    ~VKFramebuffer() {
        delete mRenderpass;
        delete mAttachments[COLOR_IMAGE];
        delete mAttachments[DEPTH_IMAGE];
    }
    VKFramebuffer(int width, int height, int sample_count) : mRenderpass(NULL), mWidth(width), mHeight(height) {
        vkImageBase *colorImage = new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, VK_FORMAT_R8G8B8A8_UNORM, mWidth,
                                          mHeight, 1, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT |
                                                      VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                                          VK_IMAGE_LAYOUT_UNDEFINED, sample_count);
        colorImage->createImageView(true);
        vkImageBase *depthImage = (new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, VK_FORMAT_D16_UNORM, mWidth,
                                           mHeight, 1, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                                           VK_IMAGE_LAYOUT_UNDEFINED));
        depthImage->createImageView(false);
        mAttachments[COLOR_IMAGE] = colorImage;
        mAttachments[DEPTH_IMAGE] = depthImage;
    }
    VKFramebuffer(int width, int height) : mRenderpass(NULL), mWidth(width), mHeight(height) {
        vkImageBase *colorImage = new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, VK_FORMAT_R8G8B8A8_UNORM, mWidth,
                                          mHeight, 1, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT |
                                                      VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                                          VK_IMAGE_LAYOUT_UNDEFINED);
        colorImage->createImageView(true);
        vkImageBase *depthImage = (new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, VK_FORMAT_D16_UNORM, mWidth,
                                           mHeight, 1, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                                           VK_IMAGE_LAYOUT_UNDEFINED));
        depthImage->createImageView(false);
        mAttachments[COLOR_IMAGE] = colorImage;
        mAttachments[DEPTH_IMAGE] = depthImage;
    }

    int getWidth() {
        return mWidth;
    }

    int getHeight() {
        return mHeight;
    }

    void addRenderPass(VkRenderPass *renderpass) {
        mRenderpass = renderpass;
    }

    const VkRenderPass *getRenderPass() {
        return mRenderpass;
    }

    const VkImage &getImage(ImageType type) {
        return mAttachments[type]->getVkImage();
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

    void createFrameBuffer(VkDevice &);

    const VkFramebuffer &getFramebuffer() {
        return mFramebuffer;
    }
};
}
#endif //FRAMEWORK_VK_FRAMEBUFFER_H
