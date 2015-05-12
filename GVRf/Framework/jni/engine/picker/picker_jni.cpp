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

#include "picker.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlongArray JNICALL
Java_org_gearvrf_NativePicker_pickScene(JNIEnv * env,
        jobject obj, jlong jscene, jfloat ox, jfloat oy, jfloat z, jfloat dx,
        jfloat dy, jfloat dz);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePicker_pickSceneObject(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jcamera_rig);
}

JNIEXPORT jlongArray JNICALL
Java_org_gearvrf_NativePicker_pickScene(JNIEnv * env,
        jobject obj, jlong jscene, jfloat ox, jfloat oy, jfloat oz, jfloat dx,
        jfloat dy, jfloat dz) {
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    std::vector<EyePointeeHolder*> eye_pointee_holders =
            Picker::pickScene(scene, ox, oy, oz, dx, dy, dz);
    std::vector<jlong> long_eye_pointee_holders;
    for (auto it = eye_pointee_holders.begin(); it != eye_pointee_holders.end();
            ++it) {
        jlong eye_pointee_holder = reinterpret_cast<jlong>(*it);
        long_eye_pointee_holders.push_back(eye_pointee_holder);
    }
    jlongArray jeye_pointee_holders = env->NewLongArray(
            long_eye_pointee_holders.size());
    env->SetLongArrayRegion(jeye_pointee_holders, 0,
            long_eye_pointee_holders.size(),
            reinterpret_cast<jlong*>(long_eye_pointee_holders.data()));
    return jeye_pointee_holders;
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativePicker_pickSceneObject(JNIEnv * env,
        jobject obj, jlong jscene_object, jlong jcamera_rig) {
    SceneObject* scene_object =
            reinterpret_cast<SceneObject*>(jscene_object);
    CameraRig* camera_rig = reinterpret_cast<CameraRig*>(jcamera_rig);
    return Picker::pickSceneObject(scene_object, camera_rig);
}

}
