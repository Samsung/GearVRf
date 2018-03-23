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



#ifndef FRAMEWORK_VK_RENDER_TEXTURE_OFFSCREEN_H
#define FRAMEWORK_VK_RENDER_TEXTURE_OFFSCREEN_H

#include "vk_render_to_texture.h"

namespace gvr {
    class VkRenderTextureOffScreen : public VkRenderTexture
    {
    public:
        explicit VkRenderTextureOffScreen(int width, int height, int sample_count = 1);
        void bind();
        bool isReady();
        bool readRenderResult(uint8_t *readback_buffer);
        bool accessRenderResult(uint8_t **readback_buffer);
        void unmapDeviceMemory();
    };

}

#endif //FRAMEWORK_VK_RENDER_TEXTURE_OFFSCREEN_H