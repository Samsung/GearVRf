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

#include "objects/scene.h"
#include "vulkan/vulkan_shader.h"
#include "vulkan_renderer.h"
#include "vulkan/vulkan_material.h"
#include "vulkan/vulkan_render_data.h"
#include "vulkan/vk_cubemap_image.h"
#include "vulkan/vk_bitmap_image.h"

namespace gvr {
    ShaderData* VulkanRenderer::createMaterial(const std::string& desc)
    {
        return new VulkanMaterial(desc);
    }

    RenderData* VulkanRenderer::createRenderData()
    {
        return new VulkanRenderData();
    }

    UniformBlock* VulkanRenderer::createUniformBlock(const std::string& desc, int binding, const std::string& name)
    {
        return new VulkanUniformBlock(desc, binding, name);
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
        // TODO: where to send the target
        return new VkTexture(static_cast<int>(VK_IMAGE_TYPE_2D));
    }

    RenderTexture* VulkanRenderer::createRenderTexture(int width, int height, int sample_count,
                                                 int jcolor_format, int jdepth_format, bool resolve_depth,
                                                 const TextureParameters* texture_parameters)
    {
        return NULL;
    }

    Shader* VulkanRenderer::createShader(int id, const std::string& signature,
                                     const std::string& uniformDescriptor, const std::string& textureDescriptor,
                                     const std::string& vertexDescriptor, const std::string& vertexShader,
                                     const std::string& fragmentShader)
    {
        return new VulkanShader(id, signature, uniformDescriptor, textureDescriptor, vertexDescriptor, vertexShader, fragmentShader);
    }


    int VulkanRenderer::renderWithShader(RenderState& rstate, Shader* shader, RenderData* rdata, ShaderData* shaderData)
    {
        Transform* const t = rdata->owner_object()->transform();

        if (shader == nullptr)
        {
            LOGE("SHADER: shader %d not found", shaderData->getNativeShader());
            return 0;
        }
        int status = shaderData->updateGPU(this, shader);
        if (status < 0)
        {
            LOGE("SHADER: textures not ready %s", rdata->owner_object()->name().c_str());
            return 0;
        }

        VulkanRenderData* vkRdata = static_cast<VulkanRenderData*>(rdata);
        VulkanUniformBlock& transformUBO = vkRdata->getTransformUbo();
        VulkanMaterial* vkmtl = static_cast<VulkanMaterial*>(shaderData);

        vkRdata->generateVbos(shader->signature(),this);
        updateTransforms(rstate, &transformUBO, t);
       // if (status != -1)
        {
            VulkanData& vkdata = vkRdata->getVkData();

            // only need to call when binding is changed
            vulkanCore_->InitLayoutRenderData(*vkmtl, vkdata, shader);

            // if texture or binding, material is changed, call this
            if(vkRdata->isDirty(0xFFFF))
                vulkanCore_->InitDescriptorSetForRenderData(this, vkdata, *vkmtl, &transformUBO, shader);
        }
        vkRdata->createPipeline(shader,this);
        try
        {
            shader->useShader(rdata->mesh());
        }
        catch (const std::string &error)
        {
            LOGE("Error detected in VulkanRenderer; name : %s, error : %s",
                 rdata->owner_object()->name().c_str(), error.c_str());
            return -1;
        }
        return 1;
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
        RenderState rstate;
        rstate.shadow_map = false;
        rstate.material_override = NULL;
        rstate.shader_manager = shader_manager;
        rstate.scene = scene;
        rstate.render_mask = camera->render_mask();
        rstate.uniforms.u_right = rstate.render_mask & RenderData::RenderMaskBit::Right;
        rstate.uniforms.u_view = camera->getViewMatrix();
        rstate.uniforms.u_proj = camera->getProjectionMatrix();
/*
        rstate.viewportX = viewportX;
        rstate.viewportY = viewportY;
        rstate.viewportWidth = viewportWidth;
        rstate.viewportHeight = viewportHeight;
 */

        for (auto &rdata : render_data_vector)
        {

            ShaderData* curr_material = rdata->material(0);
            Shader *shader = shader_manager->getShader(rdata->get_shader());

            if (rstate.material_override != nullptr)
            {
                curr_material = rstate.material_override;
            }
            int status = renderWithShader(rstate, shader, rdata, curr_material);
            if (status == 0)
            {
                LOGE("SHADER: textures not ready %s", rdata->owner_object()->name().c_str());
                continue;
            }
            if (status < 0)
            {
                LOGE("Error detected in VulkanRenderer");
                shader = shader_manager->findShader(std::string("GVRErrorShader"));
                shader->useShader(rdata->mesh());
            }
            allDescriptors.push_back(static_cast<VulkanRenderData*>(rdata)->getVkData().m_descriptorSet);
        }
        vulkanCore_->BuildCmdBufferForRenderData(allDescriptors, swapChainIndex, render_data_vector,camera);
        vulkanCore_->DrawFrameForRenderData(swapChainIndex);
    }


}