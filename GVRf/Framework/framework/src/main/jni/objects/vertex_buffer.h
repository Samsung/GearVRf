#pragma once

#include <string>
#include <vector>
#include <mutex>
#include "data_descriptor.h"
#include "hybrid_object.h"
#include "bounding_volume.h"

namespace gvr {
    class Shader;
    class Renderer;
    class IndexBuffer;

/**
 * Interleaved vertex storage
 *
 * A vertex is a user-defined set of 32-bit float or integer components
 * used by the vertex shader during rendering to compute the position
 * and lighting of meshes. The layout of a vertex buffer is defined with
 * a string and is the same for all vertices in the pool.
 * Typical vertex components include location (Vec3), normal (Vec3),
 * color (Color) and texture coordinates (Vec2).
 *
 * The format of the vertex data in the array maps directly to
 * what is required by the underlying renderer so that vertices
 * may be quickly copied without reformatting.
 *
 * @see Mesh
 */
    class VertexBuffer : public HybridObject, public DataDescriptor
    {
    public:

        explicit VertexBuffer(const char* layout_desc, int vertexCount);
        virtual ~VertexBuffer();

        /**
         * Retrieve pointer to raw vertex data for all vertex components.
         */
        const float*    getVertexData() const   { return reinterpret_cast<const float*>(mVertexData); }

        /**
         * Return the number of floats in a vertex.
         */
        int getVertexSize() const   { return getTotalSize() / sizeof(float); }

        /**
         * Return the number of vertices currently in this buffer.
         * This is established the first time any vertex attribute is
         * added and cannot be changed. All vertices must have every
         * attribute - there is a single vertex index table.
         */
        int getVertexCount() const  { return mVertexCount; }

        /**
         * Return the number of bytes in the vertex data buffer.
         */
        int getDataSize() const     { return getTotalSize() * mVertexCount; }

        /**
         * Set all the values for an float vertex attribute.
         * If the named entry is not an float vector in the descriptor
         * this function will fail and log an error. The source
         * data array may have more vertex components than required
         * to set the attribute. A source stride is provided to allow
         * you to copy an attribute from one vertex array to another.
         *
         * @param name        name of entry to set.
         * @param src         pointer to integer source data array.
         * @param srcSize     number of floats in the vector.
         * @param srcStride   number of floats to the next vertex.
         * @returns true if successfully set, false on error.
         * @see getIntVec
         */
        bool    setFloatVec(const char* attributeName, const float* src, int srcSize, int srcStride);

        /**
         * Gets all the values of a float vertex attribute.
         * If the named attribute is not a float vector in the descriptor
         * this function will return null.
         *
         * The destination array may have more vertex components than required
         * to accommodate the attribute. A destination stride is provided to allow
         * you to copy an attribute from one vertex array to another.
         *
         * @param name        name of entry to set.
         * @param dest        pointer to integer source data array.
         * @param destSize    number of floats in the vector.
         * @param destStride  number of floats to the next vertex.
         * @return true if vector retrieved, false if not found or size is wrong.
         * @see setVec
         */
        bool    getFloatVec(const char* attributeName, float* dest, int destSize, int destStride) const;

        /**
         * Set all the values for an integer vertex attribute.
         * If the named entry is not an int vector in the descriptor
         * this function will fail and log an error. The source
         * data array may have more vertex components than required
         * to set the attribute. A source stride is provided to allow
         * you to copy an attribute from one vertex array to another.
         *
         * @param name        name of entry to set.
         * @param src         pointer to integer source data array.
         * @param srcSize     number of integers in the vector.
         * @param srcStride   number of integers to the next vertex.
         * @returns true if successfully set, false on error.
         * @see getIntVec
         */
        bool            setIntVec(const char* attributeName, const int* src, int srcSize, int srcStride);

        /**
         * Gets all the values of an integer vertex attribute.
         * If the named attribute is not an integer vector in the descriptor
         * this function will return null.
         *
         * The destination array may have more vertex components than required
         * to accommodate the attribute. A destination stride is provided to allow
         * you to copy an attribute from one vertex array to another.
         *
         * @param name        name of entry to set.
         * @param dest        pointer to integer source data array.
         * @param destSize    number of floats in the vector.
         * @param destStride  number of floats to the next vertex.
         * @return true if vector retrieved, false if not found or size is wrong.
         * @see setVec
         */
        bool            getIntVec(const char* attributeName, int* data, int dataByteSize, int dataStride) const;

        bool            forAllVertices(const char* attrName, std::function<void (int iter, const float* vertex)> func) const;
        bool            forAllVertices(std::function<void (int iter, const float* vertex)> func) const;
        bool            getInfo(const char* attributeName, int& index, int& offset, int& size) const;
        void            getBoundingVolume(BoundingVolume& bv) const;
        virtual bool    updateGPU(Renderer*, IndexBuffer*, Shader*) = 0;
        virtual void    bindToShader(Shader* shader, IndexBuffer* ibuf) = 0;
        void            dump() const;
        void            dump(const char* attrName) const;

    protected:
        bool            setVertexCount(int vertexCount);
        const void*     getData(const char* attributeName, int& size) const;
        const void*     getData(int index, int& size) const;

        mutable std::mutex mLock;
        int             mVertexCount;       // current number of vertices
        char*           mVertexData;        // vertex data buffer
        int             mBoneFlags = 0;     // indicates which vertex attributes are bones
    };

} // end gvrf

