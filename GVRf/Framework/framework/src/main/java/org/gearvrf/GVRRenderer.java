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
 * Java holder for the rendering engine.
 */
public class GVRRenderer extends GVRHybridObject
{
    private GVRShaderManager mShaderManager;

    GVRRenderer(GVRContext gvrContext, GVRShaderManager shaderManager)
    {
        super(gvrContext, NativeRenderer.ctor());
        mShaderManager = shaderManager;
    }

    void makeShadowMaps(GVRScene scene)
    {
        NativeRenderer.makeShadowMaps(getNative(), scene.getNative(), mShaderManager.getNative());
    }

    void cullAndRender(GVRScene scene, GVRRenderTarget target,
                       GVRRenderTexture post_effect_render_texture_a,
                       GVRRenderTexture post_effect_render_texture_b)
    {
        NativeRenderer.cullAndRender(getNative(),
                                     target.getNative(),
                                     scene.getNative(),
                                     mShaderManager.getNative(),
                                     post_effect_render_texture_a.getNative(),
                                     post_effect_render_texture_b.getNative());
    }

    void useStencil(boolean flag)
    {
        NativeRenderer.useStencil(getNative(), flag);
    }

}

class NativeRenderer {

    static native long ctor();

    static native void makeShadowMaps(long renderer, long scene, long shaderManager);

    static native void useStencil(long renderer, boolean flag);

    static native void cullAndRender(long renderer,
                                    long renderTarget, long scene,
                                    long shader_manager,
                                    long post_effect_render_texture_a,
                                    long post_effect_render_texture_b);
}
