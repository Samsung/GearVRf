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

/***************************************************************************
 * Renders a scene, a screen.
 ***************************************************************************/

#include "renderer.h"
#include "gl_renderer.h"
#include "vulkan_renderer.h"

namespace gvr {
Renderer* Renderer::instance = nullptr;
bool Renderer::isVulkan_ = false;

/***
    Till we have Vulkan implementation, lets create GLRenderer by-default
***/
Renderer* Renderer::getInstance(std::string type){
    if(nullptr == instance){
     if(0){
            instance = new VulkanRenderer();
            isVulkan_ = true;
        }
        else {
            instance = new GLRenderer();
        }
        atexit(resetInstance);      // Destruction of instance registered at runtime exit
    }
    return instance;
}

}
