 #   
 # Copyright 2016 Samsung Electronics Co., LTD
 #
 # Licensed under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #     http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 #
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := ovrplatformloader
LOCAL_SRC_FILES := $(OVR_PLATFORM_SDK)/Android/libs/armeabi-v7a/libovrplatformloader.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := gvrf-platformsdk

LOCAL_C_INCLUDES += $(OVR_PLATFORM_SDK)/Include
LOCAL_C_INCLUDES += ../../../../Framework/framework/src/main/jni

FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
$(info $(FILE_LIST))
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_CPPFLAGS += -fexceptions -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__
LOCAL_CFLAGS := -Wattributes

LOCAL_SHARED_LIBRARIES += ovrplatformloader
LOCAL_LDLIBS += -llog

include $(BUILD_SHARED_LIBRARY)