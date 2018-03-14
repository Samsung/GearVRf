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

#include <unordered_map>
#include "vulkan_flags.h"

namespace  gvr {

    namespace vkflags {

        std::unordered_map<int, int> glToVulkan;

        void initVkRenderFlags() {

            glToVulkan[GL_ONE] = VkBlendFactor::VK_BLEND_FACTOR_ONE;
            glToVulkan[GL_SRC_ALPHA] = VkBlendFactor::VK_BLEND_FACTOR_SRC_ALPHA;
            glToVulkan[GL_SRC_COLOR] = VkBlendFactor::VK_BLEND_FACTOR_SRC_COLOR;
            glToVulkan[GL_ONE_MINUS_SRC_COLOR] = VkBlendFactor::VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR;
            glToVulkan[GL_DST_COLOR] = VkBlendFactor::VK_BLEND_FACTOR_DST_COLOR;
            glToVulkan[GL_ONE_MINUS_DST_COLOR] = VkBlendFactor::VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR;
            glToVulkan[GL_SRC_ALPHA] = VkBlendFactor::VK_BLEND_FACTOR_SRC_ALPHA;
            glToVulkan[GL_ONE_MINUS_SRC_ALPHA] = VkBlendFactor::VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
            glToVulkan[GL_DST_ALPHA] = VkBlendFactor::VK_BLEND_FACTOR_DST_ALPHA;
            glToVulkan[GL_ONE_MINUS_DST_ALPHA] = VkBlendFactor::VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA;
            glToVulkan[GL_CONSTANT_COLOR] = VkBlendFactor::VK_BLEND_FACTOR_CONSTANT_COLOR;
            glToVulkan[GL_ONE_MINUS_CONSTANT_COLOR] = VkBlendFactor::VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR;
            glToVulkan[GL_CONSTANT_ALPHA] = VkBlendFactor::VK_BLEND_FACTOR_CONSTANT_ALPHA;
            glToVulkan[GL_ONE_MINUS_CONSTANT_ALPHA] = VkBlendFactor::VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA;
            glToVulkan[GL_SRC_ALPHA_SATURATE] = VkBlendFactor::VK_BLEND_FACTOR_SRC_ALPHA_SATURATE;


            glToVulkan[GL_KEEP] = VkStencilOp::VK_STENCIL_OP_KEEP;
            glToVulkan[GL_ZERO] = VkStencilOp::VK_STENCIL_OP_ZERO;
            glToVulkan[GL_REPLACE] = VkStencilOp::VK_STENCIL_OP_REPLACE;
            glToVulkan[GL_INCR] = VkStencilOp::VK_STENCIL_OP_INCREMENT_AND_CLAMP;
            glToVulkan[GL_INCR_WRAP] = VkStencilOp::VK_STENCIL_OP_INCREMENT_AND_WRAP;
            glToVulkan[GL_DECR] = VkStencilOp::VK_STENCIL_OP_DECREMENT_AND_CLAMP;
            glToVulkan[GL_DECR_WRAP] = VkStencilOp::VK_STENCIL_OP_DECREMENT_AND_WRAP;
            glToVulkan[GL_INVERT] = VkStencilOp::VK_STENCIL_OP_INVERT;


            glToVulkan[GL_NEVER] = VkCompareOp::VK_COMPARE_OP_NEVER;
            glToVulkan[GL_LESS] = VkCompareOp::VK_COMPARE_OP_LESS;
            glToVulkan[GL_LEQUAL] = VkCompareOp::VK_COMPARE_OP_LESS_OR_EQUAL;
            glToVulkan[GL_GREATER] = VkCompareOp::VK_COMPARE_OP_GREATER;
            glToVulkan[GL_GEQUAL] = VkCompareOp::VK_COMPARE_OP_GREATER_OR_EQUAL;
            glToVulkan[GL_EQUAL] = VkCompareOp::VK_COMPARE_OP_EQUAL;
            glToVulkan[GL_NOTEQUAL] = VkCompareOp::VK_COMPARE_OP_NOT_EQUAL;
            glToVulkan[GL_ALWAYS] = VkCompareOp::VK_COMPARE_OP_ALWAYS;

        }
    }
}