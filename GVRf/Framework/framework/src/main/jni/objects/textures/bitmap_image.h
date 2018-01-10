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

#ifndef BITMAP_IMAGE_H
#define BITMAP_IMAGE_H

#include "image.h"
#include "util/gvr_jni.h"
#include "util/gvr_log.h"
#include "util/jni_utils.h"

namespace gvr {
    /*
     * Represents an image described by a 2D array of pixels.
     * The BitmapImage is uncompressed raw pixel data.
     */
    class BitmapImage : public Image
    {
    public:
        explicit BitmapImage(int format);
        virtual ~BitmapImage();
        void update(JNIEnv* env, int width, int height, jbyteArray data);
        void update(JNIEnv* env, jobject bitmap, bool hasAlpha);
        void update(JNIEnv* env, int xoffset, int yoffset, int width, int height,
                    int format, int type, jobject bitmap);
        void update(JNIEnv *env, int width, int height, int imageSize,
                    jbyteArray bytes, int levels, const int* dataOffsets);

        void set_transparency(bool hasTransparency) {
            mHasTransparency = hasTransparency;
        }

        virtual bool transparency() {
            return mHasTransparency;
        }

    protected:
        void clearData(JNIEnv* env);

    private:
        BitmapImage(const BitmapImage& texture) = delete;
        BitmapImage(BitmapImage&& texture) = delete;
        BitmapImage& operator=(const BitmapImage& texture) = delete;
        BitmapImage& operator=(BitmapImage&& texture) = delete;
        bool hasAlpha(int format);

    protected:
        JavaVM* mJava;
        jbyteArray mData;
        jobject mBitmap;
        bool mIsBuffer;
        bool mHasTransparency;
        jbyte* mPixels;
    };

}
#endif
