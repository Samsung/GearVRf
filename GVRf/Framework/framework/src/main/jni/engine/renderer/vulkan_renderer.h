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
#include "batch.h"
#include "objects/eye_type.h"
#include "objects/mesh.h"
#include "objects/bounding_volume.h"
#include <unordered_map>
#include "batch_manager.h"
#include "renderer.h"
#include "vulkan/vulkan_headers.h"

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
    Texture* createSharedTexture( int id) {};

    VulkanRenderer() : vulkanCore_(nullptr) {
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
    void renderRenderDataVector(RenderState&, std::vector<RenderData*>& render_data_vector, std::vector<RenderData*>&);
    void restoreRenderStates(RenderData* render_data){}
    void setRenderStates(RenderData* render_data, RenderState& rstate){}
    virtual void cullAndRender(RenderTarget* renderTarget, Scene* scene,
                        ShaderManager* shader_manager, PostEffectShaderManager* post_effect_shader_manager,
                        RenderTexture* post_effect_render_texture_a,
                        RenderTexture* post_effect_render_texture_b) {};
    void makeShadowMaps(Scene* scene, jobject javaSceneObject, ShaderManager* shader_manager){}
    void set_face_culling(int cull_face){}
    virtual ShaderData* createMaterial(const char* uniform_desc, const char* texture_desc);
    virtual RenderData* createRenderData();
    virtual RenderData* createRenderData(RenderData*);
    virtual RenderPass* createRenderPass();
    virtual UniformBlock* createUniformBlock(const char* desc, int binding, const char* name, int maxelems);
    Image* createImage(int type, int format);
    virtual RenderTarget* createRenderTarget(Scene*);
    virtual RenderTarget* createRenderTarget(RenderTexture*, bool);
    virtual RenderTarget* createRenderTarget(RenderTexture*, const RenderTarget*);
    virtual Texture* createTexture(int target = GL_TEXTURE_2D);
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count,
                                               int jcolor_format, int jdepth_format, bool resolve_depth,
                                               const TextureParameters* texture_parameters, int number_views);
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count,
                                               int jcolor_format, int jdepth_format, bool resolve_depth,
                                               const TextureParameters* texture_parameters, int number_views, bool monoscopic);
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count, int layers, int depthformat) { }
    virtual RenderTexture* createRenderTexture(const RenderTextureInfo*);
    virtual VertexBuffer* createVertexBuffer(const char* desc, int vcount);
    virtual IndexBuffer* createIndexBuffer(int bytesPerIndex, int icount);
    virtual Shader* createShader(int id, const char* signature,
                                 const char* uniformDescriptor, const char* textureDescriptor,
                                 const char* vertexDescriptor, const char* vertexShader,
                                 const char* fragmentShader);
    virtual void renderRenderTarget(Scene*, jobject javaSceneObject, RenderTarget* renderTarget, ShaderManager* shader_manager,
                                    RenderTexture* post_effect_render_texture_a, RenderTexture* post_effect_render_texture_b);
    virtual bool renderWithShader(RenderState& rstate, Shader* shader, RenderData* renderData, ShaderData* shaderData, int);
    virtual void updatePostEffectMesh(Mesh*);
private:
    VulkanCore* vulkanCore_;
    void renderMesh(RenderState& rstate, RenderData* render_data){}
    void renderMaterialShader(RenderState& rstate, RenderData* render_data, ShaderData *material, Shader*){}
    virtual void occlusion_cull(RenderState& rstate, std::vector<SceneObject*>& scene_objects, std::vector<RenderData*>* render_data_vector) {
        occlusion_cull_init(rstate, scene_objects, render_data_vector);

    }

};
}
#endif //FRAMEWORK_VULKANRENDERER_H
