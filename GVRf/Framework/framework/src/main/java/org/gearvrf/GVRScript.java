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

import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.script.IScriptable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Extend this class to create a GVRF application.
 * <p>
 * All methods are called from the GL thread so it is safe to make GL calls
 * either directly or indirectly (through GVRF methods). The GL thread runs at
 * {@linkplain Thread#MAX_PRIORITY top priority:} Android systems have many
 * processes running at any time, and all {@linkplain Thread#NORM_PRIORITY
 * default priority} threads compete with each other.
 */
public abstract class GVRScript implements IScriptEvents, IScriptable, IEventReceiver {

    // private static final String TAG = Log.tag(GVRScript.class);

    /**
     * Default minimum time for splash screen to show: returned by
     * {@link #getSplashDisplayTime()}.
     */
    private static final float DEFAULT_SPLASH_DISPLAY_SECONDS = 5f;

    /**
     * Default fade-to-transparency time for the splash screen: returned by
     * {@link #getSplashFadeTime()}.
     */
    private static final float DEFAULT_SPLASH_FADE_SECONDS = 0.9f;

    /** Splash screen, distance from the camera. */
    private static final float DEFAULT_SPLASH_Z = -1.25f;

    private final GVREventReceiver mEventReceiver = new GVREventReceiver(this);

    /*
     * Core methods, that you must override.
     */

    /**
     * Called before {@link #onInit(GVRContext).
     *
     * This is used for initializing plug-ins and other early components.
     */
    @Override
    public void onEarlyInit(GVRContext gvrContext) {
    }

    /**
     * Called when the GL surface is created, when your app is loaded.
     * 
     * This is where you should build your initial scene graph. Any expensive
     * calls you make here are 'hidden' (in the sense that they won't cause the
     * app to skip any frames) but they <em>will</em> still affect app startup
     * time: use lazy-create patterns where you can, and/or use the asynchronous
     * resource loading methods in {@link GVRContext} instead of the synchronous
     * ones.
     * 
     * @throws Throwable
     *             {@code onInit()} routines typically need to load various
     *             resources. Some of the Android resource-loading code throws
     *             exceptions (especially when you are loading files from the
     *             {@code assets} folder). If you don't catch these exceptions -
     *             and just let them propagate out of {@code onInit()} - GVRF
     *             will log the exception and shutdown your app.
     * 
     *             <p>
     *             This is probably <em>not</em> the behavior you want if your
     *             resources may fail to load because of (say) network issues,
     *             but it is just fine for handling development-time issues like
     *             typing {@code "mesh.obi"} instead of {@code "mesh.obj"}.
     */
    @Override
    public abstract void onInit(GVRContext gvrContext) throws Throwable;

    /**
     * Called after {@code onInit()} has finished.
     *
     * This is where you do some post-processing of the initial scene graph
     * created in the method {@link #onInit(GVRContext)}, a listener added to
     * {@link GVREventReceiver} or a {@link GVRScriptFile} attached to this {@link
     * GVRScript} using {@link GVRScriptManager#attachScript}.
     */
    @Override
    public void onAfterInit() {
    }

    /**
     * Called every frame.
     * 
     * This is where you start animations, and where you add or change
     * {@linkplain GVRSceneObject scene objects.} Keep this method as short as
     * possible, to avoid dropping any frames.
     * 
     * <p>
     * This is the 3rd user-definable step in drawing a frame:
     * <ul>
     * <li>Process the {@link GVRContext#runOnGlThread(Runnable)} queue
     * <li>Run all
     * {@linkplain GVRContext#registerDrawFrameListener(GVRDrawFrameListener)
     * registered frame listeners}
     * <li><b>Call your {@code onStep()} handler</b>.
     * </ul>
     * 
     * After these steps, {@link GVRViewManager} does stereo rendering and
     * applies the lens distortion.
     */
    @Override
    public abstract void onStep();

    @Override
    public GVREventReceiver getEventReceiver() {
        return mEventReceiver;
    }

    /*
     * Splash screen support: methods to call or overload to change the default
     * splash screen behavior
     */

    private GVRViewManager mViewManager;

    /**
     * Whether the splash screen should be displayed, and for how long.
     * 
     * Returned by {@link #getSplashMode}.
     * 
     * @since 1.6.4
     */
    public enum SplashMode {
        /**
         * The splash screen will be shown before
         * {@link GVRScript#onInit(GVRContext) onInit()} and hidden before the
         * first call to {@link GVRScript#onStep() onStep()}
         */
        AUTOMATIC,
        /**
         * The splash screen will be shown before
         * {@link GVRScript#onInit(GVRContext) onInit()} and will remain up
         * until you call {@link GVRScript#closeSplashScreen()}
         */
        MANUAL,
        /**
         * The splash screen will not be shown at all. The screen will go black
         * until your {@link GVRScript#onInit(GVRContext) onInit()} returns, at
         * which point it will show any objects you have created, over whatever
         * {@linkplain GVRCamera#setBackgroundColor(int) background color} you
         * have set.
         */
        NONE
    }

    /**
     * Override this method to change the splash mode from the default
     * {@linkplain SplashMode#AUTOMATIC automatic} mode.
     * 
     * @return One of the {@link SplashMode} enums.
     * 
     * @since 1.6.4
     */
    public SplashMode getSplashMode() {
        return SplashMode.AUTOMATIC;
    }

    /**
     * The minimum amount of time the splash screen will be visible, in seconds.
     * 
     * Override this method to change the default.
     * 
     * In {@linkplain SplashMode#AUTOMATIC AUTOMATIC} mode, the splash screen
     * will stay up for {@link #getSplashDisplayTime()} seconds. In
     * {@linkplain SplashMode#MANUAL MANUAL} mode, the splash screen will stay
     * up for <em>at least</em> {@link #getSplashDisplayTime()} seconds:
     * {@link #closeSplashScreen()} will not take effect until the splash screen
     * times out, even if you call it long before that timeout.
     * 
     * @return The minimum splash screen display time, in seconds.
     */
    public float getSplashDisplayTime() {
        return DEFAULT_SPLASH_DISPLAY_SECONDS;
    }

    /**
     * Splash screen fade time, in seconds.
     * 
     * Override this method to change the default.
     * 
     * @return Splash screen fade-out animation duration
     */
    public float getSplashFadeTime() {
        return DEFAULT_SPLASH_FADE_SECONDS;
    }

    /**
     * In {@linkplain SplashMode#MANUAL manual mode,} the splash screen will
     * stay up until you call this method.
     * 
     * Calling {@link #closeSplashScreen()} before the
     * {@linkplain #getSplashDisplayTime() display time} has elapsed will set a
     * flag, but the splash screen will stay up until the timeout; after the
     * timeout, the splash screen will close as soon as you call
     * {@link #closeSplashScreen()}.
     * 
     * @since 1.6.4
     */
    public final void closeSplashScreen() {
        mViewManager.closeSplashScreen();
    }

    /**
     * Override this method to supply a custom splash screen image.
     * 
     * @param gvrContext
     *            The new {@link GVRContext}
     * @return Texture to display
     * 
     * @since 1.6.4
     */
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource( //
                gvrContext.getContext().getResources(), //
                R.drawable.__default_splash_screen__);
        return new GVRBitmapTexture(gvrContext, bitmap);
    }

    /**
     * Override this method to supply a custom splash screen mesh.
     * 
     * The default is a 1x1 quad.
     * 
     * @param gvrContext
     *            The new {@link GVRContext}
     * @return Mesh to use with {@link #getSplashTexture(GVRContext)} and
     *         {@link #getSplashShader(GVRContext)}.
     * 
     * @since 1.6.4
     */
    public GVRMesh getSplashMesh(GVRContext gvrContext) {
        return gvrContext.createQuad(1f, 1f);
    }

    /**
     * Override this method to supply a custom splash screen shader.
     * 
     * The default is the built-in {@linkplain GVRMaterial.GVRShaderType.Unlit
     * unlit shader.}
     * 
     * @param gvrContext
     *            The new {@link GVRContext}
     * @return Shader to use with {@link #getSplashTexture(GVRContext)} and
     *         {@link #getSplashMesh(GVRContext)}.
     * 
     * @since 1.6.4
     */
    public GVRMaterialShaderId getSplashShader(GVRContext gvrContext) {
        return GVRMaterial.GVRShaderType.Texture.ID;
    }

    /**
     * Override this method to change the default splash screen size or
     * position.
     * 
     * This method will be called <em>before</em> {@link #onInit(GVRContext)
     * onInit()} and before the normal render pipeline starts up. In particular,
     * this means that any {@linkplain GVRAnimation animations} will not start
     * until the first {@link #onStep()} and normal rendering starts.
     * 
     * @param splashScreen
     *            The splash object created from
     *            {@link #getSplashTexture(GVRContext)},
     *            {@link #getSplashMesh(GVRContext)}, and
     *            {@link #getSplashShader(GVRContext)}.
     * 
     * @since 1.6.4
     */
    public void onSplashScreenCreated(GVRSceneObject splashScreen) {
        GVRTransform transform = splashScreen.getTransform();
        transform.setPosition(0, 0, DEFAULT_SPLASH_Z);
    }

    SplashScreen createSplashScreen(GVRViewManager viewManager) {
        if (getSplashMode() == SplashMode.NONE) {
            return null;
        }

        this.mViewManager = viewManager;
        SplashScreen splashScreen = new SplashScreen(viewManager, //
                getSplashMesh(viewManager), //
                getSplashTexture(viewManager), //
                getSplashShader(viewManager), //
                this);
        splashScreen.getRenderData().setRenderingOrder(
                GVRRenderData.GVRRenderingOrder.OVERLAY);
        onSplashScreenCreated(splashScreen);
        return splashScreen;
    }
}
