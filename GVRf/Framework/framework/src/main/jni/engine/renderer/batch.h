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
	Batch(int,int);
	~Batch();
	bool add(RenderData *render_data, Material *material);
	void UpdateModelMatrix(RenderData* renderdata, glm::mat4 model_matrix){
		if(renderdata){
			matrices_[matrix_index_map[renderdata]] = model_matrix;
		}
	}
	void removeRenderData(RenderData* renderdata){
		render_data_set_.erase(renderdata);
	}
	void setMeshesDirty();
	const std::vector<glm::mat4>& get_matrices() {
		return matrices_;
	}
	int getNumberOfMeshes(){
		return draw_count;
	}
	RenderData* get_renderdata() {
		return renderdata;
	}
	std::set<RenderData*>& getRenderDataSet(){
		return render_data_set_;
	}
	Material *get_material() {
		return material_;
	}
	bool notBatched(){
		return not_batched_;
	}
	bool isBatchDirty();
	unsigned int getIndexCount(){
		return index_count_;
	}
private:
	std::map<RenderData*,int>matrix_index_map;
	std::set<RenderData*>render_data_set_;  // use it later if we want to modify meshes

	Mesh *mesh;
	RenderData *renderdata;
	Material *material_;

	std::vector<glm::vec3> vertices_;
	std::vector<glm::vec3> normals_;
	std::vector<glm::vec2> tex_coords_;
	std::vector<unsigned short> indices_;
	std::vector<glm::mat4> matrices_;
	std::vector<float> matrix_indices;

	int vertex_limit_;
	int indices_limit_;
	int draw_count;

	unsigned int index_offset_;
	unsigned int vertex_count_;
	unsigned int index_count_;


	bool not_batched_;

};
}
#endif
