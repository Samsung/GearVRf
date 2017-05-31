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

#include "vulkan/vulkan_wrapper.h"
#define GVR_VK_CHECK(X) if (!(X)) { LOGD("VK_CHECK Failure"); assert((X));}
#define GVR_VK_VERTEX_BUFFER_BIND_ID 0
#define GVR_VK_SAMPLE_NAME "GVR Vulkan"
#define VK_KHR_ANDROID_SURFACE_EXTENSION_NAME "VK_KHR_android_surface"

struct GVR_VK_SwapchainBuffer
{
    VkImage image;
    VkCommandBuffer cmdBuffer;
    VkImageView view;
    VkDeviceSize size;
    VkDeviceMemory mem;
};

struct GVR_VK_DepthBuffer {
    VkFormat format;
    VkImage image;
    VkDeviceMemory mem;
    VkImageView view;
};

struct GVR_VK_Vertices {
    VkBuffer buf;
    VkDeviceMemory mem;
    VkPipelineVertexInputStateCreateInfo vi;
    VkVertexInputBindingDescription      vi_bindings[1];
    VkVertexInputAttributeDescription    vi_attrs[2];
};


class VulkanCore {
public:
    // Return NULL if Vulkan inititialisation failed. NULL denotes no Vulkan support for this device.
    static VulkanCore* getInstance() {
        if (!theInstance) {
            theInstance = new VulkanCore;
        }
        if (theInstance->m_Vulkan_Initialised)
            return theInstance;
        return NULL;
    }
private:
    static VulkanCore* theInstance;
    VulkanCore() : m_pPhysicalDevices(NULL){
        m_Vulkan_Initialised = false;
        initVulkanCore();
    }
    bool CreateInstance();
    bool GetPhysicalDevices();
    void initVulkanCore();
    void InitDevice();
    void InitSwapchain(uint32_t width, uint32_t height);
    bool GetMemoryTypeFromProperties( uint32_t typeBits, VkFlags requirements_mask, uint32_t* typeIndex);
    void InitCommandbuffers();
    void InitVertexBuffers();
    void InitLayouts();
    void InitRenderPass();
    void InitPipeline();
    void InitFrameBuffers();
    void InitSync();
    void BuildCmdBuffer();

    bool m_Vulkan_Initialised;


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
    VkSurfaceFormatKHR m_surfaceFormat;

    VkSwapchainKHR m_swapchain;
    GVR_VK_SwapchainBuffer* m_swapchainBuffers;

    uint32_t m_swapchainCurrentIdx;
    uint32_t m_height;
    uint32_t m_width;
    uint32_t m_swapchainImageCount;
    VkSemaphore m_backBufferSemaphore;
    VkSemaphore m_renderCompleteSemaphore;
    VkFramebuffer* m_frameBuffers;

    VkCommandPool m_commandPool;
    GVR_VK_DepthBuffer* m_depthBuffers;
    GVR_VK_Vertices m_vertices;

    VkDescriptorSetLayout m_descriptorLayout;
    VkPipelineLayout  m_pipelineLayout;
    VkRenderPass m_renderPass;
    VkPipeline m_pipeline;

    uint8_t * texDataVulkan;
};


extern VulkanCore gvrVulkanCore;

#endif //FRAMEWORK_VULKANCORE_H
