#ifndef _INTERNALSENSORMODULE_H
#define _INTERNALSENSORMODULE_H

//#include <gui/Sensor.h>
//#include <gui/SensorManager.h>
//#include <gui/SensorEventQueue.h>
#include <android/sensor.h>
#include "kSensor/math/quaternion.hpp"

namespace GVR {

	class VRInternalSensorModule {

		public:
			VRInternalSensorModule() ;
			Quaternion GetSensorQuaternion();
			void gyroFunction(ASensorEvent event);
			void getRotationMatrixFromVector(float *R, float *rotationVector);
			void calculateAccMagOrientation();
			void getOrientation(float*R, float *orientation);
			void callbackInternalFusedData(float *data);
			bool getRotationMatrix(float *R, float *I,float *gravity, float *geomagnetic);
			void matrixMultiplication(float *A, float *B, float *result);
			void getRotationMatrixFromOrientation(float *o, float* result);
			void calculateTask();
			void getRotationVectorFromGyro(float *gyroValues,float *deltaRotationVector,float timeFactor);
			float GetCurrentYaw();
			void quaternionFromOrientation(float* orientation, Quaternion& resQuat);
			void destroy();
			

		private:
			ALooper* 			mLooper;
			ASensorManager* 		mSensorManager;	
			ASensorEventQueue*  	mSensorEventQueue;
			
			float				mAcclX;
			float				mAcclY;
			float				mAcclZ;
			float 			NS2S;
			float 			mTimeStamp;

			float 			mGravity[3];
			float 			mGeomagnetic[3];
			float				mGyro[3];
			float 			mGyroOrientation[3];
			float				mFusedOrientation[3];
			float				mAccMagOrientation[3];
			float 			mFinalOrientation[3];
			float				mGyroMatrix[9];
			float				mRotationMatrix[9];
			float				mPreviousYaw;		
			float				mCurrentYaw;	
			float 			FILTER_COEFFICIENT;	
			float 			mFirstY;
			bool				mAccMagOrientationReady;
			bool				mInitState;
		
	};

}

#endif //_INTERNALSENSORMODULE_H
