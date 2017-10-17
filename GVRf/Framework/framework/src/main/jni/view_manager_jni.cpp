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
#include "objects/components/render_target.h"
//#include "objects/components/camera.h"

namespace gvr {

class Camera;
class Scene;

extern "C" {

    JNIEXPORT void JNICALL
    Java_org_gearvrf_GVRViewManager_readRenderResultNative(JNIEnv *env, jclass clazz,
                                                           jobject jreadback_buffer, jlong jrenderTarget, jint eye, jboolean useMultiview);
} // extern "C"


JNIEXPORT void JNICALL Java_org_gearvrf_GVRViewManager_readRenderResultNative(JNIEnv *env, jclass clazz,
                                                                              jobject jreadback_buffer, jlong jrenderTarget, jint eye, jboolean useMultiview){
    uint8_t *readback_buffer = (uint8_t*) env->GetDirectBufferAddress(jreadback_buffer);
    RenderTarget* renderTarget = reinterpret_cast<RenderTarget*>(jrenderTarget);
    RenderTexture* renderTexture =    renderTarget->getTexture();

    if(useMultiview){
            renderTexture->setLayerIndex(eye);
    }
    renderTexture->readRenderResult(readback_buffer);
}

}