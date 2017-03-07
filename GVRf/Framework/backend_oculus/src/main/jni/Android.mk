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
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

ifndef OVR_MOBILE_SDK
	OVR_MOBILE_SDK=../../../../../ovr_sdk_mobile
endif

$(info OVR_MOBILE_SDK is set to $(OVR_MOBILE_SDK))
include $(OVR_MOBILE_SDK)/cflags.mk

LOCAL_MODULE := gvrf-oculus

LOCAL_C_INCLUDES += $(OVR_MOBILE_SDK)/VrApi/Include
LOCAL_C_INCLUDES += $(OVR_MOBILE_SDK)/VrAppSupport/SystemUtils/Include

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../../framework/src/main/jni/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../../framework/src/main/jni/util
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../../framework/src/main/jni/contrib

# Uncomment for logs
# LOCAL_CFLAGS += -DANDROID -DJNI_LOG

LOCAL_C_INCLUDES += $(LOCAL_PATH)/src/main/jni
LOCAL_C_INCLUDES += $(LOCAL_PATH)/src/main/jni/util
LOCAL_C_INCLUDES += $(LOCAL_PATH)/src/main/jni/objects

FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/objects/components/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/util/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/monoscopic/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_SHARED_LIBRARIES += vrapi

## CPP flags are already defined in cflags.mk.
#LOCAL_CPPFLAGS += -fexceptions -frtti -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__ -mhard-float -D_NDK_MATH_NO_SOFTFP=1
#for NO_RTTI and softFP
LOCAL_CPPFLAGS += -fexceptions -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__
LOCAL_CFLAGS := -Wattributes

# include ld libraries defined in oculus's cflags.mk
#LOCAL_LDLIBS += -ljnigraphics -lm_hard
#softFP
LOCAL_LDLIBS += -ljnigraphics -llog -lGLESv3 -lEGL -lz -landroid
LOCAL_LDLIBS += $(PROJECT_ROOT)/backend_oculus/build/intermediates/exploded-aar/Framework/framework/unspecified/jni/armeabi-v7a/libgvrf.so

include $(BUILD_SHARED_LIBRARY)

$(call import-add-path, $(OVR_MOBILE_SDK))
$(call import-module,VrApi/Projects/AndroidPrebuilt/jni)
