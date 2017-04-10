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

#include <GLES3/gl3.h>
#include <engine/renderer/renderer.h>
#include "configuration_manager.h"

namespace gvr {

    ConfigurationManager::ConfigurationManager() {
    }

    ConfigurationManager::~ConfigurationManager() {
    }


    void ConfigurationManager::configureRendering(bool useStencil) {
        calculateMaxLights();
        Renderer::getInstance()->setUseStencilBuffer(useStencil);
    }

    /*
     * Calculates MAX lights based on Uniform count in Vertex and Fragment Shader
     * */
    void ConfigurationManager::calculateMaxLights(){
        int maxUniformVS = 0;
        int maxUniformFS = 0;
        glGetIntegerv(GL_MAX_VERTEX_UNIFORM_COMPONENTS, &maxUniformVS);
        glGetIntegerv(GL_MAX_FRAGMENT_UNIFORM_COMPONENTS, &maxUniformFS);

        if ((maxUniformVS < 1024) || (maxUniformFS < 1024)) {
            max_lights = 13;
        }
        else {
            max_lights = 16;
        }
    }

    int ConfigurationManager::getMaxLights() {
        return max_lights;
    }
}

