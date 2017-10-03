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

#include "gl/gl_material.h"
#include "gl/gl_shader.h"
#include "gl/gl_imagetex.h"

namespace gvr
{
/**
 * Binds the textures and uniforms in this material to the
 * shader being used for rendering. All materials used by a
 * shader will have the same ordering of uniforms and textures
 * in their descriptors. This is the ordering used by the
 * GLShader::getUniformLoc and GLShader::getTextureLoc.
 * @param shader    GLShader being used by this material
 * @param renderer  GLRenderer used to render
 * @return last texture unit used, -1 if a texture was not ready
 * @see GLShader::getUniformLoc GLShader::getTextureLoc GLShader::findUniforms
 */
    int GLMaterial::bindToShader(Shader* shader, Renderer* renderer)
    {
        GLShader* glshader = static_cast<GLShader*>(shader);
        int index = -1;
        int texUnit = 0;
        bool fail = false;

       assert(uniforms().usesGPUBuffer() == shader->useMaterialGPUBuffer());

        forEachTexture([fail, &texUnit, glshader, this, &index](const char* texname, Texture* tex) mutable
        {
            int loc = glshader->getTextureLoc(++index);
            if (loc == -1)
            {
                return;
            }
            if (tex && tex->getImage())
            {
                GLImageTex* image = static_cast<GLImageTex*>(tex->getImage());
                int texid = image->getId();

                glActiveTexture(GL_TEXTURE0 + texUnit);
                glBindTexture(image->getTarget(), texid);
                glUniform1i(loc, texUnit++);
                checkGLError("GLMaterial::bindTexture");
            }
            else
            {
                LOGV("ShaderData::bindTexture texture %s at loc=%d not ready", texname, loc);
                fail = true;
            }
        });
        if (!fail)
        {
           // std::string s = uniforms_.toString();
            uniforms_.bindBuffer(shader, renderer);
            return texUnit;
        }
        return -1;
    }

}

