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

#include "vulkan_headers.h"
#include "objects/textures/bitmap_image.h"
#include "vulkan/vulkan_image.h"
namespace gvr {
    class VkBitmapImage : public vkImageBase, public BitmapImage
    {
    public:
        explicit VkBitmapImage(int format);
        virtual ~VkBitmapImage() {}
        virtual int getId() { return 1; }
        int updateFromBitmap(JNIEnv *env, VkImageViewType target, jobject bitmap);
        virtual void texParamsChanged(const TextureParameters&) { }
        virtual bool isReady()
        {
            return checkForUpdate(true);
        }

    protected:
        virtual void update(int texid);
        void updateFromMemory(int texid);
        void updateFromBitmap(int texid);
        void loadCompressedMipMaps(jbyte *data, int format);

    private:
        void updateFromBuffer(JNIEnv *env, VkImageViewType target, jobject bitmap);

    };

}

#endif //FRAMEWORK_VKBITMAPIMAGE_H
