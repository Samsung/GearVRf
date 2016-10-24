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
LOCAL_MODULE    := bullet3
LOCAL_SRC_FILES := prebuilt/$(TARGET_ARCH_ABI)/libBullet.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := gvrf-physics

LOCAL_C_INCLUDES +=	$(LOCAL_PATH)/bullet3/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../../../Framework/framework/src/main/jni/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../../../Framework/framework/src/main/jni/contrib

FILE_LIST := $(wildcard $(LOCAL_PATH)/engine/physics/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/engine/physics/*/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_SHARED_LIBRARIES += bullet3

LOCAL_ARM_NEON := true

## CPP flags are already defined in cflags.mk.
#LOCAL_CPPFLAGS += -fexceptions -frtti -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__ -mhard-float -D_NDK_MATH_NO_SOFTFP=1
#for NO_RTTI and softFP
LOCAL_CPPFLAGS += -fexceptions -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__
ifdef ARM64
LOCAL_CPPFLAGS += -DARM64
endif
LOCAL_CFLAGS := -Wattributes

# include ld libraries defined in oculus's cflags.mk
#LOCAL_LDLIBS += -ljnigraphics -lm_hard
#softFP
#LOCAL_LDLIBS += -ljnigraphics -llog -lGLESv3 -lEGL -lz -landroid
LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += $(LOCAL_PATH)/../../../build/intermediates/exploded-aar/framework-debug/jni/$(TARGET_ARCH_ABI)/libgvrf.so

include $(BUILD_SHARED_LIBRARY)
