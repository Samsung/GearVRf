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

#include "render_pass.h"

#include "util/gvr_jni.h"

#include "objects/material.h"

namespace gvr {

extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderPass_ctor(JNIEnv * env,
        jobject obj);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderPass_setMaterial(JNIEnv* env,
        jobject obj, jlong jrender_pass, jlong jmaterial);


JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderPass_setCullFace(JNIEnv* env,
        jobject obj, jlong jrender_pass, jint jcull_face);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderPass_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new RenderPass());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderPass_setMaterial(JNIEnv* env,
        jobject obj, jlong jrender_pass, jlong jmaterial) {
    RenderPass* pass = reinterpret_cast<RenderPass*>(jrender_pass);
    Material* material = reinterpret_cast<Material*>(jmaterial);
    pass->set_material(material);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderPass_setCullFace(JNIEnv* env,
        jobject obj, jlong jrender_pass, jint jcull_face) {
    RenderPass* pass = reinterpret_cast<RenderPass*>(jrender_pass);
    pass->set_cull_face(static_cast<int>(jcull_face));
}

}
