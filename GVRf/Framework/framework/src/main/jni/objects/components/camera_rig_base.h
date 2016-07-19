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

#ifndef CAMERA_RIG_BASE_H_
#define CAMERA_RIG_BASE_H_

#include <map>
#include <string>

#include "glm/glm.hpp"
#include "glm/gtx/quaternion.hpp"

#include "objects/components/component.h"
#include "objects/components/transform.h"
#include "objects/rotation_sensor_data.h"

namespace gvr {
class Camera;
class PerspectiveCamera;

class CameraRigBase: public Component {
public:
    enum CameraRigType {
        FREE = 0, YAW_ONLY = 1, ROLL_FREEZE = 2, FREEZE = 3, ORBIT_PIVOT = 4,
    };

    static long long getComponentType() {
        return (long long) getComponentType;
    }

protected:
    CameraRigBase(long long componentType);
    ~CameraRigBase();

public:
    CameraRigType camera_rig_type() const {
        return camera_rig_type_;
    }

    void set_camera_rig_type(CameraRigType camera_rig_type) {
        camera_rig_type_ = camera_rig_type;
    }

    Camera* left_camera() const {
        return left_camera_;
    }

    Camera* right_camera() const {
        return right_camera_;
    }

    PerspectiveCamera* center_camera() const {
        return center_camera_;
    }

    static float default_camera_separation_distance() {
        return default_camera_separation_distance_;
    }

    static void set_default_camera_separation_distance(float distance) {
        default_camera_separation_distance_ = distance;
    }

    float camera_separation_distance() const {
        return camera_separation_distance_;
    }

    void set_camera_separation_distance(float distance) {
        camera_separation_distance_ = distance;
    }

    float getFloat(std::string key) {
        auto it = floats_.find(key);
        if (it != floats_.end()) {
            return it->second;
        } else {
            std::string error = "CameraRig::getFloat() : " + key + " not found";
            throw error;
        }
    }
    void setFloat(std::string key, float value) {
        floats_[key] = value;
    }

    glm::vec2 getVec2(std::string key) {
        auto it = vec2s_.find(key);
        if (it != vec2s_.end()) {
            return it->second;
        } else {
            std::string error = "CameraRig::getVec2() : " + key + " not found";
            throw error;
        }
    }

    void setVec2(std::string key, glm::vec2 vector) {
        vec2s_[key] = vector;
    }

    glm::vec3 getVec3(std::string key) {
        auto it = vec3s_.find(key);
        if (it != vec3s_.end()) {
            return it->second;
        } else {
            std::string error = "CameraRig::getVec3() : " + key + " not found";
            throw error;
        }
    }

    void setVec3(std::string key, glm::vec3 vector) {
        vec3s_[key] = vector;
    }

    glm::vec4 getVec4(std::string key) {
        auto it = vec4s_.find(key);
        if (it != vec4s_.end()) {
            return it->second;
        } else {
            std::string error = "CameraRig::getVec4() : " + key + " not found";
            throw error;
        }
    }

    void setVec4(std::string key, glm::vec4 vector) {
        vec4s_[key] = vector;
    }

    void attachLeftCamera(Camera* const left_camera);
    void attachRightCamera(Camera* const right_camera);
    void attachCenterCamera(PerspectiveCamera* const center_camera);
    void reset();
    void resetYaw();
    void resetYawPitch();
    void setRotationSensorData(long long time_stamp, float w, float x, float y,
            float z, float gyro_x, float gyro_y, float gyro_z);
    virtual Transform* getHeadTransform() const = 0;
    glm::vec3 getLookAt() const;
    void setRotation(const glm::quat& transform_rotation);

private:
    CameraRigBase(const CameraRigBase& camera_rig);
    CameraRigBase(CameraRigBase&& camera_rig);
    CameraRigBase& operator=(const CameraRigBase& camera_rig);
    CameraRigBase& operator=(CameraRigBase&& camera_rig);

private:
    static const CameraRigType DEFAULT_CAMERA_RIG_TYPE = FREE;
    static const int MAX_BUFFER_SIZE = 4;
    CameraRigType camera_rig_type_;
    Camera* left_camera_;
    Camera* right_camera_;
    PerspectiveCamera* center_camera_;
    static float default_camera_separation_distance_;
    float camera_separation_distance_;
    std::map<std::string, float> floats_;
    std::map<std::string, glm::vec2> vec2s_;
    std::map<std::string, glm::vec3> vec3s_;
    std::map<std::string, glm::vec4> vec4s_;
protected:
    glm::quat complementary_rotation_;
    RotationSensorData rotation_sensor_data_;
};

}
#endif
