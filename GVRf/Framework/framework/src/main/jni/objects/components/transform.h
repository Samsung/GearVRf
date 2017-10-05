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
 * Containing data about how to position an object.
 ***************************************************************************/

#ifndef TRANSFORM_H_
#define TRANSFORM_H_

#include <mutex>
#include <memory>

#include "glm/glm.hpp"
#include "glm/gtx/quaternion.hpp"
#include "glm/gtc/matrix_transform.hpp"

#include "objects/lazy.h"
#include "component.h"

namespace gvr {

class Transform: public Component {
public:
    Transform();
    virtual ~Transform();

    static long long getComponentType() {
        return COMPONENT_TYPE_TRANSFORM;
    }

    const glm::vec3& position() const {
        return position_;
    }

    float position_x() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return position_.x;
    }

    float position_y() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return position_.y;
    }

    float position_z() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return position_.z;
    }

    void set_position(const glm::vec3& position) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            position_ = position;
        }
        invalidate(false);
    }

    void set_position(float x, float y, float z) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            position_.x = x;
            position_.y = y;
            position_.z = z;
        }
        invalidate(false);
    }

    void set_position_x(float x) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            position_.x = x;
        }
        invalidate(false);
    }

    void set_position_y(float y) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            position_.y = y;
        }
        invalidate(false);
    }

    void set_position_z(float z) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            position_.z = z;
        }
        invalidate(false);
    }

    const glm::quat& rotation() const {
        return rotation_;
    }

    float rotation_w() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return rotation_.w;
    }

    float rotation_x() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return rotation_.x;
    }

    float rotation_y() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return rotation_.y;
    }

    float rotation_z() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return rotation_.z;
    }

// in radians
    float rotation_yaw() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return glm::yaw(rotation_);
    }

// in radians
    float rotation_pitch() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return glm::pitch(rotation_);
    }

// in radians
    float rotation_roll() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return glm::roll(rotation_);
    }

    void set_rotation(float w, float x, float y, float z) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            rotation_.w = w;
            rotation_.x = x;
            rotation_.y = y;
            rotation_.z = z;
        }
        invalidate(true);
    }

    void set_rotation(const glm::quat& rotation) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            rotation_ = rotation;
        }
        invalidate(true);
    }

    const glm::vec3& scale() const {
        return scale_;
    }

    float scale_x() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return scale_.x;
    }

    float scale_y() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return scale_.y;
    }

    float scale_z() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return scale_.z;
    }

    void set_scale(const glm::vec3& scale) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            scale_ = scale;
        }
        invalidate(false);
    }

    void set_scale(float x, float y, float z) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            scale_.x = x;
            scale_.y = y;
            scale_.z = z;
        }
        invalidate(false);
    }

    void set_scale_x(float x) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            scale_.x = x;
        }
        invalidate(false);
    }

    void set_scale_y(float y) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            scale_.y = y;
        }
        invalidate(false);
    }

    void set_scale_z(float z) {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            scale_.z = z;
        }
        invalidate(false);
    }

    bool isModelMatrixValid() {
        std::lock_guard<std::mutex> lock(mutex_);
        return model_matrix_.isValid();
    }

    virtual void onAttach(SceneObject* owner_object);
    virtual void onDetach(SceneObject* owner_object);

    void invalidate();
    void invalidate(bool rotationUpdated);
    glm::mat4 getModelMatrix(bool forceRecalculate = false);
    glm::mat4 getLocalModelMatrix();
    void translate(float x, float y, float z);
    void setRotationByAxis(float angle, float x, float y, float z);
    void rotate(float w, float x, float y, float z);
    void rotateByAxis(float angle, float x, float y, float z);
    void rotateByAxisWithPivot(float angle, float axis_x, float axis_y,
            float axis_z, float pivot_x, float pivot_y, float pivot_z);
    void rotateWithPivot(float w, float x, float y, float z, float pivot_x,
            float pivot_y, float pivot_z);
    void setModelMatrix(glm::mat4 mat);

private:
    Transform(const Transform& transform);
    Transform(Transform&& transform);
    Transform& operator=(const Transform& transform);
    Transform& operator=(Transform&& transform);

private:
    glm::vec3 position_;
    glm::quat rotation_;
    glm::vec3 scale_;

    Lazy<glm::mat4> model_matrix_;

    mutable std::mutex mutex_;
};

}
#endif
