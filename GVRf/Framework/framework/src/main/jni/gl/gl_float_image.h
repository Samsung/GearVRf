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
 * Textures generated from float point arrays
 ***************************************************************************/

#ifndef GL_FLOAT_IMAGE_H_
#define GL_FLOAT_IMAGE_H_

#include "objects/textures/float_image.h"
#include "gl_image.h"

namespace gvr {
    class GLFloatImage : public GLImage, public FloatImage
    {
    public:
        GLFloatImage(int pixelFormat = GL_RG) : FloatImage(pixelFormat), GLImage(GL_TEXTURE_2D)
        { }
        virtual ~GLFloatImage() {}
        virtual int getId() { return mId; }
        virtual bool isReady()
        {
            return updateGPU() && checkForUpdate(mId);
        }
        virtual void texParamsChanged(const TextureParameters& texparams)
        {
            if (mTexParams != texparams)
            {
                mTexParams = texparams;
                mTexParamsDirty = true;
            }
        }

        void updateTexParams()
        {
            int min_filter = mTexParams.getMinFilter();

            if (mIsCompressed &&
                (mLevels <= 1) &&
                (min_filter >= TextureParameters::NEAREST_MIPMAP_NEAREST))
            {
                mTexParams.setMinFilter(GL_LINEAR);
            }
            GLImage::updateTexParams(mTexParams);
        }
    protected:
        virtual void update(int texid)
        {
            JNIEnv* env = getCurrentEnv(mJava);
            jfloatArray array = static_cast<jfloatArray>(env->NewLocalRef(mData));
            float* pixels = env->GetFloatArrayElements(array, 0);
            int internalFormat = (mFormat == GL_RGB) ? GL_RGB32F : GL_RG32F;
            glBindTexture(mType, texid);
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, mWidth, mHeight, 0, mFormat, GL_FLOAT, pixels);
            env->ReleaseFloatArrayElements(array, pixels, 0);
            env->DeleteLocalRef(array);
            clearData(env);
        }

    private:
        GLFloatImage(const GLFloatImage&) = delete;
        GLFloatImage(GLFloatImage&&) = delete;
        GLFloatImage& operator=(const GLFloatImage&) = delete;
        GLFloatImage& operator=(GLFloatImage&&) = delete;
    };

}
#endif
