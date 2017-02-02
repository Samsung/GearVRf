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

#include "gl/gl_headers.h"

#include "glm/glm.hpp"
#include "batch.h"
#include "objects/eye_type.h"
#include "objects/mesh.h"
#include "objects/bounding_volume.h"
#include "gl/gl_program.h"
#include <unordered_map>
#include "batch_manager.h"

typedef unsigned long Long;
namespace gvr {
extern bool use_multiview;
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
    glm::mat4   u_view_[2];     // for multiview
    glm::mat4   u_view_inv;     // inverse of View matrix
    glm::mat4   u_view_inv_[2];     // inverse of View matrix
    glm::mat4   u_mv;           // ModelView matrix
    glm::mat4   u_mv_[2];           // ModelView matrix
    glm::mat4   u_mvp;          // ModelViewProjection matrix
    glm::mat4   u_mvp_[2];          // ModelViewProjection matrix
    glm::mat4   u_mv_it;        // inverse transpose of ModelView
    glm::mat4   u_mv_it_[2];        // inverse transpose of ModelView
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
    bool shadow_map;
};

class Renderer {
public:
    void resetStats() {
        numberDrawCalls = 0;
        numberTriangles = 0;
    }
    bool isVulkanInstace(){
        return isVulkan_;
    }
    void freeBatch(Batch* batch){
        batch_manager->freeBatch(batch);
    }
    int getNumberDrawCalls() {
        return numberDrawCalls;
    }

     int getNumberTriangles() {
        return numberTriangles;
     }
     int incrementTriangles(int number=1){
        return numberTriangles += number;
     }
     int incrementDrawCalls(){
        return ++numberDrawCalls;
     }
     static Renderer* getInstance(const char* type = " ");
     static void resetInstance(){
        delete instance;
     }
     virtual void initializeStats();
     virtual void set_face_culling(int cull_face) = 0;
     virtual void renderRenderDataVector(RenderState &rstate);
     virtual void cull(Scene *scene, Camera *camera,
            ShaderManager* shader_manager);
     virtual void renderRenderData(RenderState& rstate, RenderData* render_data);


     virtual void renderCamera(Scene* scene, Camera* camera,
             ShaderManager* shader_manager,
             PostEffectShaderManager* post_effect_shader_manager,
             RenderTexture* post_effect_render_texture_a,
             RenderTexture* post_effect_render_texture_b) = 0;

     virtual void renderCamera(Scene* scene, Camera* camera, int viewportX,
             int viewportY, int viewportWidth, int viewportHeight,
             ShaderManager* shader_manager,
             PostEffectShaderManager* post_effect_shader_manager,
             RenderTexture* post_effect_render_texture_a,
             RenderTexture* post_effect_render_texture_b)=0;

     virtual void renderCamera(Scene* scene, Camera* camera, int framebufferId,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b) = 0;

     virtual void renderCamera(Scene* scene, Camera* camera,
            RenderTexture* render_texture, ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b) = 0;

    virtual void restoreRenderStates(RenderData* render_data) = 0;
    virtual void setRenderStates(RenderData* render_data, RenderState& rstate) = 0;
    virtual void renderShadowMap(RenderState& rstate, Camera* camera, GLuint framebufferId, std::vector<SceneObject*>& scene_objects) = 0;
    virtual void makeShadowMaps(Scene* scene, ShaderManager* shader_manager, int width, int height) = 0;

private:
    static bool isVulkan_;
    virtual void build_frustum(float frustum[6][4], const float *vp_matrix);
    virtual void frustum_cull(glm::vec3 camera_position, SceneObject *object,
            float frustum[6][4], std::vector<SceneObject*>& scene_objects,
            bool continue_cull, int planeMask);

    virtual void state_sort();
    virtual bool isShader3d(const Material* curr_material);
    virtual bool isDefaultPosition3d(const Material* curr_material);

    Renderer(const Renderer& render_engine);
    Renderer(Renderer&& render_engine);
    Renderer& operator=(const Renderer& render_engine);
    Renderer& operator=(Renderer&& render_engine);
    BatchManager* batch_manager;
    static Renderer* instance;
    
protected:
    Renderer();
    virtual ~Renderer(){
        delete batch_manager;
    }
    virtual void renderMesh(RenderState& rstate, RenderData* render_data) = 0;
    virtual void renderMaterialShader(RenderState& rstate, RenderData* render_data, Material *material) = 0;
    virtual void occlusion_cull(Scene* scene,
                std::vector<SceneObject*>& scene_objects,
                ShaderManager *shader_manager, glm::mat4 vp_matrix) = 0;
    void addRenderData(RenderData *render_data);
    virtual bool occlusion_cull_init(Scene* scene, std::vector<SceneObject*>& scene_objects);
    virtual void cullFromCamera(Scene *scene, Camera *camera,
            ShaderManager* shader_manager,
            std::vector<SceneObject*>& scene_objects);

    virtual void
            renderPostEffectData(Camera* camera,
            RenderTexture* render_texture, PostEffectData* post_effect_data,
            PostEffectShaderManager* post_effect_shader_manager);

    std::vector<RenderData*> render_data_vector;
    int numberDrawCalls;
    int numberTriangles;
    bool useStencilBuffer_ = false;

public:
    //to be used only on the gl thread
    const std::vector<RenderData*>& getRenderDataVector() const { return render_data_vector; }

    void setUseStencilBuffer(bool enable) { useStencilBuffer_ = enable; }
};
extern Renderer* gRenderer;
}
#endif
