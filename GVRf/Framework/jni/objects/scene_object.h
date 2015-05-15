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
#include "util/gvr_gl.h"

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

    void set_in_frustum(bool in_frustum=true) {
            in_frustum_ = in_frustum;
    }

    bool in_frustum()   const {
        return in_frustum_;
    }

    void set_visible(bool visibility=true) {
        visible_ = visibility;
    }

    bool visible()  const {
        return visible_;
    }

    void set_query_issued(bool issued=true) {
        query_currently_issued_ = issued;
    }

    bool is_query_issued() {
        return query_currently_issued_;
    }

    void attachTransform(const std::shared_ptr<SceneObject>& self,
            const std::shared_ptr<Transform>& transform);
    void detachTransform();

    std::shared_ptr<Transform> transform() const {
        return transform_;
    }

    void attachRenderData(const std::shared_ptr<SceneObject>& self,
            const std::shared_ptr<RenderData>& render_data);
    void detachRenderData();

    std::shared_ptr<RenderData> render_data() const {
        return render_data_;
    }

    void attachCamera(const std::shared_ptr<SceneObject>& self,
            const std::shared_ptr<Camera>& camera);
    void detachCamera();

    std::shared_ptr<Camera> camera() const {
        return camera_;
    }

    void attachCameraRig(const std::shared_ptr<SceneObject>& self,
            const std::shared_ptr<CameraRig>& camera_rig);
    void detachCameraRig();

    std::shared_ptr<CameraRig> camera_rig() const {
        return camera_rig_;
    }

    void attachEyePointeeHolder(const std::shared_ptr<SceneObject>& self,
            const std::shared_ptr<EyePointeeHolder>& eye_pointee_holder);
    void detachEyePointeeHolder();

    std::shared_ptr<EyePointeeHolder> eye_pointee_holder() const {
        return eye_pointee_holder_;
    }

    std::shared_ptr<SceneObject> parent() const {
        return parent_.lock();
    }

    std::vector<std::shared_ptr<SceneObject>> children() const {
        return children_;
    }

    void addChildObject(std::shared_ptr<SceneObject> self,
            std::shared_ptr<SceneObject> child);
    void removeChildObject(std::shared_ptr<SceneObject> child);
    int getChildrenCount() const;
    const std::shared_ptr<SceneObject>& getChildByIndex(int index);
    GLuint *get_occlusion_array(){ return queries_;}

private:
    SceneObject(const SceneObject& scene_object);
    SceneObject(SceneObject&& scene_object);
    SceneObject& operator=(const SceneObject& scene_object);
    SceneObject& operator=(SceneObject&& scene_object);

private:
    std::string name_;
    std::shared_ptr<Transform> transform_;
    std::shared_ptr<RenderData> render_data_;
    std::shared_ptr<Camera> camera_;
    std::shared_ptr<CameraRig> camera_rig_;
    std::shared_ptr<EyePointeeHolder> eye_pointee_holder_;
    std::weak_ptr<SceneObject> parent_;
    std::vector<std::shared_ptr<SceneObject>> children_;

    //Flags to check for visibility of a node and
    //whether there are any pending occlusion queries on it
    bool visible_;
    bool in_frustum_;
    bool query_currently_issued_;
    GLuint *queries_;
};

}
#endif
