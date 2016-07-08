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

std::vector<std::string> Mesh::dynamicAttribute_Names_ = {"a_bone_indices", "a_bone_weights"};

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
    bounding_volume.reset();
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

void Mesh::createAttributeMapping(int programId,
        int& totalStride, int& attrLen)
{
    totalStride = attrLen = 0;
    if (programId == -1)
    {
        // If program id has not been set, return.
        return;
    }
    GLint numActiveAtributes;
    glGetProgramiv(programId, GL_ACTIVE_ATTRIBUTES, &numActiveAtributes);
    GLchar attrName[512];
    GLAttributeMapping attrData;

    for (int i = 0; i < numActiveAtributes; i++)
    {
        GLsizei length;
        GLint size;
        GLenum type;
        glGetActiveAttrib(programId, i, 512, &length, &size, &type, attrName);
        if (std::find(dynamicAttribute_Names_.begin(), dynamicAttribute_Names_.end(), attrName) != dynamicAttribute_Names_.end())
        {
            // Skip dynamic attributes. Currently only bones are dynamic attributes which changes each frame.
            // They are handled seperately.
        }
        else
        {
            attrData.type = GL_FLOAT;
            int loc = glGetAttribLocation(programId, attrName);
            attrData.index = loc;
            attrData.data = NULL;
            attrData.offset = totalStride;
            bool addData = true;
            int len = 0;

            // Two things to note --
            // 1. The 3 builtin buffers are still seperate from the maps used for the other attributes
            // 2. The attribute index *has* to be 0, 1 and 2 for position, tex_coords and normal. The
            // index from querying via glGetActiveAttrib cannot be used. Needs analysis.
            if (strcmp(attrName, "a_position") == 0)
            {
                attrData.size = 3;
                len = vertices_.size();
                attrData.data = vertices_.data();
                //attrData.index = 0;
            }
            else if (strcmp(attrName, "a_normal") == 0)
            {
                attrData.size = 3;
                len = normals_.size();
                attrData.data = normals_.data();
                //attrData.index = 1;
            }
            else if (strcmp(attrName, "a_texcoord") == 0)
            {
                attrData.size = 2;
                len = tex_coords_.size();
                attrData.data = tex_coords_.data();
                //attrData.index = 2;
            }
            else if (strcmp(attrName, "a_tex_coord") == 0)
            {
                attrData.size = 2;
                len = tex_coords_.size();
                attrData.data = tex_coords_.data();
                //attrData.index = 2;
            }
            else
            {
                switch (type)
                {
                    case GL_FLOAT:
                        attrData.size = 1;
                        {
                            const std::vector<float>& curr = getFloatVector(attrName);
                            len = curr.size();
                            attrData.data = curr.data();
                        }
                        break;
                    case GL_FLOAT_VEC2:
                        attrData.size = 2;
                        {
                            const std::vector<glm::vec2>& curr = getVec2Vector(attrName);
                            len = curr.size();
                            attrData.data = curr.data();
                        }
                        break;
                    case GL_FLOAT_VEC3:
                        attrData.size = 3;
                        {
                            const std::vector<glm::vec3>& curr = getVec3Vector(attrName);
                            len = curr.size();
                            attrData.data = curr.data();
                        }
                        break;
                    case GL_FLOAT_VEC4:
                        attrData.size = 4;
                        {
                            const std::vector<glm::vec4>& curr = getVec4Vector(attrName);
                            len = curr.size();
                            attrData.data = curr.data();
                        }
                        break;
                    default:
                        addData = false;
                        LOGE("Looking up %s failed ", attrName);
                            break;
                }
            }
            if (addData)
            {
                totalStride += attrData.size;
                attrMapping.push_back(attrData);
                if (attrLen == 0)
                    attrLen = len;
                else
                {
                    if (len != attrLen)
                        LOGE(" $$$$*** Attib length does not match %d vs %d", len, attrLen);
                }
            }
        }
    }
}

void Mesh::createBuffer(std::vector<GLfloat>& buffer, int attrLength)
{
    for (int i = 0; i < attrLength; i++)
    {
        for (auto it = attrMapping.begin(); it != attrMapping.end(); ++it)
        {
            GLAttributeMapping currAttr = *it;
            const float* ptr = (float*) currAttr.data;
            for (int k = 0; k < currAttr.size; k++)
            {
                buffer.push_back(ptr[i * currAttr.size + k]);
            }
        }
    }
}

static void generateAndBindID(GLuint& id)
{

    glGenBuffers(1, &id);
    glBindBuffer(GL_ARRAY_BUFFER, id);
}

const GLuint Mesh::getVAOId(int programId) {
    if (programId == -1)
    {
        LOGI("!! %p Prog Id -- %d ", this, programId);
        return 0;
    }
    auto it = program_ids_.find(programId);
    if (it != program_ids_.end())
    {
        GLVaoVboId id = it->second;
        return id.vaoID;
    }
    vao_dirty_ = true;
    generateVAO(programId);
    it = program_ids_.find(programId);
    if (it != program_ids_.end())
    {
        GLVaoVboId id = it->second;
        return id.vaoID;
    }
    LOGI("!! %p Error in creating VAO  for Prog Id -- %d", this, programId);
    return 0;
}

// generate vertex array object
void Mesh::generateVAO(int programId) {
    GLuint tmpID;

    if (!vao_dirty_) {
         return;
    }
    obtainDeleter();

    GLuint vaoID_;
    GLuint triangle_vboID_;

    GLuint static_vboID_;

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
    int maxDumpSize = vertices_.size();
    if (maxDumpSize > 5)
        maxDumpSize = 5;
    attrMapping.clear();
    int totalStride;
    int attrLength;
    createAttributeMapping(programId, totalStride, attrLength);

    std::vector<GLfloat> buffer;
    createBuffer(buffer, attrLength);
    generateAndBindID(static_vboID_);

    glBufferData(GL_ARRAY_BUFFER, sizeof(GLfloat) * buffer.size(),
            &buffer[0], GL_STATIC_DRAW);
    int localCnt = 0;
    for ( std::vector<GLAttributeMapping>::iterator it = attrMapping.begin(); it != attrMapping.end(); ++it)
    {
        GLAttributeMapping currData = *it;
        glVertexAttribPointer(currData.index, currData.size, currData.type, 0, totalStride * sizeof(GLfloat), (GLvoid*) (currData.offset * sizeof(GLfloat)));
        glEnableVertexAttribArray(currData.index);
    }


    // done generation
    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    GLVaoVboId id;
    id.vaoID = vaoID_;
    id.static_vboID = static_vboID_;
    id.triangle_vboID = triangle_vboID_;
    program_ids_[programId] = id;
    vao_dirty_ = false;
}

void Mesh::generateBoneArrayBuffers(GLuint programId) {
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

    auto it = program_ids_.find(programId);
    if (it == program_ids_.end())
    {
        LOGV("Invalid program Id for bones");
        return;
    }
    GLVaoVboId id = it->second;
    glBindVertexArray(id.vaoID);

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
