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

#ifndef FRAMEWORK_VK_IMAGE_H
#define FRAMEWORK_VK_IMAGE_H

#include <vector>
#include <android/bitmap.h>
#include "vulkan_headers.h"

namespace gvr{
    size_t getSize(VkFormat format, int width, int height);
    struct ImageInfo{
        int width;
        int height;
        size_t size;
        int mipLevel;
        bool isCompressed;
    };
    class vkImage{
    public:
        vkImage(){}
        virtual ~vkImage(){ }
       // void updateMipVkImage(int width, int height, void* pixels, VkImageViewType target, VkFormat internalFormat);
        int updateVkImage(uint64_t texSize, std::vector<void*>& pixels,std::vector<ImageInfo>& bitmapInfos, std::vector<VkBufferImageCopy>& bufferCopyRegions, VkImageViewType target, VkFormat internalFormat, bool isCubemap = false, int mipLevels =1,VkImageCreateFlags flags=0);
        int updateMipVkImage(uint64_t texSize, std::vector<void*>& pixels,std::vector<ImageInfo>& bitmapInfos, std::vector<VkBufferImageCopy>& bufferCopyRegions, VkImageViewType target, VkFormat internalFormat, int mipLevels =1,VkImageCreateFlags flags=0);

        const VkImage& getVkImage(){
            return image;
        }
        const VkImageView& getVkImageView(){
            return imageView;
        }
        const VkImageLayout& getImageLayout(){
            return imageLayout;
        }
    private:
        VkImage image;
        VkDeviceMemory dev_memory;
        VkImageLayout imageLayout;
        VkImageView imageView;

    };
}
#endif //FRAMEWORK_VK_IMAGE_H
