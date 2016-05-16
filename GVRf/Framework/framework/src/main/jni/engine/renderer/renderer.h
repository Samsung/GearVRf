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
class Light;

/*
 * These uniforms are commonly used in shaders.
 * They are calculated by the GearVRF renderer.
 */
struct ShaderUniformsPerObject {
    glm::mat4   u_model;        // Model matrix
    glm::mat4   u_view;         // View matrix
    glm::mat4   u_proj;         // projection matrix
    glm::mat4   u_view_inv;     // inverse of View matrix
    glm::mat4   u_mv;           // ModelView matrix
    glm::mat4   u_mvp;          // ModelViewProjection matrix
    glm::mat4   u_mv_it;        // inverse transpose of ModelView
    int         u_right;        // 1 = right eye, 0 = left
};

struct RenderState {
    int                     render_mask;
    int                     viewportX;
    int                     viewportY;
    int                     viewportWidth;
    int                     viewportHeight;
    Scene*                  scene;
    Material*               material_override;
    ShaderUniformsPerObject uniforms;
    ShaderManager*          shader_manager;
};

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
    static void renderShadowMap(RenderState& rstate, Camera* camera, GLuint framebufferId, std::vector<SceneObject*>& scene_objects);
    static void makeShadowMaps(Scene* scene, ShaderManager* shader_manager, int width, int height);
private:
    static void cullFromCamera(Scene *scene, Camera *camera,
            ShaderManager* shader_manager,
            std::vector<SceneObject*>& scene_objects);

    static void renderRenderData(RenderState& rstate, RenderData* render_data);

    static void renderMesh(RenderState& rstate, RenderData* render_data);

    static void renderMaterialShader(RenderState& rstate, RenderData* render_data, Material *material);

    static void renderPostEffectData(Camera* camera,
            RenderTexture* render_texture, PostEffectData* post_effect_data,
            PostEffectShaderManager* post_effect_shader_manager);

    static bool checkTextureReady(Material* material);
    static void cullDepthMaps(RenderState& rstate,Camera* camera, std::vector<SceneObject*>& scene_objects);
    static void occlusion_cull(Scene* scene,
            std::vector<SceneObject*>& scene_objects,
            ShaderManager *shader_manager, glm::mat4 vp_matrix);
    static void build_frustum(float frustum[6][4], const float *vp_matrix);
    static void frustum_cull(glm::vec3 camera_position, SceneObject *object,
            float frustum[6][4], std::vector<SceneObject*>& scene_objects,
            bool continue_cull, int planeMask);
    static void state_sort();

    static void set_face_culling(int cull_face);

    static bool isShader3d(const Material* curr_material);
    static bool isDefaultPosition3d(const Material* curr_material);
    void light_cull(Scene *scene, ShaderManager* shader_manager);
    Renderer(const Renderer& render_engine);
    Renderer(Renderer&& render_engine);
    Renderer& operator=(const Renderer& render_engine);
    Renderer& operator=(Renderer&& render_engine);
};

}
#endif
