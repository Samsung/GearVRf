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
#include "objects/textures/cubemap_image.h"
#include "vulkan_headers.h"
#include "vulkan/vulkan_image.h"
namespace gvr {
    class VkCubemapImage : public vkImageBase, public CubemapImage
    {
    public:
        explicit VkCubemapImage(int format);
        virtual ~VkCubemapImage() {}
        virtual int getId() { return 1; }
        virtual void texParamsChanged(const TextureParameters&) { }

        virtual bool isReady()
        {
            return checkForUpdate(true);
        }

    protected:
        virtual void update(int texid);

    private:
        VkCubemapImage(const VkCubemapImage&) = delete;
        VkCubemapImage(VkCubemapImage&&) = delete;
        VkCubemapImage& operator=(const VkCubemapImage&) = delete;
        VkCubemapImage& operator=(VkCubemapImage&&) = delete;

        void updateFromBitmap(int texid);
        void updateFromMemory(int texid);
    };

}

#endif //FRAMEWORK_VK_CUBEMAP_IMAGE_H
