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

/***************************************************************************
 * JNI
 ***************************************************************************/

#include "scene.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeScene_ctor(JNIEnv * env, jobject obj);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_addSceneObject(JNIEnv * env,
        jobject obj, jlong jscene, jlong jscene_object);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_removeSceneObject(JNIEnv * env,
        jobject obj, jlong jscene, jlong jscene_object);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setMainCameraRig(JNIEnv * env,
        jobject obj, jlong jscene, jlong jcamera_rig);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setFrustumCulling(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setOcclusionQuery(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_resetStats(JNIEnv * env,
        jobject obj, jlong jscene);

JNIEXPORT int JNICALL
Java_org_gearvrf_NativeScene_getNumberDrawCalls(JNIEnv * env,
        jobject obj, jlong jscene);

JNIEXPORT int JNICALL
Java_org_gearvrf_NativeScene_getNumberTriangles(JNIEnv * env,
        jobject obj, jlong jscene);

JNIEXPORT float JNICALL
Java_org_gearvrf_NativeScene_getDrawTime(JNIEnv * env,
        jobject obj, jlong jscene);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setStatsEnabled(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeScene_ctor(JNIEnv * env, jobject obj) {
    return reinterpret_cast<jlong>(new Scene());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_addSceneObject(JNIEnv * env,
        jobject obj, jlong jscene, jlong jscene_object) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    scene->addSceneObject(scene_object);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_removeSceneObject(JNIEnv * env,
        jobject obj, jlong jscene, jlong jscene_object) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    scene->removeSceneObject(scene_object);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setMainCameraRig(JNIEnv * env,
        jobject obj, jlong jscene, jlong jcamera_rig) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    scene->set_main_camera_rig(camera_rig);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setFrustumCulling(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    scene->set_frustum_culling(static_cast<bool>(flag));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setOcclusionQuery(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    scene->set_occlusion_culling(static_cast<bool>(flag));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_resetStats(JNIEnv * env,
        jobject obj, jlong jscene) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    scene->resetStats();
}


JNIEXPORT int JNICALL
Java_org_gearvrf_NativeScene_getNumberDrawCalls(JNIEnv * env,
        jobject obj, jlong jscene) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    return scene->getNumberDrawCalls();
}


JNIEXPORT int JNICALL
Java_org_gearvrf_NativeScene_getNumberTriangles(JNIEnv * env,
        jobject obj, jlong jscene) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    return scene->getNumberTriangles();
}

JNIEXPORT float JNICALL
Java_org_gearvrf_NativeScene_getDrawTime(JNIEnv * env,
        jobject obj, jlong jscene) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    return scene->getDrawTime();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setStatsEnabled(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    scene->set_stats_enabled(static_cast<bool>(flag));
}


}
