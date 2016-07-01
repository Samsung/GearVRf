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
#include "objects/components/render_data.h"
#include "objects/components/transform.h"
#include "objects/components/camera.h"
#include "objects/components/camera_rig.h"
#include "objects/components/collider.h"
#include "objects/bounding_volume.h"
#include "util/gvr_gl.h"

namespace gvr {
class Camera;
class CameraRig;

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

    bool enabled() const {
        return enabled_;
    }

    void set_enable(bool enable) {
        enabled_ = enable;
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

    bool attachComponent(Component* component);
    bool detachComponent(Component* component);
    Component* detachComponent(long long type);
    Component* getComponent(long long type) const;
    void getAllComponents(std::vector<Component*>& components, long long type);

    Transform* transform() const {
        return (Transform*) getComponent(Transform::getComponentType());
    }

    RenderData* render_data() const {
         return (RenderData*) getComponent(RenderData::getComponentType());
    }

    Camera* camera() const {
        return (Camera*) getComponent(Camera::getComponentType());
    }

    CameraRig* camera_rig() const {
        return (CameraRig*) getComponent(CameraRig::getComponentType());
    }

    Collider* collider() const {
        return (Collider*) getComponent(Collider::getComponentType());
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
    void getDescendants(std::vector<SceneObject*>& descendants);
    void clear();
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

    int frustumCull(glm::vec3 camera_position, const float frustum[6][4], int& planeMask);

private:
    std::string name_;
    std::vector<Component*> components_;
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
    bool enabled_;
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
