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
        int ret;
        if (bitmap == NULL) {
            std::string error =
                    "new BaseTexture() failed! Input bitmap is NULL.";
            throw error;
        }

        if ((ret = AndroidBitmap_getInfo(env, bitmap, &info_)) < 0) {
            std::string error = "AndroidBitmap_getInfo () failed! error = "
                    + ret;
            throw error;
        }

        env_ = env;
        bitmapRef_ = env->NewGlobalRef(bitmap);

        pending_gl_task_ = GL_TASK_INIT_BITMAP;
    }

    explicit BaseTexture(int width, int height, const unsigned char* pixels,
            int* texture_parameters) :
            Texture(new GLTexture(TARGET, texture_parameters)),
            pixels_(pixels)
    {
        width_ = width;
        height_ = height;

        pending_gl_task_ = GL_TASK_INIT_PIXELS_PARAMS;
    }

    explicit BaseTexture(int* texture_parameters) :
            Texture(new GLTexture(TARGET, texture_parameters)), pending_gl_task_(GL_TASK_NONE) {
    }

    virtual ~BaseTexture() {
        switch (pending_gl_task_) {
        case GL_TASK_INIT_BITMAP:
            env_->DeleteGlobalRef(bitmapRef_);
            break;

        default:
            break;
        }
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

    virtual void runPendingGL() {
        Texture::runPendingGL();

        switch (pending_gl_task_) {
        case GL_TASK_NONE:
            return;

        case GL_TASK_INIT_BITMAP: {
            int ret;
            if ((ret = AndroidBitmap_lockPixels(env_, bitmapRef_, (void**)&pixels_)) < 0) {
                std::string error = "AndroidBitmap_lockPixels () failed! error = "
                        + ret;
                throw error;
            }

            glBindTexture(GL_TEXTURE_2D, gl_texture_->id());
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, info_.width, info_.height, 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, pixels_);
            glGenerateMipmap(GL_TEXTURE_2D);

            AndroidBitmap_unlockPixels(env_, bitmapRef_);
            env_->DeleteGlobalRef(bitmapRef_);
            break;
        }

        case GL_TASK_INIT_PIXELS_PARAMS: {
            glBindTexture(GL_TEXTURE_2D, gl_texture_->id());
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width_, height_, 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, pixels_);
            glGenerateMipmap (GL_TEXTURE_2D);
            break;
        }

        } // switch

        pending_gl_task_ = GL_TASK_NONE;
    }

private:
    BaseTexture(const BaseTexture& base_texture);
    BaseTexture(BaseTexture&& base_texture);
    BaseTexture& operator=(const BaseTexture& base_texture);
    BaseTexture& operator=(BaseTexture&& base_texture);

private:
    static const GLenum TARGET = GL_TEXTURE_2D;

    // Enum for pending GL tasks. Keep a comma with each line
    // for easier merging.
    enum {
        GL_TASK_NONE = 0,
        GL_TASK_INIT_BITMAP,
        GL_TASK_INIT_PIXELS_PARAMS,
        GL_TASK_INIT_PARAMS,
    };
    int pending_gl_task_;

    JNIEnv* env_;
    jobject bitmapRef_;
    AndroidBitmapInfo info_;
    const unsigned char* pixels_;

    int width_;
    int height_;
};

}
#endif
