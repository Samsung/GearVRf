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

#include <contrib/glm/gtc/type_ptr.hpp>
#include "renderer.h"
#include "objects/scene.h"

#define MAX_INDICES 500
#define BATCH_SIZE 60
bool do_batching = false;

namespace gvr {

Renderer* gRenderer = nullptr;
bool use_multiview= false;

void Renderer::initializeStats() {
    // TODO: this function will be filled in once we add draw time stats
}

Renderer::Renderer() : numberDrawCalls(0),
                       numberTriangles(0),
                       numLights(0),
                       batch_manager(nullptr), mLeftRenderTarget{nullptr, nullptr, nullptr},
                       mRightRenderTarget{nullptr, nullptr, nullptr},
                       mMultiviewRenderTarget{nullptr, nullptr, nullptr}{
    if(do_batching && !gRenderer->isVulkanInstance()) {
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

void Renderer::state_sort(std::vector<RenderData*>* render_data_vector) {
    // The current implementation of sorting is based on
    // 1. rendering order first to maintain specified order
    // 2. shader type second to minimize the gl cost of switching shader
    // 3. camera distance last to minimize overdraw
    std::sort(render_data_vector->begin(), render_data_vector->end(),
            compareRenderDataByOrderShaderDistance);

    if (DEBUG_RENDERER) {
        LOGD("SORTING: After sorting");

        for (int i = 0; i < render_data_vector->size(); ++i) {
            RenderData* renderData = (*render_data_vector)[i];

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

/*
 * Perform view frustum culling from a specific camera viewpoint
 */
void Renderer::cullFromCamera(Scene *scene, jobject javaSceneObject, Camera* camera,
        ShaderManager* shader_manager, std::vector<RenderData*>* render_data_vector, bool is_multiview)
{
    std::vector<SceneObject*> scene_objects;

    render_data_vector->clear();
    scene_objects.clear();
    RenderState rstate;

    int lightCount = scene->getLightList().size();
    if (lightCount != numLights)
    {
        scene->bindShaders(javaSceneObject);
        numLights = lightCount;
    }

    rstate.is_multiview = is_multiview;
    rstate.material_override = NULL;
    rstate.shader_manager = shader_manager;
    rstate.uniforms.u_view = camera->getViewMatrix();
    rstate.uniforms.u_proj = camera->getProjectionMatrix();
    rstate.shader_manager = shader_manager;
    rstate.scene = scene;
    rstate.render_mask = camera->render_mask();
    rstate.uniforms.u_right = (rstate.render_mask & RenderData::RenderMaskBit::Right) ? 1 : 0;
    rstate.javaSceneObject = javaSceneObject;

    glm::mat4 vp_matrix = glm::mat4(rstate.uniforms.u_proj * rstate.uniforms.u_view);
    glm::vec3 campos(rstate.uniforms.u_view[3]);

    // Travese all scene objects in the scene as a tree and do frustum culling at the same time if enabled
    // 1. Build the view frustum
    float frustum[6][4];
    build_frustum(frustum, (const float*) glm::value_ptr(vp_matrix));

    // 2. Iteratively execute frustum culling for each root object (as well as its children objects recursively)
    SceneObject *object = scene->getRoot();
    if (DEBUG_RENDERER) {
        LOGD("FRUSTUM: start frustum culling for root %s\n", object->name().c_str());
    }
    //    frustum_cull(camera->owner_object()->transform()->position(), object, frustum, scene_objects, scene->get_frustum_culling(), 0);
    frustum_cull(campos, object, frustum, scene_objects, scene->get_frustum_culling(), 0);
    if (DEBUG_RENDERER) {
        LOGD("FRUSTUM: end frustum culling for root %s\n", object->name().c_str());
    }
    // 3. do occlusion culling, if enabled
    occlusion_cull(rstate, scene_objects, render_data_vector);
}


void Renderer::addRenderData(RenderData *render_data, RenderState& rstate, std::vector<RenderData*>& renderList)
{
    if (render_data && (render_data->isValid(this, rstate) >= 0))
    {
        renderList.push_back(render_data);
    }
}

bool Renderer::occlusion_cull_init(RenderState& renderState, std::vector<SceneObject*>& scene_objects,  std::vector<RenderData*>* render_data_vector){

    renderState.scene->lockColliders();
    renderState.scene->clearVisibleColliders();
    bool do_culling = renderState.scene->get_occlusion_culling();
    if (!do_culling) {
        for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
            SceneObject *scene_object = (*it);
            RenderData* render_data = scene_object->render_data();
            addRenderData(render_data, renderState, *render_data_vector);
            renderState.scene->pick(scene_object);
        }
        renderState.scene->unlockColliders();
        return false;
    }
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

void Renderer::updateTransforms(RenderState& rstate, UniformBlock* transform_ubo, RenderData* renderData)
{
    Transform* model = renderData->owner_object() ? renderData->owner_object()->transform() : nullptr;
    rstate.uniforms.u_model = model ? model->getModelMatrix() : glm::mat4();
//    rstate.uniforms.u_right = rstate.render_mask & RenderData::RenderMaskBit::Right;
    transform_ubo->setMat4("u_model", rstate.uniforms.u_model);
    transform_ubo->setFloat("u_right", (rstate.render_mask & RenderData::RenderMaskBit::Right) ? 1 : 0);

    if (rstate.is_multiview)
    {
        if (!rstate.shadow_map)
        {
            rstate.uniforms.u_view_[0] = rstate.scene->main_camera_rig()->left_camera()->getViewMatrix();
            rstate.uniforms.u_view_[1] = rstate.scene->main_camera_rig()->right_camera()->getViewMatrix();
            rstate.uniforms.u_mv_[0] = rstate.uniforms.u_view_[0] * rstate.uniforms.u_model;
            rstate.uniforms.u_mv_[1] = rstate.uniforms.u_view_[1] * rstate.uniforms.u_model;
            rstate.uniforms.u_mv_it_[0] = glm::inverseTranspose(rstate.uniforms.u_mv_[0]);
            rstate.uniforms.u_mv_it_[1] = glm::inverseTranspose(rstate.uniforms.u_mv_[1]);
            rstate.uniforms.u_mvp_[0] = rstate.uniforms.u_proj * rstate.uniforms.u_mv_[0];
            rstate.uniforms.u_mvp_[1] = rstate.uniforms.u_proj * rstate.uniforms.u_mv_[1];
            rstate.uniforms.u_view_inv_[0] = glm::inverse(rstate.uniforms.u_view_[0]);
            rstate.uniforms.u_view_inv_[1] = glm::inverse(rstate.uniforms.u_view_[1]);
        }
        transform_ubo->setMat4("u_view_", rstate.uniforms.u_view_[0]);
        transform_ubo->setMat4("u_mvp_", rstate.uniforms.u_mvp_[0]);
        transform_ubo->setMat4("u_mv_", rstate.uniforms.u_mv_[0]);
        transform_ubo->setMat4("u_mv_it_", rstate.uniforms.u_mv_it_[0]);
        transform_ubo->setMat4("u_view_i_", rstate.uniforms.u_view_inv_[0]);
        transform_ubo->setInt("u_render_mask",renderData->render_mask());
    }
    else
    {
        rstate.uniforms.u_mv = rstate.uniforms.u_view * rstate.uniforms.u_model;
        rstate.uniforms.u_mv_it = glm::inverseTranspose(rstate.uniforms.u_mv);
        rstate.uniforms.u_mvp = rstate.uniforms.u_proj * rstate.uniforms.u_mv;
        transform_ubo->setMat4("u_view", rstate.uniforms.u_view);
        transform_ubo->setMat4("u_mvp", rstate.uniforms.u_mvp);
        transform_ubo->setMat4("u_mv", rstate.uniforms.u_mv);
        transform_ubo->setMat4("u_mv_it", rstate.uniforms.u_mv_it);
        transform_ubo->setMat4("u_view_i", rstate.uniforms.u_view_inv);
    }
    transform_ubo->updateGPU(this);
}

bool Renderer::renderPostEffectData(RenderState& rstate, RenderTexture* input_texture, RenderData* post_effect, int pass)
{
    RenderPass* rpass = post_effect->pass(pass);
    if(rpass == NULL)
        return false;

    ShaderData* material = rpass->material();

    if(material == NULL)
        return false;

    material->setTexture("u_texture", input_texture);
    int nativeShader = rpass->get_shader(rstate.is_multiview);
    Shader* shader;

    if(post_effect->isValid(this, rstate) < 0)
    {
        LOGE("Renderer::renderPostEffectData is dirty");
        return false;             // no shader available
    }
    shader = rstate.shader_manager->getShader(nativeShader);
    if (shader == NULL)
    {
        //@todo implementation details leaked; unify common JNI reqs of Scene and RenderData
        //@todo duped in render_data.cpp
        JNIEnv* env = nullptr;
        int rc = rstate.scene->get_java_env(&env);
        post_effect->bindShader(env, rstate.javaSceneObject, rstate.is_multiview);
        if (rc > 0)
        {
            rstate.scene->detach_java_env();
        }
        nativeShader = rpass->get_shader(rstate.is_multiview);
        shader = rstate.shader_manager->getShader(nativeShader);
    }
    renderWithShader(rstate, shader, post_effect, material, pass);
    post_effect->clearDirty();
    return true;
}

}
