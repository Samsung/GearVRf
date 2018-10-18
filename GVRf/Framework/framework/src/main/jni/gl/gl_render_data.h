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
 * Containing data about how to render an object.
 ***************************************************************************/

#ifndef GL_RENDER_DATA_H_
#define GL_RENDER_DATA_H_

#include "objects/components/render_data.h"
#include "gl/gl_uniform_block.h"
#include "gl/gl_shader.h"

/**
 * OpenGL implementation of RenderData.
 * Specializes handling of bone matrices.
 */
namespace gvr
{
    class GLRenderData : public RenderData
    {
    public:

        GLRenderData() : RenderData() { }

        GLRenderData(const RenderData &rdata) : RenderData(rdata)
        {
        }

        virtual ~GLRenderData() {}

        virtual void bindToShader(Shader* shader, Renderer* renderer);
        virtual void render(Shader*, Renderer*);

    private:
        GLRenderData(GLRenderData &&render_data) = delete;
        GLRenderData &operator=(const GLRenderData &render_data) = delete;
        GLRenderData &operator=(GLRenderData &&render_data) = delete;
    };
}
#endif
