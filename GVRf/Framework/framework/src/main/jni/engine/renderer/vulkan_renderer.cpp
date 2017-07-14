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

#include <vulkan/vulkan_index_buffer.h>
#include <vulkan/vulkan_vertex_buffer.h>
#include <vulkan/vk_cubemap_image.h>
#include "renderer.h"
#include "glm/gtc/matrix_inverse.hpp"

#include "objects/scene.h"
#include "objects/textures/render_texture.h"
#include "vulkan/vulkan_shader.h"
#include "vulkan_renderer.h"
#include "vulkan/vulkan_material.h"
#include "vulkan/vulkan_render_data.h"
#include "vulkan/vk_texture.h"
#include "vulkan/vk_bitmap_image.h"

namespace gvr {
    ShaderData* VulkanRenderer::createMaterial(const char* uniform_desc, const char* texture_desc)
    {
        return new VulkanMaterial(uniform_desc, texture_desc);
    }

    RenderData* VulkanRenderer::createRenderData()
    {
        return new VulkanRenderData();
    }

    RenderPass* VulkanRenderer::createRenderPass(){
        return new VulkanRenderPass();
    }
    UniformBlock* VulkanRenderer::createUniformBlock(const char* desc, int binding, const char* name)
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

    Shader* VulkanRenderer::createShader(int id, const char* signature,
                                     const char* uniformDescriptor, const char* textureDescriptor,
                                     const char* vertexDescriptor, const char* vertexShader,
                                     const char* fragmentShader)
    {
        return new VulkanShader(id, signature, uniformDescriptor, textureDescriptor, vertexDescriptor, vertexShader, fragmentShader);
    }

    VertexBuffer* VulkanRenderer::createVertexBuffer(const char* desc, int vcount)
    {
        return new VulkanVertexBuffer(desc, vcount);
    }

    IndexBuffer* VulkanRenderer::createIndexBuffer(int bytesPerIndex, int icount)
    {
        return new VulkanIndexBuffer(bytesPerIndex, icount);
    }

    bool VulkanRenderer::renderWithShader(RenderState& rstate, Shader* shader, RenderData* rdata, ShaderData* shaderData,  int pass)
    {
        Transform* const t = rdata->owner_object()->transform();

        int status = shaderData->updateGPU(this);
        if (status < 0)
        {
            LOGE("SHADER: textures not ready %s", rdata->owner_object()->name().c_str());
            return false;
        }

        VulkanRenderData* vkRdata = static_cast<VulkanRenderData*>(rdata);
        UniformBlock& transformUBO = vkRdata->getTransformUbo();
        VulkanMaterial* vkmtl = static_cast<VulkanMaterial*>(shaderData);

        if (shader->usesMatrixUniforms())
        {
            updateTransforms(rstate, &transformUBO, t);
        }
        rdata->updateGPU(this,shader);

        vulkanCore_->InitLayoutRenderData(*vkmtl, vkRdata, shader);

        if(vkRdata->isHashCodeDirty() || vkRdata->isDirty(0xFFFF) || vkRdata->isDescriptorSetNull(pass)) {

            vulkanCore_->InitDescriptorSetForRenderData(this, pass, shader, vkRdata);
            vkRdata->createPipeline(shader, this, pass);
        }
        shader->useShader();
        return true;
    }

    bool VulkanRenderer::renderWithPostEffectShader(RenderState& rstate, Shader* shader, RenderData* rdata, ShaderData* shaderData,  int pass)
    {
        // Updates its vertex buffer
        rdata->updateGPU(this,shader);

        // For its layout (uniforms and samplers)
        VulkanRenderData* vkRdata = static_cast<VulkanRenderData*>(rdata);
        VulkanMaterial* vkmtl = static_cast<VulkanMaterial*>(shaderData);

        vulkanCore_->InitLayoutRenderDataPostEffect(*vkmtl, vkRdata, shader);

        //if(vkRdata->isHashCodeDirty() || vkRdata->isDirty(0xFFFF) || vkRdata->isDescriptorSetNull(pass)) {

            vulkanCore_->InitDescriptorSetForRenderDataPostEffect(this, pass, shader, vkRdata);
            vkRdata->createPipeline(shader, this, pass);
       // }
    }

    void VulkanRenderer::renderCamera(Scene *scene, Camera *camera,
                                      ShaderManager *shader_manager,
                                      PostEffectShaderManager *post_effect_shader_manager,
                                      RenderTexture *post_effect_render_texture_a,
                                      RenderTexture *post_effect_render_texture_b) {


        if(!vulkanCore_->swapChainCreated())
            vulkanCore_->initVulkanCore();

        std::vector<RenderData*> render_data_list;
        vulkanCore_->AcquireNextImage();
        RenderState rstate;
        rstate.shadow_map = false;
        rstate.material_override = NULL;
        rstate.shader_manager = shader_manager;
        rstate.scene = scene;
        rstate.render_mask = camera->render_mask();
        rstate.uniforms.u_right = rstate.render_mask & RenderData::RenderMaskBit::Right;
        rstate.uniforms.u_view = camera->getViewMatrix();
        rstate.uniforms.u_proj = camera->getProjectionMatrix();

        vulkanCore_->setPostEffectCount(camera->post_effect_data().size());

        std::vector<ShaderData *> post_effects = camera->post_effect_data();
        if(post_effects.size() == 0)
            return;

        for (auto &rdata : render_data_vector)
        {
            if (!(rstate.render_mask & rdata->render_mask()))
                continue;

            for(int curr_pass =0; curr_pass< rdata->pass_count(); curr_pass++) {

                ShaderData *curr_material = rdata->material(curr_pass);
                Shader *shader = rstate.shader_manager->getShader(rdata->get_shader(curr_pass));
                if (shader == NULL)
                {
                    LOGE("SHADER: shader not found");
                    continue;
                }
                if (rstate.material_override != nullptr) {
                    curr_material = rstate.material_override;
                }
                if (!renderWithShader(rstate, shader, rdata, curr_material, curr_pass))
                    break;

                if(curr_pass == rdata->pass_count()-1)
                    render_data_list.push_back(rdata);
            }
        }

        // Call Post Effect
        //std::vector<ShaderData*> postEffects = camera->post_effect_data();
        std::vector<RenderData*> renderData;
        for(int i = 0; i < post_effects.size(); i++){
            VulkanRenderPass * vulkanRenderPass = new VulkanRenderPass();
            RenderData * rr = post_effect_render_data_vulkan();
            rr->add_pass(vulkanRenderPass);
            renderData.push_back(rr);
        }

            Shader *shader = rstate.shader_manager->getShader(post_effects[0]->getNativeShader());
            renderWithPostEffectShader(rstate, shader, renderData[0],
                                       post_effects[0], 0);

            //vulkanCore_->postEffectRender(renderData[0], shader);

        vulkanCore_->BuildCmdBufferForRenderData(render_data_list,camera, shader_manager, renderData[0], shader);
        vulkanCore_->DrawFrameForRenderData();


        // Freeing RenderData of Post Effect
        for(int i = 0; i < post_effects.size(); i++){
            delete renderData[0];
        }
    }


}