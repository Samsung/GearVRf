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

#ifndef SCENE_H_
#define SCENE_H_

#include <memory>
#include <vector>
#include <mutex>

#include "objects/hybrid_object.h"
#include "objects/shader_data.h"
#include "components/camera_rig.h"
#include "engine/renderer/renderer.h"
#include "objects/light.h"


namespace gvr {

class Scene: public HybridObject {
public:
    const int MAX_LIGHTS = 16;
    Scene();
    virtual ~Scene();
    void set_java(JavaVM* javaVM, jobject javaScene);
    SceneObject* getRoot() { return &scene_root_; }
    void addSceneObject(SceneObject* scene_object);
    void removeSceneObject(SceneObject* scene_object);
    void removeAllSceneObjects();
    void deleteLightsAndDepthTextureOnRenderThread();

    const CameraRig* main_camera_rig() {
        return main_camera_rig_;
    }
    void set_main_camera_rig(CameraRig* camera_rig) {
        main_camera_rig_ = camera_rig;
    }
    std::vector<SceneObject*> getWholeSceneObjects();

    void set_frustum_culling( bool frustum_flag){ frustum_flag_ = frustum_flag; }
    bool get_frustum_culling(){ return frustum_flag_; }

    void set_occlusion_culling( bool occlusion_flag){ occlusion_flag_ = occlusion_flag; }
    bool get_occlusion_culling(){ return occlusion_flag_; }

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
    void clearLights();

    /*
     * Spawn a Java task in the framework thread to regenerate all the
     * shaders which depend on light sources.
     */
    void bindShaders();

    void resetStats() {
        gRenderer = Renderer::getInstance();
        gRenderer->resetStats();

    }
    int getNumberDrawCalls() {
        if(nullptr!= gRenderer){
            return gRenderer->getNumberDrawCalls();
        }
    }
    int getNumberTriangles() {
        if(nullptr!= gRenderer) {
            return gRenderer->getNumberTriangles();
        }
    }

    void exportToFile(std::string filepath);

    const std::vector<Light*>& getLightList() const {
        return lightList;
    }

    /*
     * If set to true only visible objects will be pickable.
     * Otherwise, all objects are pickable.
     * Enabling this feature incurs a small amount of overhead
     * during culling to gather the visible colliders.
     */
    void setPickVisible(bool pickflag) { pick_visible_ = pickflag; }

    /*
     * Returns true if only visible objects are picked.
     */
    bool getPickVisible() const { return pick_visible_; }

    /*
     * Add a collider to the internal collider list.
     * This list is used to optimize picking by only
     * searching the pickable objects.
     * Colliders are added to this list when attached
     * to a scene object.
     */
    void addCollider(Collider* collider);

    /*
     * Remove a collider from the internal collider list.
     * Colliders are removed from the list when detached
     * from a scene object.
     */
    void removeCollider(Collider* collider);

    /*
     * Clear the visible collider list.
     * This list is constructed every frame during culling
     * to contain only the pickable objects that are visible.
     * This function does not lock the collider list!
     */
    void clearVisibleColliders() { visibleColliders.clear(); }

    /*
     * Called during culling to add a scene object's
     * collider to the visible collider list.
     */
    void pick(SceneObject* sceneobj);

    /*
     * Get the current collider list and lock it.
     * If set_pick_visible is set the visible collider list
     * is returned. Otherwise the list of all colliders is returned.
     * You should call unlockColliders after you are done with the list.
     */
    const std::vector<Component*> lockColliders() {
        collider_mutex_.lock();
        return pick_visible_ ? visibleColliders : allColliders;
    }

    /*
     * Unlock the collider list.
     * Don't call this unless you have called lockColliders first.
     */
    void unlockColliders() {
        collider_mutex_.unlock();
    }

    JavaVM* getJavaVM() const { return javaVM_; }
    

    /**
     * @return new local reference to the java object or nullptr if the object has been collected
     */
    jobject getJavaObj(JNIEnv& env) const { return env.NewLocalRef(javaObj_); }

    int get_java_env(JNIEnv** envptr);

    static Scene* main_scene() {
        return main_scene_;
    }

    static void set_main_scene(Scene* scene);

    void detach_java_env() {
        javaVM_->DetachCurrentThread();
    }

private:
    Scene(const Scene& scene) = delete;
    Scene(Scene&& scene) = delete;
    Scene& operator=(const Scene& scene) = delete;
    Scene& operator=(Scene&& scene) = delete;
    void clearAllColliders();


private:
    static Scene* main_scene_;
    JavaVM* javaVM_;
    jobject javaObj_;
    jmethodID bindShadersMethod_;
    SceneObject scene_root_;
    CameraRig* main_camera_rig_;
    int dirtyFlag_;
    bool frustum_flag_;
    bool occlusion_flag_;
    bool pick_visible_;
    std::mutex collider_mutex_;
    std::vector<Light*> lightList;
    std::vector<Component*> allColliders;
    std::vector<Component*> visibleColliders;
};

}
#endif
