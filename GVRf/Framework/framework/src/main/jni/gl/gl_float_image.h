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

#ifndef FLOAT_IMAGE_H_
#define FLOAT_IMAGE_H_

#include "gl/gl_texture.h"
#include "util/gvr_log.h"

namespace gvr {
class GLFloatImage: public Image
{
public:
    explicit GLFloatImage() : Image(GL_TEXTURE_2D, GL_FLOAT), mData(NULL)
    {
    }

    bool update(int width, int height, float* data)
    {
        mWidth = width;
        mHeight = height;
        mData = data;
    }

    virtual void update(int texid)
    {
        glBindTexture(GL_TEXTURE_2D, texid);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RG32F, mWidth, mHeight, 0, GL_RG, GL_FLOAT, mData);
        mData = NULL;
        mPendingUpdate = false;
    }

private:
    GLFloatImage(const GLFloatImage&);
    GLFloatImage(GLFloatImage&&);
    GLFloatImage& operator=(const GLFloatImage&);
    GLFloatImage& operator=(GLFloatImage&&);

    float* mData;
};

}
#endif
