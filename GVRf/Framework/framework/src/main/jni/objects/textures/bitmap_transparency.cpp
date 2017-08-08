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
 * JNI
 ***************************************************************************/

#include "bitmap_transparency.h"

namespace gvr {

bool bitmap_has_transparency(JNIEnv *env, jobject jbitmap) {
    int result = -4;
    bool transparency = false;
	AndroidBitmapInfo info;
    void *addrPtr = NULL;

    result = AndroidBitmap_getInfo(env, jbitmap, &info);
    if(result != ANDROID_BITMAP_RESUT_SUCCESS) {
        LOGE("GVRBitmapTexture: unable to determine bitmap format in bitmap_transparency.cpp");
        return false;
    }

    result = AndroidBitmap_lockPixels(env, jbitmap, &addrPtr);
    if(result != ANDROID_BITMAP_RESUT_SUCCESS) {
        LOGE("GVRBitmapTexture: unable to lock bitmap in bitmap_transparency.cpp");
        return false;
    }

    uint32_t width = info.width;
    uint32_t height = info.height;
    uint32_t stride = info.stride;
    int32_t format = info.format;
    bool done = false;

    if(format == ANDROID_BITMAP_FORMAT_A_8) {
        const uint8_t *ptr = (uint8_t *)addrPtr;
        while(height > 0 && !done) {
            if(*ptr < 255) {
                transparency = true;
                done = true;
            }
            ptr += stride;
            height--;
        }
    } else if(format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        const uint32_t *ptr = (uint32_t *)addrPtr; 
        while(height > 0 && !done) {
            for(int x = 0; x < width && !done; x++) {
                uint8_t alpha = ptr[x] >> 24;
                if(alpha < 255) {
                    transparency = true;
                    done = true;
                }
            }
            ptr = (const uint32_t*)((const char*)ptr + stride);
            height--;
        }
    } else if(format == ANDROID_BITMAP_FORMAT_RGBA_4444) {
        const uint16_t *ptr = (uint16_t *)addrPtr; 
        while(height > 0 && !done) {
            for (int x = 0; x < width && !done; x++) {
                uint8_t alpha = ptr[x] & 0x7;
                if(alpha < 128) {
                    transparency = true;
                    done = true;
                }
            }
            ptr = (const uint16_t*)((const char*)ptr + stride);
            height--;
        }
    } 

    result = AndroidBitmap_unlockPixels(env, jbitmap);
    if(result != ANDROID_BITMAP_RESUT_SUCCESS) {
        LOGE("GVRBitmapTexture: unable to unlock bitmap in bitmap_transparency.cpp");
        return transparency;
    }

    return transparency;
}



}
