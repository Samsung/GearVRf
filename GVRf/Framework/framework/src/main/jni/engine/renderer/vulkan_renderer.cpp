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
UniformBlock* VulkanRenderer::createUniformBlock(const char* desc, int binding, const char* name, int maxelems)
{
    if (maxelems <= 1)
    {
        return new VulkanUniformBlock(desc, binding, name);
    }
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
                                                   const TextureParameters* texture_parameters, int number_views)
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
    VulkanRenderData* vkRdata = static_cast<VulkanRenderData*>(rdata);
    UniformBlock& transformUBO = vkRdata->getTransformUbo();
    VulkanMaterial* vkmtl = static_cast<VulkanMaterial*>(shaderData);

    if (shader->usesMatrixUniforms())
    {
        updateTransforms(rstate, &transformUBO, t);
    }
    rdata->updateGPU(this,shader);

    vulkanCore_->InitLayoutRenderData(*vkmtl, vkRdata, shader, false);

    if(vkRdata->isHashCodeDirty() || vkRdata->isDirty() || vkRdata->isDescriptorSetNull(pass)) {
        vkRdata->clearDirty();
        vulkanCore_->InitDescriptorSetForRenderData(this, pass, shader, vkRdata);
        vkRdata->createPipeline(shader, this, pass, false, 0);
    }
    return true;
}

bool VulkanRenderer::renderWithPostEffectShader(RenderState& rstate, Shader* shader, RenderData* rdata, int passNum)
{
    // Updates its vertex buffer
    rdata->updateGPU(this,shader);

    // For its layout (uniforms and samplers)
    VulkanRenderData* vkRdata = static_cast<VulkanRenderData*>(rdata);
    VulkanRenderPass* rpass = static_cast<VulkanRenderPass*>(rdata->pass(passNum));
    VulkanMaterial* vkmtl = static_cast<VulkanMaterial*>(rpass->material());

    vulkanCore_->InitLayoutRenderData(*vkmtl, vkRdata, shader, true);

    if(vkRdata->isHashCodeDirty() || vkRdata->isDirty() || vkRdata->isDescriptorSetNull(passNum)) {
        vkRdata->clearDirty();
        vulkanCore_->InitDescriptorSetForRenderDataPostEffect(this, 0, shader, vkRdata, passNum);
        vkRdata->set_depth_test(0);
        vkRdata->createPipeline(shader, this, 0, true, passNum);
    }

    return true;
}

Mesh* VulkanRenderer::getPostEffectMesh()
{
    if (post_effect_mesh_)
    {
        return post_effect_mesh_;
    }
    float positions[] = { -1.0f, 1.0f,  1.0f,
                          -1.0f, -1.0f,  1.0f,
                          1.0f,  -1.0f,  1.0f,
                          1.0f,  1.0f,  1.0f,
                          -1.0f, 1.0f,  1.0f,
                          1.0f,  -1.0f,  1.0f};

    float uvs[] = { 0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f};

    const int position_size = sizeof(positions)/ sizeof(positions[0]);
    const int uv_size = sizeof(uvs)/ sizeof(uvs[0]);

    Mesh* mesh = new Mesh("float3 a_position float2 a_texcoord");
    mesh->setVertices(positions, position_size);
    mesh->setFloatVec("a_texcoord", uvs, uv_size);
    post_effect_mesh_ = mesh;
    return mesh;
}

void VulkanRenderer::renderCamera(Scene *scene, Camera *camera,
                                  ShaderManager *shader_manager,
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

    RenderData* post_effects = camera->post_effect_data();
    int postEffectCount = post_effects->pass_count();

    for (auto &rdata : render_data_vector)
    {
        if (!(rstate.render_mask & rdata->render_mask()))
            continue;

        for(int curr_pass = 0; curr_pass < rdata->pass_count(); curr_pass++) {

            ShaderData *curr_material = rdata->material(curr_pass);
            Shader *shader = rstate.shader_manager->getShader(rdata->get_shader(rstate.is_multiview,curr_pass));
            if (shader == NULL)
            {
                LOGE("SHADER: shader not found");
                continue;
            }
            if (!renderWithShader(rstate, shader, rdata, curr_material, curr_pass))
                break;

            if(curr_pass == rdata->pass_count()-1)
                render_data_list.push_back(rdata);
        }
    }

    vulkanCore_->BuildCmdBufferForRenderData(render_data_list,camera, shader_manager);
    int index = vulkanCore_->DrawFrameForRenderData();

    if(postEffectCount)
        vulkanCore_->InitPostEffectChain();

    // Call Post Effect
    for(int i = 0; i < post_effects->pass_count(); i++) {
        RenderPass* rp =  post_effects->pass(i);
        RenderPass* rpass = post_effects->pass(i);

        int result = rpass->isValid(this, rstate, post_effects);
        if (result < 0)         // something wrong with material or texture
        {
            LOGE("Renderer::renderPostEffectData pass %d material or texture not ready", i);
            return;             // don't render this pass
        }
        if ((result == 0) && (post_effects->isValid(this, rstate) < 0))
        {
            LOGE("Renderer::renderPostEffectData pass %d shader not available", i);
            return;             // no shader available
        }
        int nativeShader = rpass->get_shader(rstate.is_multiview);
        Shader* shader = rstate.shader_manager->getShader(nativeShader);
        renderWithPostEffectShader(rstate, shader, post_effects, i);

        vulkanCore_->BuildCmdBufferForRenderDataPE(camera, post_effects, shader, i);
        vulkanCore_->DrawFrameForRenderDataPE();
        index = i % 2;
    }

    vulkanCore_->RenderToOculus(index, postEffectCount);
}


}