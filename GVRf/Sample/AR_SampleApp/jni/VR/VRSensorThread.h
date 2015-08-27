/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 #ifndef VRSensorThread_h
 #define VRSensorThread_h

#include "kSensor/KTrackerSensorModule.h"
#include "VRInternalSensorModule.h"
 
//#include <utils/RefBase.h>
//#include <utils/Log.h>
//#include <utils/Timers.h>
#include <pthread.h>

namespace GVR {

	enum VRSensorType {
		VR_SENSOR_K,
		VR_SENSOR_INTERNAL,
		VR_SENSOR_MAX
	};


	class VRSensorThread {

	public:	
		GVR::Quaternion getQuaternion();

		void start();
		void stop();
		void setSensorType(VRSensorType type);

		static VRSensorThread* getInstance();
		void terminate();
	   	
	private:
		VRSensorThread();
		~VRSensorThread();
		static void* threadStartCallback(void *myself);
		void sensorLoop();
		
		pthread_t mThreadId;
		
		GVR::KTrackerSensorModule* mKSensor;
		GVR::VRInternalSensorModule*   mISensor;
		
		bool mRunning;
		GVR::Quaternion mQuaternion;
	       static VRSensorThread* mInstance;
		VRSensorType mType;

	};
};
#endif
