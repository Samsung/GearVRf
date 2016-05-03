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
#include "activity.h"

namespace gvr {

extern "C" {

JNIEXPORT long JNICALL Java_org_gearvrf_GVRActivityNative_onCreate(JNIEnv* jni, jclass clazz,
        jobject activity, jobject vrAppSettings, jobject callbacks) {
    GVRActivity* gvrActivity = new GVRActivity(*jni, activity, vrAppSettings, callbacks);
    return reinterpret_cast<long>(gvrActivity);
}

JNIEXPORT void JNICALL Java_org_gearvrf_GVRActivityNative_onDestroy(JNIEnv * jni, jclass clazz, jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    delete activity;
}

JNIEXPORT void JNICALL Java_org_gearvrf_GVRActivityNative_setCamera(JNIEnv * jni, jclass clazz, jlong appPtr,
        jlong jcamera) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    activity->camera = camera;
}

JNIEXPORT void JNICALL Java_org_gearvrf_GVRActivityNative_setCameraRig(JNIEnv * jni, jclass clazz, jlong appPtr,
        jlong cameraRig) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->setCameraRig(cameraRig);
}

JNIEXPORT void JNICALL Java_org_gearvrf_GVRActivityNative_onDock(JNIEnv * jni, jclass clazz, jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->headRotationProvider_.onDock();
}

JNIEXPORT void JNICALL Java_org_gearvrf_GVRActivityNative_onUndock(JNIEnv * jni, jclass clazz, jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->headRotationProvider_.onUndock();
}

// -------------------- //
// VrapiActivityHandler //
// -------------------- //

JNIEXPORT void JNICALL Java_org_gearvrf_VrapiActivityHandler_nativeLeaveVrMode(JNIEnv * jni, jclass clazz,
        jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->leaveVrMode();
}

JNIEXPORT void JNICALL Java_org_gearvrf_VrapiActivityHandler_nativeOnSurfaceCreated(JNIEnv * jni, jclass clazz,
        jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->onSurfaceCreated(*jni);
}

JNIEXPORT void JNICALL Java_org_gearvrf_VrapiActivityHandler_nativeOnSurfaceChanged(JNIEnv * jni, jclass clazz,
        jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->onSurfaceChanged(*jni);
}

JNIEXPORT void JNICALL Java_org_gearvrf_VrapiActivityHandler_nativeOnDrawFrame(JNIEnv * jni, jclass clazz,
        jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->onDrawFrame();
}

JNIEXPORT void JNICALL Java_org_gearvrf_VrapiActivityHandler_nativeShowGlobalMenu(JNIEnv * jni, jclass clazz, jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->showGlobalMenu();
}

JNIEXPORT void JNICALL Java_org_gearvrf_VrapiActivityHandler_nativeShowConfirmQuit(JNIEnv * jni, jclass clazz, jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->showConfirmQuit();
}

JNIEXPORT jint JNICALL Java_org_gearvrf_VrapiActivityHandler_nativeInitializeVrApi(JNIEnv * jni, jclass clazz, jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    return activity->initializeVrApi();
}

JNIEXPORT void JNICALL Java_org_gearvrf_VrapiActivityHandler_nativeUninitializeVrApi(JNIEnv * jni, jclass clazz, jlong appPtr) {
    GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    activity->uninitializeVrApi();
}

JNIEXPORT jboolean JNICALL Java_org_gearvrf_GVRConfigurationManager_nativeIsHmtConnected(JNIEnv* jni, jclass clazz, jlong appPtr) {
    const GVRActivity *activity = reinterpret_cast<GVRActivity*>(appPtr);
    return activity->isHmtConnected();
}

} //extern "C" {

} //namespace gvr
