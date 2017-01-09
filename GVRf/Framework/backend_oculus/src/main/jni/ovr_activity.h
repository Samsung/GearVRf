/* Copyright 2016 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#ifndef ACTIVITY_JNI_H
#define ACTIVITY_JNI_H

#include "ovr_framebufferobject.h"
#include "objects/components/camera.h"
#include "objects/components/camera_rig.h"
#include "util/ovr_configuration_helper.h"
#include "VrApi_Types.h"

namespace gvr {

class CameraRig;

class GVRActivity
{
public:
    GVRActivity(JNIEnv& jni, jobject activity, jobject vrAppSettings, jobject callbacks);
    ~GVRActivity();

    bool updateSensoredScene();
    void setCameraRig(jlong cameraRig);
    void setViewManager(jobject viewManager);

    CameraRig* cameraRig_ = nullptr;   // this needs a global ref on the java object; todo
    bool sensoredSceneUpdated_ = false;

private:
    JNIEnv* envMainThread_ = nullptr;           // for use by the Java UI thread

    jclass activityClass_ = nullptr;            // must be looked up from main thread or FindClass() will fail
    jclass viewManagerClass_ = nullptr;

    jmethodID onDrawEyeMethodId = nullptr;
    jmethodID updateSensoredSceneMethodId = nullptr;

    jobject activity_;

    ConfigurationHelper configurationHelper_;

    ovrJava oculusJavaMainThread_;
    ovrJava oculusJavaGlThread_;
    ovrMobile* oculusMobile_ = nullptr;
    long long frameIndex = 1;
    FrameBufferObject frameBuffer_[VRAPI_FRAME_LAYER_EYE_MAX];
    ovrMatrix4f projectionMatrix_;
    ovrMatrix4f texCoordsTanAnglesMatrix_;
    ovrPerformanceParms oculusPerformanceParms_;
    ovrHeadModelParms oculusHeadModelParms_;

    bool mResolveDepthConfiguration = false;
    int mWidthConfiguration = 0, mHeightConfiguration = 0, mMultisamplesConfiguration = 0;
    ovrTextureFormat mColorTextureFormatConfiguration = VRAPI_TEXTURE_FORMAT_NONE;
    ovrTextureFormat mDepthTextureFormatConfiguration = VRAPI_TEXTURE_FORMAT_NONE;

    int32_t mVrapiInitResult = VRAPI_INITIALIZE_UNKNOWN_ERROR;

    int x, y, width, height;                // viewport

    void initializeOculusJava(JNIEnv& env, ovrJava& oculusJava);
    void beginRenderingEye(const int eye);
    void endRenderingEye(const int eye);

    jobject viewManager_ = nullptr;
    bool docked_ = false;

public:
    void onSurfaceCreated(JNIEnv& env);
    void onSurfaceChanged(JNIEnv& env);
    void onDrawFrame();
    int initializeVrApi();
    void uninitializeVrApi();
    void leaveVrMode();

    void showConfirmQuit();

    bool isHmtConnected() const;
    bool usingMultiview() const;

    void onDock() { docked_ = true; }
    void onUndock() { docked_ = false; }
};

}
#endif
