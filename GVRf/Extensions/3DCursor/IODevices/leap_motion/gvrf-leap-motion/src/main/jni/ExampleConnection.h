/******************************************************************************\
* Copyright (C) 2012-2016 Leap Motion, Inc. All rights reserved.               *
* Leap Motion proprietary and confidential. Not for distribution.              *
* Use subject to the terms of the Leap Motion SDK Agreement available at       *
* https://developer.leapmotion.com/sdk_agreement, or another agreement         *
* between Leap Motion and you, your company or other organization.             *
\******************************************************************************/
#ifndef ExampleConnection_h
#define ExampleConnection_h
#ifdef __cplusplus
extern "C" {
#endif

#include "LeapC.h"

/* Client functions */
LEAP_CONNECTION* OpenConnection();
LEAP_CONNECTION* OpenConnection1();
void CloseConnection();
void CloseConnectionHandle(LEAP_CONNECTION* connectionHandle);
LEAP_TRACKING_EVENT* GetFrame(); //Used in polling example
LEAP_DEVICE_INFO* GetDeviceProperties(); //Used in polling example
const char* ResultString(eLeapRS r);

/* State */
extern bool IsConnected;

/* Callback function pointers */
typedef void (*connection_callback)     ();
typedef void (*device_callback)         (const LEAP_DEVICE_INFO *device);
typedef void (*device_lost_callback)    ();
typedef void (*device_failure_callback) (const eLeapDeviceStatus failure_code,
                                         const LEAP_DEVICE failed_device);
typedef void (*policy_callback)         (const uint32_t current_policies);
typedef void (*tracking_callback)       (const LEAP_TRACKING_EVENT *tracking_event);
typedef void (*image_callback)          (const LEAP_IMAGE_COMPLETE_EVENT *image_complete_event);
typedef void (*image_error_callback)    (const LEAP_IMAGE_FRAME_REQUEST_ERROR_EVENT *image_error_event);
typedef void (*log_callback)            (const eLeapLogSeverity severity,
                                         const int64_t timestamp,
                                         const char* message);
typedef void (*config_change_callback)  (const uint32_t requestID, const bool success);
typedef void (*config_response_callback)(const uint32_t requestID, LEAP_VARIANT value);

struct Callbacks{
  connection_callback      on_connection;
  connection_callback      on_connection_lost;
  device_callback          on_device_found;
  device_lost_callback     on_device_lost;
  device_failure_callback  on_device_failure;
  policy_callback          on_policy;
  tracking_callback        on_frame;
  image_callback           on_image_complete;
  image_error_callback     on_image_request_error;
  log_callback             on_log_message;
  config_change_callback   on_config_change;
  config_response_callback on_config_response;
};
extern struct Callbacks ConnectionCallbacks;
extern void millisleep(int milliseconds);
#ifdef __cplusplus
}
#endif
#endif /* ExampleConnection_h */
