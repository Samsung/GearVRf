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
namespace gvr {

Transform::Transform() :
        Component(Transform::getComponentType()), position_(glm::vec3(0.0f, 0.0f, 0.0f)), rotation_(
                glm::quat(1.0f, 0.0f, 0.0f, 0.0f)), scale_(
                glm::vec3(1.0f, 1.0f, 1.0f)), model_matrix_(
                Lazy<glm::mat4>(glm::mat4())) {
}

Transform::~Transform() {
}

void Transform::invalidate(bool rotationUpdated) {
    if (model_matrix_.isValid()) {
        model_matrix_.invalidate();
        std::vector<SceneObject*> childrenCopy = owner_object()->children();
        for (auto it = childrenCopy.begin(); it != childrenCopy.end(); ++it) {
            Transform* const t = (*it)->transform();
            if (nullptr != t) {
                t->invalidate(false);
            }
        }
    }
    if (rotationUpdated) {
        // scale rotation_ if needed to avoid overflow
        static const float threshold = sqrt(FLT_MAX) / 2.0f;
        static const float scale_factor = 0.5f / sqrt(FLT_MAX);
        if (rotation_.w > threshold || rotation_.x > threshold
                || rotation_.y > threshold || rotation_.z > threshold) {
            rotation_.w *= scale_factor;
            rotation_.x *= scale_factor;
            rotation_.y *= scale_factor;
            rotation_.z *= scale_factor;
        }
    }

    if(owner_object()) {
        owner_object()->dirtyHierarchicalBoundingVolume();
    }
}

glm::mat4 Transform::getModelMatrix() {
    if (!model_matrix_.isValid()) {
        glm::mat4 translation_matrix = glm::translate(glm::mat4(), position_);
        glm::mat4 rotation_matrix = glm::mat4_cast(rotation_);
        glm::mat4 scale_matrix = glm::scale(glm::mat4(), scale_);

        glm::mat4 trs_matrix = translation_matrix * rotation_matrix
                * scale_matrix;
        if (owner_object()->parent() != 0) {
            Transform* const t = owner_object()->parent()->transform();
            if (nullptr != t) {
                glm::mat4 model_matrix = t->getModelMatrix() * trs_matrix;
                model_matrix_.validate(model_matrix);
            }
        } else {
            model_matrix_.validate(trs_matrix);
        }

    }
    return model_matrix_.element();
}

glm::mat4 Transform::getLocalModelMatrix() {
    glm::mat4 translation_matrix = glm::translate(glm::mat4(), position_);
    glm::mat4 rotation_matrix = glm::mat4_cast(rotation_);
    glm::mat4 scale_matrix = glm::scale(glm::mat4(), scale_);
    glm::mat4 trs_matrix = translation_matrix * rotation_matrix
            * scale_matrix;
    return trs_matrix;
}

void Transform::setModelMatrix(glm::mat4 matrix) {

	glm::vec3 new_position(matrix[3][0], matrix[3][1], matrix[3][2]);

    glm::vec3 Xaxis(matrix[0][0],matrix[0][1],matrix[0][2]);
    glm::vec3 Yaxis(matrix[1][0],matrix[1][1],matrix[1][2]);
    glm::vec3 Zaxis(matrix[2][0],matrix[2][1],matrix[2][2]);

    double zs=glm::dot(glm::cross(Xaxis,Yaxis),Zaxis);
    double ys=glm::dot(glm::cross(Zaxis,Xaxis),Yaxis);
    double xs=glm::dot(glm::cross(Yaxis,Zaxis),Xaxis);


    xs=std::signbit(xs);
    ys=std::signbit(ys);
    zs=std::signbit(zs);

    xs =(xs > 0.0 ? -1 :1);
    ys =(ys > 0.0 ? -1 :1);
    zs =(zs > 0.0 ? -1 :1);

    glm::vec3 new_scale;
    new_scale.x = xs* glm::sqrt(
                    matrix[0][0] * matrix[0][0] + matrix[0][1] * matrix[0][1]
                            + matrix[0][2] * matrix[0][2]);
    new_scale.y = ys* glm::sqrt(
                    matrix[1][0] * matrix[1][0] + matrix[1][1] * matrix[1][1]
                            + matrix[1][2] * matrix[1][2]);
    new_scale.z = zs* glm::sqrt(
                    matrix[2][0] * matrix[2][0] + matrix[2][1] * matrix[2][1]
                            + matrix[2][2] * matrix[2][2]);


    glm::mat3 rotation_mat(matrix[0][0] / new_scale.x,
            matrix[0][1] / new_scale.y, matrix[0][2] / new_scale.z,
            matrix[1][0] / new_scale.x, matrix[1][1] / new_scale.y,
            matrix[1][2] / new_scale.z, matrix[2][0] / new_scale.x,
            matrix[2][1] / new_scale.y, matrix[2][2] / new_scale.z);

    position_ = new_position;
    scale_ = new_scale;
    rotation_ = glm::quat_cast(rotation_mat);

    invalidate(true);
}

void Transform::translate(float x, float y, float z) {
    position_ += glm::vec3(x, y, z);
    invalidate(false);
}

// angle in radians
void Transform::setRotationByAxis(float angle, float x, float y, float z) {
    rotation_ = glm::angleAxis(angle, glm::vec3(x, y, z));
    invalidate(true);
}

void Transform::rotate(float w, float x, float y, float z) {
    rotation_ = glm::quat(w, x, y, z) * rotation_;
    invalidate(true);
}

// angle in radians
void Transform::rotateByAxis(float angle, float x, float y, float z) {
    rotation_ = glm::angleAxis(angle, glm::vec3(x, y, z)) * rotation_;
    invalidate(true);
}

// angle in radians
void Transform::rotateByAxisWithPivot(float angle, float axis_x, float axis_y,
        float axis_z, float pivot_x, float pivot_y, float pivot_z) {
    glm::quat axis_rotation = glm::angleAxis(angle,
            glm::vec3(axis_x, axis_y, axis_z));
    rotation_ = axis_rotation * rotation_;
    glm::vec3 pivot(pivot_x, pivot_y, pivot_z);
    glm::vec3 relative_position = position_ - pivot;
    relative_position = glm::rotate(axis_rotation, relative_position);
    position_ = relative_position + pivot;
    invalidate(true);
}

void Transform::rotateWithPivot(float w, float x, float y, float z,
        float pivot_x, float pivot_y, float pivot_z) {
    glm::quat rotation(w, x, y, z);
    rotation_ = rotation * rotation_;
    glm::vec3 pivot(pivot_x, pivot_y, pivot_z);
    glm::vec3 relative_position = position_ - pivot;
    relative_position = glm::rotate(rotation, relative_position);
    position_ = relative_position + pivot;
    invalidate(true);
}

}
