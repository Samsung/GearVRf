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

#include <engine/renderer/gl_renderer.h>
#include "objects/scene.h"
#include "gl/gl_render_data.h"
#include "objects/mesh.h"

namespace gvr
{

    void GLRenderData::render(Shader* shader, Renderer* renderer)
    {
        GLShader*   glshader = reinterpret_cast<GLShader*>(shader);
        int         programId = glshader->getProgramId();
        int         a_bone_indices;
        int         a_bone_weights;


        glBindVertexArray(mesh_->getVAOId(programId));
        a_bone_indices = glGetAttribLocation(programId, "a_bone_indices");
        a_bone_weights = glGetAttribLocation(programId, "a_bone_weights");
        if ((a_bone_indices >= 0) && (a_bone_weights >= 0) && bones_ubo_)
        {
            GLUniformBlock *bones_ubo = reinterpret_cast<GLUniformBlock *>(bones_ubo_);
            mesh_->setBoneLoc(a_bone_indices, a_bone_weights);
            mesh_->generateBoneArrayBuffers(programId);
            bones_ubo->updateGPU(renderer);
            bones_ubo->bindBuffer(shader);
        }
        if (Shader::LOG_SHADER) LOGV("SHADER: binding vertex arrays to program %d %p", programId, this);
        if (mesh_->indices().size() > 0)
        {
            glDrawElements(draw_mode(), mesh_->indices().size(), GL_UNSIGNED_SHORT, 0);

        }
        else
        {
            glDrawArrays(draw_mode(), 0, mesh_->vertices().size());
        }
        checkGLError("GLRenderData::render");
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}
