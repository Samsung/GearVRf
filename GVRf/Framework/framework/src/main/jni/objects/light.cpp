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
 * JNI
 ***************************************************************************/
#include "light.h"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_access.hpp"
#include "objects/components/shadow_map.h"
#include "objects/textures/render_texture.h"
#include "objects/components/custom_camera.h"

namespace gvr {

/*
 * Loads the uniforms associated with this light
 * into the GPU if they have changed.
 * @param program   ID of shader program light is bound ot
 * @param texIndex  next available texture location
 */
    void Light::render(int program, int texIndex)
    {
        auto it = dirty_.find(program);

        if (it != dirty_.end() && !it->second)
            return;
        if (lightID_.empty())
        {
            return;
        }
        dirty_[program] = false;
        std::string key;
        std::string lname = lightID_ + ".";
        int offset;

        for (auto it = floats_.begin(); it != floats_.end(); ++it)
        {
            key = lname + it->first;
            offset = getOffset(it->first, program);
            if (offset <= 0)
            {
                offset = glGetUniformLocation(program, key.c_str());
                offsets_[it->first][program] = offset;
            }
            if (offset >= 0)
                glUniform1f(offset, it->second);
    #ifdef DEBUG_LIGHT
            LOGD("LIGHT: %s = %f\n", key.c_str(), it->second);
    #endif
        }

        for (auto it = vec3s_.begin(); it != vec3s_.end(); ++it)
        {
            offset = getOffset(it->first, program);
            key = lname + it->first;
            if (offset <= 0)
            {
                offset = glGetUniformLocation(program, key.c_str());
                offsets_[it->first][program] = offset;
              }
            if (offset >= 0)
            {
                glm::vec3 v = it->second;
                glUniform3f(offset, v.x, v.y, v.z);
    #ifdef DEBUG_LIGHT
                LOGD("LIGHT: %s = %f, %f, %f\n", key.c_str(), v.x, v.y, v.z);
    #endif
            }
        }

        for (auto it = vec4s_.begin(); it != vec4s_.end(); ++it)
        {
            offset = getOffset(it->first, program);
            key = lname + it->first;
            if (offset <= 0)
            {
                offset = glGetUniformLocation(program, key.c_str());
                offsets_[it->first][program] = offset;
            }
            if (offset >= 0)
            {
                glm::vec4 v = it->second;
                glUniform4f(offset, v.x, v.y, v.z, v.w);
    #ifdef DEBUG_LIGHT
                LOGD("LIGHT: %s = %f, %f, %f, %f\n", key.c_str(), v.x, v.y, v.z, v.w);
    #endif
            }
        }
        for (auto it = mat4s_.begin(); it != mat4s_.end(); ++it)
        {
            offset = getOffset(it->first, program);
            key = lname + it->first;
            if (offset <= 0)
            {
                offset = glGetUniformLocation(program, key.c_str());
                offsets_[it->first][program] = offset;
            }
            if (offset >= 0)
            {
                glm::mat4 v = it->second;
                glUniformMatrix4fv(offset, 1, GL_FALSE, glm::value_ptr(v));
    #ifdef DEBUG_LIGHT
                LOGD("LIGHT: %s\n", key.c_str());
    #endif
            }
        }
    }


    /**
     * Renders the shadow map for this light.
     * @param scene             Scene to use for rendering
     * @param shader_manager    ShaderManager to use
     * @param texIndex          texture index for shadow map
     */
    bool Light::makeShadowMap(Scene* scene, ShaderManager* shader_manager, int texIndex)
    {
        ShadowMap* shadowMap = getShadowMap();
        if ((shadowMap == nullptr) || !shadowMap->hasTexture())
        {
            setFloat("shadow_map_index", -1);
            return false;
        }
        shadowMap->setLayerIndex(texIndex);
        setFloat("shadow_map_index", (float) texIndex);
        Renderer::getInstance()->cullAndRender(shadowMap, scene, shader_manager,
                     (PostEffectShaderManager*) nullptr,
                     (RenderTexture*) nullptr,
                     (RenderTexture*) nullptr);
        return true;
    }
 
}
