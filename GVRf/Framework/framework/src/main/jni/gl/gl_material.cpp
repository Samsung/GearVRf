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

namespace gvr
{

    class MaterialShaderVisitor : public Shader::ShaderVisitor
    {
    public:
        bool TexIndex;
        ShaderData* Material;

        MaterialShaderVisitor(Shader* shader, ShaderData* mtl) : Shader::ShaderVisitor(shader), TexIndex(0), Material(mtl) {  };
        virtual void visit(const std::string& key, const std::string& type, int size);
    };

    int GLMaterial::bindToShader(Shader *shader)
    {
        int texIndex = bindTextures(shader);
        if (texIndex >= 0)
        {
           // std::string s = uniforms_.toString();
            uniforms_.bindBuffer(shader);
        }
        return texIndex;
    }

    int GLMaterial::bindTextures(Shader *shader)
    {
        MaterialShaderVisitor visitor(shader, this);
        shader->forEach(shader->getTextureDescriptor(), visitor);
        return visitor.TexIndex;

    }

    void MaterialShaderVisitor::visit(const std::string& key, const std::string& type, int size)
    {
        GLShader* glshader = (GLShader*) shader_;
        int loc = glshader->getLocation(key);
        if (loc < 0)
        {
            loc = glGetUniformLocation(glshader->getProgramId(), key.c_str());
            if (loc < 0)
            {
                LOGW("texture is not present in shader");
                TexIndex = -1;
                return;
            }
        }
        Texture* tex = Material->getTexture(key);
        if (tex && tex->getImage())
        {
            GLImageTex* image = static_cast<GLImageTex*>(tex->getImage());

            glActiveTexture(GL_TEXTURE0 + TexIndex);
            glBindTexture(image->getTarget(), image->getId());
            glUniform1i(loc, TexIndex);
            checkGLError("Material::bindTextures");
            ++TexIndex;
        }
    }

}

