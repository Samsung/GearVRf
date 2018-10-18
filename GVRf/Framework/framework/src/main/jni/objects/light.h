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
 * Class containing light source parameters.
 ***************************************************************************/

#ifndef LIGHT_H_
#define LIGHT_H_

#include "objects/shader_data.h"
#include "engine/renderer/renderer.h"
#include "components/java_component.h"
#include "objects/scene_object.h"
#include "objects/components/shadow_map.h"
#include "util/gvr_jni.h"
#include "engine/renderer/renderer.h"

namespace gvr {
class SceneObject;
class Scene;
class Shader;
class ShadowMap;

//#define DEBUG_LIGHT 1

/**
 * Describes a source of illumination in the scene.
 * A light source is a collection of uniforms.
 * All light sources are combined into a single
 * UniformBlock and updated once per frame.
 * @see LightList
 * @see Scene
 */
class Light : public JavaComponent
{
public:

    explicit Light()
    :   JavaComponent(Light::getComponentType()),
        mBlockOffset(0),
        mShadowMapIndex(-1),
        mLightIndex(-1)
    {
    }

    virtual ~Light();

    static long long getComponentType()
    {
        return COMPONENT_TYPE_LIGHT;
    }

    /**
     * Get the offset of this light source in the
     * global uniform block containing all the lights.
     * @return block offset (# of floats)
     */
    int getBlockOffset() const
    {
        return mBlockOffset;
    }

    /**
     * Set the offset of this light source in the
     * global uniform block containing all the lights.
     * @param offset block offset of this light (# of floats)
     */
    void setBlockOffset(int offset)
    {
        mBlockOffset = offset;
    }

    /**
     * Get the total number of bytes this light source occupies.
     * @return byte size of light uniforms.
     */
    int getTotalSize() const
    {
        return uniforms().getTotalSize();
    }

    /**
     * Get the number of bytes for a named light uniform.
     * @param name  name of light uniform
     * @return number of bytes the uniform occupies or 0 if name not in descriptor
     */
    int getByteSize(const char* name) const
    {
        return uniforms().getByteSize(name);
    }

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
     * @param type    type from uniform descriptor
     * @return type used in shader
     */
    std::string getShaderType(const char* type)
    {
        return uniforms().getShaderType(type);
    }

    /**
     * Determine whether or not the light has a named uniform.
     * @param name  name of uniform to check.
     * @return true if uniform is in descriptor and is used.
     */
    bool hasUniform(const char* key) const
    {
        return uniforms().hasUniform(key);
    }

    /**
     * Get the number of uniforms used by this light.
     * @return number of uniforms.
     */
    int getNumUniforms() const
    {
        return uniforms().getNumUniforms();
    }

    /**
     * Call a function for each light uniform.
     * @param func function to call
     */
    void forEachUniform(std::function< void(const DataDescriptor::DataEntry&) > func) const
    {
        return uniforms().forEachEntry(func);
    }

    /**
     * Call a function for each light uniform.
     * @param func function to call
     */
    void forEachUniform(std::function< void(DataDescriptor::DataEntry&) > func)
    {
        return uniforms().forEachEntry(func);
    }

    /**
     * Get a texture by name (from texture descriptor).
     * If the name is not found in the texture descriptor,
     * this function returns NULL.
     * CURRENTLY LIGHTS DO NOTHING WITH THESE TEXTURES.
     * @param name  name of texture to get
     * @return -> Texture or NULL if not set
     */
    Texture* getTexture(const char* name) const
    {
        return uniforms().getTexture(name);
    }

    /**
     * Set a texture by name (from texture descriptor).
     * If the name is not found in the texture descriptor,
     * this function does nothing.
     * CURRENTLY LIGHTS DO NOTHING WITH THESE TEXTURES.
     * @param name      name of texture to set
     * @param texture   -> Texture or NULL if texture not used by shader
     */
    void setTexture(const char* name, Texture* texture)
    {
        uniforms().setTexture(name, texture);
    }

    /**
     * Get the value of a floating point uniform.
     * @param name  name of uniform to get
     * @param v     where to store value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool  getFloat(const char* name, float& v) const
    {
       return uniforms().getFloat(name, v);
    }

    /**
     * Get the value of an integer uniform.
     * @param name  name of uniform to get
     * @param v     where to store value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool getInt(const char* name, int& v) const
    {
        return uniforms().getInt(name, v);
    }

    /**
     * Set the value of an integer uniform.
     * @param name  name of uniform to set
     * @param v     value to set
     * @return true if successful, false if uniform name not in descriptor
     */
    bool  setInt(const char* name, int val)
    {
        return uniforms().setInt(name, val);
    }

    /**
     * Set the value of an integer vector uniform.
     * @param name  name of uniform to set
     * @param val   integer vector data
     * @return true if successful, false if uniform name not in descriptor
     */
    bool  setIntVec(const char* name, const int* val, int n)
    {
        return uniforms().setIntVec(name, val, n);
    }

    /**
     * Set the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param val   floating point vector data
     * @return true if successful, false if uniform name not in descriptor
     */
    bool setFloatVec(const char* name, const float* val, int n)
    {
        return uniforms().setFloatVec(name, val, n);
    }

    /**
     * Get the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param val   floating point array
     * @return true if successful, false if uniform name not in descriptor
     */
    bool  getFloatVec(const char* name, float* val, int n)
    {
        return uniforms().getFloatVec(name, val, n);
    }

    /**
     * Get the value of an integer vector uniform.
     * @param name  name of uniform to set
     * @param val   integer array
     * @return true if successful, false if uniform name not in descriptor
     */
    bool getIntVec(const char* name, int* val, int n)
    {
        return uniforms().getIntVec(name, val, n);
    }

    /**
     * Set the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param v    floating point vector
     * @return true if successful, false if uniform name not in descriptor
     */
    bool setVec2(const char* name, const glm::vec2& v)
    {
        return uniforms().setVec2(name, v);
    }

    /**
     * Get the value of a floating point uniform.
     * @param name  name of uniform to get
     * @param v     where to store value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool getFloat(const char* key, float& val)
    {
        return uniforms().getFloat(key, val);
    }

    /**
     * Set the value of a floating point uniform.
     * @param name  name of uniform to set
     * @param v     value to set
     * @return true if successful, false if uniform name not in descriptor
     */
    void setFloat(const char* key, float value)
    {
        uniforms().setFloat(key, value);
    }

    /**
     * Set the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param v    floating point vector
     * @return true if successful, false if uniform name not in descriptor
     */
    bool setVec3(const char* key, const glm::vec3& vector)
    {
        return uniforms().setVec3(key, vector);
    }

    /**
     * Set the value of a floating point vector uniform.
     * @param name  name of uniform to set
     * @param v    floating point vector
     * @return true if successful, false if uniform name not in descriptor
     */
    bool setVec4(const char* key, const glm::vec4& vector)
    {
        return uniforms().setVec4(key, vector);
    }

    /**
     * Get the value of a 4x4 matrix uniform.
     * @param name  name of uniform to set
     * @param m    4x4 matrix to get the value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool getMat4(const char* key, glm::mat4& matrix)
    {
        return uniforms().getMat4(key, matrix);
    }

    /**
     * Set the value of a 4x4 matrix uniform.
     * @param name  name of uniform to set
     * @param m    4x4 matrix value
     * @return true if successful, false if uniform name not in descriptor
     */
    bool setMat4(const char* key, const glm::mat4& matrix)
    {
        return uniforms().setMat4(key, matrix);
    }

    /**
     * Determine if this light casts shadows or not.
     * @return true if shadows enabled, false if not.
     */
    bool castShadow()
    {
        return getShadowMap() != nullptr;
    }

    /**
     * Get the shadow map associated with this light.
     * @return ShadowMap or null if no shadowing.
     */
    ShadowMap* getShadowMap();

    /**
     * Make a string with the shader structure layout for this light.
     * @param layout string to get shader structure
     * @return number of uniforms in light source.
     */
    int makeShaderLayout(std::string& layout);

    /**
     * Internal function called at the start of each frame
     * to update the shadow map.
     * @returns the shadow map if it was created, else null
     */
    ShadowMap* makeShadowMap(Scene* scene, jobject jscene, ShaderManager* shader_manager, int texIndex);

    /**
     * Get the light class. This describes the type of light
     * and is typically the name of the Java class that
     * implements the light source.
     * @return string with light class (type)
     */
    const char* getLightClass() const;

    /**
     * Get the light index. This is a 0-based index of the
     * light within it's class. Lights of the same type
     * will always have different light indices. Light of
     * a different type may have the same light index.
     * @return light index
     */
    int getLightIndex() const
    {
        return mLightIndex;
    }

    /**
     * Set the light index. This is a 0-based index of the
     * light within it's class. Lights of the same type
     * will always have different light indices. Light of
     * a different type may have the same light index.
     * @return light index
     */
    void setLightIndex(int index);

   /**
    * Set the light class that determines what
    * type of light this is.
    * {@link GVRScene.addLight }
    */
    void setLightClass(const char* lightClass);

    /**
     * Get the light name. This is a string that uniquely
     * identifies the light. It can be used to access
     * the light from OpenGL.
     * @return name of light source
     */
    const char* getLightName() const;

    /**
     * Called when a light is added to the scene.
     * @param scene Scene light has been added to.
     */
    virtual void onAddedToScene(Scene* scene);

    /**
     * Called when a light is removed to the scene.
     * @param scene Scene light has been removed from.
     */
    virtual void onRemovedFromScene(Scene* scene);

    /**
     * Get the material implementing this light source.
     * @return ShaderData with light uniforms.
     */
    virtual ShaderData&       uniforms() = 0;

    /**
     * Get the material implementing this light source.
     * @return ShaderData with light uniforms.
     */
    virtual const ShaderData& uniforms() const = 0;

private:
    Light(const Light& light) = delete;
    Light(Light&& light) = delete;
    Light& operator=(const Light& light) = delete;
    Light& operator=(Light&& light) = delete;

private:
    int mShadowMapIndex;
    std::string mLightClass;
    std::string mLightName;
    int mLightIndex;
    int mBlockOffset;
};
}
#endif
