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

#include "picker.h"

#include <limits>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"

#include "engine/picker/eye_point_data.h"
#include "engine/picker/eye_pointee_holder_data.h"
#include "objects/scene.h"
#include "objects/scene_object.h"
#include "objects/components/camera_rig.h"
#include "objects/components/eye_pointee_holder.h"

namespace gvr {

Picker::Picker() {
}

Picker::~Picker() {
}

std::vector<EyePointeeHolder*> Picker::pickScene(Scene* scene, float ox,
        float oy, float oz, float dx, float dy, float dz) {
    std::vector<SceneObject*> scene_objects = scene->getWholeSceneObjects();
    std::vector<EyePointeeHolder*> eye_pointee_holders;
    for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
        EyePointeeHolder* eye_pointee_holder = (*it)->eye_pointee_holder();
        if (eye_pointee_holder != 0 && eye_pointee_holder->enable()) {
            eye_pointee_holders.push_back(eye_pointee_holder);
        }
    }

    glm::mat4 view_matrix =
            glm::affineInverse(
                    scene->main_camera_rig()->owner_object()->transform()->getModelMatrix());

    std::vector<EyePointeeHolderData> picked_holder_data;

    for (auto it = eye_pointee_holders.begin(); it != eye_pointee_holders.end();
            ++it) {
        EyePointData data = (*it)->isPointed(view_matrix, ox, oy, oz, dx, dy,
                dz);
        if (data.pointed()) {
            (*it)->set_hit(data.hit());
            picked_holder_data.push_back(
                    EyePointeeHolderData(*it, data.distance()));
        }
    }

    std::sort(picked_holder_data.begin(), picked_holder_data.end(),
            compareEyePointeeHolderData);

    std::vector<EyePointeeHolder*> picked_holders;
    for (auto it = picked_holder_data.begin(); it != picked_holder_data.end();
            ++it) {
        EyePointeeHolder* holder = it->eye_pointee_holder();
        picked_holders.push_back(holder);
    }

    return picked_holders;
}

std::vector<EyePointeeHolder*> Picker::pickScene(Scene* scene) {
    return Picker::pickScene(scene, 0, 0, 0, 0, 0, -1.0f);
}

float Picker::pickSceneObject(const SceneObject* scene_object,
        const CameraRig* camera_rig) {
    glm::mat4 view_matrix = glm::affineInverse(
            camera_rig->owner_object()->transform()->getModelMatrix());
    if (scene_object->eye_pointee_holder() != 0) {
        EyePointeeHolder* eye_pointee_holder =
                scene_object->eye_pointee_holder();
        if (eye_pointee_holder->enable()) {
            EyePointData data = eye_pointee_holder->isPointed(view_matrix);
            return data.distance();
        }
    }

    return std::numeric_limits<float>::infinity();
}
}
