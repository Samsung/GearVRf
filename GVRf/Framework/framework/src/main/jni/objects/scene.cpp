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

Scene* Scene::main_scene_ = NULL;

Scene::Scene() :
        HybridObject(), scene_objects_(), main_camera_rig_(), frustum_flag_(
                false), dirtyFlag_(0), occlusion_flag_(false), is_shadowmap_invalid(true) {
    if (main_scene() == NULL) {
        set_main_scene(this);
    }
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
    lightList.clear();
    allColliders.clear();
}

void Scene::clearColliders() {
    allColliders.clear();
}

void Scene::gatherColliders() {
    allColliders.clear();
    for (auto it = scene_objects_.begin(); it != scene_objects_.end(); ++it) {
        SceneObject* obj = *it;
        obj->getAllComponents(allColliders, Collider::getComponentType());
    }
}

void Scene::addCollider(Collider* collider) {
    auto it = std::find(allColliders.begin(), allColliders.end(), collider);
    if (it == allColliders.end()) {
        allColliders.push_back(collider);
    }
}

void Scene::removeCollider(Collider* collider) {
    auto it = std::find(allColliders.begin(), allColliders.end(), collider);
    if (it != allColliders.end()) {
        allColliders.erase(it);
    }
}

void Scene::set_main_scene(Scene* scene) {
    main_scene_ = scene;
    scene->gatherColliders();
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

void Scene::addLight(Light* light) {
    auto it = std::find(lightList.begin(), lightList.end(), light);
    if (it != lightList.end())
        return;
     lightList.push_back(light);
}
}

