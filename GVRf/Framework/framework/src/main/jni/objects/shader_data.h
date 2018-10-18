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
 * Data for doing a post effect on the scene.
 ***************************************************************************/

#ifndef SHADER_DATA_H_
#define SHADER_DATA_H_

#include <map>
#include <memory>
#include <string>
#include <functional>

#include "objects/hybrid_object.h"
#include "objects/textures/texture.h"
#include "objects/uniform_block.h"


namespace gvr {

class Texture;
class RenderData;

/**
 * Contains textures and uniform data used by shaders.
 *
 * The texture descriptor provided to the constructor
 * designates the names and types of textures.
 * These must match the names and types from the fragment
 * shader this material is used with.
 *
 * The uniform data is kept in a UniformBlock which also
 * has a descriptor giving the layout of the uniforms
 * for the shader. These names should also match those
 * used by the shader.
 *
 * Both this class and UniformBlock have renderer-specific
 * variant which implement the functionality for both
 * OpenGL and Vulkan.
 * @see GLMaterial
 * @see VulkanMaterial
 * @see UniformBlock
 */
class ShaderData : public HybridObject
{
public:
    enum DIRTY_BITS
    {
        NONE = 0,
        NEW_TEXTURE = 2,
        MOD_TEXTURE = 4,
        MAT_DATA = 8,
    };

    explicit ShaderData(const char* texture_desc);

    virtual ~ShaderData() { }

    /**
     * Get the uniform descriptor for this material.
     * This is provided to the GLMaterial or
     * VulkanMaterial constructor.
     * @return string with uniform names and types
     */
    const char* getUniformDescriptor() const;

    /**
     * Get the texture descriptor for this material.
     * This is provided to the ShaderData constructor.
     * @return string with texture names and types
     */
    const char* getTextureDescriptor() const;

    /**
     * Get a texture by name (from texture descriptor).
     * If the name is not found in the texture descriptor,
     * this function returns NULL.
     * @param name  name of texture to get
     * @return -> Texture or NULL if not set
     */
    Texture* getTexture(const char* name) const;

    /**
     * Set a texture by name (from texture descriptor).
     * If the name is not found in the texture descriptor,
     * this function does nothing.
     * @param name      name of texture to set
     * @param texture   -> Texture or NULL if texture not used by shader
     */
    void    setTexture(const char* name, Texture* texture);

    /**
     * Call a function for each texture in the texture descriptor.
     * If the texture has not been set, the Texture pointer
     * passed to the function will be NULL.
     * @param func function to call for each texture
     */
    void    forEachTexture(std::function< void(const char* texname, Texture* tex) > func) const;

    /**
     * Get the number of bytes used by the named uniform.
     * This function returns 0 if the uniform is not found
     * in the uniform descriptor.
     * @param name  name of uniform to get byte size for
     * @return number of bytes occupied by this uniform, 0 on error,
     */
    int     getByteSize(const char* name) const;

    /**
     * Get the total byte size occupied by all uniforms.
     * @return total size of uniform block, 0 if descriptor was null.
     */
    int     getTotalSize() const;

    /**
     * Get the number of uniforms used by this material.
     * @return number of uniforms, 0 if descriptor was null.
     */
    int     getNumUniforms() const { return uniforms().getNumEntries(); }

    /**
     * Get the shader type for the given descriptor type.
     * Here are some examples:
     * @code
     *  float4 -> vec4
     *  int4 -> ivec4
     *  float -> float
     *  int -> int
     *  mat4 -> mat4
     *  mat3 -> mat3
     *  @endcode
     * @param descriptorType    type from uniform descriptor
     * @return type used in shader
     */
    std::string getShaderType(const char* descriptorType);

    /**
     * Get the value of a floating point uniform.
     * @param name  name of uniform to get
     * @param v     where to store value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    getFloat(const char* name, float& v) const;

    /**
     * Get the value of an integer uniform.
     * @param name  name of uniform to get
     * @param v     where to store value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    getInt(const char* name, int& v) const;

    /**
     * Set the value of an integer uniform.
     * @param name  name of uniform to set
     * @param v     value to set
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    setInt(const char* name, int val);

    /**
     * Set the value of a floating point uniform.
     * @param name  name of uniform to set
     * @param v     value to set
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    setFloat(const char* name, float val);

    /**
     * Set the value of an integer vector uniform.
     * @param name  name of uniform to set
     * @param val   integer vector data
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    setIntVec(const char* name, const int* val, int n);

    /**
     * Set the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param val   floating point vector data
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    setFloatVec(const char* name, const float* val, int n);

    /**
     * Get the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param val   floating point array
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    getFloatVec(const char* name, float* val, int n) const;

    /**
     * Get the value of an integer vector uniform.
     * @param name  name of uniform to set
     * @param val   integer array
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    getIntVec(const char* name, int* val, int n) const;

    /**
     * Set the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param v    floating point vector
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    setVec2(const char* name, const glm::vec2& v);

    /**
     * Set the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param v    floating point vector
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    setVec3(const char* name, const glm::vec3& v);

    /**
     * Set the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param v    floating point vector
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    setVec4(const char* name, const glm::vec4& v);

    /**
     * Get the value of a 4x4 matrix uniform.
     * @param name  name of uniform to set
     * @param m    4x4 matrix to get the value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    getMat4(const char* name, glm::mat4& m) const;

    /**
     * Set the value of a 4x4 matrix uniform.
     * @param name  name of uniform to set
     * @param m    4x4 matrix value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool    setMat4(const char* name, const glm::mat4& m);

    /**
     * Internal function to mark a section of this material as dirty.
     * @param bits bits indicating what part is dirty
     */
    void    makeDirty(DIRTY_BITS bits);

    /**
     * Internal function to clear dirty flags.
     */
    void    clearDirty();

    /**
     * Queries whether a part of this material is dirty.
     * @param bits dirty flags to check
     * @return true if material is dirty, else false
     */
    bool    isDirty(DIRTY_BITS bits) const;

    /**
     * Determines whether this material is transparent.
     * A material is transparent if it's main texture
     * has transparency.
     * @return true if main texture is transparent, else fals
     */
    bool    isTransparent() const;

    /**
     * Determine whether or not a material has a specific texture set.
     * @param name  name of texture to check.
     * @return true if texture has been set, false if not set or not in texture descriptor.
     */
    bool    hasTexture(const char* name) const;

    /**
     * Determine whether or not a material has a named uniform.
     * @param name  name of uniform to check.
     * @return true if uniform is in descriptor and is used.
     */
    bool    hasUniform(const char* name) const;

    /**
     * Copy the uniforms from an other material.
     * This function only copies the values of uniforms
     * common to both materials.
     * @param src   material to copy from.
     * @return true if successful, false on error.
     */
    bool    copyUniforms(const ShaderData* src);

    /**
     * Call a function for each entry in the uniform layout.
     * @param func function to call
     */
    void forEachEntry(std::function< void(const DataDescriptor::DataEntry&) > func) const
    {
        return uniforms().forEachEntry(func);
    }

    void forEachEntry(std::function< void(DataDescriptor::DataEntry&) > func)
    {
        return uniforms().forEachEntry(func);
    }

    /**
     * Updates the values of the uniforms and textures
     * by copying the relevant data from the CPU to the GPU.
     * This function operates independently of the shader,
     * so it cannot tell if a texture the shader requires
     * is missing.
     * @param renderer
     * @return 1 = success, -1 texture not ready, 0 uniforms failed to load
     */
    virtual int updateGPU(Renderer* renderer);

    /**
     * Make a string with the shader layout for this material's uniforms.
     * @return shader layout string
     */
    std::string makeShaderLayout();

    /**
     * Get the number of textures used by this material.
     * This is the number of textures in the descriptor.
     * Even if a texture has a null value it will be counted.
     * @return number of textures in texture descriptor
     */
    int getNumTextures() const { return mTextures.size(); }

    /**
     * Get the uniform block containing uniforms for this material
     * @return Renderer-specific UniformBlock instance
     */
    virtual UniformBlock&   uniforms() = 0;

    /**
     * Get the uniform block containing uniforms for this material
     * @return Renderer-specific UniformBlock instance
     */
    virtual const UniformBlock& uniforms() const = 0;

    /**
     * Designate whether a uniform buffer or direct GL
     * calls should be used for this material.
     * This function only applies to the OpenGL renderer.
     * Vulkan always uses a uniform buffer.
     * @param flag true to use uniform buffer, false to use direct calls.
     */
    virtual void useGPUBuffer(bool flag) = 0;

private:
    ShaderData(const ShaderData&) = delete;
    ShaderData(ShaderData&&) = delete;
    ShaderData& operator=(const ShaderData&) = delete;
    ShaderData& operator=(ShaderData&&) = delete;

protected:
    bool mIsTransparent;
    int mNativeShader;
    std::string mTextureDesc;
    std::vector<std::string> mTextureNames;
    std::vector<Texture*> mTextures;
    mutable std::mutex mLock;
    DIRTY_BITS mDirty;
};

}
#endif
