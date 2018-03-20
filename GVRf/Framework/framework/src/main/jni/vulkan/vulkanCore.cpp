/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");x
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

#include <thread>
#include <iostream>
#include <shaderc/shaderc.hpp>
#include "objects/scene.h"
#include "gvr_time.h"
#include "vulkan_render_data.h"
#include "vulkan_material.h"
#include "vulkan/vk_framebuffer.h"
#include "vulkan/vk_render_to_texture.h"
#include "vk_imagebase.h"
#include "vk_render_target.h"
#include "vk_render_texture_offscreen.h"
#include "vulkanCore.h"
#include <array>

#define TEXTURE_BIND_START 4
#define QUEUE_INDEX_MAX 99999
#define VERTEX_BUFFER_BIND_ID 0
namespace gvr {

    std::vector<uint64_t> samplers;
    VulkanCore *VulkanCore::theInstance = NULL;
    uint8_t *oculusTexData;
    VkSampleCountFlagBits getVKSampleBit(int sampleCount){
        switch (sampleCount){
            case 1:
                return VK_SAMPLE_COUNT_1_BIT;
            case 2:
                return  VK_SAMPLE_COUNT_2_BIT;
            case 4:
                return  VK_SAMPLE_COUNT_4_BIT;
            case 8:
                return VK_SAMPLE_COUNT_8_BIT;
        }
    }
    void VulkanDescriptor::createDescriptor(VulkanCore *vk, int index,
                                            VkShaderStageFlagBits shaderStageFlagBits) {
        //createBuffer(device, vk, ubo, index);
        createLayoutBinding(index, shaderStageFlagBits);

    }

    void VulkanDescriptor::createLayoutBinding(int binding_index, int stageFlags, bool sampler) {
        VkDescriptorType descriptorType = (sampler ? VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
                                                   : VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC);

        gvr::DescriptorLayout layout(binding_index, 1, descriptorType,
                                     stageFlags, 0);
        layout_binding = *layout;
    }

    VkDescriptorSetLayoutBinding &VulkanDescriptor::getLayoutBinding() {
        return layout_binding;
    }

    void setImageLayout(VkImageMemoryBarrier imageMemoryBarrier, VkCommandBuffer cmdBuffer,
                        VkImage image, VkImageAspectFlags aspectMask, VkImageLayout oldImageLayout,
                        VkImageLayout newImageLayout, VkImageSubresourceRange subresourceRange,
                        VkPipelineStageFlags srcStageFlags, VkPipelineStageFlags destStageFlags) {
        // update image barrier for image layouts
        imageMemoryBarrier.oldLayout = oldImageLayout;
        imageMemoryBarrier.newLayout = newImageLayout;
        imageMemoryBarrier.image = image;
        imageMemoryBarrier.subresourceRange = subresourceRange;

        switch (oldImageLayout) {
            case VK_IMAGE_LAYOUT_UNDEFINED:
                imageMemoryBarrier.srcAccessMask = 0;
                break;
            case VK_IMAGE_LAYOUT_PREINITIALIZED:
                imageMemoryBarrier.srcAccessMask = VK_ACCESS_HOST_WRITE_BIT;
                break;
            case VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL:
                imageMemoryBarrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
                break;
        }

        switch (newImageLayout) {
            case VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL:
                imageMemoryBarrier.dstAccessMask = VK_ACCESS_TRANSFER_READ_BIT;
                break;
            case VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL:
                imageMemoryBarrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
                break;
            case VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL:
                imageMemoryBarrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT;
                break;
        }

        vkCmdPipelineBarrier(
                cmdBuffer,
                srcStageFlags,
                destStageFlags,
                0,
                0, nullptr,
                0, nullptr,
                1, &imageMemoryBarrier);
    }

    bool VulkanCore::CreateInstance() {
        VkResult ret = VK_SUCCESS;

        // Discover the number of extensions listed in the instance properties in order to allocate
        // a buffer large enough to hold them.
        uint32_t instanceExtensionCount = 0;
        ret = vkEnumerateInstanceExtensionProperties(nullptr, &instanceExtensionCount, nullptr);
        GVR_VK_CHECK(!ret);

        VkBool32 surfaceExtFound = 0;
        VkBool32 platformSurfaceExtFound = 0;
        VkExtensionProperties *instanceExtensions = nullptr;
        instanceExtensions = new VkExtensionProperties[instanceExtensionCount];

        // Now request instanceExtensionCount VkExtensionProperties elements be read into out buffer
        ret = vkEnumerateInstanceExtensionProperties(nullptr, &instanceExtensionCount,
                                                     instanceExtensions);
        GVR_VK_CHECK(!ret);

        // We require two extensions, VK_KHR_surface and VK_KHR_android_surface. If they are found,
        // add them to the extensionNames list that we'll use to initialize our instance with later.
        uint32_t enabledExtensionCount = 0;
        const char *extensionNames[16];
        for (uint32_t i = 0; i < instanceExtensionCount; i++) {
            if (!strcmp(VK_KHR_SURFACE_EXTENSION_NAME, instanceExtensions[i].extensionName)) {
                surfaceExtFound = 1;
                extensionNames[enabledExtensionCount++] = VK_KHR_SURFACE_EXTENSION_NAME;
            }

            if (!strcmp(VK_KHR_ANDROID_SURFACE_EXTENSION_NAME,
                        instanceExtensions[i].extensionName)) {
                platformSurfaceExtFound = 1;
                extensionNames[enabledExtensionCount++] = VK_KHR_ANDROID_SURFACE_EXTENSION_NAME;
            }
            GVR_VK_CHECK(enabledExtensionCount < 16);
        }
        if (!surfaceExtFound) {
            LOGE("vkEnumerateInstanceExtensionProperties failed to find the "
                         VK_KHR_SURFACE_EXTENSION_NAME
                         " extension.");
            return false;
        }
        if (!platformSurfaceExtFound) {
            LOGE("vkEnumerateInstanceExtensionProperties failed to find the "
                         VK_KHR_ANDROID_SURFACE_EXTENSION_NAME
                         " extension.");
            return false;
        }

        // We specify the Vulkan version our application was built with,
        // as well as names and versions for our application and engine,
        // if applicable. This allows the driver to gain insight to what
        // is utilizing the vulkan driver, and serve appropriate versions.
        VkApplicationInfo applicationInfo = {};
        applicationInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
        applicationInfo.pNext = nullptr;
        applicationInfo.pApplicationName = GVR_VK_SAMPLE_NAME;
        applicationInfo.applicationVersion = 0;
        applicationInfo.pEngineName = "VkSample";
        applicationInfo.engineVersion = 1;
        applicationInfo.apiVersion = VK_API_VERSION_1_0;

        // Creation information for the instance points to details about
        // the application, and also the list of extensions to enable.
        VkInstanceCreateInfo instanceCreateInfo = {};
        instanceCreateInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
        instanceCreateInfo.pNext = nullptr;
        instanceCreateInfo.pApplicationInfo = &applicationInfo;
        instanceCreateInfo.enabledLayerCount = 0;
        instanceCreateInfo.ppEnabledLayerNames = nullptr;
        instanceCreateInfo.enabledExtensionCount = enabledExtensionCount;
        instanceCreateInfo.ppEnabledExtensionNames = extensionNames;


        // The main Vulkan instance is created with the creation infos above.
        // We do not specify a custom memory allocator for instance creation.
        ret = vkCreateInstance(&instanceCreateInfo, nullptr, &(m_instance));

        // we can delete the list of extensions after calling vkCreateInstance
        delete[] instanceExtensions;

        // Vulkan API return values can expose further information on a failure.
        // For instance, INCOMPATIBLE_DRIVER may be returned if the API level
        // an application is built with, exposed through VkApplicationInfo, is
        // newer than the driver present on a device.
        if (ret == VK_ERROR_INCOMPATIBLE_DRIVER) {
            LOGE("Cannot find a compatible Vulkan installable client driver: vkCreateInstance Failure");
            return false;
        } else if (ret == VK_ERROR_EXTENSION_NOT_PRESENT) {
            LOGE("Cannot find a specified extension library: vkCreateInstance Failure");
            return false;
        } else {
            GVR_VK_CHECK(!ret);
        }

        return true;
    }

    bool VulkanCore::GetPhysicalDevices() {
        VkResult ret = VK_SUCCESS;

        // Query number of physical devices available
        ret = vkEnumeratePhysicalDevices(m_instance, &(m_physicalDeviceCount), nullptr);
        GVR_VK_CHECK(!ret);

        if (m_physicalDeviceCount == 0) {
            LOGE("No physical devices detected.");
            return false;
        }

        // Allocate space the the correct number of devices, before requesting their data
        m_pPhysicalDevices = new VkPhysicalDevice[m_physicalDeviceCount];
        ret = vkEnumeratePhysicalDevices(m_instance, &(m_physicalDeviceCount), m_pPhysicalDevices);
        GVR_VK_CHECK(!ret);

        // For purposes of this sample, we simply use the first device.
        m_physicalDevice = m_pPhysicalDevices[0];

        // By querying the device properties, we learn the device name, amongst
        // other details.
        vkGetPhysicalDeviceProperties(m_physicalDevice, &(m_physicalDeviceProperties));

        LOGI("Vulkan Device: %s", m_physicalDeviceProperties.deviceName);
        LOGI("Vulkan Device: Push Constant limitations %u", m_physicalDeviceProperties.limits.maxPushConstantsSize);

        // Get Memory information and properties - this is required later, when we begin
        // allocating buffers to store data.
        vkGetPhysicalDeviceMemoryProperties(m_physicalDevice, &(m_physicalDeviceMemoryProperties));

        return true;
    }

    void VulkanCore::InitSurface() {
        VkResult ret = VK_SUCCESS;
        VkAndroidSurfaceCreateInfoKHR surfaceCreateInfo = {};
        surfaceCreateInfo.sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR;
        surfaceCreateInfo.pNext = nullptr;
        surfaceCreateInfo.flags = 0;
        surfaceCreateInfo.window = m_androidWindow;
        ret = vkCreateAndroidSurfaceKHR(m_instance, &surfaceCreateInfo, nullptr, &m_surface);
        GVR_VK_CHECK(!ret);
    }

    void VulkanCore::InitSwapChain(){
        VkResult ret = VK_SUCCESS;

        uint32_t formatCount;
        ret = vkGetPhysicalDeviceSurfaceFormatsKHR(m_physicalDevice, m_surface, &formatCount, nullptr);
        GVR_VK_CHECK(!ret);

        VkSurfaceFormatKHR *surfFormats = new VkSurfaceFormatKHR[formatCount];
        ret = vkGetPhysicalDeviceSurfaceFormatsKHR(m_physicalDevice, m_surface, &formatCount, surfFormats);
        GVR_VK_CHECK(!ret);

        if (formatCount == 1 && surfFormats[0].format == VK_FORMAT_UNDEFINED) {
            mSurfaceFormat.format = VK_FORMAT_B8G8R8A8_UNORM;
            mSurfaceFormat.colorSpace = surfFormats[0].colorSpace;
        }
        else {
            mSurfaceFormat = surfFormats[0];
        }

        delete[] surfFormats;

        VkSurfaceCapabilitiesKHR surfaceCapabilities;
        ret = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(m_physicalDevice, m_surface, &surfaceCapabilities);
        GVR_VK_CHECK(!ret);

        VkSwapchainCreateInfoKHR swapchainCreateInfo = {};
            swapchainCreateInfo.sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
            swapchainCreateInfo.surface = m_surface;
            swapchainCreateInfo.minImageCount = surfaceCapabilities.maxImageCount;
            swapchainCreateInfo.imageFormat = mSurfaceFormat.format;
            swapchainCreateInfo.imageColorSpace = mSurfaceFormat.colorSpace;
            swapchainCreateInfo.imageExtent.width = surfaceCapabilities.currentExtent.width;
            swapchainCreateInfo.imageExtent.height = surfaceCapabilities.currentExtent.height;
            swapchainCreateInfo.imageUsage = surfaceCapabilities.supportedUsageFlags;
            swapchainCreateInfo.preTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
            swapchainCreateInfo.imageArrayLayers = 1;
            swapchainCreateInfo.imageSharingMode = VK_SHARING_MODE_EXCLUSIVE;
            swapchainCreateInfo.compositeAlpha = VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR;
            swapchainCreateInfo.presentMode = VK_PRESENT_MODE_FIFO_KHR;
            swapchainCreateInfo.clipped = VK_TRUE;

        ret = vkCreateSwapchainKHR(m_device, &swapchainCreateInfo, nullptr, &mSwapchain);
        GVR_VK_CHECK(!ret);

        ret = vkGetSwapchainImagesKHR(m_device, mSwapchain, &mSwapchainImageCount, nullptr);
        GVR_VK_CHECK(!ret);

        LOGI("Swapchain Image Count: %d  and %d  %d\n", mSwapchainImageCount, surfaceCapabilities.currentExtent.width, surfaceCapabilities.currentExtent.height);

        VkImage *pSwapchainImages = new VkImage[mSwapchainImageCount];
        ret = vkGetSwapchainImagesKHR(m_device, mSwapchain, &mSwapchainImageCount, pSwapchainImages);
        GVR_VK_CHECK(!ret);

        mSwapchainBuffers = new SwapchainBuffer[mSwapchainImageCount];
        GVR_VK_CHECK(mSwapchainBuffers);

        VkImageViewCreateInfo imageViewCreateInfo = {};
            imageViewCreateInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
            imageViewCreateInfo.pNext = nullptr;
            imageViewCreateInfo.format = mSurfaceFormat.format;
            imageViewCreateInfo.components.r = VK_COMPONENT_SWIZZLE_R;
            imageViewCreateInfo.components.g = VK_COMPONENT_SWIZZLE_G;
            imageViewCreateInfo.components.b = VK_COMPONENT_SWIZZLE_B;
            imageViewCreateInfo.components.a = VK_COMPONENT_SWIZZLE_A;
            imageViewCreateInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            imageViewCreateInfo.subresourceRange.baseMipLevel = 0;
            imageViewCreateInfo.subresourceRange.levelCount = 1;
            imageViewCreateInfo.subresourceRange.baseArrayLayer = 0;
            imageViewCreateInfo.subresourceRange.layerCount = 1;
            imageViewCreateInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;
            imageViewCreateInfo.flags = 0;


        for (uint32_t i = 0; i < mSwapchainImageCount; i++){
            mSwapchainBuffers[i].image = pSwapchainImages[i];
            imageViewCreateInfo.image = pSwapchainImages[i];

            VkResult err = vkCreateImageView(m_device, &imageViewCreateInfo, nullptr, &mSwapchainBuffers[i].view);
            GVR_VK_CHECK(!err);
        }

        swapChainFlag = true;
        delete [] pSwapchainImages;
    }

    void VulkanCore::InitSync()
    {
        VkResult ret = VK_SUCCESS;

        VkSemaphoreCreateInfo semaphoreCreateInfo = {};
        semaphoreCreateInfo.sType               = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
        semaphoreCreateInfo.pNext               = nullptr;
        semaphoreCreateInfo.flags               = 0;
        ret = vkCreateSemaphore(m_device, &semaphoreCreateInfo, nullptr, &mBackBufferSemaphore);
        GVR_VK_CHECK(!ret);

        ret = vkCreateSemaphore(m_device, &semaphoreCreateInfo, nullptr, &mRenderCompleteSemaphore);
        GVR_VK_CHECK(!ret);
    }

    void VulkanCore::SetNextBackBuffer()
    {
        VkResult ret = VK_SUCCESS;

        ret  = vkAcquireNextImageKHR(m_device, mSwapchain, UINT64_MAX, mBackBufferSemaphore, VK_NULL_HANDLE, &mSwapchainCurrentIdx);
        if (ret == VK_ERROR_OUT_OF_DATE_KHR)
        {
            LOGW("VK_ERROR_OUT_OF_DATE_KHR not handled in sample");
        }
        else if (ret == VK_SUBOPTIMAL_KHR)
        {
            LOGW("VK_SUBOPTIMAL_KHR not handled in sample");
        }
        GVR_VK_CHECK(!ret);
    }

    void VulkanCore::PresentBackBuffer()
    {
        VkResult ret = VK_SUCCESS;
        VkPresentInfoKHR presentInfo = {};
        presentInfo.sType                       = VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
        presentInfo.swapchainCount              = 1;
        presentInfo.pSwapchains                 = &mSwapchain;
        presentInfo.pImageIndices               = &mSwapchainCurrentIdx;
        presentInfo.waitSemaphoreCount          = 1;
        presentInfo.pWaitSemaphores             = &mRenderCompleteSemaphore;

        ret = vkQueuePresentKHR(m_queue, &presentInfo);
        GVR_VK_CHECK(!ret);
        SetNextBackBuffer();
    }

    bool VulkanCore::InitDevice() {
        VkResult ret = VK_SUCCESS;
        // Akin to when creating the instance, we can query extensions supported by the physical device
        // that we have selected to use.
        uint32_t deviceExtensionCount = 0;
        VkExtensionProperties *device_extensions = nullptr;
        ret = vkEnumerateDeviceExtensionProperties(m_physicalDevice, nullptr, &deviceExtensionCount,
                                                   nullptr);
        GVR_VK_CHECK(!ret);

        VkBool32 swapchainExtFound = 0;
        VkExtensionProperties *deviceExtensions = new VkExtensionProperties[deviceExtensionCount];
        ret = vkEnumerateDeviceExtensionProperties(m_physicalDevice, nullptr, &deviceExtensionCount,
                                                   deviceExtensions);
        GVR_VK_CHECK(!ret);

        // For our example, we require the swapchain extension, which is used to present backbuffers efficiently
        // to the users screen.
        uint32_t enabledExtensionCount = 0;
        const char *extensionNames[16] = {0};
        for (uint32_t i = 0; i < deviceExtensionCount; i++) {
            if (!strcmp(VK_KHR_SWAPCHAIN_EXTENSION_NAME, deviceExtensions[i].extensionName)) {
                swapchainExtFound = 1;
                extensionNames[enabledExtensionCount++] = VK_KHR_SWAPCHAIN_EXTENSION_NAME;
            }
            GVR_VK_CHECK(enabledExtensionCount < 16);
        }
        if (!swapchainExtFound) {
            LOGE("vkEnumerateDeviceExtensionProperties failed to find the "
                         VK_KHR_SWAPCHAIN_EXTENSION_NAME
                         " extension: vkCreateInstance Failure");

            // Always attempt to enable the swapchain
            extensionNames[enabledExtensionCount++] = VK_KHR_SWAPCHAIN_EXTENSION_NAME;
        }



        // Before we create our main Vulkan device, we must ensure our physical device
        // has queue families which can perform the actions we require. For this, we request
        // the number of queue families, and their properties.
        uint32_t queueFamilyCount = 0;
        vkGetPhysicalDeviceQueueFamilyProperties(m_physicalDevice, &queueFamilyCount, nullptr);

        VkQueueFamilyProperties *queueProperties = new VkQueueFamilyProperties[queueFamilyCount];
        vkGetPhysicalDeviceQueueFamilyProperties(m_physicalDevice, &queueFamilyCount,
                                                 queueProperties);
        GVR_VK_CHECK(queueFamilyCount >= 1);

        // We query each queue family in turn for the ability to support the android surface
        // that was created earlier. We need the device to be able to present its images to
        // this surface, so it is important to test for this.
        VkBool32 *supportsPresent = new VkBool32[queueFamilyCount];
        for (uint32_t i = 0; i < queueFamilyCount; i++) {
            vkGetPhysicalDeviceSurfaceSupportKHR(m_physicalDevice, i, m_surface,
                                                 &supportsPresent[i]);
        }


        // Search for a graphics queue, and ensure it also supports our surface. We want a
        // queue which can be used for both, as to simplify operations.
        uint32_t queueIndex = QUEUE_INDEX_MAX;
        for (uint32_t i = 0; i < queueFamilyCount; i++) {
            if ((queueProperties[i].queueFlags & VK_QUEUE_GRAPHICS_BIT) != 0) {
                if (supportsPresent[i] == VK_TRUE) {
                    queueIndex = i;
                    break;
                }
            }
        }

        delete[] supportsPresent;
        delete[] queueProperties;

        if (queueIndex == QUEUE_INDEX_MAX) {
            GVR_VK_CHECK(
                    "Could not obtain a queue family for both graphics and presentation." && 0);
            return false;
        }

        // We have identified a queue family which both supports our android surface,
        // and can be used for graphics operations.
        m_queueFamilyIndex = queueIndex;


        // As we create the device, we state we will be creating a queue of the
        // family type required. 1.0 is the highest priority and we use that.
        float queuePriorities[1] = {1.0};
        VkDeviceQueueCreateInfo deviceQueueCreateInfo = {};
        deviceQueueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
        deviceQueueCreateInfo.pNext = nullptr;
        deviceQueueCreateInfo.queueFamilyIndex = m_queueFamilyIndex;
        deviceQueueCreateInfo.queueCount = 1;
        deviceQueueCreateInfo.pQueuePriorities = queuePriorities;

        // Now we pass the queue create info, as well as our requested extensions,
        // into our DeviceCreateInfo structure.
        VkDeviceCreateInfo deviceCreateInfo = {};
        deviceCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
        deviceCreateInfo.pNext = nullptr;
        deviceCreateInfo.queueCreateInfoCount = 1;
        deviceCreateInfo.pQueueCreateInfos = &deviceQueueCreateInfo;
        deviceCreateInfo.enabledLayerCount = 0;
        deviceCreateInfo.ppEnabledLayerNames = nullptr;
        deviceCreateInfo.enabledExtensionCount = enabledExtensionCount;
        deviceCreateInfo.ppEnabledExtensionNames = extensionNames;

        // Create the device.
        ret = vkCreateDevice(m_physicalDevice, &deviceCreateInfo, nullptr, &m_device);
        GVR_VK_CHECK(!ret);

        // Obtain the device queue that we requested.
        vkGetDeviceQueue(m_device, m_queueFamilyIndex, 0, &m_queue);
        return true;
    }

    bool VulkanCore::GetMemoryTypeFromProperties(uint32_t typeBits, VkFlags requirements_mask,
                                                 uint32_t *typeIndex) {
        GVR_VK_CHECK(typeIndex != nullptr);
        // Search memtypes to find first index with those properties
        for (uint32_t i = 0; i < 32; i++) {
            if ((typeBits & 1) == 1) {
                // Type is available, does it match user properties?
                if ((m_physicalDeviceMemoryProperties.memoryTypes[i].propertyFlags &
                     requirements_mask) == requirements_mask) {
                    *typeIndex = i;
                    return true;
                }
            }
            typeBits >>= 1;
        }
        // No memory types matched, return failure
        return false;
    }

void VulkanCore::InitCommandPools(){
    VkResult ret = VK_SUCCESS;

    ret = vkCreateCommandPool(
            m_device,
            gvr::CmdPoolCreateInfo(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT, m_queueFamilyIndex),
            nullptr, &m_commandPoolTrans
    );

    GVR_VK_CHECK(!ret);

    ret = vkCreateCommandPool(
            m_device,
            gvr::CmdPoolCreateInfo(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT,
                                   m_queueFamilyIndex),
            nullptr, &m_commandPool
    );
    GVR_VK_CHECK(!ret);
}
    void VulkanCore::createTransientCmdBuffer(VkCommandBuffer &cmdBuff) {
        VkResult ret = VK_SUCCESS;
        ret = vkAllocateCommandBuffers(
                m_device,
                gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, m_commandPoolTrans),
                &cmdBuff
        );
        GVR_VK_CHECK(!ret);
    }

    void VulkanCore::initCmdBuffer(VkCommandBufferLevel level, VkCommandBuffer &cmdBuffer) {

        VkResult ret = VK_SUCCESS;
        ret = vkAllocateCommandBuffers(
                m_device,
                gvr::CmdBufferCreateInfo(level, m_commandPool),
                &cmdBuffer
        );

        GVR_VK_CHECK(!ret);
    }

    void VulkanCore::InitLayoutRenderData(VulkanMaterial &vkMtl, VulkanRenderData* vkdata, Shader *shader) {

        const DataDescriptor& textureDescriptor = shader->getTextureDescriptor();
        DataDescriptor &uniformDescriptor = shader->getUniformDescriptor();
        bool transformUboPresent = shader->usesMatrixUniforms();
        VulkanShader* vk_shader = static_cast<VulkanShader*>(shader);
        if (!shader->isShaderDirty()) {
            return;
        }

        if ((textureDescriptor.getNumEntries() == 0) && uniformDescriptor.getNumEntries() == 0 && !transformUboPresent) {
            return;
        }

        VkResult ret = VK_SUCCESS;
        uint32_t index = 0;
        std::vector<VkDescriptorSetLayoutBinding> uniformAndSamplerBinding;

        vk_shader->makeLayout(vkMtl, uniformAndSamplerBinding,  index, vkdata);

        VkDescriptorSetLayout &descriptorLayout = static_cast<VulkanShader *>(shader)->getDescriptorLayout();

        ret = vkCreateDescriptorSetLayout(m_device, gvr::DescriptorSetLayoutCreateInfo(0,
                                                                                       uniformAndSamplerBinding.size(),
                                                                                       uniformAndSamplerBinding.data()),
                                          nullptr,
                                          &descriptorLayout);
        GVR_VK_CHECK(!ret);


        VkPushConstantRange pushConstantRange = {};
        pushConstantRange.offset                        = 0;
        pushConstantRange.size                          = (uint32_t) vkMtl.uniforms().getTotalSize();
        pushConstantRange.stageFlags                    = VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT;

        VkPipelineLayout &pipelineLayout = static_cast<VulkanShader *>(shader)->getPipelineLayout();
        ret = vkCreatePipelineLayout(m_device,
                                     gvr::PipelineLayoutCreateInfo(0, 1, &descriptorLayout, 1, &pushConstantRange),
                                     nullptr, &pipelineLayout);
        GVR_VK_CHECK(!ret);
        shader->setShaderDirty(false);
    }

    VkRenderPass getShadowRenderPass(VkDevice device){

        VkRenderPass renderPass;
        VkAttachmentDescription attachmentDescription{};
        attachmentDescription.format = VK_FORMAT_D32_SFLOAT;
        attachmentDescription.samples = VK_SAMPLE_COUNT_1_BIT;
        attachmentDescription.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;							// Clear depth at beginning of the render pass
        attachmentDescription.storeOp = VK_ATTACHMENT_STORE_OP_STORE;						// We will read from depth, so it's important to store the depth attachment results
        attachmentDescription.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        attachmentDescription.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        attachmentDescription.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;					// We don't care about initial layout of the attachment
        attachmentDescription.finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL;// Attachment will be transitioned to shader read at render pass end

        VkAttachmentReference depthReference = {};
        depthReference.attachment = 0;
        depthReference.layout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;			// Attachment will be used as depth/stencil during render pass

        VkSubpassDescription subpass = {};
        subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
        subpass.colorAttachmentCount = 0;													// No color attachments
        subpass.pDepthStencilAttachment = &depthReference;									// Reference to our depth attachment

        // Use subpass dependencies for layout transitions
        std::array<VkSubpassDependency, 2> dependencies;

        dependencies[0].srcSubpass = VK_SUBPASS_EXTERNAL;
        dependencies[0].dstSubpass = 0;
        dependencies[0].srcStageMask = VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
        dependencies[0].dstStageMask = VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT;
        dependencies[0].srcAccessMask = VK_ACCESS_MEMORY_READ_BIT;
        dependencies[0].dstAccessMask = VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
        dependencies[0].dependencyFlags = VK_DEPENDENCY_BY_REGION_BIT;

        dependencies[1].srcSubpass = 0;
        dependencies[1].dstSubpass = VK_SUBPASS_EXTERNAL;
        dependencies[1].srcStageMask = VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT;
        dependencies[1].dstStageMask = VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
        dependencies[1].srcAccessMask = VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
        dependencies[1].dstAccessMask = VK_ACCESS_MEMORY_READ_BIT;
        dependencies[1].dependencyFlags = VK_DEPENDENCY_BY_REGION_BIT;

        vkCreateRenderPass(device, gvr::RenderPassCreateInfo(0, (uint32_t) 1, &attachmentDescription,
                                                             1, &subpass, (uint32_t) dependencies.size(),
                                                             dependencies.data()), nullptr, &renderPass);
        return renderPass;
    }

    VkRenderPass VulkanCore::createVkRenderPass(RenderPassType render_pass_type, int sample_count){

        if(mRenderPassMap[render_pass_type + sample_count])
            return mRenderPassMap[render_pass_type + sample_count];

        if(render_pass_type == SHADOW_RENDERPASS){
            VkRenderPass render_pass = getShadowRenderPass(m_device);
            mRenderPassMap.insert(std::make_pair(render_pass_type, render_pass));
            return render_pass;
        }

        VkRenderPass renderPass;
        std::vector<VkAttachmentDescription> attachmentDescriptions = {};
        VkAttachmentDescription attachment;

        // Multisampled Attachment
        if(sample_count > 1) {
            attachment = {};
            attachment.flags = 0;
            attachment.format = VK_FORMAT_R8G8B8A8_UNORM;//.format;
            attachment.samples = getVKSampleBit(sample_count);
            attachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
            attachment.storeOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
            attachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
            attachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
            attachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
            attachment.finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;

            attachmentDescriptions.push_back(attachment);
        }

        // Color Attachment
        attachment = {};
        attachment.flags = 0;
        attachment.format = VK_FORMAT_R8G8B8A8_UNORM;//.format;
        attachment.samples = VK_SAMPLE_COUNT_1_BIT;
        attachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
        attachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
        attachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        attachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        attachment.initialLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
        attachment.finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;

        attachmentDescriptions.push_back(attachment);

        // Depth Attachment
        attachment = {};
        attachment.flags = 0;

        attachment.format = Renderer::getInstance()->useStencilBuffer() ? VK_FORMAT_D24_UNORM_S8_UINT: VK_FORMAT_D16_UNORM;
        attachment.samples = getVKSampleBit(sample_count);
        attachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
        attachment.storeOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        attachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
        attachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_STORE;
        attachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
        attachment.finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;

        attachmentDescriptions.push_back(attachment);

        // We have references to the attachment offsets, stating the layout type.
        VkAttachmentReference colorReference = {};
        colorReference.attachment = 0;
        colorReference.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;

        VkAttachmentReference resolveReference = {};
        resolveReference.attachment = 1;
        resolveReference.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;

        VkAttachmentReference depthReference = {};
        depthReference.attachment = (sample_count > 1) ? 2 : 1;
        depthReference.layout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;

        // There can be multiple subpasses in a renderpass, but this example has only one.
        // We set the color and depth references at the grahics bind point in the pipeline.
        VkSubpassDescription subpassDescription = {};
        subpassDescription.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
        subpassDescription.flags = 0;
        subpassDescription.inputAttachmentCount = 0;
        subpassDescription.pInputAttachments = nullptr;
        subpassDescription.colorAttachmentCount = 1;
        subpassDescription.pColorAttachments = &colorReference;
        subpassDescription.pResolveAttachments = (sample_count > 1) ? &resolveReference : nullptr;;
        subpassDescription.pDepthStencilAttachment = &depthReference;
        subpassDescription.preserveAttachmentCount = 0;
        subpassDescription.pPreserveAttachments = nullptr;


        VkResult ret = vkCreateRenderPass(m_device,
                           gvr::RenderPassCreateInfo(0, (uint32_t) attachmentDescriptions.size(), attachmentDescriptions.data(),
                                                     1, &subpassDescription, (uint32_t) 0,
                                                     nullptr), nullptr, &renderPass);
        GVR_VK_CHECK(!ret);
        mRenderPassMap.insert(std::make_pair(NORMAL_RENDERPASS + sample_count, renderPass));
        return renderPass;
    }
/*
 * Compile Vulkan Shader
 * shaderTypeID 1 : Vertex Shader
 * shaderTypeID 2 : Fragment Shader
 */

    std::vector<uint32_t> VulkanCore::CompileShader(const std::string &shaderName,
                                                    ShaderType shaderTypeID,
                                                    const std::string &shaderContents) {
        shaderc::Compiler compiler;
        shaderc::CompileOptions options;

        shaderc_shader_kind shaderType;

        switch (shaderTypeID) {
            case VERTEX_SHADER:
                shaderType = shaderc_glsl_default_vertex_shader;
                break;
            case FRAGMENT_SHADER:
                shaderType = shaderc_glsl_default_fragment_shader;
                break;
        }

        shaderc::SpvCompilationResult module = compiler.CompileGlslToSpv(shaderContents.c_str(),
                                                                         shaderContents.size(),
                                                                         shaderType,
                                                                         shaderName.c_str(),
                                                                         options);

        if (module.GetCompilationStatus() != shaderc_compilation_status_success) {
            LOGE("Vulkan shader unable to compile : %s", module.GetErrorMessage().c_str());
        }

        std::vector<uint32_t> result(module.cbegin(), module.cend());
        return result;
    }

    void VulkanCore::InitShaders(VkPipelineShaderStageCreateInfo shaderStages[],
                                 std::vector<uint32_t>& result_vert, std::vector<uint32_t>& result_frag) {

        // We define two shader stages: our vertex and fragment shader.
        // they are embedded as SPIR-V into a header file for ease of deployment.
        VkShaderModule module;
        VkResult err;

        err = vkCreateShaderModule(m_device, gvr::ShaderModuleCreateInfo(result_vert.data(), result_vert.size() *
                                                                                      sizeof(unsigned int)),
                                   nullptr, &module);
        GVR_VK_CHECK(!err);
        gvr::PipelineShaderStageCreateInfo shaderStageInfo = gvr::PipelineShaderStageCreateInfo(
                VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO, VK_SHADER_STAGE_VERTEX_BIT,
                module, "main");
        shaderStages[0] = *shaderStageInfo;

        err = vkCreateShaderModule(m_device, gvr::ShaderModuleCreateInfo(result_frag.data(), result_frag.size() *
                                                                                             sizeof(unsigned int)),
                                   nullptr, &module);
        GVR_VK_CHECK(!err);
        shaderStageInfo = gvr::PipelineShaderStageCreateInfo(
                VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO, VK_SHADER_STAGE_FRAGMENT_BIT,
                module, "main");
        shaderStages[1] = *shaderStageInfo;

    }


    VkPrimitiveTopology getTopology(uint32_t drawType) {
        switch (drawType) {
            case GL_POINTS:
                return VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
            case GL_TRIANGLES:
                return VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
            case GL_TRIANGLE_FAN:
                return VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN;
            case GL_TRIANGLE_STRIP:
                return VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
            case GL_LINES:
                return VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
            case GL_LINE_STRIP:
                return VK_PRIMITIVE_TOPOLOGY_LINE_STRIP;
            default:
                LOGE("incorrect Draw Type");
                return VK_PRIMITIVE_TOPOLOGY_MAX_ENUM;
        }

    }

VkFence VulkanCore::createFenceObject(){
    VkResult ret = VK_SUCCESS;
    VkFence fence;
    ret = vkCreateFence(m_device, gvr::FenceCreateInfo(), nullptr, &fence);
    GVR_VK_CHECK(!ret);
    return fence;
}
VkCommandBuffer VulkanCore::createCommandBuffer(VkCommandBufferLevel level){
    VkResult ret = VK_SUCCESS;
    VkCommandBuffer cmdBuffer;
    ret = vkAllocateCommandBuffers(m_device, gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, m_commandPool),
                                   &cmdBuffer
    );
    GVR_VK_CHECK(!ret);
    return cmdBuffer;
}

VkCullModeFlagBits VulkanCore::getVulkanCullFace(int cull_type){
    switch(cull_type){
        case 0:
                return VK_CULL_MODE_BACK_BIT;
        case 1:
                return VK_CULL_MODE_FRONT_BIT;
        case 2:
                return VK_CULL_MODE_NONE;
    }
}


void VulkanCore::InitPipelineForRenderData(const GVR_VK_Vertices* m_vertices, VulkanRenderData *rdata, VulkanShader* shader, int pass, VkRenderPass renderPass, int sampleCount) {
    VkResult err;

    // The pipeline contains all major state for rendering.

    // Our vertex input is a single vertex buffer, and its layout is defined
    // in our m_vertices object already. Use this when creating the pipeline.
    VkPipelineVertexInputStateCreateInfo vi = {};
    vi = m_vertices->vi;

    // For this example we do not do blending, so it is disabled.
    VkPipelineColorBlendAttachmentState att_state[1] = {};
    bool disable_color_depth_write = rdata->stencil_test() && (RenderData::Queue::Stencil == rdata->rendering_order());
    att_state[0].colorWriteMask = disable_color_depth_write ? 0x0 : (VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
    att_state[0].blendEnable = VK_FALSE;

    if(rdata->alpha_blend()) {
        att_state[0].blendEnable = VK_TRUE;
        att_state[0].srcColorBlendFactor = static_cast<VkBlendFactor>(vkflags::glToVulkan[rdata->source_alpha_blend_func()]);
        att_state[0].dstColorBlendFactor = static_cast<VkBlendFactor>(vkflags::glToVulkan[rdata->dest_alpha_blend_func()]);
        att_state[0].colorBlendOp = VK_BLEND_OP_ADD;
        att_state[0].srcAlphaBlendFactor = static_cast<VkBlendFactor>(vkflags::glToVulkan[rdata->source_alpha_blend_func()]);
        att_state[0].dstAlphaBlendFactor = static_cast<VkBlendFactor>(vkflags::glToVulkan[rdata->dest_alpha_blend_func()]);
        att_state[0].alphaBlendOp = VK_BLEND_OP_ADD;
    }
    std::vector<uint32_t> result_vert = shader->getVkVertexShader();
    std::vector<uint32_t> result_frag = shader->getVkFragmentShader();
    // We define two shader stages: our vertex and fragment shader.
    // they are embedded as SPIR-V into a header file for ease of deployment.
    VkPipelineShaderStageCreateInfo shaderStages[2] = {};



    InitShaders(shaderStages,result_vert,result_frag);
    // Out graphics pipeline records all state information, including our renderpass
    // and pipeline layout. We do not have any dynamic state in this example.
    VkGraphicsPipelineCreateInfo pipelineCreateInfo = {};
    pipelineCreateInfo.sType = VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;

    pipelineCreateInfo.layout = shader->getPipelineLayout();
    pipelineCreateInfo.pVertexInputState = &vi;
    pipelineCreateInfo.pInputAssemblyState = gvr::PipelineInputAssemblyStateCreateInfo(
            getTopology(rdata->draw_mode()));
    VkCullModeFlagBits cull_face = getVulkanCullFace(rdata->cull_face(pass));
    pipelineCreateInfo.pRasterizationState = gvr::PipelineRasterizationStateCreateInfo(VK_FALSE,
                                                                                       VK_FALSE,
                                                                                       VK_POLYGON_MODE_FILL,
                                                                                       cull_face,
                                                                                       (swapChainFlag ? VK_FRONT_FACE_COUNTER_CLOCKWISE : VK_FRONT_FACE_CLOCKWISE),
                                                                                       VK_FALSE,
                                                                                       0, 0, 0,
                                                                                       1.0);
    pipelineCreateInfo.pColorBlendState = gvr::PipelineColorBlendStateCreateInfo(1,
                                                                                 &att_state[0]);
    pipelineCreateInfo.pMultisampleState = gvr::PipelineMultisampleStateCreateInfo(
            getVKSampleBit(sampleCount), VK_NULL_HANDLE, VK_NULL_HANDLE, VK_NULL_HANDLE,
            VK_NULL_HANDLE, VK_NULL_HANDLE);


    bool depthWrite = (rdata->rendering_order() == RenderData::Queue::Stencil) ? false : true;

    pipelineCreateInfo.pDepthStencilState =
            gvr::PipelineDepthStencilStateCreateInfo(rdata->depth_test() ? VK_TRUE : VK_FALSE,
                                     (rdata->depth_mask()  && depthWrite)? VK_TRUE : VK_FALSE,
                                     VK_COMPARE_OP_LESS_OR_EQUAL,
                                     VK_FALSE,
                                     static_cast<VkStencilOp>(vkflags::glToVulkan[rdata->stencil_op_sfail()]),  //stencil pass
                                     static_cast<VkStencilOp>(vkflags::glToVulkan[rdata->stencil_op_dppass()]), //depth pass, stencil pass
                                     static_cast<VkStencilOp>(vkflags::glToVulkan[rdata->stencil_op_dpfail()]), //depth fail, stencil pass
                                     static_cast<VkCompareOp>(vkflags::glToVulkan[rdata->stencil_func_func()]), //compare function
                                     rdata->stencil_func_mask(), //compare mask
                                     rdata->getStencilMask(), //stencil mask
                                     rdata->stencil_func_ref(),  //reference value
                                     rdata->stencil_test());


    pipelineCreateInfo.pStages = &shaderStages[0];

    pipelineCreateInfo.renderPass = renderPass;


    pipelineCreateInfo.pDynamicState = nullptr;
    pipelineCreateInfo.stageCount = 2; //vertex and fragment
    std::vector<VkDynamicState> dynamic_states = {
            VK_DYNAMIC_STATE_VIEWPORT,
            VK_DYNAMIC_STATE_SCISSOR,
            VK_DYNAMIC_STATE_LINE_WIDTH
    };

    VkPipelineDynamicStateCreateInfo dynamic_state_create_info = {
            VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO,         // VkStructureType                                sType
            nullptr,                                                      // const void                                    *pNext
            0,                                                            // VkPipelineDynamicStateCreateFlags              flags
            static_cast<uint32_t>(dynamic_states.size()),                 // uint32_t                                       dynamicStateCount
            &dynamic_states[0]                                            // const VkDynamicState                          *pDynamicStates
    };

    pipelineCreateInfo.pDynamicState = &dynamic_state_create_info;
    VkPipeline pipeline = 0;
    LOGI("Vulkan graphics call before");
    err = vkCreateGraphicsPipelines(m_device, m_pipelineCache, 1, &pipelineCreateInfo, nullptr,
                                    &pipeline);
    GVR_VK_CHECK(!err);
    rdata->setPipeline(pipeline,pass);
    LOGI("Vulkan graphics call after");

}
    void VKFramebuffer::createFramebuffer(VkDevice& device){

        std::vector<VkImageView> attachments;
        VkResult ret;

        if(mAttachments[COLOR_IMAGE]!= nullptr){
            attachments.push_back(mAttachments[COLOR_IMAGE]->getVkImageView());
        }

        if(mAttachments[DEPTH_IMAGE]!= nullptr){
            attachments.push_back(mAttachments[DEPTH_IMAGE]->getVkImageView());
        }
        if(mRenderpass == 0 ){
            LOGE("renderpass  is not initialized");
        }

        ret = vkCreateFramebuffer(device,
                                  gvr::FramebufferCreateInfo(0, mRenderpass, attachments.size(),
                                                             attachments.data(), mWidth, mHeight,
                                                             uint32_t(1)), nullptr,
                                  &mFramebuffer);
        GVR_VK_CHECK(!ret);
    }
    VkSampler getSampler(uint64_t index){

        for(int i =0; i<samplers.size(); i = i+2){
            if(samplers[i] == index)
                return (VkSampler) samplers[i + 1];
        }
        LOGE("sampler not found");
        return  0;
    }

    void VKFramebuffer::createFrameBuffer(VkDevice& device, int image_type, int sample_count, bool monoscopic){
        VkResult ret;
        std::vector<VkImageView> attachments;
        VulkanRenderer* vk_renderer= static_cast<VulkanRenderer*>(Renderer::getInstance());

        if(sample_count > 1){
            vkImageBase *multisampledImage = new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, VK_FORMAT_R8G8B8A8_UNORM, mWidth,
                                                      mHeight, 1, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                                                      VK_IMAGE_LAYOUT_UNDEFINED, sample_count);
            multisampledImage->createImageView(false);
            mAttachments[MULTISAMPLED_IMAGE] = multisampledImage;
            attachments.push_back(multisampledImage->getVkImageView());
        }

        if(image_type & COLOR_IMAGE && mAttachments[COLOR_IMAGE]== nullptr) {
            vkImageBase *colorImage = new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, VK_FORMAT_R8G8B8A8_UNORM, mWidth,
                                                      mHeight, 1, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT|
                                                                  VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                                                      VK_IMAGE_LAYOUT_UNDEFINED, 1);

            mAttachments[COLOR_IMAGE] = colorImage;
            if(monoscopic) {
                colorImage->setVkImage(vk_renderer->getCore()->getSwapChainImage());
                colorImage->setVkImageView(vk_renderer->getCore()->getSwapChainView());
            }
            else{
                colorImage->createImageView(true);
            }

            attachments.push_back(colorImage->getVkImageView());
        }

        if(image_type & DEPTH_IMAGE && mAttachments[DEPTH_IMAGE]== nullptr){

            VkFormat depthFormat =  Renderer::getInstance()->useStencilBuffer() ? VK_FORMAT_D24_UNORM_S8_UINT: VK_FORMAT_D16_UNORM;
            vkImageBase *depthImage = new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, depthFormat, mWidth,
                                                      mHeight, 1, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT ,
                                                      VK_IMAGE_LAYOUT_UNDEFINED,sample_count);
            depthImage->createImageView(false);
            mAttachments[DEPTH_IMAGE] = depthImage;
            attachments.push_back(depthImage->getVkImageView());
        }

        if(mRenderpass == 0 ){
            LOGE("renderpass  is not initialized");
        }

        ret = vkCreateFramebuffer(device,
                                  gvr::FramebufferCreateInfo(0, mRenderpass, attachments.size(),
                                                             attachments.data(), mWidth, mHeight,
                                                             uint32_t(1)), nullptr,
                                  &mFramebuffer);
        GVR_VK_CHECK(!ret);
    }


    void VulkanCore::beginCmdBuffer(VkCommandBuffer cmdBuffer){
        // vkBeginCommandBuffer should reset the command buffer, but Reset can be called
        // to make it more explicit.
        VkResult err;
        err = vkResetCommandBuffer(cmdBuffer, 0);
        GVR_VK_CHECK(!err);

        VkCommandBufferInheritanceInfo cmd_buf_hinfo = {};
        cmd_buf_hinfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
        cmd_buf_hinfo.pNext = nullptr;
        cmd_buf_hinfo.renderPass = VK_NULL_HANDLE;
        cmd_buf_hinfo.subpass = 0;
        cmd_buf_hinfo.framebuffer = VK_NULL_HANDLE;
        cmd_buf_hinfo.occlusionQueryEnable = VK_FALSE;
        cmd_buf_hinfo.queryFlags = 0;
        cmd_buf_hinfo.pipelineStatistics = 0;

        VkCommandBufferBeginInfo cmd_buf_info = {};
        cmd_buf_info.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        cmd_buf_info.pNext = nullptr;
        cmd_buf_info.flags = 0;//VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT;
        cmd_buf_info.pInheritanceInfo = &cmd_buf_hinfo;

        // By calling vkBeginCommandBuffer, cmdBuffer is put into the recording state.
        vkBeginCommandBuffer(cmdBuffer, &cmd_buf_info);
        GVR_VK_CHECK(!err);
    }

    void VulkanCore::BuildCmdBufferForRenderData(std::vector<RenderData *> &render_data_vector,
                                                 Camera *camera, ShaderManager* shader_manager, RenderTarget* renderTarget, VkRenderTexture* postEffectRenderTexture, bool postEffectFlag) {

        VkResult err;
        // For the triangle sample, we pre-record our command buffer, as it is static.
        // We have a buffer per swap chain image, so loop over the creation process.
        VkCommandBuffer cmdBuffer;
        if(renderTarget != NULL)
            cmdBuffer= (static_cast<VkRenderTarget*>(renderTarget))->getCommandBuffer();
        else
            cmdBuffer = postEffectRenderTexture->getCommandBuffer();

        beginCmdBuffer(cmdBuffer);

        if(renderTarget!= NULL)
            renderTarget->beginRendering(Renderer::getInstance());
        else {
            postEffectRenderTexture->setBackgroundColor(camera->background_color_r(), camera->background_color_g(),camera->background_color_b(), camera->background_color_a());
            postEffectRenderTexture->beginRendering(Renderer::getInstance());
        }

        for (int j = 0; j < render_data_vector.size(); j++) {

            VulkanRenderData *rdata = static_cast<VulkanRenderData *>(render_data_vector[j]);

            for(int curr_pass = postEffectFlag ? (rdata->pass_count() - 1) : 0 ;curr_pass < rdata->pass_count(); curr_pass++) {
                VulkanShader *shader = static_cast<VulkanShader *>(shader_manager->getShader(
                        rdata->get_shader(false,curr_pass)));

                float line_width;
                ShaderData* material = rdata->pass(curr_pass)->material();
                if(!material || !material->getFloat("line_width", line_width)){
                    line_width = 1.0;
                }
                vkCmdSetLineWidth(cmdBuffer, line_width);

                rdata->render(shader,cmdBuffer,curr_pass);
           }
        }

        if(renderTarget!= NULL)
            renderTarget->endRendering(Renderer::getInstance());
        else
            postEffectRenderTexture->endRendering(Renderer::getInstance());

        // By ending the command buffer, it is put out of record mode.
        err = vkEndCommandBuffer(cmdBuffer);
        GVR_VK_CHECK(!err);
    }


    void VulkanCore::BuildCmdBufferForRenderDataPE(VkCommandBuffer &cmdBuffer, ShaderManager* shader_manager, Camera *camera, RenderData* rdataPE, VkRenderTexture* renderTexture, int pass) {
        // For the triangle sample, we pre-record our command buffer, as it is static.
        // We have a buffer per swap chain image, so loop over the creation process.

        VkResult err;
        beginCmdBuffer(cmdBuffer);

        renderTexture->setBackgroundColor(camera->background_color_r(), camera->background_color_g(),camera->background_color_b(), camera->background_color_a());
        renderTexture->beginRendering(Renderer::getInstance());

        VulkanRenderData *vkRdata = static_cast<VulkanRenderData *>(rdataPE);

        VulkanShader *shader = static_cast<VulkanShader *>(shader_manager->getShader(rdataPE->get_shader(false,pass)));
        vkRdata->render(shader,cmdBuffer,pass);

        renderTexture->endRendering(Renderer::getInstance());

        // By ending the command buffer, it is put out of record mode.
        err = vkEndCommandBuffer(cmdBuffer);
        GVR_VK_CHECK(!err);
    }

    void VulkanCore::submitCmdBuffer(VkFence fence, VkCommandBuffer cmdBuffer){
        VkResult err;
        // Get the next image to render to, then queue a wait until the image is ready
        vkResetFences(m_device, 1, &fence);

        VkSubmitInfo submitInfo = {};
        submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        submitInfo.pNext = nullptr;
        submitInfo.waitSemaphoreCount = (swapChainFlag ? 1 : 0);
        submitInfo.pWaitSemaphores = (swapChainFlag ? &mBackBufferSemaphore : nullptr);
        submitInfo.pWaitDstStageMask = nullptr;
        submitInfo.commandBufferCount = 1;
        submitInfo.pCommandBuffers = &cmdBuffer;
        submitInfo.signalSemaphoreCount = (swapChainFlag ? 1 : 0);
        submitInfo.pSignalSemaphores = (swapChainFlag ? &mRenderCompleteSemaphore : nullptr);

        err = vkQueueSubmit(m_queue, 1, &submitInfo, fence);
        GVR_VK_CHECK(!err);

        if(swapChainFlag) {
            int success = 0;
            while(success != 1){
                success = waitForFence(fence);
            }
            PresentBackBuffer();
        }
    }

     int VulkanCore::waitForFence(VkFence fence) {
        if(VK_SUCCESS == vkWaitForFences(m_device, 1, &fence, VK_TRUE,
                        4294967295U))
            return 1;

        return 0;

    }
    void VulkanCore::renderToOculus(RenderTarget* renderTarget){
        VkRenderTextureOffScreen* renderTexture = static_cast<VkRenderTextureOffScreen*>(static_cast<VkRenderTarget*>(renderTarget)->getTexture());
        renderTexture->accessRenderResult(&oculusTexData);
        renderTexture->unmapDeviceMemory();
    }

    void VulkanCore::GetDescriptorPool(VkDescriptorPool& descriptorPool){
        VkDescriptorPoolSize poolSize[3] = {};

        poolSize[0].type            = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;
        poolSize[0].descriptorCount = 5;

        poolSize[1].type            = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        poolSize[1].descriptorCount = 12;

        poolSize[2].type            = VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT;
        poolSize[2].descriptorCount = 5;

        VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = {};
        descriptorPoolCreateInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
        descriptorPoolCreateInfo.pNext = nullptr;
        descriptorPoolCreateInfo.maxSets = 1;
        descriptorPoolCreateInfo.poolSizeCount = 3;
        descriptorPoolCreateInfo.pPoolSizes = poolSize;

        VkResult err;
        err = vkCreateDescriptorPool(m_device, &descriptorPoolCreateInfo, NULL, &descriptorPool);
        GVR_VK_CHECK(!err);
    }

    bool VulkanCore::InitDescriptorSetForRenderData(VulkanRenderer* renderer, int pass, Shader* shader, VulkanRenderData* vkData) {

        const DataDescriptor& textureDescriptor = shader->getTextureDescriptor();
        DataDescriptor &uniformDescriptor = shader->getUniformDescriptor();
        bool transformUboPresent = shader->usesMatrixUniforms();
        VulkanMaterial* vkmtl = static_cast<VulkanMaterial*>(vkData->material(pass));

        if ((textureDescriptor.getNumEntries() == 0) && uniformDescriptor.getNumEntries() == 0 && !transformUboPresent) {
        //    vkData->setDescriptorSetNull(true,pass);
            return true;
        }
        VulkanShader* vkShader = static_cast<VulkanShader*>(shader);
        bool bones_present = shader->hasBones();

        std::vector<VkWriteDescriptorSet> writes;
        VkDescriptorPool descriptorPool;
        GetDescriptorPool(descriptorPool);
        VkDescriptorSetLayout &descriptorLayout = static_cast<VulkanShader *>(shader)->getDescriptorLayout();
        VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = {};
        descriptorSetAllocateInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
        descriptorSetAllocateInfo.pNext = nullptr;
        descriptorSetAllocateInfo.descriptorPool = descriptorPool;
        descriptorSetAllocateInfo.descriptorSetCount = 1;
        descriptorSetAllocateInfo.pSetLayouts = &descriptorLayout;

        VkDescriptorSet descriptorSet;
        VkResult err = vkAllocateDescriptorSets(m_device, &descriptorSetAllocateInfo, &descriptorSet);
        GVR_VK_CHECK(!err);
        vkData->setDescriptorSet(descriptorSet,pass);

        if (transformUboPresent) {
            vkData->getTransformUbo().setDescriptorSet(descriptorSet);
            writes.push_back(vkData->getTransformUbo().getDescriptorSet());
        }

        if(vkData->mesh()->hasBones() && bones_present){
            static_cast<VulkanUniformBlock*>(vkData->getBonesUbo())->setDescriptorSet(descriptorSet);
            writes.push_back(static_cast<VulkanUniformBlock*>(vkData->getBonesUbo())->getDescriptorSet());
        }

        // TODO: add shadowmap descriptor

        vkShader->bindTextures(vkmtl, writes,  descriptorSet);
        vkUpdateDescriptorSets(m_device, writes.size(), writes.data(), 0, nullptr);
        vkData->setDescriptorSetNull(false,pass);
        LOGI("Vulkan after update descriptor");
        return true;
    }
    void VulkanCore::createPipelineCache() {
        VkPipelineCacheCreateInfo pipelineCacheCreateInfo = {};
        pipelineCacheCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO;
        VkResult ret = vkCreatePipelineCache(m_device, &pipelineCacheCreateInfo, nullptr,
                                           &m_pipelineCache);
        GVR_VK_CHECK(!ret);
    }

    void VulkanCore::initVulkanDevice(ANativeWindow *newNativeWindow) {
        m_Vulkan_Initialised = true;
        m_androidWindow = newNativeWindow;
        if (InitVulkan() == 0) {
            m_Vulkan_Initialised = false;
            return;
        }

        if (CreateInstance() == false) {
            m_Vulkan_Initialised = false;
            return;
        }

        if (GetPhysicalDevices() == false) {
            m_Vulkan_Initialised = false;
            return;
        }

        if(m_androidWindow != NULL) {
            InitSurface();
        }

        if (InitDevice() == false) {
            m_Vulkan_Initialised = false;
            return;
        }

        if(m_androidWindow != NULL) {
            InitSwapChain();
            InitSync();
            SetNextBackBuffer();
        }

        createPipelineCache();
    }

    VulkanCore::~VulkanCore() {
        vkDestroyDevice(getDevice(), nullptr);
        vkDestroyInstance(m_instance, nullptr);
    }

    void VulkanCore::initVulkanCore() {
        InitCommandPools();
        LOGE("Vulkan after intialization");
    }
}