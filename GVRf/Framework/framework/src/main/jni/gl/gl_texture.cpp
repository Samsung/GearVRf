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
 * RAII class for GL textures.
 ***************************************************************************/

#include "gl/gl_texture.h"
#include "util/jni_utils.h"

namespace gvr {
GLenum GLTexture::MapWrap[3] = { GL_CLAMP_TO_EDGE, GL_REPEAT, GL_MIRRORED_REPEAT };
GLenum GLTexture::MapFilter[6] = { GL_NEAREST, GL_LINEAR, GL_NEAREST_MIPMAP_NEAREST,
                        GL_NEAREST_MIPMAP_LINEAR, GL_LINEAR_MIPMAP_NEAREST, GL_LINEAR_MIPMAP_LINEAR };

GLTexture::~GLTexture()
{
    if (0 != id_ && deleter_)
    {
        deleter_->queueTexture(id_);
    }
}

bool GLTexture::isReady()
{
    if (mPendingTask)
    {
        if (getTarget() || (mImage != NULL))
        {
            if (updateGPU())
            {
                setReady(true);
            }
        }
    }
    return Texture::isReady();
}

bool GLTexture::updateGPU()
{
    if (mPendingTask & Texture::TASK_INIT_PARAMS)
    {
        updateTexParams();
        mPendingTask &= ~Texture::TASK_INIT_PARAMS;
    }
    if ((mPendingTask & Texture::TASK_UPDATE_IMAGE) && id_)
    {
        updateImage();
        mPendingTask &= ~Texture::TASK_UPDATE_IMAGE;
    }
    return (mPendingTask == 0);
}

bool GLTexture::updateImage()
{
    if (mJava && mJavaImage)
    {
        JNIEnv *env = getCurrentEnv(mJava);
        jobject imageRef = env->NewLocalRef(mJavaImage);
        bool didUpdate = mImage->checkForUpdate(id_);
        env->DeleteLocalRef(imageRef);
        return didUpdate;
    }
    else
    {
        return mImage->checkForUpdate(id_);
    }
}

void GLTexture::updateTexParams()
{
    int target = getTarget();
    deleter_ = getDeleterForThisThread();

    // Sets the new MIN FILTER
    GLenum min_filter_type_ = MapFilter[mTexParams.getMinFilter()];

    // Sets the MAG FILTER
    GLenum mag_filter_type_ = MapFilter[mTexParams.getMagFilter()];

    // Sets the wrap parameter for texture coordinate S
    GLenum wrap_s_type_ = MapWrap[mTexParams.getWrapU()];

    // Sets the wrap parameter for texture coordinate S
    GLenum wrap_t_type_ = MapWrap[mTexParams.getWrapV()];

    glGenTextures(1, &id_);
    glBindTexture(target, id_);

    // Sets the anisotropic filtering if the value provided is greater than 1 because 1 is the default value
    if (mTexParams.getMaxAnisotropy() > 1.0f)
    {
        glTexParameterf(target, GL_TEXTURE_MAX_ANISOTROPY_EXT, mTexParams.getMaxAnisotropy());
    }
    glTexParameteri(target, GL_TEXTURE_WRAP_S, wrap_s_type_);
    glTexParameteri(target, GL_TEXTURE_WRAP_T, wrap_t_type_);
    glTexParameteri(target, GL_TEXTURE_MIN_FILTER, min_filter_type_);
    glTexParameteri(target, GL_TEXTURE_MAG_FILTER, mag_filter_type_);
}

}
