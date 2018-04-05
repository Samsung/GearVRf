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

#include "vulkan/vulkan_material.h"

namespace gvr
{
    VulkanMaterial::VulkanMaterial(const char* uniform_desc, const char* texture_desc)
     : ShaderData(texture_desc),
       uniforms_(uniform_desc, MATERIAL_UBO_INDEX, "Material_ubo")
    {
        uniforms_.useGPUBuffer(true);
    }

    VulkanMaterial::VulkanMaterial(const char* uniform_desc, const char* texture_desc, int bindingPoint, const char* blockName)
            : ShaderData(texture_desc),
              uniforms_(uniform_desc, bindingPoint, blockName)
    {
        uniforms_.useGPUBuffer(true);
    }
}