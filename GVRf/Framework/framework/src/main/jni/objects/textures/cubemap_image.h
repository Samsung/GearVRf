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

#ifndef CUBEMAP_IMAGE_H_
#define CUBEMAP_IMAGE_H_

#include <string>
#include <android/bitmap.h>

#include "image.h"
#include "util/scope_exit.h"
#include "util/jni_utils.h"

namespace gvr {
/*
 * Represents an cubemap described by six separate 2D pixel arrays.
 * The image data for each cube face be either raw pixel data or
 * each face may be compressed.
 */
    class CubemapImage: public Image
    {
    public:
        explicit CubemapImage(int format);
        virtual ~CubemapImage();

        void update(JNIEnv* env, jobjectArray bitmapArray);
        void update(JNIEnv* env, int width, int height, int imageSize,
                    jobjectArray textureArray, const int* textureOffset);

    private:
        CubemapImage(const CubemapImage& base_texture) = delete;
        CubemapImage(CubemapImage&& base_texture) = delete;
        CubemapImage& operator=(const CubemapImage& base_texture) = delete;
        CubemapImage& operator=(CubemapImage&& base_texture) = delete;

    protected:
        void clearData(JNIEnv* env);
        void updateFromBitmap(int texid);
        void updateFromMemory(int texid);

    protected:
        JavaVM* mJava;
        jobject mBitmaps;
        jobject mTextures;
    };

}
#endif