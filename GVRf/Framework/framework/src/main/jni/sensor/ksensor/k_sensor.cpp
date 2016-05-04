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
#include <glm/glm.hpp>
#include <glm/gtx/vector_angle.hpp>
#include <jni.h>

namespace gvr {

#define FEATURE_MAGNETOMETER_YAW_CORRECTION

#define HIDIOCGFEATURE(len)    _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x07, len)
//#define LOG_SUMMARY
//#define LOG_GYRO_FILTER
//#define LOG_MAGNETOMETER_CORRECTION
//#define LOG_TILT_CORRECTION

float acosx(const float angle);

const float KTiltCorrectionWaitInSeconds = 2.0;

void KSensor::readerThreadFunc() {
    pid_t tid = gettid();
    LOGV("k_sensor: reader starting up; tid: %d", tid);

    pthread_setname_np(pthread_self(), "ksensor");

    readFactoryCalibration();
    openOvrDevice();

    glm::quat q;
    glm::vec3 corrected_gyro;
    long long currentTime;
    KTrackerSensorZip data;

    while (processing_flag_) {
        if (!pollSensor(&data)) {
            continue;
        }

        currentTime = getCurrentTime();
        process(&data, corrected_gyro, q);

        std::lock_guard < std::mutex > lock(update_mutex_);
        latest_time_ = currentTime;
        last_corrected_gyro_ = corrected_gyro;
        q_ = q;
    }

    if (fd_ >= 0) {
        close(fd_);
        fd_ = -1;
    }

    if (nullptr != magneticSensorQueue) {
        ASensorEventQueue_disableSensor(magneticSensorQueue, magneticSensor);
        ASensorManager_destroyEventQueue(ASensorManager_getInstance(), magneticSensorQueue);
    }

    LOGV("k_sensor: reader shut down");
}

KSensor::KSensor() :
        fd_(-1), q_(), first_(true), step_(0), first_real_time_delta_(0.0f), last_timestamp_(0), full_timestamp_(0), last_sample_count_(
                0), latest_time_(0), last_acceleration_(0.0f, 0.0f, 0.0f), processing_thread_(), gyroFilter_(
                KGyroNoiseFilterCapacity), tiltFilter_(25), processing_flag_(true) {
}

KSensor::~KSensor() {
    try {
        stop();
    } catch (const std::exception&) {
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
    struct pollfd pfds;
    pfds.fd = fd_;
    pfds.events = POLLIN | POLLHUP | POLLERR;

    uint8_t buffer[100];
    int n = poll(&pfds, 1, 100);
    if (n > 0 && (pfds.revents & POLLIN)) {
        int r = read(fd_, buffer, 100);
        if (r < 0) {
            LOGI("k_sensor: read error %d", r);
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

        return true;
    } else if (0 < n && (pfds.revents & POLLHUP)) {
        LOGI("k_sensor: received POLLHUP, will try to reopen device until interrupted");
        last_rotation_rate_ = last_acceleration_ = last_corrected_gyro_ = glm::vec3(0, 0, 0);
        openOvrDevice();
    }
    return false;
}

void KSensor::process(KTrackerSensorZip* data, glm::vec3& corrected_gyro, glm::quat& q) {
    const float timeUnit = (1.0f / 1000.f);

    struct timespec tp;
    clock_gettime(CLOCK_MONOTONIC, &tp);
    const double now = tp.tv_sec + tp.tv_nsec * 0.000000001;

    double absoluteTimeSeconds = 0.0;

    if (first_) {
        last_acceleration_ = glm::vec3();
        last_rotation_rate_ = glm::vec3();
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

            updateQ(&sensors, corrected_gyro, q);
        }
    }

    KTrackerMessage sensors;
    int iterations = data->SampleCount;
    sensors.Temperature = (0 == data->Temperature ? 0 : data->Temperature * 0.01f);

    if (data->SampleCount > 3) {
        iterations = 3;
        sensors.TimeDelta = (data->SampleCount - 2) * timeUnit;
    } else {
        sensors.TimeDelta = timeUnit;
    }

    const float scale = 0.0001f;
    for (int i = 0; i < iterations; ++i) {
        sensors.Acceleration = glm::vec3(data->Samples[i].AccelX * scale, data->Samples[i].AccelY * scale,
                data->Samples[i].AccelZ * scale);
        sensors.RotationRate = glm::vec3(data->Samples[i].GyroX * scale, data->Samples[i].GyroY * scale,
                data->Samples[i].GyroZ * scale);

        updateQ(&sensors, corrected_gyro, q);

        // TimeDelta for the last two sample is always fixed.
        sensors.TimeDelta = timeUnit;
    }

    last_sample_count_ = data->SampleCount;
    last_timestamp_ = data->Timestamp;
    last_acceleration_ = sensors.Acceleration;
    last_rotation_rate_ = sensors.RotationRate;
}

void KSensor::updateQ(KTrackerMessage *msg, glm::vec3& corrected_gyro, glm::quat& q) {
    glm::vec3 filteredGyro = applyGyroFilter(msg->RotationRate, msg->Temperature);

    const float deltaT = msg->TimeDelta;
    std::pair<glm::vec3, float> axisAndMagnitude = applyTiltCorrection(filteredGyro, msg->Acceleration, deltaT, q);

    if (0.0f != axisAndMagnitude.second) {
        q *= glm::angleAxis(glm::degrees(axisAndMagnitude.second * deltaT), axisAndMagnitude.first);
    }

#ifdef LOG_SUMMARY
    if (0 == step_ % 1000) {
        LOGI("k_sensor: summary: yaw angle %f; gyro offset %f %f %f; temperature %f; gyro filter sizes %d",
                glm::yaw(q), gyroOffset_.x, gyroOffset_.y, gyroOffset_.z, msg->Temperature,
                gyroFilter_.size());
    }
#endif

#ifdef FEATURE_MAGNETOMETER_YAW_CORRECTION
    q = applyMagnetometerCorrection(q, msg->Acceleration, filteredGyro, deltaT);
#endif

    corrected_gyro = filteredGyro;

    step_++;
    // Normalize error
    if (step_ % 500 == 0) {
        q = glm::normalize(q);
    }
}

glm::vec3 transform(const glm::mat4& m, const glm::vec3& v) {
    const auto rcpW = 1 / (m[3][0] * v.x + m[3][1] * v.y + m[3][2] * v.z + m[3][3]);
    return glm::vec3((m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z + m[0][3]) * rcpW,
            (m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z + m[1][3]) * rcpW,
            (m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z + m[2][3]) * rcpW);
}

/**
 * Tries to determine the gyro noise and subtract it from the actual reading to
 * reduce the amount of yaw drift.
 */
glm::vec3 KSensor::applyGyroFilter(const glm::vec3& rawGyro, const float currentTemperature) {
    const int gyroFilterSize = gyroFilter_.size();
    if (gyroFilterSize > KGyroNoiseFilterCapacity / 2) {
        gyroOffset_ = gyroFilter_.mean();
        if (0 != currentTemperature) {
            sensorTemperature_ = currentTemperature;
        }
    }

    if (gyroFilterSize > 0) {
        if (!isnan(sensorTemperature_) && sensorTemperature_ != currentTemperature && 0 != currentTemperature) {
#ifdef LOG_GYRO_FILTER
            LOGI("k_sensor: clear gyro filter due to temperature change %f %f", sensorTemperature_, currentTemperature);
#endif
            gyroFilter_.clear();
            sensorTemperature_ = currentTemperature;
        } else {
            const glm::vec3& mean = gyroFilter_.mean();
            // magic values that happen to work
            bool c1 = glm::length(rawGyro) > 1.25f * 0.349066f;
            bool c2 = glm::length(rawGyro - mean) > 0.022f;
            if (c1 || c2) {
                gyroFilter_.clear();

#ifdef LOG_GYRO_FILTER
                LOGI("k_sensor: clear gyro filter due to motion; abs magnitude: %d, rel magnitude %d", c1, c2);
#endif
            }
        }
    }

    const float alpha = 0.4f;
    const glm::vec3 avg = (0 == gyroFilterSize) ? rawGyro : rawGyro * alpha + gyroFilter_.peekBack() * (1 - alpha);
    gyroFilter_.push(avg);

    return transform(factoryGyroMatrix_, rawGyro - gyroOffset_);
}

std::pair<glm::vec3, float> KSensor::applyTiltCorrection(const glm::vec3& gyro, const glm::vec3& accel,
        const float deltaT, const glm::quat& q) {
    tiltCorrectionTimer += deltaT;
    if (glm::length(accel) <= 0.001f) {
        return std::make_pair(glm::normalize(gyro), glm::length(gyro));
    }

    glm::quat Qinv = glm::conjugate(q);
    glm::vec3 up = glm::rotate(Qinv, glm::vec3(0, 1, 0));

    glm::vec3 accel_normalize = glm::normalize(accel);
    glm::vec3 up_normalize = glm::normalize(up);
    glm::vec3 correction = glm::cross(accel_normalize, up_normalize);

    float proportionalGain = 0.25f;
    bool fullCorrectionEnabled = tiltCorrectionTimer < KTiltCorrectionWaitInSeconds;
    if (!fullCorrectionEnabled) {
        // Spike detection
        float tiltAngle = glm::angle(up, accel);
        tiltFilter_.push(tiltAngle);

        const float spikeThreshold = 0.01f;
        if (tiltAngle > tiltFilter_.mean() + spikeThreshold) {
            proportionalGain = 0;
        }
    } else {
        // Apply full correction at the startup
        proportionalGain = KTiltCorrectionWaitInSeconds / tiltCorrectionTimer;
#ifdef LOG_TILE_CORRECTION
        LOGI("k_sensor: full tilt correction applied; %f", proportionalGain);
#endif
    }

    glm::vec3 gyroCorrected = gyro + (correction * proportionalGain);
    float magnitude = fullCorrectionEnabled ? glm::length(gyroCorrected) : glm::length(gyro);
    return std::make_pair(glm::normalize(gyroCorrected), magnitude);
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

/**
 * Using the phone magnetometer as it is continuously calibrated.
 */
glm::quat KSensor::applyMagnetometerCorrection(glm::quat& orientation, const glm::vec3& accelerometer,
        const glm::vec3& gyro, float deltaT) {
    magnetometerCorrectionTimer += deltaT;

    const float gravityThreshold = 0.1f;
    const float gravity = 9.80665f;
    if (fabs(glm::length(accelerometer) / gravity - 1) > gravityThreshold) {
        //linear acceleration detection
        magnetometerCorrectionTimer = 0;
        return orientation;
    }

    static const float angularVelocityThreshold = glm::radians(5.0f);
    if (magnetometerCorrectionTimer < 5.0 || glm::length(gyro) >= angularVelocityThreshold) {
#ifdef LOG_MAGNETOMETER_CORRECTION
        LOGI("ksensor: no yaw correction applied");
#endif
        return orientation;
    }

    getLatestMagneticField();
    if (0 == glm::length(magnetic)) {
        return orientation;
    }

    if (0 != glm::length(referencePoint_.first)) {
        // get the angle between the remembered orientation and this orientation
        const float dot = glm::dot(orientation, referencePoint_.second);
        float angleRadians = 2 * acosx(fabs(dot));

        glm::quat correctedOrientation = orientation;
        if (angleRadians < glm::radians(5.0f)) {
            //use always the most up-to-date bias for improved calibration
            glm::vec3 worldFrame = glm::rotate(orientation, magnetic - magneticBias);
            const float epsilon = 0.00001f;
            if (worldFrame.x * worldFrame.x + worldFrame.z * worldFrame.z < epsilon) {
#ifdef LOG_MAGNETOMETER_CORRECTION
                LOGI("k_sensor: horizontal component too small");
#endif
                return orientation;
            }
            worldFrame = glm::normalize(worldFrame);

            glm::vec3 referenceWorldFrame = glm::rotate(referencePoint_.second, referencePoint_.first - magneticBias);
            referenceWorldFrame = glm::normalize(referenceWorldFrame);

            const float maxTiltDifference = 0.15f;
            if (fabs(referenceWorldFrame.y - worldFrame.y) > maxTiltDifference) {
#ifdef LOG_MAGNETOMETER_CORRECTION
                LOGI("k_sensor: too big of a tilt difference");
#endif
                return orientation;
            }

            // compute in the horizontal plane
            referenceWorldFrame.y = worldFrame.y = 0;
            float errorAngleInRadians = glm::angle(referenceWorldFrame, worldFrame);
            if (isnan(errorAngleInRadians)) {
                return orientation;
            }
            if (glm::cross(worldFrame, referenceWorldFrame).y < 0.0f) {
                errorAngleInRadians *= -1.0f;
            }

            static const float maxCorrectionRadians = glm::radians(0.07f);
            static const float gain = maxCorrectionRadians / 5.0f;
            float correction = errorAngleInRadians * gain;
            correction = std::max(-maxCorrectionRadians, std::min(maxCorrectionRadians, correction));
            correction *= deltaT;
            correctedOrientation = glm::angleAxis(glm::degrees(correction), glm::vec3(0.0f, 1.0f, 0.0f)) * orientation;

#ifdef LOG_MAGNETOMETER_CORRECTION
            if (0 == step_ % 1500) {
                LOGI("k_sensor: magnetometer correction: %f degrees; angle to reference %f; reference yaw: %f; current yaw: %f; corrected yaw: %f",
                        glm::degrees(correction), errorAngleInRadians,
                        glm::yaw(referencePoint_.second),
                        glm::yaw(orientation),
                        glm::yaw(correctedOrientation));
            }
#endif
            return correctedOrientation;
        }
    }

    referencePoint_ = std::make_pair(magnetic, orientation);
#ifdef LOG_MAGNETOMETER_CORRECTION
    LOGI("k_sensor: saving reference point at uncorrected yaw %f", glm::yaw(orientation));
#endif
    return orientation;
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
                LOGE("k_sensor: error: enableSensor failed with %d", error);
            } else {
                error = ASensorEventQueue_setEventRate(magneticSensorQueue, magneticSensor,
                        ASensor_getMinDelay(magneticSensor));
                if (0 > error) {
                    LOGE("k_sensor: error: setEventRate failed with %d", error);
                }
            }
        } else {
            LOGE("k_sensor: error: getDefaultSensor failed for sensor type %d", TYPE_MAGNETIC_FIELD_UNCALIBRATED);
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
