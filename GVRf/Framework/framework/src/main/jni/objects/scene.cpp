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
        HybridObject(),
        main_camera_rig_(),
        frustum_flag_(false),
        dirtyFlag_(0),
        occlusion_flag_(false),
        pick_visible_(true),
        is_shadowmap_invalid(true) {
    if (main_scene() == NULL) {
        set_main_scene(this);
    }
}

Scene::~Scene() {
}

void Scene::addSceneObject(SceneObject* scene_object) {
    scene_root_.addChildObject(&scene_root_, scene_object);
}

void Scene::removeSceneObject(SceneObject* scene_object) {
    scene_root_.removeChildObject(scene_object);
}

void Scene::removeAllSceneObjects() {
    scene_root_.clear();
    lightList.clear();
    clearAllColliders();
}

void Scene::clearAllColliders() {
    lockColliders();
    allColliders.clear();
    visibleColliders.clear();
    unlockColliders();
}

void Scene::gatherColliders() {
    lockColliders();
    allColliders.clear();
    visibleColliders.clear();
    scene_root_.getAllComponents(allColliders, Collider::getComponentType());
    unlockColliders();
}


void Scene::pick(SceneObject* sceneobj) {
    if (pick_visible_) {
         Collider* collider = reinterpret_cast<Collider*>(sceneobj->getComponent(Collider::getComponentType()));
        if (collider) {
            visibleColliders.push_back(collider);
        }
     }
}

void Scene::addCollider(Collider* collider) {
    auto it = std::find(allColliders.begin(), allColliders.end(), collider);
    if (it == allColliders.end()) {
        lockColliders();
        allColliders.push_back(collider);
        unlockColliders();
    }
}

void Scene::removeCollider(Collider* collider) {
    auto it = std::find(allColliders.begin(), allColliders.end(), collider);
    if (it != allColliders.end()) {
        lockColliders();
        allColliders.erase(it);
        unlockColliders();
    }
}

void Scene::set_main_scene(Scene* scene) {
    main_scene_ = scene;
    scene->gatherColliders();
}


std::vector<SceneObject*> Scene::getWholeSceneObjects() {
    std::vector<SceneObject*> scene_objects;
    scene_root_.getDescendants(scene_objects);
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

