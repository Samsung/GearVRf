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
#define BATCH_SIZE 60

namespace gvr {

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

    // 1. Travese all scene objects in the scene as a tree and do frustum culling at the same time if enabled
    if (scene->get_frustum_culling()) {
        if (DEBUG_RENDERER) {
           LOGD("FRUSTUM: start frustum culling\n");
       }

        // 1. Build the view frustum
        float frustum[6][4];
        build_frustum(frustum, (const float*) glm::value_ptr(vp_matrix));

        // 2. Iteratively execute frustum culling for each root object (as well as its children objects recursively)
        std::vector<SceneObject*> root_objects = scene->scene_objects();
        for (auto it = root_objects.begin(); it != root_objects.end(); ++it) {
            SceneObject *object = *it;
            if (DEBUG_RENDERER) {
                LOGD("FRUSTUM: start frustum culling for root %s\n",
                        object->name().c_str());
            }
            frustum_cull(camera->owner_object()->transform()->position(), object, frustum, scene_objects, true, 0);
            if (DEBUG_RENDERER) {
                LOGD("FRUSTUM: end frustum culling for root %s\n",
                        object->name().c_str());
            }
        }
        if (DEBUG_RENDERER) {
            LOGD("FRUSTUM: end frustum culling\n");
        }
    }
    else
        scene_objects = scene->getWholeSceneObjects();
    // 2. do occlusion culling, if enabled
    occlusion_cull(scene, scene_objects, shader_manager, vp_matrix);
}

/*
 * store the batches with same material together
 */
static std::map<Material*, std::vector<Batch*>> static_batch_map;
static std::map<Material*, std::vector<Batch*>> dynamic_batch_map;
/*
 * Iterate through material_map and render all the batches
 */
void Renderer::renderBatches(RenderState& rstate,std::map<Material*, std::vector<Batch*>>& material_map ) {
    glm::mat4 vp_matrix = glm::mat4(rstate.uniforms.u_proj *rstate.uniforms.u_view);

    for(auto it=material_map.begin();it!=material_map.end();++it){
       std::vector<Batch*> batch_vector = it->second;

       for(int i=0;i<batch_vector.size();i++){

    	   Batch* batch = batch_vector[i];
    	   batch->update();
    	   RenderData* renderdata= batch->get_renderdata();
           std::vector<glm::mat4> matrices=batch->get_matrices();
           rstate.material_override = batch->get_material();
           rstate.shader_manager->getTextureShader()->render_batch(
        		  matrices,renderdata, rstate);

       }
    }
}

bool do_batching = true;

void Renderer::renderRenderDataVector(RenderState &rstate) {

	if(!do_batching) {
        for (auto it = render_data_vector.begin(); it != render_data_vector.end(); ++it) {
            GL(renderRenderData(rstate,*it));
        }
    } else {

        int size = render_data_vector.size();
        std::vector<int>unbatched_indices;
        unbatched_indices.reserve(100);
        /*
         * clear the material map before each rendering, if one of the batch is not in the scene
         * don't render that batch
         */
        static_batch_map.clear();
        dynamic_batch_map.clear();

        for(int i=0; i<size; i++) {

        	/* Store all the indices of renderdata for which batching is disabled, this might be becuase these
        	 * objects are intended to be rendered last
        	*/
        	if(!render_data_vector[i]->batching()){
        		unbatched_indices.push_back(i);
        		continue;
        	}

            int pass_count = render_data_vector[i]->pass_count();
            if(size == 1 || pass_count > 1) {
            	 GL(renderRenderData(rstate,render_data_vector[i]));
            	 return;
            }
            int currentShaderType = render_data_vector[i]->pass(0)->material()->shader_type();

            // render in normal way if not texture shader
            if(currentShaderType != Material::ShaderType::TEXTURE_SHADER) {
            	 GL(renderRenderData(rstate,render_data_vector[i]));
            	 continue;
            }

            Batch* renderdata_batch = render_data_vector[i]->getBatch();
            Material *material = render_data_vector[i]->pass(0)->material();

            bool is_dynamic = render_data_vector[i]->mesh()->isDynamic();
        	std::map<Material*, std::vector<Batch*>>* batch_map;
        	if(is_dynamic){
        		batch_map = &dynamic_batch_map;
        	}
        	else{
        		batch_map = &static_batch_map;
        	}

            // check if render data is newly introduces and does not have batch
            if(renderdata_batch == nullptr){

            	// find out existing material in the map, if exists update the existing batch
            	// if material does not exists, create new batch and add it into map
            	std::map<Material*, std::vector<Batch*>>::iterator it=(*batch_map).find(material);

                if(it!=(*batch_map).end()){
                	std::vector<Batch*> batch_vector = it->second;
                	Batch* batch = batch_vector.back();

                	// existing batch is not full
                	if(batch->getNumberOfMeshes() < BATCH_SIZE){
                		// mesh is dynamic and of the mesh in batch is moodified so we need to discard old batch
                		if(is_dynamic && batch->isBatchDirty()){
                			batch->setMeshesDirty();
                			batch_vector.erase(batch_vector.end()-1);
                			Batch *new_batch = new Batch();
                			new_batch->add(render_data_vector[i], material);
                			render_data_vector[i]->setBatch(new_batch);
                			std::vector<Batch*>batch_vector = (*batch_map)[material];
        					batch_vector.push_back(new_batch);
        					(*batch_map)[material] = batch_vector;

                		}
                		else {
                			render_data_vector[i]->setBatch(batch);
                			batch->add(render_data_vector[i], material);
                		}

                	}
                	else{ // batch is full, create new batch
    					Batch *new_batch = new Batch();
    					new_batch->add(render_data_vector[i], material);
    					render_data_vector[i]->setBatch(new_batch);
    					std::vector<Batch*>batch_vector = (*batch_map)[material];
    					batch_vector.push_back(new_batch);
    					(*batch_map)[material] = batch_vector;
                	}
                }
                else { // material is newly ecnountered, create new batch

					Batch *new_batch = new Batch();
					new_batch->add(render_data_vector[i], material);
					render_data_vector[i]->setBatch(new_batch);
					std::vector<Batch*>batch_vector;
					batch_vector.push_back(new_batch);
					(*batch_map)[material] = batch_vector;

                }
            }
            else { // renderdata already has the batch

            		if(render_data_vector[i]->owner_object()->isTransformDirty() && render_data_vector[i]->owner_object()->transform()){
            			// object transform is modified
            			renderdata_batch->UpdateModelMatrix(render_data_vector[i], render_data_vector[i]->owner_object()->transform()->getModelMatrix());
            		}

                	std::map<Material*, std::vector<Batch*>>::iterator it=(*batch_map).find(material);
                	// check whether the material is stored in map if not add it
                    if(it!=(*batch_map).end()){
                    	std::vector<Batch*> batch_vector = it->second;
                    	bool batch_found = false;
                    	int i = 0;
                    	for(i=0;i<batch_vector.size();i++){
                    		if(renderdata_batch == batch_vector[i]){
								batch_found = true;
								break;
                    		}
                    	}
                    	if(!batch_found && !is_dynamic){
         					 // mesh is not modified
        						batch_vector.push_back(renderdata_batch);
        						(*batch_map)[material] = batch_vector;
                    	}
                   		// check whether the mesh is modified if it is, discard the old batch
        				if(is_dynamic &&
        				(render_data_vector[i]->mesh()->isMeshModified() || renderdata_batch->isBatchDirty())){
        					renderdata_batch->setMeshesDirty();
        					if(batch_found)
        						batch_vector.erase(batch_vector.begin()+i);
        					Batch *new_batch = new Batch();
        					new_batch->add(render_data_vector[i], material);
        					render_data_vector[i]->setBatch(new_batch);
        					std::vector<Batch*>batch_vector = (*batch_map)[material];
        					batch_vector.push_back(new_batch);
        					(*batch_map)[material] = batch_vector;

        				}


                    }
                    else{ // material does not exists in map but renderdata has a batch, just add it into map

                    	//check if one of the mesh in batch is modified if it is, discard the old batch
                    	if(is_dynamic &&
    					(render_data_vector[i]->mesh()->isMeshModified() || renderdata_batch->isBatchDirty())){
    						renderdata_batch->setMeshesDirty();
    						Batch *new_batch = new Batch();
    						new_batch->add(render_data_vector[i], material);
    						render_data_vector[i]->setBatch(new_batch);
    						std::vector<Batch*>batch_vector;
    						batch_vector.push_back(new_batch);
    						(*batch_map)[material] = batch_vector;

    					}
    					else {
    						std::vector<Batch*> batch_vector;
                    		batch_vector.push_back(renderdata_batch);
                    		(*batch_map)[material] = batch_vector;
    					}

                    }

            	}
            }

        renderBatches(rstate, static_batch_map);
        renderBatches(rstate, dynamic_batch_map);

        // render the objects with high rendering order to keep them front
        for(int i=0;i<unbatched_indices.size();i++){
        	rstate.material_override = render_data_vector[unbatched_indices[i]]->pass(0)->material();
        	GL(renderRenderData(rstate,render_data_vector[unbatched_indices[i]]));
        }

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

    if (post_effects.size() == 0) {
        GL(glBindFramebuffer(GL_FRAMEBUFFER, framebufferId));
        GL(glViewport(viewportX, viewportY, viewportWidth, viewportHeight));

        GL(glClearColor(camera->background_color_r(),
                camera->background_color_g(), camera->background_color_b(),
                camera->background_color_a()));
        GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
        renderRenderDataVector(rstate);
    }
    else {
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
    if (render_data == 0 || render_data->material(0) == 0) {
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

    bool do_culling = scene->get_occlusion_culling();
    if (!do_culling) {
        for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
            SceneObject *scene_object = (*it);
            RenderData* render_data = scene_object->render_data();
            addRenderData(render_data);
        }
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
        }
    }
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

    if (!rstate.render_mask || !render_data->render_mask())
        return;

    if (render_data->offset()) {
        GL(glEnable (GL_POLYGON_OFFSET_FILL));
        GL(glPolygonOffset(render_data->offset_factor(),
                    render_data->offset_units()));
    }
    if (!render_data->depth_test()) {
        GL(glDisable (GL_DEPTH_TEST));
    }
    if (!render_data->alpha_blend()) {
        GL(glDisable (GL_BLEND));
    }
    if( render_data->alpha_to_coverage()) {
        GL(glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE));
        GL(glSampleCoverage(render_data->sample_coverage(),render_data->invert_coverage_mask()));
    }
    if (render_data->mesh() != 0) {
        GL(renderMesh(rstate, render_data));
    }

    // Restoring to Default.
    // TODO: There's a lot of redundant state changes. If on every render face culling is being set there's no need to
    // restore defaults. Possibly later we could add a OpenGL state wrapper to avoid redundant api calls.
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
	try {
         //TODO: Improve this logic to avoid a big "switch case"
        ShaderBase* shader = NULL;
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
        shader->render(&rstate, render_data, curr_material);
    } catch (const std::string &error) {
        LOGE(
                "Error detected in Renderer::renderRenderData; name : %s, error : %s",
                render_data->owner_object()->name().c_str(),
                error.c_str());
        shader_manager->getErrorShader()->render(&rstate, render_data, curr_material);
    }
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
