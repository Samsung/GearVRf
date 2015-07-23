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
 * Texture made by a bitmap.
 ***************************************************************************/

#ifndef BASE_TEXTURE_H_
#define BASE_TEXTURE_H_

#include <string>

#include <android/bitmap.h>

#include "objects/textures/texture.h"
#include "util/gvr_log.h"

namespace gvr {
class BaseTexture: public Texture {
public:
    explicit BaseTexture(JNIEnv* env, jobject bitmap) :
            Texture(new GLTexture(TARGET)) {
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
            std::string error = "AndroidBitmap_lockPixels () failed! error = "
                    + ret;
            throw error;
        }

        glBindTexture(GL_TEXTURE_2D, gl_texture_->id());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, info.width, info.height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glGenerateMipmap (GL_TEXTURE_2D);
        AndroidBitmap_unlockPixels(env, bitmap);
    }

    explicit BaseTexture(int width, int height, const unsigned char* pixels,
            int* texture_parameters) :
            Texture(new GLTexture(TARGET, texture_parameters)) {
        glBindTexture(GL_TEXTURE_2D, gl_texture_->id());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, pixels);
        glGenerateMipmap (GL_TEXTURE_2D);
    }

    explicit BaseTexture(int* texture_parameters) :
            Texture(new GLTexture(TARGET, texture_parameters)) {
    }

    bool update(int width, int height, void* data) {
        glBindTexture(GL_TEXTURE_2D, gl_texture_->id());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0,
                GL_LUMINANCE, GL_UNSIGNED_BYTE, data);
        glGenerateMipmap (GL_TEXTURE_2D);
        return (glGetError() == 0) ? 1 : 0;
    }

    GLenum getTarget() const {
        return TARGET;
    }

private:
    BaseTexture(const BaseTexture& base_texture);
    BaseTexture(BaseTexture&& base_texture);
    BaseTexture& operator=(const BaseTexture& base_texture);
    BaseTexture& operator=(BaseTexture&& base_texture);

private:
    static const GLenum TARGET = GL_TEXTURE_2D;
};

}
#endif
