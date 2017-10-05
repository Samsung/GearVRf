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
        VulkanIndexBuffer(int bytesPerIndex, int vertexCount);
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

