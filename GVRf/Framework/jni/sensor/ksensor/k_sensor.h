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
#include <thread>
#include <atomic>
#include <mutex>

#include "math/quaternion.hpp"
#include "math/vector.hpp"

#include "ktracker_sensor_filter.h"

namespace gvr {
class KTrackerSensorZip;
class KTrackerMessage;

class KSensor {
public:
    KSensor();
    void stop();
    void start();

    template<typename Target> void convertTo(Target& target) {
        std::unique_lock<std::mutex> lock(update_mutex_, std::try_to_lock);
        if (lock.owns_lock()) {
            target.update(latest_time_,
                q_.w, q_.x, q_.y, q_.z,
                last_corrected_gyro_.x, last_corrected_gyro_.y, last_corrected_gyro_.z);
        }
    }

private:
    bool update();
    bool pollSensor(KTrackerSensorZip* data);
    void process(KTrackerSensorZip* data, vec3& corrected_gyro, Quaternion& q);
    void updateQ(KTrackerMessage *msg, vec3& corrected_gyro, Quaternion& q);
    vec3 applyTiltCorrection(const vec3& gyro, const vec3& accel, const float DeltaT, Quaternion& q);
    void readerThreadFunc();
    vec3 applyGyroFilter(const vec3& rawGyro, const float currentTemperature);

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
    SensorFilter<float> tiltFilter_;
    SensorFilter<vec3> gyroFilter_;
    std::thread processing_thread_;
    std::atomic<bool> processing_flag_;
    std::mutex update_mutex_;
    vec3 gyroOffset_;
    float sensorTemperature_ = std::numeric_limits<float>::quiet_NaN();

    bool factoryCalibration = false;
    vec3 factoryAccelOffset_;
    vec3 factoryGyroOffset_;
    mat4 factoryAccelMatrix_;
    mat4 factoryGyroMatrix_;
    float factoryTemperature_ = 0.0f;

    static const int KGyroNoiseFilterCapacity = 6000;
    static const double KRadiansToDegrees;
};

class KSensorFactoryCalibration {
public:
    enum {
        PacketSize = 69
    };
    uint8_t buffer_[PacketSize];

    vec3 accelOffset_;
    vec3 gyroOffset_;
    mat4 accelMatrix_;
    mat4 gyroMatrix_;
    float temperature_ = 0;

    KSensorFactoryCalibration() {
        memset(buffer_, 0, PacketSize);
        buffer_[0] = 3;
    }

    void unpack() {
        static const float sensorMax = (1 << 20) - 1;
        int32_t x, y, z;

        unpack(buffer_ + 3, &x, &y, &z);
        accelOffset_.y = (float) y * 1e-4f;
        accelOffset_.z = (float) z * 1e-4f;
        accelOffset_.x = (float) x * 1e-4f;

        unpack(buffer_ + 11, &x, &y, &z);
        gyroOffset_.x = (float) x * 1e-4f;
        gyroOffset_.y = (float) y * 1e-4f;
        gyroOffset_.z = (float) z * 1e-4f;

        float accelMatrixTemp[4][4] = { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } };
        for (int i = 0; i < 3; i++) {
            unpack(buffer_ + 19 + 8 * i, &x, &y, &z);
            accelMatrixTemp[i][0] = (float) x / sensorMax;
            accelMatrixTemp[i][1] = (float) y / sensorMax;
            accelMatrixTemp[i][2] = (float) z / sensorMax;
            accelMatrixTemp[i][i] += 1.0f;
        }
        accelMatrix_ = mat4(&accelMatrixTemp[0][0]);

        float gyroMatrixTemp[4][4] = { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } };
        for (int i = 0; i < 3; i++) {
            unpack(buffer_ + 43 + 8 * i, &x, &y, &z);
            gyroMatrixTemp[i][0] = (float) x / sensorMax;
            gyroMatrixTemp[i][1] = (float) y / sensorMax;
            gyroMatrixTemp[i][2] = (float) z / sensorMax;
            gyroMatrixTemp[i][i] += 1.0f;
        }
        gyroMatrix_ = mat4(&gyroMatrixTemp[0][0]);

        temperature_ = (float) decodeInt16t(buffer_ + 67) / 100.0f;
    }

private:
    static int16_t decodeInt16t(const uint8_t* buffer) {
        return (int16_t(buffer[1]) << 8) | int16_t(buffer[0]);
    }
    static void unpack(const unsigned char* buffer, int32_t* x, int32_t* y, int32_t* z) {
        struct {
            int32_t x :21;
        } s;

        *x = s.x = (buffer[0] << 13) | (buffer[1] << 5) | ((buffer[2] & 0xF8) >> 3);
        *y = s.x = ((buffer[2] & 0x07) << 18) | (buffer[3] << 10) | (buffer[4] << 2) | ((buffer[5] & 0xC0) >> 6);
        *z = s.x = ((buffer[5] & 0x3F) << 15) | (buffer[6] << 7) | (buffer[7] >> 1);
    }
};
}

#endif /* K_SENSOR_H_ */
