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

GLTexture::~GLTexture()
{
    if (0 != id_ && deleter_)
    {
        deleter_->queueTexture(id_);
    }
}

bool GLTexture::isReady()
{
    int target = getTarget();
    if (!target)
    {
        return false;
    }
    updateGPU();
    bool didUpdate = false;
    if (mImage && id_)
    {
        if (mJava && mJavaImage)
        {
            JNIEnv* env = getCurrentEnv(mJava);
            jobject imageRef =  env->NewLocalRef(mJavaImage);
            didUpdate = mImage->checkForUpdate(id_);
            env->DeleteLocalRef(imageRef);
        }
        else
        {
            didUpdate = mImage->checkForUpdate(id_);
        }
    }
    if (didUpdate)
    {
        setReady(true);
        return true;
    }
    return Texture::isReady();
}


bool GLTexture::updateGPU()
{
    if (mPendingTask == Texture::TASK_INIT_PARAMS)
    {
        int target = getTarget();
        deleter_ = getDeleterForThisThread();

        // Sets the new MIN FILTER
        GLenum min_filter_type_ = mTexParams[0];

        // Sets the MAG FILTER
        GLenum mag_filter_type_ = mTexParams[1];

        // Sets the wrap parameter for texture coordinate S
        GLenum wrap_s_type_ = mTexParams[3];

        // Sets the wrap parameter for texture coordinate S
        GLenum wrap_t_type_ = mTexParams[4];

        glGenTextures(1, &id_);
        glBindTexture(target, id_);

        // Sets the anisotropic filtering if the value provided is greater than 1 because 1 is the default value
        if (mTexParams[2] > 1.0f)
        {
            glTexParameterf(target, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) mTexParams[2]);
        }
        glTexParameteri(target, GL_TEXTURE_WRAP_S, wrap_s_type_);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, wrap_t_type_);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, min_filter_type_);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, mag_filter_type_);
    } // switch
    mPendingTask = 0;
    return true;
}

}
