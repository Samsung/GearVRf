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

ifneq (,$(strip $(wildcard $(LOCAL_PATH)/../../../../framework/build/intermediates/ndkBuild/$(APP_OPTIM)/obj/local/$(TARGET_ARCH_ABI)/libgvrf.so)))
    LIBGVRF_EXISTS := 1
endif

###
ifeq ($(LIBGVRF_EXISTS),1)
    include $(CLEAR_VARS)
    LOCAL_MODULE    := gvrf
    LOCAL_SRC_FILES := ../../../../framework/build/intermediates/ndkBuild/$(APP_OPTIM)/obj/local/$(TARGET_ARCH_ABI)/libgvrf.so
    include $(PREBUILT_SHARED_LIBRARY)
endif

include $(CLEAR_VARS)

LOCAL_MODULE := gvrf-monoscopic

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

## CPP flags are already defined in cflags.mk.
#LOCAL_CPPFLAGS += -fexceptions -frtti -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__ -mhard-float -D_NDK_MATH_NO_SOFTFP=1
#for NO_RTTI and softFP
LOCAL_CPPFLAGS += -fexceptions -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__
LOCAL_CFLAGS := -Wattributes

LOCAL_LDLIBS += -ljnigraphics -llog -lGLESv3 -lEGL -lz -landroid
ifeq ($(LIBGVRF_EXISTS),1)
    LOCAL_SHARED_LIBRARIES += gvrf
endif

include $(BUILD_SHARED_LIBRARY)