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
#include <android/sensor.h>

#include <glm/glm.hpp>
#include <glm/gtc/quaternion.hpp>
#include <jni.h>

#include "ktracker_sensor_filter.h"

namespace gvr {
class KTrackerSensorZip;
class KTrackerMessage;

class KSensor {
public:
    KSensor();
    ~KSensor();
    void stop();
    void start();

    template<typename Target> void convertTo(Target& target) {
        std::unique_lock<std::mutex> lock(update_mutex_, std::try_to_lock);
        if (lock.owns_lock()) {
            target.update(latest_time_, q_.w, q_.x, q_.y, q_.z, last_corrected_gyro_.x, last_corrected_gyro_.y,
                    last_corrected_gyro_.z);
        }
    }

private:
    bool update();
    bool pollSensor(KTrackerSensorZip* data);
    void process(KTrackerSensorZip* data, glm::vec3& corrected_gyro, glm::quat& q);
    void updateQ(KTrackerMessage *msg, glm::vec3& corrected_gyro, glm::quat& q);
    std::pair<glm::vec3, float> applyTiltCorrection(const glm::vec3& gyro, const glm::vec3& accel, const float DeltaT, const glm::quat& q);
    void readerThreadFunc();
    glm::vec3 applyGyroFilter(const glm::vec3& rawGyro, const float currentTemperature);
    void readFactoryCalibration();
    bool getLatestMagneticField();
    glm::quat applyMagnetometerCorrection(glm::quat& q, const glm::vec3& accelerometer, const glm::vec3& gyro, float deltaT);

    void openOvrDevice() {
        while (0 > (fd_ = open("/dev/ovr0", O_RDONLY))) {
            std::this_thread::sleep_for(std::chrono::milliseconds(50));
            if (!processing_flag_) {
                return;
            }
        }
    }

private:
    int fd_;
    glm::quat q_;
    bool first_;
    int step_;
    unsigned int first_real_time_delta_;
    uint16_t last_timestamp_;
    uint32_t full_timestamp_;
    uint8_t last_sample_count_;
    long long latest_time_;
    glm::vec3 last_acceleration_;
    glm::vec3 last_rotation_rate_;
    glm::vec3 last_corrected_gyro_;
    SensorFilter<float> tiltFilter_;
    SensorFilter<glm::vec3> gyroFilter_;
    std::thread processing_thread_;
    std::atomic<bool> processing_flag_;
    std::mutex update_mutex_;
    glm::vec3 gyroOffset_;
    float sensorTemperature_ = std::numeric_limits<float>::quiet_NaN();
    std::pair<glm::vec3, glm::quat> referencePoint_;

    float tiltCorrectionTimer = 0;
    float magnetometerCorrectionTimer = 0;
    ASensorEventQueue* magneticSensorQueue = nullptr;
    ASensorRef magneticSensor = nullptr;
    glm::vec3 magnetic;
    glm::vec3 magneticBias;

    bool factoryCalibration = false;
    glm::vec3 factoryAccelOffset_;
    glm::vec3 factoryGyroOffset_;
    glm::mat4 factoryAccelMatrix_;
    glm::mat4 factoryGyroMatrix_;
    float factoryTemperature_ = 0.0f;

    static const int KGyroNoiseFilterCapacity = 6000;
};

class KSensorFactoryCalibration {
public:
    enum {
        PacketSize = 69
    };
    uint8_t buffer_[PacketSize];

    glm::vec3 accelOffset_;
    glm::vec3 gyroOffset_;
    glm::mat4 accelMatrix_;
    glm::mat4 gyroMatrix_;
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

        for (int i = 0; i < 3; i++) {
            unpack(buffer_ + 19 + 8 * i, &x, &y, &z);
            accelMatrix_[i][0] = (float) x / sensorMax;
            accelMatrix_[i][1] = (float) y / sensorMax;
            accelMatrix_[i][2] = (float) z / sensorMax;
            accelMatrix_[i][i] += 1.0f;
        }

        for (int i = 0; i < 3; i++) {
            unpack(buffer_ + 43 + 8 * i, &x, &y, &z);
            gyroMatrix_[i][0] = (float) x / sensorMax;
            gyroMatrix_[i][1] = (float) y / sensorMax;
            gyroMatrix_[i][2] = (float) z / sensorMax;
            gyroMatrix_[i][i] += 1.0f;
        }

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
