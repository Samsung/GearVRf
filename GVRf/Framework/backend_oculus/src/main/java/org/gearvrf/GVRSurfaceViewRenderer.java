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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

/**
 * Create a descendant of this class if you need to do complex GL state
 * management.
 * 
 * Most applications can do all their initialization in
 * {@link GVRScript#onInit(GVRContext)}. If you need explicit access to the
 * {@link EGLConfig}, or the difference between
 * {@link Renderer#onSurfaceCreated(GL10, EGLConfig)} and
 * {@link Renderer#onSurfaceChanged(GL10, int, int)} really matters to you,
 * declare a class which {@code extends GVRSurfaceViewRenderer} and pass an
 * instance to
 * {@link GVRActivity#setScript(GVRScript, String, GVRSurfaceViewRenderer)} .
 */
public class GVRSurfaceViewRenderer implements GLSurfaceView.Renderer {
    private GVRMonoscopicViewManager mViewManager = null;

    /**
     * Constructs an empty GVRSurfaceViewRenderer
     */
    public GVRSurfaceViewRenderer() {
    }

    /**
     * Constructs {@link GVRSurfaceViewRenderer} given by {@link GVRViewManager}
     * 
     * @param viewManager
     *            a {@link GVRMonoscopicViewManager} object to be used in
     *            {@link GVRSurfaceViewRenderer}
     */
    public GVRSurfaceViewRenderer(GVRMonoscopicViewManager viewManager) {
        mViewManager = viewManager;
    }

    /**
     * Sets {@link GVRMonoscopicViewManager} for the {@link GVRSurfaceViewRenderer}
     * 
     * @param viewManager
     *            a {@link GVRMonoscopicViewManager} object to be used in
     *            {@link GVRSurfaceViewRenderer}
     */
    public void setViewManager(GVRMonoscopicViewManager viewManager) {
        mViewManager = viewManager;
    }

    /**
     * Generally, you should <em>not</em> override this.
     * 
     * Your {@link GVRScript#onStep()} method will be called every frame, and
     * GVRF provides mechanisms to dynamically add and subtract per-frame
     * callbacks. You can install
     * {@linkplain GVRContext#runOnGlThread(Runnable) 'one-shot' callbacks} and
     * you can
     * {@linkplain GVRContext#registerDrawFrameListener(GVRDrawFrameListener)
     * add} and
     * {@linkplain GVRContext#unregisterDrawFrameListener(GVRDrawFrameListener)
     * remove} recurring callbacks.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        mViewManager.onDrawFrame();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewManager.onSurfaceChanged(width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mViewManager.onSurfaceCreated();
    }
}
