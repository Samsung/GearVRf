/******************************************************************************\
* Copyright (C) 2012-2016 Leap Motion, Inc. All rights reserved.               *
* Leap Motion proprietary and confidential. Not for distribution.              *
* Use subject to the terms of the Leap Motion SDK Agreement available at       *
* https://developer.leapmotion.com/sdk_agreement, or another agreement         *
* between Leap Motion and you, your company or other organization.             *
\******************************************************************************/

#include "ExampleConnection.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#if defined(_MSC_VER)
  #include <Windows.h>
  #include <process.h>
  #define LockMutex EnterCriticalSection
  #define UnlockMutex LeaveCriticalSection
#else
  #include <unistd.h>
  #include <pthread.h>
  #define LockMutex pthread_mutex_lock
  #define UnlockMutex pthread_mutex_unlock
#endif


//Forward declarations
const char* ResultString(eLeapRS r);
#if defined(_MSC_VER)
void serviceMessageLoop(void * unused);
#else
void* serviceMessageLoop(void * unused);
#endif
void setFrame(const LEAP_TRACKING_EVENT *frame);
void setDevice(const LEAP_DEVICE_INFO *deviceProps);

//External state
bool IsConnected = false;

//Internal state
bool _isRunning = false;
LEAP_CONNECTION connectionHandle;
LEAP_TRACKING_EVENT *lastFrame = NULL;
LEAP_DEVICE_INFO *lastDevice = NULL;

//Callback function pointers
struct Callbacks ConnectionCallbacks;

//Threading variables
#if defined(_MSC_VER)
uintptr_t pollingThread;
CRITICAL_SECTION dataLock;
#else
pthread_t pollingThread;
pthread_mutex_t dataLock;
#endif

void CloseConnection(){
  CloseConnectionHandle(&connectionHandle);
}

/** Close the connection and let message thread function end. */
void CloseConnectionHandle(LEAP_CONNECTION* connectionHandle){
  LeapDestroyConnection(*connectionHandle);
  _isRunning = false;
}

/** Called by serviceMessageLoop() when a connection event is returned by LeapPollConnection(). */
void handleConnectionEvent(const LEAP_CONNECTION_EVENT *connection_event){
  IsConnected = true;
  if(ConnectionCallbacks.on_connection){
    ConnectionCallbacks.on_connection();
  }
}

/** Called by serviceMessageLoop() when a connection lost event is returned by LeapPollConnection(). */
void handleConnectionLostEvent(const LEAP_CONNECTION_LOST_EVENT *connection_lost_event){
  IsConnected = false;
  if(ConnectionCallbacks.on_connection_lost){
    ConnectionCallbacks.on_connection_lost();
  }
}

/**
 * Called by serviceMessageLoop() when a device event is returned by LeapPollConnection()
 * Demonstrates how to access device properties.
 */
void handleDeviceEvent(const LEAP_DEVICE_EVENT *device_event){
  LEAP_DEVICE deviceHandle;
  //Open device using LEAP_DEVICE_REF from event struct.
  eLeapRS result = LeapOpenDevice(device_event->device, &deviceHandle);
  if(result != eLeapRS_Success){
    printf("Could not open device %s.\n", ResultString(result));
    return;
  }

  //Create a struct to hold the device properties, we have to provide a buffer for the serial string
  LEAP_DEVICE_INFO deviceProperties = { sizeof(deviceProperties) };
  // Start with a length of 1 (pretending we don't know a priori what the length is).
  // Currently device serial numbers are all the same length, but that could change in the future
  deviceProperties.serial_length = 1;
  deviceProperties.serial = malloc(deviceProperties.serial_length);
  //This will fail since the serial buffer is only 1 character long
  // But deviceProperties is updated to contain the required buffer length
  result = LeapGetDeviceInfo(deviceHandle, &deviceProperties);
  if(result == eLeapRS_InsufficientBuffer){
    //try again with correct buffer size
    free(deviceProperties.serial);
    deviceProperties.serial = malloc(deviceProperties.serial_length);
    result = LeapGetDeviceInfo(deviceHandle, &deviceProperties);
    if(result != eLeapRS_Success){
      printf("Failed to get device info %s.\n", ResultString(result));
      free(deviceProperties.serial);
      return;
    }
  }
  setDevice(&deviceProperties);
  if(ConnectionCallbacks.on_device_found){
    ConnectionCallbacks.on_device_found(&deviceProperties);
  }

  free(deviceProperties.serial);
  LeapCloseDevice(deviceHandle);
}

/** Called by serviceMessageLoop() when a device lost event is returned by LeapPollConnection(). */
void handleDeviceLostEvent(const LEAP_DEVICE_EVENT *device_event){
  if(ConnectionCallbacks.on_device_lost){
    ConnectionCallbacks.on_device_lost();
  }
}

/** Called by serviceMessageLoop() when a device failure event is returned by LeapPollConnection(). */
void handleDeviceFailureEvent(const LEAP_DEVICE_FAILURE_EVENT *device_failure_event){
  if(ConnectionCallbacks.on_device_failure){
    ConnectionCallbacks.on_device_failure(device_failure_event->status, device_failure_event->hDevice);
  }
}

/** Called by serviceMessageLoop() when a tracking event is returned by LeapPollConnection(). */
void handleTrackingEvent(const LEAP_TRACKING_EVENT *tracking_event){
  setFrame(tracking_event); //support polling tracking data from different thread
  if(ConnectionCallbacks.on_frame){
    ConnectionCallbacks.on_frame(tracking_event);
  }
}

/** Called by serviceMessageLoop() when an image complete event is returned by LeapPollConnection(). */
void handleImageCompleteEvent(const LEAP_IMAGE_COMPLETE_EVENT *image_complete_event){
  if(ConnectionCallbacks.on_image_complete){
    ConnectionCallbacks.on_image_complete(image_complete_event);
  }
}

/** Called by serviceMessageLoop() when an image reuest error event is returned by LeapPollConnection(). */
void handleImageRequestErrorEvent(const LEAP_IMAGE_FRAME_REQUEST_ERROR_EVENT *image_request_error_event){
  if(ConnectionCallbacks.on_image_request_error){
    ConnectionCallbacks.on_image_request_error(image_request_error_event);
  }
}

/** Called by serviceMessageLoop() when a log event is returned by LeapPollConnection(). */
void handleLogEvent(const LEAP_LOG_EVENT *log_event){
  if(ConnectionCallbacks.on_log_message){
    ConnectionCallbacks.on_log_message(log_event->Severity, log_event->Timestamp, log_event->Message);
  }
}

/** Called by serviceMessageLoop() when a policy event is returned by LeapPollConnection(). */
void handlePolicyEvent(const LEAP_POLICY_EVENT *policy_event){
  if(ConnectionCallbacks.on_policy){
    ConnectionCallbacks.on_policy(policy_event->current_policy);
  }
}

/** Called by serviceMessageLoop() when a config change event is returned by LeapPollConnection(). */
void handleConfigChangeEvent(const LEAP_CONFIG_CHANGE_EVENT *config_change_event){
  if(ConnectionCallbacks.on_config_change){
    ConnectionCallbacks.on_config_change(config_change_event->requestID, config_change_event->status);
  }
}

/** Called by serviceMessageLoop() when a config response event is returned by LeapPollConnection(). */
void handleConfigResponseEvent(const LEAP_CONFIG_RESPONSE_EVENT *config_response_event){
  if(ConnectionCallbacks.on_config_response){
    ConnectionCallbacks.on_config_response(config_response_event->requestID, config_response_event->value);
  }
}

/**
 * Services the LeapC message pump by calling LeapPollConnection().
 * The average polling time is determined by the framerate of the Leap Motion service.
 */
#if defined(_MSC_VER)
void serviceMessageLoop(void * unused){
#else
void* serviceMessageLoop(void * unused){
#endif
  eLeapRS result;
  LEAP_CONNECTION_MESSAGE msg;
  while(_isRunning){
    unsigned int timeout = 1000;
    result = LeapPollConnection(connectionHandle, timeout, &msg);

    if (result != eLeapRS_Success) {
      printf("LeapC PollConnection call was %s.\n", ResultString(result));
      continue;
    }

    switch (msg.type){
      case eLeapEventType_Connection:
        handleConnectionEvent(msg.connection_event);
        break;
      case eLeapEventType_ConnectionLost:
        handleConnectionLostEvent(msg.connection_lost_event);
        break;
      case eLeapEventType_Device:
        handleDeviceEvent(msg.device_event);
        break;
      case eLeapEventType_DeviceLost:
        handleDeviceLostEvent(msg.device_event);
        break;
      case eLeapEventType_DeviceFailure:
        handleDeviceFailureEvent(msg.device_failure_event);
        break;
      case eLeapEventType_Tracking:
        handleTrackingEvent(msg.tracking_event);
        break;
      case eLeapEventType_ImageComplete:
        handleImageCompleteEvent(msg.image_complete_event);
        break;
      case eLeapEventType_ImageRequestError:
        handleImageRequestErrorEvent(msg.image_request_error_event);
        break;
      case eLeapEventType_LogEvent:
        handleLogEvent(msg.log_event);
        break;
      case eLeapEventType_Policy:
        handlePolicyEvent(msg.policy_event);
        break;
      case eLeapEventType_ConfigChange:
        handleConfigChangeEvent(msg.config_change_event);
        break;
      case eLeapEventType_ConfigResponse:
        handleConfigResponseEvent(msg.config_response_event);
        break;
      default:
        //discard unknown message types
        printf("Unhandled message type %i.\n", msg.type);
    } //switch on msg.type
  }
#if !defined(_MSC_VER)
  return NULL;
#endif
}


/**
 * Creates the connection handle and opens a connection to the Leap Motion
 * service. On success, creates a thread to service the LeapC message pump.
 */
LEAP_CONNECTION* OpenConnection(){
  eLeapRS result = LeapCreateConnection(NULL, &connectionHandle);
  if(result == eLeapRS_Success){
    result = LeapOpenConnection(connectionHandle);
    if(result == eLeapRS_Success){
      _isRunning = true;
#if defined(_MSC_VER)
      InitializeCriticalSection(&dataLock);
      pollingThread = _beginthread(serviceMessageLoop, 0, NULL);
#else
      pthread_create(&pollingThread, NULL, serviceMessageLoop, NULL);
#endif
    }
  }
  return &connectionHandle;
}

/* Used in Polling Example: */

/**
 * Caches the newest frame by copying the tracking event struct returned by
 * LeapC.
 */
void setFrame(const LEAP_TRACKING_EVENT *frame){
  LockMutex(&dataLock);
  if(!lastFrame) lastFrame = malloc(sizeof(*frame));
  *lastFrame = *frame;
  UnlockMutex(&dataLock);
}

/** Returns a pointer to the cached tracking frame. */
LEAP_TRACKING_EVENT* GetFrame(){
  LEAP_TRACKING_EVENT *currentFrame;

  LockMutex(&dataLock);
  currentFrame = lastFrame;
  UnlockMutex(&dataLock);

  return currentFrame;
}

/**
 * Caches the last device found by copying the device info struct returned by
 * LeapC.
 */
void setDevice(const LEAP_DEVICE_INFO *deviceProps){
  LockMutex(&dataLock);
  if(lastDevice){
    free(lastDevice->serial);
  } else {
    lastDevice = malloc(sizeof(*deviceProps));
  }
  *lastDevice = *deviceProps;
  lastDevice->serial = malloc(deviceProps->serial_length);
  memcpy(lastDevice->serial, deviceProps->serial, deviceProps->serial_length);
  UnlockMutex(&dataLock);
}

/** Returns a pointer to the cached device info. */
LEAP_DEVICE_INFO* GetDeviceProperties(){
  LEAP_DEVICE_INFO *currentDevice;
  LockMutex(&dataLock);
  currentDevice = lastDevice;
  UnlockMutex(&dataLock);
  return currentDevice;
}

//End of polling example-specific code

/** Translates eLeapRS result codes into a human-readable string. */
const char* ResultString(eLeapRS r) {
  switch(r){
    case eLeapRS_Success:                  return "eLeapRS_Success";
    case eLeapRS_UnknownError:             return "eLeapRS_UnknownError";
    case eLeapRS_InvalidArgument:          return "eLeapRS_InvalidArgument";
    case eLeapRS_InsufficientResources:    return "eLeapRS_InsufficientResources";
    case eLeapRS_InsufficientBuffer:       return "eLeapRS_InsufficientBuffer";
    case eLeapRS_Timeout:                  return "eLeapRS_Timeout";
    case eLeapRS_NotConnected:             return "eLeapRS_NotConnected";
    case eLeapRS_HandshakeIncomplete:      return "eLeapRS_HandshakeIncomplete";
    case eLeapRS_BufferSizeOverflow:       return "eLeapRS_BufferSizeOverflow";
    case eLeapRS_ProtocolError:            return "eLeapRS_ProtocolError";
    case eLeapRS_InvalidClientID:          return "eLeapRS_InvalidClientID";
    case eLeapRS_UnexpectedClosed:         return "eLeapRS_UnexpectedClosed";
    case eLeapRS_UnknownImageFrameRequest: return "eLeapRS_UnknownImageFrameRequest";
    case eLeapRS_UnknownTrackingFrameID:   return "eLeapRS_UnknownTrackingFrameID";
    case eLeapRS_RoutineIsNotSeer:         return "eLeapRS_RoutineIsNotSeer";
    case eLeapRS_TimestampTooEarly:        return "eLeapRS_TimestampTooEarly";
    case eLeapRS_ConcurrentPoll:           return "eLeapRS_ConcurrentPoll";
    case eLeapRS_NotAvailable:             return "eLeapRS_NotAvailable";
    case eLeapRS_NotStreaming:             return "eLeapRS_NotStreaming";
    case eLeapRS_CannotOpenDevice:         return "eLeapRS_CannotOpenDevice";
    default:                               return "unknown result type.";
  }
}
/** Cross-platform sleep function */
void millisleep(int milliseconds){
#ifdef _WIN32
    Sleep(milliseconds);
#else
    usleep(milliseconds*1000);
#endif
  }
//End-of-ExampleConnection.c
