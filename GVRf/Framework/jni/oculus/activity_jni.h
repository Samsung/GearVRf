/* Copyright 2015 Samsung Electronics Co., LTD
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

#include "glm/glm.hpp"
#include "App.h"
#include "ModelView.h"
#include "Input.h"
#include "view_manager.h"
#include "../objects/components/camera.h"
#include "../objects/components/camera_rig.h"
#include "VrApi.h"

namespace gvr {

class OculusPrediction;
class KSensorPrediction;

template <class PredictionTrait> class GVRActivity : public OVR::VrAppInterface
{
public:
                        GVRActivity( JNIEnv & jni_, jobject activityObject_);
                        ~GVRActivity();

    virtual void        Configure( OVR::ovrSettings & settings );
    virtual void        OneTimeInit( const char * fromPackage, const char * launchIntentJSON, const char * launchIntentURI );
    virtual void        OneTimeShutdown();
    virtual OVR::Matrix4f    DrawEyeView( const int eye, const float fovDegrees );
    virtual OVR::Matrix4f    Frame( const OVR::VrFrame & vrFrame );
    virtual bool        OnKeyEvent( const int keyCode, const int repeatCount, const OVR::KeyEventType eventType );

    // When launched by an intent, we may be viewing a partial
    // scene for debugging, so always clear the screen to grey
    // before drawing, instead of letting partial renders show through.
    bool                forceScreenClear;
    bool                ModelLoaded;

    OVR::OvrSceneView        Scene;

    GVRViewManager*     viewManager;

    Camera*             camera;
    CameraRig*          cameraRig;
    bool                deviceIsDocked;
private:
    glm::mat4           mvp_matrix;
    void                SetMVPMatrix(glm::mat4 mvp){
        viewManager->mvp_matrix = mvp;
    }

    JNIEnv*             UiJni;            // for use by the Java UI thread
    OVR::Matrix4f            GetEyeView( const int eye, const float fovDegrees ) const;

    jobject             javaObject;
    jclass              activityClass;    // must be looked up from main thread or FindClass() will fail

    jclass              vrAppSettingsClass;
    jclass              eyeBufferParmsClass;

    jmethodID           getAppSettingsMethodId;

    jmethodID           oneTimeInitMethodId;
    jmethodID           oneTimeShutdownMethodId;

    jmethodID           drawFrameMethodId;

    jmethodID           beforeDrawEyesMethodId;
    jmethodID           drawEyeViewMethodId;
    jmethodID           afterDrawEyesMethodId;

    jmethodID           onKeyEventNativeMethodId;

    jclass              GetGlobalClassReference( const char * className ) const;
    jmethodID           GetMethodID( const char * name, const char * signature );
    jmethodID           GetStaticMethodID( jclass activityClass, const char * name, const char * signature );
};

class KSensorPrediction {
public:
    static glm::quat getPrediction(GVRActivity<KSensorPrediction>* gvrActivity, const float time) {
        if (nullptr != gvrActivity->cameraRig) {
            return gvrActivity->cameraRig->predict(time);
        } else {
            return glm::quat();
        }
    }
};

class OculusPrediction {
public:
    static glm::quat getPrediction(GVRActivity<OculusPrediction>* gvrActivity, const float time) {
        if (gvrActivity->deviceIsDocked) {
            ovrMobile* ovr = gvrActivity->app->GetOvrMobile();
            const ovrTracking& ovrTracking = vrapi_GetPredictedTracking(ovr, 0.0);

            const ovrQuatf& orientation = ovrTracking.HeadPose.Pose.Orientation;
            glm::quat quat(orientation.w, orientation.x, orientation.y, orientation.z);
            return glm::conjugate(glm::inverse(quat));
        } else if (nullptr != gvrActivity->cameraRig) {
            return gvrActivity->cameraRig->predict(time);
        } else {
            return glm::quat();
        }
    }
};

typedef GVRActivity<KSensorPrediction> GVRActivityReal;

}
#endif
