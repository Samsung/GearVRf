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

#include <map>
#include <memory>
#include <vector>
#include <string>
#include <set>
#include <unordered_set>
//#include <objects/components/render_data.h>

#include "gl/gl_headers.h"

#include "glm/glm.hpp"

#include "util/gvr_gl.h"

#include "objects/components/bone.h"
#include "objects/hybrid_object.h"
#include "objects/shader_data.h"
#include "objects/bounding_volume.h"
#include "objects/vertex_bone_data.h"
#include "vulkan/vulkan_headers.h"
namespace gvr {
struct GLAttributeMapping {
    std::string     data_type;
    std::string     name;
    GLuint          index;
    GLuint          size;
    GLenum          type;
    GLuint          offset;
    const void*     data;
};
class Mesh: public HybridObject {
public:
    Mesh(const std::string& descriptor) :
            vertexDescriptor_(descriptor), vertices_(), normals_(), indices_(), float_vectors_(), vec2_vectors_(), vec3_vectors_(), vec4_vectors_(),
            have_bounding_volume_(false), vao_dirty_(true), vkVertices_(new GVR_VK_Vertices()),
            boneVboID_(0), vertexBoneData_(this), bone_data_dirty_(true), totalStride_(0)
    {
        m_vertices.vi_bindings = NULL;
        m_vertices.vi_attrs = NULL;
    }

    ~Mesh() {
        if (m_vertices.vi_bindings) {
            delete m_vertices.vi_bindings;
        }
        if (m_vertices.vi_attrs) {
            delete m_vertices.vi_attrs;
        }
        cleanUp();
    }

    /**
     * Must be called on the rendering thread
     */
    void cleanUp() {
        std::vector<glm::vec3> vertices;
        vertices.swap(vertices_);
        std::vector<glm::vec3> normals;
        normals.swap(normals_);
        std::vector<unsigned short> indices;
        indices.swap(indices_);
        delete vkVertices_;
        deleteVaos();
    }

    //would be nice to remove this; one of the backends uses it for something unique.
    void deleteVaoForProgram(int programId)
    {
        auto it = program_ids_.find(programId);
        if (it != program_ids_.end())
        {
            GLVaoVboId ids = it->second;
            GL(glDeleteVertexArrays(1, &ids.vaoID));
            GL(glDeleteBuffers(1, &ids.static_vboID));
            GL(glDeleteBuffers(1, &ids.triangle_vboID));
            program_ids_.erase(it);
        }
    }

    /**
     * Must be called on the rendering thread
     */
    void deleteVaos() {
        for (auto it : program_ids_ )
        {
            GLVaoVboId ids = it.second;
            GL(glDeleteVertexArrays(1, &ids.vaoID));
            GL(glDeleteBuffers(1, &ids.static_vboID));
            GL(glDeleteBuffers(1, &ids.triangle_vboID));
        }
        program_ids_.clear();
        have_bounding_volume_ = false;
        vao_dirty_ = true;
        bone_data_dirty_ = true;
    }

    const std::vector<glm::vec3>& vertices() const {
        return vertices_;
    }

    void set_vertices(const std::vector<glm::vec3>& vertices) {
        vertices_ = vertices;
        have_bounding_volume_ = false;
        getBoundingVolume(); // calculate bounding volume
        vao_dirty_ = true;
        dirty(STD_ATTRIBS);
    }

    void set_vertices(std::vector<glm::vec3>&& vertices) {
        vertices_ = std::move(vertices);
        have_bounding_volume_ = false;
        getBoundingVolume(); // calculate bounding volume
        vao_dirty_ = true;
        dirty(STD_ATTRIBS);
    }

    const std::vector<glm::vec3>& normals() const {
        return normals_;
    }

    void set_normals(const std::vector<glm::vec3>& normals) {
        normals_ = normals;
        vao_dirty_ = true;
        dirty(STD_ATTRIBS);
    }

    void set_normals(std::vector<glm::vec3>&& normals) {
        normals_ = std::move(normals);
        vao_dirty_ = true;
        dirty(STD_ATTRIBS);
    }

    const std::vector<unsigned short>& triangles() const {
        return indices_;
    }

    void set_triangles(const std::vector<unsigned short>& triangles) {
        indices_ = triangles;
        vao_dirty_ = true;
        dirty(STD_ATTRIBS);
    }

    void set_triangles(std::vector<unsigned short>&& triangles) {
        indices_ = std::move(triangles);
        vao_dirty_ = true;
        dirty(STD_ATTRIBS);
    }

    const std::vector<unsigned short>& indices() const {
        return indices_;
    }

    void set_indices(const std::vector<unsigned short>& indices) {
        indices_ = indices;
        vao_dirty_ = true;
        dirty(STD_ATTRIBS);
    }

    void set_indices(std::vector<unsigned short>&& indices) {
        indices_ = std::move(indices);
        vao_dirty_ = true;
        dirty(STD_ATTRIBS);
    }

    bool hasAttribute(const std::string& key) const {
        size_t found = vertexDescriptor_.find(key);
        return (found != std::string::npos);
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
        dirty(CUSTOM_ATTRIBS);
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

    bool getVec(std::string key, std::vector<glm::vec2>** const ptr)  {
        auto it = vec2_vectors_.find(key);
        if (it != vec2_vectors_.end()) {
            *ptr = &(it->second);
            return true;
        } else {
            LOGE("Mesh::getVec2Vector() : %s not found", key.c_str());
            return false;
        }
    }

    void setVec2Vector(std::string key, const std::vector<glm::vec2>& vector) {
        vec2_vectors_[key] = vector;
        if(strstr((key.c_str()),"a_texcoord")) {
            dirty(STD_ATTRIBS);
        }
        else {
            dirty(CUSTOM_ATTRIBS);
        }
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
        dirty(CUSTOM_ATTRIBS);
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
        dirty(CUSTOM_ATTRIBS);
    }

    Mesh* createBoundingBox();
    void getTransformedBoundingBoxInfo(glm::mat4 *M,
            float *transformed_bounding_box); //Get Bounding box info transformed by matrix

    // /////////////////////////////////////////////////
    //  code for vertex attribute location

    void setBoneLoc(GLuint boneIndicesLoc, GLuint boneWeightsLoc) {

        boneIndicesLoc_ = boneIndicesLoc;
        boneWeightsLoc_ = boneWeightsLoc;
    }

    GLuint getBoneIndicesLoc() {
        return boneIndicesLoc_;
    }

    GLuint getBoneWeightsLoc() {
        return boneWeightsLoc_;
    }

    void setVertexAttribLocF(GLuint location, std::string key) {
        attribute_float_keys_[location] = key;
        vao_dirty_ = true;
        LOGD("SHADER: setVertexAttrib %s\n", key.c_str());
    }

    void setVertexAttribLocV2(GLuint location, std::string key) {
        attribute_vec2_keys_[location] = key;
        vao_dirty_ = true;
        LOGD("SHADER: setVertexAttrib %s\n", key.c_str());
    }

    void setVertexAttribLocV3(GLuint location, std::string key) {
        attribute_vec3_keys_[location] = key;
        vao_dirty_ = true;
        LOGD("SHADER: setVertexAttrib %s\n", key.c_str());
    }

    void setVertexAttribLocV4(GLuint location, std::string key) {
        attribute_vec4_keys_[location] = key;
        vao_dirty_ = true;
        LOGD("SHADER: setVertexAttrib %s\n", key.c_str());
    }

    const GLuint getVAOId(int programId);

    GLuint getNumTriangles() {
        return numTriangles_;
    }

    const BoundingVolume& getBoundingVolume();

    bool hasBones() const {
        return vertexBoneData_.getNumBones();
    }

    void setBones(std::vector<Bone*>&& bones) {
        vertexBoneData_.setBones(std::move(bones));
        bone_data_dirty_ = true;
    }

    VertexBoneData &getVertexBoneData() {
        return vertexBoneData_;
    }

    void generateBoneArrayBuffers(GLuint programId);

    void getAttribNames(std::set<std::string> &attrib_names);

     // generate VAO
     int generateVAO(int programId);
 //   void generateVAO(VkDevice &m_device, VulkanCore*);

    void add_dirty_flag(const std::shared_ptr<u_short>& dirty_flag);
    void dirty(DIRTY_BITS bit);

   GVR_VK_Vertices& getVkVertices(){
        return m_vertices;
    }
    GVR_VK_Indices& getVkIndices(){
        return m_indices;
    }
    const std::vector<GLAttributeMapping>& getBindings(){
        return attrMapping;
    }
private:
    Mesh(const Mesh& mesh);
    Mesh(Mesh&& mesh);
    Mesh& operator=(const Mesh& mesh);


private:
    GVR_VK_Indices m_indices;
    GVR_VK_Vertices m_vertices;

    GVR_VK_Vertices* vkVertices_;
    std::vector<glm::vec3> vertices_;
    std::vector<glm::vec3> normals_;

    std::map<std::string, std::vector<float>> float_vectors_;
    std::map<std::string, std::vector<glm::vec2>> vec2_vectors_;
    std::map<std::string, std::vector<glm::vec3>> vec3_vectors_;
    std::map<std::string, std::vector<glm::vec4>> vec4_vectors_;
    std::vector<unsigned short> indices_;

    // add location slot map
    std::map<int, std::string> attribute_float_keys_;
    std::map<int, std::string> attribute_vec2_keys_;
    std::map<int, std::string> attribute_vec3_keys_;
    std::map<int, std::string> attribute_vec4_keys_;

    // add vertex array object and VBO

    struct GLVaoVboId {
        GLuint vaoID;
        GLuint static_vboID;
        GLuint triangle_vboID;
    };

    std::map<GLuint, GLVaoVboId> program_ids_;


    std::vector<GLAttributeMapping> attrMapping;

    void createAttributeMapping(int programId, int& totalStride, int& attrLength);
    void createBuffer(std::vector<GLfloat>& buffer, int attrLength);

    // triangle information
    GLuint numTriangles_;
    int totalStride_;
    bool vao_dirty_;
    bool have_bounding_volume_;
    BoundingVolume bounding_volume;

    // Bone data for the shader
    VertexBoneData vertexBoneData_;
    GLuint boneIndicesLoc_;
    GLuint boneWeightsLoc_;

    GLuint boneVboID_;
    bool bone_data_dirty_;
    std::string vertexDescriptor_;
    static std::vector<std::string> dynamicAttribute_Names_;
    std::unordered_set<std::shared_ptr<u_short>> dirty_flags_;

 public:
     void getAttribData(const std::string& descriptor,std::vector<GLAttributeMapping>& bindings, int& total_size);
     void generateVKBuffers(const std::string&, VulkanRenderer* renderer);

};
}
#endif
