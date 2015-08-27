#include "VRInternalSensorModule.h"
#include <math.h>



using namespace GVR; 

#define LOOPER_ID 1

#define PI (3.1415926f)
#define EPSILON (0.000000001f)	

VRInternalSensorModule::VRInternalSensorModule() {
	mAcclX = 0.0;
	mAcclX = 0.0;
	mAcclX = 0.0;

	NS2S = 1.0f / 1000000000.0f;
	mTimeStamp = 0.0f;
	mGravity[0] = 0.0f;
	mGravity[1] = 0.0f;
	mGravity[2] = 0.0f;
		
	mGyro[0] = 0.0f;
	mGyro[1] = 0.0f;
	mGyro[2] = 0.0f;

	mGeomagnetic[0] = 0.0;
	mGeomagnetic[1] = 0.0;
	mGeomagnetic[2] = 0.0;

	mPreviousYaw = 0.0f;
	mCurrentYaw  = 0.0f;
	FILTER_COEFFICIENT = 0.98f;
	mAccMagOrientationReady = false;
	mInitState = true;
	mFirstY = 0.0f;

	mSensorManager = 0;
	mSensorEventQueue = 0;

	
}

float VRInternalSensorModule::GetCurrentYaw()
{ 
	return mCurrentYaw;
}

void VRInternalSensorModule::getRotationVectorFromGyro(float *gyroValues,float *deltaRotationVector,float timeFactor)
{
	float normValues[3];

	normValues[0] = 0.0f;
	normValues[1] = 0.0f;
	normValues[2] = 0.0f;

	// Calculate the angular speed of the sample
	float omegaMagnitude =
			(float)sqrt(gyroValues[0] * gyroValues[0] +
					gyroValues[1] * gyroValues[1] +
					gyroValues[2] * gyroValues[2]);

	// Normalize the rotation vector if it's big enough to get the axis
	if(omegaMagnitude > EPSILON) {
		normValues[0] = gyroValues[0] / omegaMagnitude;
		normValues[1] = gyroValues[1] / omegaMagnitude;
		normValues[2] = gyroValues[2] / omegaMagnitude;
	}

	// Integrate around this axis with the angular speed by the timestep
	// in order to get a delta rotation from this sample over the timestep
	// We will convert this axis-angle representation of the delta rotation
	// into a quaternion before turning it into the rotation matrix.
	float thetaOverTwo = omegaMagnitude * timeFactor;
	float sinThetaOverTwo = (float)sin(thetaOverTwo);
	float cosThetaOverTwo = (float)cos(thetaOverTwo);
	
	deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
	deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
	deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
	deltaRotationVector[3] = cosThetaOverTwo;
	
	calculateTask();
}

void VRInternalSensorModule::calculateTask()
{
	float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

	/*
	 * Fix for 179° <--> -179° transition problem:
	 * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
	 * If so, add 360° (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360° from the result
	 * if it is greater than 180°. This stabilizes the output in positive-to-negative-transition cases.
	 */

	// azimuth
	if (mGyroOrientation[0] < -0.5 * PI && mAccMagOrientation[0] > 0.0) {
		mFusedOrientation[0] = (float) (FILTER_COEFFICIENT * (mGyroOrientation[0] + 2.0 * PI) + oneMinusCoeff * mAccMagOrientation[0]);
		mFusedOrientation[0] -= (mFusedOrientation[0] > PI) ? 2.0 * PI : 0;
	}
	else if (mAccMagOrientation[0] < -0.5 * PI && mGyroOrientation[0] > 0.0) {
		mFusedOrientation[0] = (float) (FILTER_COEFFICIENT * mGyroOrientation[0] + oneMinusCoeff * (mAccMagOrientation[0] + 2.0 * PI));
		mFusedOrientation[0] -= (mFusedOrientation[0] > PI)? 2.0 * PI : 0;
	}
	else {
		mFusedOrientation[0] = FILTER_COEFFICIENT * mGyroOrientation[0] + oneMinusCoeff * mAccMagOrientation[0];
	}

	// pitch
	if (mGyroOrientation[1] < -0.5 * PI && mAccMagOrientation[1] > 0.0) {
		mFusedOrientation[1] = (float) (FILTER_COEFFICIENT * (mGyroOrientation[1] + 2.0 * PI) + oneMinusCoeff * mAccMagOrientation[1]);
		mFusedOrientation[1] -= (mFusedOrientation[1] > PI) ? 2.0 * PI : 0;
	}
	else if (mAccMagOrientation[1] < -0.5 * PI && mGyroOrientation[1] > 0.0) {
		mFusedOrientation[1] = (float) (FILTER_COEFFICIENT * mGyroOrientation[1] + oneMinusCoeff * (mAccMagOrientation[1] + 2.0 * PI));
		mFusedOrientation[1] -= (mFusedOrientation[1] > PI)? 2.0 * PI : 0;
	}
	else {
		mFusedOrientation[1] = FILTER_COEFFICIENT * mGyroOrientation[1] + oneMinusCoeff * mAccMagOrientation[1];
	}

	// roll
	if (mGyroOrientation[2] < -0.5 * PI && mAccMagOrientation[2] > 0.0) {
		mFusedOrientation[2] = (float) (FILTER_COEFFICIENT * (mGyroOrientation[2] + 2.0 * PI) + oneMinusCoeff * mAccMagOrientation[2]);
		mFusedOrientation[2] -= (mFusedOrientation[2] > PI) ? 2.0 * PI : 0;
	}
	else if (mAccMagOrientation[2] < -0.5 * PI && mGyroOrientation[2] > 0.0) {
		mFusedOrientation[2] = (float) (FILTER_COEFFICIENT * mGyroOrientation[2] + oneMinusCoeff * (mAccMagOrientation[2] + 2.0 * PI));
		mFusedOrientation[2] -= (mFusedOrientation[2] > PI)? 2.0 * PI : 0;
	}
	else {
		mFusedOrientation[2] = FILTER_COEFFICIENT * mGyroOrientation[2] + oneMinusCoeff * mAccMagOrientation[2];
	}

	// overwrite gyro matrix and orientation with fused orientation
	// to comensate gyro drift
	getRotationMatrixFromOrientation(mFusedOrientation, mGyroMatrix);

	mFusedOrientation[0] = mGyroOrientation[0];
	mFusedOrientation[1] = mGyroOrientation[1];
	mFusedOrientation[2] = mGyroOrientation[2];
	
	callbackInternalFusedData(mGyroOrientation);
}

void VRInternalSensorModule::getRotationMatrixFromOrientation(float *o, float* result)
{
	float xM[9];
	float yM[9];
	float zM[9];

	float sinX = (float)sin(o[1]);
	float cosX = (float)cos(o[1]);
	float sinY = (float)sin(o[2]);
	float cosY = (float)cos(o[2]);
	float sinZ = (float)sin(o[0]);
	float cosZ = (float)cos(o[0]);

	// rotation about x-axis (pitch)
	xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
	xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
	xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

	// rotation about y-axis (roll)
	yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
	yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
	yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

	// rotation about z-axis (azimuth)
	zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
	zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
	zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

	// rotation order is y, x, z (roll, pitch, azimuth)
	float resultMatrix[9];
	matrixMultiplication(xM, yM,resultMatrix);
	matrixMultiplication(zM, resultMatrix,result);
	return ;
}

void VRInternalSensorModule::matrixMultiplication(float *A, float *B, float *result)
{
	result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
	result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
	result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

	result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
	result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
	result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

	result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
	result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
	result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

	return;
}

bool VRInternalSensorModule::getRotationMatrix(float *R, float *I,float *gravity, float *geomagnetic)
{
	// TODO: move this to native code for efficiency
	float Ax = gravity[0];
	float Ay = gravity[1];
	float Az = gravity[2];
	float Ex = geomagnetic[0];
	float Ey = geomagnetic[1];
	float Ez = geomagnetic[2];
	float Hx = Ey*Az - Ez*Ay;
	float Hy = Ez*Ax - Ex*Az;
	float Hz = Ex*Ay - Ey*Ax;
	float normH = (float)sqrt(Hx*Hx + Hy*Hy + Hz*Hz);
	if (normH < 0.1f) {
		// device is close to free fall (or in space?), or close to
		// magnetic north pole. Typical values are  > 100.
		return false;
	}
	float invH = 1.0f / normH;
	Hx *= invH;
	Hy *= invH;
	Hz *= invH;
	float invA = 1.0f / (float)sqrt(Ax*Ax + Ay*Ay + Az*Az);
	Ax *= invA;
	Ay *= invA;
	Az *= invA;
	float Mx = Ay*Hz - Az*Hy;
	float My = Az*Hx - Ax*Hz;
	float Mz = Ax*Hy - Ay*Hx;
	if (R != NULL) {
		R[0] = Hx;     R[1] = Hy;     R[2] = Hz;
		R[3] = Mx;     R[4] = My;     R[5] = Mz;
		R[6] = Ax;     R[7] = Ay;     R[8] = Az;
	}
	if (I != NULL) {
		// compute the inclination matrix by projecting the geomagnetic
		// vector onto the Z (gravity) and X (horizontal component
		// of geomagnetic vector) axes.
		float invE = 1.0f / (float)sqrt(Ex*Ex + Ey*Ey + Ez*Ez);
		float c = (Ex*Mx + Ey*My + Ez*Mz) * invE;
		float s = (Ex*Ax + Ey*Ay + Ez*Az) * invE;
		I[0] = 1;     I[1] = 0;     I[2] = 0;
		I[3] = 0;     I[4] = c;     I[5] = s;
		I[6] = 0;     I[7] =-s;     I[8] = c;	
	}
	return true;
}

void VRInternalSensorModule::callbackInternalFusedData(float *data)
{
	//if(mInitialized)
	{
		float a[3];
		a[0] = data[0];
		a[1] = data[1];
		a[2] = data[2];
		
		float ret[3];
		ret[0] = (float) (1.566f +a[2]); //+90 deg
		ret[1] = (float) -(a[0]-mFirstY);
		ret[2] = (float) a[1];

		mFinalOrientation[0] = -ret[0];
		mFinalOrientation[1] = ret[1];
		mFinalOrientation[2] = ret[2];
			
		float dt = ret[1] - mPreviousYaw;							
		mCurrentYaw = mPreviousYaw  + dt * 0.7f;
		mPreviousYaw = mCurrentYaw;	

	}
}

void VRInternalSensorModule::getOrientation(float*R, float *orientation)
{
	orientation[0] = (float)atan2(R[1], R[4]); //Yaw
	orientation[1] = (float)asin(-R[7]);
	orientation[2] = (float)atan2(-R[6], R[8]); 
}

void VRInternalSensorModule::calculateAccMagOrientation() {
	if(getRotationMatrix(mRotationMatrix, NULL, mGravity, mGeomagnetic)) {
		getOrientation(mRotationMatrix, mAccMagOrientation);
		mAccMagOrientationReady = true;
	}
}

void VRInternalSensorModule::getRotationMatrixFromVector(float *R, float *rotationVector)
{

	float q0;
	float q1 = rotationVector[0];
	float q2 = rotationVector[1];
	float q3 = rotationVector[2];

	q0 = rotationVector[3];

	float sq_q1 = 2 * q1 * q1;
	float sq_q2 = 2 * q2 * q2;
	float sq_q3 = 2 * q3 * q3;
	float q1_q2 = 2 * q1 * q2;
	float q3_q0 = 2 * q3 * q0;
	float q1_q3 = 2 * q1 * q3;
	float q2_q0 = 2 * q2 * q0;
	float q2_q3 = 2 * q2 * q3;
	float q1_q0 = 2 * q1 * q0;

	R[0] = 1 - sq_q2 - sq_q3;
	R[1] = q1_q2 - q3_q0;
	R[2] = q1_q3 + q2_q0;

	R[3] = q1_q2 + q3_q0;
	R[4] = 1 - sq_q1 - sq_q3;
	R[5] = q2_q3 - q1_q0;

	R[6] = q1_q3 - q2_q0;
	R[7] = q2_q3 + q1_q0;
	R[8] = 1 - sq_q1 - sq_q2;	
}

void VRInternalSensorModule::gyroFunction(ASensorEvent event)
{
	// don't start until first accelerometer/magnetometer orientation has been acquired
	//if (mAccMagOrientation == null) {
	if (!mAccMagOrientationReady) {
		return;
	}

	// initialisation of the gyroscope based rotation matrix
	float result[9];
	if(mInitState) {
		float initMatrix[9];
		getRotationMatrixFromOrientation(mAccMagOrientation,initMatrix);
		float test[3];
		getOrientation(initMatrix, test);
		matrixMultiplication(mGyroMatrix, initMatrix,result);
		mGyroMatrix[0] = result[0];
		mGyroMatrix[1] = result[1];
		mGyroMatrix[2] = result[2];
		mGyroMatrix[3] = result[3];
		mGyroMatrix[4] = result[4];
		mGyroMatrix[5] = result[5];
		mGyroMatrix[6] = result[6];
		mGyroMatrix[7] = result[7];
		mGyroMatrix[8] = result[8];
		
		mFirstY = mAccMagOrientation[0];
		mInitState = false;
	}

	// copy the new gyro values into the gyro array
	// convert the raw gyro data intao a rotation vector
	float deltaVector[4];
	if(mTimeStamp != 0) {
		float dT = (event.timestamp - mTimeStamp) * NS2S;
		//System.arraycopy(event.values, 0, gyro, 0, 3);
		mGyro[0] = event.vector.x;
		mGyro[1] = event.vector.y;
		mGyro[2] = event.vector.z;
		
		getRotationVectorFromGyro(mGyro, deltaVector, dT / 2.0f);
	}

	// measurement done, save current time for next interval
	mTimeStamp = event.timestamp;

	// convert rotation vector into rotation matrix
	float deltaMatrix[9];
	for(int i = 0; i < 9; i++) 
	{
		deltaMatrix[i] = 0.0f;
	}
	getRotationMatrixFromVector(deltaMatrix, deltaVector);

	// apply the new rotation interval on the gyroscope based rotation matrix
	matrixMultiplication(mGyroMatrix, deltaMatrix,result);
	mGyroMatrix[0] = result[0];
	mGyroMatrix[1] = result[1];
	mGyroMatrix[2] = result[2];
	mGyroMatrix[3] = result[3];
	mGyroMatrix[4] = result[4];
	mGyroMatrix[5] = result[5];
	mGyroMatrix[6] = result[6];
	mGyroMatrix[7] = result[7];
	mGyroMatrix[8] = result[8];
	// get the gyroscope based orientation from the rotation matrix
	getOrientation(result, mGyroOrientation);
}

Quaternion VRInternalSensorModule::GetSensorQuaternion()
{
	Quaternion q;

	static bool firstTime = true;

	if(firstTime) {

		mLooper = ALooper_forThread();

		if(mLooper == NULL)
	       	mLooper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
		
		mSensorManager = ASensorManager_getInstance();
		const ASensor* accelerometerSensor = ASensorManager_getDefaultSensor(mSensorManager,ASENSOR_TYPE_ACCELEROMETER);
		const ASensor* magneticSensor = ASensorManager_getDefaultSensor(mSensorManager,ASENSOR_TYPE_MAGNETIC_FIELD);
		const ASensor* gyroSensor = ASensorManager_getDefaultSensor(mSensorManager,ASENSOR_TYPE_GYROSCOPE);
		
		mSensorEventQueue = ASensorManager_createEventQueue(mSensorManager, mLooper, LOOPER_ID, NULL, NULL);
	       ASensorEventQueue_enableSensor(mSensorEventQueue, accelerometerSensor);
		ASensorEventQueue_enableSensor(mSensorEventQueue, magneticSensor);
		ASensorEventQueue_enableSensor(mSensorEventQueue, gyroSensor);
		
	       ASensorEventQueue_setEventRate(mSensorEventQueue, accelerometerSensor, 10000);//(1000L/SAMP_PER_SEC)*1000);
		ASensorEventQueue_setEventRate(mSensorEventQueue, magneticSensor, 10000);
		ASensorEventQueue_setEventRate(mSensorEventQueue, gyroSensor, 10000);
		
		mGyroOrientation[0] = 0.0f;
		mGyroOrientation[1] = 0.0f;
		mGyroOrientation[2] = 0.0f;

		// initialise mGyroMatrix with identity matrix
		mGyroMatrix[0] = 1.0f; mGyroMatrix[1] = 0.0f; mGyroMatrix[2] = 0.0f;
		mGyroMatrix[3] = 0.0f; mGyroMatrix[4] = 1.0f; mGyroMatrix[5] = 0.0f;
		mGyroMatrix[6] = 0.0f; mGyroMatrix[7] = 0.0f; mGyroMatrix[8] = 1.0f;

		firstTime = false;
	}
	//while(1)
	{
		int ident = 0;//identifier 
		int events;
		//ALOGE("SISO_SF_SENSOR: threadLoop Start");
		// If a sensor has data, process it now.
		//while ((ident=ALooper_pollAll(-1, NULL, &events, NULL) >= 0)) {
			//if (ident == LOOPER_ID) {
				
				ASensorEvent event;
				while (ASensorEventQueue_getEvents(mSensorEventQueue, &event, 1) > 0) {
					{

												
						if(event.type == ASENSOR_TYPE_ACCELEROMETER) 
						{
							mGravity[0] = event.acceleration.x;
							mGravity[1] = event.acceleration.y;
							mGravity[2] = event.acceleration.z;
							calculateAccMagOrientation();
						}
						else if(event.type == ASENSOR_TYPE_MAGNETIC_FIELD) 
						{
							mGeomagnetic[0] = event.magnetic.x;
							mGeomagnetic[1] = event.magnetic.y;
							mGeomagnetic[2] = event.magnetic.z;
						}
						else if(event.type == ASENSOR_TYPE_GYROSCOPE)
						{
							gyroFunction(event);
						}
												
					}
				}
			//}
		//}
	}
	quaternionFromOrientation(mFinalOrientation, q);
	
	return q;
}


void VRInternalSensorModule::quaternionFromOrientation(float* orientation, Quaternion& resQuat) {
	vec3 axisX(1.0f, 0.0f, 0.0f);
	vec3 axisY(0.0f, 1.0f, 0.0f);
	vec3 axisZ(0.0f, 0.0f, 1.0f);
	
	Quaternion qAroundX, qAroundY, qAroundZ;
	qAroundX = qAroundX.CreateFromAxisAngle(axisX, orientation[0]);
	qAroundY = qAroundY.CreateFromAxisAngle(axisY,  orientation[1]);
	qAroundZ =  qAroundZ.CreateFromAxisAngle(axisZ,  orientation[2]);
	
	resQuat = qAroundX * qAroundY * qAroundZ;//qAroundZ.Multiplied(qAroundY).Multiplied(qAroundX);
	resQuat.Normalize();
}

void VRInternalSensorModule::destroy(){
	if(mSensorEventQueue && mSensorManager) {
		ASensorManager_destroyEventQueue(mSensorManager, mSensorEventQueue);
	}

	mSensorManager = 0;
	mSensorEventQueue = 0;
}