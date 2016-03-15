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
#ifndef GL_ES_VERSION_3_0
#include "GLES3/gl3.h"
#include <GLES2/gl2ext.h>
#include "GLES3/gl3ext.h"
#endif

#include "glm/glm.hpp"

#include "objects/eye_type.h"
#include "objects/mesh.h"
#include "objects/bounding_volume.h"
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
    static void renderCamera(Scene* scene, Camera* camera, int framebufferId,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    static void renderCamera(Scene* scene, Camera* camera, int framebufferId,
              int viewportX, int viewportY, int viewportWidth, int viewportHeight,
              ShaderManager* shader_manager,
              PostEffectShaderManager* post_effect_shader_manager,
              RenderTexture* post_effect_render_texture_a,
              RenderTexture* post_effect_render_texture_b, int modeShadow);

    static void renderCamera(Scene* scene, Camera* camera,
            RenderTexture* render_texture, ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    static void renderCamera(Scene* scene, Camera* camera, int viewportX,
            int viewportY, int viewportWidth, int viewportHeight,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    static void renderCamera(Scene* scene, Camera* camera,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    static void cull(Scene *scene, Camera *camera,
            ShaderManager* shader_manager);

    static void initializeStats();
    static void resetStats();
    static int getNumberDrawCalls();
    static int getNumberTriangles();

private:
    static void renderRenderData(RenderData* render_data,
            const glm::mat4& view_matrix, const glm::mat4& projection_matrix,
            int render_mask, ShaderManager* shader_manager, int modeShadow);

    static void renderMesh(RenderData* render_data,
            const glm::mat4& view_matrix, const glm::mat4& projection_matrix,
            int render_mask, ShaderManager* shader_manager, int modeShadow);

    static void renderMaterialShader(RenderData* render_data,
            const glm::mat4& view_matrix, const glm::mat4& projection_matrix,
            int render_mask, ShaderManager* shader_manager, int modeShadow,
            Material *material);

    static void renderPostEffectData(Camera* camera,
            RenderTexture* render_texture, PostEffectData* post_effect_data,
            PostEffectShaderManager* post_effect_shader_manager);

    static bool checkTextureReady(Material* material);

    static void occlusion_cull(Scene* scene,
            std::vector<SceneObject*>& scene_objects,
            ShaderManager *shader_manager, glm::mat4 vp_matrix);
    static void build_frustum(float frustum[6][4], const float *vp_matrix);
    static void frustum_cull(Camera *camera, SceneObject *object,
            float frustum[6][4], std::vector<SceneObject*>& scene_objects,
            bool continue_cull, int planeMask);	
	static void state_sort();

    static void set_face_culling(int cull_face);

    static bool isShader3d(const Material* curr_material);
    static bool isDefaultPosition3d(const Material* curr_material);
    static void calculateShadow(ShaderManager* shader_manager,
            const Material* curr_material, const glm::mat4& model_matrix,
            const int modeShadow, glm::vec3& lightPosition,
            glm::mat4& vp_matrixLightModel);

    Renderer(const Renderer& render_engine);
    Renderer(Renderer&& render_engine);
    Renderer& operator=(const Renderer& render_engine);
    Renderer& operator=(Renderer&& render_engine);
};

}
#endif
