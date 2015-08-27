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

#include "VRSensorThread.h"
#include "VRInternalSensorModule.h"

using namespace GVR;
//using namespace android;

VRSensorThread* VRSensorThread::mInstance = NULL;

VRSensorThread::VRSensorThread() {
	mRunning = false;

	mKSensor = new KTrackerSensorModule();
	mISensor = new VRInternalSensorModule();
}

VRSensorThread* VRSensorThread::getInstance(){
	if(mInstance == 0){
	    mInstance = new VRSensorThread();
	}
	return mInstance;
}


void VRSensorThread::terminate() {
	if(mInstance) {
		delete mInstance;
		mInstance = 0;
	}
}

VRSensorThread::~VRSensorThread() {
	if(mKSensor) {
		delete mKSensor;
		mKSensor = 0;
	}

	if(mISensor) {
		mISensor->destroy();
		delete mISensor;
		mISensor = 0;		
	}

	mRunning = false;
}

void VRSensorThread::start(){
	
    if(!mRunning){
		mRunning = true;
		pthread_create(&mThreadId, 0, threadStartCallback, mInstance);
	}
	return;
}

void VRSensorThread::stop(){
	
    if(mRunning){
		mRunning = false;
		pthread_join(mThreadId, 0);
	}
	return;
}

void VRSensorThread::setSensorType(VRSensorType type) {
	mType = type;
}


Quaternion VRSensorThread::getQuaternion(){
	return mQuaternion;
}

void VRSensorThread::sensorLoop(){

	while(mRunning){
		
		Quaternion Q;
		
		if (mType == VR_SENSOR_K) {
			Q = mKSensor->GetSensorQuaternion();
		}else {
			Q = mISensor->GetSensorQuaternion();
		}
		mQuaternion  = Q;
		
	}
	return;
	
}

void* VRSensorThread::threadStartCallback(void *myself){
	
    //VRSensorThread *sensorThread = (VRSensorThread*)myself;	
    //sensorThread->sensorLoop();

	mInstance->sensorLoop();
	
	pthread_exit(0);
	return 0;
}

