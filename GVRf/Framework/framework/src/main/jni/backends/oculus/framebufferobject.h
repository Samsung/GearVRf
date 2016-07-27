/*
/* Copyright 2016 Samsung Electronics Co., LTD
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

#ifndef _FRAMEBUFFEROBJECT_H_
#define _FRAMEBUFFEROBJECT_H_

#include <GLES3/gl3.h>
#include "VrApi_Types.h"

namespace gvr {

class FrameBufferObject {
public:

    void clear();
    bool create(const ovrTextureFormat colorFormat, const int width, const int height,
            const int multisamples, bool resolveDepth, const ovrTextureFormat depthFormat);
    void destroy();
    void bind();
    static void unbind();
    void resolve();
    void advance();

public:
    int mWidth = 0;
    int mHeight = 0;
    int mMultisamples = 0;
    int mTextureSwapChainLength = 0;
    int mDepthTextureSwapChainLength = 0;
    int mTextureSwapChainIndex = 0;
    ovrTextureSwapChain* mColorTextureSwapChain = nullptr;
    ovrTextureSwapChain* mDepthTextureSwapChain = nullptr;
    GLuint* mDepthBuffers = nullptr;
    GLuint mColorBuffer = 0;
    GLuint* mRenderFrameBuffers = nullptr;
    GLuint* mResolveFrameBuffers = nullptr;
};

} //namespace gvr

#endif /* _FRAMEBUFFEROBJECT_H_ */
