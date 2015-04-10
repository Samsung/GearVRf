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
#include <VrApi/VrApi_Helpers.h>

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
    GVRActivity *activity = (GVRActivity*)((App *)appPtr)->GetAppInterface();
    //LOG("GVRActivity::setCamera ");
    std::shared_ptr<Camera> camera =
            *reinterpret_cast<std::shared_ptr<Camera>*>(jcamera);
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

void GVRActivity::ConfigureVrMode( ovrModeParms & modeParms )
{
    LOG( "GVRActivity::ConfigureVrMode");

    modeParms.CpuLevel = 2;
    modeParms.GpuLevel = 2;

    // Always use 2x MSAA for now
    app->GetVrParms().multisamples = 2;

    // Always use bilinear texture filtering.
    app->GetVrParms().textureFilter = TEXTURE_FILTER_BILINEAR;

    app->GetSwapParms().WarpProgram = WP_SIMPLE;
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

Matrix4f GVRActivity::GetEyeView( const int eye, const float fovDegrees ) const
{
    const Matrix4f projectionMatrix = Scene.ProjectionMatrixForEye( eye, fovDegrees );
    const Matrix4f viewMatrix = Scene.ViewMatrixForEye( eye );
    return ( projectionMatrix * viewMatrix );

}

Matrix4f GVRActivity::DrawEyeView( const int eye, const float fovDegrees )
{
    const Matrix4f view = GetEyeView( eye, fovDegrees );

	// Transpose view matrix from oculus to mvp_matrix to rendering correctly with gvrf renderer.
    mvp_matrix = glm::mat4(
            view.M[0][0], view.M[1][0], view.M[2][0], view.M[3][0],
            view.M[0][1], view.M[1][1], view.M[2][1], view.M[3][1],
            view.M[0][2], view.M[1][2], view.M[2][2], view.M[3][2],
            view.M[0][3], view.M[1][3], view.M[2][3], view.M[3][3]);

    SetMVPMatrix(mvp_matrix);

    JNIEnv* jni = app->GetVrJni();
    jni->CallVoidMethod( javaObject, drawEyeViewMethodId, eye, fovDegrees );


    glm::mat4 view_matrix = camera->getViewMatrix();
    glm::mat4 projection_matrix = camera->getProjectionMatrix(); //gun
    glm::mat4 vp_matrix = glm::mat4(projection_matrix * view_matrix);

    Matrix4f view2 = Matrix4f(
    		vp_matrix[0][0], vp_matrix[1][0], vp_matrix[2][0], vp_matrix[3][0],
            vp_matrix[0][1], vp_matrix[1][1], vp_matrix[2][1], vp_matrix[3][1],
            vp_matrix[0][2], vp_matrix[1][2], vp_matrix[2][2], vp_matrix[3][2],
            vp_matrix[0][3], vp_matrix[1][3], vp_matrix[2][3], vp_matrix[3][3]);

    return view2;

}



Matrix4f GVRActivity::Frame( const VrFrame vrFrame )
{
    JNIEnv* jni = app->GetVrJni();
    jni->CallVoidMethod( javaObject, beforeDrawEyesMethodId );
    jni->CallVoidMethod( javaObject, drawFrameMethodId );

    // Get the current vrParms for the buffer resolution.
    const EyeParms vrParms = app->GetEyeParms();
    // Player movement. use dummy Scene object to update data
	Scene.UpdateViewMatrix( vrFrame );

	//This is called once while DrawEyeView is called twice, when eye=0 and eye 1.
	//So camera is set in java as one of left and right camera.
	//Centerview camera matrix can be retrieved from its parent, CameraRig
    glm::mat4 vp_matrix = camera->getCenterViewMatrix();

    ovrMatrix4f view2;
    view2.M[0][0] = vp_matrix[0][0]; view2.M[1][0] = vp_matrix[1][0]; view2.M[2][0] = vp_matrix[2][0]; view2.M[3][0] = vp_matrix[3][0];
    view2.M[0][1] = vp_matrix[0][1]; view2.M[1][1] = vp_matrix[1][1]; view2.M[2][1] = vp_matrix[2][1]; view2.M[3][1] = vp_matrix[3][1];
    view2.M[0][2] = vp_matrix[0][2]; view2.M[1][2] = vp_matrix[1][2]; view2.M[2][2] = vp_matrix[2][2]; view2.M[3][2] = vp_matrix[3][2];
    view2.M[0][3] = vp_matrix[0][3]; view2.M[1][3] = vp_matrix[1][3]; view2.M[2][3] = vp_matrix[2][3]; view2.M[3][3] = vp_matrix[3][3];


	// Set the external velocity matrix so TimeWarp can smoothly rotate the
	// view even if we are dropping frames.
	app->GetSwapParms().ExternalVelocity = CalculateExternalVelocity( &view2, Scene.YawVelocity );

    //-------------------------------------------
    // Render the two eye views, each to a separate texture, and TimeWarp
    // to the screen.
    //-------------------------------------------
    app->DrawEyeViewsPostDistorted( view2);

    jni->CallVoidMethod( javaObject, afterDrawEyesMethodId );

    return view2;
}

void GVRActivity::InitSceneObject()
{
    Scene.YawOffset = -M_PI / 2;
    Scene.Znear = 0.01f;
    Scene.Zfar = 2000.0f;
    // Set the initial player position
    Scene.FootPos = Vector3f( 0.0f, 0.0f, 0.0f );
    Scene.YawOffset = 0;
    Scene.LastHeadModelOffset = Vector3f( 0.0f, 0.0f, 0.0f );
}


}
