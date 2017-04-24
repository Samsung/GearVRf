package org.gearvrf;

import org.gearvrf.utility.Threads;

import java.lang.ref.WeakReference;

public final class GVRSceneRenderTexture extends GVRRenderTexture {
    /**
     * Render the specified scene to a texture. If the scene is used for anything other than rendering
     * to the texture the result is undefined. Executes asynchronously and when everything is set up and
     * frames are being produced, the onReady runnable is executed.
     *
     * For example, onReady can be used to set the diffuseTexture of a material to this texture.
     */
    public GVRSceneRenderTexture(final GVRContext gvrContext, final GVRScene scene, int width, int height, final Runnable onReady) {
        super(gvrContext, width, height);

        gvrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                final GVRPerspectiveCamera centerCamera = scene.getMainCameraRig().getCenterCamera();
                //flips vertically the output
                centerCamera.setFovY(-centerCamera.getFovY());

                final WeakReference<GVRSceneRenderTexture> weakRef = new WeakReference<>(GVRSceneRenderTexture.this);

                final GVRDrawFrameListener drawFrameListener = new GVRDrawFrameListener() {
                    @Override
                    public void onDrawFrame(float frameTime) {
                        final GVRRenderTexture rt = weakRef.get();
                        if (null != rt) {
                            gvrContext.renderTexture(scene, scene.getMainCameraRig().getCenterCamera(), rt);
                        } else {
                            gvrContext.unregisterDrawFrameListener(this);
                        }
                    }
                };
                gvrContext.registerDrawFrameListener(drawFrameListener);

                Threads.spawnLow(new Runnable() {
                    @Override
                    public void run() {
                        //to ensure at least one frame is rendered to the texture first
                        GVRNotifications.waitBeforeStep();
                        GVRNotifications.waitAfterStep();
                        //now let the user know it is ready
                        gvrContext.runOnTheFrameworkThread(new Runnable() {
                            @Override
                            public void run() {
                                onReady.run();
                            }
                        });
                    }
                });
            }
        });
    }
}
