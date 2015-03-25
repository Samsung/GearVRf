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

#include "scene_object.h"

#include "util/gvr_log.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeSceneObject_getName(JNIEnv * env,
        jobject obj, jlong jscene_object);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_setName(JNIEnv * env,
        jobject obj, jlong jscene_object, jstring name);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachTransform(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jtransform);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachTransform(JNIEnv * env,
        jobject obj, jlong jscene_object);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getTransform(JNIEnv * env,
        jobject obj, jlong jscene_object);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachRenderData(
        JNIEnv * env, jobject obj, jlong jscene_object, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachRenderData(
        JNIEnv * env, jobject obj, jlong jscene_object);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getRenderData(JNIEnv * env,
        jobject obj, jlong jscene_object);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachCamera(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jcamera);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachCamera(JNIEnv * env,
        jobject obj, jlong jscene_object);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getCamera(JNIEnv * env,
        jobject obj, jlong jscene_object);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachCameraRig(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jcamera_rig);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachCameraRig(JNIEnv * env,
        jobject obj, jlong jscene_object);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getCameraRig(JNIEnv * env,
        jobject obj, jlong jscene_object);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachEyePointeeHolder(
        JNIEnv * env, jobject obj, jlong jscene_object,
        jlong jeye_pointee_holder);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachEyePointeeHolder(
        JNIEnv * env, jobject obj, jlong jscene_object);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getEyePointeeHolder(
        JNIEnv * env, jobject obj, jlong jscene_object);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getParent(JNIEnv * env,
        jobject obj, jlong jscene_object);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_addChildObject(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jchild);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_removeChildObject(
        JNIEnv * env, jobject obj, jlong jscene_object, jlong jchild);
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeSceneObject_getChildrenCount(
        JNIEnv * env, jobject obj, jlong jscene_object);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getChildByIndex(JNIEnv * env,
        jobject obj, jlong jscene_object, jint index);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new std::shared_ptr<SceneObject>(
            new SceneObject()));
}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeSceneObject_getName(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::string name = scene_object->name();
    jstring jname = env->NewStringUTF(name.c_str());
    return jname;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_setName(JNIEnv * env,
        jobject obj, jlong jscene_object, jstring name) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    const char* native_name = env->GetStringUTFChars(name, 0);
    scene_object->set_name(std::string(native_name));
    env->ReleaseStringUTFChars(name, native_name);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachTransform(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jtransform) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<Transform> transform = *reinterpret_cast<std::shared_ptr<
            Transform>*>(jtransform);
    scene_object->attachTransform(scene_object, transform);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachTransform(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    scene_object->detachTransform();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getTransform(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<Transform> transform = scene_object->transform();
    return reinterpret_cast<jlong>(new std::shared_ptr<Transform>(transform));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachRenderData(
        JNIEnv * env, jobject obj, jlong jscene_object, jlong jrender_data) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    scene_object->attachRenderData(scene_object, render_data);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachRenderData(
        JNIEnv * env, jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    scene_object->detachRenderData();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getRenderData(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<RenderData> render_data = scene_object->render_data();
    return render_data.get() == NULL ?
            0 :
            reinterpret_cast<jlong>(new std::shared_ptr<RenderData>(render_data));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachCamera(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jcamera) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<Camera> camera =
            *reinterpret_cast<std::shared_ptr<Camera>*>(jcamera);
    scene_object->attachCamera(scene_object, camera);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachCamera(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    scene_object->detachCamera();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getCamera(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<Camera> camera = scene_object->camera();
    return camera.get() == NULL ?
            0 : reinterpret_cast<jlong>(new std::shared_ptr<Camera>(camera));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachCameraRig(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jcamera_rig) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<CameraRig> camera_rig = *reinterpret_cast<std::shared_ptr<
            CameraRig>*>(jcamera_rig);
    scene_object->attachCameraRig(scene_object, camera_rig);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachCameraRig(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    scene_object->detachCameraRig();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getCameraRig(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<CameraRig> camera_rig = scene_object->camera_rig();
    return camera_rig.get() == NULL ?
            0 :
            reinterpret_cast<jlong>(new std::shared_ptr<CameraRig>(camera_rig));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_attachEyePointeeHolder(
        JNIEnv * env, jobject obj, jlong jscene_object,
        jlong jeye_pointee_holder) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<EyePointeeHolder> eye_pointee_holder =
            *reinterpret_cast<std::shared_ptr<EyePointeeHolder>*>(jeye_pointee_holder);
    scene_object->attachEyePointeeHolder(scene_object, eye_pointee_holder);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_detachEyePointeeHolder(
        JNIEnv * env, jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    scene_object->detachEyePointeeHolder();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getEyePointeeHolder(
        JNIEnv * env, jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<EyePointeeHolder> eye_pointee_holder =
            scene_object->eye_pointee_holder();
    return eye_pointee_holder.get() == NULL ?
            0 :
            reinterpret_cast<jlong>(new std::shared_ptr<EyePointeeHolder>(
                    eye_pointee_holder));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getParent(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<SceneObject> parent = scene_object->parent();
    return parent.get() == NULL ?
            0 :
            reinterpret_cast<jlong>(new std::shared_ptr<SceneObject>(parent));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_addChildObject(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jchild) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<SceneObject> child = *reinterpret_cast<std::shared_ptr<
            SceneObject>*>(jchild);
    scene_object->addChildObject(scene_object, child);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_removeChildObject(
        JNIEnv * env, jobject obj, jlong jscene_object, jlong jchild) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    std::shared_ptr<SceneObject> child = *reinterpret_cast<std::shared_ptr<
            SceneObject>*>(jchild);
    scene_object->removeChildObject(child);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeSceneObject_getChildrenCount(
        JNIEnv * env, jobject obj, jlong jscene_object) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    return scene_object->getChildrenCount();
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_getChildByIndex(JNIEnv * env,
        jobject obj, jlong jscene_object, jint index) {
    std::shared_ptr<SceneObject> scene_object =
            *reinterpret_cast<std::shared_ptr<SceneObject>*>(jscene_object);
    try {
        std::shared_ptr<SceneObject> child = scene_object->getChildByIndex(
                index);
        return reinterpret_cast<jlong>(new std::shared_ptr<SceneObject>(child));
    } catch (std::string e) {
        LOGE("%s", e.c_str());
        return 0;
    }
}

}
