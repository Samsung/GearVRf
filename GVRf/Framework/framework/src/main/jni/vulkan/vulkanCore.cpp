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
#include <array>

#define CUSTOM_TEXTURE
#define TEXTURE_BIND_START 4
#define QUEUE_INDEX_MAX 99999
#define VERTEX_BUFFER_BIND_ID 0
std::string data_frag = std::string("") +
                        "#version 400 \n" +
                        "#extension GL_ARB_separate_shader_objects : enable \n" +
                        "#extension GL_ARB_shading_language_420pack : enable \n" +

                        "layout (std140, set = 0, binding = 1) uniform Material_ubo{\n"
                                //           "vec4 ambient_color;\n " +
                                //  "vec4 diffuse_color; \n " +
                                //    "vec4 specular_color; \n" +
                                //    "vec4 emissive_color; \n" +
                                "vec3 u_color; \n" +
                        "float u_opacity; \n" +
                        //  "float specular_exponent;\n" +
                        //  "float line_width;\n" +
                        "};\n"

                                " layout(set = 0, binding = 2) uniform sampler2D tex;\n" +
                        "layout (location = 0) out vec4 uFragColor;  \n" +
                        "layout(location = 1 )in vec2 o_texcoord; \n" +
                        "void main() {  \n" +
                        //      " vec4 temp = vec4(1.0,0.0,1.0,1.0);\n" +
                        //    "   uFragColor = vec4(o_texcoord, 0, 1);  \n" +
                        "   uFragColor = texture(tex, o_texcoord);  \n" +
                        //            "   uFragColor = vec4(u_color.x, 0, u_color.y,1.0); ;  \n" +
                        "}";


std::string vertexShaderData = std::string("") +
                               "#version 400 \n" +
                               "#extension GL_ARB_separate_shader_objects : enable \n" +
                               "#extension GL_ARB_shading_language_420pack : enable \n" +
                               "layout (std140, set = 0, binding = 0) uniform Transform_ubo { "
                             // "layout(std140, push_constant) uniform Transform_ubo { "
                                       "mat4 u_view;\n"
                                       "     mat4 u_mvp;\n"
                                       "     mat4 u_mv;\n"
                                       "     mat4 u_mv_it;"
                                       " mat4 u_model;\n"
                                       "     mat4 u_view_i;\n"
                                       "     float u_right;"
                                       " };\n" +
                               "layout(location = 0)in vec3 pos; \n" +
                               "layout(location = 1)in vec2 a_texcoord; \n" +
                               "layout(location = 1)out vec2 o_texcoord; \n" +
                               "void main() { \n" +
                               "  vec4 pos1 = vec4(pos, 1.0);\n"
                                       //            "o_texcoord = normalize((u_model * pos1).xyz); \n" +
                                       //             "o_texcoord.z = -o_texcoord.z; \n" +
                                       "o_texcoord = a_texcoord; \n" +
                               "  gl_Position = u_mvp * vec4(pos.x, pos.y, pos.z,1.0); \n" +
                               "}";

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
        // At this point, we create the android surface. This is because we want to
        // ensure our device is capable of working with the created surface object.
        VkAndroidSurfaceCreateInfoKHR surfaceCreateInfo = {};
        surfaceCreateInfo.sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR;
        surfaceCreateInfo.pNext = nullptr;
        surfaceCreateInfo.flags = 0;
        surfaceCreateInfo.window = m_androidWindow;
        LOGI("Vulkan Before surface creation");
        if (m_androidWindow == NULL)
            LOGI("Vulkan Before surface null");
        else
            LOGI("Vulkan Before not null surface creation");
        ret = vkCreateAndroidSurfaceKHR(m_instance, &surfaceCreateInfo, nullptr, &m_surface);
        GVR_VK_CHECK(!ret);
        LOGI("Vulkan After surface creation");
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

        //InitSurface();

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

    void VulkanCore::InitSwapchain(uint32_t width, uint32_t height) {

        m_width = width;
        m_height = height;
        swapChainCmdBuffer.reserve(SWAP_CHAIN_COUNT);
        for (int i = 0; i < SWAP_CHAIN_COUNT; i++) {
            mRenderTexture[i] = new VkRenderTexture(width,height);
            swapChainCmdBuffer[i] = new VkCommandBuffer();
        }

    }

    void VulkanCore::InitPostEffectChain(){
        if(postEffectCmdBuffer != nullptr)
            return;

        for (int i = 0; i < POSTEFFECT_CHAIN_COUNT; i++) {
            mPostEffectTexture[i] = new VkRenderTexture(m_width, m_height);
        }

        postEffectCmdBuffer = new VkCommandBuffer();
        VkResult ret = vkAllocateCommandBuffers(
                m_device,
                gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, m_commandPool),
                postEffectCmdBuffer
        );
        GVR_VK_CHECK(!ret);
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

    void VulkanCore::InitTransientCmdPool() {
        VkResult ret = VK_SUCCESS;

        ret = vkCreateCommandPool(
                m_device,
                gvr::CmdPoolCreateInfo(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT, m_queueFamilyIndex),
                nullptr, &m_commandPoolTrans
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

    void VulkanCore::InitCommandbuffers() {
        VkResult ret = VK_SUCCESS;

        ret = vkCreateCommandPool(
                m_device,
                gvr::CmdPoolCreateInfo(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT,
                                       m_queueFamilyIndex),
                nullptr, &m_commandPool
        );

        GVR_VK_CHECK(!ret);

        // Create render command buffers, one per swapchain image

        for (int i = 0; i < SWAP_CHAIN_COUNT; i++) {
            ret = vkAllocateCommandBuffers(
                    m_device,
                    gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, m_commandPool),
                    swapChainCmdBuffer[i]
            );
            GVR_VK_CHECK(!ret);
        }

#ifdef CUSTOM_TEXTURE
        // Allocating Command Buffer for Texture
        ret = vkAllocateCommandBuffers(
                m_device,
                gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, m_commandPool),
                &textureCmdBuffer
        );
#endif
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

    void VulkanCore::InitLayoutRenderData(VulkanMaterial &vkMtl, VulkanRenderData* vkdata, Shader *shader, bool postEffectFlag) {

        const DataDescriptor& textureDescriptor = shader->getTextureDescriptor();
        DataDescriptor &uniformDescriptor = shader->getUniformDescriptor();
        bool transformUboPresent = shader->usesMatrixUniforms();
        VulkanShader* vk_shader = reinterpret_cast<VulkanShader*>(shader);
        if (!shader->isShaderDirty()) {
            return;
        }

        if ((textureDescriptor.getNumEntries() == 0) && uniformDescriptor.getNumEntries() == 0 && !transformUboPresent) {
            return;
        }

        VkResult ret = VK_SUCCESS;
        uint32_t index = 0;
        std::vector<VkDescriptorSetLayoutBinding> uniformAndSamplerBinding;

        if(postEffectFlag){
            // Has only one sampler input
            index = TEXTURE_BIND_START;
            VkDescriptorSetLayoutBinding layoutBinding;
            layoutBinding.binding = index++;
            layoutBinding.descriptorCount = 1;
            layoutBinding.descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
            layoutBinding.stageFlags = VK_SHADER_STAGE_FRAGMENT_BIT;
            layoutBinding.pImmutableSamplers = nullptr;
            (uniformAndSamplerBinding).push_back(layoutBinding);
        }
        else
        vk_shader->makeLayout(vkMtl, uniformAndSamplerBinding,  index, vkdata);

        VkDescriptorSetLayout &descriptorLayout = reinterpret_cast<VulkanShader *>(shader)->getDescriptorLayout();

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

        VkPipelineLayout &pipelineLayout = reinterpret_cast<VulkanShader *>(shader)->getPipelineLayout();
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

        if(mRenderPassMap[render_pass_type])
            return mRenderPassMap[render_pass_type];

        if(render_pass_type == SHADOW_RENDERPASS){
            VkRenderPass render_pass = getShadowRenderPass(m_device);
            mRenderPassMap[SHADOW_RENDERPASS] = render_pass;
            return render_pass;
        }

        VkRenderPass renderPass;
        VkAttachmentDescription attachmentDescriptions[2] = {};
        attachmentDescriptions[0] = {};
        attachmentDescriptions[0].flags = 0;
        attachmentDescriptions[0].format = VK_FORMAT_R8G8B8A8_UNORM;//.format;
        attachmentDescriptions[0].samples = getVKSampleBit(sample_count);
        attachmentDescriptions[0].loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
        attachmentDescriptions[0].storeOp = VK_ATTACHMENT_STORE_OP_STORE;
        attachmentDescriptions[0].stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        attachmentDescriptions[0].stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        attachmentDescriptions[0].initialLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
        attachmentDescriptions[0].finalLayout = VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;

        attachmentDescriptions[1] = {};
        attachmentDescriptions[1].flags = 0;
        attachmentDescriptions[1].format = VK_FORMAT_D16_UNORM;
        attachmentDescriptions[1].samples = getVKSampleBit(sample_count);
        attachmentDescriptions[1].loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
        attachmentDescriptions[1].storeOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        attachmentDescriptions[1].stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        attachmentDescriptions[1].stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        attachmentDescriptions[1].initialLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
        attachmentDescriptions[1].finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;

        // We have references to the attachment offsets, stating the layout type.
        VkAttachmentReference colorReference = {};
        colorReference.attachment = 0;
        colorReference.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;


        VkAttachmentReference depthReference = {};
        depthReference.attachment = 1;
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
        subpassDescription.pResolveAttachments = nullptr;
        subpassDescription.pDepthStencilAttachment = &depthReference;
        subpassDescription.preserveAttachmentCount = 0;
        subpassDescription.pPreserveAttachments = nullptr;

        vkCreateRenderPass(m_device,
                           gvr::RenderPassCreateInfo(0, (uint32_t) 2, attachmentDescriptions,
                                                     1, &subpassDescription, (uint32_t) 0,
                                                     nullptr), nullptr, &renderPass);
        mRenderPassMap[NORMAL_RENDERPASS] = renderPass;
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
void VulkanCore::InitPipelineForRenderData(const GVR_VK_Vertices* m_vertices, VulkanRenderData *rdata, VulkanShader* shader, int pass, bool postEffect, int postEffectIndx) {
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
        att_state[0].srcColorBlendFactor = VK_BLEND_FACTOR_ONE;
        att_state[0].dstColorBlendFactor = VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
        att_state[0].colorBlendOp = VK_BLEND_OP_ADD;
        att_state[0].srcAlphaBlendFactor = VK_BLEND_FACTOR_ONE;
        att_state[0].dstAlphaBlendFactor = VK_BLEND_FACTOR_ZERO;
        att_state[0].alphaBlendOp = VK_BLEND_OP_ADD;
    }

    VkViewport viewport = {};
    viewport.height = (float) m_height;
    viewport.width = (float) m_width;
    viewport.minDepth = (float) 0.0f;
    viewport.maxDepth = (float) 1.0f;

    VkRect2D scissor = {};
    scissor.extent.width = m_width;
    scissor.extent.height = m_height;
    scissor.offset.x = 0;
    scissor.offset.y = 0;

#if  0
    std::vector<uint32_t> result_vert = CompileShader("VulkanVS", VERTEX_SHADER,
                                                          vertexShaderData);//vs;//
    std::vector<uint32_t> result_frag = CompileShader("VulkanFS", FRAGMENT_SHADER,
                                                          data_frag);//fs;//
#else
    std::vector<uint32_t> result_vert = shader->getVkVertexShader();
    std::vector<uint32_t> result_frag = shader->getVkFragmentShader();
#endif
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
    VkCullModeFlagBits cull_face = (rdata->cull_face(pass) ==  RenderData::CullBack) ? VK_CULL_MODE_BACK_BIT : VK_CULL_MODE_FRONT_BIT;
    ShaderData *curr_material = rdata->material(pass);
    float line_width = 1.0;
    curr_material->getFloat("line_width", line_width);
    pipelineCreateInfo.pRasterizationState = gvr::PipelineRasterizationStateCreateInfo(VK_FALSE,
                                                                                       VK_FALSE,
                                                                                       VK_POLYGON_MODE_FILL,
                                                                                       cull_face,
                                                                                       VK_FRONT_FACE_CLOCKWISE,
                                                                                       VK_FALSE,
                                                                                       0, 0, 0,
                                                                                       line_width);
    pipelineCreateInfo.pColorBlendState = gvr::PipelineColorBlendStateCreateInfo(1,
                                                                                 &att_state[0]);
    pipelineCreateInfo.pMultisampleState = gvr::PipelineMultisampleStateCreateInfo(
            VK_SAMPLE_COUNT_1_BIT, VK_NULL_HANDLE, VK_NULL_HANDLE, VK_NULL_HANDLE,
            VK_NULL_HANDLE, VK_NULL_HANDLE);
    pipelineCreateInfo.pViewportState = gvr::PipelineViewportStateCreateInfo(1, &viewport, 1,
                                                                             &scissor);
    pipelineCreateInfo.pDepthStencilState = gvr::PipelineDepthStencilStateCreateInfo(rdata->depth_test() ? VK_TRUE : VK_FALSE,
                                                                                     rdata->depth_mask() ? VK_TRUE : VK_FALSE,
                                                                                     VK_COMPARE_OP_LESS_OR_EQUAL,
                                                                                     VK_FALSE,
                                                                                     VK_STENCIL_OP_KEEP,
                                                                                     VK_STENCIL_OP_KEEP,
                                                                                     VK_COMPARE_OP_ALWAYS,
                                                                                     VK_FALSE);
    pipelineCreateInfo.pStages = &shaderStages[0];
    if(!postEffect)
        pipelineCreateInfo.renderPass =(mRenderTexture[imageIndex]->getRenderPass());
    else
        pipelineCreateInfo.renderPass =(mPostEffectTexture[postEffect%2]->getRenderPass());

    pipelineCreateInfo.pDynamicState = nullptr;
    pipelineCreateInfo.stageCount = 2; //vertex and fragment
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
    void VKFramebuffer::createFrameBuffer(VkDevice& device, int image_type, int sample_count){

        VkResult ret;
        std::vector<VkImageView> attachments;
        if(image_type & COLOR_IMAGE && mAttachments[COLOR_IMAGE]== nullptr) {
            vkImageBase *colorImage = new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, VK_FORMAT_R8G8B8A8_UNORM, mWidth,
                                                      mHeight, 1, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT |
                                                                  VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                                                      VK_IMAGE_LAYOUT_UNDEFINED, sample_count);
            colorImage->createImageView(true);
            mAttachments[COLOR_IMAGE] = colorImage;
            attachments.push_back(colorImage->getVkImageView());
        }

        if(image_type & DEPTH_IMAGE && mAttachments[DEPTH_IMAGE]== nullptr){
            vkImageBase *depthImage = new vkImageBase(VK_IMAGE_VIEW_TYPE_2D, VK_FORMAT_D16_UNORM, mWidth,
                                                      mHeight, 1, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
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

    void VulkanCore::InitSync() {
        LOGI("Vulkan initsync start");
        VkResult ret = VK_SUCCESS;

        waitFences.resize(SWAP_CHAIN_COUNT);
        for (auto &fence : waitFences) {
            ret = vkCreateFence(m_device, gvr::FenceCreateInfo(), nullptr, &fence);
            GVR_VK_CHECK(!ret);
        }

        ret = vkCreateFence(m_device, gvr::FenceCreateInfo(), nullptr, &waitSCBFences);
        GVR_VK_CHECK(!ret);


        ret = vkCreateFence(m_device, gvr::FenceCreateInfo(), nullptr, &postEffectFence);
        GVR_VK_CHECK(!ret);

        LOGI("Vulkan initsync end");
    }

    void VulkanCore::BuildCmdBufferForRenderData(std::vector<RenderData *> &render_data_vector,
                                                 Camera *camera, ShaderManager* shader_manager) {
        // For the triangle sample, we pre-record our command buffer, as it is static.
        // We have a buffer per swap chain image, so loop over the creation process.
        VkCommandBuffer &cmdBuffer = *(swapChainCmdBuffer[imageIndex]);

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
        cmd_buf_info.flags = VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT;
        cmd_buf_info.pInheritanceInfo = &cmd_buf_hinfo;

        // By calling vkBeginCommandBuffer, cmdBuffer is put into the recording state.
        err = vkBeginCommandBuffer(cmdBuffer, &cmd_buf_info);
        GVR_VK_CHECK(!err);

        mRenderTexture[imageIndex]->setBackgroundColor(camera->background_color_r(), camera->background_color_g(),camera->background_color_b(), camera->background_color_a());
        mRenderTexture[imageIndex]->bind();
        mRenderTexture[imageIndex]->beginRendering(Renderer::getInstance());

        for (int j = 0; j < render_data_vector.size(); j++) {

            VulkanRenderData *rdata = reinterpret_cast<VulkanRenderData *>(render_data_vector[j]);

            for(int curr_pass =0 ;curr_pass < rdata->pass_count(); curr_pass++) {

               vkCmdBindPipeline(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                rdata->getVKPipeline(curr_pass) );

                VulkanShader *shader = reinterpret_cast<VulkanShader *>(shader_manager->getShader(
                       rdata->get_shader(false,curr_pass)));

                VkDescriptorSet descriptorSet = rdata->getDescriptorSet(curr_pass);
               //bind out descriptor set, which handles our uniforms and samplers
               if (!rdata->isDescriptorSetNull(curr_pass)) {
                   VulkanMaterial *vkmtl = static_cast<VulkanMaterial *>(rdata->material(
                           curr_pass));

                   vkCmdPushConstants(cmdBuffer, shader->getPipelineLayout(),
                                      VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT,
                                      0,
                                      (uint32_t) vkmtl->uniforms().getTotalSize(),
                                      vkmtl->uniforms().getUniformData());

                   vkCmdBindDescriptorSets(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                           shader->getPipelineLayout(), 0, 1,
                                           &descriptorSet, 0, NULL);
               }

               // Bind our vertex buffer, with a 0 offset.
               VkDeviceSize offsets[1] = {0};
               const Mesh *mesh = rdata->mesh();
                VulkanVertexBuffer *vbuf = reinterpret_cast< VulkanVertexBuffer *>(mesh->getVertexBuffer());
               const VulkanIndexBuffer *ibuf = reinterpret_cast<const VulkanIndexBuffer *>(mesh->getIndexBuffer());
               const GVR_VK_Vertices *vert = (vbuf->getVKVertices(shader));

               vkCmdBindVertexBuffers(cmdBuffer, VERTEX_BUFFER_BIND_ID, 1, &(vert->buf), offsets);

                if(ibuf && ibuf->getIndexCount()) {
                    const GVR_VK_Indices &ind = ibuf->getVKIndices();
                    VkIndexType indexType = (ibuf->getIndexSize() == 2) ? VK_INDEX_TYPE_UINT16
                                                                        : VK_INDEX_TYPE_UINT32;
                    vkCmdBindIndexBuffer(cmdBuffer, ind.buffer, 0, indexType);
                    vkCmdDrawIndexed(cmdBuffer, ind.count, 1, 0, 0, 1);
                }
                else
                    vkCmdDraw(cmdBuffer, mesh->getVertexCount(), 1, 0, 1);
           }
        }
        mRenderTexture[imageIndex]->endRendering(Renderer::getInstance());

        // By ending the command buffer, it is put out of record mode.
        err = vkEndCommandBuffer(cmdBuffer);
        GVR_VK_CHECK(!err);
    }


    void VulkanCore::BuildCmdBufferForRenderDataPE(Camera *camera, RenderData* rdataPE, Shader* shader, int postEffectIndx) {
        // For the triangle sample, we pre-record our command buffer, as it is static.
        // We have a buffer per swap chain image, so loop over the creation process.
        VkCommandBuffer &cmdBuffer = *(postEffectCmdBuffer);

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
        cmd_buf_info.flags = VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT;
        cmd_buf_info.pInheritanceInfo = &cmd_buf_hinfo;

        // By calling vkBeginCommandBuffer, cmdBuffer is put into the recording state.
        err = vkBeginCommandBuffer(cmdBuffer, &cmd_buf_info);
        GVR_VK_CHECK(!err);

        mPostEffectTexture[postEffectIndx%2]->setBackgroundColor(camera->background_color_r(), camera->background_color_g(),camera->background_color_b(), camera->background_color_a());
        mPostEffectTexture[postEffectIndx%2]->bind();
        mPostEffectTexture[postEffectIndx%2]->beginRenderingPE(Renderer::getInstance());

        // Apply Post Effects
            VulkanRenderData *vkRdata = static_cast<VulkanRenderData *>(rdataPE);

            // Set our pipeline. This holds all major state
            // the pipeline defines, for example, that the vertex buffer is a triangle list.
            vkCmdBindPipeline(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                              vkRdata->getVKPipeline(0));

            VkPipelineLayout &pipelineLayout = reinterpret_cast<VulkanShader *>(shader)->getPipelineLayout();

            VkDescriptorSet descriptorSet1 = vkRdata->getDescriptorSet(0);
            VulkanMaterial *vkmtl = (VulkanMaterial *)static_cast<VulkanMaterial *>(vkRdata->material(
                    0));
            vkCmdPushConstants(cmdBuffer, pipelineLayout,
                               VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                               0,
                               (uint32_t) vkmtl->uniforms().getTotalSize(),
                               vkmtl->uniforms().getUniformData());

            vkCmdBindDescriptorSets(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0,
                                   1, &descriptorSet1, 1, 0);

            // Bind our vertex buffer, with a 0 offset.
            VkDeviceSize offsets[1] = {0};


            VulkanVertexBuffer *vbuf = static_cast<VulkanVertexBuffer *>(vkRdata->mesh()->getVertexBuffer());
            const GVR_VK_Vertices *vertices = vbuf->getVKVertices(shader);
            vkCmdBindVertexBuffers(cmdBuffer, VERTEX_BUFFER_BIND_ID, 1, &vertices->buf,
                                   offsets);

            // Issue a draw command, with our vertices. Full screen quad
            const Mesh *mesh = vkRdata->mesh();

        const VulkanIndexBuffer *ibuf = reinterpret_cast<const VulkanIndexBuffer *>(mesh->getIndexBuffer());
        if(ibuf && ibuf->getIndexCount()) {
            const GVR_VK_Indices &ind = ibuf->getVKIndices();
            VkIndexType indexType = (ibuf->getIndexSize() == 2) ? VK_INDEX_TYPE_UINT16
                                                                : VK_INDEX_TYPE_UINT32;
            vkCmdBindIndexBuffer(cmdBuffer, ind.buffer, 0, indexType);
            vkCmdDrawIndexed(cmdBuffer, ind.count, 1, 0, 0, 1);
        }
        else
            vkCmdDraw(cmdBuffer, mesh->getVertexCount(), 1, 0, 1);


        mPostEffectTexture[postEffectIndx%2]->endRenderingPE(Renderer::getInstance());

        // By ending the command buffer, it is put out of record mode.
        err = vkEndCommandBuffer(cmdBuffer);
        GVR_VK_CHECK(!err);
    }

    int VulkanCore::AcquireNextImage() {
        imageIndex = (imageIndex + 1) % SWAP_CHAIN_COUNT;
        return imageIndex;
    }

    int VulkanCore::DrawFrameForRenderData() {

        VkResult err;
        // Get the next image to render to, then queue a wait until the image is ready
        VkFence nullFence = waitFences[imageIndex];
        vkResetFences(m_device, 1, &waitFences[imageIndex]);

        VkSubmitInfo submitInfo = {};
        submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        submitInfo.pNext = nullptr;
        submitInfo.waitSemaphoreCount = 0;
        submitInfo.pWaitSemaphores = nullptr;
        submitInfo.pWaitDstStageMask = nullptr;
        submitInfo.commandBufferCount = 1;
        submitInfo.pCommandBuffers = swapChainCmdBuffer[imageIndex];
        submitInfo.signalSemaphoreCount = 0;
        submitInfo.pSignalSemaphores = nullptr;

        err = vkQueueSubmit(m_queue, 1, &submitInfo, waitFences[imageIndex]);
        GVR_VK_CHECK(!err);

        err = vkGetFenceStatus(m_device, waitFences[imageIndex]);
        int swapChainIndx = imageIndex;
        bool found = false;
        VkResult status;
        // check the status of current fence, if not ready take the previous one, we are incrementing with 2 for left and right frames.
        if (err != VK_SUCCESS) {
            swapChainIndx = (imageIndex + 2) % SWAP_CHAIN_COUNT;
            while (swapChainIndx != imageIndex) {
                status = vkGetFenceStatus(m_device, waitFences[swapChainIndx]);
                if (VK_SUCCESS == status) {
                    found = true;
                    break;
                }
                swapChainIndx = (swapChainIndx + 2) % SWAP_CHAIN_COUNT;
            }
             if (!found) {
            err = vkWaitForFences(m_device, 1, &waitFences[swapChainIndx], VK_TRUE,
                                  4294967295U);
             }
        }

        return swapChainIndx;
        //GVR_VK_CHECK(!err);
    }

    int VulkanCore::DrawFrameForRenderDataPE() {

        VkResult err;
        // Get the next image to render to, then queue a wait until the image is ready
        VkFence nullFence = postEffectFence;
        vkResetFences(m_device, 1, &postEffectFence);

        VkSubmitInfo submitInfo = {};
        submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        submitInfo.pNext = nullptr;
        submitInfo.waitSemaphoreCount = 0;
        submitInfo.pWaitSemaphores = nullptr;
        submitInfo.pWaitDstStageMask = nullptr;
        submitInfo.commandBufferCount = 1;
        submitInfo.pCommandBuffers = postEffectCmdBuffer;
        submitInfo.signalSemaphoreCount = 0;
        submitInfo.pSignalSemaphores = nullptr;

        err = vkQueueSubmit(m_queue, 1, &submitInfo, postEffectFence);
        GVR_VK_CHECK(!err);

        vkWaitForFences(m_device, 1, &postEffectFence, VK_TRUE,
                        4294967295U);
        //GVR_VK_CHECK(!err);
    }

    void VulkanCore::RenderToOculus(int index, int postEffectFlag){
        VkCommandBuffer trnCmdBuf;
        createTransientCmdBuffer(trnCmdBuf);
        if(postEffectFlag)
            mPostEffectTexture[index]->readVkRenderResult(&oculusTexData,trnCmdBuf,waitSCBFences);
        else
            mRenderTexture[index]->readVkRenderResult(&oculusTexData,trnCmdBuf,waitSCBFences);

        vkFreeCommandBuffers(m_device, m_commandPoolTrans, 1, &trnCmdBuf);
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
        VulkanShader* vkShader = reinterpret_cast<VulkanShader*>(shader);
        bool bones_present = shader->getVertexDescriptor().isSet("a_bone_weights");

        std::vector<VkWriteDescriptorSet> writes;
        VkDescriptorPool descriptorPool;
        GetDescriptorPool(descriptorPool);
        VkDescriptorSetLayout &descriptorLayout = reinterpret_cast<VulkanShader *>(shader)->getDescriptorLayout();
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

    bool VulkanCore::InitDescriptorSetForRenderDataPostEffect(VulkanRenderer* renderer, int pass, Shader* shader, VulkanRenderData* vkData, int postEffectIndx) {
        VkDescriptorPool descriptorPool;
        GetDescriptorPool(descriptorPool);
        VkDescriptorSetLayout &descriptorLayout = reinterpret_cast<VulkanShader *>(shader)->getDescriptorLayout();
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
        VkDescriptorImageInfo descriptorImageInfoPass2[1] = {};
             descriptorImageInfoPass2[0].sampler = VK_NULL_HANDLE;
        mRenderTexture[imageIndex]->getRenderPass();
        if(postEffectIndx == 0) {
            descriptorImageInfoPass2[0].imageView = mRenderTexture[imageIndex]->getFBO()->getImageView(
                    COLOR_IMAGE);
            descriptorImageInfoPass2[0].imageLayout = mRenderTexture[imageIndex]->getFBO()->getImageLayout(
                    COLOR_IMAGE);
        }else{
            descriptorImageInfoPass2[0].imageView = mPostEffectTexture[(postEffectIndx - 1) % 2]->getFBO()->getImageView(
                    COLOR_IMAGE);
            descriptorImageInfoPass2[0].imageLayout = mPostEffectTexture[(postEffectIndx - 1) % 2]->getFBO()->getImageLayout(
                    COLOR_IMAGE);
        }

        VkWriteDescriptorSet writes[1] = {};
        writes[0].sType             = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
        writes[0].dstBinding        = TEXTURE_BIND_START;
        writes[0].dstSet            = descriptorSet;
        writes[0].descriptorCount   = 1;
        writes[0].descriptorType    = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        writes[0].pImageInfo        = &descriptorImageInfoPass2[0];

        vkUpdateDescriptorSets(m_device, 1, &writes[0], 0, nullptr);
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
        if (InitDevice() == false) {
            m_Vulkan_Initialised = false;
            return;
        }
        createPipelineCache();

    }

    void VulkanCore::CreateSampler(TextureObject *&textureObject) {
        VkResult err;
        bool pass;

        VkMemoryAllocateInfo memoryAllocateInfo = {};
        memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
        memoryAllocateInfo.pNext = NULL;
        memoryAllocateInfo.allocationSize = 0;
        memoryAllocateInfo.memoryTypeIndex = 0;

        VkMemoryRequirements mem_reqs;

        err = vkCreateImage(m_device, gvr::ImageCreateInfo(textureObject->m_textureType,
                                                           textureObject->m_format,
                                                           textureObject->m_width,
                                                           textureObject->m_height, 1, 1, 1,
                                                           VK_IMAGE_TILING_LINEAR,
                                                           VK_IMAGE_USAGE_SAMPLED_BIT,
                                                           VkImageCreateFlags(0),
                                                           VK_SAMPLE_COUNT_1_BIT,
                                                           VK_IMAGE_LAYOUT_UNDEFINED), NULL,
                            &textureObject->m_image);
        assert(!err);

        vkGetImageMemoryRequirements(m_device, textureObject->m_image, &mem_reqs);

        memoryAllocateInfo.allocationSize = mem_reqs.size;
        pass = GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                           VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                                           &memoryAllocateInfo.memoryTypeIndex);
        assert(pass);

        /* allocate memory */
        err = vkAllocateMemory(m_device, &memoryAllocateInfo, NULL, &textureObject->m_mem);
        assert(!err);

        /* bind memory */
        err = vkBindImageMemory(m_device, textureObject->m_image, textureObject->m_mem, 0);
        assert(!err);

        // Copy source image data into mapped memory
        {
            VkImageSubresource subres;
            subres.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            subres.mipLevel = 0;
            subres.arrayLayer = 0;

            VkSubresourceLayout layout;
            uint8_t *data;

            vkGetImageSubresourceLayout(m_device, textureObject->m_image, &subres, &layout);

            err = vkMapMemory(m_device, textureObject->m_mem, 0, memoryAllocateInfo.allocationSize,
                              0, (void **) &data);
            assert(!err);

            for (int i = 0; i < ((textureObject->m_width) * (textureObject->m_height) * 4); i++) {
                data[i] = textureObject->m_data[i];
                data[i + 1] = textureObject->m_data[i + 1];
                data[i + 2] = textureObject->m_data[i + 2];
                data[i + 3] = textureObject->m_data[i + 3];
                i += 3;
            }

            vkUnmapMemory(m_device, textureObject->m_mem);
        }

        // Change the layout of the image to shader read only
        textureObject->m_imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

        // We use a shared command buffer for setup operations to change layout.
        // Reset the setup command buffer
        vkResetCommandBuffer(textureCmdBuffer, 0);

        VkCommandBufferInheritanceInfo commandBufferInheritanceInfo = {};
        commandBufferInheritanceInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
        commandBufferInheritanceInfo.pNext = NULL;
        commandBufferInheritanceInfo.renderPass = VK_NULL_HANDLE;
        commandBufferInheritanceInfo.subpass = 0;
        commandBufferInheritanceInfo.framebuffer = VK_NULL_HANDLE;
        commandBufferInheritanceInfo.occlusionQueryEnable = VK_FALSE;
        commandBufferInheritanceInfo.queryFlags = 0;
        commandBufferInheritanceInfo.pipelineStatistics = 0;

        VkCommandBufferBeginInfo setupCmdsBeginInfo;
        setupCmdsBeginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        setupCmdsBeginInfo.pNext = NULL;
        setupCmdsBeginInfo.flags = 0;
        setupCmdsBeginInfo.pInheritanceInfo = &commandBufferInheritanceInfo;

        // Begin recording to the command buffer.
        vkBeginCommandBuffer(textureCmdBuffer, &setupCmdsBeginInfo);

        VkImageMemoryBarrier imageMemoryBarrier = {};
        imageMemoryBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
        imageMemoryBarrier.pNext = NULL;
        imageMemoryBarrier.oldLayout = VK_IMAGE_LAYOUT_UNDEFINED;
        imageMemoryBarrier.newLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
        imageMemoryBarrier.image = textureObject->m_image;
        imageMemoryBarrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        imageMemoryBarrier.subresourceRange.baseMipLevel = 0;
        imageMemoryBarrier.subresourceRange.levelCount = 1;
        imageMemoryBarrier.subresourceRange.baseArrayLayer = 0;
        imageMemoryBarrier.subresourceRange.layerCount = 1;
        imageMemoryBarrier.srcAccessMask = 0;
        imageMemoryBarrier.dstAccessMask =
                VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_INPUT_ATTACHMENT_READ_BIT;

        VkPipelineStageFlags src_stages = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
        VkPipelineStageFlags dest_stages = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;

        // Barrier on image memory, with correct layouts set.
        vkCmdPipelineBarrier(textureCmdBuffer, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                             VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 0, 0, NULL, 0, NULL, 1,
                             &imageMemoryBarrier);

        // We are finished recording operations.
        vkEndCommandBuffer(textureCmdBuffer);

        VkCommandBuffer buffers[1];
        buffers[0] = textureCmdBuffer;

        VkSubmitInfo submit_info;
        submit_info.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        submit_info.pNext = NULL;
        submit_info.waitSemaphoreCount = 0;
        submit_info.pWaitSemaphores = NULL;
        submit_info.pWaitDstStageMask = NULL;
        submit_info.commandBufferCount = 1;
        submit_info.pCommandBuffers = &buffers[0];
        submit_info.signalSemaphoreCount = 0;
        submit_info.pSignalSemaphores = NULL;

        // Submit to our shared graphics queue.
        err = vkQueueSubmit(m_queue, 1, &submit_info, VK_NULL_HANDLE);
        assert(!err);

        // Wait for the queue to become idle.
        err = vkQueueWaitIdle(m_queue);
        assert(!err);

        err = vkCreateSampler(m_device, gvr::SamplerCreateInfo(VK_FILTER_NEAREST, VK_FILTER_NEAREST,
                                                               VK_SAMPLER_MIPMAP_MODE_LINEAR,
                                                               VK_SAMPLER_ADDRESS_MODE_REPEAT,
                                                               VK_SAMPLER_ADDRESS_MODE_REPEAT,
                                                               VK_SAMPLER_ADDRESS_MODE_REPEAT, 0.0f,
                                                               VK_FALSE, 0, VK_FALSE,
                                                               VK_COMPARE_OP_NEVER,
                                                               0.0f, 0.0f,
                                                               VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE,
                                                               VK_FALSE), NULL,
                              &textureObject->m_sampler);
        assert(!err);

        err = vkCreateImageView(m_device, gvr::ImageViewCreateInfo(textureObject->m_image,
                                                                   textureObject->m_textureViewType,
                                                                   textureObject->m_format, 1, 1,
                                                                   VK_IMAGE_ASPECT_COLOR_BIT), NULL,
                                &textureObject->m_view);
        assert(!err);
    }

    void VulkanCore::InitTexture() {
        VkResult err;
        bool pass;

        textureObject = new TextureObject[1];
        textureObject->m_width = 64;
        textureObject->m_height = 48;
        textureObject->m_format = VK_FORMAT_R8G8B8A8_UNORM;
        textureObject->m_textureType = VK_IMAGE_TYPE_2D;
        textureObject->m_textureViewType = VK_IMAGE_VIEW_TYPE_2D;

        textureObject->m_data = new uint8_t[((textureObject->m_width) * (textureObject->m_height) *
                                             4)];
        int flag = 0;
        for (int i = 0; i < ((textureObject->m_width) * (textureObject->m_height) * 4); i++) {
            if (flag % 2) {
                textureObject->m_data[i] = 244;

                textureObject->m_data[i + 1] = 0;
                textureObject->m_data[i + 2] = 0;
                textureObject->m_data[i + 3] = 244;
            } else {
                textureObject->m_data[i] = 244;

                textureObject->m_data[i + 1] = 255;
                textureObject->m_data[i + 2] = 244;
                textureObject->m_data[i + 3] = 244;
            }
            flag++;
            i += 3;
        }

        CreateSampler(textureObject);
    }

    VulkanCore::~VulkanCore() {
        if(swapChainCmdBuffer.capacity() != 0) {
            for (int i = 0; i < SWAP_CHAIN_COUNT; i++) {
                delete mRenderTexture[i];
                vkFreeCommandBuffers(m_device, m_commandPool, 1, swapChainCmdBuffer[i]);

                vkDestroyFence(m_device, waitFences[i], nullptr);
            }
        }

        if(postEffectCmdBuffer != nullptr){
            vkFreeCommandBuffers(m_device, m_commandPool, 1, postEffectCmdBuffer);
            for (int i = 0; i < POSTEFFECT_CHAIN_COUNT; i++) {
                delete mPostEffectTexture[i];
            }

            vkDestroyFence(m_device, postEffectFence, nullptr);
        }

        vkDestroyFence(m_device, waitSCBFences, nullptr);
        vkDestroyDevice(getDevice(), nullptr);
        vkDestroyInstance(m_instance, nullptr);
    }

    void VulkanCore::initVulkanCore() {
        GLint viewport[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        InitSwapchain(viewport[2], viewport[3]);
        InitTransientCmdPool();
        InitCommandbuffers();
#ifdef  CUSTOM_TEXTURE
        InitTexture();
#endif
        LOGE("Vulkan after intialization");
        InitSync();
        swap_chain_init_ = true;
    }
}