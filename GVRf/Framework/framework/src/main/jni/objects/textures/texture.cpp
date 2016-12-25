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

namespace gvr {

Texture::Texture() : HybridObject(),
        mImage(NULL),
        mJava(NULL),
        mJavaImage(NULL),
        mTarget(0),
        mPendingTask(TASK_NONE),
        mIsReady(false)
{
    mTexParams[0] = GL_LINEAR;
    mTexParams[1] = GL_LINEAR;
    mTexParams[2] = 1.0f;
    mTexParams[3] = GL_CLAMP_TO_EDGE;
    mTexParams[4] = GL_CLAMP_TO_EDGE;
    mPendingTask = TASK_INIT_PARAMS;
}

Texture::Texture(int target) : HybridObject(),
        mImage(NULL),
        mJava(NULL),
        mTarget(target),
        mJavaImage(NULL),
        mPendingTask(TASK_NONE),
        mIsReady(false)
{
    mTexParams[0] = GL_LINEAR;
    mTexParams[1] = GL_LINEAR;
    mTexParams[2] = 1.0f;
    mTexParams[3] = GL_CLAMP_TO_EDGE;
    mTexParams[4] = GL_CLAMP_TO_EDGE;
    mPendingTask = TASK_INIT_PARAMS;
}

Texture::~Texture()
{
    if (mJava)
    {
        clearData(getCurrentEnv(mJava));
    }
}

void Texture::clearData(JNIEnv* env)
{
    if (mJavaImage)
    {
        jobject ref = env->NewLocalRef(mJavaImage);
        if (ref)
        {
            env->DeleteWeakGlobalRef(mJavaImage);
            env->DeleteLocalRef(ref);
        }
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
    if (image)
    {
        mTarget = image->getType();
    }
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
    if (image)
    {
        mTarget = image->getType();
    }
}

void Texture::updateTextureParameters(const int* texture_parameters, int n)
{
    if (texture_parameters)
    {
        memcpy(mTexParams, texture_parameters, sizeof(int) * 5);
        mPendingTask = TASK_INIT_PARAMS;
    }
}

}

