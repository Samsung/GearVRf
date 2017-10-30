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
 * Textures.
 ***************************************************************************/


#include "texture.h"
#include "util/jni_utils.h"
#include "gl/gl_cubemap_image.h"
#include "gl/gl_bitmap_image.h"

namespace gvr {
class VkBitmapImage;
    class VkCubemapImage;

Texture::Texture(int type)
        : HybridObject(),
          mTexParamsDirty(false),
          mType(type),
          mImage(NULL),
          mJava(NULL),
          mJavaImage(NULL)
{ }

Texture::~Texture()
{
    if (mJava)
    {
        clearData(getCurrentEnv(mJava));
    }
}

bool Texture::isReady()
{
    if (mImage)
    {
        return mImage->isReady() && (mImage->getId() != 0);
    }
    return false;
}

void Texture::clearData(JNIEnv* env)
{
    if (mJavaImage)
    {
        env->DeleteWeakGlobalRef(mJavaImage);
        mJavaImage = NULL;
    }
    mImage = NULL;
}

void Texture::setImage(Image* image)
{
    if (mJava)
    {
        clearData(getCurrentEnv(mJava));
    }
    mImage = image;
}

void Texture::setImage(JNIEnv* env, jobject javaImage, Image* image)
{
    if (JNI_OK != env->GetJavaVM(&mJava))
    {
        FAIL("GetJavaVM failed");
        return;
    }
    clearData(env);
    mJavaImage = env->NewWeakGlobalRef(javaImage);
    mImage = image;
    if (mImage)
    {
        mImage->texParamsChanged(getTexParams());
    }
    LOGV("Texture::setImage");
}

void Texture::updateTextureParameters(const int* texture_parameters, int n)
{
    if (texture_parameters)
    {
        mTexParams = texture_parameters;
        if (mImage)
        {
            mImage->texParamsChanged(getTexParams());
        }
        mTexParamsDirty = true;
    }
}

}

