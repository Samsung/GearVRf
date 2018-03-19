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

#ifndef FRAMEWORK_VK_IMAGEBASE
#define FRAMEWORK_VK_IMAGEBASE

#include <vector>
#include <memory>
#include "vulkan_headers.h"

namespace gvr {
    struct ImageInfo
    {
        int width;
        int height;
        size_t size;
        int mipLevel;
        bool isCompressed;
    };

    enum ImageType{
        MULTISAMPLED_IMAGE = 0, COLOR_IMAGE = 1, DEPTH_IMAGE = 2
    };

class vkImageBase
{
    public:
    explicit vkImageBase(VkImageViewType type) : outBuffer(new VkBuffer),imageType(type), size(0), format_(VK_FORMAT_R8G8B8A8_UNORM), tiling_(VK_IMAGE_TILING_LINEAR), usage_flags_(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT),mSampleCount(1)
    { }
    explicit vkImageBase(VkImageViewType type, VkFormat format, int width, int height, int depth, VkImageTiling tiling, VkImageUsageFlags flags, VkImageLayout imageLayout)
    : mLayers(1),imageType(type), outBuffer(new VkBuffer), size(0), format_(format), usage_flags_(flags), width_(width), height_(height), depth_(depth), imageLayout(imageLayout), mSampleCount(1)
    { }

    explicit vkImageBase(VkImageViewType type, VkFormat format, int width, int height, int depth, VkImageTiling tiling, VkImageUsageFlags flags, VkImageLayout imageLayout, int sample_count)
    :mLayers(1), imageType(type), outBuffer(new VkBuffer), size(0), format_(format), usage_flags_(flags), width_(width), height_(height), depth_(depth), imageLayout(imageLayout), mSampleCount(sample_count)
    { }
    explicit vkImageBase(VkImageViewType type, VkFormat format, int width, int height, int depth, VkImageTiling tiling, VkImageUsageFlags flags, VkImageLayout imageLayout, int layers, int sample_count )
    :imageType(type), outBuffer(new VkBuffer), mLayers(layers) ,size(0), format_(format), usage_flags_(flags), width_(width), height_(height), depth_(depth), imageLayout(imageLayout), mSampleCount(sample_count)
    { }
    virtual ~vkImageBase();
        void createImageView(bool host_accessible);
        void updateMipVkImage(uint64_t texSize, std::vector<void*>& pixels,std::vector<ImageInfo>& bitmapInfos, std::vector<VkBufferImageCopy>& bufferCopyRegions, VkImageViewType target, VkFormat internalFormat, int mipLevels =1,VkImageCreateFlags flags=0);

        VkImageViewType getImageType() const { return imageType; }

        const VkImageView& getVkImageView(){
            return imageView;
        }

    void setVkImageView(VkImageView img){
         imageView = img;
    }

        const VkImageLayout& getImageLayout(){
            return imageLayout;
        }
        VkBuffer* const getBuffer(){
            return outBuffer.get();
        }
        VkDeviceMemory getDeviceMemory(){
            return dev_memory;
        }
        VkFormat getFormat(){
            return format_;
        }
        const VkImage& getVkImage(){
            return image;
        }

        void setVkImage(VkImage img){
            image = img;
        }
        VkDeviceSize getSize(){
            return size;
        }

    private:
        VkImageViewType imageType;
        VkImage image;
        VkDeviceMemory dev_memory = 0, host_memory = 0;
        VkImageLayout imageLayout;
        VkImageView imageView;
        VkFormat format_;
        int mSampleCount;
        int width_, height_, depth_,  mLayers;
        VkImageTiling tiling_;
        VkImageUsageFlags usage_flags_;
        std::unique_ptr<VkBuffer> outBuffer = nullptr;
        VkBuffer hostBuffer = 0;
        VkDeviceSize size;
        bool host_accessible_ = false;
};
}
#endif
