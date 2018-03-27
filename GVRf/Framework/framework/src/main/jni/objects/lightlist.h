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

class LightList
{
public:
    LightList() : mDirty(0),
                  mLightBlock(NULL),
                  mNumShadowMaps(0),
                  mTotalUniforms(0),
                  mUseUniformBlock(true) { }

    virtual ~LightList();

    void useUniformBlock()  { mUseUniformBlock = true; }

    bool usingUniformBlock()    { return mUseUniformBlock; }

    /*
     * Adds a new light to the scene.
     * Return true if light was added, false if already there or too many lights.
     */
    bool addLight(Light* light);

    /*
     * Removes an existing light from the scene.
     * Return true if light was removed, false if light was not in the scene.
     */
    bool removeLight(Light* light);

    /*
     * Removes all the lights from the scene.
     */
    void clear();

    /*
     * Call the given function for each light in the list.
     * @param func function to call
     */
    void forEachLight(std::function< void(const Light&) > func) const;
    void forEachLight(std::function< void(Light&) > func);

    int getLights(std::vector<Light*>& lights) const;

    void makeShaderBlock(std::string& layout) const;

    ShadowMap* updateLightBlock(Renderer* renderer);

    bool createLightBlock(Renderer* renderer);

    bool isDirty() const
    {
        return mDirty != 0;
    }

    void clearDirty()
    {
        mDirty = 0;
    }

    int getNumUniforms() const
    {
        return mTotalUniforms;
    }

    void shadersRebuilt();
    ShadowMap* scanLights();

    void makeShadowMaps(Scene* scene, jobject jscene, ShaderManager* shaderManager);
    void useLights(Renderer* renderer, Shader* shader);

private:
    LightList(const LightList& lights) = delete;
    LightList(LightList&& lights) = delete;
    LightList& operator=(const LightList& lights) = delete;
    LightList& operator=(LightList&& lights) = delete;


private:
    mutable std::recursive_mutex mLock;
    std::map<std::string, std::vector<Light*>> mClassMap;
    UniformBlock* mLightBlock;
    int mNumShadowMaps;
    int mDirty;
    bool mUseUniformBlock;
    int mTotalUniforms;
};

}
#endif
