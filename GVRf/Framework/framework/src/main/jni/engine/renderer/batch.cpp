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

namespace gvr{
Batch::Batch(): mesh_count(0), draw_count(0), vertex_count_(0), vao_dirty(true),
		renderdata(nullptr){
	vertices_.clear();
    normals_.clear();
    tex_coords_.clear();
    indices_.clear();
    matrices_.clear();
    matrix_indices.clear();
    mesh = new Mesh(); // should perhaps have a pool of meshes of a certain size
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
void Batch::add(RenderData *render_data, Material *material) {
	 material_ = material;
	 Mesh *render_mesh = render_data->mesh();
	 render_data_set_.insert(render_data);   // store all the renderdata which are in batch
	 render_mesh->setMeshModified(false);    // mark mesh clean

	 // Copy all renderData properties
	 if(!renderdata){
		 renderdata = new RenderData(*render_data);
		 renderdata->set_batching(true);
	 }

	 // mark transforms clean
	 render_data->owner_object()->setTransformUnDirty();
	 mesh_count++;

    std::vector<glm::vec3> vertices = render_mesh->vertices();
    std::vector<glm::vec3> normals = render_mesh->normals();
    std::vector<glm::vec2> tex_coords = render_mesh->tex_coords();
    std::vector<unsigned short> indices = render_mesh->indices();
    Transform* const t = render_data->owner_object()->transform();
    glm::mat4 model_matrix;
    if(t!=NULL) {
        model_matrix = glm::mat4(t->getModelMatrix());
    }

    matrix_index_map[render_data]= matrices_.size();
    matrices_.push_back(model_matrix);

    int size = 0;
    size = vertices.size();

    for(int i=0; i<size; i++) {
        vertices_.push_back(vertices[i]);
        matrix_indices.push_back((float)draw_count); // use this for modelmatrix index
    }
    size = normals.size();
    for(int i=0; i<size; i++) {
        normals_.push_back(normals[i]);
    }

    size = tex_coords.size();
    for(int i=0; i<size; i++) {
        tex_coords_.push_back(tex_coords[i]);
    }
    size = indices.size();
    for(int i=0; i<size; i++) {
        unsigned short index = indices[i];
        index += vertex_count_;
        indices_.push_back(index);
    }

    vertex_count_+=vertices.size();
    draw_count++;
    vao_dirty = true;

}
/*
 *  Create mesh for the batch
 */
void Batch::update(){
	if(vao_dirty){
		mesh->set_vertices(vertices_);
		mesh->set_normals(normals_);
		mesh->set_tex_coords(tex_coords_);
		mesh->set_indices(indices_);
		mesh->setVertexAttribLocF(3, "a_matrix_index"); // 3 is picked since 0, 1, and 2 are reserved
		mesh->setFloatVector("a_matrix_index", matrix_indices);
		mesh->generateVAO();

		renderdata->set_mesh(mesh);
		vao_dirty = false;
	}

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
