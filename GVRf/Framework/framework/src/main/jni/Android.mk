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
LOCAL_MODULE    := assimp
LOCAL_SRC_FILES := ../libs/libassimp.so
#LOCAL_SRC_FILES := ../libs/libassimp.a
#include $(PREBUILT_STATIC_LIBRARY)
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := jnlua
LOCAL_SRC_FILES := ../libs/libjnlua.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

ifndef OVR_MOBILE_SDK
#	OVR_MOBILE_SDK=../../ovr_sdk_mobile
	OVR_MOBILE_SDK=../../../../../ovr_sdk_mobile
endif

#include $(OVR_MOBILE_SDK)/VRLib/import_vrlib.mk
#include $(OVR_MOBILE_SDK)/VRLib/cflags.mk
include $(OVR_MOBILE_SDK)/cflags.mk
#$(call import-module,$(OVR_MOBILE_SDK)/LibOVR/Projects/Android/jni)
#$(call import-module,$(OVR_MOBILE_SDK)/VrApi/Projects/AndroidPrebuilt/jni)
#$(call import-module,$(OVR_MOBILE_SDK)/VrAppFramework/Projects/Android/jni)


LOCAL_MODULE := gvrf

FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES += $(OVR_MOBILE_SDK)/VrAppFramework/Src
LOCAL_C_INCLUDES += $(OVR_MOBILE_SDK)/LibOVRKernel/Include
LOCAL_C_INCLUDES += $(OVR_MOBILE_SDK)/LibOVRKernel/Src
LOCAL_C_INCLUDES += $(OVR_MOBILE_SDK)/VrApi/Include
LOCAL_C_INCLUDES += $(OVR_MOBILE_SDK)/VrAppSupport/Src
LOCAL_C_INCLUDES += $(OVR_MOBILE_SDK)/VrAppSupport/SystemUtils/Include

LOCAL_C_INCLUDES += $(LOCAL_PATH)/contrib/assimp
LOCAL_C_INCLUDES +=	$(LOCAL_PATH)/contrib/assimp/include
LOCAL_C_INCLUDES +=	$(LOCAL_PATH)/contrib/assimp/include/Compiler

LOCAL_C_INCLUDES += $(LOCAL_PATH)/contrib/jassimp2
# Uncomment for logs
# LOCAL_CFLAGS += -DANDROID -DJNI_LOG
FILE_LIST := $(wildcard $(LOCAL_PATH)/contrib/jassimp2/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/contrib
FILE_LIST := $(wildcard $(LOCAL_PATH)/contrib/glm/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/contrib/glm/detail/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/contrib/glm/gtc/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/contrib/glm/gtx/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/contrib/glm/virtrev/*.cpp)	
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)

FILE_LIST := $(wildcard $(LOCAL_PATH)/eglextension/msaa/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/eglextension/tiledrendering/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/engine/importer/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/engine/exporter/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/engine/picker/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/engine/renderer/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/engine/memory/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/gl/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/objects/*.cpp)	
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/objects/components/*.cpp)	
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/objects/textures/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/oculus/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/monoscopic/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/sensor/ksensor/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/shaders/*.cpp)	
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/shaders/material/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/shaders/posteffect/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)
FILE_LIST := $(wildcard $(LOCAL_PATH)/util/*.cpp)
LOCAL_SRC_FILES += $(FILE_LIST:$(LOCAL_PATH)/%=%)

#LOCAL_STATIC_LIBRARIES += staticAssimp
LOCAL_SHARED_LIBRARIES += assimp
LOCAL_SHARED_LIBRARIES += vrapi
LOCAL_STATIC_LIBRARIES += systemutils
LOCAL_STATIC_LIBRARIES += vrmodel
LOCAL_STATIC_LIBRARIES += vrappframework
LOCAL_STATIC_LIBRARIES += libovrkernel

LOCAL_ARM_NEON := true

## CPP flags are already defined in cflags.mk.
#LOCAL_CPPFLAGS += -fexceptions -frtti -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__ -mhard-float -D_NDK_MATH_NO_SOFTFP=1
#for NO_RTTI and softFP
LOCAL_CPPFLAGS += -fexceptions -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__
LOCAL_CFLAGS := -Wattributes

# include ld libraries defined in oculus's cflags.mk
#LOCAL_LDLIBS += -ljnigraphics -lm_hard
#softFP
LOCAL_LDLIBS += -ljnigraphics -llog -lGLESv3 -lEGL -lz -landroid
#LOCAL_LDLIBS += -L../libs/armeabi-v7a/ 

include $(BUILD_SHARED_LIBRARY)

$(call import-add-path, $(OVR_MOBILE_SDK))

$(call import-module,LibOVRKernel/Projects/AndroidPrebuilt/jni)
$(call import-module,VrApi/Projects/AndroidPrebuilt/jni)
$(call import-module,VrAppFramework/Projects/AndroidPrebuilt/jni)
$(call import-module,VrAppSupport/SystemUtils/Projects/AndroidPrebuilt/jni)
$(call import-module,VrAppSupport/VrModel/Projects/AndroidPrebuilt/jni)
