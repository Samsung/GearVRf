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
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeScene_getMainCameraRig(JNIEnv * env,
        jobject obj, jlong jscene);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setMainCameraRig(JNIEnv * env,
        jobject obj, jlong jscene, jlong jcamera_rig);
JNIEXPORT jlongArray JNICALL
Java_org_gearvrf_NativeScene_getWholeSceneObjects(JNIEnv * env,
        jobject obj, jlong jscene);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setFrustumCulling(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag);
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setOcclusionQuery(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeScene_ctor(JNIEnv * env, jobject obj) {
    return reinterpret_cast<jlong>(new std::shared_ptr<Scene>(new Scene()));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_addSceneObject(JNIEnv * env,
        jobject obj, jlong jscene, jlong jscene_object) {
    std::shared_ptr<Scene> scene =
            *reinterpret_cast<std::shared_ptr<Scene>*>(jscene);
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    scene->addSceneObject(scene_object);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_removeSceneObject(JNIEnv * env,
        jobject obj, jlong jscene, jlong jscene_object) {
    std::shared_ptr<Scene> scene =
            *reinterpret_cast<std::shared_ptr<Scene>*>(jscene);
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    scene->removeSceneObject(scene_object);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeScene_getMainCameraRig(JNIEnv * env,
        jobject obj, jlong jscene) {
    std::shared_ptr<Scene> scene =
            *reinterpret_cast<std::shared_ptr<Scene>*>(jscene);
    std::shared_ptr<CameraRig> camera_rig = scene->main_camera_rig();
    return reinterpret_cast<jlong>(new std::shared_ptr<CameraRig>(camera_rig));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setMainCameraRig(JNIEnv * env,
        jobject obj, jlong jscene, jlong jcamera_rig) {
    std::shared_ptr<Scene> scene =
            *reinterpret_cast<std::shared_ptr<Scene>*>(jscene);
    std::shared_ptr<CameraRig> camera_rig = *reinterpret_cast<std::shared_ptr<
            CameraRig>*>(jcamera_rig);
    scene->set_main_camera_rig(camera_rig);
}

JNIEXPORT jlongArray JNICALL
Java_org_gearvrf_NativeScene_getWholeSceneObjects(JNIEnv * env,
        jobject obj, jlong jscene) {
    std::shared_ptr<Scene> scene =
            *reinterpret_cast<std::shared_ptr<Scene>*>(jscene);
    std::vector<std::shared_ptr<SceneObject>> scene_objects =
            scene->getWholeSceneObjects();
    std::vector<jlong> long_scene_objects;
    for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
        long_scene_objects.push_back(
                reinterpret_cast<jlong>(new std::shared_ptr<SceneObject>(*it)));
    }
    jlongArray jscene_objects = env->NewLongArray(long_scene_objects.size());
    env->SetLongArrayRegion(jscene_objects, 0, long_scene_objects.size(),
            reinterpret_cast<jlong*>(long_scene_objects.data()));
    return jscene_objects;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setFrustumCulling(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag) {
    std::shared_ptr<Scene> scene =
            *reinterpret_cast<std::shared_ptr<Scene>*>(jscene);
    scene->set_frustum_culling(static_cast<bool>(flag));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeScene_setOcclusionQuery(JNIEnv * env,
        jobject obj, jlong jscene, jboolean flag) {
    std::shared_ptr<Scene> scene =
            *reinterpret_cast<std::shared_ptr<Scene>*>(jscene);
    scene->set_occlusion_culling(static_cast<bool>(flag));
}
}
