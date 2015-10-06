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
#include <assert.h>
#include <algorithm>
#include <cmath>

#include <android/sensor.h>
#include <android/looper.h>

#define LOG_VERBOSE_ENABLED
#ifdef LOG_VERBOSE_ENABLED
#define LOG_VERBOSE(...) if (counter_%100==0) LOGV(__VA_ARGS__)
#else
#define LOG_VERBOSE(...)
#endif

#define LOG_INFO_ENABLED
#ifdef LOG_INFO_ENABLED
static int counter_;
#define LOG_INCREMENT() (++counter_)
#define LOG_INFO(...) if (counter_%100==0) LOGI(__VA_ARGS__)
#else
#define LOG_INFO(...)
#endif

namespace gvr {

const float KDegreesToRadians = M_PI / 180.0f;
const float KRadiansToDegrees = 180.0f / M_PI;
const float KVerySmallValue = 0.00001f;

const float KYawDriftCorrection_TiltCorrectionWaitInSeconds = 2.0;
const float KYawDriftCorrection_MaxTiltDifference = 0.05f;
const float KYawCorrectionMaxRadians = 1.5f * KDegreesToRadians;
const float KYawDriftCorrection_AngularVelocityThreshold = 5 * KDegreesToRadians;

float acosx(const float angle);

void KSensor::readerThreadFunc() {
    LOGV("ksensor reader starting up");

    Quaternion q;
    KTrackerSensorZip data;

    while (processing_flag_) {
        if (!pollSensor(&data)) {
            if (fd_ >= 0) {
                close(fd_);
                fd_ = -1;
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(50));
            continue;
        }

        vec3 corrected_gyro;
        long long currentTime = getCurrentTime();
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

    LOGV("ksensor reader shut down");
}

KSensor::KSensor() :
        fd_(-1), q_(), first_(true), step_(1), first_real_time_delta_(0.0f), last_timestamp_(0), full_timestamp_(0), last_sample_count_(
                0), latest_time_(0), last_acceleration_(0.0f, 0.0f, 0.0f), last_rotation_rate_(0.0f, 0.0f, 0.0f), last_corrected_gyro_(
                0.0f, 0.0f, 0.0f), gyro_offset_(0.0f, 0.0f, 0.0f), tilt_filter_(), processing_flag_(true), processing_thread_(), referencePoint_(
                std::make_pair(vec3(0, 0, 0), Quaternion())) {
}

KSensor::~KSensor() {
    ASensorManager* sensorManager = ASensorManager_getInstance();

    if (nullptr != magneticSensorQueue) {
        ASensorEventQueue_disableSensor(magneticSensorQueue, magneticSensor);
        ASensorManager_destroyEventQueue(sensorManager, magneticSensorQueue);
    }
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
    if (fd_ < 0) {
        fd_ = open("/dev/ovr0", O_RDONLY);
    }
    if (fd_ < 0) {
        return false;
    }

    //thr priority

    struct pollfd pfds;
    pfds.fd = fd_;
    pfds.events = POLLIN;

    uint8_t buffer[100];
    int n = poll(&pfds, 1, 100);
    if (n > 0 && (pfds.revents & POLLIN)) {
        int r = read(fd_, buffer, 100);
        if (r < 0) {
            LOGI("OnSensorEvent() read error %d", r);
            return false;
        }

        data->SampleCount = buffer[1];
        data->Timestamp = (uint16_t) (*(buffer + 3) << 8) | (uint16_t) (*(buffer + 2));
        data->LastCommandID = (uint16_t) (*(buffer + 5) << 8) | (uint16_t) (*(buffer + 4));
        data->Temperature = (int16_t) (*(buffer + 7) << 8) | (int16_t) (*(buffer + 6));

        for (int i = 0; i < (data->SampleCount > 3 ? 3 : data->SampleCount); ++i) {
            struct {
                int32_t x :21;
            } s;

            data->Samples[i].AccelX = s.x = (buffer[0 + 8 + 16 * i] << 13) | (buffer[1 + 8 + 16 * i] << 5)
                    | ((buffer[2 + 8 + 16 * i] & 0xF8) >> 3);
            data->Samples[i].AccelY = s.x = ((buffer[2 + 8 + 16 * i] & 0x07) << 18) | (buffer[3 + 8 + 16 * i] << 10)
                    | (buffer[4 + 8 + 16 * i] << 2) | ((buffer[5 + 8 + 16 * i] & 0xC0) >> 6);
            data->Samples[i].AccelZ = s.x = ((buffer[5 + 8 + 16 * i] & 0x3F) << 15) | (buffer[6 + 8 + 16 * i] << 7)
                    | (buffer[7 + 8 + 16 * i] >> 1);

            data->Samples[i].GyroX = s.x = (buffer[0 + 16 + 16 * i] << 13) | (buffer[1 + 16 + 16 * i] << 5)
                    | ((buffer[2 + 16 + 16 * i] & 0xF8) >> 3);
            data->Samples[i].GyroY = s.x = ((buffer[2 + 16 + 16 * i] & 0x07) << 18) | (buffer[3 + 16 + 16 * i] << 10)
                    | (buffer[4 + 16 + 16 * i] << 2) | ((buffer[5 + 16 + 16 * i] & 0xC0) >> 6);
            data->Samples[i].GyroZ = s.x = ((buffer[5 + 16 + 16 * i] & 0x3F) << 15) | (buffer[6 + 16 + 16 * i] << 7)
                    | (buffer[7 + 16 + 16 * i] >> 1);

        }

        data->MagX = (int16_t) (*(buffer + 57) << 8) | (int16_t) (*(buffer + 56));
        data->MagY = (int16_t) (*(buffer + 59) << 8) | (int16_t) (*(buffer + 58));
        data->MagZ = (int16_t) (*(buffer + 61) << 8) | (int16_t) (*(buffer + 60));
        return true;
    }
    return false;
}

void KSensor::process(KTrackerSensorZip* data, vec3& corrected_gyro, Quaternion& q) {
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
            timestampDelta = ((((int) data->Timestamp) + 0x10000) - (int) last_timestamp_);
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
        absoluteTimeSeconds = full_timestamp_ * timeUnit + first_real_time_delta_;

        // If we missed a small number of samples, replicate the last sample.
        if ((timestampDelta > last_sample_count_) && (timestampDelta <= 254)) {
            KTrackerMessage sensors;
            sensors.TimeDelta = (timestampDelta - last_sample_count_) * timeUnit;
            sensors.Acceleration = last_acceleration_;
            sensors.RotationRate = last_rotation_rate_;
            //sensors.MagneticField =

            updateQ(&sensors, corrected_gyro, q);
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
        sensors.Acceleration = vec3(data->Samples[i].AccelX, data->Samples[i].AccelY, data->Samples[i].AccelZ)
                * 0.0001f;
        sensors.RotationRate = vec3(data->Samples[i].GyroX, data->Samples[i].GyroY, data->Samples[i].GyroZ) * 0.0001f;
        sensors.MagneticField = vec3(data->MagX, data->MagY, data->MagZ);

        updateQ(&sensors, corrected_gyro, q);

        // TimeDelta for the last two sample is always fixed.
        sensors.TimeDelta = timeUnit;
    }

    last_sample_count_ = data->SampleCount;
    last_timestamp_ = data->Timestamp;
    last_acceleration_ = sensors.Acceleration;
    last_rotation_rate_ = sensors.RotationRate;
}

void KSensor::updateQ(KTrackerMessage *msg, vec3& corrected_gyro, Quaternion& q) {
    // Put the sensor readings into convenient local variables
    vec3 gyro = msg->RotationRate;
    vec3 accel = msg->Acceleration;

    const float deltaT = msg->TimeDelta;
    std::pair<vec3, bool> result = tilt_correction(gyro, accel, deltaT, q, msg->MagneticField);
    corrected_gyro = result.first;
    auto accelerating = result.second;

    // Update the orientation quaternion based on the corrected angular velocity vector
    float gyro_length = corrected_gyro.Length();
    if (gyro_length != 0.0f) {
        float angle = gyro_length * deltaT;
        q = q * Quaternion(cos(angle * 0.5f), corrected_gyro.Normalized() * sin(angle * 0.5f));
    }

    step_++;

    // Normalize error
    if (step_ % 500 == 0) {
        q.Normalize();
    }

    if (yawCorrectionEnabled) {
        LOG_INCREMENT();
        if (accelerating) {
            yawCorrectionTimer = 0;
        }

        yawCorrectionTimer += deltaT;
        if (yawCorrectionTimer > KYawDriftCorrection_TiltCorrectionWaitInSeconds) {
            if (gyro.Length() < KYawDriftCorrection_AngularVelocityThreshold) {
                yawCorrectionWithMagnetometer(q, deltaT);
            } else {
                LOG_INFO("ksensor: no yaw correction applied; waiting for angular velocity to drop");
            }
        } else {
            LOG_INFO("ksensor: no yaw correction applied; waiting for tilt correction to settle down");
        }
    }
}

/**
 * Keeps it level
 */
std::pair<vec3, bool> KSensor::tilt_correction(const vec3& gyro, const vec3& accel, const float DeltaT, Quaternion& q,
        vec3& magneticField) {
    // Small preprocessing
    Quaternion Qinv = q.Inverted(); //rotation in opposite direction
    vec3 up = Qinv.Rotate(vec3(0, 1, 0));
    vec3 gyroCorrected = gyro;

    bool EnableGravity = true;
    bool valid_accel = accel.Length() > 0.001f;
    auto accelerating = false;

    if (EnableGravity && valid_accel) {
        gyroCorrected -= gyro_offset_;

        const float spikeThreshold = 0.01f;
        const float gravityThreshold = 0.1f;
        float proportionalGain = 0.25f, integralGain = 0.0f;

        vec3 accel_normalize = accel.Normalized();
        vec3 up_normalize = up.Normalized();
        vec3 correction_rotation_axis = accel_normalize.Cross(up_normalize);
        float cosPhi = accel_normalize.Dot(up_normalize);

        const float Tolerance = 0.00001f;
        vec3 tiltCorrection = correction_rotation_axis * sqrtf(2.0f / (1 + cosPhi + Tolerance));

        if (step_ > 5) {
            // Spike detection
            float tiltAngle = up.Angle(accel);
            tilt_filter_.AddElement(tiltAngle);
            if (tiltAngle > tilt_filter_.Mean() + spikeThreshold) {
                proportionalGain = integralGain = 0;
            }

            // Acceleration detection
            const float gravity = 9.80665f;
            if (fabs(accel.Length() / gravity - 1) > gravityThreshold) {
                integralGain = 0;
                accelerating = true;
            }
        } else {
            // Apply full correction at the startup
            proportionalGain = 1 / DeltaT;
            integralGain = 0;
        }

        gyroCorrected += (tiltCorrection * proportionalGain);
        gyro_offset_ -= (tiltCorrection * integralGain * DeltaT);
    } else {
        LOGE("invalidaccel");
    }
    return std::make_pair(gyroCorrected, accelerating);
}

/**
 * Gyro drifts; magnetometer can give a stable magnetic north vector that acts as a
 * reference to guesstimate a drift correction.
 *
 * Using the phone magnetometer as it is continuously calibrated. The headset's is
 * useless unless calibrated which cannot be guaranteed.
 */
void KSensor::yawCorrectionWithMagnetometer(Quaternion& orientation, float deltaT) {
    LOG_VERBOSE("ksensor: ------------------------------------------------------------------------");
    getLatestMagneticField();
    if (0 == magnetic.Length()) {
        return;
    }

    LOG_VERBOSE("ksensor: magnetic %f %f %f, magneticBias %f %f %f", magnetic.x, magnetic.y, magnetic.z, magneticBias.x,
            magneticBias.y, magneticBias.z);

    bool rememberReferencePoint = true;
    // get the angle between the remembered orientation and this orientation
    auto angleRadians = 2 * acosx(fabs(orientation.Dot(referencePoint_.second)));
    LOG_INFO("ksensor: degrees to reference %f", angleRadians * KRadiansToDegrees);

    if (angleRadians < 5.0f * KDegreesToRadians && 0 != referencePoint_.first.Length()) {
        //use always the most up-to-date bias for improved calibration
        const auto calibrated = magnetic - magneticBias;
        auto worldFrame = orientation.Rotate(calibrated);
        if (worldFrame.x * worldFrame.x + worldFrame.z * worldFrame.z < KVerySmallValue) {
            LOG_INFO("ksensor: horizontal component too small ***");
            return;
        }
        worldFrame.Normalize();
        LOG_VERBOSE("ksensor: worldFrame %f %f %f", worldFrame.x, worldFrame.y, worldFrame.z);

        const auto referenceCalibrated = referencePoint_.first - magneticBias;
        auto referenceWorldFrame = referencePoint_.second.Rotate(referenceCalibrated);
        referenceWorldFrame.Normalize();
        LOG_VERBOSE("ksensor: referenceWorldFrame %f %f %f", referenceWorldFrame.x, referenceWorldFrame.y,
                referenceWorldFrame.z);

        if (fabs(referenceWorldFrame.y - worldFrame.y) > KYawDriftCorrection_MaxTiltDifference) {
            LOG_INFO("ksensor: too big of a tilt difference ***");
            return;
        }

        // compute in the horizontal plane
        referenceWorldFrame.y = worldFrame.y = 0;
        auto errorAngleInRadians = referenceWorldFrame.Angle(worldFrame);
        if (isnan(errorAngleInRadians)) {
            LOG_INFO("ksensor: no correction; current and reference orientation identical ***");
            return;
        }
        if (worldFrame.Cross(referenceWorldFrame).y < 0.0f) {
            errorAngleInRadians *= -1.0f;
        }
        LOG_INFO("ksensor: error in degrees %f", errorAngleInRadians * KRadiansToDegrees);

        //0.5f happens to work; guesstimate
        float correction = errorAngleInRadians * 0.5f;
        correction = std::max(-KYawCorrectionMaxRadians, std::min(KYawCorrectionMaxRadians, correction));
        LOG_VERBOSE("ksensor: correction %f rad", correction);

        correction *= deltaT;
        LOG_VERBOSE("ksensor: correction %f rad/ms", correction);
        orientation = Quaternion::CreateFromAxisAngle(vec3(0.0f, 1.0f, 0.0f), correction) * orientation;

        rememberReferencePoint = false;
    }

    if (rememberReferencePoint) {
        referencePoint_ = std::make_pair(magnetic, orientation);
    }
}

const int TYPE_MAGNETIC_FIELD_UNCALIBRATED = 14;
bool KSensor::getLatestMagneticField() {
    bool fieldRetrieved = false;
    if (nullptr == magneticSensorQueue) {
        // Initialize on same thread as read.
        ASensorManager* sensorManager = ASensorManager_getInstance();

        ALooper* looper = ALooper_forThread();
        if (looper == nullptr) {
            looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
        }

        magneticSensor = ASensorManager_getDefaultSensor(sensorManager, TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        if (nullptr != magneticSensor) {
            magneticSensorQueue = ASensorManager_createEventQueue(sensorManager, looper, 1, NULL, NULL);

            auto error = ASensorEventQueue_enableSensor(magneticSensorQueue, magneticSensor);
            if (0 > error) {
                LOGE("ksensor-error: enableSensor failed with %d", error);
            } else {
                error = ASensorEventQueue_setEventRate(magneticSensorQueue, magneticSensor,
                        ASensor_getMinDelay(magneticSensor));
                if (0 > error) {
                    LOGE("ksensor-error: setEventRate failed with %d", error);
                }
            }
        } else {
            LOGE("ksensor-error: getDefaultSensor failed for sensor type %d", TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        }
    }

    int ident;  // Identifier.
    int events;

    while ((ident = ALooper_pollAll(0, NULL, &events, NULL) >= 0)) {
        if (ident == 1) {
            ASensorEvent event;
            while (ASensorEventQueue_getEvents(magneticSensorQueue, &event, 1) > 0) {
                if (event.type == TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
                    magnetic.x = event.uncalibrated_magnetic.x_uncalib;
                    magnetic.y = event.uncalibrated_magnetic.y_uncalib;
                    magnetic.z = event.uncalibrated_magnetic.z_uncalib;
                    magneticBias.x = event.uncalibrated_magnetic.x_bias;
                    magneticBias.y = event.uncalibrated_magnetic.y_bias;
                    magneticBias.z = event.uncalibrated_magnetic.z_bias;
                    fieldRetrieved = true;
                }
            }
        }
    }

    return fieldRetrieved;
}

/**
 * @param angle in radians
 */
float acosx(const float angle) {
    if (angle > 1) {
        return 0;
    } else if (angle < -1) {
        return M_PI;
    } else {
        return acos(angle);
    }
}

}
