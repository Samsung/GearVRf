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
#include "gl/gl_program.h"
#include "glm/gtc/matrix_inverse.hpp"

#include "eglextension/tiledrendering/tiled_rendering_enhancer.h"
#include "objects/material.h"
#include "objects/shader_data.h"
#include "objects/scene.h"
#include "objects/scene_object.h"
#include "objects/components/camera.h"
#include "objects/components/render_data.h"
#include "objects/textures/render_texture.h"
#include "shaders/shader_manager.h"
#include "shaders/post_effect_shader_manager.h"
#include "util/gvr_gl.h"
#include "util/gvr_log.h"
#include "batch_manager.h"

#include <unordered_map>
#include <unordered_set>

#include "gl_renderer.h"
#include "vulkan_renderer.h"
#define MAX_INDICES 500
#define BATCH_SIZE 60
bool do_batching = false;
namespace gvr {
Renderer* gRenderer = nullptr;
bool use_multiview= false;
Renderer* Renderer::instance = nullptr;
bool Renderer::isVulkan_ = false;
void Renderer::initializeStats() {
    // TODO: this function will be filled in once we add draw time stats
}
/***
    Till we have Vulkan implementation, lets create GLRenderer by-default
***/
Renderer* Renderer::getInstance(std::string type){
    if(nullptr == instance){
     if(0){
            instance = new VulkanRenderer();
            isVulkan_ = true;
        }
        else {
            instance = new GLRenderer();
        }
        std::atexit(resetInstance);      // Destruction of instance registered at runtime exit
    }
    return instance;
}

Renderer::Renderer() : numberDrawCalls(0),
                       numberTriangles(0),
                       numLights(0),
                       batch_manager(nullptr) {
    if(do_batching && !gRenderer->isVulkanInstace()) {
        batch_manager = new BatchManager(BATCH_SIZE, MAX_INDICES);
    }
}
void Renderer::frustum_cull(glm::vec3 camera_position, SceneObject *object,
        float frustum[6][4], std::vector<SceneObject*>& scene_objects,
        bool need_cull, int planeMask) {

    // frustumCull() return 3 possible values:
    // 0 when the HBV of the object is completely outside the frustum: cull itself and all its children out
    // 1 when the HBV of the object is intersecting the frustum but the object itself is not: cull it out and continue culling test with its children
    // 2 when the HBV of the object is intersecting the frustum and the mesh BV of the object are intersecting (inside) the frustum: render itself and continue culling test with its children
    // 3 when the HBV of the object is completely inside the frustum: render itself and all its children without further culling test
    int cullVal;

    if (!object->enabled()) {
        return;
    }

    //allows for on demand calculation of the camera distance; usually matters
    //when transparent objects are in play
    RenderData* renderData = object->render_data();
    if (nullptr != renderData) {
        renderData->setCameraDistanceLambda([object, camera_position]() {
            // Transform the bounding volume
            BoundingVolume bounding_volume_ = object->getBoundingVolume();
            glm::vec4 transformed_sphere_center(bounding_volume_.center(), 1.0f);

            // Calculate distance from camera
            glm::vec4 position(camera_position, 1.0f);
            glm::vec4 difference = transformed_sphere_center - position;
            float distance = glm::dot(difference, difference);

            // this distance will be used when sorting transparent objects
            return distance;
        });
    }

    if (need_cull) {
        cullVal = object->frustumCull(camera_position, frustum, planeMask);
        if (cullVal == 0) {
            object->setCullStatus(true);
            return;
        }

        if (cullVal >= 2) {
            object->setCullStatus(false);
            scene_objects.push_back(object);
        }

        if (cullVal == 3) {
            object->setCullStatus(false);
            need_cull = false;
        }
    } else {
        object->setCullStatus(false);
        scene_objects.push_back(object);
    }

    const std::vector<SceneObject*> children = object->children();
    for (auto it = children.begin(); it != children.end(); ++it) {
        frustum_cull(camera_position, *it, frustum, scene_objects, need_cull, planeMask);
    }
}

void Renderer::state_sort() {
    // The current implementation of sorting is based on
    // 1. rendering order first to maintain specified order
    // 2. shader type second to minimize the gl cost of switching shader
    // 3. camera distance last to minimize overdraw
    std::sort(render_data_vector.begin(), render_data_vector.end(),
            compareRenderDataByOrderShaderDistance);

    if (DEBUG_RENDERER) {
        LOGD("SORTING: After sorting");

        for (int i = 0; i < render_data_vector.size(); ++i) {
            RenderData* renderData = render_data_vector[i];

            if (DEBUG_RENDERER) {
                LOGD(
                        "SORTING: pass_count = %d, rendering order = %d, shader_type = %d, camera_distance = %f\n",
                        renderData->pass_count(), renderData->rendering_order(),
                        renderData->get_shader(0),
                        renderData->camera_distance());
            }
        }
    }
}
/**
    This function compares passes of render-data
    it checks whether no of passes are equal and then material and cull_status of each pass

*/
bool isRenderPassEqual(RenderData* rdata1, RenderData* rdata2){
    int pass_count1 = rdata1->pass_count();
    int pass_count2 = rdata2->pass_count();

    if(pass_count1 != pass_count2)
        return false;

    for(int i=0; i< pass_count1; i++){
        if(!(rdata1->material(i) == rdata2->material(i) && rdata1->get_shader(0) == rdata2->get_shader(0) &&
                   rdata1->cull_face(i) == rdata2->cull_face(i)))
            return false;
    }
    return true;
}

void Renderer::cull(Scene *scene, Camera *camera,
        ShaderManager* shader_manager) {

    if (camera->owner_object() == 0
            || camera->owner_object()->transform() == nullptr) {
        return;
    }
    std::vector<SceneObject*> scene_objects;
    scene_objects.reserve(1024);

    cullFromCamera(scene, camera, shader_manager, scene_objects);

    // Note: this needs to be scaled to sort on N states
    state_sort();

    if(do_batching && !gRenderer->isVulkanInstace()){
        batch_manager->batchSetup(render_data_vector);
    }
}

/*
 * Perform view frustum culling from a specific camera viewpoint
 */
void Renderer::cullFromCamera(Scene *scene, Camera* camera,
        ShaderManager* shader_manager,
        std::vector<SceneObject*>& scene_objects) {
    if (scene->getLightList().size() != numLights)
    {
        numLights = scene->getLightList().size();
        scene->bindShaders();
    }
    render_data_vector.clear();
    scene_objects.clear();
    RenderState rstate;

    rstate.material_override = NULL;
    rstate.shader_manager = shader_manager;
    rstate.uniforms.u_view = camera->getViewMatrix();
    rstate.uniforms.u_proj = camera->getProjectionMatrix();
    rstate.shader_manager = shader_manager;
    rstate.scene = scene;
    rstate.render_mask = camera->render_mask();
    rstate.uniforms.u_right = rstate.render_mask & RenderData::RenderMaskBit::Right;

    glm::mat4 vp_matrix = glm::mat4(rstate.uniforms.u_proj * rstate.uniforms.u_view);

    // Travese all scene objects in the scene as a tree and do frustum culling at the same time if enabled
    // 1. Build the view frustum
    float frustum[6][4];
    build_frustum(frustum, (const float*) glm::value_ptr(vp_matrix));

    // 2. Iteratively execute frustum culling for each root object (as well as its children objects recursively)
    SceneObject *object = scene->getRoot();
    if (DEBUG_RENDERER) {
        LOGD("FRUSTUM: start frustum culling for root %s\n", object->name().c_str());
    }
    frustum_cull(camera->owner_object()->transform()->position(), object, frustum, scene_objects, scene->get_frustum_culling(), 0);
    if (DEBUG_RENDERER) {
        LOGD("FRUSTUM: end frustum culling for root %s\n", object->name().c_str());
    }
    // 3. do occlusion culling, if enabled
    occlusion_cull(rstate, scene_objects);
}


void Renderer::renderRenderDataVector(RenderState &rstate) {

    if (!do_batching || gRenderer->isVulkanInstace() ) {
        for (auto it = render_data_vector.begin();
                it != render_data_vector.end(); ++it) {
            GL(renderRenderData(rstate, *it));
        }
    } else {
         batch_manager->renderBatches(rstate);
    }
}

void Renderer::addRenderData(RenderData *render_data, Scene* scene) {
    if (render_data == 0 || render_data->material(0) == 0 || !render_data->enabled()) {
        return;
    }
    if (render_data->mesh() == NULL) {
        return;
    }
    const RenderPass* pass = render_data->pass(0);
    int shaderID = pass->get_shader();
    if (shaderID < 0)
    {
        //LOGE("SHADER: RenderData %p[%p] shader being generated", render_data, pass);
        return;
    }
    if (shaderID == 0)
    {
        LOGE("SHADER: RenderData %p[%p] has no shader", render_data, pass);
        render_data->set_shader(0, -1);
        render_data->bindShader(scene);
        return;
    }
    if (render_data->render_mask() == 0) {
        return;
    }
    render_data_vector.push_back(render_data);
    return;
}

bool Renderer::occlusion_cull_init(Scene* scene, std::vector<SceneObject*>& scene_objects){

    scene->lockColliders();
    scene->clearVisibleColliders();
    LOGE("in occulusion cull init");
    bool do_culling = scene->get_occlusion_culling();
    if (!do_culling) {
        for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
            SceneObject *scene_object = (*it);
            RenderData* render_data = scene_object->render_data();
            addRenderData(render_data, scene);
            scene->pick(scene_object);
        }
        scene->unlockColliders();
        return false;
    }
    LOGE("in occlusion cull %d", render_data_vector.size());
    return true;
}


void Renderer::build_frustum(float frustum[6][4], const float *vp_matrix) {
    float t;

    /* Extract the numbers for the RIGHT plane */
    frustum[0][0] = vp_matrix[3] - vp_matrix[0];
    frustum[0][1] = vp_matrix[7] - vp_matrix[4];
    frustum[0][2] = vp_matrix[11] - vp_matrix[8];
    frustum[0][3] = vp_matrix[15] - vp_matrix[12];

    /* Normalize the result */
    t = sqrt(
            frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1]
                    + frustum[0][2] * frustum[0][2]);
    frustum[0][0] /= t;
    frustum[0][1] /= t;
    frustum[0][2] /= t;
    frustum[0][3] /= t;

    /* Extract the numbers for the LEFT plane */
    frustum[1][0] = vp_matrix[3] + vp_matrix[0];
    frustum[1][1] = vp_matrix[7] + vp_matrix[4];
    frustum[1][2] = vp_matrix[11] + vp_matrix[8];
    frustum[1][3] = vp_matrix[15] + vp_matrix[12];

    /* Normalize the result */
    t = sqrt(
            frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1]
                    + frustum[1][2] * frustum[1][2]);
    frustum[1][0] /= t;
    frustum[1][1] /= t;
    frustum[1][2] /= t;
    frustum[1][3] /= t;

    /* Extract the BOTTOM plane */
    frustum[2][0] = vp_matrix[3] + vp_matrix[1];
    frustum[2][1] = vp_matrix[7] + vp_matrix[5];
    frustum[2][2] = vp_matrix[11] + vp_matrix[9];
    frustum[2][3] = vp_matrix[15] + vp_matrix[13];

    /* Normalize the result */
    t = sqrt(
            frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1]
                    + frustum[2][2] * frustum[2][2]);
    frustum[2][0] /= t;
    frustum[2][1] /= t;
    frustum[2][2] /= t;
    frustum[2][3] /= t;

    /* Extract the TOP plane */
    frustum[3][0] = vp_matrix[3] - vp_matrix[1];
    frustum[3][1] = vp_matrix[7] - vp_matrix[5];
    frustum[3][2] = vp_matrix[11] - vp_matrix[9];
    frustum[3][3] = vp_matrix[15] - vp_matrix[13];

    /* Normalize the result */
    t = sqrt(
            frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1]
                    + frustum[3][2] * frustum[3][2]);
    frustum[3][0] /= t;
    frustum[3][1] /= t;
    frustum[3][2] /= t;
    frustum[3][3] /= t;

    /* Extract the FAR plane */
    frustum[4][0] = vp_matrix[3] - vp_matrix[2];
    frustum[4][1] = vp_matrix[7] - vp_matrix[6];
    frustum[4][2] = vp_matrix[11] - vp_matrix[10];
    frustum[4][3] = vp_matrix[15] - vp_matrix[14];

    /* Normalize the result */
    t = sqrt(
            frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1]
                    + frustum[4][2] * frustum[4][2]);
    frustum[4][0] /= t;
    frustum[4][1] /= t;
    frustum[4][2] /= t;
    frustum[4][3] /= t;

    /* Extract the NEAR plane */
    frustum[5][0] = vp_matrix[3] + vp_matrix[2];
    frustum[5][1] = vp_matrix[7] + vp_matrix[6];
    frustum[5][2] = vp_matrix[11] + vp_matrix[10];
    frustum[5][3] = vp_matrix[15] + vp_matrix[14];

    /* Normalize the result */
    t = sqrt(
            frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1]
                    + frustum[5][2] * frustum[5][2]);
    frustum[5][0] /= t;
    frustum[5][1] /= t;
    frustum[5][2] /= t;
    frustum[5][3] /= t;
}

void Renderer::renderRenderData(RenderState& rstate, RenderData* render_data) {
    if (!(rstate.render_mask & render_data->render_mask()))
        return;

    // Set the states
    setRenderStates(render_data, rstate);
    if (render_data->mesh() != 0) {
        GL(renderMesh(rstate, render_data));
    }
    // Restoring to Default.
    // TODO: There's a lot of redundant state changes. If on every render face culling is being set there's no need to
    // restore defaults. Possibly later we could add a OpenGL state wrapper to avoid redundant api calls.
    restoreRenderStates(render_data);
}

void Renderer::renderPostEffectData(RenderState& rstate,
        RenderTexture* render_texture, ShaderData* post_effect_data) {
    try {
        PostEffectShaderManager* shader_manager = reinterpret_cast<PostEffectShaderManager*>(rstate.shader_manager);
        Shader* shader = shader_manager->getShader(post_effect_data->get_shader());
        if (shader != NULL)
        {
            post_effect_data->setTexture("u_texture", render_texture);
            RenderData* rdata = shader_manager->get_render_data();
            rdata->set_shader(0, post_effect_data->get_shader());
            shader->render(&rstate, rdata, post_effect_data);
        }
        else {
            LOGE("SHADER: post effect shader %d not found", post_effect_data->get_shader());
        }
    } catch (const std::string& error) {
        LOGE("Error detected in Renderer::renderPostEffectData; error : %s", error.c_str());
    }
}

}
