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

#include <jni.h>

#include "engine/renderer/renderer.h"
#include "objects/textures/render_texture.h"
//#include "objects/components/camera.h"

namespace gvr {

class Camera;
class Scene;

extern "C" {
    void Java_org_gearvrf_GVRViewManager_cull(JNIEnv *jni, jclass clazz,
                                              jlong jscene, jlong jcamera, jlong jshader_manager) {
        Scene *scene = reinterpret_cast<Scene *>(jscene);
        Camera *camera = reinterpret_cast<Camera *>(jcamera);
        ShaderManager *shader_manager = reinterpret_cast<ShaderManager *>(jshader_manager);
        gRenderer = Renderer::getInstance();
        gRenderer->cull(scene, camera, shader_manager);
    }


    void Java_org_gearvrf_GVRViewManager_makeShadowMaps(JNIEnv *jni, jclass clazz,
                                                        jlong jscene, jlong jshader_manager,
                                                        jint width, jint height) {
        Scene *scene = reinterpret_cast<Scene *>(jscene);

        ShaderManager *shader_manager = reinterpret_cast<ShaderManager *>(jshader_manager);
        gRenderer = Renderer::getInstance();
        gRenderer->makeShadowMaps(scene, shader_manager);
    }

    void Java_org_gearvrf_GVRViewManager_cullAndRender(JNIEnv *jni, jclass clazz,
                                                      jlong jrenderTarget, jlong jscene,
                                                      jlong jshader_manager,
                                                      jlong jpost_effect_shader_manager,
                                                      jlong jpost_effect_render_texture_a,
                                                      jlong jpost_effect_render_texture_b)
    {
        Scene *scene = reinterpret_cast<Scene *>(jscene);
        RenderTarget *renderTarget = reinterpret_cast<RenderTarget *>(jrenderTarget);
        ShaderManager *shader_manager =
                reinterpret_cast<ShaderManager *>(jshader_manager);
        PostEffectShaderManager *post_effect_shader_manager =
                reinterpret_cast<PostEffectShaderManager *>(jpost_effect_shader_manager);
        RenderTexture *post_effect_render_texture_a =
                reinterpret_cast<RenderTexture *>(jpost_effect_render_texture_a);
        RenderTexture *post_effect_render_texture_b =
                reinterpret_cast<RenderTexture *>(jpost_effect_render_texture_b);

        gRenderer->cullAndRender(renderTarget, scene, shader_manager,
                                post_effect_shader_manager, post_effect_render_texture_a,
                                post_effect_render_texture_b);
    }

    void Java_org_gearvrf_GVRViewManager_renderCamera(JNIEnv *jni, jclass clazz,
                                                      jlong jscene, jlong jcamera,
                                                      jlong jshader_manager,
                                                      jlong jpost_effect_shader_manager,
                                                      jlong jpost_effect_render_texture_a,
                                                      jlong jpost_effect_render_texture_b) {
        Scene *scene = reinterpret_cast<Scene *>(jscene);
        Camera *camera = reinterpret_cast<Camera *>(jcamera);
        ShaderManager *shader_manager =
                reinterpret_cast<ShaderManager *>(jshader_manager);
        PostEffectShaderManager *post_effect_shader_manager =
                reinterpret_cast<PostEffectShaderManager *>(jpost_effect_shader_manager);
        RenderTexture *post_effect_render_texture_a =
                reinterpret_cast<RenderTexture *>(jpost_effect_render_texture_a);
        RenderTexture *post_effect_render_texture_b =
                reinterpret_cast<RenderTexture *>(jpost_effect_render_texture_b);

        gRenderer->renderCamera(scene, camera, shader_manager,
                                post_effect_shader_manager, post_effect_render_texture_a,
                                post_effect_render_texture_b);
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_GVRViewManager_readRenderResultNative(JNIEnv *env, jclass clazz,
                                                           jobject jreadback_buffer);
} // extern "C"


JNIEXPORT void JNICALL Java_org_gearvrf_GVRViewManager_readRenderResultNative(JNIEnv * env, jclass clazz, jobject jreadback_buffer) {
    uint8_t *readback_buffer = (uint8_t*) env->GetDirectBufferAddress(jreadback_buffer);

    GLint viewport[4];
    glGetIntegerv(GL_VIEWPORT, viewport);

    glReadPixels(viewport[0], viewport[1], viewport[2], viewport[3], GL_RGBA, GL_UNSIGNED_BYTE, readback_buffer);
}

}