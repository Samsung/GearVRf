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
 * A shader which an user can add in run-time.
 ***************************************************************************/
#define TEXTURE_BIND_START 4
#include "vulkan/vulkan_shader.h"
#include "vulkan/vulkan_material.h"
#include "engine/renderer/vulkan_renderer.h"
#include <shaderc/shaderc.h>
#include <shaderc/shaderc.hpp>
#include <glslang/Include/Common.h>
#include "vulkan/vulkan_render_data.h"
#include "vulkan/vk_render_to_texture.h"
namespace gvr {

VulkanShader::VulkanShader(int id,
               const char* signature,
               const char* uniformDescriptor,
               const char* textureDescriptor,
               const char* vertexDescriptor,
               const char* vertexShader,
               const char* fragmentShader)
    : Shader(id, signature, uniformDescriptor, textureDescriptor, vertexDescriptor, vertexShader, fragmentShader) { }

void VulkanShader::initialize()
{
}

int VulkanShader::makeLayout(VulkanMaterial& vkMtl, std::vector<VkDescriptorSetLayoutBinding>& samplerBinding, int index, VulkanRenderData* vkdata, LightList& lights)
{
    VkDescriptorSetLayoutBinding dummy_binding ={} ;
    if (usesMatrixUniforms()) {
        VkDescriptorSetLayoutBinding &transform_uniformBinding = vkdata->getTransformUbo().getVulkanDescriptor()->getLayoutBinding();
        samplerBinding.push_back(transform_uniformBinding);
    }
    else {
        dummy_binding.binding = TRANSFORM_UBO_INDEX;
        samplerBinding.push_back(dummy_binding);
    }

    if (getUniformDescriptor().getNumEntries()){
        VkDescriptorSetLayoutBinding &material_uniformBinding = reinterpret_cast<VulkanUniformBlock&>(vkMtl.uniforms()).getVulkanDescriptor()->getLayoutBinding();
        samplerBinding.push_back(material_uniformBinding);
    }
    else {
        // Dummy binding for Material UBO, since now a push Constant
        dummy_binding.binding = MATERIAL_UBO_INDEX;
        samplerBinding.push_back(dummy_binding);
    }

    if(vkdata->mesh()->hasBones() && hasBones()){
       VkDescriptorSetLayoutBinding &bones_uniformBinding = static_cast<VulkanUniformBlock*>(vkdata->getBonesUbo())->getVulkanDescriptor()->getLayoutBinding();
       samplerBinding.push_back(bones_uniformBinding);
   }
    else{
        dummy_binding.binding = BONES_UBO_INDEX;
        samplerBinding.push_back(dummy_binding);
    }
    // Right now, we dont' have support for shadow map, so add dummy binding for it

    if(lights.getUBO() != nullptr){
        VkDescriptorSetLayoutBinding &lights_uniformBinding = static_cast<VulkanUniformBlock*>(lights.getUBO())->getVulkanDescriptor()->getLayoutBinding();
        samplerBinding.push_back(lights_uniformBinding);
    }
    else {
        dummy_binding.binding = 3;
        samplerBinding.push_back(dummy_binding);
    }

    /*
     * TODO :: if has shadowmap, create binding for it
     */
    index = TEXTURE_BIND_START;
    vkMtl.forEachTexture([this, &samplerBinding](const char* texname, Texture* t) mutable
    {
        const DataDescriptor::DataEntry* entry = mTextureDesc.find(texname);
        if ((entry == NULL) || entry->NotUsed)
        {
            return;
        }
        VkDescriptorSetLayoutBinding layoutBinding;
        layoutBinding.binding = entry->Index + TEXTURE_BIND_START;
        layoutBinding.descriptorCount = 1;
        layoutBinding.descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        layoutBinding.stageFlags = VK_SHADER_STAGE_FRAGMENT_BIT;
        layoutBinding.pImmutableSamplers = nullptr;
        (samplerBinding).push_back(layoutBinding);
    });

    return index;
}
bool VulkanShader::bindTextures(VulkanMaterial* material, std::vector<VkWriteDescriptorSet>& writes, VkDescriptorSet& descriptorSet)
{
    bool success = true;
    material->forEachTexture([this, &writes, descriptorSet, &success](const char* texname, Texture* t) mutable
    {
        VkTexture *tex = static_cast<VkTexture *>(t);
        const DataDescriptor::DataEntry* e = mTextureDesc.find(texname);
        if ((e == NULL) || e->NotUsed) {
            return;
        }
        if(!t->isReady()) {
            success = false;
            return;
        }

        VkWriteDescriptorSet write;
        memset(&write, 0, sizeof(write));

        write.sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
        write.dstBinding = e->Index + TEXTURE_BIND_START;
        write.dstSet = descriptorSet;
        write.descriptorCount = 1;
        write.descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        if(!t->getImage())
            write.pImageInfo = &(static_cast<VkRenderTexture*>(t)->getDescriptorImage());
        else
            write.pImageInfo = &(tex->getDescriptorImage());
        writes.push_back(write);
    });

    return success;
}

VulkanShader::~VulkanShader() { }

    std::vector<uint32_t> VulkanShader::CompileVulkanShader(const std::string& shaderName, ShaderType shaderTypeID, std::string& shaderContents)
    {
        shaderc::Compiler compiler;
        shaderc::CompileOptions options;

        shaderc_shader_kind shaderType;

        switch (shaderTypeID)
        {
            case VERTEX_SHADER:
                shaderType = shaderc_glsl_default_vertex_shader;
                break;
            case FRAGMENT_SHADER:
                shaderType = shaderc_glsl_default_fragment_shader;
                break;
        }

        shaderc::SpvCompilationResult module = compiler.CompileGlslToSpv(shaderContents.c_str(),
                                                                         shaderContents.size(),
                                                                         shaderType,
                                                                         shaderName.c_str(),
                                                                         options);

        if (module.GetCompilationStatus() != shaderc_compilation_status_success)
        {
            LOGE("Vulkan shader unable to compile : %s", module.GetErrorMessage().c_str());
        }

        std::vector<uint32_t> result(module.cbegin(), module.cend());
        return result;
    }

    std::string VulkanShader::makeLayout(const DataDescriptor& desc, const char* blockName, bool useGPUBuffer)
    {

        std::ostringstream stream;
        int bindingIndex =0;

        if(strcasestr(blockName,"material"))
            bindingIndex = MATERIAL_UBO_INDEX;
        else if(strcasestr(blockName,"Transform"))
            bindingIndex = TRANSFORM_UBO_INDEX;
        else if(strcasestr(blockName,"Bones"))
            bindingIndex = BONES_UBO_INDEX;

        if (useGPUBuffer)
        {
            stream << "layout (std140, set = 0, binding = " << bindingIndex <<" ) uniform " << blockName << " {" << std::endl;
        }
        else
        {
            stream << "layout (std140, push_constant) uniform PushConstants {" << std::endl;
        }

        if (useGPUBuffer)
        {
            desc.forEachEntry([&stream](const DataDescriptor::DataEntry& entry) mutable
                              {
                                  int nelems = entry.Count;
                                  stream << "   " << entry.Type << " " << entry.Name;
                                  if (nelems > 1)
                                  {
                                      stream << "[" << nelems << "]";
                                  }
                                  stream << ";" << std::endl;
                              });
            stream << "};" << std::endl;
        }
        else
        {
            desc.forEachEntry([&stream](const DataDescriptor::DataEntry& entry) mutable
                              {
                                  if (entry.IsSet)
                                  {
                                      int nelems = entry.Count;
                                      stream << "uniform " << entry.Type << " " << entry.Name;
                                      if (nelems > 1)
                                      {
                                          stream << "[" << nelems << "]";
                                      }
                                      stream << ";" << std::endl;
                                  }
                              });
        }
        return stream.str();
    }

} /* namespace gvr */
