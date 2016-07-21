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

#include "activity.h"
#include "../util/jni_utils.h"
#include "../eglextension/msaa/msaa.h"
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
    uninitializeVrApi();

    envMainThread_->DeleteGlobalRef(activityRenderingCallbacksClass_);
    envMainThread_->DeleteGlobalRef(activityClass_);

    envMainThread_->DeleteGlobalRef(activityRenderingCallbacks_);
    envMainThread_->DeleteGlobalRef(activity_);
}

int GVRActivity::initializeVrApi() {
    initializeOculusJava(*envMainThread_, oculusJavaMainThread_);

    const ovrInitParms initParms = vrapi_DefaultInitParms(&oculusJavaMainThread_);
    mVrapiInitResult = vrapi_Initialize(&initParms);
    if (VRAPI_INITIALIZE_UNKNOWN_ERROR == mVrapiInitResult) {
        LOGE("Oculus is probably not present on this device");
        return mVrapiInitResult;
    }

    SystemActivities_Init(&oculusJavaMainThread_);
    if (VRAPI_INITIALIZE_PERMISSIONS_ERROR == mVrapiInitResult) {
        char const * msg =
                mVrapiInitResult == VRAPI_INITIALIZE_PERMISSIONS_ERROR ?
                        "Thread priority security exception. Make sure the APK is signed." :
                        "VrApi initialization error.";
        SystemActivities_DisplayError(&oculusJavaMainThread_, SYSTEM_ACTIVITIES_FATAL_ERROR_OSIG, __FILE__, msg);
        SystemActivities_Shutdown(&oculusJavaMainThread_);
    }

    return mVrapiInitResult;
}

void GVRActivity::uninitializeVrApi() {
    if (VRAPI_INITIALIZE_UNKNOWN_ERROR != mVrapiInitResult) {
        SystemActivities_Shutdown(&oculusJavaMainThread_);
        vrapi_Shutdown();
    }
    mVrapiInitResult = VRAPI_INITIALIZE_UNKNOWN_ERROR;
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

    //must happen as soon as possible as it updates the java side wherever it has default values; e.g.
    //resolutionWidth -1 becomes whatever VRAPI_SYS_PROP_SUGGESTED_EYE_TEXTURE_WIDTH is.
    configurationHelper_.getFramebufferConfiguration(env, mWidthConfiguration, mHeightConfiguration,
            vrapi_GetSystemPropertyInt(&oculusJavaGlThread_, VRAPI_SYS_PROP_SUGGESTED_EYE_TEXTURE_WIDTH),
            vrapi_GetSystemPropertyInt(&oculusJavaGlThread_, VRAPI_SYS_PROP_SUGGESTED_EYE_TEXTURE_HEIGHT),
            mMultisamplesConfiguration, mColorTextureFormatConfiguration,
            mResolveDepthConfiguration, mDepthTextureFormatConfiguration);
}

void GVRActivity::onSurfaceChanged(JNIEnv& env) {
    int maxSamples = MSAA::getMaxSampleCount();
    LOGV("GVRActivityT::onSurfaceChanged");
    initializeOculusJava(env, oculusJavaGlThread_);

    if (nullptr == oculusMobile_) {
        ovrModeParms parms = vrapi_DefaultModeParms(&oculusJavaGlThread_);
    	bool AllowPowerSave, ResetWindowFullscreen;
        configurationHelper_.getModeConfiguration(env, AllowPowerSave, ResetWindowFullscreen);
        parms.Flags |=AllowPowerSave;
        parms.Flags |=ResetWindowFullscreen;

        oculusMobile_ = vrapi_EnterVrMode(&parms);

        oculusPerformanceParms_ = vrapi_DefaultPerformanceParms();
        configurationHelper_.getPerformanceConfiguration(env, oculusPerformanceParms_);

        oculusHeadModelParms_ = vrapi_DefaultHeadModelParms();
        configurationHelper_.getHeadModelConfiguration(env, oculusHeadModelParms_);
        if (mMultisamplesConfiguration > maxSamples)
            mMultisamplesConfiguration = maxSamples;

        for (int eye = 0; eye < VRAPI_FRAME_LAYER_EYE_MAX; eye++) {
            bool b = frameBuffer_[eye].create(mColorTextureFormatConfiguration, mWidthConfiguration,
                    mHeightConfiguration, mMultisamplesConfiguration, mResolveDepthConfiguration,
                    mDepthTextureFormatConfiguration);
        }

        // default viewport same as window size
        x = 0;
        y = 0;
        width = mWidthConfiguration;
        height = mHeightConfiguration;
        configurationHelper_.getSceneViewport(env, x, y, width, height);

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

        beginRenderingEye(eye);

        if (!sensoredSceneUpdated_ && headRotationProvider_.receivingUpdates()) {
            sensoredSceneUpdated_ = updateSensoredScene();
        }
        headRotationProvider_.predict(*this, parms, (1 == eye ? 4.0f : 3.5f) / 60.0f);
        oculusJavaGlThread_.Env->CallVoidMethod(activityRenderingCallbacks_, onDrawEyeMethodId, eye);

        ovrFrameLayerTexture& eyeTexture = parms.Layers[VRAPI_FRAME_LAYER_TYPE_WORLD].Textures[eye];
        eyeTexture.ColorTextureSwapChain = frameBuffer_[eye].mColorTextureSwapChain;
        eyeTexture.DepthTextureSwapChain = frameBuffer_[eye].mDepthTextureSwapChain;
        eyeTexture.TextureSwapChainIndex = frameBuffer_[eye].mTextureSwapChainIndex;

        for (int layer = 0; layer < VRAPI_FRAME_LAYER_TYPE_MAX; layer++) {
            parms.Layers[layer].Textures[eye].TexCoordsFromTanAngles = texCoordsTanAnglesMatrix_;
            parms.Layers[layer].Textures[eye].HeadPose = updatedTracking.HeadPose;
        }

        endRenderingEye(eye);
    }

    FrameBufferObject::unbind();
    vrapi_SubmitFrame(oculusMobile_, &parms);
}

static const GLenum attachments[] = {GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT};

void GVRActivity::beginRenderingEye(const int eye) {
    frameBuffer_[eye].bind();

    GL(glViewport(x, y, width, height));
    GL(glScissor(0, 0, frameBuffer_[eye].mWidth, frameBuffer_[eye].mHeight));

    GL(glDepthMask(GL_TRUE));
    GL(glEnable(GL_DEPTH_TEST));
    GL(glDepthFunc(GL_LEQUAL));
    GL(glInvalidateFramebuffer(GL_FRAMEBUFFER, sizeof(attachments)/sizeof(GLenum), attachments));
    GL(glClear(GL_DEPTH_BUFFER_BIT));
}

void GVRActivity::endRenderingEye(const int eye) {
    GL(glDisable(GL_DEPTH_TEST));
    GL(glDisable(GL_CULL_FACE));

    // quote off VrApi_Types.h:
    // <quote>
    // Because OpenGL ES does not support clampToBorder, it is the
    // application's responsibility to make sure that all mip levels
    // of the primary eye texture have a black border that will show
    // up when time warp pushes the texture partially off screen.
    // </quote>
    // also see EyePostRender::FillEdgeColor in VrAppFramework
    GL(glClearColor(0, 0, 0, 1));
    GL(glEnable(GL_SCISSOR_TEST));

    GL(glScissor(0, 0, mWidthConfiguration, 1));
    GL(glClear( GL_COLOR_BUFFER_BIT));
    GL(glScissor(0, mHeightConfiguration - 1, mWidthConfiguration, 1));
    GL(glClear( GL_COLOR_BUFFER_BIT));
    GL(glScissor(0, 0, 1, mHeightConfiguration));
    GL(glClear( GL_COLOR_BUFFER_BIT));
    GL(glScissor(mWidthConfiguration - 1, 0, 1, mHeightConfiguration));
    GL(glClear( GL_COLOR_BUFFER_BIT));

    GL(glDisable(GL_SCISSOR_TEST));

    //per vrAppFw
    GL(glFlush());
    frameBuffer_[eye].resolve();
    frameBuffer_[eye].advance();
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

/**
 * Must be called on the GL thread
 */
bool GVRActivity::isHmtConnected() const {
    return vrapi_GetSystemStatusInt(&oculusJavaMainThread_, VRAPI_SYS_STATUS_DOCKED);
}

}
