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

#include <memory>

#include "glm/glm.hpp"
#include "glm/gtx/quaternion.hpp"
#include "glm/gtc/matrix_transform.hpp"

#include "objects/lazy.h"
#include "objects/components/component.h"

namespace gvr {
class Transform: public Component {
public:
    Transform();
    virtual ~Transform();

    static long long getComponentType() {
        return (long long) &getComponentType;
    }

    const glm::vec3& position() const {
        return position_;
    }

    float position_x() const {
        return position_.x;
    }

    float position_y() const {
        return position_.y;
    }

    float position_z() const {
        return position_.z;
    }

    void set_position(const glm::vec3& position) {
        position_ = position;
        invalidate(false);
    }

    void set_position(float x, float y, float z) {
        position_.x = x;
        position_.y = y;
        position_.z = z;
        invalidate(false);
    }

    void set_position_x(float x) {
        position_.x = x;
        invalidate(false);
    }

    void set_position_y(float y) {
        position_.y = y;
        invalidate(false);
    }

    void set_position_z(float z) {
        position_.z = z;
        invalidate(false);
    }

    const glm::quat& rotation() const {
        return rotation_;
    }

    float rotation_w() const {
        return rotation_.w;
    }

    float rotation_x() const {
        return rotation_.x;
    }

    float rotation_y() const {
        return rotation_.y;
    }

    float rotation_z() const {
        return rotation_.z;
    }

    // in radians
    float rotation_yaw() const {
        return glm::yaw(rotation_);
    }

    // in radians
    float rotation_pitch() const {
        return glm::pitch(rotation_);
    }

    // in radians
    float rotation_roll() const {
        return glm::roll(rotation_);
    }

    void set_rotation(float w, float x, float y, float z) {
        rotation_.w = w;
        rotation_.x = x;
        rotation_.y = y;
        rotation_.z = z;
        invalidate(true);
    }

    void set_rotation(const glm::quat& roation) {
        rotation_ = roation;
        invalidate(true);
    }

    const glm::vec3& scale() const {
        return scale_;
    }

    float scale_x() const {
        return scale_.x;
    }

    float scale_y() const {
        return scale_.y;
    }

    float scale_z() const {
        return scale_.z;
    }

    void set_scale(const glm::vec3& scale) {
        scale_ = scale;
        invalidate(false);
    }

    void set_scale(float x, float y, float z) {
        scale_.x = x;
        scale_.y = y;
        scale_.z = z;
        invalidate(false);
    }

    void set_scale_x(float x) {
        scale_.x = x;
        invalidate(false);
    }

    void set_scale_y(float y) {
        scale_.y = y;
        invalidate(false);
    }

    void set_scale_z(float z) {
        scale_.z = z;
        invalidate(false);
    }

    bool isModelMatrixValid() {
        return model_matrix_.isValid();
    }

    void invalidate(bool rotationUpdated);
    glm::mat4 getModelMatrix();
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
};

}
#endif
