#ifndef FRAMEWORK_VK_TEXTURE_H
#define FRAMEWORK_VK_TEXTURE_H
#include <cstdlib>
#include "vulkan/vulkanCore.h"
#include "vulkan/vulkanInfoWrapper.h"
#include "../objects/textures/image.h"
#include "../objects/textures/texture.h"
#include "vulkan/vulkan_image.h"


namespace gvr {
class VkTexture : public Texture
{
public:
    explicit VkTexture() : Texture() { }

    explicit VkTexture(int texture_type) :
            Texture(texture_type)
    {
        mTexParamsDirty = true;
    }

    virtual ~VkTexture();
    virtual bool isReady();

    const VkImageView& getVkImageView();
    VkSampler getVkSampler();

    const VkDescriptorImageInfo& getDescriptorImage(){
        imageInfo.imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
        imageInfo.imageView = getVkImageView();
        imageInfo.sampler = getVkSampler();
        return  imageInfo;
    }
private:
    VkTexture(const VkTexture& gl_texture);
    VkTexture(VkTexture&& gl_texture);
    VkTexture& operator=(const VkTexture& gl_texture);
    VkTexture& operator=(VkTexture&& gl_texture);
    void createSampler(int maxLod);
    void updateSampler();
    bool updateImage();
    VkDescriptorImageInfo imageInfo;
protected:

    static VkSamplerAddressMode MapWrap[];
    static VkFilter MapFilter[];

};

}
#endif //FRAMEWORK_VK_TEXTURE_H