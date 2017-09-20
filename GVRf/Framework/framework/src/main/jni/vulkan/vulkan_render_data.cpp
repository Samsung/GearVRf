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

namespace gvr
{
void VulkanRenderData::bindToShader(Shader* shader, Renderer* renderer)
{
/*
    VulkanShader* vkshader = reinterpret_cast<VulkanShader*>(shader);
    VulkanRenderer* vkrender = reinterpret_cast<VulkanRenderer*>(renderer);
    VulkanCore* vkcore = vkrender->getCore();
    VkDevice& device = vkcore->getDevice();

    getTransformUbo().bindBuffer(shader, renderer);
    if (uniform_dirty)
    {
        VulkanVertexBuffer* vbuf = reinterpret_cast<VulkanVertexBuffer*>(mesh()->getVertexBuffer());
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

    void VulkanRenderData::createPipeline(Shader* shader, VulkanRenderer* renderer, int pass, bool postEffect, int postEffectIndx){
        if(shader == NULL)
            return;

        VulkanVertexBuffer* vbuf = static_cast<VulkanVertexBuffer*>(mesh_->getVertexBuffer());
        const GVR_VK_Vertices* vertices = vbuf->getVKVertices(shader);
        VulkanShader* vk_shader = static_cast<VulkanShader*>(shader);

        // TODO: if viewport, vertices, shader, draw_mode, blending or depth state changes, we need to re-create the pipeline
            renderer->getCore()->InitPipelineForRenderData(vertices,this, vk_shader, pass, postEffect, postEffectIndx);
            getHashCode();
        clearDirty();


    }
}

