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

#include "vulkanCore.h"

#include "util/gvr_log.h"
#include <assert.h>
#include <iostream>
#include <vector>
#include "objects/components/camera.h"
#include "objects/components/render_data.h"
#include "objects/scene.h"
#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"
#include "glm/gtc/type_ptr.hpp"
#include <math.h>
#include "vulkan/vulkan_headers.h"
#include <thread>
#include <shaderc/shaderc.hpp>
#include "gvr_time.h"

#define UINT64_MAX 99999

#define QUEUE_INDEX_MAX 99999
#define VERTEX_BUFFER_BIND_ID 0
std::string data_frag = std::string("") +
                        "#version 400 \n" +
                        "#extension GL_ARB_separate_shader_objects : enable \n" +
                        "#extension GL_ARB_shading_language_420pack : enable \n" +

                        "layout (std140, set = 0, binding = 2) uniform Material_ubo{\n"
                                "    vec4 u_color;\n"
                                "};\n"

                                " layout(set = 0, binding = 1) uniform sampler2D tex;\n" +
                        "layout (location = 0) out vec4 uFragColor;  \n" +
                        "layout(location = 1 )in vec2 o_texcoord; \n" +
                        "void main() {  \n" +
                        " vec4 temp = vec4(1.0,0.0,1.0,1.0);\n" +
                        //                   "   uFragColor = vec4(o_texcoord, 0, 1);  \n" +
                        "   uFragColor = texture(tex, o_texcoord);  \n" +
                        //   "   uFragColor = u_color;  \n" +
                        "}";


std::string vertexShaderData = std::string("") +
                               "#version 400 \n" +
                               "#extension GL_ARB_separate_shader_objects : enable \n" +
                               "#extension GL_ARB_shading_language_420pack : enable \n" +
                               "layout (std140, set = 0, binding = 0) uniform Transform_ubo { "
                                       "mat4 u_view;\n"
                                       "     mat4 u_mvp;\n"
                                       "     mat4 u_mv;\n"
                                       "     mat4 u_mv_it;"
                                       " mat4 u_model;\n"
                                       "     mat4 u_view_i;\n"
                                       "     vec4 u_right;"
                                       " };\n" +
                               "layout(location = 0)in vec3 pos; \n" +
                               "layout(location = 1)in vec2 a_texcoord; \n" +
                               "layout(location = 1)out vec2 o_texcoord; \n" +
                               "void main() { \n" +
                               "o_texcoord = a_texcoord; \n" +
                               "  gl_Position = u_mvp * vec4(pos.x, pos.y, pos.z,1.0); \n" +
                               "}";
namespace gvr {
    VulkanCore *VulkanCore::theInstance = NULL;
    uint8_t *oculusTexData;
    uint8_t *oculus_data[SWAP_CHAIN_COUNT];

    void Descriptor::createBuffer(VkDevice &device, VulkanCore *vk) {
        ubo->createBuffer(device, vk);
    }

    void Descriptor::createDescriptor(VkDevice &device, VulkanCore *vk, int index,
                                      VkShaderStageFlagBits shaderStageFlagBits) {
        createBuffer(device, vk);
        createLayoutBinding(index, shaderStageFlagBits);
        VkDescriptorSet desc;
        createDescriptorWriteInfo(index, shaderStageFlagBits, desc);

    }

    void Descriptor::createLayoutBinding(int binding_index, int stageFlags, bool sampler) {
        VkDescriptorType descriptorType = (sampler ? VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
                                                   : VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC);

        gvr::DescriptorLayout layout = gvr::DescriptorLayout(binding_index, 1, descriptorType,
                                                             stageFlags, 0);
        layout_binding = *layout;
    }

    void Descriptor::createDescriptorWriteInfo(int binding_index, int stageFlags,
                                               VkDescriptorSet &descriptor, bool sampler) {

        VkDescriptorType descriptorType = (sampler ? VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
                                                   : VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC);
        GVR_Uniform &uniform = ubo->getBuffer();
        gvr::DescriptorWrite writeInfo = gvr::DescriptorWrite(
                VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET, binding_index, descriptor, 1,
                descriptorType, uniform.bufferInfo);
        writeDescriptorSet = *writeInfo;

    }

    VulkanUniformBlock *Descriptor::getUBO() {
        return ubo;
    }

    VkDescriptorSetLayoutBinding &Descriptor::getLayoutBinding() {
        return layout_binding;
    }

    VkWriteDescriptorSet &Descriptor::getDescriptorSet() {
        return writeDescriptorSet;
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
        VkResult err;
        bool pass;
        VkMemoryRequirements mem_reqs;
        uint32_t memoryTypeIndex;

        VkResult ret = VK_SUCCESS;
        m_width = width;
        m_height = height;

        // Create the image with details as imageCreateInfo
        m_swapchainImageCount = SWAP_CHAIN_COUNT;
        m_swapchainBuffers = new GVR_VK_SwapchainBuffer[m_swapchainImageCount];
        outputImage = new GVR_VK_SwapchainBuffer[m_swapchainImageCount];
        GVR_VK_CHECK(m_swapchainBuffers);

        for (int i = 0; i < m_swapchainImageCount; i++) {
            bool pass;

            ret = vkCreateImage(
                    m_device,
                    gvr::ImageCreateInfo(VK_IMAGE_TYPE_2D, VK_FORMAT_R8G8B8A8_UNORM, m_width,
                                         m_height, 1, 1, 1,
                                         VK_IMAGE_TILING_LINEAR,
                                         VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT |
                                         VK_IMAGE_USAGE_TRANSFER_SRC_BIT, VK_SAMPLE_COUNT_1_BIT,
                                         VK_IMAGE_LAYOUT_UNDEFINED),
                    nullptr, &m_swapchainBuffers[i].image
            );
            GVR_VK_CHECK(!ret);

            err = vkCreateBuffer(m_device,
                                 gvr::BufferCreateInfo(m_width * m_height * 4 * sizeof(uint8_t),
                                                       VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT |
                                                       VK_IMAGE_USAGE_TRANSFER_SRC_BIT), nullptr,
                                 &m_swapchainBuffers[i].buf);
            GVR_VK_CHECK(!err);



            // discover what memory requirements are for this image.
            vkGetImageMemoryRequirements(m_device, m_swapchainBuffers[i].image, &mem_reqs);
            m_swapchainBuffers[i].size = mem_reqs.size;

            pass = GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                               &memoryTypeIndex);
            GVR_VK_CHECK(pass);

            err = vkAllocateMemory(m_device,
                                   gvr::MemoryAllocateInfo(mem_reqs.size, memoryTypeIndex), nullptr,
                                   &m_swapchainBuffers[i].mem);
            GVR_VK_CHECK(!err);

            // Bind memory to the image
            err = vkBindImageMemory(m_device, m_swapchainBuffers[i].image,
                                    m_swapchainBuffers[i].mem, 0);
            GVR_VK_CHECK(!err);

            err = vkBindBufferMemory(m_device, m_swapchainBuffers[i].buf, m_swapchainBuffers[i].mem,
                                     0);
            GVR_VK_CHECK(!err);

            err = vkCreateImageView(
                    m_device,
                    gvr::ImageViewCreateInfo(m_swapchainBuffers[i].image, VK_IMAGE_VIEW_TYPE_2D,
                                             VK_FORMAT_R8G8B8A8_UNORM, 1, 1,
                                             VK_IMAGE_ASPECT_COLOR_BIT),
                    nullptr, &m_swapchainBuffers[i].view
            );

            GVR_VK_CHECK(!err);

            err = vkCreateBuffer(m_device,
                                 gvr::BufferCreateInfo(m_width * m_height * 4 * sizeof(uint8_t),
                                                       VK_BUFFER_USAGE_TRANSFER_DST_BIT), nullptr,
                                 &outputImage[i].buf);
            GVR_VK_CHECK(!err);

            // Obtain the memory requirements for this buffer.
            vkGetBufferMemoryRequirements(m_device, outputImage[i].buf, &mem_reqs);
            GVR_VK_CHECK(!err);

            pass = GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                               VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                                               &memoryTypeIndex);
            GVR_VK_CHECK(pass);

            outputImage[i].size = mem_reqs.size;
            err = vkAllocateMemory(m_device,
                                   gvr::MemoryAllocateInfo(mem_reqs.size, memoryTypeIndex), nullptr,
                                   &outputImage[i].mem);
            GVR_VK_CHECK(!err);

            err = vkBindBufferMemory(m_device, outputImage[i].buf, outputImage[i].mem, 0);
            GVR_VK_CHECK(!err);
        }

        m_depthBuffers = new GVR_VK_DepthBuffer[m_swapchainImageCount];
        for (int i = 0; i < m_swapchainImageCount; i++) {
            VkMemoryRequirements mem_reqs;
            VkResult err;
            bool pass;

            m_depthBuffers[i].format = VK_FORMAT_D16_UNORM;

            // Create the image with details as imageCreateInfo
            err = vkCreateImage(
                    m_device,
                    gvr::ImageCreateInfo(VK_IMAGE_TYPE_2D, VK_FORMAT_D16_UNORM, m_width, m_height,
                                         1, 1, 1, VK_IMAGE_TILING_OPTIMAL,
                                         VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                                         VK_SAMPLE_COUNT_1_BIT, VK_IMAGE_LAYOUT_UNDEFINED),
                    nullptr, &m_depthBuffers[i].image
            );
            GVR_VK_CHECK(!err);

            // discover what memory requirements are for this image.
            vkGetImageMemoryRequirements(m_device, m_depthBuffers[i].image, &mem_reqs);

            pass = GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, 0,
                                               &memoryTypeIndex);
            GVR_VK_CHECK(pass);

            err = vkAllocateMemory(m_device,
                                   gvr::MemoryAllocateInfo(mem_reqs.size, memoryTypeIndex), nullptr,
                                   &m_depthBuffers[i].mem);
            GVR_VK_CHECK(!err);

            // Bind memory to the image
            err = vkBindImageMemory(m_device, m_depthBuffers[i].image, m_depthBuffers[i].mem, 0);
            GVR_VK_CHECK(!err);

            // Create the view for this image
            err = vkCreateImageView(
                    m_device,
                    gvr::ImageViewCreateInfo(m_depthBuffers[i].image, VK_IMAGE_VIEW_TYPE_2D,
                                             VK_FORMAT_D16_UNORM, 1, 1, VK_IMAGE_ASPECT_COLOR_BIT),
                    nullptr, &m_depthBuffers[i].view
            );
            GVR_VK_CHECK(!err);
        }

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

    VkCommandBuffer VulkanCore::GetTransientCmdBuffer() {
        VkResult ret = VK_SUCCESS;
        VkCommandBuffer cmdBuff;
        ret = vkAllocateCommandBuffers(
                m_device,
                gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, m_commandPoolTrans),
                &cmdBuff
        );
        GVR_VK_CHECK(!ret);
        return cmdBuff;
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
        for (int i = 0; i < m_swapchainImageCount; i++) {
            ret = vkAllocateCommandBuffers(
                    m_device,
                    gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, m_commandPool),
                    &m_swapchainBuffers[i].cmdBuffer
            );


            GVR_VK_CHECK(!ret);
        }

        // Allocating Command Buffer for Texture
        ret = vkAllocateCommandBuffers(
                m_device,
                gvr::CmdBufferCreateInfo(VK_COMMAND_BUFFER_LEVEL_PRIMARY, m_commandPool),
                &textureCmdBuffer
        );

        GVR_VK_CHECK(!ret);
    }


    void VulkanCore::InitLayoutRenderData(RenderData *rdata) {
        VkResult ret = VK_SUCCESS;
        Descriptor &transform = rdata->getVkData().getDescriptor();
        VkDescriptorSetLayoutBinding &transform_uniformBinding = transform.getLayoutBinding();

        VkDescriptorSetLayoutBinding uniformAndSamplerBinding[3] = {};
        // Our MVP matrix
        uniformAndSamplerBinding[0].binding = 0;
        uniformAndSamplerBinding[0].descriptorCount = 1;
        uniformAndSamplerBinding[0].descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;
        uniformAndSamplerBinding[0].stageFlags = VK_SHADER_STAGE_VERTEX_BIT;
        uniformAndSamplerBinding[0].pImmutableSamplers = nullptr;
        uniformAndSamplerBinding[0] = transform_uniformBinding;
        Descriptor *material_descriptor = rdata->material(0)->getDescriptor();
        VkDescriptorSetLayoutBinding &material_uniformBinding = material_descriptor->getLayoutBinding();

        material_uniformBinding.binding = 2;
        uniformAndSamplerBinding[2] = material_uniformBinding;

        // Texture
        uniformAndSamplerBinding[1].binding = 1;
        uniformAndSamplerBinding[1].descriptorCount = 1;
        uniformAndSamplerBinding[1].descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        uniformAndSamplerBinding[1].stageFlags = VK_SHADER_STAGE_FRAGMENT_BIT;
        uniformAndSamplerBinding[1].pImmutableSamplers = nullptr;


        VkDescriptorSetLayout &descriptorLayout = rdata->getVkData().getDescriptorLayout();

        ret = vkCreateDescriptorSetLayout(m_device, gvr::DescriptorSetLayoutCreateInfo(0, 3,
                                                                                       &uniformAndSamplerBinding[0]),
                                          nullptr,
                                          &descriptorLayout);
        GVR_VK_CHECK(!ret);

        VkPipelineLayout &pipelineLayout = rdata->getVkData().getPipelineLayout();
        ret = vkCreatePipelineLayout(m_device,
                                     gvr::PipelineLayoutCreateInfo(0, 1, &descriptorLayout, 0, 0),
                                     nullptr, &pipelineLayout);
        GVR_VK_CHECK(!ret);
    }

    void VulkanCore::InitUniformBuffers() {
        uint32_t memoryTypeIndex;
        // the uniform in this example is a matrix in the vertex stage
        memset(&m_modelViewMatrixUniform, 0, sizeof(m_modelViewMatrixUniform));

        VkResult err = VK_SUCCESS;

        // Create our buffer object
        VkBufferCreateInfo bufferCreateInfo;
        bufferCreateInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
        bufferCreateInfo.pNext = NULL;
        bufferCreateInfo.size = sizeof(glm::mat4);
        bufferCreateInfo.usage = VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
        bufferCreateInfo.flags = 0;

        err = vkCreateBuffer(m_device, gvr::BufferCreateInfo(sizeof(glm::mat4),
                                                             VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT),
                             NULL, &m_modelViewMatrixUniform.buf);
        assert(!err);

        // Obtain the requirements on memory for this buffer
        VkMemoryRequirements mem_reqs;
        vkGetBufferMemoryRequirements(m_device, m_modelViewMatrixUniform.buf, &mem_reqs);
        assert(!err);

        bool pass = GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                                                &memoryTypeIndex);
        assert(pass);

        // We keep the size of the allocation for remapping it later when we update contents
        m_modelViewMatrixUniform.allocSize = mem_reqs.size;

        err = vkAllocateMemory(m_device, gvr::MemoryAllocateInfo(mem_reqs.size, memoryTypeIndex),
                               NULL, &m_modelViewMatrixUniform.mem);
        assert(!err);

        // Create our initial MVP matrix
        float aaa[16] = {1, 0, 0, 0,
                         0, 1, 0, 0,
                         0, 0, 1, 0,
                         0, 0, 0, 1};
        glm::mat4 mvp = glm::make_mat4(aaa);;

        // Now we need to map the memory of this new allocation so the CPU can edit it.
        void *data;
        err = vkMapMemory(m_device, m_modelViewMatrixUniform.mem, 0,
                          m_modelViewMatrixUniform.allocSize, 0, &data);
        assert(!err);

        float tempColor[4] = {0, 1, 0, 1};
        // Copy our triangle vertices and colors into the mapped memory area
        memcpy(data, &mvp, sizeof(mvp));

        // Unmap the memory back from the CPU
        vkUnmapMemory(m_device, m_modelViewMatrixUniform.mem);

        // Bind our buffer to the memory
        err = vkBindBufferMemory(m_device, m_modelViewMatrixUniform.buf,
                                 m_modelViewMatrixUniform.mem, 0);
        assert(!err);

        m_modelViewMatrixUniform.bufferInfo.buffer = m_modelViewMatrixUniform.buf;
        m_modelViewMatrixUniform.bufferInfo.offset = 0;
        m_modelViewMatrixUniform.bufferInfo.range = sizeof(glm::mat4);
    }


    void VulkanCore::InitUniformBuffersForRenderData(GVR_Uniform &m_modelViewMatrixUniform) {
        // the uniform in this example is a matrix in the vertex stage
        uint32_t memoryTypeIndex;
        memset(&m_modelViewMatrixUniform, 0, sizeof(m_modelViewMatrixUniform));

        VkResult err = VK_SUCCESS;

        // Create our buffer object
        VkBufferCreateInfo bufferCreateInfo;
        bufferCreateInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
        bufferCreateInfo.pNext = NULL;
        bufferCreateInfo.size = sizeof(glm::mat4);;//sizeof(float)*4;
        bufferCreateInfo.usage = VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
        bufferCreateInfo.flags = 0;

        err = vkCreateBuffer(m_device, gvr::BufferCreateInfo(sizeof(glm::mat4),
                                                             VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT),
                             NULL, &m_modelViewMatrixUniform.buf);
        assert(!err);

        // Obtain the requirements on memory for this buffer
        VkMemoryRequirements mem_reqs;
        vkGetBufferMemoryRequirements(m_device, m_modelViewMatrixUniform.buf, &mem_reqs);
        assert(!err);

        bool pass = GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits,
                                                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                                                &memoryTypeIndex);
        assert(pass);

        // We keep the size of the allocation for remapping it later when we update contents
        m_modelViewMatrixUniform.allocSize = mem_reqs.size;

        err = vkAllocateMemory(m_device, gvr::MemoryAllocateInfo(memoryTypeIndex, mem_reqs.size),
                               NULL, &m_modelViewMatrixUniform.mem);
        assert(!err);

        // Create our initial MVP matrix
        float aaa[16] = {1, 0, 0, 0,
                         0, 1, 0, 0,
                         0, 0, 1, 0,
                         0, 0, 0, 1};
        glm::mat4 mvp = glm::make_mat4(aaa);;

        // Now we need to map the memory of this new allocation so the CPU can edit it.
        void *data;
        err = vkMapMemory(m_device, m_modelViewMatrixUniform.mem, 0,
                          m_modelViewMatrixUniform.allocSize, 0, &data);
        assert(!err);

        float tempColor[4] = {0, 1, 0, 1};
        // Copy our triangle verticies and colors into the mapped memory area
        memcpy(data, &mvp, sizeof(mvp));

        // Unmap the memory back from the CPU
        vkUnmapMemory(m_device, m_modelViewMatrixUniform.mem);

        // Bind our buffer to the memory
        err = vkBindBufferMemory(m_device, m_modelViewMatrixUniform.buf,
                                 m_modelViewMatrixUniform.mem, 0);
        assert(!err);

        m_modelViewMatrixUniform.bufferInfo.buffer = m_modelViewMatrixUniform.buf;
        m_modelViewMatrixUniform.bufferInfo.offset = 0;
        m_modelViewMatrixUniform.bufferInfo.range = sizeof(glm::mat4);
    }

    void VulkanCore::InitRenderPass() {
        // The renderpass defines the attachments to the framebuffer object that gets
        // used in the pipeline. We have two attachments, the colour buffer, and the
        // depth buffer. The operations and layouts are set to defaults for this type
        // of attachment.
        VkAttachmentDescription attachmentDescriptions[2] = {};
        attachmentDescriptions[0].flags = 0;
        attachmentDescriptions[0].format = VK_FORMAT_R8G8B8A8_UNORM;//.format;
        attachmentDescriptions[0].samples = VK_SAMPLE_COUNT_1_BIT;
        attachmentDescriptions[0].loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
        attachmentDescriptions[0].storeOp = VK_ATTACHMENT_STORE_OP_STORE;
        attachmentDescriptions[0].stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        attachmentDescriptions[0].stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        attachmentDescriptions[0].initialLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
        attachmentDescriptions[0].finalLayout = VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;

        attachmentDescriptions[1].flags = 0;
        attachmentDescriptions[1].format = m_depthBuffers[0].format;
        attachmentDescriptions[1].samples = VK_SAMPLE_COUNT_1_BIT;
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

        VkResult ret;
        ret = vkCreateRenderPass(m_device,
                                 gvr::RenderPassCreateInfo(0, (uint32_t) 2, attachmentDescriptions,
                                                           1, &subpassDescription, (uint32_t) 0,
                                                           nullptr), nullptr, &m_renderPass);
        GVR_VK_CHECK(!ret);
    }

    /*  void VulkanCore::CreateShaderModule(VkShaderModule& module, std::vector <uint32_t> code, uint32_t size) {
          VkResult err;

          // Creating a shader is very simple once it's in memory as compiled SPIR-V.
          VkShaderModuleCreateInfo moduleCreateInfo = {};
          moduleCreateInfo.sType = VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
          moduleCreateInfo.pNext = nullptr;
          moduleCreateInfo.codeSize = size * sizeof(unsigned int);
          moduleCreateInfo.pCode = code.data();
          moduleCreateInfo.flags = 0;
          err = vkCreateShaderModule(m_device, gvr::ShaderModuleCreateInfo(code.data(), size *
                                                                                        sizeof(unsigned int)),
                                     nullptr, &module);
          GVR_VK_CHECK(!err);
      }
    */  VkShaderModule VulkanCore::CreateShaderModule(std::vector<uint32_t> code, uint32_t size) {
        VkShaderModule module;
        VkResult err;

        err = vkCreateShaderModule(m_device, gvr::ShaderModuleCreateInfo(code.data(), size *
                                                                                      sizeof(unsigned int)),
                                   nullptr, &module);
        GVR_VK_CHECK(!err);

        return module;
    }


    VkShaderModule VulkanCore::CreateShaderModuleAscii(const uint32_t *code, uint32_t size) {
        VkShaderModule module;
        VkResult err;

        err = vkCreateShaderModule(m_device, gvr::ShaderModuleCreateInfo(code, size), nullptr,
                                   &module);
        GVR_VK_CHECK(!err);

        return module;
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
            LOGI("Vulkan shader unable to compile : %s", module.GetErrorMessage().c_str());
        }

        std::vector<uint32_t> result(module.cbegin(), module.cend());
        return result;
    }

    void VulkanCore::InitShaders(VkPipelineShaderStageCreateInfo shaderStages[],
                                 std::string &vertexShader, std::string &fragmentShader) {

        std::vector<uint32_t> result_vert = CompileShader("VulkanVS", VERTEX_SHADER, vertexShader);
        std::vector<uint32_t> result_frag = CompileShader("VulkanFS", FRAGMENT_SHADER,
                                                          fragmentShader);

        // We define two shader stages: our vertex and fragment shader.
        // they are embedded as SPIR-V into a header file for ease of deployment.
        VkShaderModule module;
        module = CreateShaderModule(result_vert, result_vert.size());
        gvr::PipelineShaderStageCreateInfo shaderStageInfo = gvr::PipelineShaderStageCreateInfo(
                VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO, VK_SHADER_STAGE_VERTEX_BIT,
                module, "main");
        shaderStages[0] = *shaderStageInfo;


        module = CreateShaderModule(result_frag, result_frag.size());
        shaderStageInfo = gvr::PipelineShaderStageCreateInfo(
                VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO, VK_SHADER_STAGE_FRAGMENT_BIT,
                module, "main");
        shaderStages[1] = *shaderStageInfo;

    }


    void VulkanCore::InitPipelineForRenderData(GVR_VK_Vertices &m_vertices, RenderData *rdata,
                                               std::vector<uint32_t> &vs,
                                               std::vector<uint32_t> &fs) {
        VkResult err;

        // The pipeline contains all major state for rendering.

        // Our vertex input is a single vertex buffer, and its layout is defined
        // in our m_vertices object already. Use this when creating the pipeline.
        VkPipelineVertexInputStateCreateInfo vi = {};
        vi = m_vertices.vi;

        // For this example we do not do blending, so it is disabled.
        VkPipelineColorBlendAttachmentState att_state[1] = {};
        att_state[0].colorWriteMask = 0xf;
        att_state[0].blendEnable = VK_FALSE;


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

        std::vector<uint32_t> result_vert = CompileShader("VulkanVS", VERTEX_SHADER,
                                                          vertexShaderData);//vs;//
        std::vector<uint32_t> result_frag = CompileShader("VulkanFS", FRAGMENT_SHADER,
                                                          data_frag);//fs;//

        // We define two shader stages: our vertex and fragment shader.
        // they are embedded as SPIR-V into a header file for ease of deployment.
        VkPipelineShaderStageCreateInfo shaderStages[2] = {};
        VkShaderModule module = CreateShaderModule(result_vert, result_vert.size());
        gvr::PipelineShaderStageCreateInfo shaderStageInfo = gvr::PipelineShaderStageCreateInfo(
                VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO, VK_SHADER_STAGE_VERTEX_BIT,
                module, "main");
        shaderStages[0] = *shaderStageInfo;

        module = CreateShaderModule(result_frag, result_frag.size());
        shaderStageInfo = gvr::PipelineShaderStageCreateInfo(
                VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO, VK_SHADER_STAGE_FRAGMENT_BIT,
                module, "main");
        shaderStages[1] = *shaderStageInfo;


        // Out graphics pipeline records all state information, including our renderpass
        // and pipeline layout. We do not have any dynamic state in this example.
        VkGraphicsPipelineCreateInfo pipelineCreateInfo = {};
        pipelineCreateInfo.sType = VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
        pipelineCreateInfo.layout = rdata->getVkData().m_pipelineLayout;
        pipelineCreateInfo.pVertexInputState = &vi;
        pipelineCreateInfo.pInputAssemblyState = gvr::PipelineInputAssemblyStateCreateInfo(
                VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
        pipelineCreateInfo.pRasterizationState = gvr::PipelineRasterizationStateCreateInfo(VK_FALSE,
                                                                                           VK_FALSE,
                                                                                           VK_POLYGON_MODE_FILL,
                                                                                           VK_CULL_MODE_BACK_BIT,
                                                                                           VK_FRONT_FACE_CLOCKWISE,
                                                                                           VK_FALSE,
                                                                                           0, 0, 0,
                                                                                           0);
        pipelineCreateInfo.pColorBlendState = gvr::PipelineColorBlendStateCreateInfo(1,
                                                                                     &att_state[0]);
        pipelineCreateInfo.pMultisampleState = gvr::PipelineMultisampleStateCreateInfo(
                VK_SAMPLE_COUNT_1_BIT, VK_NULL_HANDLE, VK_NULL_HANDLE, VK_NULL_HANDLE,
                VK_NULL_HANDLE, VK_NULL_HANDLE);
        pipelineCreateInfo.pViewportState = gvr::PipelineViewportStateCreateInfo(1, &viewport, 1,
                                                                                 &scissor);
        pipelineCreateInfo.pDepthStencilState = gvr::PipelineDepthStencilStateCreateInfo(VK_TRUE,
                                                                                         VK_TRUE,
                                                                                         VK_COMPARE_OP_LESS_OR_EQUAL,
                                                                                         VK_FALSE,
                                                                                         VK_STENCIL_OP_KEEP,
                                                                                         VK_STENCIL_OP_KEEP,
                                                                                         VK_COMPARE_OP_ALWAYS,
                                                                                         VK_FALSE);
        pipelineCreateInfo.pStages = &shaderStages[0];
        pipelineCreateInfo.renderPass = m_renderPass;
        pipelineCreateInfo.pDynamicState = nullptr;
        pipelineCreateInfo.stageCount = 2; //vertex and fragment

        VkPipelineCache pipelineCache = VK_NULL_HANDLE;
        LOGI("Vulkan graphics call before");
        err = vkCreateGraphicsPipelines(m_device, VK_NULL_HANDLE, 1, &pipelineCreateInfo, nullptr,
                                        &(rdata->getVkData().m_pipeline));
        GVR_VK_CHECK(!err);
        LOGI("Vulkan graphics call after");

    }

    void VulkanCore::InitFrameBuffers() {
        //The framebuffer objects reference the renderpass, and allow
        // the references defined in that renderpass to now attach to views.
        // The views in this example are the colour view, which is our swapchain image,
        // and the depth buffer created manually earlier.
        VkImageView attachments[2] = {};
        VkResult ret;

        m_frameBuffers = new VkFramebuffer[m_swapchainImageCount];
        // Reusing the framebufferCreateInfo to create m_swapchainImageCount framebuffers,
        // only the attachments to the relevent image views change each time.
        for (uint32_t i = 0; i < m_swapchainImageCount; i++) {
            attachments[0] = m_swapchainBuffers[i].view;
            attachments[1] = m_depthBuffers[i].view;

            LOGE("Vulkan view %d created", i);
            if ((m_swapchainBuffers[i].view == VK_NULL_HANDLE) ||
                (m_renderPass == VK_NULL_HANDLE)) {
                LOGE("Vulkan image view null");
            }
            else
                LOGE("Vulkan image view not null");
            ret = vkCreateFramebuffer(m_device,
                                      gvr::FramebufferCreateInfo(0, m_renderPass, uint32_t(2),
                                                                 attachments, m_width, m_width,
                                                                 uint32_t(1)), nullptr,
                                      &m_frameBuffers[i]);
            GVR_VK_CHECK(!ret);
        }
    }

    void VulkanCore::InitSync() {
        LOGI("Vulkan initsync start");
        VkResult ret = VK_SUCCESS;

        ret = vkCreateSemaphore(m_device, gvr::SemaphoreCreateInfo(), nullptr,
                                &m_backBufferSemaphore);
        GVR_VK_CHECK(!ret);

        ret = vkCreateSemaphore(m_device, gvr::SemaphoreCreateInfo(), nullptr,
                                &m_renderCompleteSemaphore);
        GVR_VK_CHECK(!ret);

        // Fences (Used to check draw command buffer completion)
        VkFenceCreateInfo fenceCreateInfo = {};
        fenceCreateInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
        fenceCreateInfo.flags = 0;

        waitFences.resize(m_swapchainImageCount);
        for (auto &fence : waitFences) {
            ret = vkCreateFence(m_device, gvr::FenceCreateInfo(), nullptr, &fence);
            GVR_VK_CHECK(!ret);
        }

        waitSCBFences.resize(m_swapchainImageCount);
        for (auto &fence : waitSCBFences) {
            ret = vkCreateFence(m_device, gvr::FenceCreateInfo(), nullptr, &fence);
            GVR_VK_CHECK(!ret);
        }

        LOGI("Vulkan initsync end");
    }

    void VulkanCore::BuildCmdBufferForRenderData(std::vector<VkDescriptorSet> &allDescriptors,
                                                 int &swapChainIndex,
                                                 std::vector<RenderData *> &render_data_vector,
                                                 Camera *camera) {
        // For the triangle sample, we pre-record our command buffer, as it is static.
        // We have a buffer per swap chain image, so loop over the creation process.
        int i = swapChainIndex;
        VkCommandBuffer &cmdBuffer = m_swapchainBuffers[i].cmdBuffer;

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
        cmd_buf_info.flags = 0;
        cmd_buf_info.pInheritanceInfo = &cmd_buf_hinfo;

        // By calling vkBeginCommandBuffer, cmdBuffer is put into the recording state.
        err = vkBeginCommandBuffer(cmdBuffer, &cmd_buf_info);
        GVR_VK_CHECK(!err);


        VkImageMemoryBarrier preRenderBarrier = {};
        preRenderBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
        preRenderBarrier.pNext = nullptr;
        preRenderBarrier.srcAccessMask = VK_ACCESS_MEMORY_READ_BIT;
        preRenderBarrier.dstAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
        preRenderBarrier.oldLayout = VK_IMAGE_LAYOUT_UNDEFINED;
        preRenderBarrier.newLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
        preRenderBarrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        preRenderBarrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        preRenderBarrier.image = m_swapchainBuffers[i].image;
        preRenderBarrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        preRenderBarrier.subresourceRange.baseArrayLayer = 0;
        preRenderBarrier.subresourceRange.baseMipLevel = 1;
        preRenderBarrier.subresourceRange.layerCount = 0;
        preRenderBarrier.subresourceRange.levelCount = 1;
        // Thie PipelineBarrier function can operate on memoryBarriers,
        // bufferMemory and imageMemory buffers. We only provide a single
        // imageMemoryBarrier.
        vkCmdPipelineBarrier(cmdBuffer, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                             VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                             0, 0, nullptr, 0, nullptr, 1, &preRenderBarrier);

        // When starting the render pass, we can set clear values.
        VkClearValue clear_values[1] = {};
        clear_values[0].color.float32[0] = camera->background_color_r();
        clear_values[0].color.float32[1] = camera->background_color_g();
        clear_values[0].color.float32[2] = camera->background_color_b();
        clear_values[0].color.float32[3] = camera->background_color_a();

        clear_values[1].depthStencil.depth = 1.0f;
        clear_values[1].depthStencil.stencil = 0;
        VkRenderPassBeginInfo rp_begin = {};
        rp_begin.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
        rp_begin.pNext = nullptr;
        rp_begin.renderPass = m_renderPass;
        rp_begin.framebuffer = m_frameBuffers[i];
        rp_begin.renderArea.offset.x = 0;
        rp_begin.renderArea.offset.y = 0;
        rp_begin.renderArea.extent.width = m_width;
        rp_begin.renderArea.extent.height = m_height;
        rp_begin.clearValueCount = 2;
        rp_begin.pClearValues = clear_values;

        vkCmdBeginRenderPass(cmdBuffer, &rp_begin, VK_SUBPASS_CONTENTS_INLINE);

        for (int j = 0; j < allDescriptors.size(); j++) {

            // Set our pipeline. This holds all major state
            // the pipeline defines, for example, that the vertex buffer is a triangle list.
            vkCmdBindPipeline(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                              render_data_vector[j]->getVkData().m_pipeline);

            //bind out descriptor set, which handles our uniforms and samplers
            vkCmdBindDescriptorSets(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    render_data_vector[j]->getVkData().m_pipelineLayout, 0, 1,
                                    &allDescriptors[j], 0, NULL);

            // Bind our vertex buffer, with a 0 offset.
            VkDeviceSize offsets[1] = {0};
            GVR_VK_Vertices &vert = render_data_vector[j]->mesh()->getVkVertices();
            vkCmdBindVertexBuffers(cmdBuffer, VERTEX_BUFFER_BIND_ID, 1, &(vert.buf), offsets);

            // Bind triangle index buffer
            vkCmdBindIndexBuffer(cmdBuffer, (render_data_vector[j]->mesh()->getVkIndices()).buffer,
                                 0, VK_INDEX_TYPE_UINT16);
            vkCmdDrawIndexed(cmdBuffer, (render_data_vector[j]->mesh()->getVkIndices()).count, 1, 0,
                             0, 1);
        }

        // Now our render pass has ended.
        vkCmdEndRenderPass(cmdBuffer);

        VkImageMemoryBarrier prePresentBarrier = {};
        prePresentBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
        prePresentBarrier.pNext = nullptr;
        prePresentBarrier.srcAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
        prePresentBarrier.dstAccessMask = VK_ACCESS_MEMORY_READ_BIT;
        prePresentBarrier.oldLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
        prePresentBarrier.newLayout = VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
        prePresentBarrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        prePresentBarrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        prePresentBarrier.image = m_swapchainBuffers[i].image;
        prePresentBarrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        prePresentBarrier.subresourceRange.baseArrayLayer = 0;
        prePresentBarrier.subresourceRange.baseMipLevel = 1;
        prePresentBarrier.subresourceRange.layerCount = 0;
        vkCmdPipelineBarrier(cmdBuffer, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                             VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
                             0, 0, nullptr, 0, nullptr, 1, &prePresentBarrier);


        // By ending the command buffer, it is put out of record mode.
        err = vkEndCommandBuffer(cmdBuffer);
        GVR_VK_CHECK(!err);
    }

    int VulkanCore::AcquireNextImage() {
        imageIndex = (imageIndex + 1) % m_swapchainImageCount;
        return imageIndex;
    }

    void VulkanCore::DrawFrameForRenderData(int &swapChainIndex) {

        VkResult err;
        // Get the next image to render to, then queue a wait until the image is ready
        int m_swapchainCurrentIdx = swapChainIndex;
        VkFence nullFence = waitFences[m_swapchainCurrentIdx];
        vkResetFences(m_device, 1, &waitFences[m_swapchainCurrentIdx]);

        VkSubmitInfo submitInfo = {};
        submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        submitInfo.pNext = nullptr;
        submitInfo.waitSemaphoreCount = 0;
        submitInfo.pWaitSemaphores = nullptr;
        submitInfo.pWaitDstStageMask = nullptr;
        submitInfo.commandBufferCount = 1;
        submitInfo.pCommandBuffers = &m_swapchainBuffers[m_swapchainCurrentIdx].cmdBuffer;
        submitInfo.signalSemaphoreCount = 0;
        submitInfo.pSignalSemaphores = nullptr;

        err = vkQueueSubmit(m_queue, 1, &submitInfo, waitFences[m_swapchainCurrentIdx]);
        GVR_VK_CHECK(!err);

        err = vkGetFenceStatus(m_device, waitFences[m_swapchainCurrentIdx]);
        int swapChainIndx;
        bool found = false;
        VkResult status;
        // check the status of current fence, if not ready take the previous one, we are incrementing with 2 for left and right frames.
        if (err != VK_SUCCESS) {
            swapChainIndx = (m_swapchainCurrentIdx + 2) % SWAP_CHAIN_COUNT;
            while (swapChainIndx != m_swapchainCurrentIdx) {
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

        VkCommandBuffer trnCmdBuf = GetTransientCmdBuffer();
        VkCommandBufferBeginInfo beginInfo = {};
        beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        beginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
        vkBeginCommandBuffer(trnCmdBuf, &beginInfo);
        VkBufferCopy copyRegion = {};
        copyRegion.srcOffset = 0; // Optional
        copyRegion.dstOffset = 0; // Optional
        copyRegion.size = outputImage[swapChainIndx].size;
        VkExtent3D extent3D = {};
        extent3D.width = m_width;
        extent3D.height = m_height;
        extent3D.depth = 1;
        VkBufferImageCopy region = {0};
        region.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        region.imageSubresource.layerCount = 1;
        region.imageExtent = extent3D;
        vkCmdCopyImageToBuffer(trnCmdBuf, m_swapchainBuffers[swapChainIndx].image,
                               VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                               outputImage[swapChainIndx].buf, 1, &region);
        vkEndCommandBuffer(trnCmdBuf);

        VkSubmitInfo ssubmitInfo = {};
        ssubmitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        ssubmitInfo.commandBufferCount = 1;
        ssubmitInfo.pCommandBuffers = &trnCmdBuf;

        vkQueueSubmit(m_queue, 1, &ssubmitInfo, waitSCBFences[swapChainIndx]);


        err = vkWaitForFences(m_device, 1, &waitSCBFences[swapChainIndx], VK_TRUE, 4294967295U);
        vkFreeCommandBuffers(m_device, m_commandPoolTrans, 1, &trnCmdBuf);

        uint8_t *data;
        err = vkMapMemory(m_device, outputImage[swapChainIndx].mem, 0,
                          outputImage[swapChainIndx].size, 0, (void **) &data);
        GVR_VK_CHECK(!err);
        oculusTexData = data;

        vkUnmapMemory(m_device, outputImage[swapChainIndx].mem);
        // Makes Fence Un-signalled
        err = vkResetFences(m_device, 1, &waitSCBFences[swapChainIndx]);
        GVR_VK_CHECK(!err);
    }

    void updateUniform(const std::string &key, uniformDefination uniformInfo,
                       VulkanUniformBlock *material_ubo, Material *material) {
        const float *fv;
        const int *iv;
        glm::vec4 data(1.0, 1.0, 1.0, 1.0);
        std::string type = uniformInfo.type;
        int size = uniformInfo.size;
        switch (tolower(type[0])) {
            case 'f':
            case 'm':
                fv = material->getFloatVec(key, size);
                if (fv != NULL) {
                    switch (size) {
                        case 1:
                            data.x = *fv;
                            material_ubo->setVec(key, glm::value_ptr(data), 4);
                            break;

                        case 2:
                            data.x = *fv;
                            data.y = fv[1];
                            material_ubo->setVec(key, glm::value_ptr(data), 4);
                            break;

                        case 3:
                            data.x = fv[0];
                            data.y = fv[1];
                            data.z = fv[2];
                            material_ubo->setVec(key, glm::value_ptr(data), 4);
                            break;

                        case 4:
                            material_ubo->setVec(key, fv, 4);
                            break;

                        case 16:
                            material_ubo->setVec(key, fv, 16);
                            break;
                    }
                }
                break;

            case 'i':
                iv = material->getIntVec(key, size);
                if (iv != NULL)
                    switch (size) {
                        case 1:
                            material_ubo->setInt(key, *iv);
                            break;

                        case 2:
                            material_ubo->setIntVec(key, iv, 2);
                            break;

                        case 3:
                            material_ubo->setIntVec(key, iv, 3);
                            break;

                        case 4:
                            material_ubo->setIntVec(key, iv, 4);
                            break;
                    }
                break;
        }
    }

    void VulkanCore::updateMaterialUniform(Scene *scene, Camera *camera, RenderData *render_data,
                                           std::unordered_map<std::string, uniformDefination> &nameTypeMap) {
        Material *mat = render_data->material(0);
        Descriptor *desc = mat->getDescriptor();
        VulkanUniformBlock *material_ubo = desc->getUBO();

        for (auto &it: nameTypeMap) {
            updateUniform(it.first, it.second, material_ubo, mat);
        }

        material_ubo->updateBuffer(m_device, this);

    }

    void VulkanCore::UpdateUniforms(Scene *scene, Camera *camera, RenderData *render_data) {


        VkResult ret = VK_SUCCESS;
        uint8_t *pData;
        Transform *const t = render_data->owner_object()->transform();

        if (t == nullptr)
            return;

        glm::mat4 model = t->getModelMatrix();
        glm::mat4 view = camera->getViewMatrix();
        glm::mat4 proj = camera->getProjectionMatrix();
        glm::mat4 modelViewProjection = proj * view * model;

        Descriptor &desc = render_data->getVkData().getDescriptor();
        VulkanUniformBlock *transform_ubo = desc.getUBO();
        transform_ubo->setMat4("u_mvp", glm::value_ptr(modelViewProjection));
        transform_ubo->updateBuffer(m_device, this);

    }


    void VulkanCore::InitDescriptorSetForRenderData(
            RenderData *rdata) { //VkDescriptorSet &m_descriptorSet) {
        //Create a pool with the amount of descriptors we require
        VkDescriptorPoolSize poolSize[3] = {};
        poolSize[0].type = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
        poolSize[0].descriptorCount = 1;

        poolSize[2].type = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
        poolSize[2].descriptorCount = 1;

        poolSize[1].type = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        poolSize[1].descriptorCount = 1;

        VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = {};
        descriptorPoolCreateInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
        descriptorPoolCreateInfo.pNext = nullptr;
        descriptorPoolCreateInfo.maxSets = 1;
        descriptorPoolCreateInfo.poolSizeCount = 3;
        descriptorPoolCreateInfo.pPoolSizes = poolSize;

        VkResult err;
        VkDescriptorPool &descriptorPool = rdata->getVkData().getDescriptorPool();
        err = vkCreateDescriptorPool(m_device, &descriptorPoolCreateInfo, NULL, &descriptorPool);
        GVR_VK_CHECK(!err);
        VkDescriptorSetLayout &descriptorLayout = rdata->getVkData().getDescriptorLayout();

        VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = {};
        descriptorSetAllocateInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
        descriptorSetAllocateInfo.pNext = nullptr;
        descriptorSetAllocateInfo.descriptorPool = descriptorPool;
        descriptorSetAllocateInfo.descriptorSetCount = 1;
        descriptorSetAllocateInfo.pSetLayouts = &descriptorLayout;

        VkDescriptorSet &descriptorSet = rdata->getVkData().getDescriptorSet();
        err = vkAllocateDescriptorSets(m_device, &descriptorSetAllocateInfo, &descriptorSet);
        GVR_VK_CHECK(!err);

        Descriptor &transform_desc = rdata->getVkData().getDescriptor();
        VkWriteDescriptorSet &write = transform_desc.getDescriptorSet();
        write.dstSet = descriptorSet;
        Descriptor *mat_desc = rdata->material(0)->getDescriptor();
        VkWriteDescriptorSet &write1 = mat_desc->getDescriptorSet();
        write1.dstSet = descriptorSet;
        VkWriteDescriptorSet writes[3] = {};
        writes[0] = write;
        writes[2] = write1;
        write1.dstBinding = 2;


        // Texture
        VkDescriptorImageInfo descriptorImageInfo = {};
        descriptorImageInfo.sampler = textureObject->m_sampler;
        descriptorImageInfo.imageView = textureObject->m_view;
        descriptorImageInfo.imageLayout = textureObject->m_imageLayout;


        writes[1].sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
        writes[1].dstBinding = 1;
        writes[1].dstSet = descriptorSet;
        writes[1].descriptorCount = 1;
        writes[1].descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        writes[1].pImageInfo = &descriptorImageInfo;

        LOGI("Vulkan before update descriptor");
        vkUpdateDescriptorSets(m_device, 3, &writes[0], 0, nullptr);
        LOGI("Vulkan after update descriptor");
    }

    void VulkanCore::createPipelineCache() {
        VkPipelineCacheCreateInfo pipelineCacheCreateInfo = {};
        pipelineCacheCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO;
        GVR_VK_CHECK(vkCreatePipelineCache(m_device, &pipelineCacheCreateInfo, nullptr,
                                           &m_pipelineCache));
        LOGE("Pipleline cace faile");
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
        //createPipelineCache();
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

        err = vkCreateSampler(m_device, gvr::SamplerCreateInfo(VK_FILTER_LINEAR, VK_FILTER_LINEAR,
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
        textureObject->m_width = 640;
        textureObject->m_height = 480;
        textureObject->m_format = VK_FORMAT_R8G8B8A8_UNORM;
        textureObject->m_textureType = VK_IMAGE_TYPE_2D;
        textureObject->m_textureViewType = VK_IMAGE_VIEW_TYPE_2D;

        textureObject->m_data = new uint8_t[((textureObject->m_width) * (textureObject->m_height) * 4)];

        for (int i = 0; i < ((textureObject->m_width) * (textureObject->m_height) * 4); i++) {
            textureObject->m_data[i] = 244;
            textureObject->m_data[i + 1] = 0;
            textureObject->m_data[i + 2] = 0;
            textureObject->m_data[i + 3] = 244;
            i += 3;
        }

        CreateSampler(textureObject);
    }

    void VulkanCore::initVulkanCore() {
        GLint viewport[4];
        GLint curFBO;
        glGetIntegerv(GL_FRAMEBUFFER_BINDING, &curFBO);
        glGetIntegerv(GL_VIEWPORT, viewport);
        InitSwapchain(viewport[2], viewport[3]);
        InitTransientCmdPool();
        InitCommandbuffers();
        InitTexture();
        LOGE("Vulkan after InitTexture methods");
        InitRenderPass();
        LOGE("Vulkan after InitRenderPass methods");
        InitFrameBuffers();
        InitSync();
        swap_chain_init_ = true;
    }
}