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

#ifndef GL_EXTERNAL_RENDER_TEXTURE_H_
#define GL_EXTERNAL_RENDER_TEXTURE_H_

#include "objects/textures/external_renderer_texture.h"
#include "gl/gl_texture.h"

namespace gvr {

class GLExternalRendererTexture: public GLTexture, public ExternalRendererTexture
{
public:
    GLExternalRendererTexture() : GLTexture(GL_TEXTURE_EXTERNAL_OES)
    {
        mTarget = GL_TEXTURE_EXTERNAL_OES;
    }

private:
    GLExternalRendererTexture(const GLExternalRendererTexture&e) = delete;
    GLExternalRendererTexture(GLExternalRendererTexture&&) = delete;
    GLExternalRendererTexture& operator=(const GLExternalRendererTexture&) = delete;
    GLExternalRendererTexture& operator=(GLExternalRendererTexture&&) = delete;
};

}
#endif
