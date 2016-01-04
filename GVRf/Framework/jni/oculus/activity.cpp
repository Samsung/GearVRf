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

#include "activity.h"
#include "../util/jni_utils.h"

#include <jni.h>
#include "VrApi.h"
#include "VrApi_Types.h"
#include "VrApi_Helpers.h"
#include "SystemActivities.h"

static const char* activityClassName = "org/gearvrf/GVRActivity";
static const char* activityHandlerRenderingCallbacksClassName = "org/gearvrf/ActivityHandlerRenderingCallbacks";

namespace gvr {

//=============================================================================
//                             GVRActivity
//=============================================================================

GVRActivity::GVRActivity(JNIEnv& env, jobject activity, jobject vrAppSettings,
        jobject callbacks) : envMainThread_(&env), configurationHelper_(env, vrAppSettings)
{
    activity_ = env.NewGlobalRef(activity);
    activityRenderingCallbacks_ = env.NewGlobalRef(callbacks);

    activityClass_ = GetGlobalClassReference(env, activityClassName);
    activityRenderingCallbacksClass_ = GetGlobalClassReference(env, activityHandlerRenderingCallbacksClassName);

    onDrawEyeMethodId = GetMethodId(env, activityRenderingCallbacksClass_, "onDrawEye", "(I)V");
    updateSensoredSceneMethodId = GetMethodId(env, activityClass_, "updateSensoredScene", "()Z");
}

GVRActivity::~GVRActivity() {
    LOGV("GVRActivity::~GVRActivity");

    SystemActivities_Shutdown(&oculusJavaMainThread_);
    vrapi_Shutdown();

    envMainThread_->DeleteGlobalRef(activityRenderingCallbacksClass_);
    envMainThread_->DeleteGlobalRef(activityClass_);

    envMainThread_->DeleteGlobalRef(activityRenderingCallbacks_);
    envMainThread_->DeleteGlobalRef(activity_);
}

int GVRActivity::initializeVrApi() {
    initializeOculusJava(*envMainThread_, oculusJavaMainThread_);

    const ovrInitParms initParms = vrapi_DefaultInitParms(&oculusJavaMainThread_);
    int32_t initResult = vrapi_Initialize(&initParms);
    if (VRAPI_INITIALIZE_UNKNOWN_ERROR == initResult) {
        LOGE("Oculus is probably not present on this device");
        return initResult;
    }

    SystemActivities_Init(&oculusJavaMainThread_);
    if (VRAPI_INITIALIZE_PERMISSIONS_ERROR == initResult) {
        char const * msg =
                initResult == VRAPI_INITIALIZE_PERMISSIONS_ERROR ?
                        "Thread priority security exception. Make sure the APK is signed." :
                        "VrApi initialization error.";
        SystemActivities_DisplayError(&oculusJavaMainThread_, SYSTEM_ACTIVITIES_FATAL_ERROR_OSIG, __FILE__, msg);
        SystemActivities_Shutdown(&oculusJavaMainThread_);
    }

    return initResult;
}

void GVRActivity::showGlobalMenu() {
    LOGV("GVRActivity::showGlobalMenu");
    SystemActivities_StartSystemActivity(&oculusJavaMainThread_, PUI_GLOBAL_MENU, NULL);
}

void GVRActivity::showConfirmQuit() {
    LOGV("GVRActivity::showConfirmQuit");
    SystemActivities_StartSystemActivity(&oculusJavaMainThread_, PUI_CONFIRM_QUIT, NULL);
}

bool GVRActivity::updateSensoredScene() {
    return oculusJavaGlThread_.Env->CallBooleanMethod(oculusJavaGlThread_.ActivityObject, updateSensoredSceneMethodId);
}

void GVRActivity::setCameraRig(jlong cameraRig) {
    cameraRig_ = reinterpret_cast<CameraRig*>(cameraRig);
    sensoredSceneUpdated_ = false;
}

void GVRActivity::onSurfaceCreated(JNIEnv& env) {
    LOGV("GVRActivity::onSurfaceCreated");
    initializeOculusJava(env, oculusJavaGlThread_);
}

void GVRActivity::onSurfaceChanged(JNIEnv& env) {
    LOGV("GVRActivityT::onSurfaceChanged");
    initializeOculusJava(env, oculusJavaGlThread_);

    if (nullptr == oculusMobile_) {
        ovrModeParms parms = vrapi_DefaultModeParms(&oculusJavaGlThread_);
        configurationHelper_.getModeConfiguration(env, parms.AllowPowerSave, parms.ResetWindowFullscreen);
        oculusMobile_ = vrapi_EnterVrMode(&parms);

        oculusPerformanceParms_ = vrapi_DefaultPerformanceParms();
        configurationHelper_.getPerformanceConfiguration(env, oculusPerformanceParms_);

        oculusHeadModelParms_ = vrapi_DefaultHeadModelParms();
        configurationHelper_.getHeadModelConfiguration(env, oculusHeadModelParms_);

        //@todo struct instead?
        int width, height, multisamples;
        ovrTextureFormat colorTextureFormat;
        ovrTextureFormat depthTextureFormat;
        bool resolveDepth;
        configurationHelper_.getFramebufferConfiguration(env, width, height,
                vrapi_GetSystemPropertyInt(&oculusJavaGlThread_, VRAPI_SYS_PROP_SUGGESTED_EYE_TEXTURE_WIDTH),
                vrapi_GetSystemPropertyInt(&oculusJavaGlThread_, VRAPI_SYS_PROP_SUGGESTED_EYE_TEXTURE_HEIGHT),
                multisamples, colorTextureFormat, resolveDepth, depthTextureFormat);

        for (int eye = 0; eye < VRAPI_FRAME_LAYER_EYE_MAX; eye++) {
            bool b = frameBuffer_[eye].create(colorTextureFormat, width, height, multisamples, resolveDepth,
                    depthTextureFormat);
        }

        projectionMatrix_ = ovrMatrix4f_CreateProjectionFov(
                vrapi_GetSystemPropertyFloat(&oculusJavaGlThread_, VRAPI_SYS_PROP_SUGGESTED_EYE_FOV_DEGREES_X),
                vrapi_GetSystemPropertyFloat(&oculusJavaGlThread_, VRAPI_SYS_PROP_SUGGESTED_EYE_FOV_DEGREES_Y), 0.0f, 0.0f, 1.0f,
                0.0f);
        texCoordsTanAnglesMatrix_ = ovrMatrix4f_TanAngleMatrixFromProjection(&projectionMatrix_);
    }
}

void GVRActivity::onDrawFrame() {
    ovrFrameParms parms = vrapi_DefaultFrameParms(&oculusJavaGlThread_, VRAPI_FRAME_INIT_DEFAULT, vrapi_GetTimeInSeconds(),
            NULL);
    parms.FrameIndex = ++frameIndex;
    parms.MinimumVsyncs = 1;
    parms.PerformanceParms = oculusPerformanceParms_;
    parms.Layers[VRAPI_FRAME_LAYER_TYPE_WORLD].Flags |= VRAPI_FRAME_LAYER_FLAG_CHROMATIC_ABERRATION_CORRECTION;

    const double predictedDisplayTime = vrapi_GetPredictedDisplayTime(oculusMobile_, frameIndex);
    const ovrTracking baseTracking = vrapi_GetPredictedTracking(oculusMobile_, predictedDisplayTime);

    const ovrHeadModelParms headModelParms = vrapi_DefaultHeadModelParms();
    const ovrTracking tracking = vrapi_ApplyHeadModel(&headModelParms, &baseTracking);

    // Render the eye images.
    for (int eye = 0; eye < VRAPI_FRAME_LAYER_EYE_MAX; eye++) {
        ovrTracking updatedTracking = vrapi_GetPredictedTracking(oculusMobile_, tracking.HeadPose.TimeInSeconds);
        updatedTracking.HeadPose.Pose.Position = tracking.HeadPose.Pose.Position;

        frameBuffer_[eye].bind();
        GL(glViewport(0, 0, frameBuffer_[eye].mWidth, frameBuffer_[eye].mHeight));
        GL(glScissor(0, 0, frameBuffer_[eye].mWidth, frameBuffer_[eye].mHeight));

        if (!sensoredSceneUpdated_ && headRotationProvider_.receivingUpdates()) {
            sensoredSceneUpdated_ = updateSensoredScene();
        }
        headRotationProvider_.predict(*this, parms, (1 == eye ? 4.0f : 3.5f) / 60.0f);
        oculusJavaGlThread_.Env->CallVoidMethod(activityRenderingCallbacks_, onDrawEyeMethodId, eye);

        frameBuffer_[eye].resolve();

        parms.Layers[VRAPI_FRAME_LAYER_TYPE_WORLD].Textures[eye].ColorTextureSwapChain =
                frameBuffer_[eye].mColorTextureSwapChain;
        parms.Layers[VRAPI_FRAME_LAYER_TYPE_WORLD].Textures[eye].TextureSwapChainIndex =
                frameBuffer_[eye].mTextureSwapChainIndex;
        for (int layer = 0; layer < VRAPI_FRAME_LAYER_TYPE_MAX; layer++) {
            parms.Layers[layer].Textures[eye].TexCoordsFromTanAngles = texCoordsTanAnglesMatrix_;
            parms.Layers[layer].Textures[eye].HeadPose = updatedTracking.HeadPose;
        }

        frameBuffer_[eye].advance();
    }

    FrameBufferObject::unbind();

    vrapi_SubmitFrame(oculusMobile_, &parms);
}

void GVRActivity::initializeOculusJava(JNIEnv& env, ovrJava& oculusJava) {
    oculusJava.Env = &env;
    env.GetJavaVM(&oculusJava.Vm);
    oculusJava.ActivityObject = activity_;
}

void GVRActivity::leaveVrMode() {
    LOGV("GVRActivity::leaveVrMode");

    if (nullptr != oculusMobile_) {
        for (int eye = 0; eye < VRAPI_FRAME_LAYER_EYE_MAX; eye++) {
            frameBuffer_[eye].destroy();
        }

        vrapi_LeaveVrMode(oculusMobile_);
        oculusMobile_ = nullptr;
    } else {
        LOGW("GVRActivity::leaveVrMode: ignored, have not entered vrMode");
    }
}

}
