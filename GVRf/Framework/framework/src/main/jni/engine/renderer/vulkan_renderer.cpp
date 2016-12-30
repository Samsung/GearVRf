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
 * Renders a scene, a screen.
 ***************************************************************************/

#include "renderer.h"
#include "glm/gtc/matrix_inverse.hpp"

#include "objects/scene.h"
#include "objects/textures/render_texture.h"
#include "shaders/material/custom_shader.h"
#include "vulkan_renderer.h"
#include "vulkan/vulkan_material.h"
#include "vulkan/vulkan_render_data.h"
#include "vulkan/vk_texture.h"

namespace gvr {
    ShaderData* VulkanRenderer::createMaterial(const std::string& desc)
    {
        return new VulkanMaterial(desc);
    }

    RenderData* VulkanRenderer::createRenderData()
    {
        return new VulkanRenderData();
    }

    UniformBlock* VulkanRenderer::createUniformBlock(const std::string& desc,int  index)
    {
        return new VulkanUniformBlock(desc,index);
    }

    Image* VulkanRenderer::createImage(int type, int format)
    {
        switch (type)
        {
            case Image::ImageType::BITMAP: return new VkBitmapImage(format);
       //     case Image::ImageType::CUBEMAP: return new GLCubemapImage(format);
        //    case Image::ImageType::FLOAT_BITMAP: return new GLFloatImage();
        }
        return NULL;
    }

    Texture* VulkanRenderer::createTexture(int target)
    {
        // TODO: where to send the target
        return new VkTexture(static_cast<int>(VK_IMAGE_TYPE_2D));
    }

    RenderTexture* VulkanRenderer::createRenderTexture(int width, int height, int sample_count,
                                                   int jcolor_format, int jdepth_format, bool resolve_depth,
                                                   const int* texture_parameters)
    {
        return NULL;
    }

    void VulkanRenderer::updateTransforms(VulkanUniformBlock* transform_ubo, Transform* modelTrans, Camera* camera)
    {
        glm::mat4 model = modelTrans->getModelMatrix();
        glm::mat4 view = camera->getViewMatrix();
        glm::mat4 proj = camera->getProjectionMatrix();
        glm::mat4 modelViewProjection = proj * view * model;

        transform_ubo->setMat4("u_mvp", modelViewProjection);
    }

    void VulkanRenderer::renderCamera(Scene *scene, Camera *camera,
                                      ShaderManager *shader_manager,
                                      PostEffectShaderManager *post_effect_shader_manager,
                                      RenderTexture *post_effect_render_texture_a,
                                      RenderTexture *post_effect_render_texture_b) {


        if(!vulkanCore_->swapChainCreated())
            vulkanCore_->initVulkanCore();

    //    if(render_data_vector.size() == 1)
   //         return;

        std::vector<VkDescriptorSet> allDescriptors;

        int swapChainIndex = vulkanCore_->AcquireNextImage();

        for (auto &rdata : render_data_vector) {
            VulkanRenderData* vkRdata = reinterpret_cast<VulkanRenderData*>(rdata);

            // Creating and initializing Uniform Buffer for Each Render Data
            if (vkRdata->uniform_dirty) {


                VulkanUniformBlock& matUBO = reinterpret_cast<VulkanMaterial*>(vkRdata->material(0))->getVulkanUniforms();
                vkRdata->createVkTransformUbo(vulkanCore_->getDevice(), vulkanCore_);
                matUBO.createVkMaterialDescriptor(vulkanCore_->getDevice(), vulkanCore_);

                vulkanCore_->InitLayoutRenderData(vkRdata);
                Shader *shader = shader_manager->getShader(vkRdata->get_shader());

                vkRdata->mesh()->generateVKBuffers(shader->signature(), vulkanCore_->getDevice(), vulkanCore_);

                GVR_VK_Vertices &vert = vkRdata->mesh()->getVkVertices();

                vulkanCore_->InitDescriptorSetForRenderData(vkRdata);
                vulkanCore_->InitPipelineForRenderData(vert, vkRdata, shader->getVkVertexShader(), shader->getVkFragmentShader());
                vulkanCore_->updateMaterialUniform(scene, camera, vkRdata, shader->getUniformNames());

                vkRdata->uniform_dirty = false;
            }

            allDescriptors.push_back(vkRdata->getVkData().m_descriptorSet);
            VulkanUniformBlock& transformUBO = (vkRdata->getTransformUbo());
            updateTransforms(&transformUBO, vkRdata->owner_object()->transform(), camera);
            vulkanCore_->UpdateUniforms(&transformUBO);
        }
        vulkanCore_->BuildCmdBufferForRenderData(allDescriptors, swapChainIndex, render_data_vector,camera);

        vulkanCore_->DrawFrameForRenderData(swapChainIndex);

    }

}