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

#ifndef RENDERER_H_
#define RENDERER_H_

#include <vector>
#include <memory>

#define __gl2_h_
#include "EGL/egl.h"
#include "EGL/eglext.h"
#include "GLES3/gl3.h"
#include <GLES2/gl2ext.h>
#include "GLES3/gl3ext.h"

#include "glm/glm.hpp"

#include "objects/eye_type.h"
#include "objects/mesh.h"
#include "gl/gl_program.h"

namespace gvr {
class Camera;
class Scene;
class SceneObject;
class PostEffectData;
class PostEffectShaderManager;
class RenderData;
class RenderTexture;
class ShaderManager;

class Renderer {
private:
    Renderer();

public:
    static void renderCamera(Scene* scene,
            Camera* camera,
            int framebufferId,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    static void renderCamera(Scene* scene,
            Camera* camera,
            RenderTexture* render_texture,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    static void renderCamera(Scene* scene,
            Camera* camera,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    static void renderCamera(Scene* scene,
            Camera* camera,
            RenderTexture* render_texture,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b,
            glm::mat4 vp_matrix);

    static void initializeStats();
    static void resetStats();
    static int getNumberDrawCalls();
    static int getNumberTriangles();
    static float getDrawTime();
    static void startGpuTimer(int eye);
    static void stopGpuTimer(int eye);
    static float getGpuTimerResult(int eye);

private:
    static void renderRenderData(RenderData* render_data,
    		const glm::mat4& view_matrix, const glm::mat4& projection_matrix, int render_mask,
            ShaderManager* shader_manager);
    static void renderPostEffectData(Camera* camera,
            RenderTexture* render_texture,
            PostEffectData* post_effect_data,
            PostEffectShaderManager* post_effect_shader_manager);

    static void occlusion_cull(Scene* scene, std::vector <  SceneObject* > scene_objects);
    static void frustum_cull(Scene* scene,
        std::vector < SceneObject* > scene_objects,
        std::vector < RenderData* >& render_data_vector,
        glm::mat4 vp_matrix,
        ShaderManager* shader_manager);
    static void build_frustum(float frustum[6][4], float mvp_matrix[16]);
    static bool is_cube_in_frustum( float frustum[6][4], const float *vertex_limit);

    Renderer(const Renderer& render_engine);
    Renderer(Renderer&& render_engine);
    Renderer& operator=(const Renderer& render_engine);
    Renderer& operator=(Renderer&& render_engine);
};

}
#endif
