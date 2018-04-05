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

/****
 *
 * VertexBuffer maintains a vertex data array with locations, normals,
 * colors and texcoords.
 *
 ****/
#include <engine/renderer/vulkan_renderer.h>
#include "vulkan_index_buffer.h"
#include "vulkan_shader.h"

namespace gvr {
    VulkanIndexBuffer::VulkanIndexBuffer(int bytesPerIndex, int vertexCount)
    : IndexBuffer(bytesPerIndex, vertexCount)
    { }

    VulkanIndexBuffer::~VulkanIndexBuffer()
    { }


    bool VulkanIndexBuffer::updateGPU(Renderer* renderer)
    {
        std::lock_guard<std::mutex> lock(mUpdateLock);
        VulkanRenderer* vkrender = static_cast<VulkanRenderer*>(renderer);
        const void* data = getIndexData();
        if ((getIndexCount() == 0) || (data == NULL))
        {
            LOGE("IndexBuffer::updateGPU no index data yet");
            return false;
        }

        generateVKBuffers(vkrender->getCore());
        return true;
    }

    void VulkanIndexBuffer::generateVKBuffers(VulkanCore* vulkanCore)
    {

        if (!isDirty())
            return;
        m_indices.count = static_cast<uint32_t>(getIndexCount());
        VkMemoryRequirements mem_reqs;
        VkMemoryAllocateInfo memoryAllocateInfo = {};
        bool        pass;
        void*       data;
        uint32_t    indexBufferSize = getDataSize();//sizeof(uint32_t);//*
        VkDevice&   device = vulkanCore->getDevice();
        VkCommandBuffer trnCmdBuf;
        vulkanCore->createTransientCmdBuffer(trnCmdBuf);

        // Copy index data to a buffer visible to the host
        VkResult   err = vkCreateBuffer(device, gvr::BufferCreateInfo(indexBufferSize, VK_BUFFER_USAGE_INDEX_BUFFER_BIT), nullptr, &m_indices.buffer);
        GVR_VK_CHECK(!err);

        VkDeviceMemory mem_staging_indi;
        VkBuffer buf_staging_indi;
        err = vkCreateBuffer(device, gvr::BufferCreateInfo(indexBufferSize, VK_BUFFER_USAGE_INDEX_BUFFER_BIT), nullptr, &buf_staging_indi);
        GVR_VK_CHECK(!err);


        vkGetBufferMemoryRequirements(device, m_indices.buffer, &mem_reqs);
        memoryAllocateInfo.allocationSize = mem_reqs.size;
        pass = vulkanCore->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, &memoryAllocateInfo.memoryTypeIndex);
        GVR_VK_CHECK(pass);

        err = vkAllocateMemory(device, &memoryAllocateInfo, nullptr, &mem_staging_indi);
        GVR_VK_CHECK(!err);
        err = vkMapMemory(device, mem_staging_indi, 0, indexBufferSize, 0, &data);
        GVR_VK_CHECK(!err);
        memcpy(data, mIndexData, indexBufferSize);
        vkUnmapMemory(device, mem_staging_indi);

        //err = vkBindBufferMemory(m_device, m_indices.buffer, m_indices.memory, 0);
        err = vkBindBufferMemory(device, buf_staging_indi, mem_staging_indi, 0);
        GVR_VK_CHECK(!err);

        // Create Device memory optimal
        pass = vulkanCore->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, &memoryAllocateInfo.memoryTypeIndex);
        GVR_VK_CHECK(pass);
        err = vkAllocateMemory(device, &memoryAllocateInfo, nullptr, &m_indices.memory);
        GVR_VK_CHECK(!err);

        // Bind our buffer to the memory.
        err = vkBindBufferMemory(device, m_indices.buffer, m_indices.memory, 0);
        GVR_VK_CHECK(!err);

        VkCommandBufferBeginInfo beginInfo = {};
        beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        beginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
        vkBeginCommandBuffer(trnCmdBuf, &beginInfo);
        VkBufferCopy copyRegion = {};
        copyRegion.srcOffset = 0; // Optional
        copyRegion.dstOffset = 0; // Optional
        copyRegion.size = indexBufferSize;
        vkCmdCopyBuffer(trnCmdBuf, buf_staging_indi, m_indices.buffer, 1, &copyRegion);
        vkEndCommandBuffer(trnCmdBuf);

        VkSubmitInfo submitInfo = {};
        submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
        submitInfo.commandBufferCount = 1;
        submitInfo.pCommandBuffers = &trnCmdBuf;

        vkQueueSubmit(vulkanCore->getVkQueue(), 1, &submitInfo, VK_NULL_HANDLE);
        vkQueueWaitIdle(vulkanCore->getVkQueue());
        vkFreeCommandBuffers(device, vulkanCore->getTransientCmdPool(), 1, &trnCmdBuf);

        // Free up the staging buffer
        vkDestroyBuffer(device, buf_staging_indi, nullptr);
        vkFreeMemory(device, mem_staging_indi, nullptr);

        mIsDirty = false;
    }

    VkFormat VulkanIndexBuffer::getDataType(const std::string& type)
    {
        if(type.compare("float")==0)
            return VK_FORMAT_R32_SFLOAT;

        if(type.compare("vec2")==0 || type.compare("float2")==0)
            return VK_FORMAT_R32G32_SFLOAT;

        if(type.compare("float3")==0 || type.compare("vec3")==0)
            return VK_FORMAT_R32G32B32_SFLOAT;

        if(type.compare("float4")==0 || type.compare("vec4")==0)
            return VK_FORMAT_R32G32B32A32_SFLOAT;

        throw std::runtime_error("VulkanIndexBuffer::getDataType: unknown type");
    }
} // end gvrf

