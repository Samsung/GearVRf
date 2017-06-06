package org.gearvrf;

import android.content.Context;
import android.graphics.Bitmap;

import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.debug.DebugServer;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.periodic.GVRPeriodicEngine;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.script.GVRScriptManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Class that supports the scripting feature. Not meant for anything
 * but internal use by the framework.
 */
public final class GVRContextProxy extends GVRContext {
    private final WeakReference<GVRContext> mContext;

    /**
     * Proxy calls into the passed context while holding a weak reference to it.
     */
    public GVRContextProxy(GVRContext context) {
        super();
        mContext = new WeakReference<>(context);
    }

    public Context getContext() {
        return mContext.get().getContext();
    }

    public GVRActivity getActivity() {
        return mContext.get().getActivity();
    }

    public GVRAssetLoader getAssetLoader() {
        return mContext.get().getAssetLoader();
    }

    public GVREventReceiver getEventReceiver() {
        return mContext.get().getEventReceiver();
    }


    public GVRMesh createQuad(float width, float height) {
        return mContext.get().createQuad(width, height);
    }

    public void assertGLThread() {
        mContext.get().assertGLThread();
    }

    public boolean isCurrentThreadGLThread() {
        return mContext.get().isCurrentThreadGLThread();
    }

    public GVRScene getMainScene() {
        return mContext.get().getMainScene();
    }

    public void setMainScene(GVRScene scene) {
        mContext.get().setMainScene(scene);
    }

    public DebugServer startDebugServer() {
        return mContext.get().startDebugServer();
    }

    public synchronized DebugServer startDebugServer(int port, int maxClients) {
        return mContext.get().startDebugServer(port, maxClients);
    }

    public void logError(String message, Object sender) {
        mContext.get().logError(message, sender);
    }

    public synchronized void stopDebugServer() {
        mContext.get().stopDebugServer();
    }

    public GVRInputManager getInputManager() {
        return mContext.get().getInputManager();
    }

    public GVREventManager getEventManager() {
        return mContext.get().getEventManager();
    }

    public GVRScriptManager getScriptManager() {
        return mContext.get().getScriptManager();
    }

    public float getFrameTime() {
        return mContext.get().getFrameTime();
    }

    public void runOnGlThread(Runnable runnable) {
        mContext.get().runOnGlThread(runnable);
    }

    public void runOnGlThreadPostRender(int delayFrames, Runnable runnable) {
        mContext.get().runOnGlThreadPostRender(delayFrames, runnable);
    }

    public void registerDrawFrameListener(GVRDrawFrameListener frameListener) {
        mContext.get().registerDrawFrameListener(frameListener);
    }

    public void unregisterDrawFrameListener(GVRDrawFrameListener frameListener) {
        mContext.get().unregisterDrawFrameListener(frameListener);
    }

    public GVRMaterialShaderManager getMaterialShaderManager() {
        return mContext.get().getMaterialShaderManager();
    }

    public GVRPostEffectShaderManager getPostEffectShaderManager() {
        return mContext.get().getPostEffectShaderManager();
    }

    public GVRAnimationEngine getAnimationEngine() {
        return mContext.get().getAnimationEngine();
    }

    public GVRPeriodicEngine getPeriodicEngine() {
        return mContext.get().getPeriodicEngine();
    }

    public void captureScreenCenter(GVRScreenshotCallback callback) {
        mContext.get().captureScreenCenter(callback);
    }

    public void captureScreenLeft(GVRScreenshotCallback callback) {
        mContext.get().captureScreenLeft(callback);
    }

    public void captureScreenRight(GVRScreenshotCallback callback) {
        mContext.get().captureScreenRight(callback);
    }

    public void captureScreen3D(GVRScreenshot3DCallback callback) {
        mContext.get().captureScreen3D(callback);
    }

    public void setTag(Object tag) {
        mContext.get().setTag(tag);
    }

    public Object getTag() {
        return mContext.get().getTag();
    }

    public void runOnTheFrameworkThread(Runnable runnable) {
        mContext.get().runOnTheFrameworkThread(runnable);
    }

    public void showToast(String message) {
        mContext.get().showToast(message);
    }

    public void showToast(String message, float duration) {
        mContext.get().showToast(message, duration);
    }
}
