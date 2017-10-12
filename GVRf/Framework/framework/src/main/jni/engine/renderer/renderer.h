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
#include <unordered_map>

#include "glm/glm.hpp"
#include "batch.h"
#include "objects/eye_type.h"
#include "objects/mesh.h"
#include "objects/bounding_volume.h"
#include "shaders/shader_manager.h"
#include "batch_manager.h"

typedef unsigned long Long;

namespace gvr {
extern bool use_multiview;
struct RenderTextureInfo;
class Camera;
class Scene;
class SceneObject;
class ShaderData;
class RenderData;
class RenderTarget;
class RenderTexture;
class Light;
class BitmapImage;
class CubemapImage;
class CompressedImage;
class FloatImage;
class VertexBuffer;
class IndexBuffer;
class UniformBlock;
class Image;
class RenderPass;
class Texture;
extern uint8_t *oculusTexData;
/*
 * These uniforms are commonly used in shaders.
 * They are calculated by the GearVRF renderer.
 */
struct ShaderUniformsPerObject {
    glm::mat4   u_model;        // Model matrix
    glm::mat4   u_proj;         // projection matrix
    glm::mat4   u_view;         // View matrix
    glm::mat4   u_view_[2];     // for multiview
    glm::mat4   u_view_inv;     // inverse of View matrix
    glm::mat4   u_view_inv_[2]; // inverse of View matrix
    glm::mat4   u_mv;           // ModelView matrix
    glm::mat4   u_mv_[2];       // ModelView matrix
    glm::mat4   u_mvp;          // ModelViewProjection matrix
    glm::mat4   u_mvp_[2];      // ModelViewProjection matrix
    glm::mat4   u_mv_it;        // inverse transpose of ModelView
    glm::mat4   u_mv_it_[2];    // inverse transpose of ModelView
    int         u_right;        // 1 = right eye, 0 = left
};

struct RenderState {
    int                     render_mask;
    int                     viewportX;
    int                     viewportY;
    int                     viewportWidth;
    int                     viewportHeight;
    bool                    invalidateShaders;
    Scene*                  scene;
    ShaderData*             material_override;
    ShaderUniformsPerObject uniforms;
    ShaderManager*          shader_manager;
    bool                    shadow_map;
    bool                    is_multiview;
    Camera*                 camera;
};

class Renderer {
public:
    void resetStats() {
        numberDrawCalls = 0;
        numberTriangles = 0;
    }
    bool isVulkanInstance(){
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
     static Renderer* getInstance(std::string type =  " ");
     static void resetInstance(){
        delete instance;
         instance = NULL;
     }
     virtual ShaderData* createMaterial(const char* uniform_desc, const char* texture_desc) = 0;
     virtual RenderData* createRenderData() = 0;
     virtual UniformBlock* createUniformBlock(const char* desc, int, const char* name, int) = 0;
     virtual Image* createImage(int type, int format) = 0;
        virtual RenderPass* createRenderPass() = 0;
     virtual Texture* createTexture(int target = GL_TEXTURE_2D) = 0;
     virtual RenderTexture* createRenderTexture(int width, int height, int sample_count,
                                                int jcolor_format, int jdepth_format, bool resolve_depth,
                                                const TextureParameters* texture_parameters, int number_views) = 0;
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count, int layers) = 0;
    virtual RenderTexture* createRenderTexture(const RenderTextureInfo&)=0;
    virtual Shader* createShader(int id, const char* signature,
                                 const char* uniformDescriptor, const char* textureDescriptor,
                                 const char* vertexDescriptor, const char* vertexShader,
                                 const char* fragmentShader) = 0;
     virtual VertexBuffer* createVertexBuffer(const char* descriptor, int vcount) = 0;
     virtual IndexBuffer* createIndexBuffer(int bytesPerIndex, int icount) = 0;
     void updateTransforms(RenderState& rstate, UniformBlock* block, RenderData*);
     virtual void initializeStats();
     virtual void cullFromCamera(Scene *scene, Camera* camera,
                ShaderManager* shader_manager, std::vector<RenderData*>* render_data_vector,bool);
     virtual void set_face_culling(int cull_face) = 0;

     virtual void renderRenderData(RenderState& rstate, RenderData* render_data);
    virtual RenderTarget* createRenderTarget(Scene*) = 0;
    virtual RenderTarget* createRenderTarget(RenderTexture*, bool) = 0;
    virtual RenderTarget* createRenderTarget(RenderTexture*, const RenderTarget*) = 0;

    virtual void renderRenderTarget(Scene*, RenderTarget* renderTarget, ShaderManager* shader_manager,
                                    RenderTexture* post_effect_render_texture_a, RenderTexture* post_effect_render_texture_b)=0;
    virtual void restoreRenderStates(RenderData* render_data) = 0;
    virtual void setRenderStates(RenderData* render_data, RenderState& rstate) = 0;
    virtual Texture* createSharedTexture(int id) = 0;
    virtual bool renderWithShader(RenderState& rstate, Shader* shader, RenderData* renderData, ShaderData* shaderData, int) = 0;

    virtual void makeShadowMaps(Scene* scene, ShaderManager* shader_manager) = 0;
    virtual void occlusion_cull(RenderState& rstate, std::vector<SceneObject*>& scene_objects, std::vector<RenderData*>* render_data_vector) = 0;
    virtual void updatePostEffectMesh(Mesh*) = 0;
    void addRenderData(RenderData *render_data, RenderState& rstate, std::vector<RenderData*>& renderList);
private:
    static bool isVulkan_;
    virtual void build_frustum(float frustum[6][4], const float *vp_matrix);
    virtual void frustum_cull(glm::vec3 camera_position, SceneObject *object,
            float frustum[6][4], std::vector<SceneObject*>& scene_objects,
            bool continue_cull, int planeMask);

    Renderer(const Renderer& render_engine);
    Renderer(Renderer&& render_engine);
    Renderer& operator=(const Renderer& render_engine);
    Renderer& operator=(Renderer&& render_engine);
    BatchManager* batch_manager;
    static Renderer* instance;

protected:
    Renderer();
    virtual ~Renderer(){
        if(batch_manager)
            delete batch_manager;
        batch_manager = NULL;
    }

    virtual void renderMesh(RenderState& rstate, RenderData* render_data) = 0;
    virtual void renderMaterialShader(RenderState& rstate, RenderData* render_data, ShaderData *material, Shader* shader) = 0;

    virtual bool occlusion_cull_init(RenderState& , std::vector<SceneObject*>& scene_objects,  std::vector<RenderData*>* render_data_vector);

    virtual void renderPostEffectData(RenderState& rstate, RenderTexture* input_texture, RenderData* post_effect, int pass);

    int numberDrawCalls;
    int numberTriangles;
    bool useStencilBuffer_ = false;
public:
    virtual void state_sort(std::vector<RenderData*>* render_data_vector) ;
    int numLights;
    void setUseStencilBuffer(bool enable) { useStencilBuffer_ = enable; }
    bool useStencilBuffer(){
        return  useStencilBuffer_;
    }
};
extern Renderer* gRenderer;
}
#endif
