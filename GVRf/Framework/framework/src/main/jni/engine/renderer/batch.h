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
#include <map>
#include <memory>
#include <vector>
#include <string>
#include <set>
namespace gvr{

class RenderData;
class Material;
class Mesh;
class Batch {
    public:
        Batch();
        ~Batch();
        void add(RenderData *render_data, Material *material);
        void UpdateModelMatrix(RenderData* renderdata, glm::mat4 model_matrix){
        	if(renderdata){
        		matrices_[matrix_index_map[renderdata]] = model_matrix;
        	}
        }
        void removeRenderData(RenderData* renderdata){
        	render_data_set_.erase(renderdata);
        }
        void update();

        void setMeshesDirty();
        const std::vector<glm::mat4>& get_matrices() {
            return matrices_;
        }
        int getNumberOfMeshes(){
        	return render_data_set_.size();
        }
         RenderData* get_renderdata() {
            return renderdata;
        }

         Material *get_material() {
            return material_;
        }
    private:
        std::map<RenderData*,int>matrix_index_map;
        std::set<RenderData*>render_data_set_;  // use it later if we want to modify meshes
        int mesh_count;
        Mesh *mesh;
        RenderData *renderdata;
        Material *material_;

        std::vector<glm::vec3> vertices_;
        std::vector<glm::vec3> normals_;
        std::vector<glm::vec2> tex_coords_;
        std::vector<unsigned short> indices_;
        std::vector<glm::mat4> matrices_;
        std::vector<float> matrix_indices;
        int draw_count;
        bool vao_dirty;
        int vertex_count_;
};
}
#endif
