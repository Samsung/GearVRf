//
// Created by roshan on 12/28/16.
//

#ifndef FRAMEWORK_VK_TEXTURE_H
#define FRAMEWORK_VK_TEXTURE_H
#include <cstdlib>
#include "../objects/textures/image.h"
#include "vulkan/vulkanCore.h"
#include "vulkan/vulkanInfoWrapper.h"
#include "../objects/textures/texture.h"
#include "vk_bitamp_image.h"
#include "vk_cubemap_image.h"
namespace gvr {
    class VkTexture : public Texture
    {
    public:
        explicit VkTexture() : Texture() { }

        explicit VkTexture(int texture_type) :
                Texture(texture_type){ }

        virtual ~VkTexture();
        virtual bool isReady();
        virtual bool updateGPU();

        virtual int getId()
        {
            isReady();
            return id();
        }

        GLuint id() const
        {
            return id_;
        }
        const VkImageView& getVkImageView(){

            VkCubemapImage* cubemapImage;
            VkBitmapImage* bitmapImage;
            switch(mImage->getType()){

                case Image::ImageType::CUBEMAP:

                    cubemapImage= static_cast<VkCubemapImage*>(mImage);
                    return cubemapImage->getVkImageView();

                case Image::ImageType::BITMAP:
                    bitmapImage = static_cast<VkBitmapImage*>(mImage);
                    return bitmapImage->getVkImageView();
            }
        }
        VkSampler& getVkSampler(){
            return m_sampler;
        }
    private:
        VkTexture(const VkTexture& gl_texture);
        VkTexture(VkTexture&& gl_texture);
        VkTexture& operator=(const VkTexture& gl_texture);
        VkTexture& operator=(VkTexture&& gl_texture);

        void updateTexParams();
        bool updateImage();

    protected:
        static VkSamplerAddressMode MapWrap[];
        static VkFilter MapFilter[];
        static VkSamplerMipmapMode mipmapMode[];
        GLuint id_;
        VkSampler m_sampler;
     //   GlDelete* deleter_;
    };

}
#endif //FRAMEWORK_VK_TEXTURE_H
