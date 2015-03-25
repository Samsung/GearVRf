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
 * The PNG loader
 ***************************************************************************/
#ifndef PNG_LOADER_H_
#define PNG_LOADER_H_

#include "android/asset_manager_jni.h"
#include <png.h>

class PngLoader {
public:

    PngLoader() :
            gamma(0.0), png_ptr(0), info_ptr(0), end_info(0), pFileDescriptor(
                    NULL) {
    }

    void loadFromAsset(AAsset *file);

    enum ImageFormat {
        GrayFormat, RGBFormat, RGBAFormat
    };

    struct ImageData {
        unsigned char *bits;
        int width;
        int height;
        ImageFormat format;
    };
    ImageData pOutImage;

private:

    float gamma;

    png_struct *png_ptr;
    png_info *info_ptr;
    png_info *end_info;

    struct AllocatedMemoryPointers {
        AllocatedMemoryPointers() :
                row_pointers(0), accRow(0), inRow(0), outRow(0) {
        }
        void deallocate() {
            delete[] row_pointers;
            row_pointers = 0;
            delete[] accRow;
            accRow = 0;
            delete[] inRow;
            inRow = 0;
            delete[] outRow;
            outRow = 0;
        }

        png_byte **row_pointers;
        unsigned int *accRow;
        png_byte *inRow;
        unsigned char *outRow;
    };

    AllocatedMemoryPointers amp;

    AAsset * pFileDescriptor;

};

#endif
