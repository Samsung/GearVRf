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


/*
 * k_sensor.h
 *
 *  Created on: 2014. 8. 4.
 */

#ifndef K_SENSOR_H_
#define K_SENSOR_H_

#include <time.h>
#include <sys/ioctl.h>
#include <poll.h>
#include <fcntl.h>

#include "math/quaternion.hpp"
#include "math/vector.hpp"

#include "ktracker_sensor_filter.h"

namespace gvr {
class KTrackerSensorZip;
class KTrackerMessage;

class KSensor {
public:
    KSensor();
    ~KSensor();
    bool update();
    long long getLatestTime();
    Quaternion getSensorQuaternion();
    vec3 getAngularVelocity();
    void closeSensor();

private:
    bool pollSensor(KTrackerSensorZip* data);
    void process(KTrackerSensorZip* data);
    void updateQ(KTrackerMessage *msg);
    vec3 gyrocorrect(vec3 gyro, vec3 accel, const float DeltaT);

private:
    int fd_;
    Quaternion q_;
    bool first_;
    int step_;
    unsigned int first_real_time_delta_;
    uint16_t last_timestamp_;
    uint32_t full_timestamp_;
    uint8_t last_sample_count_;
    long long latest_time_;
    vec3 last_acceleration_;
    vec3 last_rotation_rate_;
    vec3 last_corrected_gyro_;
    vec3 gyro_offset_;
    SensorFilter<float> tilt_filter_;
};
}

#endif /* K_SENSOR_H_ */
