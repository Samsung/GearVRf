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

static const char * activityClassName = "org/gearvrf/GVRActivity";

namespace gvr {

extern "C" {

long Java_org_gearvrf_GVRActivity_nativeSetAppInterface(
        JNIEnv * jni, jclass clazz, jobject activity,
        jstring fromPackageName, jstring commandString,
        jstring uriString)
{
    return (new GVRActivityReal(*jni,activity))->SetActivity( jni, clazz, activity, fromPackageName, commandString, uriString );
}

void Java_org_gearvrf_GVRActivity_nativeSetCamera(
        JNIEnv * jni, jclass clazz, jlong appPtr, jlong jcamera)
{
    GVRActivityReal *activity = (GVRActivityReal*)((OVR::App *)appPtr)->GetAppInterface();
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    activity->camera = camera;
}

void Java_org_gearvrf_GVRActivity_nativeSetCameraRig(
        JNIEnv * jni, jclass clazz, jlong appPtr, jlong jCameraRig)
{
    GVRActivityReal *activity = (GVRActivityReal*)((OVR::App *)appPtr)->GetAppInterface();
    activity->cameraRig = reinterpret_cast<CameraRig*>(jCameraRig);
}

} // extern "C"

//=============================================================================
//                             GVRActivity
//=============================================================================

template <class PredictionTrait> GVRActivity<PredictionTrait>::GVRActivity(JNIEnv & jni_, jobject activityObject_)
    : forceScreenClear( false )
    , ModelLoaded( false )
    , UiJni(&jni_)
    , viewManager(NULL)
    , cameraRig(nullptr)
{
    viewManager = new GVRViewManager(jni_,activityObject_);
    javaObject = UiJni->NewGlobalRef( activityObject_ );
    activityClass = GetGlobalClassReference( activityClassName );
    vrAppSettingsClass = GetGlobalClassReference(app_settings_name);

    oneTimeInitMethodId = GetMethodID("oneTimeInit", "()V");
    oneTimeShutdownMethodId = GetMethodID("oneTimeShutDown", "()V");

    drawFrameMethodId = GetMethodID("drawFrame", "()V");
    beforeDrawEyesMethodId = GetMethodID("beforeDrawEyes", "()V");
    drawEyeViewMethodId = GetMethodID("onDrawEyeView", "(IF)V");
    afterDrawEyesMethodId = GetMethodID("afterDrawEyes", "()V");

    onKeyEventNativeMethodId = GetMethodID("onKeyEventNative", "(II)Z");
    getAppSettingsMethodId = GetMethodID("getAppSettings",
            "()Lorg/gearvrf/utility/VrAppSettings;");

}

template <class PredictionTrait> GVRActivity<PredictionTrait>::~GVRActivity() {
    if ( javaObject != 0 )
    {
        UiJni->DeleteGlobalRef( javaObject );
    }
}

template <class PredictionTrait> jmethodID GVRActivity<PredictionTrait>::GetStaticMethodID( jclass clazz, const char * name,
        const char * signature) {
    jmethodID mid = UiJni->GetStaticMethodID(clazz, name, signature);
    if (!mid) {
        FAIL("couldn't get %s", name);
    }
    return mid;
}

template <class PredictionTrait> jmethodID GVRActivity<PredictionTrait>::GetMethodID(const char * name, const char * signature) {
    jmethodID mid = UiJni->GetMethodID(activityClass, name, signature);
    if (!mid) {
        FAIL("couldn't get %s", name );
    }
    return mid;
}


template <class PredictionTrait> jclass GVRActivity<PredictionTrait>::GetGlobalClassReference(const char * className) const {
    jclass lc = UiJni->FindClass(className);
    if (lc == 0) {
        FAIL( "FindClass( %s ) failed", className);
    }
    // Turn it into a global ref, so we can safely use it in the VR thread
    jclass gc = (jclass) UiJni->NewGlobalRef(lc);

    UiJni->DeleteLocalRef(lc);

    return gc;
}

template <class PredictionTrait> void GVRActivity<PredictionTrait>::Configure(OVR::ovrSettings & settings)
{
    //General settings.
    JNIEnv *env = app->GetVrJni();
    jobject vrSettings = env->CallObjectMethod(javaObject,
            getAppSettingsMethodId);
    jint framebufferPixelsWide = env->GetIntField(vrSettings,
            env->GetFieldID(vrAppSettingsClass, "framebufferPixelsWide", "I"));
    if (framebufferPixelsWide == -1) {
        app->GetVrJni()->SetIntField(vrSettings,
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
    settings.EyeBufferParms.WidthScale = env->GetIntField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "widthScale", "I"));
    jint resolution = env->GetIntField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "resolution", "I"));
    if(resolution == -1){
        env->SetIntField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "resolution", "I"), settings.EyeBufferParms.resolution);
    }else{
        settings.EyeBufferParms.resolution = resolution;
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
    jobject textureFilter = env->GetObjectField(eyeParmsSettings, env->GetFieldID(eyeParmsClass, "textureFilter", "Lorg/gearvrf/utility/VrAppSettings$EyeBufferParms$TextureFilter;"));
    getValueID = env->GetMethodID(env->GetObjectClass(textureFilter),"getValue","()I");
    int textureFilterValue = (int)env->CallIntMethod(textureFilter, getValueID);
    switch(textureFilterValue){
    case 0:
        settings.EyeBufferParms.textureFilter = OVR::TEXTURE_FILTER_NEAREST;
        break;
    case 1:
        settings.EyeBufferParms.textureFilter = OVR::TEXTURE_FILTER_BILINEAR;
        break;
    case 2:
        settings.EyeBufferParms.textureFilter = OVR::TEXTURE_FILTER_ANISO_2;
        break;
    case 3:
        settings.EyeBufferParms.textureFilter = OVR::TEXTURE_FILTER_ANISO_4;
        break;
    default:
        break;
    }

    //Settings for ModeParms
    jobject modeParms = env->GetObjectField(vrSettings, env->GetFieldID(vrAppSettingsClass, "modeParms", "Lorg/gearvrf/utility/VrAppSettings$ModeParms;"));
    jclass modeParmsClass = env->GetObjectClass(modeParms);
    settings.ModeParms.AllowPowerSave = env->GetBooleanField(modeParms, env->GetFieldID(modeParmsClass, "allowPowerSave", "Z"));
    settings.ModeParms.ResetWindowFullscreen = env->GetBooleanField(modeParms, env->GetFieldID(modeParmsClass, "resetWindowFullScreen","Z"));
    settings.ModeParms.GpuLevel = env->GetIntField(modeParms, env->GetFieldID(modeParmsClass, "gpuLevel", "I"));
    settings.ModeParms.CpuLevel = env->GetIntField(modeParms, env->GetFieldID(modeParmsClass, "cpuLevel", "I"));

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
}

template <class PredictionTrait> void GVRActivity<PredictionTrait>::OneTimeInit(const char * fromPackage, const char * launchIntentJSON, const char * launchIntentURI)
{
    app->GetVrJni()->CallVoidMethod( javaObject, oneTimeInitMethodId );
}

template <class PredictionTrait> void GVRActivity<PredictionTrait>::OneTimeShutdown()
{
    app->GetVrJni()->CallVoidMethod(javaObject, oneTimeShutdownMethodId);

    // Free GL resources
}

template <class PredictionTrait> OVR::Matrix4f GVRActivity<PredictionTrait>::GetEyeView(const int eye, const float fovDegrees) const
{
    const OVR::Matrix4f projectionMatrix = Scene.ProjectionMatrixForEye( eye, fovDegrees );
    const OVR::Matrix4f viewMatrix = Scene.ViewMatrixForEye( eye );
    return ( projectionMatrix * viewMatrix );
}

template <class PredictionTrait> OVR::Matrix4f GVRActivity<PredictionTrait>::DrawEyeView(const int eye, const float fovDegrees) {
    const OVR::Matrix4f view = GetEyeView(eye, fovDegrees);

    // Transpose view matrix from oculus to mvp_matrix to rendering correctly with gvrf renderer.
    mvp_matrix = glm::mat4(view.M[0][0], view.M[1][0], view.M[2][0],
            view.M[3][0], view.M[0][1], view.M[1][1], view.M[2][1],
            view.M[3][1], view.M[0][2], view.M[1][2], view.M[2][2],
            view.M[3][2], view.M[0][3], view.M[1][3], view.M[2][3],
            view.M[3][3]);

    SetMVPMatrix(mvp_matrix);

    glm::quat headRotation = PredictionTrait::getPrediction(this, (1 == eye ? 4.0f : 3.5f) / 60.0f);
    cameraRig->setRotation(headRotation);

    JNIEnv* jni = app->GetVrJni();
    jni->CallVoidMethod(javaObject, drawEyeViewMethodId, eye, fovDegrees);

    if (eye == 1) {
        jni->CallVoidMethod(javaObject, afterDrawEyesMethodId);
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

template <class PredictionTrait> OVR::Matrix4f GVRActivity<PredictionTrait>::Frame( const OVR::VrFrame & vrFrame )
{
    JNIEnv* jni = app->GetVrJni();
    jni->CallVoidMethod(javaObject, beforeDrawEyesMethodId);
    jni->CallVoidMethod(javaObject, drawFrameMethodId);

    deviceIsDocked = vrFrame.DeviceStatus.DeviceIsDocked;

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

template <class PredictionTrait> bool GVRActivity<PredictionTrait>::OnKeyEvent(const int keyCode, const int repeatCode,
        const OVR::KeyEventType eventType) {

    bool handled = app->GetVrJni()->CallBooleanMethod(javaObject,
            onKeyEventNativeMethodId, keyCode, (int)eventType);

    // if not handled back key long press, show global menu
    if (handled == false && keyCode == 4 && eventType == OVR::KEY_EVENT_LONG_PRESS) {
        app->StartSystemActivity(PUI_GLOBAL_MENU);
    }

    return handled;
}

}
