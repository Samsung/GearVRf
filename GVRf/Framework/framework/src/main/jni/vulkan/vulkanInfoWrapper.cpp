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
#include "vulkanInfoWrapper.h"

namespace gvr {
    PipelineShaderStageCreateInfo::PipelineShaderStageCreateInfo(VkStructureType sType, VkShaderStageFlagBits stage,VkShaderModule&  module,const char* name):mInfo(){
        mInfo.sType = sType;
        mInfo.stage = stage;
        mInfo.module= module;
        mInfo.pName = name;

    }
    PipelineInputAssemblyStateCreateInfo::PipelineInputAssemblyStateCreateInfo(VkPrimitiveTopology topology):mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
        mInfo.topology = topology;
    }


    PipelineRasterizationStateCreateInfo::PipelineRasterizationStateCreateInfo(VkBool32 depthClamp,VkBool32 rasterizeDiscard, VkPolygonMode polyMode, VkCullModeFlags cullMode,VkFrontFace frontFace,
                                                                               VkBool32 depthBias, float depthBiasConstantFactor, float  depthBiasClamp, float depthBiasSlopeFactor, float lineWidth):mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
        mInfo.cullMode = cullMode;
        mInfo.depthBiasClamp = depthBiasClamp;
        mInfo.depthBiasConstantFactor = depthBiasConstantFactor;
        mInfo.depthBiasSlopeFactor = depthBiasSlopeFactor;
        mInfo.depthBiasEnable = depthBias;
        mInfo.depthClampEnable = depthClamp;
        mInfo.lineWidth = lineWidth;
        mInfo.frontFace = frontFace;
    }



    PipelineColorBlendAttachmentState::PipelineColorBlendAttachmentState(VkBool32 blendEnable,VkBlendOp alphablendOp, VkBlendOp colorBlendOp, VkColorComponentFlags colorwriteMask):mInfo()
    {
        mInfo.alphaBlendOp = alphablendOp;
        mInfo.blendEnable  = blendEnable;
        mInfo.colorBlendOp = colorBlendOp;
        mInfo.colorWriteMask = colorwriteMask;

    }


    PipelineColorBlendStateCreateInfo::PipelineColorBlendStateCreateInfo(uint32_t attachmentCount, const VkPipelineColorBlendAttachmentState* pAttachments ):mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
        mInfo.attachmentCount = attachmentCount;
        mInfo.pAttachments = pAttachments;
    //    mInfo.logicOpEnable = VK_TRUE;
    }

    PipelineViewportStateCreateInfo::PipelineViewportStateCreateInfo(uint32_t viewportCount, const VkViewport* pViewports, uint32_t scissorCount, const VkRect2D* pScissors):mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
        mInfo.viewportCount = viewportCount;
        mInfo.pViewports = pViewports;
        mInfo.scissorCount = scissorCount;
        mInfo.pScissors = pScissors;
    }

    PipelineMultisampleStateCreateInfo::PipelineMultisampleStateCreateInfo(VkSampleCountFlagBits rasterizationSamples, VkBool32 sampleShadingEnable,
    float minSampleShading, const VkSampleMask* pSampleMask, VkBool32 alphaToCoverageEnable, VkBool32 alphaToOneEnable):mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
        mInfo.rasterizationSamples = rasterizationSamples;
        mInfo.sampleShadingEnable = sampleShadingEnable;
        mInfo.minSampleShading = minSampleShading;
        mInfo.pSampleMask = pSampleMask;
        mInfo.alphaToCoverageEnable = alphaToCoverageEnable;
        mInfo.alphaToOneEnable = alphaToOneEnable;
    }

    /*Viewport::Viewport(int width, int height, int minDepth, int maxDepth):mInfo()
    {

        mInfo.width    = width;
        mInfo.height   = height;
        mInfo.minDepth = minDepth;
        mInfo.maxDepth = maxDepth;
    }


    ScissorRectangle::ScissorRectangle(uint32_t width, uint32_t height, uint32_t xOffset, uint32_t yOffset):mInfo()
    {
        mInfo.extent.width = width;
        mInfo.extent.height = height;
        mInfo.offset.x = xOffset;
        mInfo.offset.y = yOffset;
    }*/


    PipelineDepthStencilStateCreateInfo::PipelineDepthStencilStateCreateInfo
            (VkBool32 depthTestEnable , VkBool32 depthWriteEnable,VkCompareOp depthCompareOp,
             VkBool32 depthBoundsTestEnable ,VkStencilOp failOp,VkStencilOp passOp, VkStencilOp depthFailOp,
             VkCompareOp compareOp, uint32_t compareMask, uint32_t writeMask,
             uint32_t reference, VkBool32 stencilTestEnable ):mInfo(){

        mInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
        mInfo.depthTestEnable = depthTestEnable;
        mInfo.depthWriteEnable = depthWriteEnable;
        mInfo.depthCompareOp = depthCompareOp;
        mInfo.depthBoundsTestEnable = depthBoundsTestEnable;

        mInfo.back.failOp = failOp;
        mInfo.back.passOp = passOp;
        mInfo.back.depthFailOp = depthFailOp;
        mInfo.back.compareOp = compareOp;
        mInfo.back.compareMask = compareMask;
        mInfo.back.writeMask = writeMask;
        mInfo.back.reference = reference;

        mInfo.stencilTestEnable = stencilTestEnable;
        mInfo.front = mInfo.back;
    };

ImageCreateInfo::ImageCreateInfo(VkImageType aImageType, VkFormat aFormat,
    int32_t aWidth, int32_t aHeight, int32_t aDepth,
    VkImageTiling aTiling, VkImageUsageFlags aUsage, VkImageLayout aLayout)
    : ImageCreateInfo(aImageType, aFormat, aWidth, aHeight, aDepth, 1, aTiling, aUsage, aLayout)
{
}

ImageCreateInfo::ImageCreateInfo(VkImageType aImageType, VkFormat aFormat, int32_t aWidth,
    int32_t aHeight, int32_t aDepth, uint32_t aArraySize, VkImageTiling aTiling,
    VkImageUsageFlags aUsage, VkImageLayout aLayout)
    : ImageCreateInfo(aImageType, aFormat, aWidth, aHeight, aDepth, 1, 1, aTiling, aUsage, 0 , VK_SAMPLE_COUNT_1_BIT, aLayout)
{
}

ImageCreateInfo::ImageCreateInfo(VkImageType aImageType, VkFormat aFormat,
    int32_t aWidth, int32_t aHeight, int32_t aDepth, uint32_t aMipLevels,
    uint32_t aArraySize, VkImageTiling aTiling, VkImageUsageFlags aUsage, VkImageCreateFlags  flags,
    VkSampleCountFlagBits aSamples,
    VkImageLayout aLayout) :
    mInfo{}
{
    mInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
    mInfo.imageType = aImageType;
    mInfo.format = aFormat;
    mInfo.extent.width = aWidth;
    mInfo.extent.height = aHeight;
    mInfo.extent.depth = aDepth;
    mInfo.mipLevels = aMipLevels;
    mInfo.arrayLayers = aArraySize;
    mInfo.samples = aSamples;
    mInfo.tiling = aTiling;
    mInfo.usage = aUsage;
    mInfo.flags = flags;
    mInfo.initialLayout = aLayout;
    mInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
}

ImageViewCreateInfo::ImageViewCreateInfo(VkImage aImage, VkImageViewType aType, VkFormat aFormat,
    VkImageAspectFlags aAspectFlags)
    : ImageViewCreateInfo(aImage, aType, aFormat, 1, aAspectFlags)
{
}

ImageViewCreateInfo::ImageViewCreateInfo(VkImage aImage, VkImageViewType aType, VkFormat aFormat,
    uint32_t aArraySize, VkImageAspectFlags aAspectFlags)
    : ImageViewCreateInfo(aImage, aType, aFormat, 1, aArraySize, aAspectFlags)
{
}

ImageViewCreateInfo::ImageViewCreateInfo(VkImage aImage, VkImageViewType aType, VkFormat aFormat, uint32_t aMipLevels,
    uint32_t aArraySize, VkImageAspectFlags aAspectFlags)
    : mInfo{}
{
    mInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
    mInfo.components.r = VK_COMPONENT_SWIZZLE_R;
    mInfo.components.g = VK_COMPONENT_SWIZZLE_G;
    mInfo.components.b = VK_COMPONENT_SWIZZLE_B;
    mInfo.components.a = VK_COMPONENT_SWIZZLE_A;
    mInfo.format = aFormat;
    mInfo.image = aImage;
    mInfo.subresourceRange.aspectMask = aAspectFlags;
    mInfo.subresourceRange.baseArrayLayer = 0;
    mInfo.subresourceRange.layerCount = aArraySize;
    mInfo.subresourceRange.baseMipLevel = 0;
    mInfo.subresourceRange.levelCount = aMipLevels;
    mInfo.viewType = aType;
}
DescriptorWrite::DescriptorWrite(VkStructureType type, int index, uint32_t descriptorCount, VkDescriptorType& descriptorType,
            VkDescriptorBufferInfo& info, VkDescriptorImageInfo* descriptorImageInfo):
    write()
{
    write.sType = type;
    write.dstBinding = index;
    write.descriptorCount = descriptorCount;

    if(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER == descriptorType){
        write.descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        write.pImageInfo = descriptorImageInfo;
    }
    else {
        write.descriptorType = descriptorType;
        write.pBufferInfo = &info;
    }
}
DescriptorLayout::DescriptorLayout(int binding, int descriptorCount, VkDescriptorType& descriptorType, int stageFlags, int immulableSamplers):
    uniformAndSamplerBinding()
{
  uniformAndSamplerBinding.binding = binding;
  uniformAndSamplerBinding.descriptorCount = descriptorCount;
  uniformAndSamplerBinding.descriptorType = descriptorType;
  uniformAndSamplerBinding.stageFlags = stageFlags;
  uniformAndSamplerBinding.pImmutableSamplers = nullptr;


}

CmdPoolCreateInfo::CmdPoolCreateInfo(VkCommandPoolCreateFlags aFlags, uint32_t aFamilyIndex)
    : mInfo()
{
    mInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
    mInfo.pNext = nullptr;
    mInfo.flags = aFlags;
    mInfo.queueFamilyIndex = aFamilyIndex;
}

CmdBufferCreateInfo::CmdBufferCreateInfo(VkCommandBufferLevel aLevel, VkCommandPool aCmdPool)
    : mInfo()
{
    mInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    mInfo.level = aLevel;
    mInfo.commandPool = aCmdPool;
    mInfo.commandBufferCount = 1;
}

BufferCreateInfo::BufferCreateInfo(VkDeviceSize aSize,
    VkBufferUsageFlags aUsageFlags, VkBufferCreateFlags aCreateFlags)
    : mInfo()
{
    mInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
    mInfo.size = aSize;
    mInfo.usage = aUsageFlags;
    mInfo.flags = aCreateFlags;
}

ShaderModuleCreateInfo::ShaderModuleCreateInfo(const uint32_t* aCode, size_t aCodeSize,
    VkShaderModuleCreateFlags aFlags)
{
    mCreateInfo.sType = VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
    mCreateInfo.pNext = nullptr;
    mCreateInfo.codeSize = aCodeSize;
    mCreateInfo.pCode = aCode;
    mCreateInfo.flags = aFlags;
}

SemaphoreCreateInfo::SemaphoreCreateInfo(VkSemaphoreCreateFlags aFlags)
    : mInfo { VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO, nullptr, aFlags }
{
}

FenceCreateInfo::FenceCreateInfo(VkFenceCreateFlags aFlags)
    : mInfo()
{
    mInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    mInfo.flags = aFlags;
}

    FramebufferCreateInfo::FramebufferCreateInfo(VkFramebufferCreateFlags flags, VkRenderPass renderPass, uint32_t attachmentCount,
                                                 const VkImageView* pAttachments, uint32_t width, uint32_t height, uint32_t layers) : mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
        mInfo.flags = flags;
        mInfo.renderPass = renderPass;
        mInfo.attachmentCount = attachmentCount;
        mInfo.pAttachments = pAttachments;
        mInfo.width = width;
        mInfo.height = height;
        mInfo.layers = layers;
    }

    RenderPassCreateInfo::RenderPassCreateInfo(VkRenderPassCreateFlags flags, uint32_t attachmentCount, const VkAttachmentDescription* pAttachments,
    uint32_t subpassCount, const VkSubpassDescription* pSubpasses, uint32_t dependencyCount, const VkSubpassDependency* pDependencies) : mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
        mInfo.flags = flags;
        mInfo.attachmentCount = attachmentCount;
        mInfo.pAttachments = pAttachments;
        mInfo.subpassCount = subpassCount;
        mInfo.pSubpasses = pSubpasses;
        mInfo.dependencyCount = dependencyCount;
        mInfo.pDependencies = pDependencies;
    }

    MemoryAllocateInfo::MemoryAllocateInfo(VkDeviceSize allocationSize, uint32_t memoryTypeIndex) : mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
        mInfo.allocationSize = allocationSize;
        mInfo.memoryTypeIndex = memoryTypeIndex;
    }

    DescriptorSetLayoutCreateInfo::DescriptorSetLayoutCreateInfo(VkDescriptorSetLayoutCreateFlags flags, uint32_t bindingCount, const VkDescriptorSetLayoutBinding* pBindings) : mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
        mInfo.flags = flags;
        mInfo.bindingCount = bindingCount;
        mInfo.pBindings = pBindings;
    }

    PipelineLayoutCreateInfo::PipelineLayoutCreateInfo(VkPipelineLayoutCreateFlags flags, uint32_t setLayoutCount, const VkDescriptorSetLayout* pSetLayouts, uint32_t pushConstantRangeCount, const VkPushConstantRange* pPushConstantRanges) : mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
        mInfo.flags = flags;
        mInfo.setLayoutCount = setLayoutCount;
        mInfo.pSetLayouts = pSetLayouts;
        mInfo.pushConstantRangeCount = pushConstantRangeCount;
        mInfo.pPushConstantRanges = pPushConstantRanges;
    }

    SamplerCreateInfo::SamplerCreateInfo(VkFilter magFilter, VkFilter minFilter, VkSamplerMipmapMode mipmapMode, VkSamplerAddressMode addressModeU,
                                         VkSamplerAddressMode addressModeV, VkSamplerAddressMode addressModeW, float mipLodBias, VkBool32 anisotropyEnable,
                                         float maxAnisotropy, VkBool32 compareEnable, VkCompareOp compareOp, float minLod, float maxLod, VkBorderColor borderColor,
                                         VkBool32 unnormalizedCoordinates) : mInfo()
    {
        mInfo.sType = VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
        mInfo.pNext = nullptr;
        mInfo.magFilter = magFilter;
        mInfo.minFilter = minFilter;
        mInfo.mipmapMode = mipmapMode;
        mInfo.addressModeU = addressModeU;
        mInfo.addressModeV = addressModeV;
        mInfo.addressModeW = addressModeW;
        mInfo.mipLodBias = mipLodBias;
        mInfo.anisotropyEnable = anisotropyEnable;
        mInfo.anisotropyEnable = anisotropyEnable;
        mInfo.compareOp = compareOp;
        mInfo.minLod = minLod;
        mInfo.maxLod = maxLod;
        mInfo.borderColor = borderColor;
        mInfo.unnormalizedCoordinates = unnormalizedCoordinates;
    }
}