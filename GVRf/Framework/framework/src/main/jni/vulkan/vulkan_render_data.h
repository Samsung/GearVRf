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
#include "vulkan_vertex_buffer.h"
#include "vulkan_index_buffer.h"

typedef unsigned long Long;
namespace gvr
{
struct VulkanRenderPass : public RenderPass
{
    virtual ~VulkanRenderPass() {}
    bool descriptorSetNull = true;
    VkDescriptorPool m_descriptorPool;
    VkPipeline m_pipeline;
    VkDescriptorSet m_descriptorSet[2] = {VK_NULL_HANDLE, VK_NULL_HANDLE};
};


    /**
     * Vulkan implementation of RenderData.
     * Specializes handling of transform matrices.
     */
    class VulkanRenderData : public RenderData
    {
    public:
        VulkanRenderData() : RenderData(), ubo(
        "mat4 u_view; mat4 u_mvp; mat4 u_mv; mat4 u_mv_it; mat4 u_model; mat4 u_view_i; float u_right;", TRANSFORM_UBO_INDEX, "Transform_ubo")
        {
        }

        explicit VulkanRenderData(const RenderData &rdata) : RenderData(rdata), ubo(
        "mat4 u_view; mat4 u_mvp; mat4 u_mv; mat4 u_mv_it; mat4 u_model; mat4 u_view_i; float u_right;", TRANSFORM_UBO_INDEX, "Transform_ubo")
        {
        }

        virtual ~VulkanRenderData() {}

        void createPipeline(Shader* shader, VulkanRenderer* renderer, int pass, VkRenderPass, int sampleCount);

        VulkanUniformBlock& getTransformUbo(){
            return ubo;
        }
        VkPipeline getVKPipeline(int pass)
        {
            VulkanRenderPass* renderPass = static_cast<VulkanRenderPass*>(render_pass_list_[pass]);
            return renderPass->m_pipeline;

        }
        bool isDescriptorSetNull(int pass){
            VulkanRenderPass* renderPass = static_cast<VulkanRenderPass*>(render_pass_list_[pass]);
            return renderPass->descriptorSetNull;
        }
        VkDescriptorSet getDescriptorSet(int pass)
        {
            VulkanRenderPass* renderPass = static_cast<VulkanRenderPass*>(render_pass_list_[pass]);
            return renderPass->m_descriptorSet[0];
        }
        void setPipeline(VkPipeline pipeline, int pass){
            VulkanRenderPass* renderPass = static_cast<VulkanRenderPass*>(render_pass_list_[pass]);
            renderPass->m_pipeline = pipeline;

        }
        void setDescriptorPool(VkDescriptorPool descriptorPool, int pass){
            VulkanRenderPass* renderPass = static_cast<VulkanRenderPass*>(render_pass_list_[pass]);
            renderPass->m_descriptorPool= descriptorPool;

        }
        VkDescriptorPool& getDescriptorPool(int pass){
            VulkanRenderPass* renderPass = static_cast<VulkanRenderPass*>(render_pass_list_[pass]);
            return renderPass->m_descriptorPool;

        }
        void generateVbos(const std::string& descriptor, VulkanRenderer* renderer, Shader* shader){
            VulkanVertexBuffer* vbuf = static_cast<VulkanVertexBuffer*>(mesh_->getVertexBuffer());
            vbuf->generateVKBuffers(renderer->getCore(),shader);
            VulkanIndexBuffer* ibuf = static_cast< VulkanIndexBuffer*>(mesh_->getIndexBuffer());
            ibuf->generateVKBuffers(renderer->getCore());
        }

        VulkanRenderPass* getRenderPass(int pass){
            return static_cast<VulkanRenderPass*>(render_pass_list_[pass]);
        }

        VulkanRenderPass* getShadowRenderPass(){
            return shadowPass;
        }

        void setShadowRenderPass(VulkanRenderPass* sp){
            shadowPass = sp;
        }

        void bindToShader(Shader* shader, Renderer* renderer);
        bool isDirty(int pass){
            return isHashCodeDirty() || RenderData::isDirty() || isDescriptorSetNull(pass);
        }
        void render(Shader* shader, VkCommandBuffer cmdBuffer, int curr_pass);
    private:
        //  VulkanRenderData(const VulkanRenderData& render_data);
        VulkanRenderData(VulkanRenderData&&);

        VulkanRenderData &operator=(const VulkanRenderData&);

        VulkanRenderData &operator=(VulkanRenderData&&);

    private:
        VulkanUniformBlock ubo;
        VulkanRenderPass * shadowPass = nullptr;

    };

}


#endif
