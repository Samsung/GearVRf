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

#include "objects/components/render_data.h"
#include "vulkan/vulkan_headers.h"

typedef unsigned long Long;
namespace gvr
{
    class VulkanData
    {

    public:
        VulkanData() : ubo(
                "mat4 u_view; mat4 u_mvp; mat4 u_mv; mat4 u_mv_it; mat4 u_model; mat4 u_view_i; float u_right;", TRANSFORM_UBO_INDEX)
        {
        }

        void createTransformDescriptor(VkDevice &device, VulkanCore *vk)
        {
            ubo.createBuffer(device,vk);
            vk_descriptor.createLayoutBinding( TRANSFORM_UBO_INDEX, VK_SHADER_STAGE_VERTEX_BIT);
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
        VkPipelineLayout m_pipelineLayout;
        // Vulkan
        GVR_Uniform m_modelViewMatrixUniform;

        VkPipeline m_pipeline;
        VkDescriptorSet m_descriptorSet;

    private:
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

        void createVkTransformUbo(VkDevice &device, VulkanCore *vk)
        {
            vkData.createTransformDescriptor(device, vk);
        }
        VulkanUniformBlock& getTransformUbo(){
            return vkData.getTransformUbo();
        }
        VulkanData &getVkData()
        {
            return vkData;
        }

        bool       uniform_dirty;

    private:
        //  VulkanRenderData(const VulkanRenderData& render_data);
        VulkanRenderData(VulkanRenderData&&);

        VulkanRenderData &operator=(const VulkanRenderData&);

        VulkanRenderData &operator=(VulkanRenderData&&);

    private:
         VulkanData vkData;
    };

}


#endif
