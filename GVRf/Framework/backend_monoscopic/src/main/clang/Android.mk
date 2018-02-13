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

ROOT_PATH := $(LOCAL_PATH)/../jni
LOCAL_C_INCLUDES += $(ROOT_PATH)/../../../../framework/src/main/jni/
LOCAL_C_INCLUDES += $(ROOT_PATH)/../../../../framework/src/main/jni/util
LOCAL_C_INCLUDES += $(ROOT_PATH)/../../../../framework/src/main/jni/contrib

# Uncomment for logs
# LOCAL_CFLAGS += -DANDROID -DJNI_LOG

LOCAL_C_INCLUDES += $(ROOT_PATH)/src/main/jni
LOCAL_C_INCLUDES += $(ROOT_PATH)/src/main/jni/util
LOCAL_C_INCLUDES += $(ROOT_PATH)/src/main/jni/objects

FILE_LIST := $(wildcard $(ROOT_PATH)/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST)
FILE_LIST := $(wildcard $(ROOT_PATH)/objects/components/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST)
FILE_LIST := $(wildcard $(ROOT_PATH)/util/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST)
FILE_LIST := $(wildcard $(ROOT_PATH)/monoscopic/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST)

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
#LOCAL_LDLIBS += -ldl
LOCAL_LDLIBS += $(PROJECT_DIR)/../framework/build/intermediates/ndkBuild/$(APP_OPTIM)/obj/local/$(TARGET_ARCH_ABI)/libgvrf.so

include $(BUILD_SHARED_LIBRARY)

$(call import-add-path, $(OVR_MOBILE_SDK))
$(call import-module,VrApi/Projects/AndroidPrebuilt/jni)
