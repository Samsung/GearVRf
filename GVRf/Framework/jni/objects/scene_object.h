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
 * Objects in a scene.
 ***************************************************************************/

#ifndef SCENE_OBJECT_H_
#define SCENE_OBJECT_H_

#include <algorithm>
#include <vector>
#include <memory>

#include "objects/hybrid_object.h"
#include "objects/components/transform.h"

namespace gvr {
class Camera;
class CameraRig;
class EyePointeeHolder;
class RenderData;

class SceneObject: public HybridObject {
public:
    SceneObject();
    ~SceneObject();

    std::string name() const {
        return name_;
    }

    void set_name(std::string name) {
        name_ = name;
    }

    void attachTransform(SceneObject* self, Transform* transform);
    void detachTransform();

    Transform* transform() const {
        return transform_;
    }

    void attachRenderData(SceneObject* self, RenderData* render_data);
    void detachRenderData();

    RenderData* render_data() const {
        return render_data_;
    }

    void attachCamera(SceneObject* self, Camera* camera);
    void detachCamera();

    Camera* camera() const {
        return camera_;
    }

    void attachCameraRig(SceneObject* self, CameraRig* camera_rig);
    void detachCameraRig();

    CameraRig* camera_rig() const {
        return camera_rig_;
    }

    void attachEyePointeeHolder(SceneObject* self,
            EyePointeeHolder* eye_pointee_holder);
    void detachEyePointeeHolder();

    EyePointeeHolder* eye_pointee_holder() const {
        return eye_pointee_holder_;
    }

    SceneObject* parent() const {
        return parent_;
    }

    const std::vector<SceneObject*>& children() const {
        return children_;
    }

    void addChildObject(SceneObject* self, SceneObject* child);
    void removeChildObject(SceneObject* child);
    int getChildrenCount() const;
    SceneObject* getChildByIndex(int index);

private:
    SceneObject(const SceneObject& scene_object);
    SceneObject(SceneObject&& scene_object);
    SceneObject& operator=(const SceneObject& scene_object);
    SceneObject& operator=(SceneObject&& scene_object);

private:
    std::string name_;
    Transform* transform_;
    RenderData* render_data_;
    Camera* camera_;
    CameraRig* camera_rig_;
    EyePointeeHolder* eye_pointee_holder_;
    SceneObject* parent_;
    std::vector<SceneObject*> children_;
};

}
#endif
