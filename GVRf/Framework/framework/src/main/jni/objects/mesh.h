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
/**
 * A mesh describes a geometric object that can be rendered.
 *
 * The mesh has a vertex buffer which contains the unique
 * vertices. Typically each vertex will have a  positions.
 * It could have normals, colors and texture coordinates too.
 * The index buffer has the topology of the mesh.
 * It designates which vertices comprise each polygon.
 * If there is no index buffer, each triangle is assumed
 * to be composed of 3 consecutive vertices.
 * Currently, GearVRF only supports triangle meshes.
 *
 * @see VertexBuffer
 * @see IndexBuffer
 */
class Mesh: public HybridObject {
public:
    explicit Mesh(const char* descriptor);
    explicit Mesh(VertexBuffer& vbuf);
    virtual ~Mesh() {}

    /**
     * Get the vertex buffer containing the vertices for this mesh
     * @return vertex buffer
     */
    VertexBuffer* getVertexBuffer() const { return mVertices; }

    /**
     * Get the index buffer containing the indices for this mesh
     * @return index buffer or null if no indices
     */
    IndexBuffer* getIndexBuffer() const { return mIndices; }

    /**
     * Set the vertex buffer containing the vertices for the mesh
     * @param vbuf vertex buffer to use, may not be null
     */
    void setVertexBuffer(VertexBuffer* vbuf) { mVertices = vbuf; }

    /**
     * Set the index buffer containing the indices for the mesh
     * @param ibuf index buffer or null if no indices
     */
    void setIndexBuffer(IndexBuffer* ibuf) { mIndices = ibuf; }

    /**
     * Copy the vertex positions from the input array to this mesh
     * (the "a_position" vertex attribute).
     * @param vertices  Floating point vertex data
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getVertexCount()
     * @return true if vertices successfully copied, false on error
     */
    bool setVertices(const float* vertices, int nelems);

    /**
     * Copy the vertex positions from the this mesh to the input array
     * (the "a_position" vertex attribute).
     * @param vertices  Floating point array to get vertices.
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getVertexCount()
     * @return true if vertices successfully copied, false on error
     */
    bool getVertices(float* vertices, int nelems);

    /**
     * Copy the vertex normals from the input array to this mesh
     * (the "a_normal" vertex attribute).
     * @param normals   Floating point normal data
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getVertexCount()
     * @return true if normals successfully copied, false on error
     */
    bool setNormals(const float* normals, int nelems);

    /**
     * Copy the vertex normals from the this mesh to the input array
     * (the "a_normal" vertex attribute).
     * @param normals   Floating point array to get normals.
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getVertexCount()
     * @return true if normals successfully copied, false on error
     */
    bool getNormals(float* normals, int nelems);

    /**
     * Copy the triangle indices from the input array to this mesh
     * @param indices   Integer index data
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getIndexCount()
     * @return true if indices successfully copied, false on error
     */
    bool setIndices(const unsigned int* indices, int nindices);

    /**
     * Copy the triangle indices from the input array to this mesh
     * @param indices   Short index data
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getIndexCount()
     * @return true if indices successfully copied, false on error
     */
    bool setTriangles(const unsigned short* indices, int nindices);

    /**
     * Copy the triangle indices from this mesh to the input array
     * @param indices   Short array to get indices
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getIndexCount()
     * @return true if indices successfully copied, false on error
     */
    bool getIndices(unsigned short* indices, int nindices);

    /**
     * Copy the triangle indices from this mesh to the input array
     * @param indices   Integer array to get indices
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getIndexCount()
     * @return true if indices successfully copied, false on error
     */
    bool getLongIndices(unsigned int* indices, int nindices);

    /**
     * Copy data associated with the given vertex attribute from the input array
     * to this mesh.
     * @param vertices  Floating point array with data..
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getVertexCount()
     * @return true if data successfully copied, false on error
     */
    bool setFloatVec(const char* attrName, const float* src, int nelems);

    /**
     * Copy data associated with the given vertex attribute from the input array
     * to this mesh.
     * @param vertices  Integer array with data..
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getVertexCount()
     * @return true if data successfully copied, false on error
     */
    bool setIntVec(const char* attrName, const int* src, int nelems);

    /**
     * Copy data associated with the given vertex attribute from the this mesh to the input array
     * @param vertices  Floating point array to get vertices.
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getVertexCount()
     * @return true if data successfully copied, false on error
     */
    bool getFloatVec(const char* attrName, float* dest, int nelems);

    /**
     * Copy data associated with the given vertex attribute from the this mesh to the input array
     * @param vertices  Integer point array to get vertices.
     * @param nelems    Number of entries in the input array.
     *                  This number must match the vertex
     *                  buffer size from getVertexCount()
     * @return true if data successfully copied, false on error
     */
    bool getIntVec(const char* attrName, int* dest, int nelems);

    /**
     * Call the specified function for each index in the index buffer.
     * @param func function to call
     */
    void forAllIndices(std::function<void(int iter, int index)> func);

    /**
     * Call a function for each vertex in the vertex buffer
     * associated with the specified attribute.
     * @param attrName  vertex attribute to fetch
     * @param func      function to call
     */
    void forAllVertices(const char* attrName, std::function<void(int iter, const float* vertex)> func) const;
    /**
     * Call a function for each triangle in the mesh.
     * @param func  function to call.
     */
    void forAllTriangles(std::function<void(int iter, const float* V1, const float* V2, const float* V3)> func) const;

    /**
     * Get the mesh bounds transformed by the given matrix.
     * @param matrix    4x4 matrix to transform the mesh with.
     * @param bbox      where to put the transformed bounds
     */
    void getTransformedBoundingBoxInfo(const glm::mat4& matrix, float* bbox); //Get Bounding box info transformed by matrix

    /**
     * Get the mesh bounding volume
     * @return mesh bounds in the local coordinate system of the mesh
     */
    const BoundingVolume& getBoundingVolume();

    /**
     * Get the number of bytes per index.
     * @return 2 for short indices, 4 for integer, 0 for no indices
     */
    int getIndexSize() const
    {
        return mIndices ? mIndices->getIndexSize() : 0;
    }

    /**
     * Get the number of indices in the index buffer
     * @return index count or 0 if no indices
     */
    int getIndexCount() const
    {
        return mIndices ? mIndices->getIndexCount() : 0;
    }

    /**
     * Get the number of vertices in the vertex buffer.
     * @return vertex count
     */
    int getVertexCount() const
    {
        return mVertices->getVertexCount();
    }

    /**
     * Determine if mesh has bones or not
     * @return true if mesh is skinned, false if not
     */
    bool hasBones() const
    {
        return vertexBoneData_.getNumBones();
    }

    /**
     * Set the bone positions and orientations for the mesh
     * @param bones array of bone matrices
     */
    void setBones(std::vector<Bone*>&& bones)
    {
        vertexBoneData_.setBones(std::move(bones));
    }

    /**
     * Get the vertex bone data
     * @return vertex bone data information
     */
    VertexBoneData& getVertexBoneData()
    {
        return vertexBoneData_;
    }

    /**
     * Determine if mesh vertices or indices have changed
     * @return true if dirty, else false
     */
    bool isDirty() const { return mVertices->isDirty(); }

private:
    Mesh(const Mesh& mesh) = delete;
    Mesh(Mesh&& mesh) = delete;
    Mesh& operator=(const Mesh& mesh) = delete;

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