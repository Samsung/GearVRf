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
#include "VrApi.h"
#include "VrApi_Helpers.h"

namespace gvr {

void OculusHeadRotation::predict(GVRActivity& gvrActivity, const ovrFrameParms& frameParms, const float time) {
    if (docked_) {
        ovrMobile* ovr = gvrActivity.getOculusContext();
        ovrTracking tracking = vrapi_GetPredictedTracking(ovr,
                vrapi_GetPredictedDisplayTime(ovr, frameParms.FrameIndex));
        tracking = vrapi_ApplyHeadModel(gvrActivity.getOculusHeadModelParms(), &tracking);

        const ovrQuatf& orientation = tracking.HeadPose.Pose.Orientation;
        const glm::quat tmp(orientation.w, orientation.x, orientation.y, orientation.z);
        const glm::quat quat = glm::conjugate(glm::inverse(tmp));
        gvrActivity.cameraRig_->setRotationSensorData(0, quat.w, quat.x, quat.y, quat.z, 0, 0, 0);
        gvrActivity.cameraRig_->predict(0);

        const ovrVector3f& position = tracking.HeadPose.Pose.Position;
        const glm::vec3 pos(position.x, position.y, position.z);
        gvrActivity.cameraRig_->setPosition(pos);
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

}
