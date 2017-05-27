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
 * RAII class for Vulkan textures.
 ***************************************************************************/

#include "engine/renderer/renderer.h"
#include "engine/renderer/vulkan_renderer.h"
#include "vk_bitmap_image.h"
#include "vk_cubemap_image.h"

namespace gvr {

    VkSamplerAddressMode VkTexture::MapWrap[3] = { VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE, VK_SAMPLER_ADDRESS_MODE_REPEAT, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT };
    VkFilter VkTexture::MapFilter[] = { VK_FILTER_NEAREST, VK_FILTER_LINEAR};
    std::unordered_map<int, VkSampler > samplers_;
    // TODO: Vulkan does not have capability to generate mipmaps on its own, we need to implement this for vulkan
    VkSamplerMipmapMode VkTexture::mipmapMode[] = { VK_SAMPLER_MIPMAP_MODE_NEAREST, VK_SAMPLER_MIPMAP_MODE_LINEAR};
    VkTexture::~VkTexture()
    {
        // Delete texture memory code
    }

    bool VkTexture::isReady()
    {
        if (!Texture::isReady())
        {
            return false;
        }
        if (mTexParamsDirty)
        {
            updateSampler();
            mTexParamsDirty = false;
        }
        return true;
    }

    void VkTexture::updateSampler()
    {
        int numlod = 1;
        if (mImage)
        {
            numlod = mImage->getLevels();
        }
        // create hash code
        int hash = 23;
        hash = hash * 31 + numlod;
        hash = hash * 31 + getTexParams().getHashCode();

        if(samplers_.find(hash) != samplers_.end())
            m_sampler = samplers_[hash];
        else {
            createSampler(numlod);
            samplers_[hash] = m_sampler;
        }
    }

    void VkTexture::createSampler(int maxLod) {

        // Sets the new MIN FILTER
        VkFilter min_filter_type_ = MapFilter[mTexParams.getMinFilter()];

        // Sets the MAG FILTER
        VkFilter mag_filter_type_ = MapFilter[mTexParams.getMagFilter()];

        // Sets the wrap parameter for texture coordinate S
        VkSamplerAddressMode wrap_s_type_ = MapWrap[mTexParams.getWrapU()];

        // Sets the wrap parameter for texture coordinate S
        VkSamplerAddressMode wrap_t_type_ = MapWrap[mTexParams.getWrapV()];

        VulkanRenderer *vk_renderer = static_cast<VulkanRenderer *>(Renderer::getInstance());

        VkResult err;

        err = vkCreateSampler(vk_renderer->getDevice(), gvr::SamplerCreateInfo(min_filter_type_,
                                                                               mag_filter_type_,
                                                                               VK_SAMPLER_MIPMAP_MODE_LINEAR,
                                                                               wrap_s_type_,
                                                                               wrap_t_type_,
                                                                               VK_SAMPLER_ADDRESS_MODE_REPEAT,
                                                                               0.0f,
                                                                               VK_FALSE, 0,
                                                                               VK_FALSE,
                                                                               VK_COMPARE_OP_NEVER,
                                                                               0.0f, (float) maxLod,
                                                                               VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE,
                                                                               VK_FALSE), NULL,
                              &m_sampler);

        assert(!err);

    }

    const VkImageView& VkTexture::getVkImageView()
    {
        if (mImage == NULL)
            LOGE("GetImageView : image is NULL");

        VkCubemapImage* cubemapImage;
        VkBitmapImage* bitmapImage;
        switch(mImage->getType()){

            case Image::ImageType::CUBEMAP:
                cubemapImage = reinterpret_cast<VkCubemapImage*>(mImage);
                return cubemapImage->getVkImageView();

            case Image::ImageType::BITMAP:
                bitmapImage = reinterpret_cast<VkBitmapImage*>(mImage);
                return bitmapImage->getVkImageView();
        }
    }
}
