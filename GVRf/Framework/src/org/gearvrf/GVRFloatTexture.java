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
 * A specialized texture, for doing computation on the GPU.
 * 
 * Loads an array of float pairs into a texture; you can access the individual
 * components of each 'pixel' as the {@code .r} and {code .g} swizzles.
 * 
 * @since 1.6.3
 */
public class GVRFloatTexture extends GVRTexture {

    /**
     * Create a float-point texture.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param width
     *            Texture width, in pixels
     * @param height
     *            Texture height, in pixels
     * @param data
     *            A linear array of float pairs.
     * @throws IllegalArgumentException
     *             If {@code width} or {@code height} is {@literal <= 0,} or if
     *             {@code data} is {@code null}, or if
     *             {@code data.length < height * width * 2}
     * 
     * @since 1.6.3
     */
    public GVRFloatTexture(GVRContext gvrContext, int width, int height,
            float[] data) throws IllegalArgumentException {
        super(gvrContext, NativeFloatTexture.ctor());
        update(width, height, data);
    }

    /**
     * Copy new data to an existing float-point texture.
     * 
     * Creating a new {@link GVRFloatTexture} is pretty cheap, but it's still
     * not a totally trivial operation: it does involve some memory management
     * and some GL hardware handshaking. Reusing the texture reduces this
     * overhead (primarily by delaying garbage collection). Do be aware that
     * updating a texture will affect any and all {@linkplain GVRMaterial
     * materials} (and/or {@link GVRPostEffect post effects)} that use the
     * texture!
     * 
     * @param width
     *            Texture width, in pixels
     * @param height
     *            Texture height, in pixels
     * @param data
     *            A linear array of float pairs.
     * @return {@code true} if the update succeeded, and {@code false} if it
     *         failed. Updating a texture requires that the new data parameter
     *         has the exact same {@code width} and {@code height} and pixel
     *         format as the original data.
     * @throws IllegalArgumentException
     *             If {@code width} or {@code height} is {@literal <= 0,} or if
     *             {@code data} is {@code null}, or if
     *             {@code data.length < height * width * 2}
     */
    public boolean update(int width, int height, float[] data)
            throws IllegalArgumentException {
        if (width <= 0 || height <= 0 || data == null
                || data.length < height * width * 2) {
            throw new IllegalArgumentException();
        }
        return NativeFloatTexture.update(getNative(), width, height, data);
    }
}

class NativeFloatTexture {
    static native long ctor();

    static native boolean update(long pointer, int width, int height,
            float[] data);
}
