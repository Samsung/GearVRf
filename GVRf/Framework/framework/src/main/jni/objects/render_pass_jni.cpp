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

#include <engine/renderer/renderer.h>
#include "util/gvr_jni.h"
#include "util/gvr_log.h"
#include "render_pass.h"
#include "objects/shader_data.h"

namespace gvr {

extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeRenderPass_ctor(JNIEnv * env, jobject obj);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderPass_setMaterial(JNIEnv* env,
            jobject obj, jlong jrender_pass, jlong jmaterial);


    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderPass_setCullFace(JNIEnv* env,
            jobject obj, jlong jrender_pass, jint jcull_face);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderPass_setShader(JNIEnv* env,
            jobject obj, jlong jrender_pass, jint jshaderid, jboolean jUse_multiview);

    JNIEXPORT jint JNICALL
    Java_org_gearvrf_NativeRenderPass_getShader(JNIEnv* env,
            jobject obj, jlong jrender_pass, jboolean useMultiview);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderPass_ctor(JNIEnv * env, jobject obj)
{
    return reinterpret_cast<jlong>(Renderer::getInstance()->createRenderPass());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderPass_setMaterial(JNIEnv* env,
        jobject obj, jlong jrender_pass, jlong jmaterial)
{
    RenderPass* pass = reinterpret_cast<RenderPass*>(jrender_pass);
    ShaderData* material = reinterpret_cast<ShaderData*>(jmaterial);
    pass->set_material(material);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderPass_setCullFace(JNIEnv* env,
        jobject obj, jlong jrender_pass, jint jcull_face)
{
    RenderPass* pass = reinterpret_cast<RenderPass*>(jrender_pass);
    pass->set_cull_face(static_cast<int>(jcull_face));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderPass_setShader(JNIEnv* env,
        jobject obj, jlong jrender_pass, jint jshaderid, jboolean jUse_multiview)
{
    RenderPass* pass = reinterpret_cast<RenderPass*>(jrender_pass);
    LOGD("SHADER: RenderPass: NativeRenderPass_setShader(%p)", pass);
    pass->set_shader(jshaderid, jUse_multiview);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeRenderPass_getShader(JNIEnv* env,
     jobject obj, jlong jrender_pass, jboolean useMultiview)
{
    RenderPass* pass = reinterpret_cast<RenderPass*>(jrender_pass);
    return pass->get_shader(useMultiview);
}
}


