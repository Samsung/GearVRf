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

#include "renderer.h"
#include "objects/scene.h"
#include "shaders/shader_manager.h"
#include "objects/components/render_target.h"
#include "util/gvr_jni.h"

namespace gvr
{

extern "C"
{
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeRenderer_ctor(JNIEnv* env, jobject obj);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderer_makeShadowMaps(JNIEnv* env, jclass clazz,
                                                   jlong jrenderer,
                                                   jlong jscene,
                                                   jlong jshader_manager);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_GVRRenderer_cullAndRender(JNIEnv* env, jclass clazz,
                                                    jlong renderer,
                                                    jlong jrenderTarget,
                                                    jlong jscene,
                                                    jlong jshader_manager,
                                                    jlong jrender_texture_a,
                                                    jlong jrender_texture_b);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderer_useStencil(JNIEnv* env, jclass clazz,
                                               jlong jrenderer, jint useStencil);

};


JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderer_ctor(JNIEnv* env, jobject obj)
{
    return reinterpret_cast<jlong>(Renderer::createRenderer());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderer_makeShadowMaps(JNIEnv* env, jclass clazz,
                                               jlong jrenderer,
                                               jlong jscene,
                                               jlong jshader_manager)
{
    Scene *scene = reinterpret_cast<Scene*>(jscene);
    Renderer* renderer = reinterpret_cast<Renderer*>(jrenderer);
    ShaderManager *shader_manager = reinterpret_cast<ShaderManager*>(jshader_manager);
    renderer->makeShadowMaps(scene, shader_manager);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderer_useStencil(JNIEnv* env, jclass clazz,
        jlong jrenderer, jint useStencil)
{
    Renderer* renderer = reinterpret_cast<Renderer*>(jrenderer);
    bool flag = useStencil != 0;
    renderer->setUseStencilBuffer(flag);
}


JNIEXPORT void JNICALL
Java_org_gearvrf_GVRRenderer_cullAndRender(JNIEnv* env, jclass clazz,
                                           jlong jrenderer,
                                           jlong jrenderTarget,
                                           jlong jscene,
                                           jlong jshader_manager,
                                           jlong jrender_texture_a,
                                           jlong jrender_texture_b)
{
    Scene *scene = reinterpret_cast<Scene *>(jscene);
    Renderer* renderer = reinterpret_cast<Renderer*>(jrenderer);
    RenderTarget* renderTarget = reinterpret_cast<RenderTarget *>(jrenderTarget);
    ShaderManager* shader_manager =
            reinterpret_cast<ShaderManager *>(jshader_manager);
    RenderTexture* render_texture_a =
            reinterpret_cast<RenderTexture *>(jrender_texture_a);
    RenderTexture* render_texture_b =
            reinterpret_cast<RenderTexture *>(jrender_texture_b);

    renderTarget->cullFromCamera(scene, renderTarget->getCamera(), renderer, shader_manager);
    renderTarget->beginRendering(renderer);
    renderer->renderRenderTarget(scene, renderTarget, shader_manager, render_texture_a, render_texture_b);
    renderTarget->endRendering(renderer);
}

}


