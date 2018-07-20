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

#include <gl/gl_render_data.h>
#include "scene.h"

#include "engine/exporter/exporter.h"
#include "gl/gl_material.h"
#include "objects/components/shadow_map.h"

namespace gvr {

Scene* Scene::main_scene_ = NULL;

Scene::Scene() :
        HybridObject(),
        javaVM_(NULL),
        makeDepthShadersMethod_(0),
        main_camera_rig_(),
        frustum_flag_(false),
        dirtyFlag_(0),
        occlusion_flag_(false),
        pick_visible_(true)

{ }

Scene::~Scene() {
}

void Scene::set_java(JavaVM* javaVM, jobject javaScene)
{
    JNIEnv *env = getCurrentEnv(javaVM);
    javaVM_ = javaVM;
    if (env)
    {
        jclass sceneClass = env->GetObjectClass(javaScene);
        makeDepthShadersMethod_ = env->GetMethodID(sceneClass, "makeDepthShaders", "()V");
        if (makeDepthShadersMethod_ == 0)
        {
            LOGE("Scene::set_java ERROR cannot find 'GVRScene.makeDepthShaders()' Java method");
        }
    }
}

int Scene::get_java_env(JNIEnv** envptr)
{
    JavaVM *javaVM = getJavaVM();
    int rc = javaVM->GetEnv((void **) envptr, JNI_VERSION_1_6);
    if (rc == JNI_EDETACHED)
    {
        if (javaVM->AttachCurrentThread(envptr, NULL) && *envptr)
        {
            return 1;
        }
        FAIL("Scene::get_java_env Could not attach to Java VM");
    }
    else if (rc == JNI_EVERSION)
    {
        FAIL("Scene::get_java_env JNI version not supported");
        return -1;
    }
    return 0;
}


/**
 * Called when the depth shaders for shadow mapping are required.
 * Typically it will only be called once per scene.
 */
void Scene::makeDepthShaders(jobject jscene)
{
    if (makeDepthShadersMethod_ == NULL)
    {
        LOGE("SHADER: Could not call GVRScene::makeDepthShadersMethod_");
    }
    JNIEnv* env = NULL;
    int rc = get_java_env(&env);
    if (env && (rc >= 0))
    {
        env->CallVoidMethod(jscene, makeDepthShadersMethod_);
        if (rc > 0)
        {
            getJavaVM()->DetachCurrentThread();
        }
    }
}

void Scene::addSceneObject(SceneObject* scene_object) {
    scene_root_->addChildObject(scene_root_, scene_object);
}

void Scene::removeSceneObject(SceneObject* scene_object) {
    scene_root_->removeChildObject(scene_object);
}

void Scene::removeAllSceneObjects() {
    scene_root_->clear();
    clearAllColliders();
}


void Scene::clearAllColliders() {
    lockColliders();
    allColliders.clear();
    visibleColliders.clear();
    unlockColliders();
}

void Scene::pick(SceneObject* sceneobj) {
    if (pick_visible_) {
         Collider* collider = static_cast<Collider*>(sceneobj->getComponent(Collider::getComponentType()));
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

/**
 * Called when the main scene is first presented for render.
 */
void Scene::set_main_scene(Scene* scene) {
    main_scene_ = scene;
    scene->getRoot()->onAddedToScene(scene);
}


std::vector<SceneObject*> Scene::getWholeSceneObjects() {
    std::vector<SceneObject*> scene_objects;
    scene_root_->getDescendants(scene_objects);
    return scene_objects;
}

void Scene::exportToFile(std::string filepath) {
    Exporter::writeToFile(this, filepath);
}

bool Scene::addLight(Light* light)
{
    return lights_.addLight(light);
}

bool Scene::removeLight(Light* light)
{
    return lights_.removeLight(light);
}

void Scene::clearLights()
{
    lights_.clear();
}

void Scene::setSceneRoot(SceneObject* sceneRoot) {
    scene_root_ = sceneRoot;
}

}

