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

#ifndef VIEW_MANAGER_H
#define VIEW_MANAGER_H

#include <vector>
#include <memory>
#include "App.h"
#include "ModelView.h"
#include "AppLocal.h"
#include "util/gvr_jni.h"
#include "glm/glm.hpp"
#include "objects/textures/render_texture.h"

namespace gvr {

class Camera;
class Distorter;
class DistortionGrid;
class Scene;
class SceneObject;
class PostEffectData;
class PostEffectShaderManager;
class RenderData;
class RenderTexture;
class ShaderManager;


#define OCULUS_EXAMPLE_CODE
//#define GVRF_FBO_FPS

class GVRViewManager
{
public:
    GVRViewManager( JNIEnv & jni_, jobject activityObject_);
    ~GVRViewManager();
    void renderCamera(OVR::OvrSceneView &ovr_scene,
                        Scene* scene,
                        Camera* camera,
                        RenderTexture* render_texture,
                        ShaderManager* shader_manager,
                        PostEffectShaderManager* post_effect_shader_manager,
                        RenderTexture* post_effect_render_texture_a,
                        RenderTexture* post_effect_render_texture_b,
                        glm::mat4 mvp);

    glm::mat4 mvp_matrix;

    // collect data
    float  m_fps;
    int    m_frameRendered;
    float  m_startTime;
    float  m_currentTime;
    int    gNumFrame;
    float  gTotalSec;

};
}
#endif
