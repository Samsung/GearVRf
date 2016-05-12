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

#include "base_texture.h"
#include "util/gvr_jni.h"
#include "util/gvr_java_stack_trace.h"
#include "android/asset_manager_jni.h"


namespace gvr {

extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeBaseTexture_bareConstructor(JNIEnv * env, jobject obj, jintArray jtexture_parameters);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeBaseTexture_setJavaOwner(JNIEnv * env, jobject obj, jlong jtexture, jobject owner);

    JNIEXPORT jboolean JNICALL
    Java_org_gearvrf_NativeBaseTexture_update(JNIEnv * env, jobject obj,
            jlong jtexture, jint width, jint height, jbyteArray jdata);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeBaseTexture_bareConstructor(JNIEnv * env, jobject obj, jintArray jtexture_parameters) {
    jint* texture_parameters = env->GetIntArrayElements(jtexture_parameters,0);
    jlong result =  reinterpret_cast<jlong>(new BaseTexture(texture_parameters));
    env->ReleaseIntArrayElements(jtexture_parameters, texture_parameters, 0);
    return result;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeBaseTexture_setJavaOwner(JNIEnv * env, jobject obj, jlong jtexture, jobject owner) {
    BaseTexture* texture = reinterpret_cast<BaseTexture*>(jtexture);
    texture->setJavaOwner(*env, owner);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeBaseTexture_update(JNIEnv * env, jobject obj,
        jlong jtexture, jint width, jint height, jbyteArray jdata) {
    BaseTexture* texture = reinterpret_cast<BaseTexture*>(jtexture);
    jbyte* data = env->GetByteArrayElements(jdata, 0);
    jboolean result = texture->update(width, height, data);
    env->ReleaseByteArrayElements(jdata, data, 0);
    return result;
}

}
