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
 * Save front bufer image
 ***************************************************************************/

#ifndef GVR_CPP_IMAGE_CAPTURE_H_
#define GVR_CPP_IMAGE_CAPTURE_H_
#include "GLES3/gl3.h"
#include <vector>
#include <string>


/* Saves a buffer with RGBA values as a tga file */

int write_truecolor_tga( int width, int height, GLubyte* valRGBA, char* fileName );

// Reads back in RGBA format.
// Images are saved to /sdcard/image-xx.tga

class GVRImageCapture {
public:
    GVRImageCapture(uint width = 0, uint height = 0); // Default width and height for PBOs.
    ~GVRImageCapture();
    void captureImage(int startX, int startY, uint width, uint height, char* msg = NULL);
    void captureImage(int startX, int startY, char* msg = NULL);
    void saveAllImages();
private:
    struct PBOINFO
    {
        GLuint id;
        uint width;
        uint height;
        int startX;
        int startY;
        std::string msg;
    };

    uint mDefaultWidth;
    uint mDefaultHeight;
    std::vector<PBOINFO> mPBOData;
    uint mMaxWidth;
    uint mMaxHeight;


};

#endif // GVR_CPP_STACK_TRACE_H_
