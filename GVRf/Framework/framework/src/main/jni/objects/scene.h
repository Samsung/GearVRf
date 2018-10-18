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
#include "objects/lightlist.h"


namespace gvr {

class Light;
class Collider;

/**
 * Manages the scene graph and global properties such as
 * the camera rig, light sources, picking and colliders.
 * An application may have more than one scene and scene objects
 * can be shared between scenes.
 * @see SceneObject
 * @see Collider
 * @see Light
 */
class Scene: public HybridObject {
public:
    static const int MAX_LIGHTS = 16;
    Scene();
    virtual ~Scene();

    /**
     * Get the topmost root of the scene graph.
     * This node cannot be removed.
     * @return root of scene graph.
     */
    SceneObject* getRoot() { return &scene_root_; }

    /**
     * Add a scene object underneath the root.
     * @param scene_object SceneObject to add.
     * @see #removeSceneObject
     */
    void addSceneObject(SceneObject* scene_object);

    /**
     * Remove a scene object from the root.
     * @param scene_object SceneObject to remove.
     * @see #addSceneObject
     */
    void removeSceneObject(SceneObject* scene_object);

    /**
     * Remove all of the scene objects added by the application.
     * The camera rig and cursor will remain.
     * @see #removeSceneObject
     */
    void removeAllSceneObjects();

    /**
     * Get the main camera rig for the scene.
     * The scene objects within the scene may have Camera
     * components attached. The main CameraRig is attached to
     * a scene object underneath the root.
     * @return CameraRig for main camera.
     * @see #set_main_camera_rig
     */
    const CameraRig* main_camera_rig()
    {
        return main_camera_rig_;
    }

    /**
     * Set the main camera rig for the scene.
     * The main CameraRig is attached to
     * a scene object underneath the root.
     * @return CameraRig for main camera.
     * @see #main_camera_rig
     */
    void set_main_camera_rig(CameraRig* camera_rig)
    {
        main_camera_rig_ = camera_rig;
    }

    /**
     * Get a vector with all of the scene objects.
     * This function is ridiculously inefficient
     * and should be avoided.
     * @return vector with all the scene objects.
     */
    std::vector<SceneObject*> getWholeSceneObjects();

    /**
     * Enable or disable view frustum culling.
     * When culling is enabled, GearVRF does not render
     * scene objects outside the camera view frustum.
     * @param flag  true to enable frustum culling, false to disabled
     * @see #get_frustum_culling
     */
    void set_frustum_culling( bool flag)
    {
        frustum_flag_ = flag;
    }

    /**
     * Determine whether view frustum culling is enabled.
     * @return true if enabled, false if not
     * @see #set_frustum_culling
     */
    bool get_frustum_culling(){ return frustum_flag_; }

    /**
     * Enable or disable view occlusion culling.
     * When occlusion culling is enabled, GearVRF tries to
     * not render objects that are obscured by other objects.
     * @param frustum_flag true to enable frustum culling, false to disabled
     * @see #get_occlusion_culling
     */
    void set_occlusion_culling(bool flag)
    {
        occlusion_flag_ = flag;
    }

    /**
     * Determine whether occlusion culling is enabled.
     * @return true if enabled, false if not
     * @see #set_occlusion_culling
     */
    bool get_occlusion_culling(){ return occlusion_flag_; }

    /*
     * Adds a new light to the scene.
     * @return true if light was added, false if already there or too many lights.
     * @see #removeLight
     */
    bool addLight(Light* light);

    /*
     * Removes an existing light from the scene.
     * @return true if light was removed, false if light was not in the scene.
     * @see #addLight
     */
    bool removeLight(Light* light);

    /*
     * Removes all the lights from the scene.
     * @see #addLight
     * @see #removeLight
     */
    void clearLights();

    /*
     * Executes a Java function which generates the
     * depth shaders for shadow mapping.
     */
    bool makeDepthShaders(Renderer* renderer, jobject jscene);

    /**
     * Reset draw call and triangle rendering statistics.
     */
    void resetStats()
    {
        gRenderer = Renderer::getInstance();
        gRenderer->resetStats();

    }

    /**
     * Get the number of draw calls made so far in the current frame.
     * @return count of draw calls
     */
    int getNumberDrawCalls()
    {
        if (gRenderer)
        {
            return gRenderer->getNumberDrawCalls();
        }
        return 0;
    }

    /**
     * Get the number of triangles rendered so far in the current frame.
     * @return count of draw triangles rendered
     */
    int getNumberTriangles()
    {
        if (gRenderer)
        {
            return gRenderer->getNumberTriangles();
        }
        return 0;
    }

    void exportToFile(std::string filepath);

    /**
     * Get the list of lights used by this scene.
     * @return LightList
     */
    const LightList& getLights() const
    {
        return lights_;
    }

    /**
     * Get the list of lights used by this scene.
     * @return LightList
     */
    LightList& getLights()
    {
        return lights_;
    }

    /*
     * If set to true only visible objects will be pickable.
     * Otherwise, all objects are pickable.
     * Enabling this feature incurs a small amount of overhead
     * during culling to gather the visible colliders.
     * @see #getPickVisible
     * @see #addCollider
     * @see #removeCollider
     */
    void setPickVisible(bool pickflag) { pick_visible_ = pickflag; }

    /*
     * Returns true if only visible objects are picked.
     * @see #setPickVisible
     * @see #addCollider
     * @see #removeCollider
     */
    bool getPickVisible() const { return pick_visible_; }

    /*
     * Add a collider to the internal collider list.
     * This list is used to optimize picking by only
     * searching the pickable objects.
     * Colliders are added to this list when attached
     * to a scene object.
     * @see #setPickVisible
     * @see #removeCollider
    */
    void addCollider(Collider* collider);

    /*
     * Remove a collider from the internal collider list.
     * Colliders are removed from the list when detached
     * from a scene object.
     * @see #setPickVisible
     * @see #addCollider
    */
    void removeCollider(Collider* collider);

    /*
     * Clear the visible collider list.
     * This list is constructed every frame during culling
     * to contain only the pickable objects that are visible.
     * This function does not lock the collider list!
     * @see #setPickVisible
     * @see #addCollider
     * @see #removeCollider
     */
    void clearVisibleColliders() { visibleColliders.clear(); }

    /*
     * Called during culling to add a scene object's
     * collider to the visible collider list.
     * @see #setPickVisible
     * @see #addCollider
     * @see #removeCollider
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
    void unlockColliders()
    {
        collider_mutex_.unlock();
    }

    /**
     * Get the current main scene (the one being rendered
     * in stereo to both eyes).
     * @return main Scene object
     * @see #set_main_scene
     */
    static Scene* main_scene()
    {
        return main_scene_;
    }

    /**
     * Set the current main scene (the one being rendered
     * in stereo to both eyes).
     * @param new main scene
     * @see #get_main_scene
     */
    static void set_main_scene(Scene* scene);

private:
    Scene(const Scene& scene) = delete;
    Scene(Scene&& scene) = delete;
    Scene& operator=(const Scene& scene) = delete;
    Scene& operator=(Scene&& scene) = delete;

    /*
     * Clear the entire collider list.
     * @see #setPickVisible
     * @see #addCollider
     * @see #removeCollider
     */
    void clearAllColliders();

private:
    static Scene* main_scene_;
    SceneObject scene_root_;
    CameraRig* main_camera_rig_;
    bool frustum_flag_;
    bool occlusion_flag_;
    bool pick_visible_;
    jmethodID makeDepthShadersMethod_;
    std::mutex collider_mutex_;
    LightList lights_;
    std::vector<Component*> allColliders;
    std::vector<Component*> visibleColliders;
};

}
#endif
