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

#include <engine/renderer/renderer.h>
#include <engine/renderer/vulkan_renderer.h>
#include "vk_bitmap_image.h"
#include "vk_cubemap_image.h"

namespace gvr {

VkSamplerAddressMode VkTexture::MapWrap[3] = { VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE, VK_SAMPLER_ADDRESS_MODE_REPEAT, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT };
VkFilter VkTexture::MapFilter[] = { VK_FILTER_NEAREST, VK_FILTER_LINEAR};

// TODO: Vulkan does not have capability to generate mipmaps on its own, we need to implement this for vulkan

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
    Image* image = getImage();
    if (image)
    {
        numlod = image->getLevels();
    }
    createSampler(mTexParams, numlod);
}
VkSampler VkTexture::getVkSampler(){
    uint64_t index = mTexParams.getHashCode();
    index = (index << 32) | getImage()->getLevels();
    return getSampler(index);
}
void VkTexture::createSampler(TextureParameters& textureParameters, int maxLod) {
    uint64_t index = textureParameters.getHashCode();
    index = (index << 32) | maxLod;

    VkSampler sampler = getSampler(index);
    if(sampler != 0)
        return;
    
    // Sets the new MIN FILTER
    int min_filter = textureParameters.getMinFilter();
    VkFilter min_filter_type_ = min_filter <= 1 ? MapFilter[min_filter] : VK_FILTER_LINEAR;
    // Sets the MAG FILTER
    VkFilter mag_filter_type_ = MapFilter[textureParameters.getMagFilter()];

    // Sets the wrap parameter for texture coordinate S
    VkSamplerAddressMode wrap_s_type_ = MapWrap[textureParameters.getWrapU()];

    // Sets the wrap parameter for texture coordinate S
    VkSamplerAddressMode wrap_t_type_ = MapWrap[textureParameters.getWrapV()];

    VulkanRenderer *vk_renderer = static_cast<VulkanRenderer *>(Renderer::getInstance());

    VkResult err;

    err = vkCreateSampler(vk_renderer->getDevice(), gvr::SamplerCreateInfo(min_filter_type_,
                                                                           mag_filter_type_,
                                                                           VK_SAMPLER_MIPMAP_MODE_NEAREST,
                                                                           wrap_s_type_,
                                                                           wrap_t_type_,
                                                                           wrap_t_type_,
                                                                           0.0f,
                                                                           VK_FALSE, 0,
                                                                           VK_FALSE,
                                                                           VK_COMPARE_OP_NEVER,
                                                                           0.0f, (float) maxLod,
                                                                           VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE,
                                                                           VK_FALSE), NULL, &sampler);

    samplers.push_back(index);
    samplers.push_back((uint64_t )sampler);
    assert(!err);

}
const VkImageView& VkTexture::getVkImageView()
{
    Image* image = getImage();
    if (image == NULL)
        LOGE("GetImageView : image is NULL");

    VkCubemapImage* cubemapImage;
    VkBitmapImage* bitmapImage;
    switch(image->getType()){

        case Image::ImageType::CUBEMAP:
            cubemapImage = static_cast<VkCubemapImage*>(image);
            return cubemapImage->getVkImageView();

        case Image::ImageType::BITMAP:
            bitmapImage = static_cast<VkBitmapImage*>(image);
            return bitmapImage->getVkImageView();
    }
}
}
