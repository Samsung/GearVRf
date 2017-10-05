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
 * Cube map texture made by six bitmaps.
 ***************************************************************************/

#include <cmath>
#include "vulkan/vk_cubemap_image.h"
#include "engine/renderer/renderer.h"
#include "engine/renderer/vulkan_renderer.h"
#include "util/jni_utils.h"
#include "util/scope_exit.h"

namespace gvr {
extern std::map<int, VkFormat> compressed_formats;
    VkCubemapImage::VkCubemapImage(int format) :
            vkImageBase(VK_IMAGE_VIEW_TYPE_CUBE),
            CubemapImage(format)
    { }


    void VkCubemapImage::update(int texid) {
        if (mJava == NULL) {
            return;
        }
        if (mBitmaps != NULL) {
            updateFromBitmap(texid);
            updateComplete();
        }
        else if (mTextures != NULL) {
            updateFromMemory(texid);
            updateComplete();
        }
    }

    void VkCubemapImage::updateFromBitmap(int texid) {

        JNIEnv *env = getCurrentEnv(mJava);
        jobjectArray bmapArray = static_cast<jobjectArray>(env->NewLocalRef(mBitmaps));
        if (bmapArray == NULL) {
            LOGE("CubemapImage::updateFromBitmap bitmap array NULL");
            return;
        }
        // Clean up upon scope exit. The SCOPE_EXIT utility is used
        // to avoid duplicated code in the throw case and normal
        // case.
        SCOPE_EXIT(clearData(env);
                           env->DeleteLocalRef(bmapArray));
        VkImageViewType t = getImageType();

        std::vector<void *> texData;
        size_t tex_size = 0;
        std::vector<VkBufferImageCopy> bufferCopyRegions;
        std::vector<jobject> bitmaps;
        std::vector<ImageInfo> imageInfos;
        for (int i = 0; i < 6; i++) {
            jobject bitmap = env->NewLocalRef(env->GetObjectArrayElement(bmapArray, i));
            AndroidBitmapInfo info;
            void *pixels;
            int ret;

            if (bitmap == NULL) {
                LOGE("CubemapImage::updateFromBitmap bitmap %d is NULL", i);
            }
            else if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
                LOGE("CubemapImage::updateFromBitmap AndroidBitmap_getInfo() failed! error = %d",
                     ret);
            }
            else if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
                LOGE("CubemapImage::updateFromBitmap AndroidBitmap_lockPixels() failed! error = %d",
                     ret);
            }
            else {
                mLevels = static_cast<int>(floor(log2(std::max(info.width, info.height))) + 1);
                {
                    VkBufferImageCopy bufferCopyRegion = {};
                    ImageInfo imageInfo = {};
                    mWidth = info.width;
                    mHeight = info.height;
                    mImageSize = info.height * info.stride;
                    bufferCopyRegion.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
                    bufferCopyRegion.imageSubresource.mipLevel = 0;
                    bufferCopyRegion.imageSubresource.baseArrayLayer = i;
                    bufferCopyRegion.imageSubresource.layerCount = 1;
                    bufferCopyRegion.imageExtent.depth = 1;

                    bufferCopyRegion.imageExtent.width = static_cast<uint32_t>(mWidth);
                    bufferCopyRegion.imageExtent.height = static_cast<uint32_t>(mHeight);
                    bufferCopyRegion.imageExtent.depth = 1;
                    bufferCopyRegion.bufferOffset = tex_size;
                    imageInfo.width = bufferCopyRegion.imageExtent.width;
                    imageInfo.height = bufferCopyRegion.imageExtent.height;
                    imageInfo.size = mImageSize;
                    imageInfo.isCompressed = false;
                    imageInfo.mipLevel = 0;

                    tex_size += mImageSize;

                    bufferCopyRegions.push_back(bufferCopyRegion);
                    texData.push_back(pixels);
                    bitmaps.push_back(bitmap);
                    imageInfos.push_back(imageInfo);
                }
            }
        }
        updateMipVkImage(tex_size, texData, imageInfos, bufferCopyRegions, t,
                         VK_FORMAT_R8G8B8A8_UNORM, mLevels);
        for (int i = 0; i < bitmaps.size(); i++) {
            AndroidBitmap_unlockPixels(env, bitmaps[i]);
            env->DeleteLocalRef(bitmaps[i]);
        }

    }

    void VkCubemapImage::updateFromMemory(int texid) {
        LOGE("calling updatefrommeomry");
        JNIEnv *env = getCurrentEnv(mJava);
        jobjectArray texArray = static_cast<jobjectArray>(mTextures);
        if (texArray == NULL)
        {
            LOGE("CubemapImage::updateFromMemory texture array NULL");
            return;
        }

        // Clean up upon scope exit
        SCOPE_EXIT( clearData(env); );

        VkImageViewType t = getImageType();

        std::vector<void *> texData;
        size_t tex_size = 0;
        std::vector<VkBufferImageCopy> bufferCopyRegions;
        std::vector<jbyteArray> bitmaps;
        std::vector<ImageInfo> imageInfos;
        for (int i = 0; i < 6; i++) {
            jbyteArray byteArray = static_cast<jbyteArray>(env->GetObjectArrayElement(texArray, i));
            void *pixels;
            int ret;

            if (byteArray == NULL) {
                LOGE("CubemapImage::updateFromBitmap bitmap %d is NULL", i);
            }
            else {
                pixels = env->GetByteArrayElements(byteArray, 0);
                pixels = (char*)pixels + getDataOffset(i);
                // compressed textures doesn't have mipmaps, change it for sampler
                mLevels = 0;
                {
                    VkBufferImageCopy bufferCopyRegion = {};
                    ImageInfo imageInfo = {};
                    bufferCopyRegion.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
                    bufferCopyRegion.imageSubresource.mipLevel = 0;
                    bufferCopyRegion.imageSubresource.baseArrayLayer = i;
                    bufferCopyRegion.imageSubresource.layerCount = 1;
                    bufferCopyRegion.imageExtent.depth = 1;

                    bufferCopyRegion.imageExtent.width = static_cast<uint32_t>(mWidth);
                    bufferCopyRegion.imageExtent.height = static_cast<uint32_t>(mHeight);
                    bufferCopyRegion.imageExtent.depth = 1;
                    bufferCopyRegion.bufferOffset = tex_size;
                    imageInfo.width = bufferCopyRegion.imageExtent.width;
                    imageInfo.height = bufferCopyRegion.imageExtent.height;
                    imageInfo.size = mImageSize;
                    imageInfo.isCompressed = true;
                    imageInfo.mipLevel = 0;

                    tex_size += mImageSize;

                    bufferCopyRegions.push_back(bufferCopyRegion);
                    texData.push_back(pixels);
                    bitmaps.push_back(byteArray);
                    imageInfos.push_back(imageInfo);
                }
            }
        }

        VkFormat internal_format = compressed_formats[mFormat];
        updateMipVkImage(tex_size, texData, imageInfos, bufferCopyRegions, t,
                         internal_format, 1);
        for (int i = 0; i < bitmaps.size(); i++) {
            env->ReleaseByteArrayElements(bitmaps[i], (jbyte *)texData[i]- getDataOffset(i), 0);
            env->DeleteLocalRef(bitmaps[i]);

        }

    }

}
