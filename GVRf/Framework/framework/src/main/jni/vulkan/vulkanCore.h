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
#include "vulkan_flags.h"


#define GVR_VK_CHECK(X) if (!(X)) { FAIL("VK_CHECK Failure"); }
#define GVR_VK_VERTEX_BUFFER_BIND_ID 0
#define GVR_VK_SAMPLE_NAME "GVR Vulkan"
#define VK_KHR_ANDROID_SURFACE_EXTENSION_NAME "VK_KHR_android_surface"

namespace gvr {
class VulkanUniformBlock;

extern  void setImageLayout(VkImageMemoryBarrier imageMemoryBarrier, VkCommandBuffer cmdBuffer, VkImage image, VkImageAspectFlags aspectMask, VkImageLayout oldImageLayout, VkImageLayout newImageLayout, VkImageSubresourceRange subresourceRange,
                            VkPipelineStageFlags srcStageFlags = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VkPipelineStageFlags destStageFlags = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT);
enum ShaderType{
    VERTEX_SHADER,
    FRAGMENT_SHADER
};
enum RenderPassType{
    // Shadow RenderPass will have index from 1 to 16
    // Whereas Normal Renderpass will have index 16 + sampleCount
    SHADOW_RENDERPASS = 0, NORMAL_RENDERPASS = 16
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
class VkRenderTarget;
class RenderTarget;
class LightList;

class VulkanCore final {

public:
    // Return NULL if Vulkan inititialisation failed. NULL denotes no Vulkan support for this device.
    static VulkanCore *getInstance(ANativeWindow *newNativeWindow = nullptr) {
        if (!theInstance) {

            theInstance = new VulkanCore(newNativeWindow);
            theInstance->initVulkanCore();
        }
        if (theInstance->m_Vulkan_Initialised)
            return theInstance;
        return NULL;
    }

    void releaseInstance(){
        delete theInstance;
        theInstance = nullptr;
    }

    ~VulkanCore();

    void InitLayoutRenderData(VulkanMaterial& vkMtl, VulkanRenderData* vkdata, Shader*, LightList& lights);

    void initCmdBuffer(VkCommandBufferLevel level,VkCommandBuffer& cmdBuffer);

    bool InitDescriptorSetForRenderData(VulkanRenderer* renderer, int pass, Shader*, VulkanRenderData* vkData, LightList& lights);
    void beginCmdBuffer(VkCommandBuffer cmdBuffer);
    void BuildCmdBufferForRenderData(std::vector<RenderData *> &render_data_vector, Camera*, ShaderManager*,RenderTarget*,VkRenderTexture*, bool);
    void BuildCmdBufferForRenderDataPE(VkCommandBuffer &cmdBuffer, ShaderManager*, Camera*, RenderData* rdata, VkRenderTexture*, int);

    int waitForFence(VkFence fence);

    VkFence createFenceObject();
    VkCommandBuffer createCommandBuffer(VkCommandBufferLevel level);
    void InitPipelineForRenderData(const GVR_VK_Vertices *m_vertices, VulkanRenderData *rdata, VulkanShader* shader, int, VkRenderPass, int sampleCount);
    void submitCmdBuffer(VkFence fence, VkCommandBuffer cmdBuffer);

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

    VkRenderPass createVkRenderPass(RenderPassType render_pass_type, int sample_count = 1);

    VkPipeline getPipeline(std::string key){
        std::unordered_map<std::string, VkPipeline >::const_iterator got = pipelineHashMap.find(key);
        if(got == pipelineHashMap.end())
            return 0;
        else
            return got->second;
    }

    void addPipeline(std::string key, VkPipeline pipeline){
        pipelineHashMap[key] = pipeline;
    }
    void InitCommandPools();
    VkCommandPool getCommandPool(){
        return m_commandPool;
    }

    void renderToOculus(RenderTarget* renderTarget);
    void InitSwapChain();

    VkImage getSwapChainImage(){
        return mSwapchainBuffers[swapChainImageIndex].image;
    }

    VkImageView getSwapChainView(){
        return mSwapchainBuffers[swapChainImageIndex++].view;
    }

    bool isSwapChainPresent(){
        return swapChainFlag;
    }
    int getSwapChainIndexToRender(){
        return mSwapchainCurrentIdx;
    }
    void SetNextBackBuffer();
    void PresentBackBuffer();
private:

    static VulkanCore *theInstance;
    std::unordered_map<std::string, VkPipeline> pipelineHashMap;

    explicit VulkanCore(ANativeWindow *newNativeWindow) : m_pPhysicalDevices(NULL){
        m_Vulkan_Initialised = false;
        initVulkanDevice(newNativeWindow);
    }

    bool CreateInstance();
    bool GetPhysicalDevices();


    void initVulkanDevice(ANativeWindow *newNativeWindow);

    bool InitDevice();

    void InitSurface();

    void InitSync();

    void createPipelineCache();

    bool m_Vulkan_Initialised;

    std::vector <uint32_t> CompileShader(const std::string &shaderName,
                                         ShaderType shaderTypeID,
                                         const std::string &shaderContents);
    void InitShaders(VkPipelineShaderStageCreateInfo shaderStages[],
                     std::vector<uint32_t>& result_vert, std::vector<uint32_t>& result_frag);

    void GetDescriptorPool(VkDescriptorPool& descriptorPool);
    VkCullModeFlagBits getVulkanCullFace(int);

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
    VkSurfaceFormatKHR mSurfaceFormat;
    VkSwapchainKHR mSwapchain;
    struct SwapchainBuffer
    {
        VkImage image;
        VkImageView view;
    };

    int swapChainImageIndex = 0;
    SwapchainBuffer* mSwapchainBuffers;
    bool swapChainFlag = false;
    // Vulkan Synchronization objects
    VkSemaphore mBackBufferSemaphore;
    VkSemaphore mRenderCompleteSemaphore;

    uint32_t mSwapchainCurrentIdx = 0;
    uint32_t mSwapchainImageCount;

    VkCommandPool m_commandPool;
    VkCommandPool m_commandPoolTrans;

    VkPipelineCache m_pipelineCache;
    std::unordered_map<int, VkRenderPass> mRenderPassMap;
};
}
#endif //FRAMEWORK_VULKANCORE_H
