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

package org.gearvrf;

/**
 * <span style="color:red; font-weight: bold">For internal use only.</span>
 * 
 * <p>
 * Provides methods for rendering scenes
 */
abstract class GVRMonoscopicRenderer {
    /**
     * Renders the given scene for the specified camera into the supplied
     * texture.
     * 
     * @param scene
     *            The {@link GVRScene scene} to render.
     * @param camera
     *            The {@link GVRCamera camera} to render the scene for.
     * @param renderTexture
     *            The framebuffer {@link GVRRenderTexture texture} to render the
     *            scene into.
     * @param renderBundle
     *            Options and data for the renderer's use.
     * @param listPostEffectData
     *            Data for {@link GVRPostEffectMap custom} post-effect shaders.
     */
    static void renderCamera(GVRScene scene, GVRCamera camera, int viewportX,
            int viewportY, int viewportWidth, int viewportHeight,
            GVRRenderBundle renderBundle) {

        NativeMonoscopicRenderer.renderCamera(scene.getNative(), camera
                .getNative(), viewportX, viewportY, viewportWidth,
                viewportHeight, renderBundle.getMaterialShaderManager()
                        .getNative(), renderBundle.getPostEffectShaderManager()
                        .getNative(), renderBundle
                        .getPostEffectRenderTextureA().getNative(),
                renderBundle.getPostEffectRenderTextureB().getNative());
    }

    static void cull(GVRScene scene, GVRCamera camera, GVRRenderBundle renderBundle) {
        NativeMonoscopicRenderer.cull(scene.getNative(), camera.getNative(), renderBundle.getMaterialShaderManager().getNative());
    }
}

class NativeMonoscopicRenderer {
    static native void cull(long scene, long camera, long shader_manager);
    static native void renderCamera(long scene, long camera, int viewportX,
            int viewportY, int viewportWidth, int viewportHeight,
            long shaderManager, long postEffectShaderManager,
            long postEffectRenderTextureA, long postEffectRenderTextureB);
}
