#include "KTrackerSensorModule.h"


#include <math.h>
#include <time.h>
#include <sys/ioctl.h>
#include <poll.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <utils/Log.h>

using namespace GVR;


KTrackerSensorModule::KTrackerSensorModule() : step(0), first(false), first_real_time_delta(0), GyroOffset(0, 0, 0),
		LastTimestamp(0), LastSampleCount(0), LastAcceleration(0, 0, 0), LastRotationRate(0, 0, 0), FullTimestamp(0), mFd(-1) {


	coordinate_transform = Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
	offset_transform = Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
	yaw_complementary_rotation_ = Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
	raw_rotation_ = Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
	raw_angular_velocity_ = vec3(1.0f, 0.0f, 0.0f);
	StopReadingData = false;



}


void KTrackerSensorModule::setReadThreadStatus(bool bReadData){
		StopReadingData = bReadData;
	}


KTrackerSensorModule::~KTrackerSensorModule() {
	closeDevice();
}

void KTrackerSensorModule::closeDevice(){
	if (mFd >= 0){
		close(mFd);
	}
	mFd = -1;
}


vec3 KTrackerSensorModule::GetAngularVelocity() {
	return LastCorrectedGyro;
}

Quaternion KTrackerSensorModule::GetSensorQuaternion() {
	KTrackerSensorZip data;
	if (readfile(&data)){
		process(&data);
		raw_rotation_ = Quaternion(Q.x, Q.y, Q.z,Q.w);
		Q = raw_rotation_; //yaw_complementary_rotation_ * coordinate_transform.Inverse()* offset_transform * raw_rotation_ * coordinate_transform;	
	}else{
		Q = Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
	}
	
	return Q;

/*
	if(firstTime) {
		Quaternion rotation = coordinate_transform.Inverse()
						   * offset_transform
						   * raw_rotation_
						   * coordinate_transform;
		
		vec3 view_dir = rotation.Rotate_(vec3(0.0f, 0.0f, -1.0f));//glm::rotate(rotation, glm::vec3(0.0f, 0.0f, -1.0f));
		float rotated_degree = atan2(-view_dir.x, -view_dir.z);
		yaw_complementary_rotation_ = Quaternion(1.0f,vec3(0.0f, -rotated_degree, 0.0f));

		firstTime = false;
	}

	vec3 axis;
	axis.x = LastCorrectedGyro.x;
	axis.y = LastCorrectedGyro.y;
	axis.z = LastCorrectedGyro.z;
	
	float angle = axis.Length()
	
	if(angle != 0.0f)
	{
		axis /= angle;
	}
	float delta_t = 1.0 / 60.0f;
	angle *= delta_t * 180.0f / 3.14f;

	//K tracker accumulates the rotation on the right-side.
	Quaternion rotation = raw_rotation_ ;//* glm::angleAxis(angle, axis);
*/

	
	
}

vec3 KTrackerSensorModule::GetSensorEulerAngle() {
	KTrackerSensorZip data;
	if (readfile(&data)) process(&data);
	return Q.ToEulerAngle();
}

Quaternion KTrackerSensorModule::GetSensorPredictionQuaternion(float DeltaT) {
	Quaternion newQ;

	float gyro_length = LastCorrectedGyro.Length();
	if (gyro_length != 0.0f) newQ = Q * Quaternion(LastCorrectedGyro.x, LastCorrectedGyro.y, LastCorrectedGyro.z, gyro_length * DeltaT);
	else newQ = Q;

	return newQ;
}

void KTrackerSensorModule::process(KTrackerSensorZip* data) {
	const float timeUnit = (1.0f / 1000.f);

	struct timespec tp;
	clock_gettime(CLOCK_MONOTONIC, &tp);
	const double now = tp.tv_sec + tp.tv_nsec * 0.000000001;

	double absoluteTimeSeconds = 0.0;

	if (first) {
		LastAcceleration = vec3(0, 0, 0);
		LastRotationRate = vec3(0, 0, 0);
		first = true;

		// This is our baseline sensor to host time delta,
		// it will be adjusted with each new message.
		FullTimestamp = data->Timestamp;
		first_real_time_delta = now - (FullTimestamp * timeUnit);
	}
	else {
		unsigned timestampDelta;

		if (data->Timestamp < LastTimestamp) {
			// The timestamp rolled around the 16 bit counter, so FullTimeStamp
			// needs a high word increment.
			FullTimestamp += 0x10000;
			timestampDelta = ((((int) data->Timestamp) + 0x10000)
					- (int) LastTimestamp);
		} else {
			timestampDelta = (data->Timestamp - LastTimestamp);
		}
		// Update the low word of FullTimeStamp
		FullTimestamp = (FullTimestamp & ~0xffff) | data->Timestamp;

		// If this timestamp, adjusted by our best known delta, would
		// have the message arriving in the future, we need to adjust
		// the delta down.
		if (FullTimestamp * timeUnit + first_real_time_delta > now) {
			first_real_time_delta = now - (FullTimestamp * timeUnit);
		} else {
			// Creep the delta by 100 microseconds so we are always pushing
			// it slightly towards the high clamping case, instead of having to
			// worry about clock drift in both directions.
			first_real_time_delta += 0.0001;
		}
		// This will be considered the absolute time of the last sample in
		// the message.  If we are double or tripple stepping the samples,
		// their absolute times will be adjusted backwards.
		absoluteTimeSeconds = FullTimestamp * timeUnit + first_real_time_delta;

		// If we missed a small number of samples, replicate the last sample.
		if ((timestampDelta > LastSampleCount) && (timestampDelta <= 254)) {
			KTrackerMessage sensors;
			sensors.TimeDelta = (timestampDelta - LastSampleCount) * timeUnit;
			sensors.Acceleration = LastAcceleration;
			sensors.RotationRate = LastRotationRate;

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
		sensors.Acceleration = vec3(data->Samples[i].AccelX, data->Samples[i].AccelY, data->Samples[i].AccelZ) * 0.0001f;
		sensors.RotationRate = vec3(data->Samples[i].GyroX, data->Samples[i].GyroY, data->Samples[i].GyroZ) * 0.0001f;

		updateQ(&sensors);

		// TimeDelta for the last two sample is always fixed.
		sensors.TimeDelta = timeUnit;
	}

	LastSampleCount = data->SampleCount;
	LastTimestamp = data->Timestamp;
	LastAcceleration = sensors.Acceleration;
	LastRotationRate = sensors.RotationRate;

}

bool KTrackerSensorModule::readfile(KTrackerSensorZip* data) {


	if (mFd < 0){
		mFd = open("/dev/ovr0", O_RDONLY);
	}
	if (mFd < 0) {
		closeDevice();
		return false;
	}

	struct pollfd pfds;
	pfds.fd = mFd;
	pfds.events = POLLIN;

	int n = poll(&pfds, 1, 100);
	if (n > 0 && (pfds.revents & POLLIN)) {
		uint8_t buffer[100];
		int r = read(mFd, buffer, 100);
		if (r < 0) {
			closeDevice();
			return false;
		}else{
			data->SampleCount 	= buffer[1];
			data->Timestamp 	= (uint16_t)( *(buffer+3) << 8) | (uint16_t)( *(buffer+2));
			data->LastCommandID = (uint16_t)( *(buffer+5) << 8) | (uint16_t)( *(buffer+4));
			data->Temperature 	= (int16_t)( *(buffer+7) << 8) |  (int16_t)( *(buffer+6));

			for (int i=0;i<(data->SampleCount>3? 3: data->SampleCount);++i) {
				struct {
					int32_t x :21;
				} s;

				data->Samples[i].AccelX = s.x = (buffer[0 + 8 + 16 * i] << 13) | (buffer[1 + 8 + 16 * i] << 5) | ((buffer[2 + 8 + 16 * i] & 0xF8) >> 3);
				data->Samples[i].AccelY = s.x = ((buffer[2 + 8 + 16 * i] & 0x07) << 18) | (buffer[3 + 8 + 16 * i] << 10) | (buffer[4 + 8 + 16 * i] << 2) | ((buffer[5 + 8 + 16 * i] & 0xC0) >> 6);
				data->Samples[i].AccelZ = s.x = ((buffer[5 + 8 + 16 * i] & 0x3F) << 15) | (buffer[6 + 8 + 16 * i] << 7) | (buffer[7 + 8 + 16 * i] >> 1);

				data->Samples[i].GyroX = s.x = (buffer[0 + 16 + 16 * i] << 13) | (buffer[1 + 16 + 16 * i] << 5) | ((buffer[2 + 16 + 16 * i] & 0xF8) >> 3);
				data->Samples[i].GyroY = s.x = ((buffer[2 + 16 + 16 * i] & 0x07) << 18) | (buffer[3 + 16 + 16 * i] << 10) | (buffer[4 + 16 + 16 * i] << 2) | ((buffer[5 + 16 + 16 * i] & 0xC0) >> 6);
				data->Samples[i].GyroZ = s.x = ((buffer[5 + 16 + 16 * i] & 0x3F) << 15) | (buffer[6 + 16 + 16 * i] << 7) | (buffer[7 + 16 + 16 * i] >> 1);

			}

			data->MagX = (int16_t)(*(buffer+57) << 8) | (int16_t)(*(buffer+56));
			data->MagY = (int16_t)(*(buffer+59) << 8) | (int16_t)(*(buffer+58));
			data->MagZ = (int16_t)(*(buffer+61) << 8) | (int16_t)(*(buffer+60));
			return true;
		}
	}else{
		closeDevice();
		return false;
	}

}

void KTrackerSensorModule::updateQ(KTrackerMessage *msg) {
	// Put the sensor readings into convenient local variables
	vec3 gyro = msg->RotationRate;
	vec3 accel = msg->Acceleration;
	const float DeltaT = msg->TimeDelta;
	vec3 gyroCorrected = gyrocorrect(gyro, accel, DeltaT);
	LastCorrectedGyro = gyroCorrected;

	// Update the orientation quaternion based on the corrected angular velocity vector
	float gyro_length = gyroCorrected.Length();
	if (gyro_length != 0.0f) {
		float angle = gyro_length * DeltaT;
		Q = Q * Quaternion(cos(angle * 0.5f), gyroCorrected.Normalized() * sin(angle * 0.5f));
	}

	step++;

	// Normalize error
	if (step % 500 == 0) {
		Q.Normalize();
	}
}

vec3 KTrackerSensorModule::gyrocorrect(vec3 gyro, vec3 accel, const float DeltaT) {
	// Small preprocessing
	Quaternion Qinv = Q.Inverted();
	vec3 up = Qinv.Rotate(vec3(0, 1, 0));
	vec3 gyroCorrected = gyro;

	bool EnableGravity = true;

	if (EnableGravity) {
		gyroCorrected -= GyroOffset;

		const float spikeThreshold = 0.01f;
		const float gravityThreshold = 0.1f;
		float proportionalGain = 0.25f, integralGain = 0.0f;

		vec3 accel_normalize = accel.Normalized();
		vec3 up_normalize    = up.Normalized();
		vec3 correction = accel_normalize.Cross(up_normalize);
		float cosError = accel_normalize.Dot(up_normalize);
		const float Tolerance = 0.00001f;
		vec3 tiltCorrection = correction * sqrtf(2.0f / (1 + cosError + Tolerance));

		if (step > 5) {
			// Spike detection
			float tiltAngle = up.Angle(accel);
			tiltFilter.AddElement(tiltAngle);
			if (tiltAngle > tiltFilter.Mean() + spikeThreshold)
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
		GyroOffset -= (tiltCorrection * integralGain * DeltaT);
	}

	return gyroCorrected;
}

