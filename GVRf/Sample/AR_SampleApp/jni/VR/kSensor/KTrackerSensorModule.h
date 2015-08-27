#ifndef _KTRACKER_SENSOR_THREAD_H_
#define _KTRACKER_SENSOR_THREAD_H_

#include "ktracker_data_info.h"
#include "ktracker_device_info.h"
#include "ktracker_sensor_filter.h"
#include "math/quaternion.hpp"
#include "math/vector.hpp"

#include <sys/types.h>


namespace GVR {

class KTrackerSensorModule {

public:
	KTrackerSensorModule();
	~KTrackerSensorModule();

	vec3 GetAngularVelocity();
	void	setReadThreadStatus(bool bReadData);
	GVR::Quaternion GetSensorQuaternion();
	vec3 GetSensorEulerAngle();
	GVR::Quaternion GetSensorPredictionQuaternion(float DeltaT);

private:
	bool readfile(KTrackerSensorZip* data);
	void process(KTrackerSensorZip* data);
	void updateQ(KTrackerMessage* data);
	vec3 gyrocorrect(vec3 gyro, vec3 accel, const float DeltaT);
	void closeDevice();

private:
	int mHid;

	GVR::Quaternion Q;

// fusion
	int step;
	bool first;
	unsigned first_real_time_delta;
	vec3 GyroOffset;
	vec3 LastCorrectedGyro;
	bool StopReadingData;

	uint16_t LastTimestamp;
	uint8_t LastSampleCount;
	vec3 LastAcceleration;
	vec3 LastRotationRate;
	uint32_t FullTimestamp;
	SensorFilter<float> tiltFilter;

	GVR::Quaternion coordinate_transform;
	GVR::Quaternion offset_transform;
	GVR::Quaternion yaw_complementary_rotation_;
	GVR::Quaternion raw_rotation_;
	vec3 raw_angular_velocity_;
	
	int mFd;
};
}
#endif /* _KTRACKER_SENSOR_THREAD_H_ */
