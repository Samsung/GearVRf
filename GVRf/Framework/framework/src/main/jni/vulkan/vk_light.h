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

#ifndef FRAMEWORK_VK_LIGHT_H
#define FRAMEWORK_VK_LIGHT_H


#include <string>

#include "objects/light.h"
#include "vulkan/vulkan_material.h"
#include "shaders/shader.h"

namespace gvr
{

/**
 * Vulkan implementation of Material which keeps uniform data
 * in a VulkanUniformBlock.
 */
    class VKLight : public Light
    {
    public:
        explicit VKLight(const char* uniform_desc, const char* texture_desc)
                :   Light(), uniforms_(uniform_desc, texture_desc, LIGHT_UBO_INDEX, "Lights_ubo")
        {
            uniforms_.useGPUBuffer(true);
        }

        virtual ShaderData& uniforms()
        {
            return uniforms_;
        }

        void useGPUBuffer(bool flag)
        {
            uniforms_.useGPUBuffer(flag);
        }

        virtual const ShaderData& uniforms() const
        {
            return uniforms_;
        }

    protected:
        VulkanMaterial uniforms_;
    };
}

#endif //FRAMEWORK_VK_LIGHT_H
