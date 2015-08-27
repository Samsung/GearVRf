# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := prebuilt_vrengine
LOCAL_SRC_FILES := libVrRenderEngine.so

include $(PREBUILT_SHARED_LIBRARY)



include $(CLEAR_VARS)
LOCAL_MODULE := vuforia
LOCAL_SRC_FILES := libVuforia.so

include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libgl2jni
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := VRInterface.cpp
LOCAL_LDLIBS    := -llog -lGLESv2
LOCAL_LDLIBS += -L $(LOCAL_PATH)
LOCAL_SHARED_LIBRARIES := libstlport  prebuilt_vrengine

LOCAL_C_INCLUDES := VR jni/VR

include $(BUILD_SHARED_LIBRARY)
