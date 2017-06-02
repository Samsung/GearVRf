//
// Created by roshan on 4/4/17.
//
#include <string>


#include "vulkan/vulkan_material.h"
#include "../shaders/shader.h"
#include "../objects/shader_data.h"
#include "vulkan_material.h"

namespace gvr {

class VkMaterialShaderVisitor : public Shader::ShaderVisitor {
public:
    bool TexIndex;
    ShaderData *Material;

    VkMaterialShaderVisitor(Shader *shader, ShaderData *mtl) : Shader::ShaderVisitor(shader),
                                                               TexIndex(0), Material(mtl) {};

    virtual void visit(const std::string &key, const std::string &type, int size);
};
void VkMaterialShaderVisitor::visit(const std::string& key, const std::string& type, int size)
{


  /*  GLShader* glshader = (GLShader*) shader_;
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
    */
}
int VulkanMaterial::bindTextures(Shader *shader)
{
    VkMaterialShaderVisitor visitor(shader, this);
    shader->forEach(shader->getTextureDescriptor(), visitor);
    return visitor.TexIndex;
}
}