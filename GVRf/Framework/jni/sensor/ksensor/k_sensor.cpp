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

namespace gvr {
KSensor::KSensor() :
        fd_(-1), q_(), first_(true), step_(0), first_real_time_delta_(0.0f), last_timestamp_(
                0), full_timestamp_(0), last_sample_count_(0), latest_time_(0), last_acceleration_(
                0.0f, 0.0f, 0.0f), last_rotation_rate_(0.0f, 0.0f, 0.0f), last_corrected_gyro_(
                0.0f, 0.0f, 0.0f), gyro_offset_(0.0f, 0.0f, 0.0f), tilt_filter_() {
}

KSensor::~KSensor() {
}

bool KSensor::update() {
    KTrackerSensorZip data;
    if (!pollSensor(&data)) {
        if (fd_ >= 0) {
            close(fd_);
            fd_ = -1;
        }
        return false;
    }
    latest_time_ = getCurrentTime();

    process(&data);

    return true;
}

long long KSensor::getLatestTime() {
    return latest_time_;
}

Quaternion KSensor::getSensorQuaternion() {
    return q_;
}

vec3 KSensor::getAngularVelocity() {
    return last_corrected_gyro_;
}

void KSensor::closeSensor() {
    if (fd_ >= 0) {
        close(fd_);
        fd_ = -1;
    }
}

bool KSensor::pollSensor(KTrackerSensorZip* data) {
    if (fd_ < 0) {
        fd_ = open("/dev/ovr0", O_RDONLY);
    }
    if (fd_ < 0) {
        return false;
    }

    struct pollfd pfds;
    pfds.fd = fd_;
    pfds.events = POLLIN;

    int n = poll(&pfds, 1, 100);
    if (n > 0 && (pfds.revents & POLLIN)) {
        uint8_t buffer[100];
        int r = read(fd_, buffer, 100);
        if (r < 0) {
            LOGI("OnSensorEvent() read error %d", r);
            return false;
        }

        data->SampleCount = buffer[1];
        data->Timestamp = (uint16_t)(*(buffer + 3) << 8)
                | (uint16_t)(*(buffer + 2));
        data->LastCommandID = (uint16_t)(*(buffer + 5) << 8)
                | (uint16_t)(*(buffer + 4));
        data->Temperature = (int16_t)(*(buffer + 7) << 8)
                | (int16_t)(*(buffer + 6));

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

        data->MagX = (int16_t)(*(buffer + 57) << 8) | (int16_t)(*(buffer + 56));
        data->MagY = (int16_t)(*(buffer + 59) << 8) | (int16_t)(*(buffer + 58));
        data->MagZ = (int16_t)(*(buffer + 61) << 8) | (int16_t)(*(buffer + 60));

        return true;
    }

    return false;
}

void KSensor::process(KTrackerSensorZip* data) {
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

            updateQ(&sensors);
        }
    }

    KTrackerMessage sensors;
    int iterations = data->SampleCount;

    if (data->SampleCount > 3) {
        iterations = 3;
        sensors.TimeDelta = (data->SampleCount - 2) * timeUnit;
    } else {
        sensors.TimeDelta = timeUnit;
    }

    for (int i = 0; i < iterations; ++i) {
        sensors.Acceleration = vec3(data->Samples[i].AccelX,
                data->Samples[i].AccelY, data->Samples[i].AccelZ) * 0.0001f;
        sensors.RotationRate = vec3(data->Samples[i].GyroX,
                data->Samples[i].GyroY, data->Samples[i].GyroZ) * 0.0001f;

        updateQ(&sensors);

        // TimeDelta for the last two sample is always fixed.
        sensors.TimeDelta = timeUnit;
    }

    last_sample_count_ = data->SampleCount;
    last_timestamp_ = data->Timestamp;
    last_acceleration_ = sensors.Acceleration;
    last_rotation_rate_ = sensors.RotationRate;

}

void KSensor::updateQ(KTrackerMessage *msg) {
    // Put the sensor readings into convenient local variables
    vec3 gyro = msg->RotationRate;
    vec3 accel = msg->Acceleration;
    const float DeltaT = msg->TimeDelta;
    vec3 gyroCorrected = gyrocorrect(gyro, accel, DeltaT);
    last_corrected_gyro_ = gyroCorrected;

    // Update the orientation quaternion based on the corrected angular velocity vector
    float gyro_length = gyroCorrected.Length();
    if (gyro_length != 0.0f) {
        float angle = gyro_length * DeltaT;
        q_ = q_
                * Quaternion(cos(angle * 0.5f),
                        gyroCorrected.Normalized() * sin(angle * 0.5f));
    }

    step_++;

    // Normalize error
    if (step_ % 500 == 0) {
        q_.Normalize();
    }
}

vec3 KSensor::gyrocorrect(vec3 gyro, vec3 accel, const float DeltaT) {
    // Small preprocessing
    Quaternion Qinv = q_.Inverted();
    vec3 up = Qinv.Rotate(vec3(0, 1, 0));
    vec3 gyroCorrected = gyro;

    bool EnableGravity = true;
    bool valid_accel = accel.Length() > 0.001f;

    if (EnableGravity && valid_accel) {
        gyroCorrected -= gyro_offset_;

        const float spikeThreshold = 0.01f;
        const float gravityThreshold = 0.1f;
        float proportionalGain = 0.25f, integralGain = 0.0f;

        vec3 accel_normalize = accel.Normalized();
        vec3 up_normalize = up.Normalized();
        vec3 correction = accel_normalize.Cross(up_normalize);
        float cosError = accel_normalize.Dot(up_normalize);
        const float Tolerance = 0.00001f;
        vec3 tiltCorrection = correction
                * sqrtf(2.0f / (1 + cosError + Tolerance));

        if (step_ > 5) {
            // Spike detection
            float tiltAngle = up.Angle(accel);
            tilt_filter_.AddElement(tiltAngle);
            if (tiltAngle > tilt_filter_.Mean() + spikeThreshold)
                proportionalGain = integralGain = 0;
            // Acceleration detection
            const float gravity = 9.8f;
            if (fabs(accel.Length() / gravity - 1) > gravityThreshold)
                integralGain = 0;
        } else {
            // Apply full correction at the startup
            proportionalGain = 1 / DeltaT;
            integralGain = 0;
        }

        gyroCorrected += (tiltCorrection * proportionalGain);
        gyro_offset_ -= (tiltCorrection * integralGain * DeltaT);
    } else {
        LOGI("invalidaccel");
    }

    return gyroCorrected;
}
}
