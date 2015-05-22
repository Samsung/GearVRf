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

#include "scene_object.h"

#include "objects/components/camera.h"
#include "objects/components/camera_rig.h"
#include "objects/components/eye_pointee_holder.h"
#include "objects/components/render_data.h"
#include "util/gvr_log.h"

namespace gvr {
SceneObject::SceneObject() :
        HybridObject(), name_(""), transform_(), render_data_(), camera_(), camera_rig_(), eye_pointee_holder_(), parent_(),
        children_(), visible_(true), in_frustum_(false), query_currently_issued_(false), vis_count_(0) {

    // Occlusion query setup
#if _GVRF_USE_GLES3_
    queries_ = new GLuint[1];
    glGenQueries(1, queries_);
#endif
}

SceneObject::~SceneObject() {
#if _GVRF_USE_GLES3_
    delete queries_;
#endif
}

void SceneObject::attachTransform(SceneObject* self, Transform* transform) {
    if (transform_) {
        detachTransform();
    }
    SceneObject* owner_object(transform->owner_object());
    if (owner_object) {
        owner_object->detachRenderData();
    }
    transform_ = transform;
    transform_->set_owner_object(self);
}

void SceneObject::detachTransform() {
    if (transform_) {
        transform_->removeOwnerObject();
        transform_ = NULL;
    }
}

void SceneObject::attachRenderData(SceneObject* self, RenderData* render_data) {
    if (render_data_) {
        detachRenderData();
    }
    SceneObject* owner_object(render_data->owner_object());
    if (owner_object) {
        owner_object->detachRenderData();
    }
    render_data_ = render_data;
    render_data->set_owner_object(self);
}

void SceneObject::detachRenderData() {
    if (render_data_) {
        render_data_->removeOwnerObject();
        render_data_ = NULL;
    }
}

void SceneObject::attachCamera(SceneObject* self, Camera* camera) {
    if (camera_) {
        detachCamera();
    }
    SceneObject* owner_object(camera->owner_object());
    if (owner_object) {
        owner_object->detachCamera();
    }
    camera_ = camera;
    camera_->set_owner_object(self);
}

void SceneObject::detachCamera() {
    if (camera_) {
        camera_->removeOwnerObject();
        camera_ = NULL;
    }
}

void SceneObject::attachCameraRig(SceneObject* self, CameraRig* camera_rig) {
    if (camera_rig_) {
        detachCameraRig();
    }
    SceneObject* owner_object(camera_rig->owner_object());
    if (owner_object) {
        owner_object->detachCameraRig();
    }
    camera_rig_ = camera_rig;
    camera_rig_->set_owner_object(self);
}

void SceneObject::detachCameraRig() {
    if (camera_rig_) {
        camera_rig_->removeOwnerObject();
        camera_rig_ = NULL;
    }
}

void SceneObject::attachEyePointeeHolder(
        SceneObject* self,
        EyePointeeHolder* eye_pointee_holder) {
    if (eye_pointee_holder_) {
        detachEyePointeeHolder();
    }
    SceneObject* owner_object(
            eye_pointee_holder->owner_object());
    if (owner_object) {
        owner_object->detachEyePointeeHolder();
    }
    eye_pointee_holder_ = eye_pointee_holder;
    eye_pointee_holder_->set_owner_object(self);
}

void SceneObject::detachEyePointeeHolder() {
    if (eye_pointee_holder_) {
        eye_pointee_holder_->removeOwnerObject();
        eye_pointee_holder_ = NULL;
    }
}

void SceneObject::addChildObject(SceneObject* self, SceneObject* child) {
    for (SceneObject* parent = parent_; parent; parent = parent->parent_) {
        if (child == parent) {
            std::string error =
                    "SceneObject::addChildObject() : cycle of scene objects is not allowed.";
            LOGE("%s", error.c_str());
            throw error;
        }
    }
    children_.push_back(child);
    child->parent_ = self;
    child->transform()->invalidate();
}

void SceneObject::removeChildObject(SceneObject* child) {
    if (child->parent_ == this) {
        children_.erase(std::remove(children_.begin(), children_.end(), child),
                children_.end());
        child->parent_ = NULL;
    }
}

int SceneObject::getChildrenCount() const {
    return children_.size();
}

SceneObject* SceneObject::getChildByIndex(int index) {
    if (index < children_.size()) {
        return children_[index];
    } else {
        std::string error = "SceneObject::getChildByIndex() : Out of index.";
        throw error;
    }
}

void SceneObject::set_visible(bool visibility=true) {

	//HACK
	//If checked every frame, queries may return
	//an inconsistent result when used with bounding boxes.

	//We need to make sure that the object's visibility status is consistent before
	//changing the status to avoid flickering artifacts.

	if(visibility == true)
		vis_count_++;
	else
		vis_count_--;

	if(vis_count_>check_frames_) {
		visible_ = true;
		vis_count_= 0;
	}
	else if(vis_count_<(-1*check_frames_)) {
		visible_ = false;
		vis_count_= 0;
	}
}

}
