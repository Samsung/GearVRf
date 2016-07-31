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

#include "objects/components/ovr_camera_rig.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
    JNIEXPORT void JNICALL Java_org_gearvrf_OvrNativeCameraRig_predict(
        JNIEnv * env, jobject obj, jlong jcamera_rig, jfloat time)
    {
        OvrCameraRig* camera_rig = reinterpret_cast<OvrCameraRig*>(jcamera_rig);
        camera_rig->predict(time);
    }

    JNIEXPORT jlong JNICALL Java_org_gearvrf_OvrNativeCameraRig_ctor(JNIEnv* env, jobject obj) {
        return reinterpret_cast<jlong>(new OvrCameraRig());
    }
}; // extern "C"


}
