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

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeSceneObject_attachComponent(JNIEnv * env,
            jobject obj, jlong jscene_object, jlong jcomponent);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeSceneObject_detachComponent(JNIEnv * env,
            jobject obj, jlong jscene_object, jlong type);

    JNIEXPORT long JNICALL
    Java_org_gearvrf_NativeSceneObject_findComponent(JNIEnv * env,
            jobject obj, jlong jscene_object, jlong type);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeSceneObject_addChildObject(JNIEnv * env,
            jobject obj, jlong jscene_object, jlong jchild);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeSceneObject_removeChildObject(
            JNIEnv * env, jobject obj, jlong jscene_object, jlong jchild);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeSceneObject_isColliding(
            JNIEnv * env, jobject obj, jlong jscene_object, jlong jother_object);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeSceneObject_isEnabled(
            JNIEnv * env, jobject obj, jlong jscene_object);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeSceneObject_setEnable(
            JNIEnv * env, jobject obj, jlong jscene_object, bool flag);

    JNIEXPORT bool JNICALL
    Java_org_gearvrf_NativeSceneObject_intersectsBoundingVolume(JNIEnv * env,
            jobject obj, jlong jscene_object, jfloat rox, jfloat roy, jfloat roz,
            jfloat rdx, jfloat rdy, jfloat rdz);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeSceneObject_setLODRange(
            JNIEnv * env, jobject obj, jlong jscene_object, jfloat min_range, jfloat max_range);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_NativeSceneObject_getLODMinRange(
            JNIEnv * env, jobject obj, jlong jscene_object);

    JNIEXPORT jfloat JNICALL
    Java_org_gearvrf_NativeSceneObject_getLODMaxRange(
            JNIEnv * env, jobject obj, jlong jscene_object);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeSceneObject_getBoundingVolume(JNIEnv * env,
            jobject obj, jlong jSceneObject);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeSceneObject_expandBoundingVolumeByPoint(JNIEnv * env,
            jobject obj, jlong jSceneObject, jfloat pointX, jfloat pointY, jfloat pointZ);

    JNIEXPORT jfloatArray JNICALL
    Java_org_gearvrf_NativeSceneObject_expandBoundingVolumeByCenterAndRadius(JNIEnv * env,
            jobject obj, jlong jSceneObject, jfloat centerX, jfloat centerY, jfloat centerZ, jfloat radius);
} // extern "C"

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeSceneObject_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new SceneObject());
}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeSceneObject_getName(JNIEnv * env,
        jobject obj, jlong jscene_object) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    std::string name = scene_object->name();
    jstring jname = env->NewStringUTF(name.c_str());
    return jname;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_setName(JNIEnv * env,
        jobject obj, jlong jscene_object, jstring name) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    const char* native_name = env->GetStringUTFChars(name, 0);
    scene_object->set_name(std::string(native_name));
    env->ReleaseStringUTFChars(name, native_name);
}


JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeSceneObject_attachComponent(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jcomponent) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    Component* component = reinterpret_cast<Component*>(jcomponent);
    return scene_object->attachComponent(component);
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeSceneObject_detachComponent(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong type) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    return scene_object->detachComponent(type) != NULL;
}


JNIEXPORT long JNICALL
Java_org_gearvrf_NativeSceneObject_findComponent(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong type) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    Component* component = scene_object->getComponent(type);
    return (long) component;
}


JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_addChildObject(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jchild) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    SceneObject* child = reinterpret_cast<SceneObject*>(jchild);
    scene_object->addChildObject(scene_object, child);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_removeChildObject(
        JNIEnv * env, jobject obj, jlong jscene_object, jlong jchild) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    SceneObject* child = reinterpret_cast<SceneObject*>(jchild);
    scene_object->removeChildObject(child);
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeSceneObject_isColliding(
        JNIEnv * env, jobject obj, jlong jscene_object, jlong jother_object) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    SceneObject* other_object = reinterpret_cast<SceneObject*>(jother_object);
    return scene_object->isColliding(other_object);
}


JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeSceneObject_isEnabled(
        JNIEnv * env, jobject obj, jlong jscene_object) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    return scene_object->enabled();
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeSceneObject_setEnable(
        JNIEnv * env, jobject obj, jlong jscene_object, bool flag) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    scene_object->set_enable(flag);
}

JNIEXPORT bool JNICALL
Java_org_gearvrf_NativeSceneObject_intersectsBoundingVolume(JNIEnv * env,
        jobject obj, jlong jscene_object, jfloat rox, jfloat roy, jfloat roz,
        jfloat rdx, jfloat rdy, jfloat rdz) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    return scene_object->intersectsBoundingVolume(rox, roy, roz, rdx, rdy, rdz);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeSceneObject_setLODRange(
        JNIEnv * env, jobject obj, jlong jscene_object, jfloat min_range, jfloat max_range) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    scene_object->setLODRange(min_range, max_range);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeSceneObject_getLODMinRange(
        JNIEnv * env, jobject obj, jlong jscene_object) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    return scene_object->getLODMinRange();
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeSceneObject_getLODMaxRange(
        JNIEnv * env, jobject obj, jlong jscene_object) {
    SceneObject* scene_object = reinterpret_cast<SceneObject*>(jscene_object);
    return scene_object->getLODMaxRange();
}

jfloatArray boundingVolumeToArray(JNIEnv* env, const BoundingVolume& bvol) {
    jfloat temp[10];
    temp[0] = bvol.center().x;
    temp[1] = bvol.center().y;
    temp[2] = bvol.center().z;
    temp[3] = bvol.radius();
    temp[4] = bvol.min_corner().x;
    temp[5] = bvol.min_corner().y;
    temp[6] = bvol.min_corner().z;
    temp[7] = bvol.max_corner().x;
    temp[8] = bvol.max_corner().y;
    temp[9] = bvol.max_corner().z;

    jfloatArray result = env->NewFloatArray(10);
    env->SetFloatArrayRegion(result, 0, 10, temp);
    return result;
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeSceneObject_getBoundingVolume(JNIEnv * env,
        jobject obj, jlong jSceneObject) {
    SceneObject* sceneObject = reinterpret_cast<SceneObject*>(jSceneObject);
    const BoundingVolume& bvol = sceneObject->getBoundingVolume();
    return boundingVolumeToArray(env, bvol);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeSceneObject_expandBoundingVolumeByPoint(JNIEnv * env,
        jobject obj, jlong jSceneObject, jfloat pointX, jfloat pointY, jfloat pointZ) {

    SceneObject* sceneObject = reinterpret_cast<SceneObject*>(jSceneObject);
    BoundingVolume& bvol = sceneObject->getBoundingVolume();
    bvol.expand(glm::vec3(pointX, pointY, pointZ));

    return boundingVolumeToArray(env, bvol);
}

JNIEXPORT jfloatArray JNICALL
Java_org_gearvrf_NativeSceneObject_expandBoundingVolumeByCenterAndRadius(JNIEnv * env,
        jobject obj, jlong jSceneObject, jfloat centerX, jfloat centerY, jfloat centerZ, jfloat radius) {

    SceneObject* sceneObject = reinterpret_cast<SceneObject*>(jSceneObject);
    BoundingVolume& bvol = sceneObject->getBoundingVolume();
    bvol.expand(glm::vec3(centerX, centerY, centerZ), radius);

    return boundingVolumeToArray(env, bvol);
}

} // namespace gvr
