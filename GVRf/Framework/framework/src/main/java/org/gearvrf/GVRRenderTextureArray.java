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
 * A render texture array is an array of textures
 * which may be rendered into. All of the textures
 * in the array must be the same size.
 * The GVRRenderTextureArray object is used internally
 * for shadow mapping. All of the shadow maps are kept
 * in a single layered texture.
 * @see GVRShadowMap
 * @see GVRRenderTexture
 */
public class GVRRenderTextureArray extends GVRRenderTexture
{
    /**
     * Create a layered texture array. The GPU memory for the
     * textures is not allocated here. It will be created in
     * the GL thread the first time the texture array is
     * used for rendering.
     * @param ctx       GVRContext to associate the texture array with.
     * @param width     pixel width of textures in the array.
     * @param height    pixel height of textures in the array.
     * @param samples   number of MSAA samples
     * @param layers    maximum number of textures in the array.
     */
    public GVRRenderTextureArray(GVRContext ctx, int width, int height, int samples, int layers)
    {
        super(ctx, NativeRenderTexture.ctorArray(width, height, samples, layers));
    }
}
