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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Gravity;

import org.gearvrf.GVRAndroidResource.MeshCallback;
import org.gearvrf.GVRHybridObject.NativeCleanupHandler;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRMaterialAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader;
import org.gearvrf.debug.DebugServer;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.periodic.GVRPeriodicEngine;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.script.GVRScriptManager;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.ResourceCache;
import org.gearvrf.utility.Threads;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Like the Android {@link Context} class, {@code GVRContext} provides core
 * services, and global information about an application environment.
 * {@code GVRContext} also holds the {@linkplain GVRScene main scene} and miscellaneous information
 * like {@linkplain #getFrameTime() the frame time.}
 * <ul>
 * <li>
 * The application <b>activity</b> resides in the context {@link #getActivity()}.
 * </li>
 * <li>
 * The current scene in the context contains all the displayable 3D objects {@link #getMainScene()}.
 * </li>
 * <li>
 * The <b>event receiver</b> in the context listens for events from scripting,
 * picking, the asset loader and dispatches to user callbacks {@link #getEventReceiver()}.
 * </li>
 * <li>
 * The context allows you to run code on either the Java or rendering thread
 * {@link #runOnGlThread(Runnable), {@link #runOnTheFrameworkThread(Runnable)}.
 * </li>
 * <li>
 * The <b>asset loader</b> in the context can load textures and models from a variety of sources
 * both synchronously and asynchronously {@link #getAssetLoader()}.
 * </li>
 * <li>
 * You can capture the 3D screen using context screen capture functions {@link #captureScreenCenter(GVRScreenshotCallback)}.
 * </li>
 * <li>
 *  The <b>shader manager</b> in the context lets you create custom shaders.
 * </ul>
 * @see GVRAssetLoader
 * @see GVREventReceiver
 * @see GVRScene
 */
public abstract class GVRContext implements IEventReceiver {
    private static final String TAG = Log.tag(GVRContext.class);

    private final GVRActivity mContext;

    private GVREventReceiver mEventReceiver;
    /*
     * Fields and constants
     */

    // Debug and log level settings

    /**
     * Set to true for displaying statistics line.
     */
    public static boolean DEBUG_STATS = false;

    /**
     * Period of statistic log in milliseconds.
     */
    public static long DEBUG_STATS_PERIOD_MS = 1000;

    // Priorities constants, for asynchronous loading

    /**
     * GVRF can't use every {@code int} as a priority - it needs some sentinel
     * values. It will probably never need anywhere near this many, but raising
     * the number of reserved values narrows the 'dynamic range' available to
     * apps mapping some internal score to the {@link #LOWEST_PRIORITY} to
     * {@link #HIGHEST_PRIORITY} range, and might change app behavior in subtle
     * ways that seem best avoided.
     * 
     * @since 1.6.1
     */
    public static final int RESERVED_PRIORITIES = 1024;

    /**
     * GVRF can't use every {@code int} as a priority - it needs some sentinel
     * values. A simple approach to generating priorities is to score resources
     * from 0 to 1, and then map that to the range {@link #LOWEST_PRIORITY} to
     * {@link #HIGHEST_PRIORITY}.
     * 
     * @since 1.6.1
     */
    public static final int LOWEST_PRIORITY = Integer.MIN_VALUE
            + RESERVED_PRIORITIES;

    /**
     * GVRF can't use every {@code int} as a priority - it needs some sentinel
     * values. A simple approach to generating priorities is to score resources
     * from 0 to 1, and then map that to the range {@link #LOWEST_PRIORITY} to
     * {@link #HIGHEST_PRIORITY}.
     * 
     * @since 1.6.1
     */
    public static final int HIGHEST_PRIORITY = Integer.MAX_VALUE;

    /**
     * The priority used by
     * {@link GVRAssetLoader#loadTexture(GVRAndroidResource)}
     * and
     * {@link #loadMesh(GVRAndroidResource.MeshCallback, GVRAndroidResource)}
     *
     * @since 1.6.1
     * @deprecated use GVRAssetLoader.DEFAULT_PRIORITY instead
     */
    public static final int DEFAULT_PRIORITY = 0;

    /**
     * The ID of the GLthread. We use this ID to prevent non-GL thread from
     * calling GL functions.
     * 
     * @since 1.6.5
     */
    protected long mGLThreadID;

    /**
     * The default texture parameter instance for overloading texture methods
     * @deprecated use GVRAssetLoader.getDefaultTextureParameters instead
     */
    public final GVRTextureParameters DEFAULT_TEXTURE_PARAMETERS = new GVRTextureParameters(
            this);

    // true or false based on the support for anisotropy
    public boolean isAnisotropicSupported;

    // Max anisotropic value if supported and -1 otherwise
    public int maxAnisotropicValue = -1;

    // Debug server
    protected DebugServer mDebugServer;

    protected GVRAssetLoader mImporter = new GVRAssetLoader(this);
    /*
     * Methods
     */

    GVRContext(GVRActivity context) {
        mContext = context;
        mEventReceiver = new GVREventReceiver(this);

        mHandlerThread = new HandlerThread("gvrf-main");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    /**
     * Just for proxy-ing. See GVRContextProxy.
     */
    GVRContext() {
        mContext = null;
        mHandler = null;
        mHandlerThread = null;
    }

    /**
     * Get the Android {@link Context}, which provides access to system services
     * and to your application's resources. Since version 2.0.1, this is
     * actually your {@link GVRActivity} implementation, but you should probably
     * use the new {@link #getActivity()} method, rather than casting this
     * method to an {@code (Activity)} or {@code (GVRActivity)}.
     * 
     * @return An Android {@code Context}
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Get the Android {@link Activity} which launched your GVRF app.
     * 
     * An {@code Activity} is-a {@link Context} and so provides access to system
     * services and to your application's resources; the {@code Activity} class
     * also provides additional services, including
     * {@link Activity#runOnUiThread(Runnable)}.
     * 
     * @return The {@link GVRActivity} which launched your GVRF app. The
     *         {@link GVRActivity} class doesn't actually add much useful
     *         functionality besides
     *         {@link GVRActivity#setMain(GVRMain, String)}, but returning
     *         the most-derived class here may prevent someone from having to
     *         write {@code (GVRActivity) gvrContext.getActivity();}.
     * 
     * @since 2.0.1
     */
    public GVRActivity getActivity() {
        return mContext;
    }

    /**
     * Get the {@link GVRAssetLoader} to use for loading assets.
     * 
     * A {@code GVRAssetLoader} loads models asynchronously from your application's
     * local storage or the network.
     * 
     * 
     * @return The asset loader associated with this context.
     */
    public GVRAssetLoader getAssetLoader() {
        return mImporter;
    }
    
    /**
     * Get the event receiver for this context.
     * 
     * The context event receiver processes events raised on the context.
     * These include asset loading events (IAssetEvents)
     * 
     * @see IAssetEvents
     * @see IEventReceiver
     */
    public GVREventReceiver getEventReceiver() {
        return mEventReceiver;
    }

    /**
     * Creates a quad consisting of two triangles, with the specified width and
     * height.
     * 
     * @param width
     *            the quad's width
     * @param height
     *            the quad's height
     * @return A 2D, rectangular mesh with four vertices and two triangles
     */
    @SuppressWarnings("deprecation")
    public GVRMesh createQuad(float width, float height) {
        GVRMesh mesh = new GVRMesh(this);

        float[] vertices = { width * -0.5f, height * 0.5f, 0.0f, width * -0.5f,
                height * -0.5f, 0.0f, width * 0.5f, height * 0.5f, 0.0f,
                width * 0.5f, height * -0.5f, 0.0f };
        mesh.setVertices(vertices);

        final float[] normals = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f };
        mesh.setNormals(normals);

        final float[] texCoords = { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                1.0f };
        mesh.setTexCoords(texCoords);

        char[] triangles = { 0, 1, 2, 1, 3, 2 };
        mesh.setTriangles(triangles);

        return mesh;
    }

    /**
     * Throws an exception if the current thread is not a GL thread.
     * 
     * @since 1.6.5
     * 
     */
    public void assertGLThread() {
        if (Thread.currentThread().getId() != mGLThreadID) {
            RuntimeException e = new RuntimeException(
                    "Should not run GL functions from a non-GL thread!");
            e.printStackTrace();
            throw e;
        }
    }

    /*
     * To see if current thread is GL thread.
     * 
     * @return {@code true} if current thread is GL thread, {@code false} if
     * current thread is not GL thread
     */

    public boolean isCurrentThreadGLThread() {
        return Thread.currentThread().getId() == mGLThreadID;
    }

    /**
     * Get the current {@link GVRScene}, which contains the scene graph (a
     * hierarchy of {@linkplain GVRSceneObject scene objects}) and the
     * {@linkplain GVRCameraRig camera rig}
     * 
     * @return A {@link GVRScene} instance, containing scene and camera
     *         information
     */
    public abstract GVRScene getMainScene();

    /** Set the current {@link GVRScene} */
    public abstract void setMainScene(GVRScene scene);

    /**
     * Start a debug server on the default TCP/IP port for the default number
     * of clients.
     */
    public DebugServer startDebugServer() {
        return startDebugServer(DebugServer.DEFAULT_DEBUG_PORT, DebugServer.NUM_CLIENTS);
    }

    /**
     * Start a debug server on a specified TCP/IP port, allowing a specified number
     * of concurrent clients.
     *
     * @param port
     *     The port number for the TCP/IP server.
     * @param maxClients
     *     The maximum number of concurrent clients.
     */
    public synchronized DebugServer startDebugServer(int port, int maxClients) {
        if (mDebugServer != null) {
            Log.e(TAG, "Debug server has already been started.");
            return mDebugServer;
        }

        mDebugServer = new DebugServer(this, port, maxClients);
        Threads.spawn(mDebugServer);
        return mDebugServer;
    }

    /**
     * Logs an error by sending an error event to all listeners.
     * 
     * Error events can be generated by any part of GearVRF,
     * from any thread. They are always sent to the event receiver
     * of the GVRContext.
     * 
     * @param message error message
     * @param sender object which had the error
     * @see IErrorEvents
     */
    public void logError(String message, Object sender) {
        getEventManager().sendEvent(this, IErrorEvents.class, "onError", new Object[] { message, sender });
    }
    
    /**
     * Stops the current debug server. Active connections are
     * not affected.
     */
    public synchronized void stopDebugServer() {
        if (mDebugServer == null) {
            Log.e(TAG, "Debug server is not running.");
            return;
        }

        mDebugServer.shutdown();
        mDebugServer = null;
    }

    /**
     * Returns the {@link GVRInputManager}.
     * 
     * @return A {@link GVRInputManager} to help the GVRf application interface
     *         with the input subsystem.
     * 
     */
    public abstract GVRInputManager getInputManager();

    /**
     * Returns the {@link GVREventManager}.
     *
     * @return A {@link GVREventManager} to help the GVRf framework and
     * applications to deliver events.
     *
     */
    public abstract GVREventManager getEventManager();

    /**
     * Returns the {@link GVRScriptManager}.
     *
     * @return A {@link GVRInputManager} to help the GVRf application to
     * create, load or execute scripts.
     *
     */
    public abstract GVRScriptManager getScriptManager();

    /**
     * The interval between this frame and the previous frame, in seconds: a
     * rough gauge of Frames Per Second.
     */
    public abstract float getFrameTime();

    /**
     * Enqueues a callback to be run in the GL thread.
     * 
     * This is how you take data generated on a background thread (or the main
     * (GUI) thread) and pass it to the coprocessor, using calls that must be
     * made from the GL thread (aka the "GL context"). The callback queue is
     * processed before any registered
     * {@linkplain #registerDrawFrameListener(GVRDrawFrameListener) frame
     * listeners}.
     * 
     * @param runnable
     *            A bit of code that must run on the GL thread
     */
    public abstract void runOnGlThread(Runnable runnable);

    /**
     * Enqueues a callback to be run in the GL thread after rendering a frame.
     *
     * This is how you take data generated on a background thread (or the main
     * (GUI) thread) and pass it to the coprocessor, using calls that must be
     * made from the GL thread (aka the "GL context"). The callback queue is
     * processed after a frame has been rendered.
     *
     * @param delayFrames
     *            Number of frames to delay the task. 0 means current frame.
     * @param runnable
     *            A bit of code that must run on the GL thread after rendering
     *            a frame.
     */
    public abstract void runOnGlThreadPostRender(int delayFrames, Runnable runnable);

    /**
     * Subscribes a {@link GVRDrawFrameListener}.
     * 
     * Each frame listener is called, once per frame, after any pending
     * {@linkplain #runOnGlThread(Runnable) GL callbacks} and before
     * {@link GVRMain#onStep()}.
     * 
     * @param frameListener
     *            A callback that will fire once per frame, until it is
     *            {@linkplain #unregisterDrawFrameListener(GVRDrawFrameListener)
     *            unregistered}
     */
    public abstract void registerDrawFrameListener(
            GVRDrawFrameListener frameListener);

    /**
     * Remove a previously-subscribed {@link GVRDrawFrameListener}.
     * 
     * @param frameListener
     *            An instance of a {@link GVRDrawFrameListener} implementation.
     *            Unsubscribing a listener which is not actually subscribed will
     *            not throw an exception.
     */
    public abstract void unregisterDrawFrameListener(
            GVRDrawFrameListener frameListener);

    /**
     * The {@linkplain GVRMaterialShaderManager object shader manager}
     * singleton.
     * 
     * Use the shader manager to define custom GL object shaders, which are used
     * to render a scene object's surface.
     * 
     * @return The {@linkplain GVRMaterialShaderManager shader manager}
     *         singleton.
     */
    public abstract GVRMaterialShaderManager getMaterialShaderManager();

    /**
     * The {@linkplain GVRPostEffectShaderManager scene shader manager}
     * singleton.
     * 
     * Use the shader manager to define custom GL scene shaders, which can be
     * inserted into the rendering pipeline to apply image processing effects to
     * the rendered scene graph. In classic GL programming, this is often
     * referred to as a "post effect."
     * 
     * @return The {@linkplain GVRPostEffectShaderManager post effect shader
     *         manager} singleton.
     */
    public abstract GVRPostEffectShaderManager getPostEffectShaderManager();

    /**
     * The {@linkplain GVRAnimationEngine animation engine} singleton.
     * 
     * Use the animation engine to start and stop {@linkplain GVRAnimation
     * animations}.
     * 
     * @return The {@linkplain GVRAnimationEngine animation engine} singleton.
     */
    public GVRAnimationEngine getAnimationEngine() {
        return GVRAnimationEngine.getInstance(this);
    }

    /**
     * The {@linkplain GVRPeriodicEngine periodic engine} singleton.
     * 
     * Use the periodic engine to schedule {@linkplain Runnable runnables} to
     * run on the GL thread at a future time.
     * 
     * @return The {@linkplain GVRPeriodicEngine periodic engine} singleton.
     */
    public GVRPeriodicEngine getPeriodicEngine() {
        return GVRPeriodicEngine.getInstance(this);
    }

    /**
     * Register a method that is called every time GVRF creates a new
     * {@link GVRContext}.
     * 
     * Android apps aren't mapped 1:1 to Linux processes; the system may keep a
     * process loaded even after normal complete shutdown, and call Android
     * lifecycle methods to reinitialize it. This causes problems for (in
     * particular) lazy-created singletons that are tied to a particular
     * {@code GVRContext}. This method lets you register a handler that will be
     * called on restart, which can reset your {@code static} variables to the
     * compiled-in start state.
     * 
     * <p>
     * For example,
     * 
     * <pre>
     * 
     * static YourSingletonClass sInstance;
     * static {
     *     GVRContext.addResetOnRestartHandler(new Runnable() {
     * 
     *         &#064;Override
     *         public void run() {
     *             sInstance = null;
     *         }
     *     });
     * }
     * 
     * </pre>
     * 
     * <p>
     * GVRF will force an Android garbage collection after running any handlers,
     * which will free any remaining native objects from the previous run.
     * 
     * @param handler
     *            Callback to run on restart.
     */
    public synchronized static void addResetOnRestartHandler(Runnable handler) {
        sHandlers.add(handler);
    }

    protected synchronized static void resetOnRestart() {
        for (Runnable handler : sHandlers) {
            Log.d(TAG, "Running on-restart handler %s", handler);
            handler.run();
        }

        // We've probably just nulled-out a bunch of references, but many GVRF
        // apps do relatively little Java memory allocation, so it may actually
        // be a longish while before the recyclable references go stale.
        System.gc();

        // We do NOT want to clear sHandlers - the static initializers won't be
        // run again, even if the new run does recreate singletons.
    }

    private static final List<Runnable> sHandlers = new ArrayList<Runnable>();

    /**
     * Capture a 2D screenshot from the position in the middle of left eye and
     * right eye.
     * 
     * The screenshot capture is done asynchronously -- the function does not
     * return the result immediately. Instead, it registers a callback function
     * and pass the result (when it is available) to the callback function. The
     * callback will happen on a background thread: It will probably not be the
     * same thread that calls this method, and it will not be either the GUI or
     * the GL thread.
     * 
     * Users should not start a {@code captureScreenCenter} until previous
     * {@code captureScreenCenter} callback has returned. Starting a new
     * {@code captureScreenCenter} before the previous
     * {@code captureScreenCenter} callback returned may cause out of memory
     * error.
     * 
     * @param callback
     *            Callback function to process the capture result. It may not be
     *            {@code null}.
     */
    public abstract void captureScreenCenter(GVRScreenshotCallback callback);

    /**
     * Capture a 2D screenshot from the position of left eye.
     * 
     * The screenshot capture is done asynchronously -- the function does not
     * return the result immediately. Instead, it registers a callback function
     * and pass the result (when it is available) to the callback function. The
     * callback will happen on a background thread: It will probably not be the
     * same thread that calls this method, and it will not be either the GUI or
     * the GL thread.
     * 
     * Users should not start a {@code captureScreenLeft} until previous
     * {@code captureScreenLeft} callback has returned. Starting a new
     * {@code captureScreenLeft} before the previous {@code captureScreenLeft}
     * callback returned may cause out of memory error.
     * 
     * @param callback
     *            Callback function to process the capture result. It may not be
     *            {@code null}.
     */
    public abstract void captureScreenLeft(GVRScreenshotCallback callback);

    /**
     * Capture a 2D screenshot from the position of right eye.
     * 
     * The screenshot capture is done asynchronously -- the function does not
     * return the result immediately. Instead, it registers a callback function
     * and pass the result (when it is available) to the callback function. The
     * callback will happen on a background thread: It will probably not be the
     * same thread that calls this method, and it will not be either the GUI or
     * the GL thread.
     * 
     * Users should not start a {@code captureScreenRight} until previous
     * {@code captureScreenRight} callback has returned. Starting a new
     * {@code captureScreenRight} before the previous {@code captureScreenRight}
     * callback returned may cause out of memory error.
     * 
     * @param callback
     *            Callback function to process the capture result. It may not be
     *            {@code null}.
     */
    public abstract void captureScreenRight(GVRScreenshotCallback callback);

    /**
     * Capture a 3D screenshot from the position of left eye. The 3D screenshot
     * is composed of six images from six directions (i.e. +x, -x, +y, -y, +z,
     * and -z).
     * 
     * The screenshot capture is done asynchronously -- the function does not
     * return the result immediately. Instead, it registers a callback function
     * and pass the result (when it is available) to the callback function. The
     * callback will happen on a background thread: It will probably not be the
     * same thread that calls this method, and it will not be either the GUI or
     * the GL thread.
     * 
     * Users should not start a {@code captureScreen3D} until previous
     * {@code captureScreen3D} callback has returned. Starting a new
     * {@code captureScreen3D} before the previous {@code captureScreen3D}
     * callback returned may cause out of memory error.
     * 
     * @param callback
     *            Callback function to process the capture result. It may not be
     *            {@code null}.
     * 
     * @since 1.6.8
     */
    public abstract void captureScreen3D(GVRScreenshot3DCallback callback);

    private Object mTag;

    /**
     * Sets the tag associated with this context.
     * 
     * Tags can be used to store data within the context without
     * resorting to another data structure.
     *
     * @param tag an object to associate with this context
     * 
     * @see #getTag()
     * @since 3.0.0
     */
    public void setTag(Object tag) {
        mTag = tag;
    }

    /**
     * Returns this context's tag.
     * 
     * @return the Object stored in this context as a tag,
     *         or {@code null} if not set
     * 
     * @see #setTag(Object)
     * @since 3.0.0
     */
    public Object getTag() {
        return mTag;
    }

    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    /**
     * Execute on the so called framework thread. For now this is mostly for
     * internal use. To actually enable the use of this framework thread you
     * should derive from the GVRMain base class instead of GVRMain.
     */
    public void runOnTheFrameworkThread(final Runnable runnable) {
        mHandler.post(runnable);
    }

    /**
     * Show a toast-like message for 3 seconds
     *
     * @param message
     */
    public void showToast(final String message) {
        showToast(message, 3f);
    }

    /**
     * Show a toast-like message for the specified duration
     *
     * @param message
     * @param duration in seconds
     */
    public void showToast(final String message, float duration) {
        final float quadWidth = 1.2f;
        final GVRTextViewSceneObject toastSceneObject = new GVRTextViewSceneObject(this, quadWidth, quadWidth / 5,
                message);

        toastSceneObject.setTextSize(6);
        toastSceneObject.setTextColor(Color.WHITE);
        toastSceneObject.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
        toastSceneObject.setBackgroundColor(Color.DKGRAY);
        toastSceneObject.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.REALTIME);

        final GVRTransform t = toastSceneObject.getTransform();
        t.setPositionZ(-1.5f);

        final GVRRenderData rd = toastSceneObject.getRenderData();
        final float finalOpacity = 0.7f;
        rd.getMaterial().setOpacity(0);
        rd.setRenderingOrder(2 * GVRRenderData.GVRRenderingOrder.OVERLAY);
        rd.setDepthTest(false);

        final GVRCameraRig rig = getMainScene().getMainCameraRig();
        rig.addChildObject(toastSceneObject);

        final GVRMaterialAnimation fadeOut = new GVRMaterialAnimation(rd.getMaterial(), duration / 4.0f) {
            @Override
            protected void animate(GVRHybridObject target, float ratio) {
                final GVRMaterial material = (GVRMaterial) target;
                material.setOpacity(finalOpacity - ratio * finalOpacity);
            }
        };
        fadeOut.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation animation) {
                rig.removeChildObject(toastSceneObject);
            }
        });

        final GVRMaterialAnimation fadeIn = new GVRMaterialAnimation(rd.getMaterial(), 3.0f * duration / 4.0f) {
            @Override
            protected void animate(GVRHybridObject target, float ratio) {
                final GVRMaterial material = (GVRMaterial) target;
                material.setOpacity(ratio * finalOpacity);
            }
        };
        fadeIn.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation animation) {
                getAnimationEngine().start(fadeOut);
            }
        });

        getAnimationEngine().start(fadeIn);
    }

    /**
     * Our {@linkplain GVRReference references} are placed on this queue, once
     * they've been finalized
     */
    private ReferenceQueue<GVRHybridObject> mReferenceQueue = new ReferenceQueue<GVRHybridObject>();
    /**
     * We need hard references to {@linkplain GVRReference our references} -
     * otherwise, the references get garbage collected (usually before their
     * objects) and never get enqueued.
     */
    private Set<GVRReference> mReferenceSet = new HashSet<GVRReference>();

    protected final void finalizeUnreachableObjects() {
        GVRReference reference;
        while (null != (reference = (GVRReference)mReferenceQueue.poll())) {
            reference.close(mReferenceSet);
        }
    }

    final static class UndertakerThread extends Thread {
        private final ReferenceQueue<GVRHybridObject> referenceQueue;
        private final Set<GVRReference> referenceSet;

        UndertakerThread(final ReferenceQueue<GVRHybridObject> referenceQueue, final Set<GVRReference> referenceSet, final String threadName) {
            super(threadName);
            this.referenceQueue = referenceQueue;
            this.referenceSet = referenceSet;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    GVRReference reference = (GVRReference)referenceQueue.remove();
                    reference.close(referenceSet);

                    synchronized (referenceSet) {
                        if (0 == referenceSet.size()) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    //ignore; nobody has a handle to this thread, nobody can and is supposed to interrupt it
                }
            }
        }
    }

    void onDestroy() {
        if (null != mHandlerThread) {
            mHandlerThread.getLooper().quitSafely();
        }

        final String threadName = "Undertaker-" + Integer.toHexString(hashCode());
        new UndertakerThread(mReferenceQueue, mReferenceSet, threadName).start();

        mReferenceQueue = null;
        mReferenceSet = null;
    }

    static final class GVRReference extends PhantomReference<GVRHybridObject> {
        private long mNativePointer;
        private final List<NativeCleanupHandler> mCleanupHandlers;

        private GVRReference(GVRHybridObject object, long nativePointer, List<NativeCleanupHandler> cleanupHandlers, final ReferenceQueue<GVRHybridObject> referenceQueue) {
            super(object, referenceQueue);

            mNativePointer = nativePointer;
            mCleanupHandlers = cleanupHandlers;
        }

        private void close(final Set<GVRReference> referenceSet) {
            close(referenceSet, true);
        }

        private void close(final Set<GVRReference> referenceSet, boolean removeFromSet) {
            synchronized (referenceSet) {
                if (mNativePointer != 0) {
                    if (mCleanupHandlers != null) {
                        for (NativeCleanupHandler handler : mCleanupHandlers) {
                            handler.nativeCleanup(mNativePointer);
                        }
                    }
                    NativeHybridObject.delete(mNativePointer);
                    mNativePointer = 0;
                }

                if (removeFromSet) {
                    referenceSet.remove(this);
                }
            }
        }
    }

    final void registerHybridObject(GVRHybridObject gvrHybridObject, long nativePointer, List<NativeCleanupHandler> cleanupHandlers) {
        synchronized (mReferenceSet) {
            mReferenceSet.add(new GVRReference(gvrHybridObject, nativePointer, cleanupHandlers, mReferenceQueue));
        }
    }

    /**
     * Explicitly close()ing an object is going to be relatively rare - most
     * native memory will be freed when the owner-objects are garbage collected.
     * Doing a lookup in these rare cases means that we can avoid giving every @link
     * {@link GVRHybridObject} a hard reference to its {@link GVRReference}.
     */
    final GVRReference findReference(long nativePointer) {
        for (GVRReference reference : mReferenceSet) {
            if (reference.mNativePointer == nativePointer) {
                return reference;
            }
        }
        return null;
    }

}
