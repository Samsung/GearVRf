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

#include "scene.h"

#include "engine/exporter/exporter.h"
#include "objects/scene_object.h"

namespace gvr {
Scene::Scene() :
        HybridObject(), scene_objects_(), main_camera_rig_(), frustum_flag_(
                false), dirtyFlag_(0), occlusion_flag_(false), directional_light_() {
}

Scene::~Scene() {
}

void Scene::addSceneObject(SceneObject* scene_object) {
    scene_objects_.push_back(scene_object);
}

void Scene::removeSceneObject(SceneObject* scene_object) {
    scene_objects_.erase(
            std::remove(scene_objects_.begin(), scene_objects_.end(),
                    scene_object), scene_objects_.end());
}

void Scene::removeAllSceneObjects() {
    scene_objects_.clear();
}

std::vector<SceneObject*> Scene::getWholeSceneObjects() {
    std::vector<SceneObject*> scene_objects(scene_objects_);
    for (int i = 0; i < scene_objects.size(); ++i) {
        std::vector<SceneObject*> childrenCopy = scene_objects[i]->children();
        for (auto it = childrenCopy.begin(); it != childrenCopy.end(); ++it) {
            scene_objects.push_back(*it);
        }
    }

    return scene_objects;
}

void Scene::exportToFile(std::string filepath) {
    Exporter::writeToFile(this, filepath);
}
}
