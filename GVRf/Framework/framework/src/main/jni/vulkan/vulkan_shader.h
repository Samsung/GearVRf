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

#ifndef VULKAN_SHADER_H_
#define VULKAN_SHADER_H_
#include <sstream>
#include <iostream>
#include "shaders/shader.h"
#include "vulkan_headers.h"


namespace gvr {
class Mesh;

/**
 * Contains information about the vertex attributes, textures and
 * uniforms used by the shader source and the sources for the
 * vertex and fragment shaders.
 *
 * Shaders are only created by the ShaderManager when addShader is called.
 */
class VulkanShader : public Shader
{
public:

/*
 * Creates a native shader description.
 * The actual shader program is not made until the first call to render()
 * @param id                ShaderManager ID for the shader
 * @param signature         Unique signature string
 * @param uniformDescriptor String giving the names and types of shader material uniforms
 *                          This does NOT include uniforms used by light sources
 * @param textureDescriptor String giving the names and types of texture samplers
 * @param vertexDescriptor  String giving the names and types of vertex attributes
 * @param vertexShader      String with GLSL source for vertex shader
 * @param fragmentShader    String with GLSL source for fragment shader
 * @see ShaderManager::addShader
 */
    explicit VulkanShader(int id, const char* signature,
            const char* uniformDescriptor,
            const char* textureDescriptor,
            const char* vertexDescriptor,
            const char* vertexShader,
            const char* fragmentShader);

    virtual ~VulkanShader();

    virtual bool useShader(bool) { return true; }

    std::vector<uint32_t>& getVkVertexShader()
    {
        if(!compiledVS.size())
        {
            compiledVS = CompileVulkanShader("VertexShader", VERTEX_SHADER, mVertexShader);
        }

        return compiledVS;
    }

    std::vector<uint32_t>& getVkFragmentShader()
    {
        if(!compiledFS.size())
        {
            compiledFS = CompileVulkanShader("FragmentShader", FRAGMENT_SHADER, mFragmentShader);
        }

        return compiledFS;
    }
    VkPipelineLayout &getPipelineLayout()
    {
        return m_pipelineLayout;
    }

    VkDescriptorSetLayout &getDescriptorLayout()
    {
        return m_descriptorLayout;
    }
    void  convertToVkShaders();
    virtual void bindLights(LightList&, Renderer* r) { }

    int makeLayout(VulkanMaterial& vkMtl, std::vector<VkDescriptorSetLayoutBinding>& samplerBinding, int index, VulkanRenderData* vkdata, LightList& lights);
    bool bindTextures(VulkanMaterial* material, std::vector<VkWriteDescriptorSet>& writes, VkDescriptorSet& descriptorSet);
    static std::string makeLayout(const DataDescriptor& desc, const char* blockName, bool useGPUBuffer);
private:
    VkPipelineLayout m_pipelineLayout;
    VkDescriptorSetLayout m_descriptorLayout;

    VulkanShader(const VulkanShader& shader) = delete;
    VulkanShader(VulkanShader&& shader) = delete;
    VulkanShader& operator=(const VulkanShader& shader) = delete;
    VulkanShader& operator=(VulkanShader&& shader) = delete;
    std::vector<uint32_t> compiledVS;
    std::vector<uint32_t> compiledFS;

protected:
    virtual void initialize();
    std::vector<uint32_t> CompileVulkanShader(const std::string& shaderName, ShaderType shaderTypeID, std::string& shaderContents);
};

}
#endif
