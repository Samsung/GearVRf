package org.gearvrf.script;

import android.content.Context;
import android.graphics.Bitmap;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRAtlasInformation;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCubemapTexture;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventManager;
import org.gearvrf.GVREventReceiver;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPostEffectShaderManager;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshot3DCallback;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.debug.DebugServer;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.periodic.GVRPeriodicEngine;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Future;

public final class GVRContextProxy {
    private final WeakReference<GVRContext> mContext;

    GVRContextProxy(GVRContext context) {
        mContext = new WeakReference<GVRContext>(context);
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

    public GVRMesh loadMesh(GVRAndroidResource androidResource) {
        return mContext.get().loadMesh(androidResource);
    }

    public GVRMesh loadMesh(GVRAndroidResource androidResource, EnumSet<GVRImportSettings> settings) {
        return mContext.get().loadMesh(androidResource, settings);
    }

    public void loadMesh(GVRAndroidResource.MeshCallback callback, GVRAndroidResource androidResource) throws IllegalArgumentException {
        mContext.get().loadMesh(callback, androidResource);
    }

    public void loadMesh(GVRAndroidResource.MeshCallback callback, GVRAndroidResource resource, int priority) throws IllegalArgumentException {
        mContext.get().loadMesh(callback, resource, priority);
    }

    public Future<GVRMesh> loadFutureMesh(GVRAndroidResource resource) {
        return mContext.get().loadFutureMesh(resource);
    }

    public Future<GVRMesh> loadFutureMesh(GVRAndroidResource resource, int priority) {
        return mContext.get().loadFutureMesh(resource, priority);
    }

    public GVRSceneObject getAssimpModel(String assetRelativeFilename) throws IOException {
        return mContext.get().getAssimpModel(assetRelativeFilename);
    }

    public GVRSceneObject getAssimpModel(String assetRelativeFilename, EnumSet<GVRImportSettings> settings) throws IOException {
        return mContext.get().getAssimpModel(assetRelativeFilename, settings);
    }

    public GVRModelSceneObject loadJassimpModelFromSD(String externalFile) throws IOException {
        return mContext.get().loadJassimpModelFromSD(externalFile);
    }

    public GVRModelSceneObject loadJassimpModelFromSD(String externalFile, EnumSet<GVRImportSettings> settings) throws IOException {
        return mContext.get().loadJassimpModelFromSD(externalFile, settings);
    }

    public GVRModelSceneObject loadJassimpModel(String assetFile) throws IOException {
        return mContext.get().loadJassimpModel(assetFile);
    }

    public GVRModelSceneObject loadJassimpModel(String assetFile, EnumSet<GVRImportSettings> settings) throws IOException {
        return mContext.get().loadJassimpModel(assetFile, settings);
    }

    public GVRModelSceneObject loadModelFromSD(String externalFile) throws IOException {
        return mContext.get().loadModelFromSD(externalFile);
    }

    public GVRModelSceneObject loadModelFromSD(String externalFile, EnumSet<GVRImportSettings> settings) throws IOException {
        return mContext.get().loadModelFromSD(externalFile, settings);
    }

    public GVRModelSceneObject loadModel(String assetFile) throws IOException {
        return mContext.get().loadModel(assetFile);
    }

    public GVRModelSceneObject loadModel(String assetFile, EnumSet<GVRImportSettings> settings, GVRScene scene) throws IOException {
        return mContext.get().loadModel(assetFile, settings, scene);
    }

    public GVRModelSceneObject loadModel(String assetFile, EnumSet<GVRImportSettings> settings) throws IOException {
        return mContext.get().loadModel(assetFile, settings);
    }

    public GVRSceneObject loadModelFromURL(String urlString) throws IOException {
        return mContext.get().loadModelFromURL(urlString);
    }

    public GVRSceneObject loadModelFromURL(String urlString, boolean cacheEnabled) throws IOException {
        return mContext.get().loadModelFromURL(urlString, cacheEnabled);
    }

    public GVRSceneObject loadModelFromURL(String urlString, EnumSet<GVRImportSettings> settings) throws IOException {
        return mContext.get().loadModelFromURL(urlString, settings);
    }

    public GVRMesh createQuad(float width, float height) {
        return mContext.get().createQuad(width, height);
    }

    public Bitmap loadBitmap(String fileName) {
        return mContext.get().loadBitmap(fileName);
    }

    public GVRBitmapTexture loadTexture(String fileName) {
        return mContext.get().loadTexture(fileName);
    }

    public GVRBitmapTexture loadTexture(String fileName, GVRTextureParameters textureParameters) {
        return mContext.get().loadTexture(fileName, textureParameters);
    }

    public GVRTexture loadTexture(GVRAndroidResource resource) {
        return mContext.get().loadTexture(resource);
    }

    public GVRTexture loadTexture(GVRAndroidResource resource, GVRTextureParameters textureParameters) {
        return mContext.get().loadTexture(resource, textureParameters);
    }

    public GVRCubemapTexture loadCubemapTexture(GVRAndroidResource[] resourceArray) {
        return mContext.get().loadCubemapTexture(resourceArray);
    }

    public GVRCubemapTexture loadCubemapTexture(GVRAndroidResource[] resourceArray, GVRTextureParameters textureParameters) {
        return mContext.get().loadCubemapTexture(resourceArray, textureParameters);
    }

    public void assertGLThread() {
        mContext.get().assertGLThread();
    }

    public boolean isCurrentThreadGLThread() {
        return mContext.get().isCurrentThreadGLThread();
    }

    public void loadBitmapTexture(GVRAndroidResource.BitmapTextureCallback callback, GVRAndroidResource resource) {
        mContext.get().loadBitmapTexture(callback, resource);
    }

    public void loadBitmapTexture(GVRAndroidResource.TextureCallback callback, GVRAndroidResource resource, int priority) throws IllegalArgumentException {
        mContext.get().loadBitmapTexture(callback, resource, priority);
    }

    public void loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback callback, GVRAndroidResource resource) {
        mContext.get().loadCompressedTexture(callback, resource);
    }

    public void loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback callback, GVRAndroidResource resource, int quality) {
        mContext.get().loadCompressedTexture(callback, resource, quality);
    }

    public void loadTexture(GVRAndroidResource.TextureCallback callback, GVRAndroidResource resource) {
        mContext.get().loadTexture(callback, resource);
    }

    public void loadTexture(GVRAndroidResource.TextureCallback callback, GVRAndroidResource resource, int priority) {
        mContext.get().loadTexture(callback, resource, priority);
    }

    public void loadTexture(GVRAndroidResource.TextureCallback callback, GVRAndroidResource resource, int priority, int quality) {
        mContext.get().loadTexture(callback, resource, priority, quality);
    }

    public void loadTexture(GVRAndroidResource.TextureCallback callback, GVRAndroidResource resource, GVRTextureParameters textureParameters) {
        mContext.get().loadTexture(callback, resource, textureParameters);
    }

    public void loadTexture(GVRAndroidResource.TextureCallback callback, GVRAndroidResource resource, GVRTextureParameters textureParameters, int priority) {
        mContext.get().loadTexture(callback, resource, textureParameters, priority);
    }

    public void loadTexture(GVRAndroidResource.TextureCallback callback, GVRAndroidResource resource, GVRTextureParameters textureParameters, int priority, int quality) {
        mContext.get().loadTexture(callback, resource, textureParameters, priority, quality);
    }

    public Future<GVRTexture> loadFutureTexture(GVRAndroidResource resource) {
        return mContext.get().loadFutureTexture(resource);
    }

    public Future<GVRTexture> loadFutureTexture(GVRAndroidResource resource, int priority) {
        return mContext.get().loadFutureTexture(resource, priority);
    }

    public Future<GVRTexture> loadFutureTexture(GVRAndroidResource resource, int priority, int quality) {
        return mContext.get().loadFutureTexture(resource, priority, quality);
    }

    public Future<GVRTexture> loadFutureCubemapTexture(GVRAndroidResource resource) {
        return mContext.get().loadFutureCubemapTexture(resource);
    }

    public Future<GVRTexture> loadFutureCompressedCubemapTexture(GVRAndroidResource resource) {
        return mContext.get().loadFutureCompressedCubemapTexture(resource);
    }

    public List<GVRAtlasInformation> loadTextureAtlasInformation(GVRAndroidResource resource) throws IOException {
        return mContext.get().loadTextureAtlasInformation(resource);
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
