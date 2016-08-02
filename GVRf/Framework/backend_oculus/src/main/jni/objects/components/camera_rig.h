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

#ifndef CAMERA_RIG_H_
#define CAMERA_RIG_H_

#include "objects/components/camera_rig_base.h"

namespace gvr {

class CameraRig: public CameraRigBase {
public:
    CameraRig();
    virtual ~CameraRig();

    void predict(float time);
    void predict(float time, const RotationSensorData& rotationSensorData);
    void setPosition(const glm::vec3& transform_position);

    virtual Transform* getHeadTransform() const;

private:
    CameraRig(const CameraRig& camera_rig);
    CameraRig(CameraRig&& camera_rig);
    CameraRig& operator=(const CameraRig& camera_rig);
    CameraRig& operator=(CameraRig&& camera_rig);
};

}
#endif
