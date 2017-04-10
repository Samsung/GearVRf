/******************************************************************************\
* Copyright (C) 2012-2016 Leap Motion, Inc. All rights reserved.               *
* Leap Motion proprietary and confidential. Not for distribution.              *
* Use subject to the terms of the Leap Motion SDK Agreement available at       *
* https://developer.leapmotion.com/sdk_agreement, or another agreement         *
* between Leap Motion and you, your company or other organization.             *
\******************************************************************************/

#undef __cplusplus

#include <stdio.h>
#include <stdlib.h>
#include "LeapC.h"
#include "ExampleConnection.h"

#include <android/log.h>

#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define  LOG_TAG    "LeapController"

/** Callback for when the connection opens. */
void OnConnect(){
  LOGI("Connected.\n");
}

/** Callback for when a device is found. */
void OnDevice(const LEAP_DEVICE_INFO *props){
  LOGI("Found device %s.\n", props->serial);
}

/** Callback for when a frame of tracking data is available. */
void OnFrame(const LEAP_TRACKING_EVENT *frame){
  printf("Frame %lli with %i hands.\n", (long long int)frame->info.frame_id, frame->nHands);
  uint32_t h = 0;
  for(h = 0; h < frame->nHands; h++){
    LEAP_HAND* hand = &frame->pHands[h];
    //printf("    Hand id %i is a %s handType with position (%f, %f, %f).\n",
    //            handType->id,
    //            (handType->type == eLeapHandType_Left ? "left" : "right"),
    //            handType->palm.position.x,
    //            handType->palm.position.y,
    //            handType->palm.position.z);
  }
}


int main(int argc, char** argv) {
  //Set callback function pointers
  ConnectionCallbacks.on_connection          = &OnConnect;
  ConnectionCallbacks.on_device_found        = &OnDevice;
  ConnectionCallbacks.on_frame               = &OnFrame;

  OpenConnection();

  LOGI("Press Enter to exit program.\n");

  getchar();
  return 0;
}

void start(){
  //Set callback function pointers
  ConnectionCallbacks.on_connection          = &OnConnect;
  ConnectionCallbacks.on_device_found        = &OnDevice;
  ConnectionCallbacks.on_frame               = &OnFrame;

  OpenConnection();

  LOGI("Press Enter to exit program.\n");

  getchar();
  //return 0;
}
//End-of-Sample.c
