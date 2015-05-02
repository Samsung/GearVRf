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
 * {@link GVRActivity#setScript(GVRScript, String, GVRSurfaceViewRenderer)}
 * .
 */
public class GVRSurfaceViewRenderer implements GLSurfaceView.Renderer {
    private GVRMonoViewManager mViewManager = null;

    /**
     * Constructs an empty GVRSurfaceViewRenderer
     */
    public GVRSurfaceViewRenderer() {
    }

    /**
     * Constructs {@link GVRSurfaceViewRenderer} given by {@link GVRViewManager}
     * 
     * @param viewManager
     *            a {@link GVRViewManager} object to be used in
     *            {@link GVRSurfaceViewRenderer}
     */
    public GVRSurfaceViewRenderer(GVRMonoViewManager viewManager) {
        mViewManager = viewManager;
    }

    /**
     * Sets {@link GVRViewManager} for the {@link GVRSurfaceViewRenderer}
     * 
     * @param viewManager
     *            a {@link GVRViewManager} object to be used in
     *            {@link GVRSurfaceViewRenderer}
     */
    public void setViewManager(GVRMonoViewManager viewManager) {
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