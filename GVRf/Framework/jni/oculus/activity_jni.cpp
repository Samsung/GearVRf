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

static const char * activityClassName = "org/gearvrf/GVRActivity";

namespace gvr {

extern "C"
{

long Java_org_gearvrf_GVRActivity_nativeSetAppInterface(
        JNIEnv * jni, jclass clazz, jobject activity,
        jstring fromPackageName, jstring commandString,
        jstring uriString)
{
    LOG("GVRActivity::nativeSetupAppInterface ");
    return (new GVRActivity(*jni,activity))->SetActivity( jni, clazz, activity, fromPackageName, commandString, uriString );
}

void Java_org_gearvrf_GVRActivity_nativeSetCamera(
        JNIEnv * jni, jclass clazz, jlong appPtr, jlong jcamera )
{
    GVRActivity *activity = (GVRActivity*)((OVR::App *)appPtr)->GetAppInterface();
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    activity->camera = camera;
}

} // extern "C"

//=============================================================================
//                             GVRActivity
//=============================================================================

GVRActivity::GVRActivity(JNIEnv & jni_, jobject activityObject_)
    : forceScreenClear( false )
    , ModelLoaded( false )
    , UiJni(&jni_)
    , viewManager(NULL)
{
    viewManager = new GVRViewManager(jni_,activityObject_);
    javaObject = UiJni->NewGlobalRef( activityObject_ );
    activityClass = GetGlobalClassReference( activityClassName );

    oneTimeInitMethodId     = GetMethodID( "oneTimeInit", "()V" );
    oneTimeShutdownMethodId = GetMethodID( "oneTimeShutDown", "()V" );

    drawFrameMethodId      = GetMethodID( "drawFrame", "()V" );
    beforeDrawEyesMethodId = GetMethodID( "beforeDrawEyes", "()V" );
    drawEyeViewMethodId = GetMethodID( "onDrawEyeView", "(IF)V" );
    afterDrawEyesMethodId = GetMethodID( "afterDrawEyes", "()V" );

    onKeyEventNativeMethodId = GetMethodID("onKeyEventNative", "(II)Z");
}

GVRActivity::~GVRActivity() {
    LOG( "GVRActivity::~GVRActivity()");
    if ( javaObject != 0 )
    {
        UiJni->DeleteGlobalRef( javaObject );
    }
}

jmethodID GVRActivity::GetStaticMethodID( jclass clazz, const char * name, const char * signature )
{
    jmethodID mid = UiJni->GetStaticMethodID( clazz, name, signature );
    if ( !mid )
    {
        FAIL( "couldn't get %s", name );
    }
    return mid;
}

jmethodID GVRActivity::GetMethodID( const char * name, const char * signature )
{
    jmethodID mid = UiJni->GetMethodID( activityClass, name, signature );
    if ( !mid )
    {
        FAIL( "couldn't get %s", name );
    }
    return mid;
}


jclass GVRActivity::GetGlobalClassReference( const char * className ) const
{
    jclass lc = UiJni->FindClass(className);
    if ( lc == 0 )
    {
        FAIL( "FindClass( %s ) failed", className );
    }
    // Turn it into a global ref, so we can safely use it in the VR thread
    jclass gc = (jclass)UiJni->NewGlobalRef( lc );

    UiJni->DeleteLocalRef( lc );

    return gc;
}

void GVRActivity::Configure( OVR::ovrSettings & settings )
{
    LOG( "GVRActivity::Configure");
    // leave it as the oculus defaults for now.
}
void GVRActivity::OneTimeInit( const char * fromPackage, const char * launchIntentJSON, const char * launchIntentURI )
{
    LOG( "GVRActivity::OneTimeInit" );
    app->GetVrJni()->CallVoidMethod( javaObject, oneTimeInitMethodId );

    // Check if we already loaded the model through an intent
    if ( !ModelLoaded )
    {
        InitSceneObject();
    }
}

void GVRActivity::OneTimeShutdown()
{
    LOG( "GVRActivity::OneTimeShutdown" );

    app->GetVrJni()->CallVoidMethod( javaObject, oneTimeShutdownMethodId );

    // Free GL resources
}

void GVRActivity::NewIntent( const char * fromPackageName, const char * command, const char * uri )
{
	InitSceneObject();
}

void GVRActivity::Command( const char * msg )
{
    //LOG( "GVRActivity::Command %s", msg );
}


void GVRActivity::WindowCreated(){
    //LOG( "GVRActivity::WindowCreated");
}

OVR::Matrix4f GVRActivity::GetEyeView( const int eye, const float fovDegrees ) const
{
    const OVR::Matrix4f projectionMatrix = Scene.ProjectionMatrixForEye( eye, fovDegrees );
    const OVR::Matrix4f viewMatrix = Scene.ViewMatrixForEye( eye );
    return ( projectionMatrix * viewMatrix );

}

OVR::Matrix4f GVRActivity::DrawEyeView( const int eye, const float fovDegrees )
{
    const OVR::Matrix4f view = GetEyeView( eye, fovDegrees );

	// Transpose view matrix from oculus to mvp_matrix to rendering correctly with gvrf renderer.
    mvp_matrix = glm::mat4(
            view.M[0][0], view.M[1][0], view.M[2][0], view.M[3][0],
            view.M[0][1], view.M[1][1], view.M[2][1], view.M[3][1],
            view.M[0][2], view.M[1][2], view.M[2][2], view.M[3][2],
            view.M[0][3], view.M[1][3], view.M[2][3], view.M[3][3]);

    SetMVPMatrix(mvp_matrix);

    JNIEnv* jni = app->GetVrJni();
    jni->CallVoidMethod( javaObject, drawEyeViewMethodId, eye, fovDegrees );

    if(eye == 1) {
        jni->CallVoidMethod( javaObject, afterDrawEyesMethodId );
    }

    glm::mat4 view_matrix = camera->getViewMatrix();
    glm::mat4 projection_matrix = camera->getProjectionMatrix(); //gun
    glm::mat4 vp_matrix = glm::mat4(projection_matrix * view_matrix);

    OVR::Matrix4f view2 = OVR::Matrix4f(
    		vp_matrix[0][0], vp_matrix[1][0], vp_matrix[2][0], vp_matrix[3][0],
            vp_matrix[0][1], vp_matrix[1][1], vp_matrix[2][1], vp_matrix[3][1],
            vp_matrix[0][2], vp_matrix[1][2], vp_matrix[2][2], vp_matrix[3][2],
            vp_matrix[0][3], vp_matrix[1][3], vp_matrix[2][3], vp_matrix[3][3]);

    return view2;

}



OVR::Matrix4f GVRActivity::Frame( const OVR::VrFrame & vrFrame )
{
    LOGD("GVRActivity::Frame() was called");

    JNIEnv* jni = app->GetVrJni();
    jni->CallVoidMethod( javaObject, beforeDrawEyesMethodId );
    jni->CallVoidMethod( javaObject, drawFrameMethodId );

	//This is called once while DrawEyeView is called twice, when eye=0 and eye 1.
	//So camera is set in java as one of left and right camera.
	//Centerview camera matrix can be retrieved from its parent, CameraRig
    glm::mat4 vp_matrix = camera->getCenterViewMatrix();

    ovrMatrix4f view2;

    view2.M[0][0] = vp_matrix[0][0]; view2.M[1][0] = vp_matrix[0][1]; view2.M[2][0] = vp_matrix[0][2]; view2.M[3][0] = vp_matrix[0][3];
    view2.M[0][1] = vp_matrix[1][0]; view2.M[1][1] = vp_matrix[1][1]; view2.M[2][1] = vp_matrix[1][2]; view2.M[3][1] = vp_matrix[1][3];
    view2.M[0][2] = vp_matrix[2][0]; view2.M[1][2] = vp_matrix[2][1]; view2.M[2][2] = vp_matrix[2][2]; view2.M[3][2] = vp_matrix[2][3];
    view2.M[0][3] = vp_matrix[3][0]; view2.M[1][3] = vp_matrix[3][1]; view2.M[2][3] = vp_matrix[3][2]; view2.M[3][3] = vp_matrix[3][3];

    return view2;
}

void GVRActivity::InitSceneObject()
{
}

bool GVRActivity::OnKeyEvent(const int keyCode, const int repeatCode,
        const OVR::KeyEventType eventType) {

    // 1: KeyState::KEY_EVENT_DOWN, 0: KeyState::KEY_EVENT_UP. Other information is lost from Oculus side.
    int isDown = (eventType == OVR::KEY_EVENT_DOWN) ? 1 : 0;

    return app->GetVrJni()->CallBooleanMethod(javaObject,
            onKeyEventNativeMethodId, keyCode, isDown);
}

}
