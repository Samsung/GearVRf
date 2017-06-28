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

#ifndef BATCH_H
#define BATCH_H
#include "renderer.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include <map>
#include <unordered_map>
#include "batch_manager.h"
#include <memory>
#include <vector>
#include <string>
#include <set>
#include<unordered_set>
namespace gvr{
class RenderData;
class Material;
class Mesh;
class Batch {
public:
    Batch();
    Batch(int,int);
    ~Batch();
    bool add(RenderData *render_data);
    bool setupMesh(bool);
    void resetBatch();
    void meshInit();
    void regenerateMeshData();
    void UpdateModelMatrix(RenderData* renderdata, glm::mat4 model_matrix){
        if(renderdata){
            matrices_[matrix_index_map_[renderdata]] = model_matrix;
        }
    }
    void removeRenderData(RenderData* renderdata){
        renderdata->set_batching(false);
        render_data_set_.erase(renderdata);
        batch_dirty_ = true;
        if(0 == render_data_set_.size())
            resetBatch();
    }
    const std::vector<glm::mat4>& get_matrices() {
        return matrices_;
    }
    int getNumberOfMeshes(){
        return draw_count_;
    }
    RenderData* get_renderdata() {
        return renderdata_;
    }
    const std::unordered_set<RenderData*>& getRenderDataSet(){
        return render_data_set_;
    }
    bool notBatched(){
        return not_batched_;
    }
    Material* material(int passIndex=0){
        if(passIndex ==0)
            return material_;
        return renderdata_->pass(passIndex)->material();
    }
    void setDirty(bool dirty){
        batch_dirty_ = dirty;
    }
    bool isBatchDirty(){
        return batch_dirty_;
    }
    int renderDataSetSize(){
        return render_data_set_.size();
    }
    unsigned int getIndexCount(){
        return index_count_;
    }
private:
    void updateMesh(Mesh* render_mesh);
    void clearData();
    bool isRenderModified();
    bool batch_dirty_;
    std::unordered_map<RenderData*,int>matrix_index_map_;
    std::unordered_set<RenderData*>render_data_set_;
    Mesh mesh_;
    RenderData *renderdata_;
    Material *material_;
    std::vector<glm::vec3> vertices_;
    std::vector<glm::vec3> normals_;
    std::vector<glm::vec2> tex_coords_;
    std::vector<unsigned short> indices_;
    std::vector<glm::mat4> matrices_;
    std::vector<float> matrix_indices_;
    int vertex_limit_;
    int indices_limit_;
    int draw_count_;
    unsigned int index_offset_;
    unsigned int vertex_count_;
    unsigned int index_count_;
    bool mesh_init_;
    bool not_batched_;
};
}
#endif
