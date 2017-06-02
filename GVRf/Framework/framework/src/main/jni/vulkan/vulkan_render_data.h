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

#ifndef VULKAN_RENDER_DATA_H_
#define VULKAN_RENDER_DATA_H_

#include "engine/renderer/vulkan_renderer.h"
#include "objects/components/render_data.h"
#include "vulkan_headers.h"
#include "vulkan_shader.h"

typedef unsigned long Long;
namespace gvr
{
    class VulkanData
    {

    public:
        VulkanData() : ubo(
        "mat4 u_view; mat4 u_mvp; mat4 u_mv; mat4 u_mv_it; mat4 u_model; mat4 u_view_i; float u_right;", TRANSFORM_UBO_INDEX, "Transform_ubo")
        {
        }

        VkPipeline &getVKPipeline()
        {
            return m_pipeline;
        }

        VulkanDescriptor &getDescriptor()
        {
            return vk_descriptor;
        }

        VkPipelineLayout &getPipelineLayout()
        {
            return m_pipelineLayout;
        }

        VkDescriptorSetLayout &getDescriptorLayout()
        {
            return m_descriptorLayout;
        }

        VkDescriptorPool &getDescriptorPool()
        {
            return m_descriptorPool;
        }

        VkDescriptorSet &getDescriptorSet()
        {
            return m_descriptorSet;
        }
        VulkanUniformBlock& getTransformUbo(){
            return ubo;
        }
        bool isDescriptorSetNull(){
            return descriptorSetNull;
        }
        void setDescriptorSetNull(bool flag){
            descriptorSetNull = flag;
        }
        void setPipelineLayout(VkPipelineLayout& pipelineLayout){
            m_pipelineLayout = pipelineLayout;
        }
        VkPipelineLayout m_pipelineLayout;
        // Vulkan
        GVR_Uniform m_modelViewMatrixUniform;

        VkPipeline m_pipeline;
        VkDescriptorSet m_descriptorSet;

    private:
        bool descriptorSetNull = false;
        VulkanUniformBlock ubo;
        VkDescriptorPool m_descriptorPool;
        VkDescriptorSetLayout m_descriptorLayout;
        VulkanDescriptor vk_descriptor;
    };

    /**
     * Vulkan implementation of RenderData.
     * Specializes handling of transform matrices.
     */
    class VulkanRenderData : public RenderData
    {
    public:
        VulkanRenderData() : RenderData(), uniform_dirty(true)
        {
        }

        VulkanRenderData(const RenderData &rdata) : RenderData(rdata)
        {
        }
        void createPipeline(Shader* shader, VulkanRenderer* renderer){
            if(shader == NULL)
                return;

            generateVbos(shader->signature(),renderer);
            GVR_VK_Vertices& vertices = mesh_->getVkVertices();
            VulkanShader* vk_shader = static_cast<VulkanShader*>(shader);

            // TODO: if viewport, vertices, shader, draw_mode, blending or depth state changes, we need to re-create the pipeline
            if(isHashCodeDirty() || isDirty(0xFFFF)){
                renderer->getCore()->InitPipelineForRenderData(vertices,this, vk_shader->getVkVertexShader(),vk_shader->getVkFragmentShader());
                getHashCode();
                setDirty(false);
            }

        }
        VulkanUniformBlock& getTransformUbo(){
            return vkData.getTransformUbo();
        }
        VulkanData &getVkData()
        {
            return vkData;
        }
        bool isDescriptorSetNull(){
            vkData.isDescriptorSetNull();
        }
        void setDescriptorSetNull(bool flag){
            vkData.setDescriptorSetNull(flag);
        }
        void generateVbos(const std::string& descriptor, VulkanRenderer* renderer){
            mesh_->generateVKBuffers(descriptor,renderer);
        }

        bool       uniform_dirty;

        void bindToShader(Shader* shader, Renderer* renderer);

    private:
        //  VulkanRenderData(const VulkanRenderData& render_data);
        VulkanRenderData(VulkanRenderData&&);

        VulkanRenderData &operator=(const VulkanRenderData&);

        VulkanRenderData &operator=(VulkanRenderData&&);

    private:
        bool m_pipelineInit = false;
         VulkanData vkData;
    };

}


#endif
