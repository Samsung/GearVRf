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
            case Image::ImageType::CUBEMAP: return new VkCubemapImage(format);
        //    case Image::ImageType::FLOAT_BITMAP: return new GLFloatImage();
        }
        return NULL;
    }

    Texture* VulkanRenderer::createTexture(int target)
    {
    /*    switch (type)
        {
            case Texture::TextureType::TEXTURE_2D: return new GLTexture(GL_TEXTURE_2D);
            case Texture::TextureType::TEXTURE_ARRAY: return new GLTexture(GL_TEXTURE_2D_ARRAY);
            case Texture::TextureType::TEXTURE_EXTERNAL: return new GLTexture(GL_TEXTURE_EXTERNAL_OES);
            case Texture::TextureType::TEXTURE_EXTERNAL_RENDERER: return new GLExternalRendererTexture();
        }
        return new GLTexture(type);
      */
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

        if (modelTrans == nullptr)
        {
            LOGE("Transform is NULL");
            return;
        }

        glm::mat4 model = modelTrans->getModelMatrix();
        glm::mat4 view = camera->getViewMatrix();
        glm::mat4 proj = camera->getProjectionMatrix();
        glm::mat4 mv = view * model;
        glm::mat4 modelViewProjection = proj * mv;
        glm::mat4 u_view_i = glm::inverse(view);
        glm::mat4 u_mv_it = glm::inverseTranspose(mv);

        transform_ubo->setMat4("u_view", view);
        transform_ubo->setMat4("u_mvp", modelViewProjection);
        transform_ubo->setMat4("u_mv", mv);
        transform_ubo->setMat4("u_mv_it",u_mv_it);
        transform_ubo->setMat4("u_model", model);
        transform_ubo->setMat4("u_view_i",u_view_i );
        transform_ubo->setMat4("u_view_i",u_view_i );
        transform_ubo->setFloat("u_right", camera->render_mask() & RenderData::RenderMaskBit::Right);
    }

    void VulkanRenderer::renderCamera(Scene *scene, Camera *camera,
                                      ShaderManager *shader_manager,
                                      PostEffectShaderManager *post_effect_shader_manager,
                                      RenderTexture *post_effect_render_texture_a,
                                      RenderTexture *post_effect_render_texture_b) {



        if(!vulkanCore_->swapChainCreated())
            vulkanCore_->initVulkanCore();

        std::vector<VkDescriptorSet> allDescriptors;

        int swapChainIndex = vulkanCore_->AcquireNextImage();


        for (auto &rdata : render_data_vector) {
            VulkanRenderData* vkRdata = reinterpret_cast<VulkanRenderData*>(rdata);

            Shader *shader = shader_manager->getShader(vkRdata->get_shader());
            std::string shader_sig = shader->signature();
     //       if(shader_sig.find("u_texture")!=std::string::npos)
      //          return;
            // Creating and initializing Uniform Buffer for Each Render Data
            if (vkRdata->uniform_dirty) {


                VulkanUniformBlock& matUBO = reinterpret_cast<VulkanMaterial*>(vkRdata->material(0))->getVulkanUniforms();
                vkRdata->createVkTransformUbo(vulkanCore_->getDevice(), vulkanCore_);
                matUBO.createVkMaterialDescriptor(vulkanCore_->getDevice(), vulkanCore_);

                vulkanCore_->InitLayoutRenderData(vkRdata);

                vkRdata->mesh()->generateVKBuffers(shader_sig, vulkanCore_->getDevice(), vulkanCore_);

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