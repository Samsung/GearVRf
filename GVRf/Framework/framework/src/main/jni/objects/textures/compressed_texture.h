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

#ifndef compressed_texture_H_
#define compressed_texture_H_

//#include <GLES3/gl3.h>
#include "objects/textures/texture.h"
#include "util/gvr_jni.h"
#include "util/gvr_log.h"
#include "util/jni_utils.h"

namespace gvr {
class CompressedTexture: public Texture {
public:

    // The constructor to use when loading a mipmap chain, from Java
    explicit CompressedTexture(GLenum target) :
            Texture(new GLTexture(target)), target(target) {
        pending_gl_task_ = GL_TASK_INIT_PLAIN;
    }

    // The constructor to use when loading a single-level texture
    explicit CompressedTexture(JNIEnv* env, GLenum target, GLenum internalFormat,
            GLsizei width, GLsizei height, GLsizei imageSize, jbyteArray bytes,
            int dataOffset, int* texture_parameters) :
            Texture(new GLTexture(target, texture_parameters)), target(target) {
        pending_gl_task_ = GL_TASK_INIT_INTERNAL_FORMAT;
        if (JNI_OK != env->GetJavaVM(&javaVm_)) {
            FAIL("GetJavaVM failed");
        }

        internalFormat_ = internalFormat;
        width_ = width;
        height_ = height;
        imageSize_ = imageSize;
        bytesRef_ = static_cast<jbyteArray>(env->NewGlobalRef(bytes));
        dataOffset_ = dataOffset;
    }

    virtual ~CompressedTexture() {
        JNIEnv* env = getCurrentEnv(javaVm_);

        // Release global refs. Race condition does not occur because if
        // the runPendingGL is running, the object won't be destructed.
        switch (pending_gl_task_) {
        case GL_TASK_INIT_INTERNAL_FORMAT: {
            env->DeleteGlobalRef(bytesRef_);
            break;
        }

        default:
            break;
        }
    }

    GLenum getTarget() const {
        return target;
    }

    virtual void runPendingGL() {
        Texture::runPendingGL();

        switch (pending_gl_task_) {
        case GL_TASK_NONE:
            return;

        case GL_TASK_INIT_PLAIN:
            glBindTexture(target, gl_texture_->id());
            break;

        case GL_TASK_INIT_INTERNAL_FORMAT: {
            JNIEnv* env = getCurrentEnv(javaVm_);
            jbyte* data = env->GetByteArrayElements(bytesRef_, 0);

            glBindTexture(target, gl_texture_->id());
            glCompressedTexImage2D(target, 0, internalFormat_, width_, height_, 0,
                    imageSize_, data + dataOffset_);

            env->ReleaseByteArrayElements(bytesRef_, data, 0);
            env->DeleteGlobalRef(bytesRef_);
            break;
        }

        } // switch

        pending_gl_task_ = GL_TASK_NONE;
    }

private:
    CompressedTexture(const CompressedTexture& compressed_texture);
    CompressedTexture(CompressedTexture&& compressed_texture);
    CompressedTexture& operator=(const CompressedTexture& compressed_texture);
    CompressedTexture& operator=(CompressedTexture&& compressed_texture);

private:
    GLenum const target;

    // Enum for pending GL tasks. Keep a comma with each line
    // for easier merging.
    enum {
        GL_TASK_NONE = 0,
        GL_TASK_INIT_PLAIN,
        GL_TASK_INIT_INTERNAL_FORMAT,
    };
    int pending_gl_task_;

    JavaVM* javaVm_;

    // For GL_TASK_INIT_INTERNAL_FORMAT
    GLenum internalFormat_;
    GLsizei width_;
    GLsizei height_;
    GLsizei imageSize_;
    int dataOffset_;
    jbyteArray bytesRef_;
};

}
#endif
