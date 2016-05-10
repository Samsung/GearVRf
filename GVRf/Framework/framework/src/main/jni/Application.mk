 #   
 # Copyright 2015 Samsung Electronics Co., LTD
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


APP_ABI := armeabi-v7a
APP_PLATFORM := android-19
#APP_STL := stlport_static
APP_STL := gnustl_static
NDK_TOOLCHAIN_VERSION := 4.9
ifndef OVR_MOBILE_SDK
#	OVR_MOBILE_SDK=../../ovr_sdk_mobile
   	OVR_MOBILE_SDK=../../../../../ovr_sdk_mobile
endif

NDK_MODULE_PATH := $(OVR_MOBILE_SDK)
