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
#include <vulkan/vk_render_to_texture.h>
#include <vulkan/vk_render_target.h>
#include <vulkan/vk_light.h>
#include "renderer.h"
#include "main_sorter.h"
#include "glm/gtc/matrix_inverse.hpp"

#include "objects/scene.h"
#include "vulkan/vulkan_shader.h"
#include "vulkan_renderer.h"
#include "vulkan/vulkan_material.h"
#include "vulkan/vulkan_render_data.h"
#include "vulkan/vk_texture.h"
#include "vulkan/vk_bitmap_image.h"

#include <glslang/Include/Common.h>

namespace gvr {
ShaderData* VulkanRenderer::createMaterial(const char* uniform_desc, const char* texture_desc)
{
    return new VulkanMaterial(uniform_desc, texture_desc);
}

RenderTexture* VulkanRenderer::createRenderTexture(const RenderTextureInfo& renderTextureInfo) {
    return new VkRenderTexture(renderTextureInfo.fboWidth, renderTextureInfo.fboHeight, DEPTH_IMAGE | COLOR_IMAGE, 1, renderTextureInfo.multisamples);
}

RenderTexture* VulkanRenderer::createRenderTexture(int width, int height, int sample_count, int layers, int jdepth_format) {
    return new VkRenderTexture(width, height, DEPTH_IMAGE | COLOR_IMAGE, layers, sample_count);
}

Light* VulkanRenderer::createLight(const char* uniformDescriptor, const char* textureDescriptor)
{
    return new VKLight(uniformDescriptor, textureDescriptor);
}

RenderData* VulkanRenderer::createRenderData()
{
    return new VulkanRenderData();
}

RenderData* VulkanRenderer::createRenderData(RenderData* data)
{
    return new VulkanRenderData(*data);
}

UniformBlock* VulkanRenderer::createTransformBlock(int numMatrices)
{
    std::ostringstream stream;
    stream <<  " uint u_right; uint u_render_mask; uint u_matrix_offset; uint u_pad; mat4 u_matrices[";
    stream << numMatrices;
    stream << ']';
    return VulkanRenderer::createUniformBlock(stream.str().c_str(), TRANSFORM_UBO_INDEX, "Transform_ubo", 0);
}

RenderTarget* VulkanRenderer::createRenderTarget(Scene* scene, bool stereo)
{
    VkRenderTarget* renderTarget = new VkRenderTarget(scene, stereo);
    RenderSorter* sorter = new MainSceneSorter(*this, 0, true);
    renderTarget->setRenderSorter(sorter);
    return renderTarget;
}

RenderTarget* VulkanRenderer::createRenderTarget(RenderTexture* renderTexture, bool isMultiview, bool isStereo)
{
    VkRenderTarget* renderTarget = new VkRenderTarget(renderTexture, isMultiview, isStereo);
    RenderSorter* sorter = new MainSceneSorter(*this, 0, true);
    renderTarget->setRenderSorter(sorter);
    return renderTarget;
}

RenderTarget* VulkanRenderer::createRenderTarget(RenderTexture* renderTexture, const RenderTarget* renderTarget)
{
    VkRenderTarget* vkTarget = new VkRenderTarget(renderTexture, renderTarget);
    RenderSorter* sorter = new MainSceneSorter(*this, 0, true);
    vkTarget->setRenderSorter(sorter);
    return vkTarget;
}

RenderPass* VulkanRenderer::createRenderPass()
{
    return new VulkanRenderPass();
}

UniformBlock* VulkanRenderer::createUniformBlock(const char* desc, int binding, const char* name, int maxelems)
{
    if (maxelems <= 1)
    {
        return new VulkanUniformBlock(desc, binding, name);
    }

    return new VulkanUniformBlock(desc, binding, name, maxelems);
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
    return new VkRenderTexture(width, height, DEPTH_IMAGE | COLOR_IMAGE, 1, sample_count);
}

Shader* VulkanRenderer::createShader(int id, const char* signature,
                                     const char* uniformDescriptor, const char* textureDescriptor,
                                     const char* vertexDescriptor, const char* vertexShader,
                                     const char* fragmentShader, const char* matrixCalc)
{
    return new VulkanShader(id, signature, uniformDescriptor, textureDescriptor, vertexDescriptor, vertexShader, fragmentShader, matrixCalc);
}

VertexBuffer* VulkanRenderer::createVertexBuffer(const char* desc, int vcount)
{
    return new VulkanVertexBuffer(desc, vcount);
}

IndexBuffer* VulkanRenderer::createIndexBuffer(int bytesPerIndex, int icount)
{
    return new VulkanIndexBuffer(bytesPerIndex, icount);
}

void VulkanRenderer::updatePostEffectMesh(Mesh* copy_mesh)
{
    float positions[] = { -1.0f, +1.0f, 1.0f,
                          +1.0f, -1.0f, 1.0f,
                          -1.0f, -1.0f, 1.0f,

                          +1.0f, +1.0f, 1.0f,
                          +1.0f, -1.0f, 1.0f,
                          -1.0f, +1.0f, 1.0f,
    };

    float uvs[] = { 0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,

                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
    };

    const int position_size = sizeof(positions)/ sizeof(positions[0]);
    const int uv_size = sizeof(uvs)/ sizeof(uvs[0]);

    copy_mesh->setVertices(positions, position_size);
    copy_mesh->setFloatVec("a_texcoord", uvs, uv_size);
}

void VulkanRenderer::validate(RenderSorter::Renderable& r)
{
    r.material->updateGPU(this);
    r.renderData->updateGPU(this, r.shader);
}


void VulkanRenderer::render(const RenderState& rstate, const RenderSorter::Renderable& r)
{

}

void VulkanRenderer::renderRenderTarget(Scene* scene, jobject javaSceneObject, RenderTarget* renderTarget, ShaderManager* shader_manager,
                                        RenderTexture* post_effect_render_texture_a, RenderTexture* post_effect_render_texture_b){
    std::vector<RenderData*> render_data_list;
    Camera* camera = renderTarget->getCamera();
    RenderState rstate = renderTarget->getRenderState();
    RenderData* post_effects = camera->post_effect_data();
    rstate.scene = scene;
    rstate.shader_manager = shader_manager;
    rstate.u_matrices[VIEW] = camera->getViewMatrix();
    rstate.u_matrices[PROJECTION] = camera->getProjectionMatrix();
    rstate.javaSceneObject = javaSceneObject;

    if(vulkanCore_->isSwapChainPresent())
        rstate.u_matrices[PROJECTION] = glm::mat4(1,0,0,0,  0,-1,0,0, 0,0,0.5,0, 0,0,0.5,1) * rstate.u_matrices[PROJECTION];
    int postEffectCount = 0;

    if (!rstate.is_shadow)
    {
        rstate.u_render_mask = camera->render_mask();
        rstate.u_right = rstate.u_render_mask & RenderData::RenderMaskBit::Right;
    }

    renderTarget->beginRendering();
    renderTarget->render();
    renderTarget->endRendering();
    VkRenderTarget *vk_renderTarget = static_cast<VkRenderTarget *>(renderTarget);

    if ((post_effects != NULL) &&
        (post_effect_render_texture_a != nullptr) &&
        (post_effects->pass_count() >= 0))
    {

        VkRenderTexture* renderTexture = static_cast<VkRenderTexture*>(post_effect_render_texture_a);
        VkRenderTexture* input_texture = renderTexture;
        vulkanCore_->BuildCmdBufferForRenderData(render_data_list, camera, shader_manager,
                                                 nullptr, renderTexture, false, rstate.is_shadow);

        vulkanCore_->submitCmdBuffer(renderTexture->getFenceObject(), renderTexture->getCommandBuffer());
        vulkanCore_->waitForFence(renderTexture->getFenceObject());

        postEffectCount = post_effects->pass_count();
        // Call Post Effect
        for (int i = 0; i < postEffectCount-1; i++) {
            if (i % 2 == 0)
            {
                renderTexture = static_cast<VkRenderTexture*>(post_effect_render_texture_b);
            }
            else
            {
                renderTexture = static_cast<VkRenderTexture*>(post_effect_render_texture_a);
            }

            if (!renderPostEffectData(rstate,input_texture,post_effects,i))
                return;

            VkCommandBuffer cmdbuffer = renderTexture->getCommandBuffer();
            vulkanCore_->BuildCmdBufferForRenderDataPE(cmdbuffer, rstate.shader_manager,camera, post_effects, renderTexture, i);
            vulkanCore_->submitCmdBuffer(renderTexture->getFenceObject(),cmdbuffer);
            vulkanCore_->waitForFence(renderTexture->getFenceObject());
            input_texture = renderTexture;
        }
        if (!renderPostEffectData(rstate, input_texture, post_effects, postEffectCount - 1))
            return;
        vulkanCore_->BuildCmdBufferForRenderData(render_data_list, camera, shader_manager, renderTarget, nullptr, true, rstate.is_shadow);
        vulkanCore_->submitCmdBuffer(
                static_cast<VkRenderTexture *>(renderTarget->getTexture())->getFenceObject(),
                vk_renderTarget->getCommandBuffer());
    }
    else
    {
        vulkanCore_->BuildCmdBufferForRenderData(render_data_list, camera, shader_manager,
                                                 renderTarget, nullptr, false, rstate.is_shadow);
        vulkanCore_->submitCmdBuffer(
                static_cast<VkRenderTexture *>(renderTarget->getTexture())->getFenceObject(),
                vk_renderTarget->getCommandBuffer());

        // Wait for shadowmap to be rendered
        if(rstate.is_shadow){
            int success = 0;
            while(success != 1){
                success = vulkanCore_->waitForFence(static_cast<VkRenderTexture *>(renderTarget->getTexture())->getFenceObject());
            }
        }

    }

    // Presenting image to swapchain
    if(vulkanCore_->isSwapChainPresent()) {
        vulkanCore_->waitForFence(static_cast<VkRenderTexture *>(renderTarget->getTexture())->getFenceObject());
        vulkanCore_->PresentBackBuffer();
    }

}

    /**
     * Generate shadow maps for all the lights that cast shadows.
     * The scene is rendered from the viewpoint of the light using a
     * special depth shader (GVRDepthShader) to create the shadow map.
     * @see Renderer::renderShadowMap Light::makeShadowMap
     */
    void VulkanRenderer::makeShadowMaps(Scene* scene, jobject javaSceneObject, ShaderManager* shader_manager)
    {
        scene->getLights().makeShadowMaps(scene, javaSceneObject, shader_manager);
    }

}