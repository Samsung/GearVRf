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

namespace gvr {
class GLFloatImage : public FloatImage
{
public:
    explicit GLFloatImage(JNIEnv* env, int width, int height, jfloatArray data) : FloatImage(env, width, height, data) { }
    explicit GLFloatImage() : FloatImage() { }

protected:
    virtual void update(int texid)
    {
        JNIEnv *env = getCurrentEnv(mJava);
        jfloatArray array = static_cast<jfloatArray>(env->NewLocalRef(mData));
        float* pixels = env->GetFloatArrayElements(array, 0);
        glBindTexture(mType, texid);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RG32F, mWidth, mHeight, 0, GL_RG, GL_FLOAT, pixels);
        glGenerateMipmap(mType);
        env->ReleaseFloatArrayElements(array, pixels, 0);
        env->DeleteLocalRef(array);
        clearData(env);
        mPendingUpdate = false;
    }

private:
    GLFloatImage(const GLFloatImage&);
    GLFloatImage(GLFloatImage&&);
    GLFloatImage& operator=(const GLFloatImage&);
    GLFloatImage& operator=(GLFloatImage&&);
};

}
#endif
