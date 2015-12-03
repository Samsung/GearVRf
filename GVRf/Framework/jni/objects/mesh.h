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

#ifndef MESH_H_
#define MESH_H_

#include <algorithm>
#include <map>
#include <memory>
#include <vector>
#include <string>

#ifndef GL_ES_VERSION_3_0
#include "GLES3/gl3.h"
#endif

#include "glm/glm.hpp"
#include "gl/gl_buffer.h"
#include "gl/gl_program.h"

#include "objects/components/bone.h"
#include "objects/hybrid_object.h"
#include "objects/material.h"
#include "objects/bounding_volume.h"
#include "objects/vertex_bone_data.h"

#include "engine/memory/gl_delete.h"

namespace gvr {
class Mesh: public HybridObject {
public:
    Mesh() :
            vertices_(), normals_(), tex_coords_(), indices_(), float_vectors_(), vec2_vectors_(), vec3_vectors_(), vec4_vectors_(),
                    have_bounding_volume_(false), vao_dirty_(true),
                    vaoID_(GVR_INVALID), triangle_vboID_(GVR_INVALID), vert_vboID_(GVR_INVALID),
                    norm_vboID_(GVR_INVALID), tex_vboID_(GVR_INVALID),
                    vertexBoneData_(this)
    {
    }

    ~Mesh() {
        cleanUp();
    }

    void cleanUp() {
        std::vector<glm::vec3> vertices;
        vertices.swap(vertices_);
        std::vector<glm::vec3> normals;
        normals.swap(normals_);
        std::vector<glm::vec2> tex_coords;
        tex_coords.swap(tex_coords_);
        std::vector<unsigned short> indices;
        indices.swap(indices_);

        deleteVaos();
    }

    void deleteVaos() {
        if (vaoID_ != GVR_INVALID)
            gl_delete.queueVertexArray(vaoID_);
        if (triangle_vboID_ != GVR_INVALID)
            gl_delete.queueBuffer(triangle_vboID_);
        if (vert_vboID_ != GVR_INVALID)
            gl_delete.queueBuffer(vert_vboID_);
        if (norm_vboID_ != GVR_INVALID)
            gl_delete.queueBuffer(norm_vboID_);
        if (tex_vboID_ != GVR_INVALID)
            gl_delete.queueBuffer(tex_vboID_);
        have_bounding_volume_ = false;

        vao_dirty_ = true;
        vaoID_ = triangle_vboID_ = vert_vboID_ = norm_vboID_ = tex_vboID_ = GVR_INVALID;

        for (auto iterator = boneIndicesVboID_.begin();
                iterator != boneIndicesVboID_.end(); iterator++) {
            gl_delete.queueBuffer(iterator->second);
        }
        boneIndicesVboID_.clear();

        for (auto iterator = boneWeightsVboID_.begin();
                iterator != boneWeightsVboID_.end(); iterator++) {
            gl_delete.queueBuffer(iterator->second);
        }
        boneWeightsVboID_.clear();

        shouldResetBoneBuffers = true;
    }

    const std::vector<glm::vec3>& vertices() const {
        return vertices_;
    }

    void set_vertices(const std::vector<glm::vec3>& vertices) {
        vertices_ = vertices;
        have_bounding_volume_ = false;
        getBoundingVolume(); // calculate bounding volume
        vao_dirty_ = true;
    }

    void set_vertices(std::vector<glm::vec3>&& vertices) {
        vertices_ = std::move(vertices);
        have_bounding_volume_ = false;
        getBoundingVolume(); // calculate bounding volume
        vao_dirty_ = true;
    }

    const std::vector<glm::vec3>& normals() const {
        return normals_;
    }

    void set_normals(const std::vector<glm::vec3>& normals) {
        normals_ = normals;
        vao_dirty_ = true;
    }

    void set_normals(std::vector<glm::vec3>&& normals) {
        normals_ = std::move(normals);
        vao_dirty_ = true;
    }

    const std::vector<glm::vec2>& tex_coords() const {
        return tex_coords_;
    }

    void set_tex_coords(const std::vector<glm::vec2>& tex_coords) {
        tex_coords_ = tex_coords;
        vao_dirty_ = true;
    }

    void set_tex_coords(std::vector<glm::vec2>&& tex_coords) {
        tex_coords_ = std::move(tex_coords);
        vao_dirty_ = true;
    }

    const std::vector<unsigned short>& triangles() const {
        return indices_;
    }

    void set_triangles(const std::vector<unsigned short>& triangles) {
        indices_ = triangles;
        vao_dirty_ = true;
    }

    void set_triangles(std::vector<unsigned short>&& triangles) {
        indices_ = std::move(triangles);
        vao_dirty_ = true;
    }

    const std::vector<unsigned short>& indices() const {
        return indices_;
    }

    void set_indices(const std::vector<unsigned short>& indices) {
        indices_ = indices;
        vao_dirty_ = true;
    }

    void set_indices(std::vector<unsigned short>&& indices) {
        indices_ = std::move(indices);
        vao_dirty_ = true;
    }

    const std::vector<float>& getFloatVector(std::string key) const {
        auto it = float_vectors_.find(key);
        if (it != float_vectors_.end()) {
            return it->second;
        } else {
            std::string error = "Mesh::getFloatVector() : " + key
                    + " not found";
            throw error;
        }
    }

    void setFloatVector(std::string key, const std::vector<float>& vector) {
        float_vectors_[key] = vector;
        vao_dirty_ = true;
    }

    const std::vector<glm::vec2>& getVec2Vector(std::string key) const {
        auto it = vec2_vectors_.find(key);
        if (it != vec2_vectors_.end()) {
            return it->second;
        } else {
            std::string error = "Mesh::getVec2Vector() : " + key + " not found";
            throw error;
        }
    }

    void setVec2Vector(std::string key, const std::vector<glm::vec2>& vector) {
        vec2_vectors_[key] = vector;
        vao_dirty_ = true;
    }

    const std::vector<glm::vec3>& getVec3Vector(std::string key) const {
        auto it = vec3_vectors_.find(key);
        if (it != vec3_vectors_.end()) {
            return it->second;
        } else {
            std::string error = "Mesh::getVec3Vector() : " + key + " not found";
            throw error;
        }
    }

    void setVec3Vector(std::string key, const std::vector<glm::vec3>& vector) {
        vec3_vectors_[key] = vector;
        vao_dirty_ = true;
    }

    const std::vector<glm::vec4>& getVec4Vector(std::string key) const {
        auto it = vec4_vectors_.find(key);
        if (it != vec4_vectors_.end()) {
            return it->second;
        } else {
            std::string error = "Mesh::getVec4Vector() : " + key + " not found";
            throw error;
        }
    }

    void setVec4Vector(std::string key, const std::vector<glm::vec4>& vector) {
        vec4_vectors_[key] = vector;
        vao_dirty_ = true;
    }

    const BoundingVolume& getBoundingVolume() const { return bounding_volume; }
    Mesh* getBoundingBox();
    void getTransformedBoundingBoxInfo(glm::mat4 *M,
            float *transformed_bounding_box); //Get Bounding box info transformed by matrix

    // /////////////////////////////////////////////////
    //  code for vertex attribute location

    void setBoneLoc(GLuint boneIndicesLoc, GLuint boneWeightsLoc, GLuint boneTransformLoc) {
        boneIndicesLoc_ = boneIndicesLoc;
        boneWeightsLoc_ = boneWeightsLoc;
        boneTransformLoc_ = boneTransformLoc;
    }

    GLuint getBoneIndicesLoc() {
        return boneIndicesLoc_;
    }

    GLuint getBoneWeightsLoc() {
        return boneWeightsLoc_;
    }

    GLuint getBoneTransformLoc() {
        return boneTransformLoc_;
    }

    void setVertexAttribLocF(GLuint location, std::string key) {
        attribute_float_keys_[location] = key;
        vao_dirty_ = true;
    }

    void setVertexAttribLocV2(GLuint location, std::string key) {
        attribute_vec2_keys_[location] = key;
        vao_dirty_ = true;
    }

    void setVertexAttribLocV3(GLuint location, std::string key) {
        attribute_vec3_keys_[location] = key;
        vao_dirty_ = true;
    }

    void setVertexAttribLocV4(GLuint location, std::string key) {
        attribute_vec4_keys_[location] = key;
        vao_dirty_ = true;
    }

    // generate VAO
    void generateVAO();

    const GLuint getVAOId(Material::ShaderType key) const {
    	return vaoID_;
    }

    GLuint getNumTriangles() {
        return numTriangles_;
    }

    const BoundingVolume& getBoundingVolume();

    bool hasBones() const {
        return vertexBoneData_.getNumBones();
    }

    void setBones(std::vector<Bone*>&& bones) {
        vertexBoneData_.setBones(std::move(bones));
        shouldResetBoneBuffers = true;
    }

    VertexBoneData &getVertexBoneData() {
        return vertexBoneData_;
    }

    void generateBoneArrayBuffers(Material::ShaderType key);

private:
    Mesh(const Mesh& mesh);
    Mesh(Mesh&& mesh);
    Mesh& operator=(const Mesh& mesh);

private:
    std::vector<glm::vec3> vertices_;
    std::vector<glm::vec3> normals_;
    std::vector<glm::vec2> tex_coords_;
    std::map<std::string, std::vector<float>> float_vectors_;
    std::map<std::string, std::vector<glm::vec2>> vec2_vectors_;
    std::map<std::string, std::vector<glm::vec3>> vec3_vectors_;
    std::map<std::string, std::vector<glm::vec4>> vec4_vectors_;
    std::vector<unsigned short> indices_;
    std::vector<unsigned short> triangles_;

    // add location slot map
    std::map<int, std::string> attribute_float_keys_;
    std::map<int, std::string> attribute_vec2_keys_;
    std::map<int, std::string> attribute_vec3_keys_;
    std::map<int, std::string> attribute_vec4_keys_;

    // add vertex array object and VBO

    GLuint vaoID_;
    GLuint triangle_vboID_;
    GLuint vert_vboID_;
    GLuint norm_vboID_;
    GLuint tex_vboID_;

    // triangle information
    GLuint numTriangles_;
    bool vao_dirty_;

    bool have_bounding_volume_;
    BoundingVolume bounding_volume;

    // Bone data for the shader
    VertexBoneData vertexBoneData_;
    GLuint boneTransformLoc_;
    GLuint boneIndicesLoc_;
    GLuint boneWeightsLoc_;

    std::map<Material::ShaderType, GLuint> boneIndicesVboID_;
    std::map<Material::ShaderType, GLuint> boneWeightsVboID_;

    bool shouldResetBoneBuffers;
};
}
#endif
