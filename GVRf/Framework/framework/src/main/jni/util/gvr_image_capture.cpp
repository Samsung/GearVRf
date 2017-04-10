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
#include "gvr_image_capture.h"

#include <stdio.h>
#include <cstring>
#include "util/gvr_log.h"

int write_truecolor_tga( uint width, uint height, GLubyte* val, char* fileName ) {
     FILE *fp = fopen(fileName, "wb");
     if (fp == NULL) return 0;


     // The image header
     char header[ 18 ] = { 0 }; // char = byte
     header[ 2 ] = 2; // truecolor
     header[ 12 ] = width & 0xFF;
     header[ 13 ] = (width >> 8) & 0xFF;
     header[ 14 ] = height & 0xFF;
     header[ 15 ] = (height >> 8) & 0xFF;
     header[ 16 ] = 24; // bits per pixel
     fwrite((const char*)&header, 1, sizeof(header), fp);

    // The image data is stored bottom-to-top, left-to-right
     for (int y = 0; y < height; y++)
     {
        for (int x = 0; x < width * 4; x += 4)
        {
            int pos = y * height * 4 + x;

            char r = val[pos];
            char g = val[pos + 1];
            char b = val[pos + 2];

            putc(r,fp);
            putc(g,fp);
            putc(b,fp);
        }
     }

    // The file footer
     static const char footer[ 26 ] =
     "\0\0\0\0" // no extension area
     "\0\0\0\0" // no developer directory
     "TRUEVISION-XFILE" // yep, this is a TGA file
     ".";
     fwrite((const char*)&footer, 1, sizeof(footer), fp);

    fclose(fp);
    return 1;
 }

GVRImageCapture::GVRImageCapture(uint width, uint height) :
        mDefaultWidth(width), mDefaultHeight(height),
        mMaxWidth(0), mMaxHeight(0)
{
}

GVRImageCapture::~GVRImageCapture()
{
    for (std::vector<PBOINFO>::iterator it = mPBOData.begin(); it != mPBOData.end(); ++it)
    {
        PBOINFO currPBO = *it;
        GLuint id = currPBO.id;
        glDeleteBuffers(1, &id);
    }
}

void GVRImageCapture::captureImage(int startX, int startY, uint width, uint height, char* msg)
{
    mMaxWidth = std::max(width, mMaxWidth);
    mMaxHeight = std::max(height, mMaxHeight);
    GLuint id;
    glGenBuffers(1, &id);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, id);
    glBufferData(GL_PIXEL_PACK_BUFFER, width * height * 4, 0, GL_STREAM_READ);
    PBOINFO pbo;
    pbo.id = id;
    pbo.startX = startX;
    pbo.startY = startY;
    pbo.width = width;
    pbo.height = height;
    if (msg)
    {
        pbo.msg = msg;
    }
    mPBOData.push_back(pbo);
    glReadPixels(startX, startY, width, height, GL_RGBA, GL_UNSIGNED_BYTE, 0);
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
}

void GVRImageCapture::captureImage(int startX, int startY, char* msg)
{
    captureImage(startX, startY, mDefaultWidth, mDefaultHeight, msg);
}

void GVRImageCapture::saveAllImages()
{
    GLubyte* data = new GLubyte[mMaxWidth * mMaxHeight * 4];
    int fileIndex = 0;
    for (std::vector<PBOINFO>::iterator it = mPBOData.begin(); it != mPBOData.end(); ++it)
    {
        const PBOINFO currPBO = *it;
        GLuint id = currPBO.id;

        char fileName[64];
        sprintf(fileName, "/sdcard/image-%d.tga", fileIndex); // Hardcoded path to save images.
        uint currWidth = currPBO.width;
        uint currHeight = currPBO.height;
        glBindBuffer(GL_PIXEL_PACK_BUFFER, id);
        int *buf = (int *)glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0, currWidth * currHeight  * 4,
                 GL_MAP_READ_BIT);
        std::memcpy(data, buf, currWidth * currHeight * 4);
        write_truecolor_tga(currWidth, currHeight, data, fileName);
        glDeleteBuffers(1, &id);
        if (currPBO.msg.length())
        {
            sprintf(fileName, "/sdcard/image-%d.txt", fileIndex); // Hardcoded path to save text data for images.
            FILE *fp = fopen(fileName, "w");
            if (fp != NULL)
            {
                fprintf(fp, "%s", currPBO.msg.c_str());
                fclose(fp);
            }
        }
        fileIndex++;
    }
    mPBOData.clear();
    delete data;
    mMaxWidth = mMaxHeight = 0;
}
