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

#include "engine/renderer/gl_renderer.h"
#include "gl/gl_render_data.h"
#include "objects/scene_object.h"
#include "objects/components/skeleton.h"
#include "objects/components/skin.h"

namespace gvr
{
    void GLRenderData::render(Shader* shader, Renderer* renderer)
    {
        GLShader*   glshader = static_cast<GLShader*>(shader);
        int         programId = glshader->getProgramId();
        int         indexCount = mesh_->getIndexCount();
        int         vertexCount = mesh_->getVertexCount();
        int         mode = draw_mode();

#ifdef DEBUG_SHADER
        LOGV("SHADER: RenderData::render binding vertex arrays to program %d %p %d vertices, %d indices",
                                     programId, this, vertexCount, indexCount);
#endif
        if (shader->hasBones())
        {
            Skin* skin = (Skin*) owner_object()->getComponent(Skin::getComponentType());

            if (skin)
            {
                skin->bindBuffer(renderer, shader);
            }
        }
        mesh_->getVertexBuffer()->bindToShader(shader, mesh_->getIndexBuffer());
        checkGLError("renderMesh::mesh_->getVertexBuffer()->bindToShader(");
        switch (mesh_->getIndexSize())
        {
            case 2:
            glDrawElements(mode, indexCount, GL_UNSIGNED_SHORT, 0);
            break;

            case 4:
            glDrawElements(mode, indexCount, GL_UNSIGNED_INT, 0);
            break;

            default:
            glDrawArrays(mode, 0, vertexCount);
            break;
        }
        checkGLError(" RenderData::render after draw");
        glBindVertexArray(0);
    }

}
