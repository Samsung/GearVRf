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
#include "objects/components/camera.h"
#include "objects/components/render_data.h"
#include "objects/light.h"
#define BATCH_SIZE 60
namespace gvr{
Batch::Batch():  draw_count(0), vertex_count_(0),  index_count_(0), vertex_limit_(400),indices_limit_(400),
        renderdata(nullptr),index_offset_(0), not_batched_(false){
	vertices_.clear();
    normals_.clear();
    tex_coords_.clear();
    indices_.clear();
    matrices_.clear();
    matrix_indices.clear();
    mesh = new Mesh(); // should perhaps have a pool of meshes of a certain size
}
Batch::Batch(int no_vertices, int no_indices): draw_count(0), vertex_count_(0),  index_count_(0), vertex_limit_(no_vertices),indices_limit_(no_indices),
        renderdata(nullptr),index_offset_(0), not_batched_(false){

    vertices_.clear();
    normals_.clear();
    tex_coords_.clear();
    indices_.clear();
    matrices_.clear();
    matrix_indices.clear();
    render_data_set_.clear();

    vertices_.reserve(no_vertices);
    indices_.reserve(no_indices);
    normals_.reserve(no_vertices);
    tex_coords_.reserve(no_vertices);
    matrix_indices.reserve(no_vertices);
    glm::mat4 identity;
    mesh = new Mesh();
    glm::vec3 init_vec3(0,0,0);
    glm::vec2 init_vec2(0,0);
    for(int i=0;i<no_vertices;i++){
        vertices_.push_back(init_vec3);
        tex_coords_.push_back(init_vec2);
        matrix_indices.push_back(0);
        normals_.push_back(init_vec3);
        indices_.push_back(0);
    }
    for(int i=0;i<60;i++)
        matrices_.push_back(identity);
}

Batch::~Batch() {
    vertices_.clear();
    normals_.clear();
    tex_coords_.clear();
    indices_.clear();
    matrices_.clear();
    matrix_indices.clear();
    delete renderdata;
    delete mesh;
}

/*
 * Add renderdata of scene object into mesh, add vertices, texcoords, normals, model matrices
 */
bool Batch::add(RenderData *render_data, Material *material) {
	 material_ = material;
	 Mesh *render_mesh = render_data->mesh();
	 const std::vector<unsigned short>& indices = render_mesh->indices();

	 // if it is not texture shader, dont add into batch, render in normal way
     if(material->shader_type() != Material::ShaderType::TEXTURE_SHADER){
    	 render_data_set_.insert(render_data);
    	 return true;
     }

     // if mesh is large, render in normal way
     if(indices.size() + index_count_ > indices_limit_){
    	 if(draw_count > 0){
    		 return false;
    	 }
    	 else {
    		 render_data_set_.insert(render_data);
        	 not_batched_ = true;
        	 return true;
    	 }
     }
	 // Copy all renderData properties
     if(draw_count == 0){

		 if(!renderdata){
			 renderdata = new RenderData(*render_data);
			 renderdata->set_batching(true);
			 mesh->set_vertices(vertices_);
			 mesh->set_normals(normals_);
			 mesh->set_tex_coords(tex_coords_);
			 mesh->set_indices(indices_);

			 // if render data has light, custom attribute will have location:3 if it does not have light,
			 // it will have location 2
			 if(renderdata->light_enabled() && renderdata->light()->enabled()){
				 mesh->setVertexAttribLocF(3, "a_matrix_index");
			 }
			 else{
				 mesh->setVertexAttribLocF(2, "a_matrix_index");
			 }
			 mesh->setFloatVector("a_matrix_index", matrix_indices);
			 mesh->generateVAO();
		 }
		 else
			 renderdata->copy(*render_data);

		 renderdata->set_mesh(mesh);
     }

	 render_data_set_.insert(render_data);   // store all the renderdata which are in batch
	 render_mesh->setMeshModified(false);    // mark mesh clean
	 // mark transforms clean
	 render_data->owner_object()->setTransformUnDirty();
	 Transform* const t = render_data->owner_object()->transform();
	 glm::mat4 model_matrix;
	 if(t!=NULL) {
		 model_matrix = glm::mat4(t->getModelMatrix());
	  }

	 // Store the model matrix and its index into map for update
	 matrix_index_map[render_data]= draw_count;
	 matrices_[draw_count]=model_matrix;

	 const std::vector<glm::vec3>& vertices = render_mesh->vertices();
	 const std::vector<glm::vec3>& normals = render_mesh->normals();
	 const std::vector<glm::vec2>& tex_coords = render_mesh->tex_coords();
	 int size = 0;

	 size = vertices.size();

	 std::fill(matrix_indices.begin()+ vertex_count_, matrix_indices.begin()+vertex_count_+size, (float)draw_count);

	 size = indices.size();
	 unsigned int indx_start = index_count_;

	 for(int i=0; i<size; i++) {
		 unsigned short index = indices[i];
		 index += index_offset_;
		 indices_[index_count_++]=index;
	 }

	 // update all VBO data
	 mesh->updateVBOs(vertex_count_,indx_start,index_count_,matrix_indices,vertices,indices_,tex_coords, normals);

	 vertex_count_+=vertices.size();
	 index_offset_+=vertices.size();
	 draw_count++;
	 return true;
}

/*
 *  Check if any of the meshes in batch are modified
 */
bool Batch::isBatchDirty(){
	for(auto it= render_data_set_.begin();it!=render_data_set_.end();it++){
		if((*it)->mesh()->isMeshModified())
			return true;
	}
	return false;
}

/*
 * Set batch in render data to null. can be use to mark dirty.
 */
void Batch::setMeshesDirty(){
	for(auto it= render_data_set_.begin();it!=render_data_set_.end();it++){
		(*it)->setBatchNull();
	}
}
}
