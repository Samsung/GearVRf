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
#include "shadow_sorter.h"
#include "objects/scene.h"
#include "shaders/shader.h"

namespace gvr {

bool ShadowRenderSorter::isValid(RenderState& rstate, Renderable& r)
{
    if (!r.renderData->cast_shadows())
    {
        return false;
    }
    if (rstate.javaEnv)
    {
        r.shader = selectShader(rstate, r);
        r.material = &mShadowMaterial;
        r.renderModes = mShadowRenderMode;
        return r.shader != nullptr;
    }
    return false;
}

Shader* ShadowRenderSorter::selectShader(const RenderState& rstate, Renderable& r)
{
    int index = r.mesh->hasBones() ? 1 : 0;
    Shader* shader = mDepthShader[index];
    if (shader != nullptr)
    {
        return shader;
    }
    const char* depthShaderName = index ? "GVRDepthShader$a_bone_weights$a_bone_indices" : "GVRDepthShader";
    shader = rstate.shader_manager->findShader(depthShaderName);
    if (shader == nullptr)
    {
    #ifdef DEBUG_RENDER
        LOGD("RENDER: making depth shaders");
    #endif
        if (rstate.scene->makeDepthShaders(&mRenderer, rstate.javaSceneObject))
        {
            shader = rstate.shader_manager->findShader(depthShaderName);
            if (shader == nullptr)
            {
                LOGE("RENDER: cannot find depth shader %s", depthShaderName);
                return nullptr;
            }
        }
    }
    mDepthShader[index] = shader;
    return shader;
}

}
