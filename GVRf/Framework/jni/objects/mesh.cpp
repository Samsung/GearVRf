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
 * The mesh for rendering.
 ***************************************************************************/

#include "mesh.h"

#include <limits>

#include "assimp/Importer.hpp"
#include "assimp/mesh.h"
#include "assimp/postprocess.h"
#include "assimp/scene.h"
#include "util/gvr_log.h"
#include "util/gvr_gl.h"

namespace gvr {
std::shared_ptr<Mesh> Mesh::getBoundingBox() const {
    Mesh* mesh = new Mesh();
    float* bounding_box_info = this->getBoundingBoxInfo();
    float min_x = bounding_box_info[0];
    float max_x = bounding_box_info[3];
    float min_y = bounding_box_info[1];
    float max_y = bounding_box_info[4];
    float min_z = bounding_box_info[2];
    float max_z = bounding_box_info[5];

    mesh->vertices_.push_back(glm::vec3(min_x, min_y, min_z));
    mesh->vertices_.push_back(glm::vec3(max_x, min_y, min_z));
    mesh->vertices_.push_back(glm::vec3(min_x, max_y, min_z));
    mesh->vertices_.push_back(glm::vec3(max_x, max_y, min_z));
    mesh->vertices_.push_back(glm::vec3(min_x, min_y, max_z));
    mesh->vertices_.push_back(glm::vec3(max_x, min_y, max_z));
    mesh->vertices_.push_back(glm::vec3(min_x, max_y, max_z));
    mesh->vertices_.push_back(glm::vec3(max_x, max_y, max_z));

    mesh->triangles_.push_back(0);
    mesh->triangles_.push_back(2);
    mesh->triangles_.push_back(1);
    mesh->triangles_.push_back(1);
    mesh->triangles_.push_back(2);
    mesh->triangles_.push_back(3);

    mesh->triangles_.push_back(1);
    mesh->triangles_.push_back(3);
    mesh->triangles_.push_back(7);
    mesh->triangles_.push_back(1);
    mesh->triangles_.push_back(7);
    mesh->triangles_.push_back(5);

    mesh->triangles_.push_back(4);
    mesh->triangles_.push_back(5);
    mesh->triangles_.push_back(6);
    mesh->triangles_.push_back(5);
    mesh->triangles_.push_back(7);
    mesh->triangles_.push_back(6);

    mesh->triangles_.push_back(0);
    mesh->triangles_.push_back(6);
    mesh->triangles_.push_back(2);
    mesh->triangles_.push_back(0);
    mesh->triangles_.push_back(4);
    mesh->triangles_.push_back(6);

    mesh->triangles_.push_back(0);
    mesh->triangles_.push_back(1);
    mesh->triangles_.push_back(5);
    mesh->triangles_.push_back(0);
    mesh->triangles_.push_back(5);
    mesh->triangles_.push_back(4);

    mesh->triangles_.push_back(2);
    mesh->triangles_.push_back(7);
    mesh->triangles_.push_back(3);
    mesh->triangles_.push_back(2);
    mesh->triangles_.push_back(6);
    mesh->triangles_.push_back(7);

    return std::shared_ptr < Mesh > (mesh);
}

// an array of size:6 with Xmin, Ymin, Zmin and Xmax, Ymax, Zmax values
float* Mesh::getBoundingBoxInfo() const {
    float* bounding_box_info_ = new float[6];

    float min_x = std::numeric_limits<float>::infinity();
    float max_x = -std::numeric_limits<float>::infinity();
    float min_y = std::numeric_limits<float>::infinity();
    float max_y = -std::numeric_limits<float>::infinity();
    float min_z = std::numeric_limits<float>::infinity();
    float max_z = -std::numeric_limits<float>::infinity();

    for (auto it = vertices_.begin(); it != vertices_.end(); ++it) {
        if (it->x < min_x) {
            min_x = it->x;
        }
        if (it->x > max_x) {
            max_x = it->x;
        }
        if (it->y < min_y) {
            min_y = it->y;
        }
        if (it->y > max_y) {
            max_y = it->y;
        }
        if (it->z < min_z) {
            min_z = it->z;
        }
        if (it->z > max_z) {
            max_z = it->z;
        }
    }

    bounding_box_info_[0] = min_x;
    bounding_box_info_[1] = min_y;
    bounding_box_info_[2] = min_z;

    bounding_box_info_[3] = max_x;
    bounding_box_info_[4] = max_y;
    bounding_box_info_[5] = max_z;

    return bounding_box_info_;
}

// generate vertex array object
void Mesh::generateVAO() {
#if _GVRF_USE_GLES3_
    GLuint tmpID;

    if (vaoID_)
    {
        // already initialized
        return;
    }

    if (vertices_.size() == 0 && normals_.size() == 0 && tex_coords_.size()==0)
    {
        std::string error = "no vertex data yet, shouldn't call here. ";
        throw error;
        return;
    }

    if (vertexLoc_ == -1 && normalLoc_== -1 && texCoordLoc_== -1)
    {
        std::string error = "no attrib loc setup yet, please compile shader and set attribLoc first. ";
        throw error;
        return;
    }

    glGenVertexArrays(1, &vaoID_);
    glBindVertexArray(vaoID_);

    glGenBuffers(1, &triangle_vboID_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangle_vboID_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(unsigned short)*triangles_.size(), &triangles_[0], GL_STATIC_DRAW);

    if (vertices_.size())
    {
        glGenBuffers(1, &vert_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, vert_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec3)*vertices_.size(), &vertices_[0], GL_STATIC_DRAW);
        glEnableVertexAttribArray(getVertexLoc());
        glVertexAttribPointer(getVertexLoc(), 3, GL_FLOAT, 0, 0, 0);
    }

    if (normals_.size())
    {
        glGenBuffers(1, &norm_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, norm_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec3)*normals_.size(), &normals_[0], GL_STATIC_DRAW);
        glEnableVertexAttribArray(getNormalLoc());
        glVertexAttribPointer(getNormalLoc(), 3, GL_FLOAT, 0, 0, 0);
    }

    if (tex_coords_.size())
    {
        glGenBuffers(1, &tex_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, tex_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec2)*tex_coords_.size(), &tex_coords_[0], GL_STATIC_DRAW);
        glEnableVertexAttribArray(getTexCoordLoc());
        glVertexAttribPointer(getTexCoordLoc(), 2, GL_FLOAT, 0, 0, 0);
    }

    for(auto it = attribute_float_keys_.begin(); it != attribute_float_keys_.end(); ++it)
    {
        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ARRAY_BUFFER,
                sizeof(GLfloat)*getFloatVector(it->second).size(),
                getFloatVector(it->second).data(),
                GL_STATIC_DRAW);
        glEnableVertexAttribArray(it->first);
        glVertexAttribPointer(it->first, 1, GL_FLOAT, 0, 0, 0);
    }

    for(auto it = attribute_vec2_keys_.begin(); it != attribute_vec2_keys_.end(); ++it)
    {
        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ARRAY_BUFFER,
                sizeof(glm::vec2)*getVec2Vector(it->second).size(),
                getVec2Vector(it->second).data(),
                GL_STATIC_DRAW);
        glEnableVertexAttribArray(it->first);
        glVertexAttribPointer(it->first, 2, GL_FLOAT, 0, 0, 0);
    }

    for(auto it = attribute_vec3_keys_.begin(); it != attribute_vec3_keys_.end(); ++it)
    {
        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ARRAY_BUFFER,
                sizeof(glm::vec3)*getVec3Vector(it->second).size(),
                getVec3Vector(it->second).data(),
                GL_STATIC_DRAW);
        glEnableVertexAttribArray(it->first);
        glVertexAttribPointer(it->first, 3, GL_FLOAT, 0, 0, 0);
    }

    for(auto it = attribute_vec4_keys_.begin(); it != attribute_vec4_keys_.end(); ++it)
    {
        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ARRAY_BUFFER,
                sizeof(glm::vec4)*getVec4Vector(it->second).size(),
                getVec4Vector(it->second).data(),
                GL_STATIC_DRAW);
        glEnableVertexAttribArray(it->first);
        glVertexAttribPointer(it->first, 4, GL_FLOAT, 0, 0, 0);
    }

    // done generation
    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
#endif
}

}
