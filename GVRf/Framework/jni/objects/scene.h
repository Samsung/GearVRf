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


#include "objects/hybrid_object.h"
#include "components/camera_rig.h"
#include "engine/renderer/renderer.h"

#include "objects/components/directional_light.h"

namespace gvr {
class SceneObject;

class Scene: public HybridObject {
public:
    Scene();
    virtual ~Scene();
    void addSceneObject(SceneObject* scene_object);
    void removeSceneObject(SceneObject* scene_object);
    void removeAllSceneObjects();
    const std::vector<SceneObject*>& scene_objects() {
        return scene_objects_;
    }
    const CameraRig* main_camera_rig() {
        return main_camera_rig_;
    }
    void set_main_camera_rig(CameraRig* camera_rig) {
        main_camera_rig_ = camera_rig;
    }
    std::vector<SceneObject*> getWholeSceneObjects();

    int getSceneDirtyFlag() { return 1 || dirtyFlag_;  /* force to be true */}
    void setSceneDirtyFlag(int dirtyBits) { dirtyFlag_ |= dirtyBits; }

    void set_frustum_culling( bool frustum_flag){ frustum_flag_ = frustum_flag; }
    bool get_frustum_culling(){ return frustum_flag_; }

    void set_occlusion_culling( bool occlusion_flag){ occlusion_flag_ = occlusion_flag; }
    bool get_occlusion_culling(){ return occlusion_flag_; }

    void resetStats() {
        if (!statsInitialized) {
            Renderer::initializeStats();
            statsInitialized = true;
        }
        Renderer::resetStats();
    }
    int getNumberDrawCalls() {
        return Renderer::getNumberDrawCalls();
    }
    int getNumberTriangles() {
        return Renderer::getNumberTriangles();
    }

    void exportToFile(std::string filepath);

    void attachDirectionalLight(DirectionalLight* directional_light) {
        directional_light_ = directional_light;
    }

    DirectionalLight* getDirectionalLight() const {
        return directional_light_;
    }

private:
    Scene(const Scene& scene);
    Scene(Scene&& scene);
    Scene& operator=(const Scene& scene);
    Scene& operator=(Scene&& scene);

private:
    std::vector<SceneObject*> scene_objects_;
    CameraRig* main_camera_rig_;

    DirectionalLight* directional_light_;

    int dirtyFlag_;
    bool frustum_flag_;
    bool occlusion_flag_;
    bool statsInitialized = false;

};

}
#endif
