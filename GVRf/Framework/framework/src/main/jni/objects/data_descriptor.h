
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


#ifndef DATA_DESCRIPTOR_H_
#define DATA_DESCRIPTOR_H_

#include <vector>
#include <functional>
#include <string>

namespace gvr {

/**
 * Data descriptor which defines the layout for uniform blocks
 * and vertex arrays.
 *
 * @see UniformBlock
 */
    class DataDescriptor
    {
    public:
        /*
         * Information kept for each uniform in the block.
         */
        struct DataEntry
        {
            char Index;                 // 0-based index in descriptor order
            char Count;                 // number of elements
            short Offset;               // offset in bytes from the top of the uniform block
            short Size;                 // total byte size of uniform entry
            unsigned int IsSet : 1;     // true if the entry has been set, else false
            unsigned int IsInt : 1;     // true if the entry represents an integer, false for float
            unsigned int IsMatrix : 1;  // true if the entry represents a matrix
            unsigned int NotUsed : 1;   // true if the shader does not use this entry
            char NameLength;            // length of the name
            char Name[64];              // name of the entry
            std::string Type;           // type of the entry
        };

    public:
        explicit DataDescriptor(const char* descriptor);
        virtual ~DataDescriptor() { }

        /**
         * Determine if a named uniform exists in this block.
         * This function will return false for names which are
         * in the descriptor but have not been given a value yet.
         *
         * @param name name of uniform to look for
         * @returns true if uniform is in this block, false if not
         */
        bool isSet(const char* name) const
        {
            int i = findName(name);

            return (i >= 0) && mLayout[i].IsSet;
        }

        /*
         * Get the number of bytes occupied by the vertex or data area.
         * @return number of bytes
         */
        int getTotalSize() const
        {
            return mTotalSize;
        }

        /**
         *   Get the number of entries in the layout descriptor
         */
        int getNumEntries() const { return mLayout.size(); }

        /**
         * Get the layout descriptor.
         * The layout descriptor defines the name, type and size
         * of each uniform or vertex. This descriptor
         * should match the layout used by the shader it
         * is intended to work with.
         * {@code
         *  "float3 color, float opacity"
         *  "float factor float power int2 offset"
         * }
         * @return layout descriptor string
         * @see setDescriptor
         */
        const char* getDescriptor() const
        {
            return mDescriptor.c_str();
        }

        /**
         * Visits each entry in the descriptor and calls the given function
         * once for each named item.
         */
        void forEach(std::function< void(const char* name, const char* type, int size) > func);

        /**
         * Visits each entry in the descriptor and calls the given function
         * with the entry.
         */
        void forEachEntry(std::function< void(DataEntry&) > func);
        void forEachEntry(std::function< void(const DataEntry&) > func) const;

        /**
         * Look up the named uniform in the mLayout.
         * This function fails if the uniform found does not
         * have the same byte size as the input bytesize.
         * @param name name of uniform to find.
         * @param dataptr pointer to where to store data pointer
         * @return pointer to Uniform structure describing the uniform or NULL on failure
         */
        const DataEntry* find(const char* name) const;
        DataEntry* find(const char* name);

        /*
         * Get the number of bytes occupied by the named entry.
         * For vertex arrays, it is the number of bytes occupied
         * by that attribute in a single vertex.
         * @param name string name of uniform whose size you want
         */
        int getByteSize(const char* name) const;

        /*
         * Get the shader type for this descriptor type.
         * @param dtype type from a vertex or uniform descriptor
         */
        std::string getShaderType(const char* dtype) const;

        /*
         * Determine if data has changed since last render.
         * @returns true if data has been updated, else false.
         */
        bool isDirty() const { return mIsDirty; }
        virtual void markDirty() { mIsDirty = true; }
        virtual std::string makeShaderType(const char* type, int byteSize);

        /**
         * Calculate the byte size of the given type.
         */
        static short calcSize(const char* type);

    protected:
        void removePunctuations(const char*);
        /**
         * Parse the descriptor string to create the map
         * which contains the name, offset and size of all uniforms.
         */
        void parseDescriptor();

        const char* addName(const char* name, int len, DataEntry& entry);
        int findName(const char* name) const;

        mutable bool mIsDirty;          // true if data in block has changed since last render
        std::string mDescriptor;        // descriptor with name, type and size of uniforms
        int         mTotalSize;         // number of bytes in data block or vertex
        std::vector<DataEntry> mLayout; // entries describing layout
    };

}
#endif
