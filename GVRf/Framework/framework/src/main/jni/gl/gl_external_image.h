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

#ifndef GL_EXTERNAL_IMAGE_H_
#define GL_EXTERNAL_IMAGE_H_

#include "../objects/textures/external_image.h"
#include "gl_image.h"
#include "gl_headers.h"

namespace gvr {

class GLExternalImage: public GLImage, public ExternalImage
{
public:
    GLExternalImage() : GLImage(GL_TEXTURE_EXTERNAL_OES)
    { }
    virtual ~GLExternalImage() {}
    virtual int getId() { return mId; }
    virtual bool isReady()
    {
        return updateGPU();
    }
    virtual void texParamsChanged(const TextureParameters& texparams)
    {
        mTexParams = texparams;
        mTexParamsDirty = true;
    }
    void updateTexParams() {
        mTexParams.setMinFilter(GL_LINEAR);
        GLImage::updateTexParams(mTexParams);
    }
private:
    GLExternalImage(const GLExternalImage&e) = delete;
    GLExternalImage(GLExternalImage&&) = delete;
    GLExternalImage& operator=(const GLExternalImage&) = delete;
    GLExternalImage& operator=(GLExternalImage&&) = delete;
};

}
#endif
