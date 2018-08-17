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

#include "transform.h"

#include "glm/gtc/type_ptr.hpp"

#include "objects/scene_object.h"
#include <math.h>
#include <glm/gtx/matrix_decompose.hpp>

namespace gvr {

Transform::Transform() :
        Component(Transform::getComponentType()), position_(glm::vec3(0.0f, 0.0f, 0.0f)),
        rotation_(
                glm::quat(1.0f, 0.0f, 0.0f, 0.0f)), scale_(
        glm::vec3(1.0f, 1.0f, 1.0f)), model_matrix_(
        Lazy<glm::mat4>(glm::mat4())) {
}

Transform::~Transform() {
    if(owner_object_) {
        owner_object_->onTransformChanged();
    }
}

void Transform::invalidate()
{
    mutex_.lock();
    model_matrix_.invalidate();
    mutex_.unlock();
}

void Transform::invalidate(bool rotationUpdated)
{
    SceneObject* owner = owner_object();

    invalidate();
    if (rotationUpdated)
    {
        // scale rotation_ if needed to avoid overflow
        static const float threshold = sqrt(FLT_MAX) / 2.0f;
        static const float scale_factor = 0.5f / sqrt(FLT_MAX);
        mutex_.lock();
        if (rotation_.w > threshold || rotation_.x > threshold ||
            rotation_.y > threshold || rotation_.z > threshold)
        {
            rotation_.w *= scale_factor;
            rotation_.x *= scale_factor;
            rotation_.y *= scale_factor;
            rotation_.z *= scale_factor;
        }
        mutex_.unlock();
    }
    if (owner)
    {
        owner->onTransformChanged();
//        owner->dirtyHierarchicalBoundingVolume();
    }
}

glm::mat4 Transform::getModelMatrix(bool forceRecalculate) {
    if (!isModelMatrixValid() || forceRecalculate) {
        mutex_.lock();
        glm::mat4 translation_matrix = glm::translate(glm::mat4(), position_);
        glm::mat4 rotation_matrix = glm::mat4_cast(rotation_);
        glm::mat4 scale_matrix = glm::scale(glm::mat4(), scale_);
        mutex_.unlock();

        glm::mat4 trs_matrix = translation_matrix * rotation_matrix * scale_matrix;
        SceneObject* owner = owner_object();
        if (nullptr != owner && nullptr != owner->parent()) {
            Transform *const t = owner->parent()->transform();
            if (nullptr != t) {
                glm::mat4 model_matrix = t->getModelMatrix() * trs_matrix;
                mutex_.lock();
                model_matrix_.validate(model_matrix);
                mutex_.unlock();
            }
        } else {
            mutex_.lock();
            model_matrix_.validate(trs_matrix);
            mutex_.unlock();
        }
    }
    mutex_.lock();
    glm::mat4 elem = model_matrix_.element();
    mutex_.unlock();
    return elem;
}

glm::mat4 Transform::getLocalModelMatrix() {
    mutex_.lock();
    glm::mat4 translation_matrix = glm::translate(glm::mat4(), position_);
    glm::mat4 rotation_matrix = glm::mat4_cast(rotation_);
    glm::mat4 scale_matrix = glm::scale(glm::mat4(), scale_);
    mutex_.unlock();
    glm::mat4 trs_matrix = translation_matrix * rotation_matrix
                           * scale_matrix;
    return trs_matrix;
}

void Transform::setModelMatrix(glm::mat4 matrix) {
    glm::vec3 scale;
    glm::quat rotation;
    glm::vec3 translation;
    glm::vec3 skew;
    glm::vec4 perspective;
    glm::decompose(matrix, scale, rotation, translation, skew, perspective);

    mutex_.lock();
    position_ = translation;
    scale_ = scale;
    rotation_ = glm::conjugate(rotation);
    mutex_.unlock();
    invalidate(true);
}

void Transform::translate(float x, float y, float z) {
    mutex_.lock();
    position_ += glm::vec3(x, y, z);
    mutex_.unlock();
    invalidate(false);
}

// angle in radians
void Transform::setRotationByAxis(float angle, float x, float y, float z) {
    mutex_.lock();
    rotation_ = glm::angleAxis(angle, glm::vec3(x, y, z));
    mutex_.unlock();
    invalidate(true);
}

void Transform::rotate(float w, float x, float y, float z) {
    mutex_.lock();
    rotation_ = glm::quat(w, x, y, z) * rotation_;
    mutex_.unlock();
    invalidate(true);
}

// angle in radians
void Transform::rotateByAxis(float angle, float x, float y, float z) {
    mutex_.lock();
    rotation_ = glm::angleAxis(angle, glm::vec3(x, y, z)) * rotation_;
    mutex_.unlock();
    invalidate(true);
}

// angle in radians
void Transform::rotateByAxisWithPivot(float angle, float axis_x, float axis_y,
                                      float axis_z, float pivot_x, float pivot_y,
                                      float pivot_z) {
    glm::quat axis_rotation = glm::angleAxis(angle,
                                             glm::vec3(axis_x, axis_y, axis_z));
    mutex_.lock();
    rotation_ = axis_rotation * rotation_;
    glm::vec3 pivot(pivot_x, pivot_y, pivot_z);
    glm::vec3 relative_position = position_ - pivot;
    relative_position = glm::rotate(axis_rotation, relative_position);
    position_ = relative_position + pivot;
    mutex_.unlock();
    invalidate(true);
}

void Transform::rotateWithPivot(float w, float x, float y, float z,
                                float pivot_x, float pivot_y, float pivot_z) {
    glm::quat rotation(w, x, y, z);
    mutex_.lock();
    rotation_ = rotation * rotation_;
    glm::vec3 pivot(pivot_x, pivot_y, pivot_z);
    glm::vec3 relative_position = position_ - pivot;
    relative_position = glm::rotate(rotation, relative_position);
    position_ = relative_position + pivot;
    mutex_.unlock();
    invalidate(true);
}

void Transform::onAttach(SceneObject *owner_object) {
    owner_object->onTransformChanged();
//    owner_object->dirtyHierarchicalBoundingVolume();
}

void Transform::onDetach(SceneObject *owner_object) {
    owner_object->onTransformChanged();
//    owner_object->dirtyHierarchicalBoundingVolume();
}
}
