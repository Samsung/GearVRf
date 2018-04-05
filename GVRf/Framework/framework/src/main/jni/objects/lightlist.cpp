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

#include "engine/renderer/renderer.h"
#include "objects/lightlist.h"
#include "objects/light.h"
#include "objects/scene.h"
#include "shaders/shader.h"

#define LIGHT_ADDED 1
#define LIGHT_REMOVED 2
#define SHADOW_CHANGED 4
#define REBUILD_SHADERS 8

namespace gvr {

LightList::~LightList()
{
    if (mLightBlock)
    {
        delete mLightBlock;
        mLightBlock = nullptr;
    }
#ifdef DEBUG_LIGHT
    LOGD("LIGHT: deleting light block");
#endif
}

int LightList::getLights(std::vector<Light*>& lightList) const
{
    std::lock_guard < std::recursive_mutex > lock(mLock);
    for (auto itc = mClassMap.begin(); itc != mClassMap.end(); ++itc)
    {
        const std::vector<Light*>& lights = itc->second;
        for (auto itl = lights.begin(); itl != lights.end(); ++itl)
        {
            Light* light = *itl;
            lightList.push_back(light);
        }
    }
    return lightList.size();
}

/*
 * Adds a new light to the scene.
 * Return true if light was added, false if already there or too many lights.
 */
bool LightList::addLight(Light* light)
{
    std::lock_guard < std::recursive_mutex > lock(mLock);

    auto it2 = mClassMap.find(light->getLightClass());
    if (it2 != mClassMap.end())
    {
        std::vector<Light*>& lights = it2->second;
        light->setLightIndex(lights.size());
        lights.push_back(light);
    }
    else
    {
        std::pair<std::string, std::vector<Light*>> pair;

        light->setLightIndex(0);
        pair.first = light->getLightClass();
        pair.second.push_back(light);
        mClassMap.insert(pair);
    }
    mDirty |= LIGHT_ADDED | REBUILD_SHADERS;
#ifdef DEBUG_LIGHT
    LOGD("LIGHT: %s added to scene", light->getLightClass());
#endif
    return true;
}

/*
 * Removes an existing light from the scene.
 * Return true if light was removed, false if light was not in the scene.
 */
bool LightList::removeLight(Light* light)
{
    std::lock_guard < std::recursive_mutex > lock(mLock);

    /*
     * Find the list of lights of this type in the light class map.
     */
    auto it3 = mClassMap.find(light->getLightClass());
    if (it3 != mClassMap.end())
    {
        std::vector<Light*>& lights = it3->second;
        auto it2 = std::find(lights.begin(), lights.end(), light);
        if (it2 == lights.end())
        {
            return false;
        }
        light->setLightIndex(-1);
        /*
         * If all lights in the class are gone,
         * remove the class from the map.
         */
        if (lights.size() == 0)
        {
            mClassMap.erase(it3);
            return true;
        }
        /*
         * Removed a light, recompute light indices for all
         * lights of that type
         */
        else
        {
            int index = 0;
            lights.erase(it2);
            for (auto it = lights.begin();
                 it != lights.end();
                 ++it)
            {
                Light* l = *it;
                l->setLightIndex(index++);
            }
        }
    }
#ifdef DEBUG_LIGHT
    LOGD("LIGHT: %s removed from scene", light->getLightClass());
#endif
    mDirty |= LIGHT_REMOVED | REBUILD_SHADERS;
    return true;
}

void LightList::shadersRebuilt()
{
    mDirty &= ~REBUILD_SHADERS;
}

void LightList::forEachLight(std::function<void(Light&)> func)
{
    for (auto it1 = mClassMap.begin();
         it1 != mClassMap.end();
         ++it1)
    {
        const std::vector<Light*>& lights = it1->second;
        for (auto it2 = lights.begin(); it2 != lights.end(); ++it2)
        {
            Light* light = *it2;
            if (light)
            {
                func(*light);
            }
        }
    }
}

void LightList::forEachLight(std::function<void(const Light&)> func) const
{
    for (auto it1 = mClassMap.begin();
         it1 != mClassMap.end();
         ++it1)
    {
        const std::vector<Light*>& lights = it1->second;
        for (auto it2 = lights.begin(); it2 != lights.end(); ++it2)
        {
            Light* light = *it2;
            if (light)
            {
                func(*light);
            }
        }
    }
}

ShadowMap* LightList::scanLights()
{
    ShadowMap* shadowMap = NULL;

    mTotalUniforms = 0;
    for (auto it1 = mClassMap.begin();
         it1 != mClassMap.end();
         ++it1)
    {
        const std::vector<Light*>& lights = it1->second;
        for (auto it2 = lights.begin(); it2 != lights.end(); ++it2)
        {
            Light* light = *it2;

            mTotalUniforms += light->uniforms().uniforms().getNumEntries();
            ShadowMap* sm = light->getShadowMap();
            if (sm && sm->enabled())
            {
                shadowMap = sm;
            }
        }
    }
    return shadowMap;
}

ShadowMap* LightList::updateLightBlock(Renderer* renderer)
{
    std::lock_guard < std::recursive_mutex > lock(mLock);
    bool dirty = mDirty != 0;
    ShadowMap* shadowMap = NULL;

    if (mDirty & REBUILD_SHADERS)
    {
        return scanLights();
    }
    if (mDirty & LIGHT_ADDED)
    {
        createLightBlock(renderer);
    }
    mTotalUniforms = 0;
    if (mClassMap.size() == 0)
    {
        return NULL;
    }
    for (auto it1 = mClassMap.begin();
         it1 != mClassMap.end();
         ++it1)
    {
        const std::vector<Light*>& lights = it1->second;
        for (auto it2 = lights.begin(); it2 != lights.end(); ++it2)
        {
            Light* light = *it2;
            ShadowMap* sm = light->getShadowMap();

            mTotalUniforms += light->uniforms().uniforms().getNumEntries();
            if (sm && sm->enabled())
            {
                shadowMap = sm;
            }
            if (dirty || light->uniforms().isDirty(ShaderData::MAT_DATA))
            {
                int offset = light->getBlockOffset();
                const UniformBlock& uniforms = light->uniforms().uniforms();
                mLightBlock->updateGPU(renderer, offset, uniforms);
                light->uniforms().clearDirty();
            }
        }
    }
    mDirty = 0;
    return shadowMap;
}

void LightList::useLights(Renderer* renderer, Shader* shader)
{
    if (mUseUniformBlock)
    {
        mLightBlock->bindBuffer(shader, renderer);
    }
    else
    {
        shader->bindLights(*this, renderer);
    }
}

void LightList::makeShadowMaps(Scene* scene, jobject jscene, ShaderManager* shaderManager)
{
    std::lock_guard < std::recursive_mutex > lock(mLock);
    int layerIndex = 0;
    int numShadowMaps = 0;

    for (auto it2 = mClassMap.begin(); it2 != mClassMap.end(); ++it2)
    {
        const std::vector<Light*>& lights = it2->second;
        for (auto it = lights.begin(); it != lights.end(); ++it)
        {
            Light *l = (*it);
            if (l->enabled())
            {
                if (l->makeShadowMap(scene, jscene, shaderManager, layerIndex))
                {
                    ++numShadowMaps;
                    ++layerIndex;
                }
            }
        }
    }
    if (mNumShadowMaps != numShadowMaps)
    {
        mNumShadowMaps = numShadowMaps;
#ifdef DEBUG_LIGHT
        LOGD("LIGHT: %d shadow maps", mNumShadowMaps);
#endif
    }
}

bool LightList::createLightBlock(Renderer* renderer)
{
    int numFloats = 0;

    for (auto it = mClassMap.begin(); it != mClassMap.end(); ++it)
    {
        const std::vector<Light*>& lights = it->second;
        for (auto it2 = lights.begin(); it2 != lights.end(); ++it2)
        {
            Light *light = (*it2);
            light->setBlockOffset(numFloats);
            numFloats += light->getTotalSize() / sizeof(float);
        }
    }
    if ((mLightBlock == NULL) ||
        (numFloats > (mLightBlock->getTotalSize()/ sizeof(float))))
    {
        std::string desc("float lightdata");
        if (mLightBlock)
        {
            delete mLightBlock;
        }
        mLightBlock = renderer->createUniformBlock(desc.c_str(), LIGHT_UBO_INDEX, "Lights_ubo", numFloats);
        mLightBlock->useGPUBuffer(true);
#ifdef DEBUG_LIGHT
        LOGD("LIGHT: creating light uniform block");
#endif
        return true;
    }
    return false;
}

/*
 * Removes all the lights from the scene.
 */
void LightList::clear()
{
    std::lock_guard < std::recursive_mutex > lock(mLock);
    mClassMap.clear();
    mDirty = LIGHT_REMOVED | REBUILD_SHADERS;
#ifdef DEBUG_LIGHT
    LOGD("LIGHT: clearing lights");
#endif
}

void LightList::makeShaderBlock(std::string& layout) const
{
    std::lock_guard < std::recursive_mutex > lock(mLock);
    std::ostringstream stream;
    std::string prefix;

    if (mUseUniformBlock)
    {
        int uboLightBinding = LIGHT_UBO_INDEX;
        stream <<
                "\n#ifdef VULKAN\n"
                "layout (std140, set = 0, binding = " << uboLightBinding << ")"
                "\n#else\n"
                "layout (std140)"
                "\n#endif\n"
                "uniform Lights_ubo\n{"
               << std::endl;
    }
    else
    {
        prefix = "uniform ";
    }
    for (auto it = mClassMap.begin(); it != mClassMap.end(); ++it)
    {
        const std::vector<Light*>& lights = it->second;
        stream << prefix << 'U' << it->first << " " << it->first << "s[" << lights.size() << "];" << std::endl;
    }
    if (mUseUniformBlock)
    {
        stream << "};" << std::endl;
    }
    layout = stream.str();
}

}
