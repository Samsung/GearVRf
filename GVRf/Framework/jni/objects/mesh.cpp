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
#include "../gl/gl_program.h"

namespace gvr {
Mesh* Mesh::createBoundingBox() {

    Mesh* mesh = new Mesh();

    getBoundingVolume(); // Make sure bounding_volume is valid

    glm::vec3 min_corner = bounding_volume.min_corner();
    glm::vec3 max_corner = bounding_volume.max_corner();

    float min_x = min_corner[0];
    float min_y = min_corner[1];
    float min_z = min_corner[2];
    float max_x = max_corner[0];
    float max_y = max_corner[1];
    float max_z = max_corner[2];

    mesh->vertices_.push_back(glm::vec3(min_x, min_y, min_z));
    mesh->vertices_.push_back(glm::vec3(max_x, min_y, min_z));
    mesh->vertices_.push_back(glm::vec3(min_x, max_y, min_z));
    mesh->vertices_.push_back(glm::vec3(max_x, max_y, min_z));
    mesh->vertices_.push_back(glm::vec3(min_x, min_y, max_z));
    mesh->vertices_.push_back(glm::vec3(max_x, min_y, max_z));
    mesh->vertices_.push_back(glm::vec3(min_x, max_y, max_z));
    mesh->vertices_.push_back(glm::vec3(max_x, max_y, max_z));

    mesh->indices_.push_back(0);
    mesh->indices_.push_back(2);
    mesh->indices_.push_back(1);
    mesh->indices_.push_back(1);
    mesh->indices_.push_back(2);
    mesh->indices_.push_back(3);

    mesh->indices_.push_back(1);
    mesh->indices_.push_back(3);
    mesh->indices_.push_back(7);
    mesh->indices_.push_back(1);
    mesh->indices_.push_back(7);
    mesh->indices_.push_back(5);

    mesh->indices_.push_back(4);
    mesh->indices_.push_back(5);
    mesh->indices_.push_back(6);
    mesh->indices_.push_back(5);
    mesh->indices_.push_back(7);
    mesh->indices_.push_back(6);

    mesh->indices_.push_back(0);
    mesh->indices_.push_back(6);
    mesh->indices_.push_back(2);
    mesh->indices_.push_back(0);
    mesh->indices_.push_back(4);
    mesh->indices_.push_back(6);

    mesh->indices_.push_back(0);
    mesh->indices_.push_back(1);
    mesh->indices_.push_back(5);
    mesh->indices_.push_back(0);
    mesh->indices_.push_back(5);
    mesh->indices_.push_back(4);

    mesh->indices_.push_back(2);
    mesh->indices_.push_back(7);
    mesh->indices_.push_back(3);
    mesh->indices_.push_back(2);
    mesh->indices_.push_back(6);
    mesh->indices_.push_back(7);

    return mesh;
}

// an array of size:6 with Xmin, Ymin, Zmin and Xmax, Ymax, Zmax values
const BoundingVolume& Mesh::getBoundingVolume() {
    if (have_bounding_volume_) {
        return bounding_volume;
    }

    for (auto it = vertices_.begin(); it != vertices_.end(); ++it) {
        bounding_volume.expand(*it);
    }

    have_bounding_volume_ = true;
    return bounding_volume;
}

void Mesh::getTransformedBoundingBoxInfo(glm::mat4 *Mat,
        float *transformed_bounding_box) {

    if (have_bounding_volume_ == false) {
        getBoundingVolume();
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

    glm::vec3 min_corner = bounding_volume.min_corner();
    glm::vec3 max_corner = bounding_volume.max_corner();

    for (int i = 0; i < 3; i++) {
        //x coord
        a = M[i].x * min_corner.x;
        b = M[i].x * max_corner.x;
        if (a < b) {
            transformed_bounding_box[0] += a;
            transformed_bounding_box[3] += b;
        } else {
            transformed_bounding_box[0] += b;
            transformed_bounding_box[3] += a;
        }

        //y coord
        a = M[i].y * min_corner.y;
        b = M[i].y * max_corner.y;
        if (a < b) {
            transformed_bounding_box[1] += a;
            transformed_bounding_box[4] += b;
        } else {
            transformed_bounding_box[1] += b;
            transformed_bounding_box[4] += a;
        }

        //z coord
        a = M[i].z * min_corner.z;
        b = M[i].z * max_corner.z;
        if (a < b) {
            transformed_bounding_box[2] += a;
            transformed_bounding_box[5] += b;
        } else {
            transformed_bounding_box[2] += b;
            transformed_bounding_box[5] += a;
        }
    }
}

// generate vertex array object
void Mesh::generateVAO() {
    GLuint tmpID;

    if (!vao_dirty_) {
         return;
    }
    obtainDeleter();
    deleteVaos();

    if (vertices_.size() == 0 && normals_.size() == 0
            && tex_coords_.size() == 0) {
        std::string error = "no vertex data yet, shouldn't call here. ";
        throw error;
        return;
    }

    glGenVertexArrays(1, &vaoID_);
    glBindVertexArray(vaoID_);

    glGenBuffers(1, &triangle_vboID_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangle_vboID_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,
            sizeof(unsigned short) * indices_.size(), &indices_[0],
            GL_STATIC_DRAW);
    numTriangles_ = indices_.size() / 3;

    if (vertices_.size()) {
        glGenBuffers(1, &vert_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, vert_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec3) * vertices_.size(),
                &vertices_[0], GL_STATIC_DRAW);
        GLuint vertexLoc = GLProgram::POSITION_ATTRIBUTE_LOCATION;
        glEnableVertexAttribArray(vertexLoc);
        glVertexAttribPointer(vertexLoc, 3, GL_FLOAT, 0, 0, 0);
    }

    if (normals_.size()) {
        glGenBuffers(1, &norm_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, norm_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec3) * normals_.size(),
                &normals_[0], GL_STATIC_DRAW);
        GLuint normalLoc = GLProgram::NORMAL_ATTRIBUTE_LOCATION;
        glEnableVertexAttribArray(normalLoc);
        glVertexAttribPointer(normalLoc, 3, GL_FLOAT, 0, 0, 0);
    }

    if (tex_coords_.size()) {
        glGenBuffers(1, &tex_vboID_);
        glBindBuffer(GL_ARRAY_BUFFER, tex_vboID_);
        glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec2) * tex_coords_.size(),
                &tex_coords_[0], GL_STATIC_DRAW);
        GLuint texCoordLoc = GLProgram::TEXCOORD_ATTRIBUT_LOCATION;
        glEnableVertexAttribArray(texCoordLoc);
        glVertexAttribPointer(texCoordLoc, 2, GL_FLOAT, 0, 0, 0);
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

    // done generation
    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    vao_dirty_ = false;
}

void Mesh::generateBoneArrayBuffers() {
    if (!bone_data_dirty_) {
        return;
    }

    // delete
    if (boneVboID_ != GVR_INVALID) {
        deleter_->queueBuffer(boneVboID_);
        boneVboID_ = GVR_INVALID;
    }

    int nVertices = vertices().size();
    if (!vertexBoneData_.getNumBones() || !nVertices) {
        LOGV("no bones or vertices");
        return;
    }

    glBindVertexArray(vaoID_);

    // BoneID
    GLuint boneVboID;
    glGenBuffers(1, &boneVboID);
    glBindBuffer(GL_ARRAY_BUFFER, boneVboID);
    glBufferData(GL_ARRAY_BUFFER,
            sizeof(vertexBoneData_.boneData[0]) * vertexBoneData_.boneData.size(),
            &vertexBoneData_.boneData[0], GL_STATIC_DRAW);
    glEnableVertexAttribArray(getBoneIndicesLoc());
    glVertexAttribIPointer(getBoneIndicesLoc(), 4, GL_INT, sizeof(VertexBoneData::BoneData), (const GLvoid*) 0);

    // BoneWeight
    glEnableVertexAttribArray(getBoneWeightsLoc());
    glVertexAttribPointer(getBoneWeightsLoc(), 4, GL_FLOAT, GL_FALSE, sizeof(VertexBoneData::BoneData),
            (const GLvoid*) (sizeof(VertexBoneData::BoneData::ids)));

    boneVboID_ = boneVboID;

    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

}
