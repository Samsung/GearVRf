//
// Created by roshan on 10/12/16.
//

#ifndef FRAMEWORK_VULKAN_HEADERS_H
#define FRAMEWORK_VULKAN_HEADERS_H

#include "vulkan/vulkan_wrapper.h"
#include "vulkanInfoWrapper.h"
#include <vector>
#include "glm/glm.hpp"
#include "objects/uniform_block.h"
#include "vulkan/vulkanCore.h"
namespace gvr{
class Descriptor{
public:
    Descriptor(){}
    ~Descriptor(){
        delete ubo;
    }
    Descriptor(const std::string& ubo_descriptor): ubo(nullptr){
        ubo = new VulkanUniformBlock(ubo_descriptor);
    }
    void createDescriptor(VkDevice &,VulkanCore*, int, VkShaderStageFlagBits);
    void  createBuffer(VkDevice &device,VulkanCore* vk);
    void createLayoutBinding(int binding_index,int stageFlags, bool sampler=false);
    void createDescriptorWriteInfo(int binding_index,int stageFlags, VkDescriptorSet& descriptor, bool sampler=false);
    VulkanUniformBlock* getUBO();
    VkDescriptorSetLayoutBinding& getLayoutBinding();
    VkWriteDescriptorSet& getDescriptorSet();


private:

    VulkanUniformBlock* ubo;
     VkDescriptorSetLayoutBinding layout_binding;
     VkWriteDescriptorSet writeDescriptorSet;
};

}
#endif //FRAMEWORK_VULKAN_HEADERS_H
