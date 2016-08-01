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
#include "objects/post_effect_data.h"
#include "objects/scene.h"
#include "objects/scene_object.h"
#include "objects/components/camera.h"
#include "objects/components/render_data.h"
#include "objects/textures/render_texture.h"
#include "shaders/shader_manager.h"
#include "shaders/post_effect_shader_manager.h"
#include "util/gvr_gl.h"
#include "util/gvr_log.h"

#include <unordered_map>
#include <unordered_set>

#define BATCH_SIZE 60


namespace gvr {
bool use_multiview= false;
static int numberDrawCalls;
static int numberTriangles;

void Renderer::initializeStats() {
    // TODO: this function will be filled in once we add draw time stats
}

void Renderer::resetStats() {
    numberDrawCalls = 0;
    numberTriangles = 0;
}

int Renderer::getNumberDrawCalls() {
    return numberDrawCalls;
}

int Renderer::getNumberTriangles() {
    return numberTriangles;
}

static std::vector<RenderData*> render_data_vector;

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
                        renderData->material(0)->shader_type(),
                        renderData->camera_distance());
            }
        }
    }
}
struct comparator {
    std::string renderdata_properties;
    Material* mat;
    Material::ShaderType shader_type;
    bool mesh_dynamic;
};

/*
 * batch_set stores all the batches, batch_map stores the indices of batches and the respective map
 * to get constant look up time
 */
std::vector<Batch*> batch_set;
std::unordered_map<Batch*, int> batch_map;
#define MAX_INDICES 400

void getNewBatch(RenderData* rdata, Batch** existing_batch){
    Batch* new_batch = new Batch(MAX_INDICES, MAX_INDICES);
    new_batch->add(rdata);
    rdata->setBatch(new_batch);
    batch_set.push_back(new_batch);
    batch_map[new_batch] = batch_set.size() - 1;
    *existing_batch = new_batch;
}

/*
 * batch indices stores the indices in render_vector where batches are split
 */
std::vector<int> batch_indices;
void createBatch(int start, int end) {
    Batch* existing_batch = nullptr;
    int size = BATCH_SIZE;
    // get batch with least no of meshes in it
    for (int i = start; i <= end; ++i) {
        if (render_data_vector[i]->getBatch() != nullptr) {
            if (render_data_vector[i]->getBatch()->getNumberOfMeshes()
                    <= size) {
                size = render_data_vector[i]->getBatch()->getNumberOfMeshes();
                existing_batch = render_data_vector[i]->getBatch();
            }
        }
    }

    for (int i = start; i <= end; ++i) {
        RenderData* render_data = render_data_vector[i];
        Batch* current_batch = render_data->getBatch();
        if (!current_batch) {
            if (!render_data->mesh()->isDynamic()) { // mesh is static
                // existing batch is not full
                if (existing_batch
                        && existing_batch->getNumberOfMeshes() < BATCH_SIZE) {
                    // add failed because mesh is large try with next batch
                    if (!existing_batch->add(render_data)) {
                        getNewBatch(render_data, &existing_batch);
                    }

                    // if batch does not exist in set, add it
                    if (batch_map.find(existing_batch) == batch_map.end()) {
                        batch_set.push_back(existing_batch);
                        batch_map[existing_batch] = batch_set.size() - 1;

                    }
                    render_data->setBatch(existing_batch);
                } else { // existing batch is full or does not exists
                    getNewBatch(render_data, &existing_batch);
                }
            } else { // mesh is dynamic

                // if one of the mesh is modified
                if (existing_batch && existing_batch->isBatchDirty()) {
                    existing_batch->setMeshesDirty();

                    std::unordered_map<Batch*, int>::iterator it =
                            batch_map.find(existing_batch);
                    if (it != batch_map.end()) {
                        int index = it->second;
                        batch_set.erase(batch_set.begin() + index);
                    }
                    delete existing_batch;
                    getNewBatch(render_data, &existing_batch);
                }
                // existing batch is not full
                else if (existing_batch
                        && existing_batch->getNumberOfMeshes() < BATCH_SIZE) {

                    if (!existing_batch->add(render_data)) {
                        getNewBatch(render_data, &existing_batch);
                    }

                    render_data->setBatch(existing_batch);

                    if (batch_map.find(existing_batch) == batch_map.end()) {
                        batch_set.push_back(existing_batch);
                        batch_map[existing_batch] = batch_set.size() - 1;
                    }
                } else { //existing batch is full or not exists
                    getNewBatch(render_data, &existing_batch);
                }
            }
        } else { // batch is not null

            // update the transform if model matrix is changed
             if (render_data->owner_object()->isTransformDirty()
                   && render_data->owner_object()->transform()) {
                current_batch->UpdateModelMatrix(render_data,
                        render_data->owner_object()->transform()->getModelMatrix());
            }

            if (!render_data->mesh()->isDynamic()) {
                if (batch_map.find(current_batch) == batch_map.end()) {
                    batch_set.push_back(current_batch);
                    batch_map[current_batch] = batch_set.size() - 1;
                }
            } else { // mesh is dynamic
                if (current_batch->isBatchDirty()) {
                    current_batch->setMeshesDirty();

                    std::unordered_map<Batch*, int>::iterator it =
                            batch_map.find(current_batch);
                    if (it != batch_map.end()) {
                        int index = it->second;
                        batch_set.erase(batch_set.begin() + index);
                    }

                    delete current_batch;
                    Batch* new_batch = new Batch(MAX_INDICES, MAX_INDICES);
                    render_data->setBatch(new_batch);
                    new_batch->add(render_data);
                    batch_set.push_back(new_batch);
                    batch_map[new_batch] = batch_set.size() - 1;

                } else {
                    if (batch_map.find(current_batch) == batch_map.end()) {
                        batch_set.push_back(current_batch);
                        batch_map[current_batch] = batch_set.size() - 1;
                    }
                }
            }
        }
    }
}

/*
 * It creates array of indices which specifies indices of the spliting of batches in renderdata vector
 * for renderdatas to have in same batch, they need to have same render order, material,
 * shader type and mesh dynamic-ness
 */
void Renderer::BatchSetup() {
    batch_indices.clear();
    comparator prev, current;

    // copy first render vector properties
    if (render_data_vector.size() != 0) {
        prev.renderdata_properties = render_data_vector[0]->getHashCode();
        prev.mat = render_data_vector[0]->material(0);
        prev.shader_type = render_data_vector[0]->material(0)->shader_type();
        prev.mesh_dynamic = render_data_vector[0]->mesh()->isDynamic();
        batch_indices.push_back(0);
    }

    // if previous render data does not have same properies which are required for batching, split them
    // into different batches
    for (int i = 1; i < render_data_vector.size(); i++) {
        current.renderdata_properties = render_data_vector[i]->getHashCode();
        current.mat = render_data_vector[i]->material(0);
        current.shader_type = render_data_vector[i]->material(0)->shader_type();
        current.mesh_dynamic = render_data_vector[i]->mesh()->isDynamic();
        if (!current.renderdata_properties.compare(prev.renderdata_properties)
                && current.mesh_dynamic == prev.mesh_dynamic
                && current.mat == prev.mat
                && current.shader_type == prev.shader_type) {
            continue;
        } else {
            batch_indices.push_back(i);
            prev.mat = current.mat;
            prev.renderdata_properties = current.renderdata_properties;
            prev.shader_type = current.shader_type;
            prev.mesh_dynamic = current.mesh_dynamic;
        }
    }
    batch_indices.push_back(render_data_vector.size());
    batch_set.clear(); // Clear batch vector
    batch_map.clear();

  //  LOGE("render data vector size %d", render_data_vector.size());
    for (int i = 1; i < batch_indices.size(); i++) {
        createBatch(batch_indices[i - 1], batch_indices[i] - 1);
    }

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

    BatchSetup();
}

/*
 * Perform view frustum culling from a specific camera viewpoint
 */
void Renderer::cullFromCamera(Scene *scene, Camera* camera,
        ShaderManager* shader_manager,
        std::vector<SceneObject*>& scene_objects) {
    render_data_vector.clear();
    scene_objects.clear();

    glm::mat4 view_matrix = camera->getViewMatrix();
    glm::mat4 projection_matrix = camera->getProjectionMatrix();
    glm::mat4 vp_matrix = glm::mat4(projection_matrix * view_matrix);

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
    occlusion_cull(scene, scene_objects, shader_manager, vp_matrix);
}

/**
 * Set the render states for render data
 */
void Renderer::setRenderStates(RenderData* render_data, RenderState& rstate) {

    if (!(rstate.render_mask & render_data->render_mask()))
        return;

    if (render_data->offset()) {
        GL(glEnable (GL_POLYGON_OFFSET_FILL));
        GL(
                glPolygonOffset(render_data->offset_factor(),
                        render_data->offset_units()));
    }
    if (!render_data->depth_test()) {
        GL(glDisable (GL_DEPTH_TEST));
    }
    if (!render_data->alpha_blend()) {
        GL(glDisable (GL_BLEND));
    }
    if (render_data->alpha_to_coverage()) {
        GL(glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE));
        GL(
                glSampleCoverage(render_data->sample_coverage(),
                        render_data->invert_coverage_mask()));
    }
}
/**
 * Restore the render states for render data
 */
void Renderer::restoreRenderStates(RenderData* render_data) {
    if (render_data->cull_face() != RenderData::CullBack) {
        GL(glEnable (GL_CULL_FACE));
        GL(glCullFace (GL_BACK));
    }

    if (render_data->offset()) {
        GL(glDisable (GL_POLYGON_OFFSET_FILL));
    }
    if (!render_data->depth_test()) {
        GL(glEnable (GL_DEPTH_TEST));
    }
    if (!render_data->alpha_blend()) {
        GL(glEnable (GL_BLEND));
    }
    if (render_data->alpha_to_coverage()) {
        GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));
    }
}

void Renderer::renderbatches(RenderState& rstate) {
    glm::mat4 vp_matrix = glm::mat4(
            rstate.uniforms.u_proj * rstate.uniforms.u_view);

    for (auto it = batch_set.begin(); it != batch_set.end(); ++it) {

        Batch* batch = *it;
        rstate.material_override = batch->get_material();
        int currentShaderType = rstate.material_override->shader_type();

        // if shader type is other than texture shader, render it with non-batching mode
        // if the mesh is large, we are not batching it
        if (currentShaderType != Material::ShaderType::TEXTURE_SHADER
                || batch->notBatched()) {
            const std::unordered_set<RenderData*>& render_data_set = batch->getRenderDataSet();
            for (auto it3 = render_data_set.begin();
                    it3 != render_data_set.end(); ++it3) {
                renderRenderData(rstate, (*it3));
            }
            continue;
        }

        RenderData* renderdata = batch->get_renderdata();
        const std::vector<glm::mat4>& matrices = batch->get_matrices();
        numberDrawCalls++;
        batch->setupMesh();
        setRenderStates(renderdata, rstate);

        if(use_multiview){

            rstate.uniforms.u_view_[0] = rstate.scene->main_camera_rig()->left_camera()->getViewMatrix();
            rstate.uniforms.u_view_[1] = rstate.scene->main_camera_rig()->right_camera()->getViewMatrix();
        }

        rstate.shader_manager->getTextureShader()->render_batch(matrices,
                renderdata, rstate, batch->getIndexCount(),
                batch->getNumberOfMeshes());
        restoreRenderStates(renderdata);
    }

}
bool do_batching = true;

void Renderer::renderRenderDataVector(RenderState &rstate) {

    if (!do_batching) {
        for (auto it = render_data_vector.begin();
                it != render_data_vector.end(); ++it) {
            GL(renderRenderData(rstate, *it));
        }
    } else {
        renderbatches(rstate);
    }
}
void Renderer::renderCamera(Scene* scene, Camera* camera, int framebufferId,
        int viewportX, int viewportY, int viewportWidth, int viewportHeight,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    numberDrawCalls = 0;
    numberTriangles = 0;

    RenderState rstate;
    rstate.material_override = NULL;
    rstate.viewportX = viewportX;
    rstate.viewportY = viewportY;
    rstate.viewportWidth = viewportWidth;
    rstate.viewportHeight = viewportHeight;
    rstate.shader_manager = shader_manager;
    rstate.uniforms.u_view = camera->getViewMatrix();
    rstate.uniforms.u_proj = camera->getProjectionMatrix();
    rstate.shader_manager = shader_manager;
    rstate.scene = scene;
    rstate.render_mask = camera->render_mask();
    rstate.uniforms.u_right = rstate.render_mask & RenderData::RenderMaskBit::Right;

    std::vector<PostEffectData*> post_effects = camera->post_effect_data();

    GL(glEnable (GL_DEPTH_TEST));
    GL(glDepthFunc (GL_LEQUAL));
    GL(glEnable (GL_CULL_FACE));
    GL(glFrontFace (GL_CCW));
    GL(glCullFace (GL_BACK));
    GL(glEnable (GL_BLEND));
    GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));
    GL(glBlendEquation (GL_FUNC_ADD));
    GL(glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA));
    GL(glDisable (GL_POLYGON_OFFSET_FILL));
    GL(glLineWidth(1.0f));
    if (post_effects.size() == 0) {
        GL(glBindFramebuffer(GL_FRAMEBUFFER, framebufferId));
        GL(glViewport(viewportX, viewportY, viewportWidth, viewportHeight));

        GL(glClearColor(camera->background_color_r(),
                camera->background_color_g(), camera->background_color_b(),
                camera->background_color_a()));
        GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
        renderRenderDataVector(rstate);
    } else {
        RenderTexture* texture_render_texture = post_effect_render_texture_a;
        RenderTexture* target_render_texture;

        GL(glBindFramebuffer(GL_FRAMEBUFFER,
                texture_render_texture->getFrameBufferId()));
        GL(glViewport(0, 0, texture_render_texture->width(),
                texture_render_texture->height()));

        GL(glClearColor(camera->background_color_r(),
                camera->background_color_g(), camera->background_color_b(), camera->background_color_a()));
        GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));

        for (auto it = render_data_vector.begin();
                it != render_data_vector.end(); ++it) {
            GL(renderRenderData(rstate, *it));
        }

        GL(glDisable(GL_DEPTH_TEST));
        GL(glDisable(GL_CULL_FACE));

        for (int i = 0; i < post_effects.size() - 1; ++i) {
            if (i % 2 == 0) {
                texture_render_texture = post_effect_render_texture_a;
                target_render_texture = post_effect_render_texture_b;
            } else {
                texture_render_texture = post_effect_render_texture_b;
                target_render_texture = post_effect_render_texture_a;
            }
            GL(glBindFramebuffer(GL_FRAMEBUFFER, framebufferId));
            GL(glViewport(viewportX, viewportY, viewportWidth, viewportHeight));

            GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
            GL(renderPostEffectData(camera, texture_render_texture,
                    post_effects[i], post_effect_shader_manager));
        }

        GL(glBindFramebuffer(GL_FRAMEBUFFER, framebufferId));
        GL(glViewport(viewportX, viewportY, viewportWidth, viewportHeight));
        GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
        renderPostEffectData(camera, texture_render_texture, post_effects.back(), post_effect_shader_manager);
    }

    GL(glDisable(GL_DEPTH_TEST));
    GL(glDisable(GL_CULL_FACE));
    GL(glDisable(GL_BLEND));
}

/**
 * Generate shadow maps for all the lights that cast shadows.
 * The scene is rendered from the viewpoint of the light using a
 * special depth shader (GVRDepthShader) to create the shadow map.
 * @see Renderer::renderShadowMap Light::makeShadowMap
 */
void Renderer::makeShadowMaps(Scene* scene, ShaderManager* shader_manager, int width, int height)
{
    const std::vector<Light*> lights = scene->getLightList();
    GL(glEnable (GL_DEPTH_TEST));
    GL(glDepthFunc (GL_LEQUAL));
    GL(glEnable (GL_CULL_FACE));
    GL(glFrontFace (GL_CCW));
    GL(glCullFace (GL_BACK));
    GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));

    int texIndex = 0;
    std::vector<SceneObject*> scene_objects;
    scene_objects.reserve(1024);
    for (auto it = lights.begin(); it != lights.end(); ++it) {
     	if ((*it)->castShadow() &&
     	    (*it)->makeShadowMap(scene, shader_manager, texIndex, scene_objects, width, height))
            ++texIndex;
    }
    GL(glDisable(GL_DEPTH_TEST));
    GL(glDisable(GL_CULL_FACE));

}

/**
 * Generates a shadow map into the specified framebuffer.
 * @param rstate        RenderState with rendering parameters
 * @param camera        camera with light viewpoint
 * @param framebufferId ID of framebuffer to render shadow map into
 * @param scene_objects temporary storage for culling
 * @see Light::makeShadowMap Renderer::makeShadowMaps
 */
void Renderer::renderShadowMap(RenderState& rstate, Camera* camera, GLuint framebufferId, std::vector<SceneObject*>& scene_objects) {

	cullFromCamera(rstate.scene, camera, rstate.shader_manager, scene_objects);

	GL(glBindFramebuffer(GL_FRAMEBUFFER, framebufferId));
    GL(glViewport(rstate.viewportX, rstate.viewportY, rstate.viewportWidth, rstate.viewportHeight));
    glClearColor(0,0,0,1);
    GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));

    for (auto it = render_data_vector.begin();
            it != render_data_vector.end(); ++it) {
        GL(renderRenderData(rstate, *it));
    }
}

void addRenderData(RenderData *render_data) {
    if (render_data == 0 || render_data->material(0) == 0 || !render_data->enabled()) {
        return;
    }

    if (render_data->mesh() == NULL) {
        return;
    }

    if (render_data->render_mask() == 0) {
        return;
    }

    render_data_vector.push_back(render_data);
    return;
}

void Renderer::occlusion_cull(Scene* scene,
        std::vector<SceneObject*>& scene_objects, ShaderManager *shader_manager,
        glm::mat4 vp_matrix) {
    scene->lockColliders();
    scene->clearVisibleColliders();
    bool do_culling = scene->get_occlusion_culling();
    if (!do_culling) {
        for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
            SceneObject *scene_object = (*it);
            RenderData* render_data = scene_object->render_data();
            addRenderData(render_data);
            scene->pick(scene_object);
        }
        scene->unlockColliders();
        return;
    }

    for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
        SceneObject *scene_object = (*it);
        RenderData* render_data = scene_object->render_data();
        if (render_data == 0 || render_data->material(0) == 0) {
            continue;
        }

        //If a query was issued on an earlier or same frame and if results are
        //available, then update the same. If results are unavailable, do nothing
        if (!scene_object->is_query_issued()) {
            continue;
        }

        //If a previous query is active, do not issue a new query.
        //This avoids overloading the GPU with too many queries
        //Queries may span multiple frames

        bool is_query_issued = scene_object->is_query_issued();
        if (!is_query_issued) {
            //Setup basic bounding box and material
            RenderData* bounding_box_render_data(new RenderData());
            Mesh* bounding_box_mesh = render_data->mesh()->createBoundingBox();
            Material *bbox_material = new Material(
                    Material::BOUNDING_BOX_SHADER);
            RenderPass *pass = new RenderPass();
            pass->set_material(bbox_material);
            bounding_box_render_data->set_mesh(bounding_box_mesh);
            bounding_box_render_data->add_pass(pass);

            GLuint *query = scene_object->get_occlusion_array();

            glDepthFunc (GL_LEQUAL);
            glEnable (GL_DEPTH_TEST);
            glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);

            glm::mat4 model_matrix_tmp(
                    scene_object->transform()->getModelMatrix());
            glm::mat4 mvp_matrix_tmp(vp_matrix * model_matrix_tmp);

            //Issue the query only with a bounding box
            glBeginQuery(GL_ANY_SAMPLES_PASSED, query[0]);
            shader_manager->getBoundingBoxShader()->render(mvp_matrix_tmp,
                    bounding_box_render_data,
                    bounding_box_render_data->material(0));
            glEndQuery (GL_ANY_SAMPLES_PASSED);
            scene_object->set_query_issued(true);

            glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);

            //Delete the generated bounding box mesh
            bounding_box_mesh->cleanUp();
            delete bbox_material;
            delete pass;
            delete bounding_box_render_data;
        }

        GLuint query_result = GL_FALSE;
        GLuint *query = (*it)->get_occlusion_array();
        glGetQueryObjectuiv(query[0], GL_QUERY_RESULT_AVAILABLE, &query_result);

        if (query_result) {
            GLuint pixel_count;
            glGetQueryObjectuiv(query[0], GL_QUERY_RESULT, &pixel_count);
            bool visibility = ((pixel_count & GL_TRUE) == GL_TRUE);

            (*it)->set_visible(visibility);
            (*it)->set_query_issued(false);
            addRenderData((*it)->render_data());
            scene->pick(scene_object);
        }
    }
    scene->unlockColliders();
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

void Renderer::renderCamera(Scene* scene, Camera* camera,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {
    GLint curFBO;
    GLint viewport[4];
    glGetIntegerv(GL_FRAMEBUFFER_BINDING, &curFBO);
    glGetIntegerv(GL_VIEWPORT, viewport);

    renderCamera(scene, camera, curFBO, viewport[0], viewport[1], viewport[2],
            viewport[3], shader_manager, post_effect_shader_manager,
            post_effect_render_texture_a, post_effect_render_texture_b);
}

void Renderer::renderCamera(Scene* scene, Camera* camera,
        RenderTexture* render_texture, ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    renderCamera(scene, camera, render_texture->getFrameBufferId(), 0, 0,
            render_texture->width(), render_texture->height(), shader_manager,
            post_effect_shader_manager, post_effect_render_texture_a,
            post_effect_render_texture_b);

}

void Renderer::renderCamera(Scene* scene, Camera* camera, int viewportX,
        int viewportY, int viewportWidth, int viewportHeight,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    renderCamera(scene, camera, 0, viewportX, viewportY, viewportWidth,
            viewportHeight, shader_manager, post_effect_shader_manager,
            post_effect_render_texture_a, post_effect_render_texture_b);
}

bool Renderer::isShader3d(const Material* curr_material) {
    bool shaders3d;

    switch (curr_material->shader_type()) {
    case Material::ShaderType::UNLIT_HORIZONTAL_STEREO_SHADER:
    case Material::ShaderType::UNLIT_VERTICAL_STEREO_SHADER:
    case Material::ShaderType::OES_SHADER:
    case Material::ShaderType::OES_HORIZONTAL_STEREO_SHADER:
    case Material::ShaderType::OES_VERTICAL_STEREO_SHADER:
    case Material::ShaderType::CUBEMAP_SHADER:
    case Material::ShaderType::CUBEMAP_REFLECTION_SHADER:
        shaders3d = false;
        break;
    case Material::ShaderType::TEXTURE_SHADER:
    case Material::ShaderType::EXTERNAL_RENDERER_SHADER:
    case Material::ShaderType::ASSIMP_SHADER:
    case Material::ShaderType::LIGHTMAP_SHADER:
    default:
        shaders3d = true;
        break;
    }

    return shaders3d;
}

bool Renderer::isDefaultPosition3d(const Material* curr_material) {
    bool defaultShadersForm = false;

    switch (curr_material->shader_type()) {
    case Material::ShaderType::TEXTURE_SHADER:
        defaultShadersForm = true;
        break;
    default:
        defaultShadersForm = false;
        break;
    }

    return defaultShadersForm;
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

void Renderer::renderMesh(RenderState& rstate, RenderData* render_data) {

    for (int curr_pass = 0; curr_pass < render_data->pass_count();
            ++curr_pass) {
        numberTriangles += render_data->mesh()->getNumTriangles();
        numberDrawCalls++;

        set_face_culling(render_data->pass(curr_pass)->cull_face());
        Material* curr_material = rstate.material_override;

        if (curr_material == nullptr)
            curr_material = render_data->pass(curr_pass)->material();
        if (curr_material != nullptr) {
            GL(renderMaterialShader(rstate, render_data, curr_material));
        }
    }
}

void Renderer::renderMaterialShader(RenderState& rstate, RenderData* render_data, Material *curr_material) {

    if (Material::ShaderType::BEING_GENERATED == curr_material->shader_type()) {
        return;
    }

    //Skip the material whose texture is not ready with some exceptions
    if (!checkTextureReady(curr_material))
        return;
    ShaderManager* shader_manager = rstate.shader_manager;
    Transform* const t = render_data->owner_object()->transform();

    if (t == nullptr)
        return;

    rstate.uniforms.u_model = t->getModelMatrix();
	rstate.uniforms.u_mv = rstate.uniforms.u_view * rstate.uniforms.u_model;
	rstate.uniforms.u_mv_it = glm::inverseTranspose(rstate.uniforms.u_mv);
	rstate.uniforms.u_mvp = rstate.uniforms.u_proj * rstate.uniforms.u_mv;
    rstate.uniforms.u_right = rstate.render_mask & RenderData::RenderMaskBit::Right;


    if(use_multiview){
        rstate.uniforms.u_view_[0] = rstate.scene->main_camera_rig()->left_camera()->getViewMatrix();
        rstate.uniforms.u_view_[1] = rstate.scene->main_camera_rig()->right_camera()->getViewMatrix();
        rstate.uniforms.u_mv_[0] = rstate.uniforms.u_view_[0] * rstate.uniforms.u_model;
        rstate.uniforms.u_mv_[1] = rstate.uniforms.u_view_[1] * rstate.uniforms.u_model;
        rstate.uniforms.u_mv_it_[0] = glm::inverseTranspose(rstate.uniforms.u_mv_[0]);
        rstate.uniforms.u_mv_it_[1] = glm::inverseTranspose(rstate.uniforms.u_mv_[1]);
        rstate.uniforms.u_mvp_[0] = rstate.uniforms.u_proj * rstate.uniforms.u_mv_[0];
        rstate.uniforms.u_mvp_[1] = rstate.uniforms.u_proj * rstate.uniforms.u_mv_[1];
    }
    Mesh* mesh = render_data->mesh();

    GLuint programId = -1;
    ShaderBase* shader = NULL;

    try {
         //TODO: Improve this logic to avoid a big "switch case"
        if (rstate.material_override != nullptr)
            curr_material = rstate.material_override;
         switch (curr_material->shader_type()) {
            case Material::ShaderType::UNLIT_HORIZONTAL_STEREO_SHADER:
            	shader = shader_manager->getUnlitHorizontalStereoShader();
                break;
            case Material::ShaderType::UNLIT_VERTICAL_STEREO_SHADER:
                shader = shader_manager->getUnlitVerticalStereoShader();
                break;
            case Material::ShaderType::OES_SHADER:
                shader = shader_manager->getOESShader();
                break;
            case Material::ShaderType::OES_HORIZONTAL_STEREO_SHADER:
                shader = shader_manager->getOESHorizontalStereoShader();
                break;
            case Material::ShaderType::OES_VERTICAL_STEREO_SHADER:
                shader = shader_manager->getOESVerticalStereoShader();
                break;
            case Material::ShaderType::CUBEMAP_SHADER:
                shader = shader_manager->getCubemapShader();
                break;
            case Material::ShaderType::CUBEMAP_REFLECTION_SHADER:
                if(use_multiview){
                    rstate.uniforms.u_view_inv_[0] = glm::inverse(rstate.uniforms.u_view_[0]);
                    rstate.uniforms.u_view_inv_[1] = glm::inverse(rstate.uniforms.u_view_[1]);
                }
                else
                    rstate.uniforms.u_view_inv = glm::inverse(rstate.uniforms.u_view);
                shader = shader_manager->getCubemapReflectionShader();
                break;
            case Material::ShaderType::TEXTURE_SHADER:
                shader = shader_manager->getTextureShader();
                break;
            case Material::ShaderType::EXTERNAL_RENDERER_SHADER:
                shader = shader_manager->getExternalRendererShader();
                break;
            case Material::ShaderType::ASSIMP_SHADER:
                shader = shader_manager->getAssimpShader();
                break;
            case Material::ShaderType::LIGHTMAP_SHADER:
                shader = shader_manager->getLightMapShader();
                break;
			case Material::ShaderType::UNLIT_FBO_SHADER:
				shader = shader_manager->getUnlitFboShader();
                break;
            default:
                shader = shader_manager->getCustomShader(curr_material->shader_type());
                break;
        }
         if (shader == NULL) {
             LOGE("Rendering error: GVRRenderData shader cannot be determined\n");
             shader_manager->getErrorShader()->render(&rstate, render_data, curr_material);
             return;
         }
         if ((render_data->draw_mode() == GL_LINE_STRIP) ||
             (render_data->draw_mode() == GL_LINES) ||
             (render_data->draw_mode() == GL_LINE_LOOP)) {
             if (curr_material->hasUniform("line_width")) {
                 float lineWidth = curr_material->getFloat("line_width");
                 glLineWidth(lineWidth);
             }
             else {
                 glLineWidth(1.0f);
             }
         }
         shader->render(&rstate, render_data, curr_material);
    } catch (const std::string &error) {
        LOGE(
                "Error detected in Renderer::renderRenderData; name : %s, error : %s",
                render_data->owner_object()->name().c_str(),
                error.c_str());
        shader_manager->getErrorShader()->render(&rstate, render_data, curr_material);
    }

    programId = shader->getProgramId();
    //there is no program associated with EXTERNAL_RENDERER_SHADER
    if (-1 != programId) {
        glBindVertexArray(mesh->getVAOId(programId));
        if (mesh->indices().size() > 0) {
            if(use_multiview) {
                glDrawElementsInstanced(render_data->draw_mode(), mesh->indices().size(), GL_UNSIGNED_SHORT, NULL, 2 );
            } else {
                glDrawElements(render_data->draw_mode(), mesh->indices().size(), GL_UNSIGNED_SHORT, 0);
            }
        } else {
            if(use_multiview) {
                glDrawArraysInstanced(render_data->draw_mode(), 0, mesh->vertices().size(),2);
            } else {
                glDrawArrays(render_data->draw_mode(), 0, mesh->vertices().size());
            }
        }
    }
    checkGlError("renderMesh::renderMaterialShader");
}

bool Renderer::checkTextureReady(Material* material) {
    int shaderType = material->shader_type();

    //Skip custom shader here since they are rendering multiple textures
    //Check the textures later inside the rendering pass inside the custom shader
    if (shaderType < 0
            || shaderType >= Material::ShaderType::BUILTIN_SHADER_SIZE) {
        return true;
    }
    //For regular shaders, check its main texture
    else if (shaderType != Material::ShaderType::ASSIMP_SHADER) {
        return material->isMainTextureReady();
    }
    //For ASSIMP_SHADER as diffused texture, check its main texture
    //For non diffused texture, the rendering doesn't take any textures and needs to be skipped
    else if (ISSET(material->get_shader_feature_set(), AS_DIFFUSE_TEXTURE)) {
        return material->isMainTextureReady();
    }
    else {
        return true;
    }
}

void Renderer::renderPostEffectData(Camera* camera,
        RenderTexture* render_texture, PostEffectData* post_effect_data,
        PostEffectShaderManager* post_effect_shader_manager) {
    try {
        switch (post_effect_data->shader_type()) {
        case PostEffectData::ShaderType::COLOR_BLEND_SHADER:
            post_effect_shader_manager->getColorBlendPostEffectShader()->render(
                    render_texture, post_effect_data,
                    post_effect_shader_manager->quad_vertices(),
                    post_effect_shader_manager->quad_uvs(),
                    post_effect_shader_manager->quad_triangles());
            break;
        case PostEffectData::ShaderType::HORIZONTAL_FLIP_SHADER:
            post_effect_shader_manager->getHorizontalFlipPostEffectShader()->render(
                    render_texture, post_effect_data,
                    post_effect_shader_manager->quad_vertices(),
                    post_effect_shader_manager->quad_uvs(),
                    post_effect_shader_manager->quad_triangles());
            break;
        default:
            post_effect_shader_manager->getCustomPostEffectShader(
                    post_effect_data->shader_type())->render(camera,
                    render_texture, post_effect_data,
                    post_effect_shader_manager->quad_vertices(),
                    post_effect_shader_manager->quad_uvs(),
                    post_effect_shader_manager->quad_triangles());
            break;
        }
    } catch (const std::string& error) {
        LOGE(
                "Error detected in Renderer::renderPostEffectData; error : %s", error.c_str());
    }
}

void Renderer::set_face_culling(int cull_face) {
    switch (cull_face) {
    case RenderData::CullFront:
        glEnable (GL_CULL_FACE);
        glCullFace (GL_FRONT);
        break;

    case RenderData::CullNone:
        glDisable(GL_CULL_FACE);
        break;

        // CullBack as Default
    default:
        glEnable(GL_CULL_FACE);
        glCullFace (GL_BACK);
        break;
    }
}
}
