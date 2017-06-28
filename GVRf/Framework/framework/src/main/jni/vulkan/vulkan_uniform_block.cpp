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
#include <engine/renderer/vulkan_renderer.h>
#include "vulkan_headers.h"
#include "util/gvr_gl.h"
#include "vulkan_shader.h"

namespace gvr {

    VulkanUniformBlock::VulkanUniformBlock(const char* descriptor, int bindingPoint, const char* blockName)
            : UniformBlock(descriptor, bindingPoint, blockName), vk_descriptor(nullptr)
    {
        vk_descriptor = new VulkanDescriptor();
    }

    VulkanUniformBlock::VulkanUniformBlock(const char* descriptor, int bindingPoint, const char* blockName, int maxelems)
            : UniformBlock(descriptor, bindingPoint, blockName, maxelems), vk_descriptor(nullptr)
    {
        vk_descriptor = new VulkanDescriptor();
    }

    void VulkanUniformBlock::createDescriptorWriteInfo(int binding_index, int stageFlags, bool sampler) {

        VkDescriptorType descriptorType = (sampler ? VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
                                                   : VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC);
        GVR_Uniform &uniform = getBuffer();
        gvr::DescriptorWrite writeInfo = gvr::DescriptorWrite(
                VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET, binding_index, 1,
                descriptorType, uniform.bufferInfo);
        writeDescriptorSet = *writeInfo;

    }
    const VkWriteDescriptorSet& VulkanUniformBlock::getDescriptorSet() {
          return writeDescriptorSet;
    }

    bool VulkanUniformBlock::updateGPU(Renderer* renderer)
    {
        VulkanRenderer* vkrender = reinterpret_cast<VulkanRenderer *>(renderer);
        VulkanCore* vk = vkrender->getCore();
        if (!mIsDirty)
        {
            return false;
        }
        if (!buffer_init_)
        {
            createBuffer(vk);
        }
        updateBuffer(vk);
        mIsDirty = false;
        return true;
    }

    std::string VulkanUniformBlock::makeShaderLayout()
    {
        std::ostringstream stream;
        if (mUseBuffer)
        {
            stream << "layout (std140, set = 0, binding = " << getBindingPoint() <<" ) uniform " << getBlockName() << " {" << std::endl;
            DataDescriptor::forEachEntry([&stream](const DataEntry& entry) mutable
                                         {
                                             if(entry.IsSet)
                                                 stream << "   " << entry.Type << " " << entry.Name << ";" << std::endl;
                                         });
        }
        else
        {
            stream << "layout (std140, push_constant) uniform PushConstants {" << std::endl;
            DataDescriptor::forEachEntry([&stream, this](const DataEntry& entry) mutable
                                         {
                                             if (entry.IsSet)
                                             {
                                                 stream << "   " << entry.Type << " " << entry.Name << ";" << std::endl;
                                             }
                                         });
        }
        stream << "};" << std::endl;
        return stream.str();
    }

    void VulkanUniformBlock::updateBuffer(VulkanCore* vk)
    {
        VkDevice& device = vk->getDevice();
        VkResult ret = VK_SUCCESS;
        uint8_t *pData;
        ret = vkMapMemory(device, m_bufferInfo.mem, 0, m_bufferInfo.allocSize, 0, (void **) &pData);
        assert(!ret);

        memcpy(pData, mUniformData, mTotalSize);
        vkUnmapMemory(device, m_bufferInfo.mem);
    }

    void VulkanUniformBlock::createBuffer(VulkanCore* vk)
    {
        VkDevice& device = vk->getDevice();

        VkResult err = VK_SUCCESS;
        memset(&m_bufferInfo, 0, sizeof(m_bufferInfo));
        err = vkCreateBuffer(device, gvr::BufferCreateInfo(mTotalSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT), NULL, &m_bufferInfo.buf);
        assert(!err);
        // Obtain the requirements on memory for this buffer
        VkMemoryRequirements mem_reqs;
        vkGetBufferMemoryRequirements(device, m_bufferInfo.buf, &mem_reqs);
        assert(!err);

        // And allocate memory according to those requirements
        VkMemoryAllocateInfo memoryAllocateInfo;
        memoryAllocateInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
        memoryAllocateInfo.pNext = NULL;
        memoryAllocateInfo.allocationSize = 0;
        memoryAllocateInfo.memoryTypeIndex = 0;
        memoryAllocateInfo.allocationSize  = mem_reqs.size;
        bool pass = vk->GetMemoryTypeFromProperties(mem_reqs.memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, &memoryAllocateInfo.memoryTypeIndex);
        assert(pass);

        // We keep the size of the allocation for remapping it later when we update contents
        m_bufferInfo.allocSize = memoryAllocateInfo.allocationSize;

        err = vkAllocateMemory(device, &memoryAllocateInfo, NULL, &m_bufferInfo.mem);
        assert(!err);

        // Bind our buffer to the memory
        err = vkBindBufferMemory(device, m_bufferInfo.buf, m_bufferInfo.mem, 0);
        assert(!err);

        m_bufferInfo.bufferInfo.buffer = m_bufferInfo.buf;
        m_bufferInfo.bufferInfo.offset = 0;

        m_bufferInfo.bufferInfo.range = mTotalSize;
        // TODO: we should select a descriptor based on the uniform descriptor string
        // we should not create new descriptors for each buffer!
        createDescriptorWriteInfo(getBindingPoint(), VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_VERTEX_BIT);
        vk_descriptor->createDescriptor(vk, getBindingPoint(),  VkShaderStageFlagBits(VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_VERTEX_BIT ));
        buffer_init_ = true;
    }

}
