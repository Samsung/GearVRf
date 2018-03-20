
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
#ifndef FRAMEWORK_VULKANINFOWRAPPER_H
#define FRAMEWORK_VULKANINFOWRAPPER_H

#include <vulkan/vulkan.h>
#include <vector>

namespace gvr {
struct GVR_VK_SwapchainBuffer
{
    VkImage image;
    VkCommandBuffer cmdBuffer;
    VkImageView view;
    VkDeviceSize size;
    VkDeviceMemory mem;
    VkBuffer buf;
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
   //    VkVertexInputBindingDescription      vi_bindings[6];
   //     VkVertexInputAttributeDescription    vi_attrs[6];

    VkVertexInputBindingDescription      vi_bindings;
    std::vector<VkVertexInputAttributeDescription>    vi_attrs;
};

struct GVR_Uniform {
    VkBuffer buf;
    VkDeviceMemory mem;
    VkDescriptorBufferInfo bufferInfo;
    VkDeviceSize allocSize;
};

struct OutputBuffer
{
    VkBuffer imageOutputBuffer;
    VkDeviceMemory memory;
    VkDeviceSize size;
};

// Index buffer
struct GVR_VK_Indices {
    VkDeviceMemory memory;
    VkBuffer buffer;
    uint32_t count;
};
    class PipelineShaderStageCreateInfo final
    {
        VkPipelineShaderStageCreateInfo mInfo;
    public:
        explicit PipelineShaderStageCreateInfo(VkStructureType sType, VkShaderStageFlagBits stage,VkShaderModule&  module,const char* name);
        operator const VkPipelineShaderStageCreateInfo*()const
        {
            return &mInfo;
        }
    };
    class PipelineInputAssemblyStateCreateInfo final
    {
        VkPipelineInputAssemblyStateCreateInfo mInfo;
    public:
        explicit PipelineInputAssemblyStateCreateInfo(VkPrimitiveTopology topology);

        operator const VkPipelineInputAssemblyStateCreateInfo*() const
        {
            return &mInfo;
        }
    };

    class PipelineRasterizationStateCreateInfo final
    {
        VkPipelineRasterizationStateCreateInfo mInfo;
    public:
        explicit PipelineRasterizationStateCreateInfo(VkBool32 depthClamp,VkBool32 rasterizeDiscard, VkPolygonMode polyMode, VkCullModeFlags cullMode, VkFrontFace frontFace,
                                                      VkBool32 depthBias, float depthBiasConstantFactor, float  depthBiasClamp, float depthBiasSlopeFactor, float lineWidth);

        operator const VkPipelineRasterizationStateCreateInfo*() const
        {
            return &mInfo;
        }
    };

    class PipelineColorBlendAttachmentState final
    {
        VkPipelineColorBlendAttachmentState mInfo;
    public:
        explicit PipelineColorBlendAttachmentState(VkBool32 blendEnable,VkBlendOp alphablendOp, VkBlendOp colorBlendOp, VkColorComponentFlags colorwriteMask );
        operator const VkPipelineColorBlendAttachmentState*() const {
            return &mInfo;
        }

    };

    class PipelineColorBlendStateCreateInfo final
    {
        VkPipelineColorBlendStateCreateInfo mInfo;
    public:
        explicit PipelineColorBlendStateCreateInfo(uint32_t attachmentCount, const VkPipelineColorBlendAttachmentState* pAttachments );
        operator const VkPipelineColorBlendStateCreateInfo*() const{
            return &mInfo;
        }
    };

    class PipelineMultisampleStateCreateInfo final
    {
        VkPipelineMultisampleStateCreateInfo mInfo;
    public:
        explicit PipelineMultisampleStateCreateInfo(VkSampleCountFlagBits rasterizationSamples, VkBool32 sampleShadingEnable,
                                                    float minSampleShading, const VkSampleMask* pSampleMask, VkBool32 alphaToCoverageEnable, VkBool32 alphaToOneEnable);
        operator const VkPipelineMultisampleStateCreateInfo*() const{
            return &mInfo;
        }
    };

    class PipelineViewportStateCreateInfo final
    {
        VkPipelineViewportStateCreateInfo mInfo;
    public:
        explicit PipelineViewportStateCreateInfo(uint32_t viewportCount, const VkViewport* pViewports, uint32_t scissorCount, const VkRect2D* pScissors);
        operator const VkPipelineViewportStateCreateInfo*() const{
            return &mInfo;
        }
    };


    /*class Viewport final
    {
        VkViewport mInfo;
    public:
        explicit Viewport(int width, int height, int minDepth, int maxDepth);
        operator const VkViewport*() const{
            return &mInfo;
        }

    };
    class ScissorRectangle final
    {
        VkRect2D mInfo;
    public:
        explicit ScissorRectangle(uint32_t width, uint32_t height, uint32_t xOffset, uint32_t yOffset);
        operator const VkRect2D*() const{
            return &mInfo;
        }
    };*/
    class PipelineDepthStencilStateCreateInfo final
    {
        VkPipelineDepthStencilStateCreateInfo mInfo;
    public:
        explicit PipelineDepthStencilStateCreateInfo(VkBool32 depthTestEnable , VkBool32 depthWriteEnable,VkCompareOp depthCompareOp,
                                                     VkBool32 depthBoundsTestEnable ,VkStencilOp failOp,VkStencilOp passOp, VkStencilOp depthFailOp,
                                                     VkCompareOp compareOp, uint32_t compareMask, uint32_t writeMask,
                                                     uint32_t reference, VkBool32 stencilTestEnable );
        operator const VkPipelineDepthStencilStateCreateInfo*() const{
            return &mInfo;
        }
    };
class ImageCreateInfo final
    {
    public:
        ImageCreateInfo(VkImageType aImageType, VkFormat aFormat,
            int32_t aWidth, int32_t aHeight, int32_t aDepth,
            VkImageTiling aTiling, VkImageUsageFlags aUsage,
            VkImageLayout aLayout = VK_IMAGE_LAYOUT_UNDEFINED);

        ImageCreateInfo(VkImageType aImageType, VkFormat aFormat,
            int32_t aWidth, int32_t aHeight, int32_t aDepth,
            uint32_t aArraySize, VkImageTiling aTiling, VkImageUsageFlags aUsage,
            VkImageLayout aLayout = VK_IMAGE_LAYOUT_UNDEFINED);

        ImageCreateInfo(VkImageType aImageType, VkFormat aFormat,
            int32_t aWidth, int32_t aHeight, int32_t aDepth, uint32_t aMipLevels,
            uint32_t aArraySize, VkImageTiling aTiling, VkImageUsageFlags aUsage, VkImageCreateFlags,
            VkSampleCountFlagBits aSamples = VK_SAMPLE_COUNT_1_BIT,
            VkImageLayout aLayout = VK_IMAGE_LAYOUT_UNDEFINED);


        operator const VkImageCreateInfo*() const
        {
            return &mInfo;
        }
    private:
        VkImageCreateInfo mInfo;
    };


class ImageViewCreateInfo final
    {
    public:
        ImageViewCreateInfo(VkImage aImage, VkImageViewType aType, VkFormat aFormat, VkImageAspectFlags aAspectFlags);
        ImageViewCreateInfo(VkImage aImage, VkImageViewType aType, VkFormat aFormat, uint32_t aArraySize, VkImageAspectFlags aAspectFlags);
        ImageViewCreateInfo(VkImage aImage, VkImageViewType aType, VkFormat aFormat, uint32_t aMipLevels, uint32_t aArraySize, VkImageAspectFlags aAspectFlags);

        operator const VkImageViewCreateInfo*() const
        {
            return &mInfo;
        }
    private:
        VkImageViewCreateInfo mInfo;
    };

class CmdPoolCreateInfo final
    {
        VkCommandPoolCreateInfo mInfo;
    public:
        explicit CmdPoolCreateInfo(VkCommandPoolCreateFlags aFlags = 0, uint32_t aFamilyIndex = 0);

        operator const VkCommandPoolCreateInfo*() const
        {
            return &mInfo;
        }
    };
class DescriptorWrite final
{
    VkWriteDescriptorSet write;
    public:
        explicit DescriptorWrite(VkStructureType type, int index, uint32_t descriptorCount, VkDescriptorType& descriptorType, VkDescriptorBufferInfo& info,
                                    VkDescriptorImageInfo* descriptorImageInfo =0);

           operator const VkWriteDescriptorSet*() const
           {
               return &write;
           }

};

class  DescriptorLayout final
{
    VkDescriptorSetLayoutBinding uniformAndSamplerBinding;
    public:
        explicit DescriptorLayout(int binding, int descriptorCount, VkDescriptorType& descriptorType, int stageFlags, int immulableSamplers);
        operator const VkDescriptorSetLayoutBinding*()const
        {
            return &uniformAndSamplerBinding;
        }
};
class CmdBufferCreateInfo final
    {
        VkCommandBufferAllocateInfo mInfo;
    public:
        explicit CmdBufferCreateInfo(VkCommandBufferLevel aLevel = VK_COMMAND_BUFFER_LEVEL_PRIMARY, VkCommandPool aCmdPool = VK_NULL_HANDLE);

        operator const VkCommandBufferAllocateInfo*() const
        {
            return &mInfo;
        }
    };

class BufferCreateInfo final
    {
    public:
        BufferCreateInfo(VkDeviceSize aSize, VkBufferUsageFlags aUsageFlags,
            VkBufferCreateFlags aCreateFlags = 0);

        operator const VkBufferCreateInfo*() const
        {
            return &mInfo;
        }
    private:
        VkBufferCreateInfo mInfo;
    };

class ShaderModuleCreateInfo final
    {
        VkShaderModuleCreateInfo mCreateInfo;
    public:
        ShaderModuleCreateInfo(const uint32_t* aSource, size_t aSize, VkShaderModuleCreateFlags aFlags = 0);

        operator const VkShaderModuleCreateInfo*() const
        {
            return &mCreateInfo;
        }
    };

class SemaphoreCreateInfo final
    {
    public:
        SemaphoreCreateInfo(VkSemaphoreCreateFlags aFlags = 0);

        operator const VkSemaphoreCreateInfo*() const
        {
            return &mInfo;
        }
    private:
        VkSemaphoreCreateInfo mInfo;
    };

class FenceCreateInfo final
    {
    public:
        explicit FenceCreateInfo(VkFenceCreateFlags aFlags = VK_FENCE_CREATE_SIGNALED_BIT);

        operator const VkFenceCreateInfo*() const
        {
            return &mInfo;
        }
    private:
        VkFenceCreateInfo mInfo;
    };

    class FramebufferCreateInfo final
    {
        VkFramebufferCreateInfo mInfo;
    public:
        explicit FramebufferCreateInfo(VkFramebufferCreateFlags flags, VkRenderPass renderPass, uint32_t attachmentCount, const VkImageView* pAttachments, uint32_t width, uint32_t height, uint32_t layers);

        operator const VkFramebufferCreateInfo*() const
        {
            return &mInfo;
        }
    };

    class RenderPassCreateInfo final
    {
        VkRenderPassCreateInfo mInfo;
    public:
        explicit RenderPassCreateInfo(VkRenderPassCreateFlags flags, uint32_t attachmentCount, const VkAttachmentDescription* pAttachments,
                                      uint32_t subpassCount, const VkSubpassDescription* pSubpasses, uint32_t dependencyCount, const VkSubpassDependency* pDependencies);

        operator const VkRenderPassCreateInfo*() const
        {
            return &mInfo;
        }
    };

    class MemoryAllocateInfo final
    {
        VkMemoryAllocateInfo mInfo;
    public:
        explicit MemoryAllocateInfo(VkDeviceSize allocationSize, uint32_t memoryTypeIndex);

        operator const VkMemoryAllocateInfo*() const
        {
            return &mInfo;
        }
    };

    class DescriptorSetLayoutCreateInfo final
    {
        VkDescriptorSetLayoutCreateInfo mInfo;
    public:
        explicit DescriptorSetLayoutCreateInfo(VkDescriptorSetLayoutCreateFlags flags, uint32_t bindingCount, const VkDescriptorSetLayoutBinding* pBindings);

        operator const VkDescriptorSetLayoutCreateInfo*() const
        {
            return &mInfo;
        }
    };

    class PipelineLayoutCreateInfo final
    {
        VkPipelineLayoutCreateInfo mInfo;
    public:
        explicit PipelineLayoutCreateInfo(VkPipelineLayoutCreateFlags flags, uint32_t setLayoutCount, const VkDescriptorSetLayout* pSetLayouts, uint32_t pushConstantRangeCount, const VkPushConstantRange* pPushConstantRanges);

        operator const VkPipelineLayoutCreateInfo*() const
        {
            return &mInfo;
        }
    };

    class SamplerCreateInfo final
    {
        VkSamplerCreateInfo mInfo;
    public:
        explicit SamplerCreateInfo(VkFilter magFilter, VkFilter minFilter, VkSamplerMipmapMode mipmapMode, VkSamplerAddressMode addressModeU,
                                   VkSamplerAddressMode addressModeV, VkSamplerAddressMode addressModeW, float mipLodBias, VkBool32 anisotropyEnable,
                                   float maxAnisotropy, VkBool32 compareEnable, VkCompareOp compareOp, float minLod, float maxLod, VkBorderColor borderColor,
                                   VkBool32 unnormalizedCoordinates);

        operator const VkSamplerCreateInfo*() const
        {
            return &mInfo;
        }
    };
}
#endif //FRAMEWORK_VULKANINFOWRAPPER_H
