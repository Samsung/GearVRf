package org.gearvrf.widgetplugin;

import java.lang.reflect.Method;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.IActivityEvents;
import org.gearvrf.IScriptEvents;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidAudio;
import com.badlogic.gdx.backends.android.AndroidClipboard;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.AndroidInputFactory;
import com.badlogic.gdx.backends.android.AndroidNet;
import com.badlogic.gdx.backends.android.AndroidPreferences;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;

/**
 * This provides GVR (libGDX) widget lifecycle and context management and brings
 * it together with GVR activity context. Base activity for GVR apps which use
 * GVRWidgets
 */
public class GVRWidgetPlugin implements AndroidApplicationBase {

    static {
        GdxNativesLoader.load();
    }
    protected GLSurfaceView mWidgetView;

    private GVRWidgetInputDispatcher mInputDispatcher = new GVRWidgetInputDispatcher();

    protected AndroidGraphics mGraphics;

    protected int mViewWidth;
    protected int mViewHeight;

    protected AndroidAudio mAudio;
    protected AndroidFiles mFiles;
    protected AndroidNet mNet;
    protected GVRScript mScript;
    protected GVRWidget mWidget;

    protected ApplicationListener mListener;
    public Handler mHandler;
    protected boolean mFirstResume = true;
    protected final Array<Runnable> mRunnables = new Array<Runnable>();
    protected final Array<Runnable> mExecutedRunnables = new Array<Runnable>();
    protected final Array<LifecycleListener> mLifecycleListeners = new Array<LifecycleListener>();
    private final Array<AndroidEventListener> mAndroidEventListeners = new Array<AndroidEventListener>();
    protected boolean mUseImmersiveMode = false;
    protected boolean mHideStatusBar = false;
    private int mWasFocusChanged = -1;
    private boolean mIsWaitingForAudio = false;
    protected int mFBOTextureId = 0;
    private Object mSync = new Object();
    private EGLContext mEGLContext;
    private GVRActivity mActivity;

    private IActivityEvents mActivityEventsListener = new GVREventListeners.ActivityEvents() {
        @Override
        public void onPause() {
            if(mGraphics != null){
            boolean isContinuous = mGraphics.isContinuousRendering();
            boolean isContinuousEnforced = AndroidGraphics.enforceContinuousRendering;

            // from here we don't want non continuous rendering
            AndroidGraphics.enforceContinuousRendering = true;
            mGraphics.setContinuousRendering(true);
            // calls to setContinuousRendering(false) from other thread (ex:
            // GLThread)
            // will be ignored at this point...
            mGraphics.pause();

            mInputDispatcher.getInput().onPause();

            if (mActivity.isFinishing()) {
                mGraphics.clearManagedCaches();
                mGraphics.destroy();
            }

            AndroidGraphics.enforceContinuousRendering = isContinuousEnforced;
            mGraphics.setContinuousRendering(isContinuous);

            mGraphics.onPauseGLSurfaceView();
            }
        }

        @Override
        public void onSetScript(GVRScript script) {
            script.getEventReceiver().addListener(mScriptEventsListener);
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            useImmersiveMode(GVRWidgetPlugin.this.mUseImmersiveMode);
            hideStatusBar(GVRWidgetPlugin.this.mHideStatusBar);
            if (hasFocus) {
                GVRWidgetPlugin.this.mWasFocusChanged = 1;
                if (GVRWidgetPlugin.this.mIsWaitingForAudio) {
                    GVRWidgetPlugin.this.mAudio.resume();
                    GVRWidgetPlugin.this.mIsWaitingForAudio = false;
                }
            } else {
                GVRWidgetPlugin.this.mWasFocusChanged = 0;
            }
        }

        @Override
        public void onConfigurationChanged(Configuration config) {
            boolean keyboardAvailable = false;
            if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
                keyboardAvailable = true;
            mInputDispatcher.getInput().keyboardAvailable = keyboardAvailable;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            // forward events to our listeners if there are any installed
            synchronized (mAndroidEventListeners) {
                for (int i = 0; i < mAndroidEventListeners.size; i++) {
                    mAndroidEventListeners.get(i).onActivityResult(requestCode,
                            resultCode, data);
                }
            }
        }        
    };

    private IScriptEvents mScriptEventsListener = new GVREventListeners.ScriptEvents() {
        @Override
        public void onEarlyInit(GVRContext context) {
            initWidget();
        }  
    };

    public GVRWidgetPlugin(GVRActivity activity) {
        mActivity = activity;
        activity.getEventReceiver().addListener(mActivityEventsListener);
    }

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)}
     * method. It sets up all the things necessary to get input, render via
     * OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
     * 
     * @param listener
     *            the {@link ApplicationListener} implementing the program logic
     **/
    public void initialize(ApplicationListener listener) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(listener, config, null);
    }

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)}
     * method. It sets up all the things necessary to get input, render via
     * OpenGL and so on. You can configure other aspects of the application with
     * the rest of the fields in the {@link AndroidApplicationConfiguration}
     * instance.
     * 
     * @param listener
     *            the {@link ApplicationListener} implementing the program logic
     * @param config
     *            the {@link AndroidApplicationConfiguration}, defining various
     *            settings of the application (use accelerometer, etc.).
     */
    public void initialize(ApplicationListener listener,
            AndroidApplicationConfiguration config, EGLContext sharedcontext) {
        init(listener, config, false, sharedcontext);
    }

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)}
     * method. It sets up all the things necessary to get input, render via
     * OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
     * <p>
     * Note: you have to add the returned view to your layout!
     * 
     * @param listener
     *            the {@link ApplicationListener} implementing the program logic
     * @return the GLSurfaceView of the application
     */
    public View initializeForView(ApplicationListener listener) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        return initializeForView(listener, config, null);
    }

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)}
     * method. It sets up all the things necessary to get input, render via
     * OpenGL and so on. You can configure other aspects of the application with
     * the rest of the fields in the {@link AndroidApplicationConfiguration}
     * instance.
     * <p>
     * Note: you have to add the returned view to your layout!
     * 
     * @param listener
     *            the {@link ApplicationListener} implementing the program logic
     * @param config
     *            the {@link AndroidApplicationConfiguration}, defining various
     *            settings of the application (use accelerometer, etc.).
     * @return the GLSurfaceView of the application
     */
    public View initializeForView(ApplicationListener listener,
            AndroidApplicationConfiguration config, EGLContext sharedcontext) {
        init(listener, config, true, sharedcontext);
        return mGraphics.getView();
    }

    private void init(ApplicationListener listener,
            AndroidApplicationConfiguration config, boolean isForView,
            EGLContext sharedcontext) {
        // if (this.getVersion() < MINIMUM_SDK) {
        // throw new GdxRuntimeException("LibGDX requires Android API Level " +
        // MINIMUM_SDK + " or later.");
        // }
        
        mGraphics = new AndroidGraphics(
                this,
                config,
                config.resolutionStrategy == null ? new FillResolutionStrategy()
                        : config.resolutionStrategy, sharedcontext);

        mInputDispatcher.setInput(AndroidInputFactory.newAndroidInput(this,
                mActivity, mGraphics.getView(), config));        
        mAudio = new AndroidAudio(mActivity, config);
        mActivity.getFilesDir(); // workaround for Android bug #10515463
        mFiles = new AndroidFiles(mActivity.getAssets(), mActivity.getFilesDir()
                .getAbsolutePath());
        mNet = new AndroidNet(this);
        this.mListener = listener;
        this.mHandler = new Handler();
        this.mUseImmersiveMode = config.useImmersiveMode;
        this.mHideStatusBar = config.hideStatusBar;

        // Add a specialized audio lifecycle listener
        addLifecycleListener(new LifecycleListener() {

            @Override
            public void resume() {
                // No need to resume audio here
            }

            @Override
            public void pause() {
                mAudio.pause();
            }

            @Override
            public void dispose() {
                mAudio.dispose();
            }
        });

        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();
        Gdx.net = this.getNet();

        if (!isForView) {
            try {
                mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            } catch (Exception ex) {
                log("AndroidApplication",
                        "Content already displayed, cannot request FEATURE_NO_TITLE",
                        ex);
            }
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mActivity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mActivity.setContentView(mGraphics.getView(), createLayoutParams());
        }

        createWakeLock(config.useWakelock);
        hideStatusBar(this.mHideStatusBar);
        useImmersiveMode(this.mUseImmersiveMode);
        if (this.mUseImmersiveMode
                && getVersion() >= Build.VERSION_CODES.KITKAT) {
            try {
                Class<?> vlistener = Class
                        .forName("com.badlogic.gdx.backends.android.AndroidVisibilityListener");
                Object o = vlistener.newInstance();
                Method method = vlistener.getDeclaredMethod("createListener",
                        AndroidApplicationBase.class);
                method.invoke(o, this);
            } catch (Exception e) {
                log("AndroidApplication",
                        "Failed to create AndroidVisibilityListener", e);
            }
        }
    }

    protected FrameLayout.LayoutParams createLayoutParams() {
        // FrameLayout.LayoutParams layoutParams = new
        // FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        // android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                mViewWidth, mViewHeight);
        // layoutParams.gravity = Gravity.CENTER;
        return layoutParams;
    }

    public void setViewSize(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
    }

    protected void createWakeLock(boolean use) {
        if (use) {
            mActivity.getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected void hideStatusBar(boolean hide) {
        if (!hide || getVersion() < 11)
            return;

        View rootView = mActivity.getWindow().getDecorView();

        try {
            Method m = View.class.getMethod("setSystemUiVisibility", int.class);
            if (getVersion() <= 13)
                m.invoke(rootView, 0x0);
            m.invoke(rootView, 0x1);
        } catch (Exception e) {
            log("AndroidApplication", "Can't hide status bar", e);
        }
    }

    @TargetApi(19)
    @Override
    public void useImmersiveMode(boolean use) {
        if (!use || getVersion() < Build.VERSION_CODES.KITKAT)
            return;

        View view = mActivity.getWindow().getDecorView();
        try {
            Method m = View.class.getMethod("setSystemUiVisibility", int.class);
            int code = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            m.invoke(view, code);
        } catch (Exception e) {
            log("AndroidApplication", "Can't set immersive mode", e);
        }
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return mListener;
    }

    @Override
    public Audio getAudio() {
        return mAudio;
    }

    @Override
    public Files getFiles() {
        return mFiles;
    }

    @Override
    public AndroidGraphics getGraphics() {
        return mGraphics;
    }

    @Override
    public AndroidInput getInput() {
        return mInputDispatcher.getInput();
    }

    @Override
    public Net getNet() {
        return mNet;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.Android;
    }

    @Override
    public int getVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    @Override
    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize();
    }

    @Override
    public Preferences getPreferences(String name) {
        return new AndroidPreferences(mActivity.getSharedPreferences(name,
                Context.MODE_PRIVATE));
    }

    AndroidClipboard clipboard;

    @Override
    public Clipboard getClipboard() {
        if (clipboard == null) {
            clipboard = new AndroidClipboard(mActivity);
        }
        return clipboard;
    }

    @Override
    public void postRunnable(Runnable runnable) {
        synchronized (mRunnables) {
            mRunnables.add(runnable);
            Gdx.graphics.requestRendering();
        }
    }

    @Override
    public void exit() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mActivity.finish();
            }
        });
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        synchronized (mLifecycleListeners) {
            mLifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized (mLifecycleListeners) {
            mLifecycleListeners.removeValue(listener, true);
        }
    }

    /**
     * Adds an event listener for Android specific event such as
     * onActivityResult(...).
     */
    public void addAndroidEventListener(AndroidEventListener listener) {
        synchronized (mAndroidEventListeners) {
            mAndroidEventListeners.add(listener);
        }
    }

    /**
     * Removes an event listener for Android specific event such as
     * onActivityResult(...).
     */
    public void removeAndroidEventListener(AndroidEventListener listener) {
        synchronized (mAndroidEventListeners) {
            mAndroidEventListeners.removeValue(listener, true);
        }
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public Array<Runnable> getRunnables() {
        return mRunnables;
    }

    @Override
    public Array<Runnable> getExecutedRunnables() {
        return mExecutedRunnables;
    }

    @Override
    public Array<LifecycleListener> getLifecycleListeners() {
        return mLifecycleListeners;
    }

    @Override
    public Window getApplicationWindow() {
        return mActivity.getWindow();
    }

    @Override
    public Handler getHandler() {
        return this.mHandler;
    }

    protected void initWidget() {
        mEGLContext = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        syncNotify();
        while (!isInitialised()) {
           syncWait();
        }
    }

    public View getWidgetView() {
        return mWidgetView;
    }

    public int getWidth() {
        return mViewWidth;
    }

    public int getHeight() {
        return mViewHeight;
    }

    protected boolean isInitialised() {
        return mWidget.isInitialised();
    }

    public GVRScript getScript() {
        return mScript;
    }

    public int getTextureId() {
        return mWidget.getTexId();
    }

    public void setCurrentScript(GVRScript script) {
        mScript = script;
    }

    public void setPickedObject(GVRSceneObject obj) {
        mInputDispatcher.setPickedObject(obj);
    }

    public GVRSceneObject getPickedObject() {
        return mInputDispatcher.getPickedObject();
    }

    public void initializeWidget(GVRWidget widget) {
        mWidget = widget;
        mWidget.setSyncObject(mSync);
        Thread thread = new Thread() {
            @Override
            public void run() {
                synchronized (mSync) {
                    try {
                        while (mEGLContext == null) {
                            mSync.wait();
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                doResume(mWidget);
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();

    }

    private void doResume(GVRWidget widget) {
        mWidgetView = (GLSurfaceView) initializeForView(widget,
                new AndroidApplicationConfiguration(), mEGLContext);

        mActivity.addContentView(mWidgetView, createLayoutParams());
        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();
        Gdx.net = this.getNet();
        mGraphics.setFramebuffer(mViewWidth, mViewHeight);

        mInputDispatcher.getInput().onResume();
        if (mGraphics != null) {
            mGraphics.onResumeGLSurfaceView();
        }

        if (!mFirstResume) {
            mGraphics.resume();
        } else
            mFirstResume = false;

        this.mIsWaitingForAudio = true;
        if (this.mWasFocusChanged == 1 || this.mWasFocusChanged == -1) {
            // this.audio.resume();
            this.mIsWaitingForAudio = false;
        }
    }

    protected void syncNotify() {
        // TODO Auto-generated method stub
        synchronized (mSync) {
            mSync.notifyAll();
        }
    }

    protected void syncWait() {
        // TODO Auto-generated method stub
        synchronized (mSync) {
            try {
                mSync.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void debug(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void debug(String arg0, String arg1, Throwable arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(String arg0, String arg1, Throwable arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getLogLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void log(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void log(String arg0, String arg1, Throwable arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLogLevel(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public WindowManager getWindowManager() {
        return mActivity.getWindowManager();
    }

    @Override
    public void runOnUiThread(Runnable arg0) {
        mActivity.runOnUiThread(arg0);
    }

    @Override
    public void startActivity(Intent arg0) {
        mActivity.startActivity(arg0);
    }

    public GVRActivity getGVRActivity() {
        return mActivity;
    }
    
    public boolean dispatchTouchEvent(MotionEvent event) {
    	return  mInputDispatcher.dispatchEvent(event, mWidgetView) ? true:
    	 mActivity.onTouchEvent(event);
    	
    }
    
   
}