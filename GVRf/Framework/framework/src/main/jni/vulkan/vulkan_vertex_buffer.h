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

#include "objects/vertex_buffer.h"
#include "vulkan/vulkan_headers.h"
#include <vector>
#include <map>

namespace gvr {
    class Shader;
    class Renderer;

 /**
  * Interleaved vertex storage for OpenGL
  *
  * @see VertexBuffer
  */
    class VulkanVertexBuffer : public VertexBuffer
    {
    public:
        VulkanVertexBuffer(const char* layout_desc, int vertexCount);
        virtual ~VulkanVertexBuffer();

        virtual bool    updateGPU(Renderer*, IndexBuffer*, Shader*);
        virtual void    bindToShader(Shader*r, IndexBuffer*) { }
        const GVR_VK_Vertices* getVKVertices(Shader* shader);
        void    generateVKBuffers(VulkanCore* vulkanCore, Shader* shader);

    protected:
        void    freeGPUResources();
        VkFormat getDataType(const std::string& type);
        std::unordered_map<Shader*,std::shared_ptr<GVR_VK_Vertices>> mVerticesMap;
    };

} // end gvrf

