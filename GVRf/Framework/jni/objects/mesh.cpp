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
#include "glm/gtc/matrix_inverse.hpp"

namespace gvr {
Mesh* Mesh::getBoundingBox() {
    Mesh* mesh = new Mesh();

    getBoundingBoxInfo(); // Make sure bounding_box_info_ is valid

    float min_x = bounding_box_info_[0];
    float max_x = bounding_box_info_[3];
    float min_y = bounding_box_info_[1];
    float max_y = bounding_box_info_[4];
    float min_z = bounding_box_info_[2];
    float max_z = bounding_box_info_[5];

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

    return mesh;
}

// an array of size:6 with Xmin, Ymin, Zmin and Xmax, Ymax, Zmax values
const float* Mesh::getBoundingBoxInfo() {
    if (have_bounding_box_) {
        return bounding_box_info_;
    }

    float min_x = std::numeric_limits<float>::infinity();
    float max_x = -std::numeric_limits<float>::infinity();
    float min_y = std::numeric_limits<float>::infinity();
    float max_y = -std::numeric_limits<float>::infinity();
    float min_z = std::numeric_limits<float>::infinity();
    float max_z = -std::numeric_limits<float>::infinity();

    if (vertices_.size() == 0) {
        return NULL;
    }

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

    have_bounding_box_ = true;
    return bounding_box_info_;
}

void Mesh::getTransformedBoundingBoxInfo(glm::mat4 *Mat,
        float *transformed_bounding_box) {

    if (have_bounding_box_ == false) {
        getBoundingBoxInfo();
    }

    glm::mat4 M = *Mat;
    float a, b;

    //Inspired by Graphics Gems - TransBox.c
    //Transform the AABB to the correct position in world space
    //Generate a new AABB from the non axis aligned bounding box

    transformed_bounding_box[0] = M[3].x;
    transformed_bounding_box[3] = M[3].x;

    transformed_bounding_box[1] = M[3].y;
    transformed_bounding_box[4] = M[3].y;

    transformed_bounding_box[2] = M[3].z;
    transformed_bounding_box[5] = M[3].z;

    for (int i = 0; i < 3; i++) {
        //x coord
        a = M[i].x * bounding_box_info_[0];
        b = M[i].x * bounding_box_info_[3];
        if (a < b) {
            transformed_bounding_box[0] += a;
            transformed_bounding_box[3] += b;
        } else {
            transformed_bounding_box[0] += b;
            transformed_bounding_box[3] += a;
        }

        //y coord
        a = M[i].y * bounding_box_info_[1];
        b = M[i].y * bounding_box_info_[4];
        if (a < b) {
            transformed_bounding_box[1] += a;
            transformed_bounding_box[4] += b;
        } else {
            transformed_bounding_box[1] += b;
            transformed_bounding_box[4] += a;
        }

        //z coord
        a = M[i].z * bounding_box_info_[2];
        b = M[i].z * bounding_box_info_[5];
        if (a < b) {
            transformed_bounding_box[2] += a;
            transformed_bounding_box[5] += b;
        } else {
            transformed_bounding_box[2] += b;
            transformed_bounding_box[5] += a;
        }
    }
}

// This gives us a really coarse bounding sphere given the already calcuated bounding box.  This won't be a tight-fitting sphere because it is based on the bounding box.  We can revisit this later if we decide we need a tighter sphere.
const float *Mesh::getBoundingSphereInfo() {
    if (!have_bounding_box_) {
        getBoundingBoxInfo();
    }

    if (have_bounding_sphere_) {
        return bounding_sphere_info_;
    }

    // get the bounding box into nicely readable variables
    float min_x = bounding_box_info_[0];
    float max_x = bounding_box_info_[3];
    float min_y = bounding_box_info_[1];
    float max_y = bounding_box_info_[4];
    float min_z = bounding_box_info_[2];
    float max_z = bounding_box_info_[5];

    // find center
    float center_x = (min_x + max_x) / 2.0f;
    float center_y = (min_y + max_y) / 2.0f;
    float center_z = (min_z + max_z) / 2.0f;

    // find radius
    float x_squared = (min_x - center_x) * (min_x - center_x);
    float y_squared = (min_y - center_y) * (min_y - center_y);
    float z_squared = (min_z - center_z) * (min_z - center_z);
    float radius = sqrtf(x_squared + y_squared + z_squared);

    // assign the sphere
    bounding_sphere_info_[0] = center_x;
    bounding_sphere_info_[1] = center_y;
    bounding_sphere_info_[2] = center_z;
    bounding_sphere_info_[3] = radius;

    have_bounding_sphere_ = true;

    return bounding_sphere_info_;
}

// generate vertex array object
void Mesh::generateVAO(Material::ShaderType key) {
#if _GVRF_USE_GLES3_
    GLuint tmpID;

    if (vao_dirty_) {
        deleteVaos();
    }

    if (vaoID_map_.find(key) != vaoID_map_.end()) {
        // already initialized
        return;
    }

    if (vertices_.size() == 0 && normals_.size() == 0
            && tex_coords_.size() == 0) {
        std::string error = "no vertex data yet, shouldn't call here. ";
        throw error;
        return;
    }

    if (vertexLoc_ == -1 && normalLoc_ == -1 && texCoordLoc_ == -1) {
        std::string error =
                "no attrib loc setup yet, please compile shader and set attribLoc first. ";
        throw error;
        return;
    }

    GLuint vaoID_ = 0;
    GLuint triangle_vboID_, vert_vboID_, norm_vboID_, tex_vboID_;

    glGenVertexArrays(1, &vaoID_);
    glBindVertexArray(vaoID_);

    glGenBuffers(1, &triangle_vboID_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangle_vboID_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,
            sizeof(unsigned short) * triangles_.size(), &triangles_[0],
            GL_STATIC_DRAW);
    numTriangles_ = triangles_.size() / 3;

    if (vertices_.size()) {
        glGenBuffers(1, &vert_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, vert_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec3) * vertices_.size(),
                &vertices_[0], GL_STATIC_DRAW);
        glEnableVertexAttribArray(getVertexLoc());
        glVertexAttribPointer(getVertexLoc(), 3, GL_FLOAT, 0, 0, 0);
    }

    if (normals_.size()) {
        glGenBuffers(1, &norm_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, norm_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec3) * normals_.size(),
                &normals_[0], GL_STATIC_DRAW);
        glEnableVertexAttribArray(getNormalLoc());
        glVertexAttribPointer(getNormalLoc(), 3, GL_FLOAT, 0, 0, 0);
    }

    if (tex_coords_.size()) {
        glGenBuffers(1, &tex_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, tex_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec2) * tex_coords_.size(),
                &tex_coords_[0], GL_STATIC_DRAW);
        glEnableVertexAttribArray(getTexCoordLoc());
        glVertexAttribPointer(getTexCoordLoc(), 2, GL_FLOAT, 0, 0, 0);
    }

    for (auto it = attribute_float_keys_.begin();
            it != attribute_float_keys_.end(); ++it) {
        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ARRAY_BUFFER,
                sizeof(GLfloat) * getFloatVector(it->second).size(),
                getFloatVector(it->second).data(), GL_STATIC_DRAW);
        glEnableVertexAttribArray(it->first);
        glVertexAttribPointer(it->first, 1, GL_FLOAT, 0, 0, 0);
    }

    for (auto it = attribute_vec2_keys_.begin();
            it != attribute_vec2_keys_.end(); ++it) {
        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ARRAY_BUFFER,
                sizeof(glm::vec2) * getVec2Vector(it->second).size(),
                getVec2Vector(it->second).data(), GL_STATIC_DRAW);
        glEnableVertexAttribArray(it->first);
        glVertexAttribPointer(it->first, 2, GL_FLOAT, 0, 0, 0);
    }

    for (auto it = attribute_vec3_keys_.begin();
            it != attribute_vec3_keys_.end(); ++it) {
        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ARRAY_BUFFER,
                sizeof(glm::vec3) * getVec3Vector(it->second).size(),
                getVec3Vector(it->second).data(), GL_STATIC_DRAW);
        glEnableVertexAttribArray(it->first);
        glVertexAttribPointer(it->first, 3, GL_FLOAT, 0, 0, 0);
    }

    for (auto it = attribute_vec4_keys_.begin();
            it != attribute_vec4_keys_.end(); ++it) {
        glGenBuffers(1, &tmpID);
        glBindBuffer(GL_ARRAY_BUFFER, tmpID);
        glBufferData(GL_ARRAY_BUFFER,
                sizeof(glm::vec4) * getVec4Vector(it->second).size(),
                getVec4Vector(it->second).data(), GL_STATIC_DRAW);
        glEnableVertexAttribArray(it->first);
        glVertexAttribPointer(it->first, 4, GL_FLOAT, 0, 0, 0);
    }

    vaoID_map_[key] = vaoID_;
    triangle_vboID_map_[key] = triangle_vboID_;
    vert_vboID_map_[key] = vert_vboID_;
    norm_vboID_map_[key] = norm_vboID_;
    tex_vboID_map_[key] = tex_vboID_;

    // done generation
    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    vao_dirty_ = false;
#endif
}

}
