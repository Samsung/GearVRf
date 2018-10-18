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
 * Holds scene objects. Can be used by engines.
 ***************************************************************************/

#ifndef LIGHTLIST_H_
#define LIGHTLIST_H_

#include <functional>
#include "engine/renderer/renderer.h"

#include "objects/light.h"

namespace gvr {

    /**
     * Contains the light sources used by a Scene.
     *
     * The GearVRF light sources are global - all of
     * them illuminate all the scene objects.
     * Internally they are kept in a UniformBlock and
     * updated once per frame. The OpenGL implementation
     * can emit direct GL calls instead of using the
     * uniform block.
     * @see Light
     * @see Scene
     * @see UniformBlock
     */
    class LightList
    {
    public:
        LightList() : mDirty(0),
                mLightBlock(nullptr),
                mNumShadowMaps(0),
                mTotalUniforms(0),
                mNumLights(0),
                mShadowMap(nullptr),
                mUseUniformBlock(true) { }

        ~LightList() { clear(); }

        /**
         * Enable or disable the use of uniform block.
         * (OpenGL only).
         * @see #usingUniformBlock
         */
        void useUniformBlock()  { mUseUniformBlock = true; }

        /**
         * Determine if a uniform block is used for lights.
         * @return true if lights are in uniform block,
         *         false if direct GL calls are used to update.
         * @see #useUniformBlock
         */
        bool usingUniformBlock()    { return mUseUniformBlock; }

        /**
         * Adds a new light to the scene.
         * Return true if light was added, false if already there or too many lights.
         * @see #removeLight
         * @see Scene::addLight
         */
        bool addLight(Light* light);

        /*
         * Removes an existing light from the scene.
         * Return true if light was removed, false if light was not in the scene.
         * @see #addLight
         * @see Scene::addLight
         */
        bool removeLight(Light* light);

        /*
         * Removes all the lights from the scene.
         * @see #addLight
         * @see #removeLight
         * @see Scene::clearLights
         */
        void clear();

        /*
         * Call the given function for each light in the list.
         * @param func function to call
         */
        void forEachLight(std::function< void(const Light&) > func) const;
        void forEachLight(std::function< void(Light&) > func);

        /**
         * Get a vector with all the lights in the scene.
         * They will be ordered by light type (all the lights
         * of the same type are together).
         * @param lights vector to get light list
         * @return number of lights
         */
        int getLights(std::vector<Light*>& lights) const;

        /**
         * Make a string with the shader layout for the light sources.
         * @param layout shader layout string.
         */
        void makeShaderBlock(std::string& layout) const;

        /**
         * Update the light sources, copying data from the
         * CPU to the GPU.
         * @param renderer  Renderer to use
         * @return shadow map if null if no shadows
         */
        ShadowMap* updateLights(Renderer *renderer);

        /**
         * Create the uniform block to hold the light sources.
         * @param renderer  Renderer to use
         * @return true if light block created, else false.
         */
        bool createLightBlock(Renderer* renderer);

        /**
         * Determine light list is dirty (lights have been
         * added or removed since last frame).
         * @return true if dirty, false if clean.
         */
        bool isDirty() const
        {
            return mDirty != 0;
        }

        /**
         * Get the number of uniforms used by all light sources.
         * @return number of light source uniforms.
         */
        int getNumUniforms() const
        {
            return mTotalUniforms;
        }

        /**
         * Get the number of lights with shadows enabled.
         * @return number of shadow maps.
         */
        int getShadowMapCount() const
        {
            return mNumShadowMaps;
        }

        /**
         * Get the number of light sources.
         * @return light count.
         */
        int getLightCount() const
        {
            return mNumLights;
        }

        /**
         * Get the uniform block that contains the light sources.
         * @return UniformBlock with lights, null if none
         */
        UniformBlock* getUBO()
        {
            return mLightBlock;
        }

        /**
         * Get the ShadowMap for the light sources.
         * Each light source has its own ShadowMap,
         * but all the shadow maps share a single layered texture.
         * @return ShadowMap or null if none
         */
        ShadowMap* getShadowMap() const { return mShadowMap; }

        /**
         * Generate shadow maps for all the lights that require them.
         * This function renders the scene from the viewpoint of each
         * light and caputures the resulting depth buffer in a texture
         * which is later used to shadow the scene.
         * @param scene         Scene to make shadow maps for
         * @param jscene        GVRScene to make shadow maps for
         * @param shaderManager ShaderManager with shaders
         */
        void makeShadowMaps(Scene* scene, jobject jscene, ShaderManager* shaderManager);

        /**
         * Internal function used to tell the renderer to use
         * light sources for rendering an object.
         * @param renderer  Renderer to use.
         * @param shader    Shader to use.
         */
        void useLights(Renderer* renderer, Shader* shader);

        /**
         * Returns the descriptor for the light sources in the shader.
         * This is a string that is unique for a particular
         * set of light sources.
         * @return light source descriptor
         */
        const char* getDescriptor() const { return mLightDesc; }

    private:
        LightList(const LightList& lights) = delete;
        LightList(LightList&& lights) = delete;
        LightList& operator=(const LightList& lights) = delete;
        LightList& operator=(LightList&& lights) = delete;


    private:
        static const int LIGHT_DESC_LENGTH  = 256;
        mutable std::recursive_mutex mLock;
        std::map<std::string, std::vector<Light*>> mClassMap;
        UniformBlock* mLightBlock;
        ShadowMap* mShadowMap;
        int mNumShadowMaps;
        int mDirty;
        bool mUseUniformBlock;
        int mTotalUniforms;
        int mNumLights;
        char mLightDesc[LIGHT_DESC_LENGTH];
    };

}
#endif
