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
 * Picks scene object in a scene.
 ***************************************************************************/

#ifndef PICKING_ENGINE_H_
#define PICKING_ENGINE_H_

#include <vector>
#include <memory>
#include "objects/components/collider.h"
#include "glm/glm.hpp"

namespace gvr {
class Scene;
class Collider;
class SceneObject;
class CameraRig;

class Picker {
private:
    Picker();
    ~Picker();

public:
    static void pickScene(Scene* scene, std::vector<ColliderData>& pickList);
    static void pickScene(
            Scene* scene, std::vector<ColliderData>& pickList,
            float ox, float oy, float oz,
            float dx, float dy, float dz);
    static float pickSceneObject(
            const SceneObject* scene_object,
            const CameraRig* camera_rig);
    static glm::vec3 pickSceneObjectAgainstBoundingBox(
            const SceneObject* scene_object, float ox, float oy, float oz,
            float dx, float dy, float dz);
};

}

#endif
