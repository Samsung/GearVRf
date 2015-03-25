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


#ifndef KTRACKER_DATA_INFO_H_
#define KTRACKER_DATA_INFO_H_

#include "math/vector.hpp"

namespace gvr {

struct KTrackerSensorRawData {
    int32_t AccelX, AccelY, AccelZ;
    int32_t GyroX, GyroY, GyroZ;
};

struct KTrackerSensorZip {
    uint8_t SampleCount;
    uint16_t Timestamp;
    uint16_t LastCommandID;
    int16_t Temperature;

    KTrackerSensorRawData Samples[3];

    int16_t MagX, MagY, MagZ;
};

struct KTrackerMessage {
    vec3 Acceleration;
    vec3 RotationRate;
    vec3 MagneticField;
    float Temperature;
    float TimeDelta;
    double AbsoluteTimeSeconds;
};

}
#endif
