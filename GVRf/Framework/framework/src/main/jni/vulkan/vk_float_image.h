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
 * Float Textures
 ***************************************************************************/
#ifndef FRAMEWORK_VK_FLOAT_IMAGE_H
#define FRAMEWORK_VK_FLOAT_IMAGE_H

#include "vulkan_headers.h"
#include "objects/textures/float_image.h"
#include "../util/jni_utils.h"

namespace gvr {
    class VkFloatImage : public vkImageBase, public FloatImage
    {
    public:
        VkFloatImage() : FloatImage(), vkImageBase(VK_IMAGE_VIEW_TYPE_2D)
        { }

        virtual ~VkFloatImage() {}

        virtual int getId() { return 0; }
        virtual void texParamsChanged(const TextureParameters&) { }
        virtual bool isReady()
        {
            return checkForUpdate(true);
        }

    protected:
        virtual void update(int texid)
        {
            JNIEnv *env = getCurrentEnv(mJava);
            jfloatArray array = static_cast<jfloatArray>(env->NewLocalRef(mData));
            float* pixels = env->GetFloatArrayElements(array, 0);
            VkImageViewType target = static_cast<VkImageViewType>(getTarget());
            VkBufferImageCopy bufferCopyRegion = {};
            ImageInfo imageInfo = {};
            std::vector<VkBufferImageCopy> bufferCopyRegions;
            std::vector<ImageInfo> imageInfos;
            std::vector<void*> texData;
            bufferCopyRegion.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            bufferCopyRegion.imageSubresource.mipLevel = 0;
            bufferCopyRegion.imageSubresource.baseArrayLayer = 0;
            bufferCopyRegion.imageSubresource.layerCount = 1;
            bufferCopyRegion.imageExtent.width = static_cast<uint32_t>(mWidth);
            bufferCopyRegion.imageExtent.height = static_cast<uint32_t>(mHeight);
            bufferCopyRegion.imageExtent.depth = 1;
            bufferCopyRegion.bufferOffset = 0;
            bufferCopyRegions.push_back(bufferCopyRegion);
            imageInfo.width = bufferCopyRegion.imageExtent.width;
            imageInfo.height = bufferCopyRegion.imageExtent.height;
            imageInfo.size = mImageSize;
            imageInfo.isCompressed = false;
            imageInfo.mipLevel = 0;
            texData.push_back(pixels);
            imageInfos.push_back(imageInfo);
            texData.push_back(pixels);
            VkFormat internalFormat = VK_FORMAT_R32G32_SFLOAT;
            updateVkImage(mImageSize, texData, imageInfos, bufferCopyRegions, target,
                                   internalFormat);

            env->ReleaseFloatArrayElements(array, pixels, 0);
            env->DeleteLocalRef(array);
            clearData(env);
        }

    private:
        VkFloatImage(const VkFloatImage&) = delete;
        VkFloatImage(VkFloatImage&&) = delete;
        VkFloatImage& operator=(const VkFloatImage&) = delete;
        VkFloatImage& operator=(VkFloatImage&&) = delete;
    };

}
#endif //FRAMEWORK_VK_FLOAT_IMAGE_H
