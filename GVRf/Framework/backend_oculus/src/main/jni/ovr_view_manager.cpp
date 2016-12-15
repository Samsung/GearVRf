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

#include "../engine/renderer/renderer.h"
#include "../objects/components/camera.h"
#include "objects/textures/render_texture.h"
#include "vulkan/vulkanCore.h"
#include <jni.h>

namespace gvr {

extern "C" {

void Java_org_gearvrf_OvrViewManager_readRenderResultNative(JNIEnv *jni,
                                                            jclass clazz, jlong jrender_texture,
                                                            jobject jreadback_buffer) {

    uint8_t *pReadbackBuffer = (uint8_t *) jni->GetDirectBufferAddress(
            jreadback_buffer);
    RenderTexture *render_texture =
            reinterpret_cast<RenderTexture *>(jrender_texture);
    int width = render_texture->width();
    int height = render_texture->height();

// remember current FBO bindings
    GLint currentReadFBO, currentDrawFBO;
    glGetIntegerv(GL_READ_FRAMEBUFFER_BINDING, &currentReadFBO);
    glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &currentDrawFBO);

// blit the multisampled FBO to a normal FBO and read from it
    GLuint renderTextureFBO = render_texture->getFrameBufferId();
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, renderTextureFBO);
    glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,
                      GL_COLOR_BUFFER_BIT, GL_NEAREST);
    glBindFramebuffer(GL_READ_FRAMEBUFFER, renderTextureFBO);

    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE,
                 pReadbackBuffer);

// recover FBO bindings
    glBindFramebuffer(GL_READ_FRAMEBUFFER, currentReadFBO);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, currentDrawFBO);
}

} // extern "C"

}