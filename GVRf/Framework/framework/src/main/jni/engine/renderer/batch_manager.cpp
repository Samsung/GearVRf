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
#include "batch.h"
#include "objects/scene_object.h"
#include "objects/components/render_data.h"
#include "batch_manager.h"
#include "renderer.h"
#include "shaders/shader_manager.h"
#include "objects/scene.h"
#include "objects/scene_object.h"
#include "objects/components/camera.h"
#define BATCH_SIZE 60
namespace gvr {
 /*
  * It creates array of indices which specifies indices of the spliting of batches in renderdata vector
  * for renderdatas to have in same batch, they need to have same render order, material,
  * shader type and mesh dynamic-ness
  */
void BatchManager::batchSetup(std::vector<RenderData*>& render_data_vector) {
   batch_indices_.clear();
   int render_vector_size = render_data_vector.size();
   RenderData* prev = nullptr;
   RenderData* curr = nullptr;

   if(render_vector_size != 0){
       batch_indices_.push_back(0);
       prev = render_data_vector[0];
   }
    /*
        Separate batch is created for following cases
       case 1 : if prev render data has batching disabled
       case 2: if current render data has batching disabled
       case 3: if any of the properties fails to match between current and render data:  rendering order/ static or dynamic mesh/ no of passes/
               material and cull status in each render-pass, different states in render-data
    */
    for (int i = 1; i < render_vector_size ; i++) {
        curr = render_data_vector[i];
        if(!(prev->batching() && prev->rendering_order() == curr->rendering_order() && prev->mesh()->isDynamic() == curr->mesh()->isDynamic()
              && isRenderPassEqual(prev,curr) && !prev->getHashCode().compare(curr->getHashCode())) || !curr->batching()){
            batch_indices_.push_back(i);
            prev = curr;
        }
    }
    batch_indices_.push_back(render_data_vector.size());
    clearBatchSet();
    for (int i = 1; i < batch_indices_.size(); i++) {
        createBatch(batch_indices_[i - 1], batch_indices_[i] - 1,render_data_vector);
    }
}
void BatchManager::renderBatches(RenderState& rstate) {

    for (auto it = batch_set_.begin(); it != batch_set_.end(); ++it) {
        Batch* batch = *it;
        rstate.material_override = batch->material(0);
        if(rstate.material_override == nullptr)
            continue;

         int currentShaderType = batch->material(0)->shader_type();
        // if shader type is other than texture shader, render it with non-batching mode
        // if the mesh is large, we are not batching it
        if (currentShaderType != Material::ShaderType::TEXTURE_SHADER
                || batch->notBatched()) {
                
            rstate.material_override = nullptr;
            const std::unordered_set<RenderData*>& render_data_set = batch->getRenderDataSet();
            for (auto it3 = render_data_set.begin();
                    it3 != render_data_set.end(); ++it3) {
                if((*it3)->owner_object()==nullptr){
                    continue;
                }
               // this check is needed as we are not removing render data from batches
               if(!(*it3)->owner_object()->isCulled() && (*it3)->enabled() && (*it3)->owner_object()->enabled())
                    Renderer::renderRenderData(rstate, (*it3));
            }
            continue;
        }

        RenderData* renderdata = batch->get_renderdata();

        if(renderdata == nullptr)
            continue;

        if (!(rstate.render_mask & renderdata->render_mask()))
            continue;

        batch->setupMesh();

        if(!batch->renderDataSetSize())
           continue;

        Renderer::setRenderStates(renderdata, rstate);

        if(use_multiview){
           rstate.uniforms.u_view_[0] = rstate.scene->main_camera_rig()->left_camera()->getViewMatrix();
           rstate.uniforms.u_view_[1] = rstate.scene->main_camera_rig()->right_camera()->getViewMatrix();
        }

        const std::vector<glm::mat4>& matrices = batch->get_matrices();

        for(int passIndex =0; passIndex< renderdata->pass_count(); passIndex++){
            Renderer::set_face_culling(renderdata->pass(passIndex)->cull_face());
            rstate.material_override = batch->material(passIndex);
            if(rstate.material_override == nullptr)
                continue;

            rstate.shader_manager->getTextureShader()->render_batch(matrices,
                        renderdata, rstate, batch->getIndexCount(),
                        batch->getNumberOfMeshes());
        }

        Renderer::restoreRenderStates(renderdata);
    }
}

void BatchManager::createBatch(int start, int end, std::vector<RenderData*>& render_data_vector) {
     Batch* existing_batch = nullptr;
     int size = batch_size_;
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
                         && existing_batch->getNumberOfMeshes() < batch_size_) {
                     // add failed because mesh is large try with next batch
                     if (!existing_batch->add(render_data)) {
                         getNewBatch(render_data, &existing_batch);
                     }

                     // if batch does not exist in set, add it
                     if (batch_map_.find(existing_batch) == batch_map_.end()) {
                         batch_set_.push_back(existing_batch);
                         batch_map_[existing_batch] = batch_set_.size() - 1;

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
                             batch_map_.find(existing_batch);
                     if (it != batch_map_.end()) {
                         int index = it->second;
                         batch_set_.erase(batch_set_.begin() + index);
                     }
                     delete existing_batch;
                     getNewBatch(render_data, &existing_batch);
                 }
                 // existing batch is not full
                 else if (existing_batch
                         && existing_batch->getNumberOfMeshes() < batch_size_) {

                     if (!existing_batch->add(render_data)) {
                         getNewBatch(render_data, &existing_batch);
                     }

                     render_data->setBatch(existing_batch);

                     if (batch_map_.find(existing_batch) == batch_map_.end()) {
                         batch_set_.push_back(existing_batch);
                         batch_map_[existing_batch] = batch_set_.size() - 1;
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
                 if (batch_map_.find(current_batch) == batch_map_.end()) {
                     batch_set_.push_back(current_batch);
                     batch_map_[current_batch] = batch_set_.size() - 1;
                 }
             } else { // mesh is dynamic
                 if (current_batch->isBatchDirty()) {
                     current_batch->setMeshesDirty();

                     std::unordered_map<Batch*, int>::iterator it =
                             batch_map_.find(current_batch);
                     if (it != batch_map_.end()) {
                         int index = it->second;
                         batch_set_.erase(batch_set_.begin() + index);
                     }

                     delete current_batch;
                     Batch* new_batch = new Batch(max_indices_, max_indices_);
                     render_data->setBatch(new_batch);
                     new_batch->add(render_data);
                     batch_set_.push_back(new_batch);
                     batch_map_[new_batch] = batch_set_.size() - 1;

                 } else {
                     if (batch_map_.find(current_batch) == batch_map_.end()) {
                         batch_set_.push_back(current_batch);
                         batch_map_[current_batch] = batch_set_.size() - 1;
                     }
                 }
             }
         }
     }
}
void BatchManager::getNewBatch(RenderData* rdata, Batch** existing_batch){
    Batch* new_batch = new Batch(max_indices_, max_indices_);
    new_batch->add(rdata);
    rdata->setBatch(new_batch);
    batch_set_.push_back(new_batch);
    batch_map_[new_batch] = batch_set_.size() - 1;
    *existing_batch = new_batch;
}

}