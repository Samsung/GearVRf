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
    static const int DEFAULT_BLOCK_SIZE = 1024;
    LightList() : mDirty(0), mLightBlock(NULL) { }

    virtual ~LightList();

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

    const std::vector<Light*>& getLights() const
    {
        return mLightList;
    }

    void makeShaderBlock(std::string& layout) const;

    ShadowMap* updateLights(Renderer* renderer, Shader* shader);

    bool createLightBlock(Renderer* renderer);

    bool isDirty() const
    {
        return mDirty != 0;
    }

    void makeShadowMaps(Scene* scene, ShaderManager* shaderManager);

private:
    LightList(const LightList& lights) = delete;
    LightList(LightList&& lights) = delete;
    LightList& operator=(const LightList& lights) = delete;
    LightList& operator=(LightList&& lights) = delete;

private:
    mutable std::mutex mLock;
    std::vector<Light*> mLightList;
    std::map<std::string, int> mClassMap;
    UniformBlock* mLightBlock;
    int mDirty;
};

}
#endif
