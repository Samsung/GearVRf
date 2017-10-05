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
     //   GVR_VK_Vertices m_vertices;
    };

} // end gvrf

