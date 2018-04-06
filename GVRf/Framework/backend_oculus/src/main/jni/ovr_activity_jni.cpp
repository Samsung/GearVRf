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

#include <jni.h>
#include <engine/renderer/renderer.h>
#include <objects/textures/render_texture.h>
#include "ovr_activity.h"
namespace gvr {
    extern "C" {

    JNIEXPORT long JNICALL Java_org_gearvrf_OvrActivityNative_onCreate(JNIEnv* jni, jclass clazz,
                                                                       jobject activity, jobject vrAppSettings, jobject callbacks) {
        GVRActivity* gvrActivity = new GVRActivity(*jni, activity, vrAppSettings, callbacks);
        return reinterpret_cast<long>(gvrActivity);
    }

    JNIEXPORT long JNICALL Java_org_gearvrf_OvrViewManager_getRenderTextureInfo(JNIEnv* jni, jclass clazz, jlong jactivity ,  jint index, jint eye){
        GVRActivity* gvrActivity = reinterpret_cast<GVRActivity*>(jactivity);
        return reinterpret_cast<long>(gvrActivity->getRenderTextureInfo(eye, index));
    }

    JNIEXPORT void JNICALL Java_org_gearvrf_OvrActivityNative_onDestroy(JNIEnv * jni, jclass clazz, jlong appPtr) {
        GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        delete activity;
    }

    JNIEXPORT void JNICALL Java_org_gearvrf_OvrActivityNative_setCameraRig(JNIEnv * jni, jclass clazz, jlong appPtr,
                                                                           jlong cameraRig) {
        GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        activity->setCameraRig(cameraRig);
    }

// -------------------- //
// VrapiActivityHandler //
// -------------------- //

    JNIEXPORT void JNICALL Java_org_gearvrf_OvrVrapiActivityHandler_nativeLeaveVrMode(JNIEnv * jni, jclass clazz,
                                                                                      jlong appPtr) {
        GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        activity->leaveVrMode();
    }

    JNIEXPORT void JNICALL Java_org_gearvrf_OvrVrapiActivityHandler_nativeOnSurfaceCreated(JNIEnv * jni, jclass clazz,
                                                                                           jlong appPtr) {
        GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        activity->onSurfaceCreated(*jni);
    }

    JNIEXPORT void JNICALL Java_org_gearvrf_OvrVrapiActivityHandler_nativeOnSurfaceChanged(JNIEnv * jni, jclass clazz,
                                                                                           jlong appPtr) {
        GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        activity->onSurfaceChanged(*jni);
    }

    JNIEXPORT void JNICALL Java_org_gearvrf_OvrViewManager_drawEyes(JNIEnv * jni, jobject jViewManager,
                                                                    jlong appPtr) {
        GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        activity->onDrawFrame(jViewManager);
    }
    
    JNIEXPORT void JNICALL Java_org_gearvrf_OvrVrapiActivityHandler_nativeShowConfirmQuit(JNIEnv * jni, jclass clazz, jlong appPtr) {
        GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        activity->showConfirmQuit();
    }
    
    JNIEXPORT jint JNICALL Java_org_gearvrf_OvrVrapiActivityHandler_nativeInitializeVrApi(JNIEnv * jni, jclass clazz, jlong appPtr) {
        GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        return activity->initializeVrApi();
    }
    
    JNIEXPORT void JNICALL Java_org_gearvrf_OvrVrapiActivityHandler_nativeUninitializeVrApi(JNIEnv *, jclass) {
        GVRActivity::uninitializeVrApi();
    }
    
    JNIEXPORT jboolean JNICALL Java_org_gearvrf_OvrConfigurationManager_nativeIsHmtConnected(JNIEnv* jni, jclass clazz, jlong appPtr) {
        const GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        return activity->isHmtConnected();
    }
    
    JNIEXPORT jboolean JNICALL Java_org_gearvrf_GVRConfigurationManager_nativeUsingMultiview(JNIEnv* jni, jclass clazz, jlong appPtr) {
        const GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
        return activity->usingMultiview();
    }
    
    } //extern "C" {
    
} //namespace gvr
