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

#ifndef FRAMEWORK_VK_CUBEMAP_IMAGE_H
#define FRAMEWORK_VK_CUBEMAP_IMAGE_H
#include <vector>
#include "../objects/textures/cubemap_image.h"
#include "vulkan_headers.h"
#include "vk_image.h"
#include "../util/gvr_log.h"
namespace gvr {
    class VkCubemapImage : public CubemapImage
    {
    public:
        explicit VkCubemapImage(int format) :
                CubemapImage(format)
        {
            mTarget = static_cast<int>( VK_IMAGE_VIEW_TYPE_CUBE);
        }
        const VkImageView& getVkImageView(){
            return mVkImage.getVkImageView();
        }

    protected:
        virtual void update(int texid);

    private:
        vkImage mVkImage;
        VkCubemapImage(const VkCubemapImage&);
        VkCubemapImage(VkCubemapImage&&);
        VkCubemapImage& operator=(const VkCubemapImage&);
        VkCubemapImage& operator=(VkCubemapImage&&);

        void updateFromBitmap(int texid);
        void updateFromMemory(int texid);
    };

}

#endif //FRAMEWORK_VK_CUBEMAP_IMAGE_H
