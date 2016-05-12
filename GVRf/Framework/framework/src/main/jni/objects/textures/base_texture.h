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

#include "objects/textures/texture.h"
#include "util/gvr_log.h"
#include "util/jni_utils.h"

namespace gvr {

static const char* kTextureClassName = "org/gearvrf/GVRTexture";
static const char* kIdAvailableMethodName = "idAvailable";
static const char* kIdAvailableMethodSignature = "(I)V";

class BaseTexture: public Texture {
public:

    explicit BaseTexture(int* texture_parameters) :
        Texture(new GLTexture(TARGET, texture_parameters)) {
    }

    void setJavaOwner(JNIEnv& env, jobject javaObject) {
        env.GetJavaVM(&javaVm_);

        gvrTextureClass_ = GetGlobalClassReference(env, kTextureClassName);
        javaMethodIdAvailable_ = GetMethodId(env, gvrTextureClass_, kIdAvailableMethodName,
                kIdAvailableMethodSignature);
        textureObjectWeak_ = env.NewWeakGlobalRef(javaObject);
    }

    virtual ~BaseTexture() {
        JNIEnv* env = getCurrentEnv(javaVm_);
        if (nullptr != gvrTextureClass_) {
            env->DeleteGlobalRef(gvrTextureClass_);
        }
        if (nullptr != textureObjectWeak_) {
            env->DeleteWeakGlobalRef(textureObjectWeak_);
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

        if (nullptr != textureObjectWeak_) {
            JNIEnv* env = getCurrentEnv(javaVm_);

            jobject textureObject = env->NewLocalRef(textureObjectWeak_);
            if (nullptr != textureObject) {
                env->CallVoidMethod(textureObject, javaMethodIdAvailable_, gl_texture_->id());
                env->DeleteLocalRef(textureObject);
            }

            env->DeleteWeakGlobalRef(textureObjectWeak_);
            textureObjectWeak_ = nullptr;
        }
    }

private:
    BaseTexture(const BaseTexture& base_texture);
    BaseTexture(BaseTexture&& base_texture);
    BaseTexture& operator=(const BaseTexture& base_texture);
    BaseTexture& operator=(BaseTexture&& base_texture);

private:
    static const GLenum TARGET = GL_TEXTURE_2D;

    JavaVM* javaVm_ = nullptr;

    jweak textureObjectWeak_ = nullptr;
    jclass gvrTextureClass_ = nullptr;
    jmethodID javaMethodIdAvailable_;
};

}
#endif
