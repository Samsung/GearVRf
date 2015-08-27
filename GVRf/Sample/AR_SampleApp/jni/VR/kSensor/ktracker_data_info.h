#ifndef KTRACKER_DATA_INFO_H_
#define KTRACKER_DATA_INFO_H_

#include "math/vector.hpp"
#include <sys/types.h>

namespace GVR {

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
