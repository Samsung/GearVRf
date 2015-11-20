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

/** Frame Buffer object. */
public class GVRRenderTexture extends GVRTexture {
    /**
     * Constructs a GVRRenderTexture for a frame buffer of the specified size.
     *
     * @param gvrContext
     *            Current gvrContext
     * @param width
     *            Width of the frame buffer.
     * @param height
     *            Height of the frame buffer.
     */
    public GVRRenderTexture(GVRContext gvrContext, int width, int height) {
        super(gvrContext, NativeRenderTexture.ctor(width, height));

        mWidth = width;
        mHeight = height;
    }

    /**
     * Constructs a GVRRenderTexture for a frame buffer of the specified size,
     * with MSAA enabled at the specified sample count.
     *
     * @param gvrContext
     *            Current gvrContext
     * @param width
     *            Width of the frame buffer.
     * @param height
     *            Height of the frame buffer.
     * @param sampleCount
     *            MSAA sample count.
     */
    public GVRRenderTexture(GVRContext gvrContext, int width, int height,
            int sampleCount) {
        super(gvrContext, NativeRenderTexture.ctorMSAA(width, height,
                sampleCount));
        mWidth = width;
        mHeight = height;
    }

    GVRRenderTexture(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    /**
     * Return the width of GVRRenderTexture (FBO)
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Return the height of GVRRenderTexture (FBO)
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Return the render texture.
     *
     * @param readbackBuffer
     *        A preallocated IntBuffer to receive the data from the texture. Its capacity should
     *        be width * height. The output pixel format is GPU format GL_RGBA packed as an 32-bit
     *        integer.
     *
     * @return true if successful.
     */
    boolean readRenderResult(int[] readbackBuffer) {
        return NativeRenderTexture.readRenderResult(getNative(), readbackBuffer);
    }

    private int mWidth, mHeight;
}

class NativeRenderTexture {
    static native long ctor(int width, int height);

    static native long ctorMSAA(int width, int height, int sampleCount);

    static native boolean readRenderResult(long ptr, int[] readbackBuffer);
}
