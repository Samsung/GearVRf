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

#ifndef CUBEMAP_TEXTURE_H_
#define CUBEMAP_TEXTURE_H_

#include <string>

#include <android/bitmap.h>

#include "objects/textures/texture.h"
#include "util/gvr_log.h"

namespace gvr {
class CubemapTexture: public Texture {
public:
    explicit CubemapTexture(JNIEnv* env, jobjectArray bitmapArray,
            int* texture_parameters) :
            Texture(new GLTexture(TARGET, texture_parameters)) {
        glBindTexture(TARGET, gl_texture_->id());
        for (int i = 0; i < 6; i++) {
            jobject bitmap = env->GetObjectArrayElement(bitmapArray, i);

            AndroidBitmapInfo info;
            void *pixels;
            int ret;

            if (bitmap == NULL) {
                std::string error =
                        "new BaseTexture() failed! Input bitmap is NULL.";
                throw error;
            }
            if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
                std::string error = "AndroidBitmap_getInfo () failed! error = "
                        + ret;
                throw error;
            }
            if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
                std::string error =
                        "AndroidBitmap_lockPixels () failed! error = " + ret;
                throw error;
            }

            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA,
                    info.width, info.height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                    pixels);

            AndroidBitmap_unlockPixels(env, bitmap);
        }
    }

    explicit CubemapTexture() :
            Texture(new GLTexture(TARGET)) {
    }

    GLenum getTarget() const {
        return TARGET;
    }

private:
    CubemapTexture(const CubemapTexture& base_texture);
    CubemapTexture(CubemapTexture&& base_texture);
    CubemapTexture& operator=(const CubemapTexture& base_texture);
    CubemapTexture& operator=(CubemapTexture&& base_texture);

private:
    static const GLenum TARGET = GL_TEXTURE_CUBE_MAP;
};

}
#endif
