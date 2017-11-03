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

#pragma once

#include "../objects/index_buffer.h"
#include "vulkan_headers.h"

namespace gvr {
    class Shader;
    class Renderer;

 /**
  * Index storage for Vulkan
  *
  * @see IndexBuffer
  */
    class VulkanIndexBuffer : public IndexBuffer
    {
    public:
        explicit VulkanIndexBuffer(int bytesPerIndex, int vertexCount);
        virtual ~VulkanIndexBuffer();

        virtual bool    updateGPU(Renderer*);
        virtual bool    bindBuffer(Shader*)  { return true; }
        const GVR_VK_Indices& getVKIndices() const { return m_indices; }
        void    generateVKBuffers(VulkanCore* vulkanCore);
    protected:
        void    freeGPUResources();

        VkFormat getDataType(const std::string& type);

        GVR_VK_Indices m_indices;
    };

} // end gvrf

