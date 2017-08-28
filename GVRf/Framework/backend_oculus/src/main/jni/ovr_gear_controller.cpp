/*
/* Copyright 2017 Samsung Electronics Co., LTD
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

#include "ovr_gear_controller.h"

namespace gvr {

    bool GearController::findConnectedGearController() {
        bool foundRemote = false;

        for (uint32_t deviceIndex = 0;; deviceIndex++) {
            ovrInputCapabilityHeader curCaps;

            if (vrapi_EnumerateInputDevices(ovrMobile_, deviceIndex, &curCaps) < 0) {
                break;
            }
            switch (curCaps.Type) {
                case ovrControllerType_TrackedRemote:
                    if (!foundRemote) {
                        foundRemote = true;
                        if (RemoteDeviceID != curCaps.DeviceID) {
                            onControllerDisconnected(RemoteDeviceID);
                            RemoteDeviceID = curCaps.DeviceID;
                            onControllerConnected(RemoteDeviceID);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        if (!foundRemote && RemoteDeviceID != ovrDeviceIdType_Invalid) {
            onControllerDisconnected(RemoteDeviceID);
            RemoteDeviceID = ovrDeviceIdType_Invalid;
            return false;
        }

        return true;
    }

    void GearController::onControllerConnected(const ovrDeviceID deviceID) {
        ovrInputTrackedRemoteCapabilities remoteCapabilities;
        remoteCapabilities.Header.Type = ovrControllerType_TrackedRemote;
        remoteCapabilities.Header.DeviceID = deviceID;
        ovrResult result = vrapi_GetInputDeviceCapabilities(ovrMobile_, &remoteCapabilities.Header);
        if (result == ovrSuccess) {
            handedness = (remoteCapabilities.ControllerCapabilities & ovrControllerCaps_LeftHand
                         ) ? 0 : 1;

            ovrInputStateTrackedRemote remoteInputState;
            remoteInputState.Header.ControllerType = ovrControllerType_TrackedRemote;
            result = vrapi_GetCurrentInputState(ovrMobile_, RemoteDeviceID, &remoteInputState
                    .Header);
        }
        vrapi_RecenterInputPose(ovrMobile_, RemoteDeviceID);
    }

    void GearController::onControllerDisconnected(const ovrDeviceID deviceID) {
        // not used at the moment
    }


    void GearController::onFrame(double predictedDisplayTime) {
        ovrTracking tracking;
        if (RemoteDeviceID != ovrDeviceIdType_Invalid) {
            orientationTrackingReadbackBuffer[0] = CONNECTED;

            orientationTrackingReadbackBuffer[1] = handedness;
            ovrResult result = vrapi_GetInputTrackingState(ovrMobile_, RemoteDeviceID,
                                                           predictedDisplayTime, &tracking);

            ovrQuatf orientation = tracking.HeadPose.Pose.Orientation;
            const glm::quat tmp(orientation.w, orientation.x, orientation.y, orientation.z);
            const glm::quat quat = glm::conjugate(glm::inverse(tmp));
            orientationTrackingReadbackBuffer[6] = quat.w;
            orientationTrackingReadbackBuffer[7] = quat.x;
            orientationTrackingReadbackBuffer[8] = quat.y;
            orientationTrackingReadbackBuffer[9] = quat.z;

            ovrInputStateTrackedRemote state;
            state.Header.ControllerType = ovrControllerType_TrackedRemote;
            vrapi_GetCurrentInputState(ovrMobile_, RemoteDeviceID, &state.Header);

            orientationTrackingReadbackBuffer[2] = state.TrackpadStatus;

            orientationTrackingReadbackBuffer[10] = state.Buttons;
            orientationTrackingReadbackBuffer[11] = state.TrackpadPosition.x;
            orientationTrackingReadbackBuffer[12] = state.TrackpadPosition.y;

            ovrVector3f position = tracking.HeadPose.Pose.Position;
            orientationTrackingReadbackBuffer[3] = position.x;
            orientationTrackingReadbackBuffer[4] = position.y;
            orientationTrackingReadbackBuffer[5] = position.z;

        } else {
            // set disconnected
            orientationTrackingReadbackBuffer[0] = DISCONNECTED;
        }
    }
}