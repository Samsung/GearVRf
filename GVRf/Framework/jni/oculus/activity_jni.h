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
#include "view_manager.h"
#include "../objects/components/camera.h"

namespace gvr {

class GVRActivity : public OVR::VrAppInterface
{
public:
                        GVRActivity( JNIEnv & jni_, jobject activityObject_);
                        ~GVRActivity();

    virtual void         ConfigureVrMode( ovrModeParms & modeParms );
    virtual void        OneTimeInit( const char * launchIntent );
    virtual void        OneTimeShutdown();
    virtual Matrix4f    DrawEyeView( const int eye, const float fovDegrees );
    virtual Matrix4f    Frame( VrFrame vrFrame );
    virtual    void     NewIntent( const char * intent );
    virtual void        Command( const char * msg );
    virtual void        WindowCreated();
    void                InitSceneObject( );

    // When launched by an intent, we may be viewing a partial
    // scene for debugging, so always clear the screen to grey
    // before drawing, instead of letting partial renders show through.
    bool                forceScreenClear;
    bool                ModelLoaded;

    OvrSceneView        Scene;

    GVRViewManager  *viewManager;


    std::shared_ptr<Camera> camera;
private:
    glm::mat4            mvp_matrix;
    void                 SetMVPMatrix(glm::mat4 mvp){
        viewManager->mvp_matrix = mvp;
    }

    JNIEnv *            UiJni;            // for use by the Java UI thread
    Matrix4f            GetEyeView( const int eye, const float fovDegrees ) const;

    jobject              javaObject;
    jclass               activityClass;    // must be looked up from main thread or FindClass() will fail

    jmethodID            oneTimeInitMethodId;
    jmethodID            oneTimeShutdownMethodId;

    jmethodID            drawFrameMethodId;

    jmethodID            beforeDrawEyesMethodId;
    jmethodID            drawEyeViewMethodId;
    jmethodID            afterDrawEyesMethodId;

    jclass               GetGlobalClassReference( const char * className ) const;
    jmethodID            GetMethodID( const char * name, const char * signature );
    jmethodID            GetStaticMethodID( jclass activityClass, const char * name, const char * signature );
};
}
#endif
