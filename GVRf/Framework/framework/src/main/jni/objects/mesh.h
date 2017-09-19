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
#include "glm/glm.hpp"

#include "util/gvr_gl.h"
#include "objects/components/bone.h"
#include "objects/hybrid_object.h"
#include "objects/shader_data.h"
#include "objects/bounding_volume.h"
#include "objects/vertex_bone_data.h"
#include "objects/vertex_buffer.h"
#include "objects/index_buffer.h"
#include "bounding_volume.h"

namespace gvr {

class Mesh: public HybridObject {
public:
    Mesh(const char* descriptor);
    Mesh(VertexBuffer& vbuf);

    VertexBuffer* getVertexBuffer() const { return mVertices; }
    IndexBuffer* getIndexBuffer() const { return mIndices; }
    void setVertexBuffer(VertexBuffer* vbuf) { mVertices = vbuf; }
    void setIndexBuffer(IndexBuffer* ibuf) { mIndices = ibuf; }
    bool setVertices(const float* vertices, int nelems);
    bool getVertices(float* vertices, int nelems);
    bool setNormals(const float* normals, int nelems);
    bool getNormals(float* normals, int nelems);
    bool setIndices(const unsigned int* indices, int nindices);
    bool setTriangles(const unsigned short* indices, int nindices);
    bool getIndices(unsigned short* indices, int nindices);
    bool getLongIndices(unsigned int* indices, int nindices);
    bool setFloatVec(const char* attrName, const float* src, int nelems);
    bool setIntVec(const char* attrName, const int* src, int nelems);
    bool getFloatVec(const char* attrName, float* dest, int nelems);
    bool getIntVec(const char* attrName, int* dest, int nelems);
    bool getAttributeInfo(const char* attributeName, int& index, int& offset, int& size) const;

    void forAllIndices(std::function<void(int iter, int index)> func);
    void forAllVertices(const char* attrName, std::function<void(int iter, const float* vertex)> func) const;
    void forAllTriangles(std::function<void(int iter, const float* V1, const float* V2, const float* V3)> func) const;
    Mesh* createBoundingBox();
    void getTransformedBoundingBoxInfo(glm::mat4 *M, float *transformed_bounding_box); //Get Bounding box info transformed by matrix

    int getIndexSize() const
    {
        return mIndices ? mIndices->getIndexSize() : 0;
    }

    int getIndexCount() const
    {
        return mIndices ? mIndices->getIndexCount() : 0;
    }

    int getVertexCount() const
    {
        return mVertices->getVertexCount();
    }

    const BoundingVolume& getBoundingVolume();

    bool hasBones() const
    {
        return vertexBoneData_.getNumBones();
    }

    void setBones(std::vector<Bone*>&& bones)
    {
        vertexBoneData_.setBones(std::move(bones));
    }

    VertexBoneData &getVertexBoneData()
    {
        return vertexBoneData_;
    }

    bool isDirty() const { return mVertices->isDirty(); }

private:
    Mesh(const Mesh& mesh);
    Mesh(Mesh&& mesh);
    Mesh& operator=(const Mesh& mesh);


protected:
    IndexBuffer* mIndices;
    VertexBuffer* mVertices;
    bool have_bounding_volume_;
    BoundingVolume bounding_volume;

    // Bone data for the shader
    VertexBoneData vertexBoneData_;
    std::unordered_set<std::shared_ptr<u_short>> dirty_flags_;
};
}
#endif