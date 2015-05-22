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
 * Holds left, right cameras and reacts to the rotation sensor.
 ***************************************************************************/

#include "camera_rig.h"

#include "glm/gtc/quaternion.hpp"

#include "objects/scene_object.h"
#include "objects/components/camera.h"
#include "util/gvr_time.h"

namespace gvr {

float CameraRig::default_camera_separation_distance_ = 0.062f;

CameraRig::CameraRig() :
        Component(), camera_rig_type_(DEFAULT_CAMERA_RIG_TYPE), left_camera_(), right_camera_(), camera_separation_distance_(
                default_camera_separation_distance_), floats_(), vec2s_(), vec3s_(), vec4s_(), complementary_rotation_(), rotation_sensor_data_(), rotation_buffer_() {
}

CameraRig::~CameraRig() {
}

void CameraRig::attachLeftCamera(Camera* const left_camera) {
    left_camera->owner_object()->transform()->set_position(
            -camera_separation_distance_ * 0.5f, 0.0f, 0.0f);
    left_camera_ = left_camera;
}

void CameraRig::attachRightCamera(Camera* const right_camera) {
    right_camera->owner_object()->transform()->set_position(
            camera_separation_distance_ * 0.5f, 0.0f, 0.0f);
    right_camera_ = right_camera;
}

void CameraRig::reset() {
    complementary_rotation_ = glm::inverse(rotation_sensor_data_.quaternion());
    rotation_buffer_.clear();
}

void CameraRig::resetYaw() {
    glm::vec3 look_at = glm::rotate(rotation_sensor_data_.quaternion(),
            glm::vec3(0.0f, 0.0f, -1.0f));
    float yaw = atan2f(-look_at.x, -look_at.z) * 180.0f / M_PI;
    complementary_rotation_ = glm::angleAxis(-yaw, glm::vec3(0.0f, 1.0f, 0.0f));
    rotation_buffer_.clear();
}

void CameraRig::resetYawPitch() {
    glm::vec3 look_at = glm::rotate(rotation_sensor_data_.quaternion(),
            glm::vec3(0.0f, 0.0f, -1.0f));
    float pitch = atan2f(look_at.y,
            sqrtf(look_at.x * look_at.x + look_at.z * look_at.z)) * 180.0f
            / M_PI;
    float yaw = atan2f(-look_at.x, -look_at.z) * 180.0f / M_PI;
    glm::quat quat = glm::angleAxis(pitch, glm::vec3(1.0f, 0.0f, 0.0f));
    quat = glm::angleAxis(yaw, glm::vec3(0.0f, 1.0f, 0.0f)) * quat;
    complementary_rotation_ = glm::inverse(quat);
    rotation_buffer_.clear();
}

void CameraRig::setRotationSensorData(long long time_stamp, float w, float x,
        float y, float z, float gyro_x, float gyro_y, float gyro_z) {
    rotation_sensor_data_ = RotationSensorData(time_stamp, w, x, y, z, gyro_x,
            gyro_y, gyro_z);
}

void CameraRig::predict(float time) {
    long long clock_time = getCurrentTime();
    float time_diff = (clock_time - rotation_sensor_data_.time_stamp())
            / 1000000000.0f;

    glm::vec3 axis = rotation_sensor_data_.gyro();
    float angle = glm::length(axis);

    if (angle != 0.0f) {
        axis /= angle;
    }
    angle *= (time + time_diff) * 180.0f / M_PI;

    glm::quat rotation = rotation_sensor_data_.quaternion()
            * glm::angleAxis(angle, axis);

    glm::quat transfrom_rotation = complementary_rotation_ * rotation;

    if (camera_rig_type_ == FREE) {
        owner_object()->transform()->set_rotation(transfrom_rotation);
    } else if (camera_rig_type_ == YAW_ONLY) {
        glm::vec3 look_at = glm::rotate(transfrom_rotation,
                glm::vec3(0.0f, 0.0f, -1.0f));
        float yaw = atan2f(-look_at.x, -look_at.z) * 180.0f / M_PI;
        owner_object()->transform()->set_rotation(
                glm::angleAxis(yaw, glm::vec3(0.0f, 1.0f, 0.0f)));
    } else if (camera_rig_type_ == ROLL_FREEZE) {
        glm::vec3 look_at = glm::rotate(transfrom_rotation,
                glm::vec3(0.0f, 0.0f, -1.0f));
        float pitch = atan2f(look_at.y,
                sqrtf(look_at.x * look_at.x + look_at.z * look_at.z)) * 180.0f
                / M_PI;
        float yaw = atan2f(-look_at.x, -look_at.z) * 180.0f / M_PI;
        owner_object()->transform()->set_rotation(
                glm::angleAxis(pitch, glm::vec3(1.0f, 0.0f, 0.0f)));
        owner_object()->transform()->rotateByAxis(yaw, 0.0f, 1.0f, 0.0f);
    } else if (camera_rig_type_ == FREEZE) {
        owner_object()->transform()->set_rotation(glm::quat());
    } else if (camera_rig_type_ == ORBIT_PIVOT) {
        glm::vec3 pivot(getVec3("pivot"));
        owner_object()->transform()->set_position(pivot.x, pivot.y,
                pivot.z + getFloat("distance"));
        owner_object()->transform()->set_rotation(glm::quat());
        owner_object()->transform()->rotateWithPivot(transfrom_rotation.w,
                transfrom_rotation.x, transfrom_rotation.y,
                transfrom_rotation.z, pivot.x, pivot.y, pivot.z);
    }
}

glm::vec3 CameraRig::getLookAt() const {
    glm::mat4 model_matrix = owner_object()->transform()->getModelMatrix();
    float x0 = model_matrix[3][0];
    float y0 = model_matrix[3][1];
    float z0 = model_matrix[3][2];
    float reciprocalW0 = 1 / model_matrix[3][3];
    x0 *= reciprocalW0;
    y0 *= reciprocalW0;
    z0 *= reciprocalW0;

    float x1 = model_matrix[2][0] * -1.0f + model_matrix[3][0];
    float y1 = model_matrix[2][1] * -1.0f + model_matrix[3][1];
    float z1 = model_matrix[2][2] * -1.0f + model_matrix[3][2];
    float reciprocalW1 = 1.0f
            / (model_matrix[2][3] * -1.0f + model_matrix[3][3]);
    x1 *= reciprocalW1;
    y1 *= reciprocalW1;
    z1 *= reciprocalW1;

    float lookAtX = x1 - x0;
    float lookAtY = y1 - y0;
    float lookAtZ = z1 - z0;
    float reciprocalLength = 1.0f
            / sqrtf(lookAtX * lookAtX + lookAtY * lookAtY + lookAtZ * lookAtZ);
    lookAtX *= reciprocalLength;
    lookAtY *= reciprocalLength;
    lookAtZ *= reciprocalLength;

    return glm::vec3(lookAtX, lookAtY, lookAtZ);
}

}
