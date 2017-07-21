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


#ifndef FRAMEWORK_VULKANCORE_H
#define FRAMEWORK_VULKANCORE_H

#include "util/gvr_log.h"
#include <android/native_window_jni.h>	// for native window JNI
#include <string>
#include <unordered_map>
#include "objects/components/camera.h"
#include "vk_texture.h"

#define GVR_VK_CHECK(X) if (!(X)) { LOGD("VK_CHECK Failure"); assert((X));}
#define GVR_VK_VERTEX_BUFFER_BIND_ID 0
#define GVR_VK_SAMPLE_NAME "GVR Vulkan"
#define VK_KHR_ANDROID_SURFACE_EXTENSION_NAME "VK_KHR_android_surface"
#define SWAP_CHAIN_COUNT 6
#define POSTEFFECT_CHAIN_COUNT 2

namespace gvr {
class VulkanUniformBlock;


extern  void setImageLayout(VkImageMemoryBarrier imageMemoryBarrier, VkCommandBuffer cmdBuffer, VkImage image, VkImageAspectFlags aspectMask, VkImageLayout oldImageLayout, VkImageLayout newImageLayout, VkImageSubresourceRange subresourceRange,
                            VkPipelineStageFlags srcStageFlags = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VkPipelineStageFlags destStageFlags = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT);
enum ShaderType{
    VERTEX_SHADER,
    FRAGMENT_SHADER
};
enum RenderPassType{
    SHADOW_RENDERPASS = 0, NORMAL_RENDERPASS
};
extern std::vector<uint64_t> samplers;
extern VkSampler getSampler(uint64_t index);

extern VkSampleCountFlagBits getVKSampleBit(int sampleCount);
extern VkRenderPass* createVkRenderPass(RenderPassType);
struct TextureObject{
    VkSampler m_sampler;
    VkImage m_image;
    VkImageView m_view;
    VkDeviceMemory m_mem;
    VkFormat m_format;
    VkImageLayout m_imageLayout;
    uint32_t m_width;
    uint32_t m_height;
    VkImageType m_textureType;
    VkImageViewType m_textureViewType;
    uint8_t *m_data;
};

class Scene;
class ShaderManager;
class RenderData;
class VulkanRenderData;
class Camera;
class VulkanData;
class VulkanMaterial;
class VulkanRenderer;
class UniformBlock;
class Shader;
class VKFramebuffer;
extern uint8_t *oculusTexData;
class VkRenderTexture;
class VulkanShader;
class VulkanCore {
public:
    // Return NULL if Vulkan inititialisation failed. NULL denotes no Vulkan support for this device.
    static VulkanCore *getInstance(ANativeWindow *newNativeWindow = nullptr) {
        if (!theInstance) {
            theInstance = new VulkanCore(newNativeWindow);
        }
        if (theInstance->m_Vulkan_Initialised)
            return theInstance;
        return NULL;
    }
    void InitLayoutRenderData(VulkanMaterial& vkMtl, VulkanRenderData* vkdata, Shader*, bool postEffectFlag);

    void initCmdBuffer(VkCommandBufferLevel level,VkCommandBuffer& cmdBuffer);

    bool InitDescriptorSetForRenderData(VulkanRenderer* renderer, int pass, Shader*, VulkanRenderData* vkData);
    bool InitDescriptorSetForRenderDataPostEffect(VulkanRenderer* renderer, int pass, Shader*, VulkanRenderData* vkData, int postEffectIndx);


    void BuildCmdBufferForRenderData(std::vector<RenderData *> &render_data_vector, Camera*, ShaderManager*);
    void BuildCmdBufferForRenderDataPE(Camera*, RenderData* rdata, Shader* shader, int postEffectIndx);

    int DrawFrameForRenderData();
    int DrawFrameForRenderDataPE(int);
    int getCurrentSwapChainIndx(){
        return imageIndex;
    }
    VkFence getCurrentWaitFence(){
        return waitSCBFences[imageIndex];
    }
    VkCommandBuffer* getCurrentCmdBuffer(){
        return swapChainCmdBuffer[imageIndex];
    }

    VkCommandBuffer* getCurrentCmdBufferPE(int indx){
        return postEffectCmdBuffer[indx];
    }
    int AcquireNextImage();

    void InitPipelineForRenderData(const GVR_VK_Vertices *m_vertices, VulkanRenderData *rdata, VulkanShader* shader, int, bool poastEffect, int postEffectIndx);

    bool GetMemoryTypeFromProperties(uint32_t typeBits, VkFlags requirements_mask,
                                     uint32_t *typeIndex);

    VkDevice &getDevice() {
        return m_device;
    }

    VkPhysicalDevice& getPhysicalDevice(){
        return m_physicalDevice;
    }

    VkQueue &getVkQueue() {
        return m_queue;
    }

    void createTransientCmdBuffer(VkCommandBuffer&);

    VkCommandPool &getTransientCmdPool() {
        return m_commandPoolTrans;
    }

    void initVulkanCore();
    bool swapChainCreated(){
        return swap_chain_init_;
    }
    VkRenderPass createVkRenderPass(RenderPassType render_pass_type, int sample_count = 1);
    void RenderToOculus(int index, int postEffectFlag);
    void InitPostEffectChain();
private:
    std::vector <VkFence> waitFences;
    VkFence postEffectFence;
    std::vector <VkFence> waitSCBFences;
    static VulkanCore *theInstance;

    bool swap_chain_init_;
    VulkanCore(ANativeWindow *newNativeWindow) : m_pPhysicalDevices(NULL),swap_chain_init_(false) {
        m_Vulkan_Initialised = false;
        initVulkanDevice(newNativeWindow);

    }

    bool CreateInstance();
    bool GetPhysicalDevices();


    void initVulkanDevice(ANativeWindow *newNativeWindow);

    bool InitDevice();

    void InitSurface();

    void InitSwapchain(uint32_t width, uint32_t height);


    void InitCommandbuffers();

    void InitTransientCmdPool();

    void InitSync();

    void createPipelineCache();
    void InitTexture();
    VkCommandBuffer textureCmdBuffer;

    bool m_Vulkan_Initialised;

    std::vector <uint32_t> CompileShader(const std::string &shaderName,
                                         ShaderType shaderTypeID,
                                         const std::string &shaderContents);
    void InitShaders(VkPipelineShaderStageCreateInfo shaderStages[],
                     std::vector<uint32_t>& result_vert, std::vector<uint32_t>& result_frag);
    void CreateSampler(TextureObject * &textureObject);
    VkDescriptorPool GetDescriptorPool();

    ANativeWindow *m_androidWindow;

    VkInstance m_instance;
    VkPhysicalDevice *m_pPhysicalDevices;
    VkPhysicalDevice m_physicalDevice;
    VkPhysicalDeviceProperties m_physicalDeviceProperties;
    VkPhysicalDeviceMemoryProperties m_physicalDeviceMemoryProperties;
    VkDevice m_device;
    uint32_t m_physicalDeviceCount;
    uint32_t m_queueFamilyIndex;
    VkQueue m_queue;
    VkSurfaceKHR m_surface;

    std::vector<VkCommandBuffer*> swapChainCmdBuffer;
    std::vector<VkCommandBuffer*> postEffectCmdBuffer;

    uint32_t m_height;
    uint32_t m_width;

    VkSemaphore m_backBufferSemaphore;
    VkSemaphore m_renderCompleteSemaphore;

    VkCommandPool m_commandPool;
    VkCommandPool m_commandPoolTrans;

    int imageIndex = 0;

    VkPipelineCache m_pipelineCache;

    TextureObject * textureObject;

    VkRenderTexture* mRenderTexture[SWAP_CHAIN_COUNT];
    VkRenderTexture* mPostEffectTexture[POSTEFFECT_CHAIN_COUNT];
    VkRenderPass mRenderPassMap[2];
};
}
#endif //FRAMEWORK_VULKANCORE_H
