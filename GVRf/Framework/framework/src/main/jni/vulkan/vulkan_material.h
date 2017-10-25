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

#ifndef FRAMEWORK_VULKAN_MATERIAL_H
#define FRAMEWORK_VULKAN_MATERIAL_H

#include "objects/shader_data.h"
#include "vulkan/vulkan_headers.h"
#include "util/gvr_log.h"

/**
 * Vulkan implementation of Material which keeps uniform data
 * in a VulkanUniformBlock.
 */
namespace gvr {

    class VulkanMaterial : public ShaderData
    {
    public:
        VulkanMaterial(const char* uniform_desc, const char* texture_desc);
        virtual ~VulkanMaterial() {}
        void useGPUBuffer(bool flag){
            uniforms_.useGPUBuffer(flag);
        }
        virtual VulkanUniformBlock& uniforms() { return uniforms_; }
        virtual const VulkanUniformBlock& uniforms() const { return uniforms_; }
    protected:
        VulkanUniformBlock uniforms_;
    };
}

#endif //FRAMEWORK_VULKAN_MATERIAL_H
