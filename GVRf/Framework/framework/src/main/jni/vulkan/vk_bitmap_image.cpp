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
 * Texture from a (Java-loaded) byte stream containing a compressed texture
 ***************************************************************************/

#include <android/bitmap.h>
#include "engine/renderer/renderer.h"
#include "engine/renderer/vulkan_renderer.h"
#include "vk_bitmap_image.h"
namespace gvr {
std::map<int, VkFormat> compressed_formats = {
        {0x93B0,                        VK_FORMAT_ASTC_4x4_UNORM_BLOCK},
        {0x93B1,                        VK_FORMAT_ASTC_5x4_UNORM_BLOCK},
        {0x93B2,                        VK_FORMAT_ASTC_5x5_UNORM_BLOCK},
        {0x93B3,                        VK_FORMAT_ASTC_6x5_UNORM_BLOCK},
        {0x93B4,                        VK_FORMAT_ASTC_6x6_UNORM_BLOCK},
        {0x93B5,                        VK_FORMAT_ASTC_8x5_UNORM_BLOCK},
        {0x93B6,                        VK_FORMAT_ASTC_8x6_UNORM_BLOCK},
        {0x93B7,                        VK_FORMAT_ASTC_8x8_UNORM_BLOCK},
        {0x93B8,                        VK_FORMAT_ASTC_10x5_UNORM_BLOCK},
        {0x93B9,                        VK_FORMAT_ASTC_10x6_UNORM_BLOCK},
        {0x93BA,                        VK_FORMAT_ASTC_10x8_UNORM_BLOCK},
        {0x93BB,                        VK_FORMAT_ASTC_10x10_UNORM_BLOCK},
        {0x93BC,                        VK_FORMAT_ASTC_12x10_UNORM_BLOCK},
        {0x93BD,                        VK_FORMAT_ASTC_12x12_UNORM_BLOCK},
        {GL_COMPRESSED_SIGNED_RG11_EAC, VK_FORMAT_EAC_R11G11_SNORM_BLOCK},
        {GL_COMPRESSED_RG11_EAC,        VK_FORMAT_EAC_R11G11_UNORM_BLOCK},
        {GL_COMPRESSED_SIGNED_R11_EAC,  VK_FORMAT_EAC_R11_SNORM_BLOCK},
        {GL_COMPRESSED_R11_EAC,         VK_FORMAT_EAC_R11_UNORM_BLOCK},
        {GL_COMPRESSED_RGB8_ETC2,       VK_FORMAT_ETC2_R8G8B8_UNORM_BLOCK},
        {GL_COMPRESSED_RGBA8_ETC2_EAC,  VK_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK},
};

    VkBitmapImage::VkBitmapImage(int format) :
            vkImageBase(VK_IMAGE_VIEW_TYPE_2D),
            BitmapImage(format)
    { }

    void VkBitmapImage::updateFromBuffer(JNIEnv *env, VkImageViewType target, jobject buffer)
    {
        void* pixels = env->GetDirectBufferAddress(buffer);
        // TODO: update bitmap image from pixels
        LOGE("VkBitmapImage::updateFromBuffer() not implemented yet");
    }

    int VkBitmapImage::updateFromBitmap(JNIEnv *env, VkImageViewType target, jobject bitmap) {
        AndroidBitmapInfo info;
        void *pixels;
        int ret;
        int imageFormat = 0;
        std::vector<void *> texData;
        size_t tex_size = 0;
        std::vector<VkBufferImageCopy> bufferCopyRegions;
        std::vector<jobject> bitmaps;
        std::vector<ImageInfo> imageInfos;
        if (bitmap == NULL) {
            LOGE("BitmapImage::updateFromBitmap bitmap is NULL");
        }
        else if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
            LOGE("BitmapImage::updateFromBitmap AndroidBitmap_getInfo() failed! error = %d", ret);
        }
        else if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
            LOGE("BitmapImage::updateFromBitmap AndroidBitmap_lockPixels() failed! error = %d",
                 ret);
            return 0;
        }
        else {
            VkFormat internalFormat = VK_FORMAT_R8G8B8A8_UNORM;
            switch (info.format) {
                case ANDROID_BITMAP_FORMAT_RGB_565:
                    internalFormat = VK_FORMAT_R5G6B5_UNORM_PACK16;
                    break;

                case ANDROID_BITMAP_FORMAT_RGBA_4444:
                    internalFormat = VK_FORMAT_R4G4B4A4_UNORM_PACK16;
                    break;

                case ANDROID_BITMAP_FORMAT_A_8:
                    internalFormat = VK_FORMAT_R8_UNORM;
                    break;
            }
            mLevels = static_cast<int>(floor(log2(std::max(info.width, info.height))) + 1);
            VkBufferImageCopy bufferCopyRegion = {};
            ImageInfo imageInfo = {};
            bufferCopyRegion.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            bufferCopyRegion.imageSubresource.mipLevel = 0;
            bufferCopyRegion.imageSubresource.baseArrayLayer = 0;
            bufferCopyRegion.imageSubresource.layerCount = 1;
            bufferCopyRegion.imageExtent.width = static_cast<uint32_t>(info.width);
            bufferCopyRegion.imageExtent.height = static_cast<uint32_t>(info.height);
            bufferCopyRegion.imageExtent.depth = 1;
            bufferCopyRegion.bufferOffset = 0;
            bufferCopyRegions.push_back(bufferCopyRegion);
            imageInfo.width = bufferCopyRegion.imageExtent.width;
            imageInfo.height = bufferCopyRegion.imageExtent.height;
            imageInfo.size = info.height * info.stride;
            imageInfo.isCompressed = false;
            imageInfo.mipLevel = 0;
            tex_size = info.height * info.stride;
            texData.push_back(pixels);
            imageInfos.push_back(imageInfo);
            updateMipVkImage(tex_size, texData, imageInfos, bufferCopyRegions, target,
                             internalFormat, mLevels);


            AndroidBitmap_unlockPixels(env, bitmap);
            return internalFormat;
        }
        return imageFormat;
    }

    void VkBitmapImage::update(int texid) {
        if (mJava == NULL) {
            return;
        }
        if (mBitmap != NULL) {
            updateFromBitmap(texid);
        }
        else if (mData != NULL) {
            updateFromMemory(texid);
        }
        else {
            return;
        }
        clearData(getCurrentEnv(mJava));
        updateComplete();
    }

    void VkBitmapImage::updateFromMemory(int texid) {
        JNIEnv *env = getCurrentEnv(mJava);
        if (mData == NULL) {
            LOGE("BitmapImage::updateFromMemory array is null");
            return;
        }
        mLevels = 0;
        jbyte *pixels = env->GetByteArrayElements(mData, 0);
        std::vector<void *> texData;
        std::vector<VkBufferImageCopy> bufferCopyRegions;
        std::vector<ImageInfo> imageInfos;
        VkFormat internal_format;

            VkImageViewType target = getImageType();
            ImageInfo imageInfo = {};
            VkBufferImageCopy bufferCopyRegion = {};
            bufferCopyRegion.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            bufferCopyRegion.imageSubresource.mipLevel = 0;
            bufferCopyRegion.imageSubresource.baseArrayLayer = 0;
            bufferCopyRegion.imageSubresource.layerCount = 1;
            bufferCopyRegion.imageExtent.width = static_cast<uint32_t>(mWidth);
            bufferCopyRegion.imageExtent.height = static_cast<uint32_t>(mHeight);
            bufferCopyRegion.imageExtent.depth = 1;
            bufferCopyRegion.bufferOffset = 0;
            imageInfo.width = mWidth;
            imageInfo.height = mHeight;
            imageInfo.size = mImageSize;
            imageInfo.isCompressed = true;
            imageInfo.mipLevel = 0;
            imageInfos.push_back(imageInfo);
            bufferCopyRegions.push_back(bufferCopyRegion);
            texData.push_back(pixels + getDataOffset(0));

            if (mIsCompressed)
                internal_format = compressed_formats[mFormat];
            else
                internal_format = VK_FORMAT_R8_UNORM;

            updateMipVkImage(mImageSize, texData, imageInfos, bufferCopyRegions, target,
                             internal_format, 1);

        env->ReleaseByteArrayElements(mData, pixels, 0);
        clearData(env);
    }

    void VkBitmapImage::updateFromBitmap(int texid) {
        JNIEnv *env = getCurrentEnv(mJava);
        if (mBitmap == NULL) {
            LOGE("BitmapImage::updateFromBitmap bitmap is null");
            return;
        }
        if(mIsBuffer) {
            updateFromBuffer(env, getImageType(), mBitmap);
        } else {
            updateFromBitmap(env, getImageType(), mBitmap);
        }
    }

    void VkBitmapImage::loadCompressedMipMaps(jbyte *data, int format) {
        for (int level = 0; level < mLevels; ++level) {
            int levelOffset = getDataOffset(level);
            int levelSize = getDataOffset(level + 1) - levelOffset;
            int width = mWidth >> level;
            int height = mHeight >> level;
            if (width < 1) width = 1;
            if (height < 1) height = 1;
        }
    }
}
