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

namespace gvr {
class Scene;
class EyePointeeHolder;
class SceneObject;
class CameraRig;

class Picker {
private:
    Picker();
    ~Picker();

public:
    static std::vector<EyePointeeHolder*> pickScene(Scene* scene);
    static std::vector<EyePointeeHolder*> pickScene(
            Scene* scene, float ox, float oy, float oz,
            float dx, float dy, float dz);
    static float pickSceneObject(
            const SceneObject* scene_object,
            const CameraRig* camera_rig);
};

}

#endif
