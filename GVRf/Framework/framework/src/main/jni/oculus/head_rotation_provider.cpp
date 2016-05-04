/* Copyright 2016 Samsung Electronics Co., LTD
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

#include "head_rotation_provider.h"
#include "activity.h"

#ifndef USE_FEATURE_KSENSOR
#include "VrApi.h"
#include "VrApi_Helpers.h"
#endif

namespace gvr {

#ifdef USE_FEATURE_KSENSOR
void KSensorHeadRotation::predict(GVRActivity& gvrActivity, const ovrFrameParms&,
        const float time) {
    if (nullptr != gvrActivity.cameraRig_) {
        if (nullptr == sensor_.get()) {
            gvrActivity.cameraRig_->predict(time);
        } else {
            sensor_->convertTo(rotationSensorData_);
            gvrActivity.cameraRig_->predict(time, rotationSensorData_);
        }
    } else {
        gvrActivity.cameraRig_->setRotation(glm::quat());
    }
}
bool KSensorHeadRotation::receivingUpdates() {
    return rotationSensorData_.hasBeenUpdated();
}
void KSensorHeadRotation::onDock() {
    sensor_.reset(new KSensor());
    sensor_->start();
}
void KSensorHeadRotation::onUndock() {
    if (nullptr != sensor_.get()) {
        sensor_->stop();
        sensor_.reset(nullptr);
    }
}

#else

void OculusHeadRotation::predict(GVRActivity& gvrActivity, const ovrFrameParms& frameParms, const float time) {
    if (docked_) {
        ovrMobile* ovr = gvrActivity.getOculusContext();
        ovrTracking tracking = vrapi_GetPredictedTracking(ovr,
                vrapi_GetPredictedDisplayTime(ovr, frameParms.FrameIndex));
        tracking = vrapi_ApplyHeadModel(gvrActivity.getOculusHeadModelParms(), &tracking);

        const ovrQuatf& orientation = tracking.HeadPose.Pose.Orientation;
        glm::quat quat(orientation.w, orientation.x, orientation.y, orientation.z);
        gvrActivity.cameraRig_->setRotation(glm::conjugate(glm::inverse(quat)));
    } else if (nullptr != gvrActivity.cameraRig_) {
        gvrActivity.cameraRig_->predict(time);
    } else {
        gvrActivity.cameraRig_->setRotation(glm::quat());
    }
}
bool OculusHeadRotation::receivingUpdates() {
    return docked_;
}
void OculusHeadRotation::onDock() {
    docked_ = true;
}
void OculusHeadRotation::onUndock() {
    docked_ = false;
}

#endif

}
