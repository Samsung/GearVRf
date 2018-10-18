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

#ifndef FRAMEWORK_VULKANRENDERER_H
#define FRAMEWORK_VULKANRENDERER_H

#include <vector>
#include <memory>



#include "glm/glm.hpp"
#include "objects/eye_type.h"
#include "objects/mesh.h"
#include "objects/bounding_volume.h"
#include <unordered_map>
#include "renderer.h"
#include "vulkan/vulkan_headers.h"
#include "vulkan/vulkan_flags.h"


namespace gvr {

class Camera;
class Scene;
class SceneObject;
class ShaderData;
class RenderData;
class RenderTexture;
class Light;
class BitmapImage;
class CubemapImage;
class CompressedImage;

class VulkanRenderer: public Renderer {
    friend class Renderer;

protected:
    virtual ~VulkanRenderer(){
        vulkanCore_->releaseInstance();
    }

public:

    VkFence createFenceObject(){
        return vulkanCore_->createFenceObject();
    }
    VkCommandBuffer createCommandBuffer(VkCommandBufferLevel level){
        return vulkanCore_->createCommandBuffer(level);
    }
    void renderToOculus(RenderTarget* renderTarget){
        vulkanCore_->renderToOculus(renderTarget);
    }

    void unmapRenderToOculus(RenderTarget* renderTarget){
        vulkanCore_->unmapRenderToOculus(renderTarget);
    }

    Texture* createSharedTexture( int id) { return nullptr; };


    VulkanRenderer() : Renderer(), vulkanCore_(nullptr) {
        vkflags::initVkRenderFlags();
        vulkanCore_ = VulkanCore::getInstance();
    }

    VulkanCore* getCore() { return vulkanCore_; }
    VkDevice& getDevice(){
        return vulkanCore_->getDevice();
    }
    bool GetMemoryTypeFromProperties(uint32_t typeBits, VkFlags requirements_mask,
                                     uint32_t *typeIndex){
        return vulkanCore_->GetMemoryTypeFromProperties(typeBits,requirements_mask,typeIndex);
    }
    void initCmdBuffer(VkCommandBufferLevel level,VkCommandBuffer& cmdBuffer){
        vulkanCore_->initCmdBuffer(level,cmdBuffer);
    }
    VkQueue& getQueue(){
        return vulkanCore_->getVkQueue();
    }
    VkPhysicalDevice& getPhysicalDevice(){
        return vulkanCore_->getPhysicalDevice();
    }
    void makeShadowMaps(Scene* scene, jobject javaSceneObject, ShaderManager* shader_manager);
    virtual ShaderData* createMaterial(const char* uniform_desc, const char* texture_desc);
    virtual RenderData* createRenderData();
    virtual RenderData* createRenderData(RenderData*);
    virtual RenderPass* createRenderPass();
    virtual ShadowMap* createShadowMap(ShaderData*) { return nullptr; }
    virtual UniformBlock* createUniformBlock(const char* desc, int binding, const char* name, int maxelems);
    Image* createImage(int type, int format);
    virtual RenderTarget* createRenderTarget(Scene*, bool stereo);
    virtual RenderTarget* createRenderTarget(RenderTexture*, bool multiview, bool stereo);
    virtual RenderTarget* createRenderTarget(RenderTexture*, const RenderTarget*);
    virtual Texture* createTexture(int target = GL_TEXTURE_2D);
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count,
                                               int jcolor_format, int jdepth_format, bool resolve_depth,
                                               const TextureParameters* texture_parameters, int number_views);
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count, int layers, int depthformat);
    virtual RenderTexture* createRenderTexture(const RenderTextureInfo&);
    virtual VertexBuffer* createVertexBuffer(const char* desc, int vcount);
    virtual IndexBuffer* createIndexBuffer(int bytesPerIndex, int icount);
    virtual Shader* createShader(int id, const char* signature,
                                 const char* uniformDescriptor, const char* textureDescriptor,
                                 const char* vertexDescriptor, const char* vertexShader,
                                 const char* fragmentShader, const char* matrixCalc);
    virtual void renderRenderTarget(Scene*, jobject javaSceneObject, RenderTarget* renderTarget, ShaderManager* shader_manager,
                                    RenderTexture* post_effect_render_texture_a, RenderTexture* post_effect_render_texture_b);
    virtual Light* createLight(const char* uniformDescriptor, const char* textureDescriptor);
    virtual void updatePostEffectMesh(Mesh*);
    virtual void validate(RenderSorter::Renderable& r);
    virtual void render(const RenderState&, const RenderSorter::Renderable&);
    virtual UniformBlock* createTransformBlock(int numMatrices);

private:
    VulkanCore* vulkanCore_;
};
}
#endif //FRAMEWORK_VULKANRENDERER_H