//
// Created by roshan on 12/28/16.
//

#ifndef FRAMEWORK_VKBITMAPIMAGE_H
#define FRAMEWORK_VKBITMAPIMAGE_H


#include "objects/textures/bitmap_image.h"
#include "vulkan/vulkan.h"

namespace gvr {
    class VkBitmapImage : public BitmapImage
    {
    public:
      //  static VkImageViewType MapFormat[];

        explicit VkBitmapImage (int format) :
                BitmapImage(format)
        {
            mTarget = static_cast<int>(VK_IMAGE_VIEW_TYPE_2D);
        }

        VkImageView& getVkImageView(){
            return imageView;
        }
        int updateFromBitmap(JNIEnv *env, VkImageViewType target, jobject bitmap);
    private:
        int updateVkImage(VkImageViewType target, VkFormat internal_format, int width, int height, void*);
    protected:
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
