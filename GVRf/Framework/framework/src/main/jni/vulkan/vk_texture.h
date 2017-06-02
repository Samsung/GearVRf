//
// Created by roshan on 12/28/16.
//

#ifndef FRAMEWORK_VK_TEXTURE_H
#define FRAMEWORK_VK_TEXTURE_H
#include <cstdlib>
#include "objects/textures/image.h"
#include "objects/textures/texture.h"

#include "vulkanCore.h"
#include "vulkanInfoWrapper.h"

namespace gvr {
    class VkTexture : public Texture
    {
    public:
        explicit VkTexture() : Texture() { }

        explicit VkTexture(int texture_type) :
                Texture(texture_type)
        {
        }

        virtual ~VkTexture();
        virtual bool isReady();

        const VkImageView& getVkImageView();
        VkSampler& getVkSampler(){
            return m_sampler;
        }
    private:
        VkTexture(const VkTexture& gl_texture);
        VkTexture(VkTexture&& gl_texture);
        VkTexture& operator=(const VkTexture& gl_texture);
        VkTexture& operator=(VkTexture&& gl_texture);
        void createSampler(int maxLod);
        void updateSampler();
        bool updateImage();

    protected:
        static VkSamplerAddressMode MapWrap[];
        static VkFilter MapFilter[];
        static VkSamplerMipmapMode mipmapMode[];
        VkSampler m_sampler;
        VkWriteDescriptorSet writeDescriptorSet;
    };

}
#endif //FRAMEWORK_VK_TEXTURE_H
