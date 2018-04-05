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


#include "vulkan/vulkan_render_data.h"
#include "vulkan/vulkan_material.h"
#define VERTEX_BUFFER_BIND_ID 0
namespace gvr
{
void VulkanRenderData::render(Shader* shader, VkCommandBuffer cmdBuffer, int curr_pass){

    if(shader == NULL)
        LOGE("Shader is NULL");

    vkCmdBindPipeline(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                      getVKPipeline(curr_pass) );

    VulkanShader *Vkshader = reinterpret_cast<VulkanShader *>(shader);

    VkDescriptorSet descriptorSet = getDescriptorSet(curr_pass);
    //bind out descriptor set, which handles our uniforms and samplers
    if (!isDescriptorSetNull(curr_pass)) {
        vkCmdBindDescriptorSets(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                Vkshader->getPipelineLayout(), 0, 1,
                                &descriptorSet, 0, NULL);
    }

    // Bind our vertex buffer, with a 0 offset.
    VkDeviceSize offsets[1] = {0};
    VulkanVertexBuffer *vbuf = reinterpret_cast< VulkanVertexBuffer *>(mesh_->getVertexBuffer());
    const VulkanIndexBuffer *ibuf = reinterpret_cast<const VulkanIndexBuffer *>(mesh_->getIndexBuffer());
    const GVR_VK_Vertices *vert = (vbuf->getVKVertices(shader));

    vkCmdBindVertexBuffers(cmdBuffer, VERTEX_BUFFER_BIND_ID, 1, &(vert->buf), offsets);

    if(ibuf && ibuf->getIndexCount()) {
        const GVR_VK_Indices &ind = ibuf->getVKIndices();
        VkIndexType indexType = (ibuf->getIndexSize() == 2) ? VK_INDEX_TYPE_UINT16
                                                            : VK_INDEX_TYPE_UINT32;
        vkCmdBindIndexBuffer(cmdBuffer, ind.buffer, 0, indexType);
        vkCmdDrawIndexed(cmdBuffer, ind.count, 1, 0, 0, 1);
    }
    else
        vkCmdDraw(cmdBuffer, mesh_->getVertexCount(), 1, 0, 1);

}
    void VulkanRenderData::bindToShader(Shader* shader, Renderer* renderer)
    {
/*
    VulkanShader* vkshader = static_cast<VulkanShader*>(shader);
    VulkanRenderer* vkrender = static_cast<VulkanRenderer*>(renderer);
    VulkanCore* vkcore = vkrender->getCore();
    VkDevice& device = vkcore->getDevice();

    getTransformUbo().bindBuffer(shader, renderer);
    if (uniform_dirty)
    {
        VulkanVertexBuffer* vbuf = static_cast<VulkanVertexBuffer*>(mesh()->getVertexBuffer());
        GVR_VK_Vertices& vkverts = *(vbuf->getVKVertices());
        ShaderData* mtl = material(0);
        VulkanMaterial* vkmtl = static_cast<VulkanMaterial*>(mtl);
        vkcore->InitLayoutRenderData(*vkmtl, getVkData(), shader);
        vbuf->bindToShader(shader, mesh_->getIndexBuffer());
        vkcore->InitDescriptorSetForRenderData(vkrender, getVkData(), *vkmtl, getTransformUbo(), shader);
        vkcore->InitPipelineForRenderData(vkverts, this, vkshader->getVkVertexShader(), vkshader->getVkFragmentShader());
        uniform_dirty = false;
    }

  */
    }

    void VulkanRenderData::createPipeline(Shader* shader, VulkanRenderer* renderer, int pass, VkRenderPass render_pass, int sampleCount){
        if(shader == NULL)
            return;

        VulkanVertexBuffer* vbuf = static_cast<VulkanVertexBuffer*>(mesh_->getVertexBuffer());
        const GVR_VK_Vertices* vertices = vbuf->getVKVertices(shader);
        VulkanShader* vk_shader = static_cast<VulkanShader*>(shader);

        // TODO: if viewport, vertices, shader, draw_mode, blending or depth state changes, we need to re-create the pipeline
        renderer->getCore()->InitPipelineForRenderData(vertices,this, vk_shader,pass, render_pass, sampleCount);
        getHashCode();
        clearDirty();
    }
}

