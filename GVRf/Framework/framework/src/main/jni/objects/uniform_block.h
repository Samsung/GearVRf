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


#ifndef UNIFORMBLOCK_H_
#define UNIFORMBLOCK_H_

#include<unordered_map>
#include "data_descriptor.h"
#include "glm/glm.hpp"
#include "util/gvr_log.h"
#include <map>
#include <vector>

#define TRANSFORM_UBO_INDEX 0
#define MATERIAL_UBO_INDEX  1
#define BONES_UBO_INDEX     2
#define SAMPLER_UBO_INDEX   3

namespace gvr
{
    class SceneObject;

    class Shader;

    class Renderer;

/**
 * Manages a Uniform Block containing data parameters to pass to
 * the vertex and fragment shaders.
 *
 * The UniformBlock may be updated by the application. If it has changed,
 * GearVRf resends the entire data block to the GPU. Each block has one or more
 * named entries that refer to floating point or integer vectors.
 * Each entry can be independently accessed by name. All of the entries are
 * packed into a single data block.
 *
 * A uniform block is a renderer-dependent class which is implemented
 * differently depending on which underlying renderer GearVRf is using.
 *
 * @see DataDescriptor
 * @see GLUniformBlock
 * @see VulkanUniformBlock
 */
    class UniformBlock : public DataDescriptor
    {
    public:
        UniformBlock(const char *descriptor, int binding_point, const char *blockName);
        UniformBlock(const char *descriptor, int binding_point, const char *blockName, int maxElems);

        /**
         * Gets the OpenGL binding point for this uniform block.
         * @return GL binding point or -1 if not set
         */
        int getBindingPoint() const
        {
            return mBindingPoint;
        }

        /**
         * Enables or disabled the use of a GPU uniform buffer.
          */
        void useGPUBuffer(bool flag)
        {
            mUseBuffer = flag;
        }

        /**
         * Determines if a GPU buffer is used for this uniform block
         * or if it uses immediate mode to update the GPU.
         * @return true if GPU buffer used, else false
         */
        bool usesGPUBuffer() const
        {
            return mUseBuffer;
        }

        /**
        * Get the name of the uniform block.
        * This name should be set by the caller to be the same
        * as the name used for the block in the shader.
        * @returns uniform block name or NULL if not set.
        * @see #setBlockName
        */
        const char *getBlockName() const
        {
            return mBlockName.c_str();
        }

        /**
         * Set the value of an integer uniform.
         * If the named uniform is not an "int" in the descriptor
         * this function will fail and log an error.
         * @param name name of uniform to set.
         * @param val integer value to set.
         * @returns true if successfully set, false on error.
         * @see getInt
         */
        virtual bool setInt(const char *, int val);

        /**
         * Set the value of a floating point uniform.
         * If the named uniform is not a "float" in the descriptor
         * this function will fail and log an error.
         * @param name name of uniform to set.
         * @param val float value to set.
         * @returns true if successfully set, false on error.
         * @see getFloat
         */
        virtual bool setFloat(const char *, float val);

        /**
         * Set the value of an integer vector uniform.
         * If the named uniform is not an int vector in the descriptor
         * of the proper size this function will fail and log an error.
         * @param name name of uniform to set.
         * @param val pointer to integer vector.
         * @param n number of integers in the vector.
         * @returns true if successfully set, false on error.
         * @see getIntVec
         */
        virtual bool setIntVec(const char *name, const int *val, int n)=0;

        /**
         * Set the value of a floating point vector uniform.
         * If the named uniform is not a float vector in the descriptor
         * of the proper size this function will fail and log an error.
         * @param name name of uniform to set.
         * @param val pointer to float vector.
         * @param n number of floats in the vector.
         * @returns true if successfully set, false on error.
         * @see getVec
         */
        virtual bool setFloatVec(const char *name, const float *val, int n)=0;

        /**
         * Set the value of a 2D vector uniform.
         * If the named uniform is not a "float2" in the descriptor
         * this function will fail and log an error.
         * @param name name of uniform to set.
         * @param val 2D vector value to set.
         * @returns true if successfully set, false on error.
         * @see setVec
         * @see getVec
         * @see getVec2
         */
        virtual bool setVec2(const char *name, const glm::vec2 &val);

        /**
         * Set the value of a 3D vector uniform.
         * If the named uniform is not a "float3" in the descriptor
         * this function will fail and log an error.
         * @param name name of uniform to set.
         * @param val 3D vector value to set.
         * @returns true if successfully set, false on error.
         * @see setVec
         * @see getVec
         * @see getVec3
         */
        virtual bool setVec3(const char *name, const glm::vec3 &val);

        /**
         * Set the value of a 4D vector uniform.
         * If the named uniform is not a "float4" in the descriptor
         * this function will fail and log an error.
         * @param name name of uniform to set.
         * @param val 4D vector value to set.
         * @returns true if successfully set, false on error.
         * @see getVec
         * @see setVec
         * @see getVec4
         */
        virtual bool setVec4(const char *name, const glm::vec4 &val);

        /**
         * Set the value of a 4x4 matrix uniform.
         * If the named uniform is not a "mat4" in the descriptor
         * this function will fail and log an error.
         * @param name name of uniform to set.
         * @param val 4x4 matrix value to set.
         * @see setMat4
         * @see setVec
         * @see getVec
         */
        virtual bool setMat4(const char *name, const glm::mat4 &val);

        /**
         * Get the value of a 2D vector uniform.
         * If the named uniform is not a 2D vector this function
         * will return null.
         * @param name name of uniform to get.
         * @returns pointer to 2D vector or NULL if uniform not found.
         * @see setVec2
         * @see setVec
         * @see getVec
         */
        virtual const glm::vec2 *getVec2(const char *name) const;

        /**
         * Get the value of a 3D vector uniform.
         * If the named uniform is not a 3D vector this function
         * will return null.
         * @param name name of uniform to get.
         * @returns pointer to 3D vector or NULL if uniform not found.
         * @see getVec
         * @see setVec3
         * @see setVec
         */
        virtual const glm::vec3 *getVec3(const char *name) const;

        /**
         * Get the value of a 4D vector uniform.
         * If the named uniform is not a 4D vector this function
         * will return null.
         * @param name name of uniform to get.
         * @returns pointer to 4D vector or NULL if uniform not found.
         * @see getVec
         * @see setVec4
         * @see setVec
         */
        virtual const glm::vec4 *getVec4(const char *name) const;

        /**
         * Get the value of a 4x4 matrix uniform.
         * If the named uniform is not a 4x4 matrix this function
         * will return false.
         * @param name name of uniform to get.
         * @returns true if matrix found, false if not.
         * @see getVec
         * @see setMat4
         * @see setVec
         */
        virtual bool getMat4(const char *, glm::mat4 &val) const;

        /**
         * Get the value of a floating po2int uniform.
         * If the named uniform is not a "float" in the descriptor
         * this function returns 0 and logs an error.
         * @param name name of uniform to get.
         * @param v where to store float value.
         * @returns true if value found, else false.
         * @see setVec
         * @see getVec
         * @see setFloat
         */
        virtual bool getFloat(const char *name, float &v) const;

        /**
         * Get the value of an integer uniform.
         * If the named uniform is not "inat" in the descriptor
         * this function returns 0 and logs an error.
         * @param name name of uniform to get.
         * @param v where to store integer value.
         * @returns true if value found, else false.
         * @see setVec
         * @see getVec
         * @see setInt
         */
        virtual bool getInt(const char *name, int &v) const;

        /**
         * Get the value of a float vector uniform.
         * If the named uniform is not a float vector
         * of the proper size this function will return null.
         * @param name name of uniform to get.
         * @param val pointer to float array to get value.
         * @param n number of floats in the array.
         * @return true if vector retrieved, false if not found or size is wrong.
         * @see setVec
         */
        virtual bool getFloatVec(const char *name, float *val, int n) const;

        /**
         * Get the value of an integer vector uniform.
         * If the named uniform is not an int vector
         * of the proper size this function will return null.
         * @param name name of uniform to get.
         * @param val pointer to float array to get value.
         * @param n number of ints in the array.
         * @return true if vector retrieved, false if not found or size is wrong.
         * @see setVec
         */
        virtual bool getIntVec(const char *name, int *val, int n) const;

        /**
         * Copy the data from the CPU into the GPU.
         * If useGPUBuffer is enabled, the data is copied into a uniform
         * buffer in the GPU. Otherwise immediate mode is used to
         * copy the data to the graphics driver.
         */
        virtual bool updateGPU(Renderer *) = 0;

        /**
         * Bind the uniform block to a shader
         */
        virtual bool bindBuffer(Shader *, Renderer *) = 0;

        virtual ~UniformBlock()
        {
            if ((mUniformData != NULL) && mOwnData)
            {
                delete[] mUniformData;
            }
            mUniformData = NULL;
        }

        /**
         * Returns a string with the names and offsets
         * of all the uniforms in the block.
         * @return string describing the uniform block.
         */
        std::string toString();

        /**
         * Get a pointer to the entire uniform data area.
         * @returns -> uniform block data if it exists, else NULL
         */
        const void *getData()
        {
            return mUniformData;
        }

        int getNumElems() const;
        int getMaxElems() const;
        int getElemSize() const;
        bool setNumElems(int numElems);

        virtual std::string makeShaderLayout();
        const char* getDataAt(int elemIndex);
        bool setAt(int elemIndex, UniformBlock& srcBlock);
        bool setRange(int elemIndex, const void* srcData, int numElems);

    protected:
        UniformBlock(const char *descriptor);

        /**
         * Constructs the data block containing the values
         * for all the uniform variables in the descriptor.
         */
        void makeData()
        {
            if (mUniformData == NULL)
            {
                mUniformData = new char[mTotalSize];
                mOwnData = true;
            }
        }

        /**
         * Get a pointer to the value for the named uniform.
         * @param name name of uniform to get.
         * @param bytesize number of bytes uniform occupies
         * @return pointer to start of uniform value or NULL if not found.
         */
        char* getData(const char *name, int &bytesize);

        const char* getData(const char *name, int &bytesize) const;

        int mBindingPoint;           // shader binding point
        unsigned int mOwnData : 1;   // true if this uniform block owns its data
        unsigned int mUseBuffer : 1; // true if this uniform block uses a GPU buffer
        std::string mBlockName;      // name of the block in the shader
        char *mUniformData;          // -> data block with uniform values
        int mElemSize;
        int mMaxElems;
        int mNumElems;
    };
}
#endif
