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
 * k_sensor.cpp
 *
 *  Created on: 2014. 8. 4.
 */

#include "k_sensor.h"

#include "ktracker_data_info.h"
#include "util/gvr_log.h"
#include "util/gvr_time.h"
#include <chrono>
#include "math/matrix.hpp"

namespace gvr {

#define HIDIOCGFEATURE(len)    _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x07, len)
//#define LOG_SUMMARY
//#define LOG_GYRO_FILTER

void KSensor::readerThreadFunc() {
    LOGV("k_sensor: reader starting up");

    readFactoryCalibration();

    while (0 > (fd_ = open("/dev/ovr0", O_RDONLY))) {
        std::this_thread::sleep_for(std::chrono::milliseconds(50));
        if (!processing_flag_) {
            return;
        }
    }

    Quaternion q;
    vec3 corrected_gyro;
    long long currentTime;
    KTrackerSensorZip data;

    while (processing_flag_) {
        if (!pollSensor(&data)) {
            continue;
        }

        currentTime = getCurrentTime();
        process(&data, corrected_gyro, q);

        std::lock_guard<std::mutex> lock(update_mutex_);
        latest_time_ = currentTime;
        last_corrected_gyro_ = corrected_gyro;
        q_ = q;
    }

    if (fd_ >= 0) {
        close(fd_);
        fd_ = -1;
    }

    LOGV("k_sensor: reader shut down");
}

KSensor::KSensor() :
        fd_(-1), q_(), first_(true), step_(0), first_real_time_delta_(0.0f), last_timestamp_(
                0), full_timestamp_(0), last_sample_count_(0), latest_time_(0), last_acceleration_(
                0.0f, 0.0f, 0.0f), processing_thread_(), gyroFilter_(
                KGyroNoiseFilterCapacity), tiltFilter_(25), processing_flag_(
                true) {
}

void KSensor::start() {
    processing_thread_ = std::thread(&KSensor::readerThreadFunc, this);
}

void KSensor::stop() {
    processing_flag_ = false;
    if (processing_thread_.joinable()) {
        processing_thread_.join();
    }
}

bool KSensor::pollSensor(KTrackerSensorZip* data) {
    struct pollfd pfds;
    pfds.fd = fd_;
    pfds.events = POLLIN;

    uint8_t buffer[100];
    int n = poll(&pfds, 1, 100);
    if (n > 0 && (pfds.revents & POLLIN)) {
        int r = read(fd_, buffer, 100);
        if (r < 0) {
            LOGI("k_sensor: read error %d", r);
            return false;
        }

        data->SampleCount = buffer[1];
        data->Timestamp = (uint16_t) (*(buffer + 3) << 8)
                | (uint16_t) (*(buffer + 2));
        data->LastCommandID = (uint16_t) (*(buffer + 5) << 8)
                | (uint16_t) (*(buffer + 4));
        data->Temperature = (int16_t) (*(buffer + 7) << 8)
                | (int16_t) (*(buffer + 6));

        for (int i = 0; i < (data->SampleCount > 3 ? 3 : data->SampleCount);
                ++i) {
            struct {
                int32_t x :21;
            } s;

            data->Samples[i].AccelX = s.x = (buffer[0 + 8 + 16 * i] << 13)
                    | (buffer[1 + 8 + 16 * i] << 5)
                    | ((buffer[2 + 8 + 16 * i] & 0xF8) >> 3);
            data->Samples[i].AccelY = s.x = ((buffer[2 + 8 + 16 * i] & 0x07)
                    << 18) | (buffer[3 + 8 + 16 * i] << 10)
                    | (buffer[4 + 8 + 16 * i] << 2)
                    | ((buffer[5 + 8 + 16 * i] & 0xC0) >> 6);
            data->Samples[i].AccelZ = s.x = ((buffer[5 + 8 + 16 * i] & 0x3F)
                    << 15) | (buffer[6 + 8 + 16 * i] << 7)
                    | (buffer[7 + 8 + 16 * i] >> 1);

            data->Samples[i].GyroX = s.x = (buffer[0 + 16 + 16 * i] << 13)
                    | (buffer[1 + 16 + 16 * i] << 5)
                    | ((buffer[2 + 16 + 16 * i] & 0xF8) >> 3);
            data->Samples[i].GyroY = s.x = ((buffer[2 + 16 + 16 * i] & 0x07)
                    << 18) | (buffer[3 + 16 + 16 * i] << 10)
                    | (buffer[4 + 16 + 16 * i] << 2)
                    | ((buffer[5 + 16 + 16 * i] & 0xC0) >> 6);
            data->Samples[i].GyroZ = s.x = ((buffer[5 + 16 + 16 * i] & 0x3F)
                    << 15) | (buffer[6 + 16 + 16 * i] << 7)
                    | (buffer[7 + 16 + 16 * i] >> 1);
        }

        return true;
    }
    return false;
}

void KSensor::process(KTrackerSensorZip* data, vec3& corrected_gyro,
        Quaternion& q) {
    const float timeUnit = (1.0f / 1000.f);

    struct timespec tp;
    clock_gettime(CLOCK_MONOTONIC, &tp);
    const double now = tp.tv_sec + tp.tv_nsec * 0.000000001;

    double absoluteTimeSeconds = 0.0;

    if (first_) {
        last_acceleration_ = vec3(0, 0, 0);
        last_rotation_rate_ = vec3(0, 0, 0);
        first_ = false;

        // This is our baseline sensor to host time delta,
        // it will be adjusted with each new message.
        full_timestamp_ = data->Timestamp;
        first_real_time_delta_ = now - (full_timestamp_ * timeUnit);
    } else {
        unsigned timestampDelta;

        if (data->Timestamp < last_timestamp_) {
            // The timestamp rolled around the 16 bit counter, so FullTimeStamp
            // needs a high word increment.
            full_timestamp_ += 0x10000;
            timestampDelta = ((((int) data->Timestamp) + 0x10000)
                    - (int) last_timestamp_);
        } else {
            timestampDelta = (data->Timestamp - last_timestamp_);
        }
        // Update the low word of FullTimeStamp
        full_timestamp_ = (full_timestamp_ & ~0xffff) | data->Timestamp;

        // If this timestamp, adjusted by our best known delta, would
        // have the message arriving in the future, we need to adjust
        // the delta down.
        if (full_timestamp_ * timeUnit + first_real_time_delta_ > now) {
            first_real_time_delta_ = now - (full_timestamp_ * timeUnit);
        } else {
            // Creep the delta by 100 microseconds so we are always pushing
            // it slightly towards the high clamping case, instead of having to
            // worry about clock drift in both directions.
            first_real_time_delta_ += 0.0001;
        }
        // This will be considered the absolute time of the last sample in
        // the message.  If we are double or tripple stepping the samples,
        // their absolute times will be adjusted backwards.
        absoluteTimeSeconds = full_timestamp_ * timeUnit
                + first_real_time_delta_;

        // If we missed a small number of samples, replicate the last sample.
        if ((timestampDelta > last_sample_count_) && (timestampDelta <= 254)) {
            KTrackerMessage sensors;
            sensors.TimeDelta = (timestampDelta - last_sample_count_)
                    * timeUnit;
            sensors.Acceleration = last_acceleration_;
            sensors.RotationRate = last_rotation_rate_;

            updateQ(&sensors, corrected_gyro, q);
        }
    }

    KTrackerMessage sensors;
    int iterations = data->SampleCount;
    sensors.Temperature = data->Temperature * 0.01f;

    if (data->SampleCount > 3) {
        iterations = 3;
        sensors.TimeDelta = (data->SampleCount - 2) * timeUnit;
    } else {
        sensors.TimeDelta = timeUnit;
    }

    const float scale = 0.0001f;
    for (int i = 0; i < iterations; ++i) {
        sensors.Acceleration = vec3(data->Samples[i].AccelX * scale,
                data->Samples[i].AccelY * scale,
                data->Samples[i].AccelZ * scale);
        sensors.RotationRate = vec3(data->Samples[i].GyroX * scale,
                data->Samples[i].GyroY * scale, data->Samples[i].GyroZ * scale);

        updateQ(&sensors, corrected_gyro, q);

        // TimeDelta for the last two sample is always fixed.
        sensors.TimeDelta = timeUnit;
    }

    last_sample_count_ = data->SampleCount;
    last_timestamp_ = data->Timestamp;
    last_acceleration_ = sensors.Acceleration;
    last_rotation_rate_ = sensors.RotationRate;
}

void KSensor::updateQ(KTrackerMessage *msg, vec3& corrected_gyro,
        Quaternion& q) {
    vec3 filteredGyro = applyGyroFilter(msg->RotationRate, msg->Temperature);

    const float deltaT = msg->TimeDelta;
    corrected_gyro = applyTiltCorrection(filteredGyro, msg->Acceleration,
            deltaT, q);

    float gyroLength = filteredGyro.Length();
    if (gyroLength != 0.0f) {
        q = q
                * Quaternion::CreateFromAxisAngle(corrected_gyro.Normalized(),
                        gyroLength * deltaT);
    }

#ifdef LOG_SUMMARY
    if (0 == step_ % 1000) {
        LOGI(
                "k_sensor: summary: yaw angle %f; gyro offset %f %f %f; temperature %f; gyro filter sizes %d",
                q.ToEulerAngle().y * KRadiansToDegrees, gyroOffset_.x,
                gyroOffset_.y, gyroOffset_.z, msg->Temperature,
                gyroFilter_.size());
    }
#endif

    step_++;
    // Normalize error
    if (step_ % 500 == 0) {
        q.Normalize();
    }
}

/**
 * Tries to determine the gyro noise and subtract it from the actual reading to
 * reduce the amount of yaw drift.
 */
vec3 KSensor::applyGyroFilter(const vec3& rawGyro,
        const float currentTemperature) {
    const int gyroFilterSize = gyroFilter_.size();
    if (gyroFilterSize > KGyroNoiseFilterCapacity / 2) {
        gyroOffset_ = gyroFilter_.mean();
        sensorTemperature_ = currentTemperature;
    }

    if (gyroFilterSize > 0) {
        if (!isnan(sensorTemperature_)
                && sensorTemperature_ != currentTemperature) {
            gyroFilter_.clear();
            sensorTemperature_ = currentTemperature;

#ifdef LOG_GYRO_FILTER
            LOGI("k_sensor: clear gyro filter due to temperature change");
#endif
        } else {
            const vec3& mean = gyroFilter_.mean();
            // magic values that happen to work
            if (rawGyro.Length() > 1.25f * 0.349066f
                    || (rawGyro - mean).Length() > 0.018f) {
                gyroFilter_.clear();

#ifdef LOG_GYRO_FILTER
                LOGI("k_sensor: clear gyro filter due to motion %f %f", rawGyro.Length(), (rawGyro - mean).Length());
#endif
            }
        }
    }

    const float alpha = 0.4f;
    const vec3 avg =
            (0 == gyroFilterSize) ?
                    rawGyro :
                    rawGyro * alpha + gyroFilter_.peekBack() * (1 - alpha);
    gyroFilter_.push(avg);

    return factoryGyroMatrix_.Transform(rawGyro - gyroOffset_);
}

vec3 KSensor::applyTiltCorrection(const vec3& gyro, const vec3& accel,
        const float DeltaT, Quaternion& q) {
    vec3 gyroCorrected = gyro;

    if (accel.Length() > 0.001f) {
        Quaternion Qinv = q.Inverted();
        vec3 up = Qinv.Rotate(vec3(0, 1, 0));

        const float spikeThreshold = 0.01f;
        float proportionalGain = 0.25f;

        vec3 accel_normalize = accel.Normalized();
        vec3 up_normalize = up.Normalized();
        vec3 correction = accel_normalize.Cross(up_normalize);
        float cosError = accel_normalize.Dot(up_normalize);

        if (step_ > 5) {
            // Spike detection
            float tiltAngle = up.Angle(accel);
            tiltFilter_.push(tiltAngle);
            if (tiltAngle > tiltFilter_.mean() + spikeThreshold) {
                proportionalGain = 0;
            }
        } else {
            // Apply full correction at the startup
            proportionalGain = 1 / DeltaT;
        }

        gyroCorrected += (correction * proportionalGain);
    }

    return gyroCorrected;
}

void KSensor::readFactoryCalibration() {
    if (!factoryCalibration) {
        factoryCalibration = true;
        KSensorFactoryCalibration ksfc;
        int handle;
        while (0 > (handle = open("/dev/ovr0", O_RDONLY))) {
            std::this_thread::sleep_for(std::chrono::milliseconds(50));
            if (!processing_flag_) {
                return;
            }
        }

        int r = ioctl(handle, HIDIOCGFEATURE(ksfc.PacketSize), ksfc.buffer_);
        if (0 > r) {
            LOGI("k_sensor: ioctl to get factory calibration failed!");
            return;
        } else {
            close(handle);
            ksfc.unpack();
            factoryAccelMatrix_ = ksfc.accelMatrix_;
            factoryGyroMatrix_ = ksfc.gyroMatrix_;
            factoryAccelOffset_ = ksfc.accelOffset_;
            gyroOffset_ = factoryGyroOffset_ = ksfc.gyroOffset_;
            factoryTemperature_ = ksfc.temperature_;
        }
    }
}

const double KSensor::KRadiansToDegrees = 180 / M_PI;

}
