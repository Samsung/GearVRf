LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    VRSceneManager.cpp \
	VRCamera.cpp \
	VRQuad.cpp\
	VRCurvedMesh.cpp\
	VRDistortionGrid.cpp\
	VRDistorter.cpp \
	VRRenderEngine.cpp \
	VRShaderUtil.cpp \
	VRSensorThread.cpp \
	VRInternalSensorModule.cpp\
	kSensor/KTrackerSensorModule.cpp

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/../Include \
			external/skia/include \
            external/skia/include/core \
			external/skia/include/lazy \
            external/skia/include/utils \
			external/skia/include/effects \
			external/skia/include/images \
			external/skia/src/ports \
			ndk/sources/cxx-stl/stlport/stlport \
			frameworks/native/include
			
LOCAL_SHARED_LIBRARIES := \
	libutils \
	libGLESv2 \
	libgui \
	libskia \
	liblog \
	libandroid
#	libui \
#	libdl \
#	libGLESv1_CM \
#	libhardware \
#	libEGL \
#	libbinder \
                                          
#Split Surface support
ifeq (true,$(call spf_check,SEC_PRODUCT_FEATURE_SUPPORT_WEARABLE_HMT,TRUE))
#LOCAL_CFLAGS += -DVR_STEREO_RENDERING_ENABLE
#LOCAL_CFLAGS += -DUSE_VR_RENDER_ENGINE_LIB
LOCAL_CFLAGS += -DSEC_SUPPORT_VRSURFACE
LOCAL_CFLAGS += -DVR_DEBUG_ENABLE
#LOCAL_CFLAGS += -DUSE_COMPRESSED_TEXTURE
endif

LOCAL_MODULE:= libVrRenderEngine


include $(BUILD_SHARED_LIBRARY)
