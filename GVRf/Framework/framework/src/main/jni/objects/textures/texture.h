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

#ifndef TEXTURE_H_
#define TEXTURE_H_

#include <memory>
#include "image.h"
#define MAX_TEXTURE_PARAM_NUM 10

namespace gvr {

class Texture : public HybridObject
{
public:
    //
    // Enum for pending update tasks.
    //
    enum
    {
        TASK_NONE = 0,
        TASK_INIT_PARAMS = 1,
    };

    Texture() : HybridObject(),
                mImage(NULL),
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

    virtual ~Texture() { }

    virtual void setImage(Image* image)
    {
        mImage = image;
        mTarget = image->getType();
    }

    void updateTextureParameters(const int* texture_parameters, int n)
    {
        memcpy(mTexParams, texture_parameters, sizeof(int) * 5);
        mPendingTask = TASK_INIT_PARAMS;
    }

    virtual int getTarget() const
    {
        return mTarget;
    }

    virtual bool isReady()
    {
        return mIsReady;
    }

    virtual int getId() = 0;

    void setReady(bool ready)
    {
        mIsReady = ready;
    }

protected:
    Image*  mImage;
    int     mTexParams[MAX_TEXTURE_PARAM_NUM];
    int     mTarget;
    int     mPendingTask;
    bool    mIsReady;

private:
    Texture(const Texture& texture);
    Texture(Texture&& texture);
    Texture& operator=(const Texture& texture);
    Texture& operator=(Texture&& texture);
};

}

#endif
