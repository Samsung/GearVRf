
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
#include "vulkan/vulkanCore.h"
#include "glm/glm.hpp"
#include "util/gvr_log.h"
#include <map>
#include <vector>
#include "vulkan/vulkanInfoWrapper.h"
#define TRANSFORM_UBO_INDEX 0
#define MATERIAL_UBO_INDEX  1
#define SAMPLER_UBO_INDEX   2
#define BONES_UBO_INDEX     3
namespace gvr {
class SceneObject;
//struct GVR_Uniform;
/**
 * Manages a Uniform Block containing data parameters to pass to
 * the vertex and fragment shaders.
 *
 * The UniformBlock may be updated by the application. If it has changed,
 * GearVRf resends the entire data block to the GPU. Each block has one or more
 * named entries that refer to floating point or integer vectors.
 * Each entry can be independently accessed by name. All of the entries are
 * packed into a single data block.
 */
class UniformBlock
{
protected:
    struct Uniform
    {
        int Offset;
        int Size;
        std::string Type;
        std::string Name;
    };

public:
    UniformBlock();
    UniformBlock(const std::string& descriptor);

    /**
     * Determine if a named uniform exists in this block.
     * @param name name of uniform to look for
     * @returns true if uniform is in this block, false if not
     */
    bool hasUniform(std::string name)
    {
        return UniformMap.find(name) != UniformMap.end();
    }

    /*
     * Get the number of bytes occupied by this uniform block.
     * @return byte size of uniform block
     */
    int getTotalSize() const
    {
        return TotalSize;
    }

    /**
     * Get the name of this uniform block.
     * This name is used to identify the block in the shader.
     * @return string with block name.
     * @see setBlockName
     */
    const std::string& getBlockName() const
    {
        return BlockName;
    }

    /**
     * Get the uniform descriptor.
     * The uniform descriptor defines the name, type and size
     * of each uniform in the block. This descriptor
     * should match the layout used by the shader this
     * block is intended to work with.
     * @return uniform descriptor string
     * @see setDescriptor
     */
    const std::string& getDescriptor() const
    {
        return Descriptor;
    }

    /**
     * Set the name of this uniform block.
     * This name is used to identify the block in the shader.
     * @param name string with name of uniform block
     * @see getBlockName
     */
    void setBlockName(const std::string& blockName)
    {
        if (!BlockName.empty())
        {
            LOGE("UniformBlock: ERROR: blocks cannot be renamed\n");
            return;
        }
        BlockName = blockName;
    }


    /**
     * Set the uniform descriptor.
     * The uniform descriptor defines the name, type and size
     * of each uniform in the block. Each entry has a type,
     * size and name. Entries are separated by spaces but
     * other delimiters (commas, semicolons) are permitted.
     * Sample strings:
     *  "float4 diffuseColor, float specularExponent"
     *  "int2 offset mat4 texMatrix"
     *
     * This descriptor should match the layout used by the shader this
     * block is intended to work with.
     * @param descriptor string with uniform descriptor.
     * @see getDescriptor
     */
    void setDescriptor(const std::string& descriptor)
    {
        if (!Descriptor.empty())
        {
            LOGE("UniformBlock: ERROR: descriptor cannot be changed once it is set\n");
            return;
        }

        Descriptor = descriptor;
        parseDescriptor();
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
    bool setInt(std::string name, int val);

    /**
     * Set the value of a floating point uniform.
     * If the named uniform is not a "float" in the descriptor
     * this function will fail and log an error.
     * @param name name of uniform to set.
     * @param val float value to set.
     * @returns true if successfully set, false on error.
     * @see getFloat
     */
    bool setFloat(std::string name, float val);

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
    bool setIntVec(std::string name, const int* val, int n);

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
    bool setVec(std::string name, const float* val, int n);

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
    bool setVec2(std::string name, const glm::vec2& val);

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
    bool setVec3(std::string name, const glm::vec3& val);

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
    bool setVec4(std::string name, const glm::vec4& val);

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
    bool setMat4(std::string name,  const float* val);

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
    const glm::vec2* getVec2(std::string name) const;

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
    const glm::vec3* getVec3(std::string name) const;

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
    const glm::vec4* getVec4(std::string name) const;

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
    bool getMat4(std::string name, glm::mat4& val) const;

    /**
     * Get the value of a floating po2int uniform.
     * If the named uniform is not a "float" in the descriptor
     * this function returns 0 and logs an error.
     * @param name name of uniform to get.
     * @returns floating value, 0 if not found.
     * @see setVec
     * @see getVec
     * @see setFloat
     */
    float getFloat(std::string name) const;

    /**
     * Get the value of an integer uniform.
     * If the named uniform is not "inat" in the descriptor
     * this function returns 0 and logs an error.
     * @param name name of uniform to get.
     * @returns integer value, 0 if not found.
     * @see setVec
     * @see getVec
     * @see setInt
     */
    int getInt(std::string name) const;

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
    bool getVec(std::string name, float* val, int n) const;

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
    bool getIntVec(std::string name, int* val, int n) const;

    virtual ~UniformBlock()
    {
        if ((UniformData != NULL) && ownData)
        {
            free(UniformData);
        }
        UniformData = NULL;
    }
private:
    std::map<std::string, Uniform> UniformMap;

protected:

    /**
     * Parse the descriptor string to create the UniformMap
     * which contains the name, offset and size of all uniforms.
     */
    void parseDescriptor();

    /**
     * Calculate the byte size of the given type.
     */
    int calcSize(std::string type) const;
    /**
     * Constructs the data block containing the values
     * for all the uniform variables in the descriptor.
     */
    void  makeData()
    {
        if (UniformData == NULL)
        {
            UniformData = malloc(TotalSize);
            ownData = true;
        }
    }

    /**
     * Look up the named uniform in the UniformMap.
     * This function fails if the uniform found does not
     * have the same byte size as the input bytesize.
     * @param name name of uniform to find.
     * @param bytesize byte size of uniform.
     * @return pointer to Uniform structure describing the uniform or NULL on failure
     */
    Uniform* getUniform(std::string name, int& bytesize);
    const Uniform* getUniform(std::string name, int& bytesize) const;


    /**
     * Get a pointer to the value for the named uniform.
     * @param name name of uniform to get.
     * @param bytesize number of bytes uniform occupies
     * @return pointer to start of uniform value or NULL if not found.
     */
    char* getData(std::string name, int& bytesize);
    const char* getData(std::string name, int& bytesize) const;

    /*
     * Marks the uniform block as dirty for all shaders.
     */
    virtual void setDirty() { }
    bool        ownData;        // true if this uniform owns its data block
    std::string BlockName;      // uniform block name in shadere
    std::string Descriptor;     // descriptor with name, type and size of uniforms
    void*       UniformData;    // -> data block with uniform values
    GLint       TotalSize;      // number of bytes in data block


};
class VulkanUniformBlock: public UniformBlock
{
public:
bool buffer_init_;
    GVR_Uniform m_bufferInfo;
    VulkanUniformBlock();
    VulkanUniformBlock(const std::string& descriptor);
    void createBuffer(VkDevice &,VulkanCore*);
    GVR_Uniform& getBuffer(){
        return m_bufferInfo;
    }
    void updateBuffer(VkDevice &device,VulkanCore* vk);
};
/**
 * Manages a GLSL Uniform Block containing data parameters to pass to
 * the vertex and fragment shaders.
 */
class GLUniformBlock : public UniformBlock
{
public:
    GLUniformBlock();
    GLUniformBlock(const std::string& descriptor);

    virtual void render(GLuint programId);
    virtual void bindBuffer(GLuint programId);


    void setData(char* data, int offset);

    virtual ~GLUniformBlock()
    {
        if (GLBuffer > 0)
            glDeleteBuffers(1, &GLBuffer);
    }


    void setBuffer(GLuint buffer, GLuint bindingPoint)
    {
        if (GLBuffer != 0)
        {
            LOGE("UniformBlock: ERROR: GL buffer cannot be changed\n");
            return;
        }
        GLBuffer = buffer;
        GLBindingPoint = bindingPoint;
    }

    int getGLBindingPoint() const
    {
        return GLBindingPoint;
    }

    void setGLBindingPoint(int bufferNum)
    {
        GLBindingPoint = bufferNum;
    }


    /*
     * Mark the block as needing update for all shaders using it
     */
    virtual void setDirty() {
        for (auto it = Dirty.begin(); it != Dirty.end(); ++it) {
            it->second = true;
        }
    }

    static void dump(GLuint programID, int blockIndex);

protected:
    static GLint sizeFromUniformType(GLint type);

    GLint       GLBlockIndex;
    GLint       GLBindingPoint;
    GLuint      GLBuffer;
    GLuint      GLOffset;
    std::map<int, bool> Dirty;
};
}
#endif
