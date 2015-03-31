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

#include "view_manager.h"
#include "activity_jni.h"
#include <jni.h>
#include "../engine/renderer/renderer.h"
#include "../objects/components/camera.h"
namespace gvr {

extern "C"
{

void Java_org_gearvrf_GVRViewManager_renderCamera(
        JNIEnv * jni, jclass clazz, jlong appPtr,
        jlong jscene, jlong jcamera, jlong jrender_texture,
        jlong jshader_manager, jlong jpost_effect_shader_manager,
        jlong jpost_effect_render_texture_a,
        jlong jpost_effect_render_texture_b
        )
{
    GVRActivity *activity = (GVRActivity*)((App *)appPtr)->GetAppInterface();

    std::shared_ptr<Scene> scene =
            *reinterpret_cast<std::shared_ptr<Scene>*>(jscene);
    std::shared_ptr<Camera> camera =
            *reinterpret_cast<std::shared_ptr<Camera>*>(jcamera);
    std::shared_ptr<RenderTexture> render_texture =
            *reinterpret_cast<std::shared_ptr<RenderTexture>*>(jrender_texture);
    std::shared_ptr<ShaderManager> shader_manager =
            *reinterpret_cast<std::shared_ptr<ShaderManager>*>(jshader_manager);
    std::shared_ptr<PostEffectShaderManager> post_effect_shader_manager =
            *reinterpret_cast<std::shared_ptr<PostEffectShaderManager>*>(jpost_effect_shader_manager);
    std::shared_ptr<RenderTexture> post_effect_render_texture_a =
            *reinterpret_cast<std::shared_ptr<RenderTexture>*>(jpost_effect_render_texture_a);
    std::shared_ptr<RenderTexture> post_effect_render_texture_b =
            *reinterpret_cast<std::shared_ptr<RenderTexture>*>(jpost_effect_render_texture_b);

    activity->viewManager->renderCamera(activity->Scene,
            scene, camera, render_texture, shader_manager,
            post_effect_shader_manager, post_effect_render_texture_a,
            post_effect_render_texture_b,
            activity->viewManager->mvp_matrix);
}

} // extern "C"

//=============================================================================
//                             GVRViewManager
//=============================================================================

GVRViewManager::GVRViewManager(JNIEnv & jni_, jobject activityObject_)
{
    // initial
	m_frameRendered = 0;
    m_fps = 0.0;
    m_startTime = m_currentTime = 0.0f;
    gNumFrame = 0;

    LOG("GVRViewManager::GVRViewManager");
}

GVRViewManager::~GVRViewManager() {
    LOG( "GVRViewManager::~GVRViewManager()");
}

void GVRViewManager::renderCamera(OvrSceneView &ovr_scene,
            std::shared_ptr<Scene> scene,
            std::shared_ptr<Camera> camera,
            std::shared_ptr<RenderTexture> render_texture,
            std::shared_ptr<ShaderManager> shader_manager,
            std::shared_ptr<PostEffectShaderManager> post_effect_shader_manager,
            std::shared_ptr<RenderTexture> post_effect_render_texture_a,
            std::shared_ptr<RenderTexture> post_effect_render_texture_b,
            glm::mat4 mvp) {
#ifdef GVRF_FBO_FPS
	// starting to collect rendering time
	// first flash GPU tasks
	glFinish();

	// collect data
	struct timeval start, end;
	double t1, t2;
	float fps = 0.0;
	gettimeofday(&start, NULL);
#endif

    if( camera->render_mask() == 1){
        glClearColor( 0.0f, 1.0f, 0.0f, 1.0f );
    }else{
        glClearColor( 1.0f, 0.0f, 0.0f, 1.0f );
    }
    glClear( GL_COLOR_BUFFER_BIT );

    Renderer::renderCamera(scene, camera, render_texture, shader_manager,
                post_effect_shader_manager, post_effect_render_texture_a,
                post_effect_render_texture_b, mvp);

#ifdef GVRF_FBO_FPS
    // finish rendering
	glFinish(); // force rendering

	gettimeofday(&end, NULL);
	t1 = start.tv_sec + (start.tv_usec / 1000000.0);
	t2 = end.tv_sec + (end.tv_usec / 1000000.0);
	gTotalSec += (t2 - t1);
	gNumFrame++;

	fps = (float)gNumFrame/gTotalSec;
	if(!(gNumFrame%50) )
	{
	    LOGI("FPS is %.2f gNumFrame=%d, gTotalSec = %.2f", fps, gNumFrame, gTotalSec);
	}
#endif

}
}
