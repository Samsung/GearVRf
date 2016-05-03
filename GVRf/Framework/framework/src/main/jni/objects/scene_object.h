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
#include <mutex>

#include "objects/hybrid_object.h"
#include "objects/components/transform.h"
#include "objects/bounding_volume.h"
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

    void set_in_frustum(bool in_frustum = true) {
        in_frustum_ = in_frustum;
    }

    bool in_frustum() const {
        return in_frustum_;
    }

    void set_visible(bool visibility);
    bool visible() const {
        return visible_;
    }

    void set_query_issued(bool issued = true) {
        query_currently_issued_ = issued;
    }

    bool is_query_issued() {
        return query_currently_issued_;
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

    std::vector<SceneObject*> children() {
        std::lock_guard < std::mutex > lock(children_mutex_);
        return std::vector<SceneObject*>(children_);
    }

    void addChildObject(SceneObject* self, SceneObject* child);
    void removeChildObject(SceneObject* child);
    int getChildrenCount() const;
    SceneObject* getChildByIndex(int index);
    GLuint *get_occlusion_array() {
        return queries_;
    }
    bool isColliding(SceneObject* scene_object);
    bool intersectsBoundingVolume(float rox, float roy, float roz, float rdx,
            float rdy, float rdz);

    void setLODRange(float minRange, float maxRange) {
        lod_min_range_ = minRange * minRange;
        lod_max_range_ = maxRange * maxRange;
        using_lod_ = true;
    }

    float getLODMinRange() {
        return lod_min_range_;
    }

    float getLODMaxRange() {
        return lod_max_range_;
    }

    bool inLODRange(float distance_from_camera) {
        if (!using_lod_) {
            return true;
        }
        if (distance_from_camera >= lod_min_range_
                && distance_from_camera < lod_max_range_) {
            return true;
        }
        return false;
    }

    void dirtyHierarchicalBoundingVolume();
    BoundingVolume& getBoundingVolume();

    int frustumCull(Camera *camera, const float frustum[6][4], int& planeMask);

private:
    std::string name_;
    Transform* transform_ = nullptr;
    RenderData* render_data_ = nullptr;
    Camera* camera_ = nullptr;
    CameraRig* camera_rig_ = nullptr;
    EyePointeeHolder* eye_pointee_holder_ = nullptr;
    SceneObject* parent_ = nullptr;
    std::vector<SceneObject*> children_;
    float lod_min_range_;
    float lod_max_range_;
    bool using_lod_;

    BoundingVolume transformed_bounding_volume_;
    bool bounding_volume_dirty_;
    BoundingVolume mesh_bounding_volume;

    //Flags to check for visibility of a node and
    //whether there are any pending occlusion queries on it
    const int check_frames_ = 12;
    int vis_count_;
    bool visible_;
    bool in_frustum_;
    bool query_currently_issued_;
    GLuint *queries_ = nullptr;

    SceneObject(const SceneObject& scene_object);
    SceneObject(SceneObject&& scene_object);
    SceneObject& operator=(const SceneObject& scene_object);
    SceneObject& operator=(SceneObject&& scene_object);

    bool checkSphereVsFrustum(float frustum[6][4], BoundingVolume &sphere);

    int checkAABBVsFrustumOpt(const float frustum[6][4],
            BoundingVolume &bounding_volume, int& planeMask);

    bool checkAABBVsFrustumBasic(const float frustum[6][4],
            BoundingVolume &bounding_volume);

    std::mutex children_mutex_;
};

}
#endif
