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
#include <sys/system_properties.h>
#include <cstring>

namespace gvr {
Renderer* Renderer::instance = nullptr;
bool Renderer::isVulkan_ = false;


Renderer* Renderer::getInstance(std::string type)
{
    if (instance == nullptr)
    {
        if (useVulkanInstance())
        {
            instance = new VulkanRenderer();
            if (static_cast<VulkanRenderer*>(instance)->getCore() != NULL)
            {
                isVulkan_ = true;
            }
            else
            {
                LOGE("Vulkan is not supported on your device");
            }
        }
        else
        {
            instance = new GLRenderer();
        }
    }
    return instance;
}

bool Renderer::useVulkanInstance(){
    // Debug setting selecting Vulkan renderer:
    //     setprop debug.gearvrf.vulkan <value>
    //     <property not present>, <empty>, not recognized, or 0
    //                            - use setting from gvr.xml (not implemented yet. Select OpenGL ES.)
    //     1                      - pretend gvr.xml asked for Vulkan (not implemented yet. Select Vulkan.)
    //     2                      - always use Vulkan.
    bool useVulkan = false; // TODO: obtain setting from gvr.xml
    const prop_info *pi = __system_property_find("debug.gearvrf.vulkan");
    char buffer[PROP_VALUE_MAX];
    int len = 0;
    if( pi ) {
        len = __system_property_read(pi,0,buffer);
    }
    if( len ) {
        if( strcmp(buffer,"1") == 0 || // TODO: "1" should check if Vulkan is supported
            strcmp(buffer,"2") == 0
                ) {
            useVulkan = true;
            LOGI("Vulkan renderer: debug.gearvrf.vulkan is \"%s\".", buffer );
        } else {
            LOGI("OpenGL ES renderer: debug.gearvrf.vulkan is \"%s\".", buffer );
        }
    }

    return useVulkan;
}

}
