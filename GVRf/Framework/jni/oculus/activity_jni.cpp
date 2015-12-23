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

#include "activity_jni.h"
#include <jni.h>
#include <glm/gtc/type_ptr.hpp>
#include "VrApi_Helpers.h"
#include "objects/scene_object.h"
#include "VrApi_Types.h"
#include <sstream>

static const char * activityClassName = "org/gearvrf/GVRActivity";
static const char * app_settings_name = "org/gearvrf/utility/VrAppSettings";

namespace gvr {

extern "C" {

long Java_org_gearvrf_GVRActivity_nativeSetAppInterface(
        JNIEnv * jni, jclass clazz, jobject activity,
        jstring fromPackageName, jstring commandString,
        jstring uriString)
{
    GVRActivity* gvrActivity = new GVRActivity(*jni);
    //Oculus takes ownership of gvrActivity
    jlong appPtr = gvrActivity->SetActivity(jni, clazz, activity, fromPackageName, commandString, uriString);
    gvrActivity->initJni();
    return appPtr;
}

void Java_org_gearvrf_GVRActivity_nativeSetCamera(
        JNIEnv * jni, jclass clazz, jlong appPtr, jlong jcamera)
{
    GVRActivity *activity = static_cast<GVRActivity*>(reinterpret_cast<OVR::App*>(appPtr)->GetAppInterface());
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    activity->camera = camera;
}

void Java_org_gearvrf_GVRActivity_nativeSetCameraRig(
        JNIEnv * jni, jclass clazz, jlong appPtr, jlong cameraRig)
{
    GVRActivity *activity = static_cast<GVRActivity*>(reinterpret_cast<OVR::App*>(appPtr)->GetAppInterface());
    activity->setCameraRig(cameraRig);
}

void Java_org_gearvrf_GVRActivity_nativeOnDock(
        JNIEnv * jni, jclass clazz, jlong appPtr)
{
    GVRActivity *activity = static_cast<GVRActivity*>(reinterpret_cast<OVR::App*>(appPtr)->GetAppInterface());
    activity->headRotationProvider_.onDock();
}

void Java_org_gearvrf_GVRActivity_nativeOnUndock(
        JNIEnv * jni, jclass clazz, jlong appPtr)
{
    GVRActivity *activity = static_cast<GVRActivity*>(reinterpret_cast<OVR::App*>(appPtr)->GetAppInterface());
    activity->headRotationProvider_.onUndock();
}

} // extern "C"

//=============================================================================
//                             GVRActivity
//=============================================================================

template <class R> GVRActivityT<R>::GVRActivityT(JNIEnv& jni_)
    : forceScreenClear( false )
    , ModelLoaded( false )
    , uiJni(&jni_)
    , viewManager(NULL)
{
}

template <class R> void GVRActivityT<R>::initJni() {
    viewManager = new GVRViewManager(*uiJni, app->GetJava()->ActivityObject);
    activityClass = GetGlobalClassReference( activityClassName );
    vrAppSettingsClass = GetGlobalClassReference(app_settings_name);

    oneTimeInitMethodId = GetMethodID("oneTimeInit", "()V");
    oneTimeShutdownMethodId = GetMethodID("oneTimeShutDown", "()V");

    drawFrameMethodId = GetMethodID("drawFrame", "()V");
    beforeDrawEyesMethodId = GetMethodID("beforeDrawEyes", "()V");
    drawEyeViewMethodId = GetMethodID("onDrawEyeView", "(IF)V");
    afterDrawEyesMethodId = GetMethodID("afterDrawEyes", "()V");

    onKeyEventNativeMethodId = GetMethodID("onKeyEventNative", "(II)Z");
    updateSensoredSceneMethodId = GetMethodID("updateSensoredScene", "()Z");
    getAppSettingsMethodId = GetMethodID("getAppSettings", "()Lorg/gearvrf/utility/VrAppSettings;");
}

template <class R> jmethodID GVRActivityT<R>::GetStaticMethodID( jclass clazz, const char * name,
        const char * signature) {
    jmethodID mid = uiJni->GetStaticMethodID(clazz, name, signature);
    if (!mid) {
        FAIL("couldn't get %s", name);
    }
    return mid;
}

template <class R> jmethodID GVRActivityT<R>::GetMethodID(const char * name, const char * signature) {
    jmethodID mid = uiJni->GetMethodID(activityClass, name, signature);
    if (!mid) {
        FAIL("couldn't get %s", name );
    }
    return mid;
}


template <class PredictionTrait> jclass GVRActivityT<PredictionTrait>::GetGlobalClassReference(const char * className) const {
    jclass lc = uiJni->FindClass(className);
    if (lc == 0) {
        FAIL( "FindClass( %s ) failed", className);
    }
    // Turn it into a global ref, so we can safely use it in the VR thread
    jclass gc = (jclass) uiJni->NewGlobalRef(lc);
    uiJni->DeleteLocalRef(lc);

    return gc;
}

template <class R> void GVRActivityT<R>::Configure(OVR::ovrSettings & settings)
{
    //General settings.
    JNIEnv *env = app->GetJava()->Env;
    jobject vrSettings = env->CallObjectMethod(app->GetJava()->ActivityObject,
            getAppSettingsMethodId);
    jint framebufferPixelsWide = env->GetIntField(vrSettings,
            env->GetFieldID(vrAppSettingsClass, "framebufferPixelsWide", "I"));
    if (framebufferPixelsWide == -1) {
        app->GetJava()->Env->SetIntField(vrSettings,
                env->GetFieldID(vrAppSettingsClass, "framebufferPixelsWide",
                        "I"), settings.FramebufferPixelsWide);
    } else {
        settings.FramebufferPixelsWide = framebufferPixelsWide;
    }
    jint framebufferPixelsHigh = env->GetIntField(vrSettings,
            env->GetFieldID(vrAppSettingsClass, "framebufferPixelsHigh", "I"));
    if (framebufferPixelsHigh == -1) {
        env->SetIntField(vrSettings,
                env->GetFieldID(vrAppSettingsClass, "framebufferPixelsHigh",
                        "I"), settings.FramebufferPixelsHigh);
    } else {
        settings.FramebufferPixelsHigh = framebufferPixelsHigh;
    }
    settings.ShowLoadingIcon = env->GetBooleanField(vrSettings,
            env->GetFieldID(vrAppSettingsClass, "showLoadingIcon", "Z"));
    settings.UseSrgbFramebuffer = env->GetBooleanField(vrSettings,
            env->GetFieldID(vrAppSettingsClass, "useSrgbFramebuffer", "Z"));
    settings.UseProtectedFramebuffer = env->GetBooleanField(vrSettings,
            env->GetFieldID(vrAppSettingsClass, "useProtectedFramebuffer",
                    "Z"));

    //Settings for EyeBufferParms.
    jobject eyeParmsSettings = env->GetObjectField(vrSettings,
            env->GetFieldID(vrAppSettingsClass, "eyeBufferParms",
                    "Lorg/gearvrf/utility/VrAppSettings$EyeBufferParms;"));
    jclass eyeParmsClass = env->GetObjectClass(eyeParmsSettings);
    settings.EyeBufferParms.multisamples = env->GetIntField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "multiSamples", "I"));
    settings.EyeBufferParms.resolveDepth = env->GetBooleanField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "resolveDepth", "Z"));

    jint resolutionWidth = env->GetIntField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "resolutionWidth", "I"));
    if(resolutionWidth == -1){
        env->SetIntField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "resolutionWidth", "I"), settings.EyeBufferParms.resolutionWidth);
    }else{
        settings.EyeBufferParms.resolutionWidth = resolutionWidth;
    }

    jint resolutionHeight = env->GetIntField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "resolutionHeight", "I"));
    if(resolutionHeight == -1){
        env->SetIntField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "resolutionHeight", "I"), settings.EyeBufferParms.resolutionHeight);
    }else{
        settings.EyeBufferParms.resolutionHeight = resolutionHeight;
    }

    jobject depthFormat = env->GetObjectField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "depthFormat", "Lorg/gearvrf/utility/VrAppSettings$EyeBufferParms$DepthFormat;"));
    jmethodID getValueID;
    getValueID = env->GetMethodID(env->GetObjectClass(depthFormat),"getValue","()I");
    int depthFormatValue = (int)env->CallIntMethod(depthFormat, getValueID);
    switch(depthFormatValue){
    case 0:
        settings.EyeBufferParms.depthFormat = OVR::DEPTH_0;
        break;
    case 1:
        settings.EyeBufferParms.depthFormat = OVR::DEPTH_16;
        break;
    case 2:
        settings.EyeBufferParms.depthFormat = OVR::DEPTH_24;
        break;
    case 3:
        settings.EyeBufferParms.depthFormat = OVR::DEPTH_24_STENCIL_8;
        break;
    default:
        break;
    }
    jobject colorFormat = env->GetObjectField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "colorFormat", "Lorg/gearvrf/utility/VrAppSettings$EyeBufferParms$ColorFormat;"));
    getValueID = env->GetMethodID(env->GetObjectClass(colorFormat),"getValue","()I");
    int colorFormatValue = (int)env->CallIntMethod(colorFormat, getValueID);
    switch(colorFormatValue){
    case 0:
        settings.EyeBufferParms.colorFormat = OVR::COLOR_565;
        break;
    case 1:
        settings.EyeBufferParms.colorFormat = OVR::COLOR_5551;
        break;
    case 2:
        settings.EyeBufferParms.colorFormat = OVR::COLOR_4444;
        break;
    case 3:
        settings.EyeBufferParms.colorFormat = OVR::COLOR_8888;
        break;
    case 4:
        settings.EyeBufferParms.colorFormat = OVR::COLOR_8888_sRGB;
        break;
    default:
        break;
    }


    //Settings for ModeParms
    jobject modeParms = env->GetObjectField(vrSettings, env->GetFieldID(vrAppSettingsClass, "modeParms", "Lorg/gearvrf/utility/VrAppSettings$ModeParms;"));
    jclass modeParmsClass = env->GetObjectClass(modeParms);
    settings.ModeParms.AllowPowerSave = env->GetBooleanField(modeParms, env->GetFieldID(modeParmsClass, "allowPowerSave", "Z"));
    settings.ModeParms.ResetWindowFullscreen = env->GetBooleanField(modeParms, env->GetFieldID(modeParmsClass, "resetWindowFullScreen","Z"));
    jobject performanceParms = env->GetObjectField(vrSettings, env->GetFieldID(vrAppSettingsClass, "performanceParms", "Lorg/gearvrf/utility/VrAppSettings$PerformanceParms;"));
    jclass performanceParmsClass = env->GetObjectClass(performanceParms);
    settings.PerformanceParms.GpuLevel = env->GetIntField(performanceParms, env->GetFieldID(performanceParmsClass, "gpuLevel", "I"));
    settings.PerformanceParms.CpuLevel = env->GetIntField(performanceParms, env->GetFieldID(performanceParmsClass, "cpuLevel", "I"));

    // Settings for HeadModelParms
    jobject headModelParms = env->GetObjectField(vrSettings, env->GetFieldID(vrAppSettingsClass, "headModelParms", "Lorg/gearvrf/utility/VrAppSettings$HeadModelParms;" ));
    jclass headModelParmsClass = env->GetObjectClass(headModelParms);
    float interpupillaryDistance = (float)env->GetFloatField(headModelParms, env->GetFieldID(headModelParmsClass, "interpupillaryDistance", "F"));
    if(interpupillaryDistance != interpupillaryDistance){
        //Value not set in Java side, current Value is NaN
        //Need to copy the system settings to java side.
        env->SetFloatField(headModelParms, env->GetFieldID(headModelParmsClass, "interpupillaryDistance", "F"), settings.HeadModelParms.InterpupillaryDistance);
    }else{
        settings.HeadModelParms.InterpupillaryDistance = interpupillaryDistance;
    }
    float eyeHeight = (float)env->GetFloatField(headModelParms, env->GetFieldID(headModelParmsClass, "eyeHeight", "F"));
    if(eyeHeight != eyeHeight){
        //same as interpupilaryDistance
        env->SetFloatField(headModelParms, env->GetFieldID(headModelParmsClass, "eyeHeight", "F"), settings.HeadModelParms.EyeHeight);
    }else{
        settings.HeadModelParms.EyeHeight = eyeHeight;
    }
    float headModelDepth = (float)env->GetFloatField(headModelParms, env->GetFieldID(headModelParmsClass, "headModelDepth", "F"));
    if(headModelDepth != headModelDepth){
            //same as interpupilaryDistance
        env->SetFloatField(headModelParms, env->GetFieldID(headModelParmsClass, "headModelDepth", "F"), settings.HeadModelParms.HeadModelDepth);
    }else{
        settings.HeadModelParms.HeadModelDepth = headModelDepth;
    }
    float headModelHeight = (float)env->GetFloatField(headModelParms, env->GetFieldID(headModelParmsClass, "headModelHeight", "F"));
    if(headModelHeight != headModelHeight){
            //same as interpupilaryDistance
        env->SetFloatField(headModelParms, env->GetFieldID(headModelParmsClass, "headModelHeight", "F"), settings.HeadModelParms.HeadModelHeight);
    }else{
        settings.HeadModelParms.HeadModelHeight = headModelHeight;
    }
    if (env->GetStaticBooleanField(vrAppSettingsClass,
            env->GetStaticFieldID(vrAppSettingsClass, "isShowDebugLog", "Z"))) {
        std::stringstream logInfo;
        logInfo << "====== General Configuration ======" << std::endl;
        if (settings.FramebufferPixelsHigh == 0
                && settings.FramebufferPixelsWide == 0) {
            logInfo
                    << "FramebufferPixelsHigh = screen size; FramebufferPixelsWide = screen size \n";
        } else {
            logInfo << "FramebufferPixelsHigh = "
                    << settings.FramebufferPixelsHigh
                    << "; FrameBufferPixelsWide = "
                    << settings.FramebufferPixelsWide << std::endl;
        }
        logInfo << "ShowLoadingIcon = " << settings.ShowLoadingIcon
                << "; UseProtectedFramebuffer = "
                << settings.UseProtectedFramebuffer << "; UseSrgbFramebuffer = "
                << settings.UseSrgbFramebuffer << "\n";
        logInfo << "====== Eye Buffer Configuration ======\n";
        logInfo << "colorFormat = ";
        switch (settings.EyeBufferParms.colorFormat) {
        case 0:
            logInfo << "COLOR_565";
            break;
        case 1:
            logInfo << "COLOR_5551";
            break;
        case 2:
            logInfo << "COLOR_4444";
            break;
        case 3:
            logInfo << "COLOR_8888";
            break;
        case 4:
            logInfo << "COLOR_8888_sRGB";
            break;
        default:
            break;
        }
        logInfo << "; depthFormat = ";
        switch (settings.EyeBufferParms.depthFormat) {
        case 0:
            logInfo << "DEPTH_0";
            break;
        case 1:
            logInfo << "DEPTH_16";
            break;
        case 2:
            logInfo << "DEPTH_24";
            break;
        case 3:
            logInfo << "DEPTH_24_STENCIL_8";
            break;
        default:
            break;
        }
        logInfo << "; ResolveDepth = " << settings.EyeBufferParms.resolveDepth
                << "; multiSample = " << settings.EyeBufferParms.multisamples
                << "; resolutionWidth = " << settings.EyeBufferParms.resolutionWidth
                << "; resolutionHeight = " << settings.EyeBufferParms.resolutionHeight
                << std::endl;
        logInfo << "====== Head Model Configuration ======" << std::endl;
        logInfo << "EyeHeight = " << settings.HeadModelParms.EyeHeight
                << "; HeadModelDepth = "
                << settings.HeadModelParms.HeadModelDepth
                << "; HeadModelHeight = "
                << settings.HeadModelParms.HeadModelHeight
                << "; InterpupillaryDistance = "
                << settings.HeadModelParms.InterpupillaryDistance << std::endl;
        logInfo << "====== Mode Configuration ======" << std::endl;
        logInfo << "AllowPowerSave = " << settings.ModeParms.AllowPowerSave
                << "; ResetWindowFullscreen = "
                << settings.ModeParms.ResetWindowFullscreen << std::endl;
        logInfo << "====== Performance Configuration ======"
                << "; CpuLevel = " << settings.PerformanceParms.CpuLevel
                << "; GpuLevel = " << settings.PerformanceParms.GpuLevel << std::endl;

        LOGI("%s", logInfo.str().c_str());
    }
}

template <class R> void GVRActivityT<R>::OneTimeInit(const char * fromPackage, const char * launchIntentJSON, const char * launchIntentURI)
{
    app->GetJava()->Env->CallVoidMethod(app->GetJava()->ActivityObject, oneTimeInitMethodId );
}

template <class R> void GVRActivityT<R>::OneTimeShutdown()
{
    app->GetJava()->Env->CallVoidMethod(app->GetJava()->ActivityObject, oneTimeShutdownMethodId);

    // Free GL resources
}

template <class R> OVR::Matrix4f GVRActivityT<R>::GetEyeView(const int eye, const float fovDegreesX, const float fovDegreesY) const
{
    const OVR::Matrix4f projectionMatrix = Scene.GetEyeProjectionMatrix( eye, fovDegreesX, fovDegreesY );
    const OVR::Matrix4f viewMatrix = Scene.GetEyeViewMatrix( eye );
    return ( projectionMatrix * viewMatrix );
}

template <class R> OVR::Matrix4f GVRActivityT<R>::DrawEyeView(const int eye, const float fovDegreesX, const float fovDegreesY, ovrFrameParms & frameParms) {
    const OVR::Matrix4f view = GetEyeView(eye, fovDegreesX, fovDegreesY);

    // Transpose view matrix from oculus to mvp_matrix to rendering correctly with gvrf renderer.
    mvp_matrix = glm::mat4(view.M[0][0], view.M[1][0], view.M[2][0],
            view.M[3][0], view.M[0][1], view.M[1][1], view.M[2][1],
            view.M[3][1], view.M[0][2], view.M[1][2], view.M[2][2],
            view.M[3][2], view.M[0][3], view.M[1][3], view.M[2][3],
            view.M[3][3]);

    SetMVPMatrix(mvp_matrix);

    if (!sensoredSceneUpdated_ && headRotationProvider_.receivingUpdates()) {
        sensoredSceneUpdated_ = updateSensoredScene();
    }
    headRotationProvider_.predict(*this, frameParms, (1 == eye ? 4.0f : 3.5f) / 60.0f);

    JNIEnv* jni = app->GetJava()->Env;
    jni->CallVoidMethod(app->GetJava()->ActivityObject, drawEyeViewMethodId, eye, fovDegreesY);

    if (eye == 1) {
        jni->CallVoidMethod(app->GetJava()->ActivityObject, afterDrawEyesMethodId);
    }

    glm::mat4 view_matrix = camera->getViewMatrix();
    glm::mat4 projection_matrix = camera->getProjectionMatrix(); //gun
    glm::mat4 vp_matrix = glm::mat4(projection_matrix * view_matrix);

    OVR::Matrix4f view2 = OVR::Matrix4f(vp_matrix[0][0], vp_matrix[1][0],
            vp_matrix[2][0], vp_matrix[3][0], vp_matrix[0][1], vp_matrix[1][1],
            vp_matrix[2][1], vp_matrix[3][1], vp_matrix[0][2], vp_matrix[1][2],
            vp_matrix[2][2], vp_matrix[3][2], vp_matrix[0][3], vp_matrix[1][3],
            vp_matrix[2][3], vp_matrix[3][3]);

    return view2;

}

template <class R> OVR::Matrix4f GVRActivityT<R>::Frame( const OVR::VrFrame & vrFrame )
{
    JNIEnv* jni = app->GetJava()->Env;
    jni->CallVoidMethod(app->GetJava()->ActivityObject, beforeDrawEyesMethodId);
    jni->CallVoidMethod(app->GetJava()->ActivityObject, drawFrameMethodId);

	//This is called once while DrawEyeView is called twice, when eye=0 and eye 1.
	//So camera is set in java as one of left and right camera.
	//Centerview camera matrix can be retrieved from its parent, CameraRig
    glm::mat4 vp_matrix = camera->getCenterViewMatrix();

    ovrMatrix4f view2;

    view2.M[0][0] = vp_matrix[0][0];
    view2.M[1][0] = vp_matrix[0][1];
    view2.M[2][0] = vp_matrix[0][2];
    view2.M[3][0] = vp_matrix[0][3];
    view2.M[0][1] = vp_matrix[1][0];
    view2.M[1][1] = vp_matrix[1][1];
    view2.M[2][1] = vp_matrix[1][2];
    view2.M[3][1] = vp_matrix[1][3];
    view2.M[0][2] = vp_matrix[2][0];
    view2.M[1][2] = vp_matrix[2][1];
    view2.M[2][2] = vp_matrix[2][2];
    view2.M[3][2] = vp_matrix[2][3];
    view2.M[0][3] = vp_matrix[3][0];
    view2.M[1][3] = vp_matrix[3][1];
    view2.M[2][3] = vp_matrix[3][2];
    view2.M[3][3] = vp_matrix[3][3];

    return view2;
}

template <class R> bool GVRActivityT<R>::OnKeyEvent(const int keyCode, const int repeatCode,
        const OVR::KeyEventType eventType) {

    bool handled = app->GetJava()->Env->CallBooleanMethod(app->GetJava()->ActivityObject,
            onKeyEventNativeMethodId, keyCode, (int)eventType);

    // if not handled back key long press, show global menu
    if (handled == false && keyCode == OVR::OVR_KEY_BACK && eventType == OVR::KEY_EVENT_LONG_PRESS) {
        app->StartSystemActivity(PUI_GLOBAL_MENU);
    }

    return handled;
}

template <class R> bool GVRActivityT<R>::updateSensoredScene() {
    return app->GetJava()->Env->CallBooleanMethod(app->GetJava()->ActivityObject, updateSensoredSceneMethodId);
}

template <class R> void GVRActivityT<R>::setCameraRig(jlong cameraRig) {
    cameraRig_ = reinterpret_cast<CameraRig*>(cameraRig);
    sensoredSceneUpdated_ = false;
}

}
