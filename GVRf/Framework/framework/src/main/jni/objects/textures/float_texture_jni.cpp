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

#include "float_texture.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeFloatTexture_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeFloatTexture_update(JNIEnv * env,
        jobject obj, jlong jtexture, jint width, jint height, jfloatArray jdata);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeFloatTexture_ctor(JNIEnv * env,
    jobject obj) {
return reinterpret_cast<jlong>(new FloatTexture());
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeFloatTexture_update(JNIEnv * env,
    jobject obj, jlong jtexture, jint width, jint height, jfloatArray jdata) {
    FloatTexture* texture = reinterpret_cast<FloatTexture*>(jtexture);
    jfloat* data = env->GetFloatArrayElements(jdata, 0);
    jboolean result = texture->update(width, height, data);
    env->ReleaseFloatArrayElements(jdata, data, 0);
    return result;
}

}
