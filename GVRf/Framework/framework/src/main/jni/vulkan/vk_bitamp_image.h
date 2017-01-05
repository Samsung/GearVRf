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

#ifndef FRAMEWORK_VKBITMAPIMAGE_H
#define FRAMEWORK_VKBITMAPIMAGE_H


#include "objects/textures/bitmap_image.h"
#include "vulkan/vulkan.h"
//#include "vk_image.h"

namespace gvr {
    class VkBitmapImage : public BitmapImage
    {
    public:

        explicit VkBitmapImage (int format) :
                BitmapImage(format)
        {
            mTarget = static_cast<int>(VK_IMAGE_VIEW_TYPE_2D);
        }

        VkImageView& getVkImageView(){
          //  return mVkImage.getVkImageView();
            return imageView;
        }
        int updateFromBitmap(JNIEnv *env, VkImageViewType target, jobject bitmap);
    private:
        int updateVkImage(size_t texSize, std::vector<void*>& pixels,std::vector<AndroidBitmapInfo>& bitmapInfos, std::vector<VkBufferImageCopy>& bufferCopyRegions, VkImageViewType target, VkFormat internalFormat);

        int updateVkImage(VkImageViewType target, VkFormat internal_format, int width, int height, void*);
    protected:
        //vkImage mVkImage;
        VkImage image;
        VkDeviceMemory dev_memory;
        VkImageLayout imageLayout;
        VkImageView imageView;

      virtual void update(int texid);
        void updateFromMemory(int texid);
        void updateFromBitmap(int texid);
        void loadCompressedMipMaps(jbyte *data, int format);
    };

}

#endif //FRAMEWORK_VKBITMAPIMAGE_H
