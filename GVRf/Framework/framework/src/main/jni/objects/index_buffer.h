#pragma once

#include <mutex>
#include <string>
#include <vector>
#include "hybrid_object.h"

namespace gvr {
    class Shader;
    class Renderer;

/**
 * Storage for mesh indices (either 16 or 32 bit)
 *
 * The format of the index data in the array maps directly to
 * what is required by the underlying renderer so that indices
 * may be quickly copied without reformatting.
 *
 * @see Mesh
 */
    class IndexBuffer : public HybridObject
    {
    public:
        explicit IndexBuffer(int bytesPerIndex, int indexCount);
        virtual ~IndexBuffer();

        /**
         * Retrieve pointer to raw index data
         */
        const unsigned int* getIndexData() const   { return reinterpret_cast<const unsigned int*>(mIndexData); }

        /**
         * Return the number of bytes in an index (either 2 or 4).
         */
        int getIndexSize() const   { return mIndexByteSize; }

        /**
         * Return the number of indices currently in this buffer.
         * This is established the first time indices are added
         * and cannot be changed subsequently.
         */
        int getIndexCount() const; //  { return mIndexCount; }

        /**
         * Return the number of bytes in the index data buffer.
         */
        int getDataSize() const     { return getIndexSize() * mIndexCount; }

        /**
         * Set all the values for short (16 bit) indices.
         *
         * @param src         pointer to short integer source data array.
         * @param srcSize     number of shorts in the vector.
         * @returns true if successfully set, false on error.
         * @see getShortVec
         */
        bool    setShortVec(const unsigned short* src, int srcSize);

        /**
         * Set all the values for long (32 bit) indices.
         *
         * @param src         pointer to integer source data array.
         * @param srcSize     number of ints in the vector.
         * @returns true if successfully set, false on error.
         * @see getIntVec
         */
        bool    setIntVec(const unsigned int* src, int srcSize);

        /**
         * Gets all the values for long (32 bit) indices.
         *
         * @param dest        pointer to integer source data array.
         * @param destSize    number of floats in the vector.
         * @return true if vector retrieved, false if not found or size is wrong.
         * @see setIntVec
         */
        bool    getIntVec(unsigned int* dest, int destSize) const;

        /**
        * Gets all the values for short (32 bit) indices.
        *
        * @param dest        pointer to integer source data array.
        * @param destSize    number of floats in the vector.
        * @return true if vector retrieved, false if not found or size is wrong.
        * @see setIntVec
        */
        bool    getShortVec(unsigned short* dest, int destSize) const;

        bool            isDirty() const { return mIsDirty; }
        virtual bool    bindBuffer(Shader*) = 0;
        virtual bool    updateGPU(Renderer*) = 0;
        void            dump() const;

    protected:
        bool            setIndexCount(int count);
        bool            setIndexSize(int v);

        mutable std::mutex mUpdateLock;
        mutable bool    mIsDirty;
        int     mIndexByteSize;     // index size in bytes (either 2 or 4)
        int     mIndexCount;        // current number of vertices
        char*   mIndexData;         // index data buffer
    };


} // end gvrf

