package org.gearvrf;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GVRGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private final static String TAG = "GVRGLSurfaceView";

    private final static boolean DEBUG = false;

    private final static boolean LOG_ATTACH_DETACH = false;
    private final static boolean LOG_THREADS = false;
    private final static boolean LOG_PAUSE_RESUME = false;
    private final static boolean LOG_SURFACE = false;
    private final static boolean LOG_RENDERER = false;
    private final static boolean LOG_RENDERER_DRAW_FRAME = false;
    private final static boolean LOG_EGL = false;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    public final static int DEBUG_CHECK_GL_ERROR = 1;
    public final static int DEBUG_LOG_GL_CALLS = 2;

    public GVRGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public GVRGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mGLThread != null) {
                mGLThread.requestExitAndWait();
            }
        } finally {
            super.finalize();
        }
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void setGLWrapper(GLWrapper glWrapper) {
        mGLWrapper = glWrapper;
    }

    public void setDebugFlags(int debugFlags) {
        mDebugFlags = debugFlags;
    }

    public int getDebugFlags() {
        return mDebugFlags;
    }

    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        mPreserveEGLContextOnPause = preserveOnPause;
    }

    public boolean getPreserveEGLContextOnPause() {
        return mPreserveEGLContextOnPause;
    }

    public void setRenderer(Renderer renderer) {
        checkRenderThreadState();

        /*
         * check if the custom config chooser has been set correctly. If not
         * set, it is a programming error that requires abort the app.
         */
        if (mEGLConfigChooser == null) {
            Log.e(TAG, "setRenderer (mEGLConfigChooser == null)");
            return;
        }
        if (mEGLContextFactory == null) {
            Log.e(TAG, "setRenderer (mEGLContextFactory == null)");
            return;
        }
        if (mEGLWindowSurfaceFactory == null) {
            Log.e(TAG, "setRenderer (mEGLWindowSurfaceFactory == null)");
            return;
        }
        mRenderer = renderer;
        mGLThread = new GLThread(mThisWeakRef);
        mGLThread.start();
    }
    
    public void setEGLContextFactory(EGLContextFactory factory) {
        checkRenderThreadState();
        mEGLContextFactory = factory;
    }

    public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
        checkRenderThreadState();
        mEGLWindowSurfaceFactory = factory;
    }

    public void setEGLConfigChooser(EGLConfigChooser configChooser) {
        checkRenderThreadState();
        mEGLConfigChooser = configChooser;
    }

    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
        mEGLContextClientVersion = version;
    }

    public void setRenderMode(int renderMode) {
        mGLThread.setRenderMode(renderMode);
    }

    public int getRenderMode() {
        return mGLThread.getRenderMode();
    }

    public void requestRender(int param1) {
        if (DEBUG) Log.d(TAG, "requestRender");
        requestRender(param1, 0L);
    }

    public void requestRender(int param1, long param2) {
        if (DEBUG) Log.d(TAG, "requestRender" + param1 + "," + param2);
        mGLThread.requestRender(param1, param2);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mGLThread.surfaceCreated();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return
        mGLThread.surfaceDestroyed();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        mGLThread.onWindowResize(w, h);
    }

    public void onPause() {
        Log.v(TAG, " onPause ");
        mGLThread.onPause();
    }

    public void onResume() {
        Log.v(TAG, " onResume ");
        mGLThread.onResume();
    }

    public void queueEvent(Runnable r) {
        mGLThread.queueEvent(r);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow reattach =" + mDetached);
        }
        if (mDetached && (mRenderer != null)) {
            int renderMode = RENDERMODE_CONTINUOUSLY;
            if (mGLThread != null) {
                renderMode = mGLThread.getRenderMode();
            }
            mGLThread = new GLThread(mThisWeakRef);
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                mGLThread.setRenderMode(renderMode);
            }
            mGLThread.start();
        }
        mDetached = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow");
        }
        if (mGLThread != null) {
            mGLThread.requestExitAndWait();
        }
        mDetached = true;
        super.onDetachedFromWindow();
    }

    void glStarted() {
    }
    
    void glInitialized(GL10 gl) {
    }
    
    void glRendered(long param2) {
    }
    
    void glForEyeDrawMade() {
    }
    
    void requestGLForEyeDraw() {
        mGLThread.requestGLForEyeDraw();
    }
    
    void requestGLForEyeShow() {
        mGLThread.requestGLForEyeShow();
    }
    
    void requestSwapShow() {
        mGLThread.requestSwapShow();
    }

    // ----------------------------------------------------------------------

    public interface GLWrapper {
        GL wrap(GL gl);
    }

    public interface Renderer {
        void onSurfaceCreated(GL10 gl, EGLConfig config);
        
        void onSurfaceChanged(GL10 gl, int width, int height);
        
        void onDrawFrame(int bufferIdx);

        // Moved to direct call and no customization allowed.
        //
        // /**
        // * Show the current frame.
        // */
        // void showFrame(int bufferIdx, GVRFrameRegulator.HeartbeatInfo
        // heartbeat);
    }

    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig);
        void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);
    }

    public interface EGLWindowSurfaceFactory {
        EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config,
                Object nativeWindow);
        void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface);
    }

    public interface EGLConfigChooser {
        EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
    }

    private abstract class BaseConfigChooser
            implements EGLConfigChooser {
        public BaseConfigChooser(int[] configSpec) {
            mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException(
                        "No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs);

        protected int[] mConfigSpec;

        private int[] filterConfigSpec(int[] configSpec) {
            if (mEGLContextClientVersion != 2) {
                return configSpec;
            }
            int len = configSpec.length;
            int[] newConfigSpec = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len-1);
            newConfigSpec[len-1] = EGL10.EGL_RENDERABLE_TYPE;
            newConfigSpec[len] = 4; /* EGL_OPENGL_ES2_BIT */
            newConfigSpec[len+1] = EGL10.EGL_NONE;
            return newConfigSpec;
        }
    }

    static class EglHelper {
        public EglHelper(WeakReference<GVRGLSurfaceView> GVRGLSurfaceViewWeakRef) {
            mGVRGLSurfaceViewWeakRef = GVRGLSurfaceViewWeakRef;
        }
        
        //=====================================================================

        private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        private static final int EGL_CONTEXT_PRIORITY_LEVEL_IMG = 0x3100;

        private static final int EGL_CONTEXT_PRIORITY_HIGH_IMG = 0x3101;

        private static final int EGL_CONTEXT_PRIORITY_MEDIUM_IMG = 0x3102;

        private static final int EGL_CONTEXT_PRIORITY_LOW_IMG = 0x3103;

        GL makeGLForEyeDraw(int width, int height) {
            Log.v(TAG, "makeGLForEyeDraw");

            int[] surfaceAttribs = {
                    EGL10.EGL_WIDTH, width, EGL10.EGL_HEIGHT, height, EGL10.EGL_NONE
            };

            // make sure call this on every restart
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            EGLSurface eglSurface = mEgl.eglCreatePbufferSurface(mEglDisplay, mEglConfig,
                    surfaceAttribs);
            if (eglSurface == null) {
                throw new RuntimeException("surface was null");
            }

            mEgl.eglMakeCurrent(mEglDisplay, eglSurface, eglSurface, mEglContext);

            // set the current parameters for any subsequent queries
            mEglSurfaceOff = eglSurface;

            GL gl = mEglContext.getGL();

            return gl;
        }

        void makeGLForEyeShow() {
            if (mEglSurface == null || mEglDisplay == null) {
                Log.e(TAG, "makeGLForEyeShow() failed (mEglSurface == null || mEglDisplay == null)");
                return;
            }

            int[] attrib_list = {
                    EGL_CONTEXT_CLIENT_VERSION, 3, 
                    EGL_CONTEXT_PRIORITY_LEVEL_IMG, EGL_CONTEXT_PRIORITY_HIGH_IMG,
                    EGL10.EGL_NONE
            };
            // create a new context, and set the current parameters for any
            // subsequent queries
            mEglContextEyeShow = mEgl.eglCreateContext(mEglDisplay, mEglConfig, mEglContext,
                    attrib_list);
            Log.v(TAG, "makeGLForEyeShow() created mEglContextEyeShow=" + mEglContextEyeShow);

            int[] values = new int[1];
            mEgl.eglQueryContext(mEglDisplay, mEglContextEyeShow, EGL_CONTEXT_PRIORITY_LEVEL_IMG,
                    values);
            int actualPriorityLevel = values[0];
            Log.d(TAG, "makeGLForEyeShow queried EGL_CONTEXT_PRIORITY_LEVEL_IMG: "
                    + actualPriorityLevel);

            mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContextEyeShow);
        }

        EGLContext getCurrentContext() {
            return mEgl.eglGetCurrentContext();
        }

        boolean swapShow() {
            if (DEBUG) Log.v(TAG, " swapShow()() -->");
            EGLSurface eglSurface = mEglSurface; // the surface in concern
            mEgl.eglSwapBuffers(mEglDisplay, eglSurface);
            checkEglError();

            return mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
        }

        void checkEglError() {
            final int error = mEgl.eglGetError();
            if (error != EGL10.EGL_SUCCESS) {
                Log.e(TAG, "EGL error = 0x" + Integer.toHexString(error));
            }
        }

        
        void finishAll() {
            /*
             * Cleans up all handles, including the current context, created so
             * far. It can be called more than one times. No need to separate
             * for each worker partially at this moment, the whole GL need to be
             * re-initialized anyway when they are about to resume.
             */

            if (mEglSurface != null) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);
                mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
                mEglSurface = null;
            }
            if (mEglSurfaceOff != null) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);
                mEgl.eglDestroySurface(mEglDisplay, mEglSurfaceOff);
                mEglSurfaceOff = null;
            }
            if (mEglContext != null) {
                mEgl.eglDestroyContext(mEglDisplay, mEglContext);
                mEglContext = null;
            }
            if (mEglContextEyeShow != null) {
                mEgl.eglDestroyContext(mEglDisplay, mEglContextEyeShow);
                mEglContextEyeShow = null;
            }
            if (mEglDisplay != null) {
                mEgl.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
        }
        
        //===========================================================================
        
        public void start() {
            if (LOG_EGL) {
                Log.w("EglHelper", "start() tid=" + Thread.currentThread().getId());
            }

            mEgl = (EGL10) EGLContext.getEGL();
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }

            int[] version = new int[2];
            if(!mEgl.eglInitialize(mEglDisplay, version)) {
                throw new RuntimeException("eglInitialize failed");
            }
            GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
            if (view == null) {
                mEglConfig = null;
                mEglContext = null;
            } else {
                mEglConfig = view.mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);
                mEglContext = view.mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);
            }
            if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
                mEglContext = null;
                throwEglException("createContext");
            }
            if (LOG_EGL) {
                Log.w("EglHelper", "createContext " + mEglContext + " tid=" + Thread.currentThread().getId());
            }

            mEglSurface = null;
        }

        public boolean createSurface() {
            if (LOG_EGL) {
                Log.w("EglHelper", "createSurface()  tid=" + Thread.currentThread().getId());
            }

            if (mEgl == null) {
                throw new RuntimeException("egl not initialized");
            }
            if (mEglDisplay == null) {
                throw new RuntimeException("eglDisplay not initialized");
            }
            if (mEglConfig == null) {
                throw new RuntimeException("mEglConfig not initialized");
            }

            destroySurfaceImp();

            GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
            if (view != null) {
                mEglSurface = view.mEGLWindowSurfaceFactory.createWindowSurface(mEgl,
                        mEglDisplay, mEglConfig, view.getHolder());
            } else {
                mEglSurface = null;
            }

            if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
                int error = mEgl.eglGetError();
                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                }
                return false;
            }

            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", mEgl.eglGetError());
                return false;
            }

            return true;
        }

        GL createGL() {
            GL gl = mEglContext.getGL();
            GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
            if (view != null) {
                if (view.mGLWrapper != null) {
                    gl = view.mGLWrapper.wrap(gl);
                }
            }
            return gl;
        }

        public int swap() {
            if (! mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                return mEgl.eglGetError();
            }
            return EGL10.EGL_SUCCESS;
        }

        public void destroySurface() {
            if (LOG_EGL) {
                Log.w("EglHelper", "destroySurface()  tid=" + Thread.currentThread().getId());
            }
            destroySurfaceImp();
        }

        private void destroySurfaceImp() {
            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);
                GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
                }
                mEglSurface = null;
            }
        }

        public void finish() {
            if (LOG_EGL) {
                Log.w("EglHelper", "finish() tid=" + Thread.currentThread().getId());
            }
            if (mEglContext != null) {
                GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
                }
                mEglContext = null;
            }
            if (mEglDisplay != null) {
                mEgl.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
        }

        private void throwEglException(String function) {
            throwEglException(function, mEgl.eglGetError());
        }

        public static void throwEglException(String function, int error) {
            String message = formatEglError(function, error);
            if (LOG_THREADS) {
                Log.e("EglHelper", "throwEglException tid=" + Thread.currentThread().getId() + " "
                        + message);
            }
            throw new RuntimeException(message);
        }

        public static void logEglErrorAsWarning(String tag, String function, int error) {
            Log.w(tag, formatEglError(function, error));
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: ";// + EGLLogWrapper.getErrorString(error);
        }

        private WeakReference<GVRGLSurfaceView> mGVRGLSurfaceViewWeakRef;
        EGL10 mEgl;
        EGLDisplay mEglDisplay;
        EGLSurface mEglSurface;
        EGLConfig mEglConfig;
        EGLContext mEglContext;
        EGLSurface mEglSurfaceOff;
        EGLContext mEglContextEyeShow;
    }

    static class GLThread extends Thread {
        GLThread(WeakReference<GVRGLSurfaceView> GVRGLSurfaceViewWeakRef) {
            super();
            mWidth = 0;
            mHeight = 0;
            mRequestRender = true;
            mRenderMode = RENDERMODE_CONTINUOUSLY;
            mGVRGLSurfaceViewWeakRef = GVRGLSurfaceViewWeakRef;
        }

        @Override
        public void run() {
            setName("GLThread " + getId());
            if (LOG_THREADS) {
                Log.i("GLThread", "starting tid=" + getId());
            }

            GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
            if (view != null) {
                if (LOG_RENDERER) {
                    Log.w("GLThread", "GLThread run()");
                }
                view.glStarted();
            }
            
            try {
                guardedRun();
            } catch (InterruptedException e) {
                // fall thru and exit normally
            } finally {
                sGLThreadManager.threadExiting(this);
            }
        }

        private void stopEglSurfaceLocked() {
            if (mHaveEglSurface) {
                mHaveEglSurface = false;
                mEglHelper.destroySurface();
            }
        }

        private void stopEglContextLocked() {
            if (mHaveEglContext) {
                mEglHelper.finish();
                mHaveEglContext = false;
                sGLThreadManager.releaseEglContextLocked(this);
            }
        }
        
        private void guardedRun() throws InterruptedException {
            mEglHelper = new EglHelper(mGVRGLSurfaceViewWeakRef);
            mHaveEglContext = false;
            mHaveEglSurface = false;
            try {
                GL10 gl = null;
                boolean createEglContext = false;
                boolean createEglSurface = false;
                boolean createGlInterface = false;
                boolean lostEglContext = false;
                boolean sizeChanged = false;
                boolean wantRenderNotification = false;
                boolean doRenderNotification = false;
                boolean askedToReleaseEglContext = false;
                int w = 0;
                int h = 0;
                Runnable event = null;

                while (true) {
                    synchronized (sGLThreadManager) {
                        while (true) {
                            if (LOG_PAUSE_RESUME) {
                                Log.i("GLThread", "while (true) { " );
                            }
                            if (mShouldExit) {
                                return;
                            }

                            if (! mEventQueue.isEmpty()) {
                                event = mEventQueue.remove(0);
                                break;
                            }
                            if (LOG_PAUSE_RESUME) {
                                Log.i("GLThread",  "boolean pausing = false; " );
                            }
                            // Update the pause state.
                            boolean pausing = false;
                            if (mPaused != mRequestPaused) {
                                pausing = mRequestPaused;
                                mPaused = mRequestPaused;
                                sGLThreadManager.notifyAll();
                                if (LOG_PAUSE_RESUME) {
                                    Log.i("GLThread", "mPaused is now " + mPaused + " tid=" + getId());
                                }
                            }

                            // Do we need to give up the EGL context?
                            if (mShouldReleaseEglContext) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "releasing EGL context because asked to tid=" + getId());
                                }
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                mShouldReleaseEglContext = false;
                                askedToReleaseEglContext = true;
                            }

                            // Have we lost the EGL context?
                            if (lostEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                lostEglContext = false;
                            }

                            // When pausing, release the EGL surface:
                            if (pausing && mHaveEglSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "releasing EGL surface because paused tid=" + getId());
                                }
                                stopEglSurfaceLocked();
                            }

                            // When pausing, optionally release the EGL Context:
                            if (pausing && mHaveEglContext) {
                                GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
                                boolean preserveEglContextOnPause = view == null ?
                                        false : view.mPreserveEGLContextOnPause;
                                if (!preserveEglContextOnPause || sGLThreadManager.shouldReleaseEGLContextWhenPausing()) {
                                    stopEglContextLocked();
                                    if (LOG_SURFACE) {
                                        Log.i("GLThread", "releasing EGL context because paused tid=" + getId());
                                    }
                                }
                            }

                            // When pausing, optionally terminate EGL:
                            if (pausing) {
                                if (sGLThreadManager.shouldTerminateEGLWhenPausing()) {
                                    mEglHelper.finish();
                                    if (LOG_SURFACE) {
                                        Log.i("GLThread", "terminating EGL because paused tid=" + getId());
                                    }
                                }
                            }

                            // Have we lost the SurfaceView surface?
                            if ((! mHasSurface) && (! mWaitingForSurface)) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "noticed surfaceView surface lost tid=" + getId());
                                }
                                if (mHaveEglSurface) {
                                    stopEglSurfaceLocked();
                                }
                                mWaitingForSurface = true;
                                mSurfaceIsBad = false;
                                sGLThreadManager.notifyAll();
                            }

                            // Have we acquired the surface view surface?
                            if (mHasSurface && mWaitingForSurface) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "noticed surfaceView surface acquired tid=" + getId());
                                }
                                mWaitingForSurface = false;
                                sGLThreadManager.notifyAll();
                            }

                            if (doRenderNotification) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "sending render notification tid=" + getId());
                                }
                                wantRenderNotification = false;
                                doRenderNotification = false;
                                mRenderComplete = true;
                                sGLThreadManager.notifyAll();
                            }

                            // -------------------
                            if (mGLForEyeDrawRequested) {
                                // a small pbuffer Surface
                                final int cOffWidth = 10;
                                final int cOffHeight = 10;
                                
                                mEglHelper.makeGLForEyeDraw(cOffWidth, cOffHeight);
                                GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
                                if (view != null) {
                                    view.glForEyeDrawMade();
                                }
                                mGLForEyeDrawRequested = false;
                            }
                            
                            // -------------------

                            // Ready to draw?
                            if (readyToDraw()) {
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "(readyToDraw())" );
                                }
                                // If we don't have an EGL context, try to acquire one.
                                if (! mHaveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false;
                                    } else if (sGLThreadManager.tryAcquireEglContextLocked(this)) {
                                        try {
                                            mEglHelper.start();
                                        } catch (RuntimeException t) {
                                            sGLThreadManager.releaseEglContextLocked(this);
                                            throw t;
                                        }
                                        mHaveEglContext = true;
                                        createEglContext = true;

                                        sGLThreadManager.notifyAll();
                                    }
                                }

                                if (mHaveEglContext && !mHaveEglSurface) {
                                    mHaveEglSurface = true;
                                    createEglSurface = true;
                                    createGlInterface = true;
                                    sizeChanged = true;
                                }

                                if (mHaveEglSurface) {
                                    if (mSizeChanged) {
                                        sizeChanged = true;
                                        w = mWidth;
                                        h = mHeight;
                                        wantRenderNotification = true;
                                        if (LOG_SURFACE) {
                                            Log.i("GLThread",
                                                    "noticing that we want render notification tid="
                                                    + getId());
                                        }

                                        // Destroy and recreate the EGL surface.
                                        createEglSurface = true;

                                        mSizeChanged = false;
                                    }
                                    mRequestRender = false;
                                    sGLThreadManager.notifyAll();
                                    break;
                                }
                            } else {
                                if (LOG_SURFACE) {
                                    Log.w("GLThread", "not readyToDraw()!");
                                }
                                // return back anyway to avoid blocking
                                GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
                                if (view != null) {
                                    view.glRendered(mRenderParam2);
                                }
                            }

                            // By design, this is the only place in a GLThread thread where we wait().
                            if (LOG_THREADS) {
                                Log.i("GLThread", "waiting tid=" + getId()
                                    + " mHaveEglContext: " + mHaveEglContext
                                    + " mHaveEglSurface: " + mHaveEglSurface
                                    + " mFinishedCreatingEglSurface: " + mFinishedCreatingEglSurface
                                    + " mPaused: " + mPaused
                                    + " mHasSurface: " + mHasSurface
                                    + " mSurfaceIsBad: " + mSurfaceIsBad
                                    + " mWaitingForSurface: " + mWaitingForSurface
                                    + " mWidth: " + mWidth
                                    + " mHeight: " + mHeight
                                    + " mRequestRender: " + mRequestRender
                                    + " mRenderMode: " + mRenderMode);
                            }
                            sGLThreadManager.wait();
                        }
                        if (LOG_PAUSE_RESUME) {
                            Log.i("GLThread",  "end of synchronized(sGLThreadManager)" );
                        }
                    } // end of synchronized(sGLThreadManager)
                    if (LOG_PAUSE_RESUME) {
                        Log.i("GLThread",  "if (event != null) {" );
                    }
                    if (event != null) {
                        event.run();
                        event = null;
                        continue;
                    }

                    if (createEglSurface) {
                        if (LOG_SURFACE) {
                            Log.w("GLThread", "egl createSurface");
                        }
                        if (mEglHelper.createSurface()) {
                            synchronized(sGLThreadManager) {
                                mFinishedCreatingEglSurface = true;
                                sGLThreadManager.notifyAll();
                            }
                        } else {
                            synchronized(sGLThreadManager) {
                                mFinishedCreatingEglSurface = true;
                                mSurfaceIsBad = true;
                                sGLThreadManager.notifyAll();
                            }
                            continue;
                        }
                        createEglSurface = false;
                    }

                    if (createGlInterface) {
                        gl = (GL10) mEglHelper.createGL();

                        sGLThreadManager.checkGLDriver(gl);
                        createGlInterface = false;
                    }

                    if (createEglContext) {
                        if (LOG_RENDERER) {
                            Log.w("GLThread", "onSurfaceCreated");
                        }
                        GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
                        if (view != null) {
                            if (LOG_RENDERER) {
                                Log.w("GLThread", "onSurfaceCreated (view != null)");
                            }
                            view.mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
                            
                            //--------------------------------------------------------
                            view.glInitialized(gl);
                            //--------------------------------------------------------
                           
                        }
                        createEglContext = false;
                    }

                    if (sizeChanged) {
                        if (LOG_RENDERER) {
                            Log.w("GLThread", "onSurfaceChanged(" + w + ", " + h + ")");
                        }
                        GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
                        if (view != null) {
                            view.mRenderer.onSurfaceChanged(gl, w, h);
                        }
                        sizeChanged = false;
                    }
                    
                    
                    if (LOG_RENDERER_DRAW_FRAME) {
                        Log.w("GLThread", "onDrawFrame tid=" + getId());
                    }
                    {
                        GVRGLSurfaceView view = mGVRGLSurfaceViewWeakRef.get();
                        if (!mRenderRequested) {
                            Log.w(TAG, "(!mRenderRequested) ");
                        }
                        if (view != null/* && mRenderRequested -- only when requested*/) {
                            view.mRenderer.onDrawFrame(mRenderParam1);
                            view.glRendered(mRenderParam2);
                        }
                    }
                    
                    /*
                     * swap call for eye draw, even when front buffer turned
                     * for performance wise, it seems no downside affect
                     */
                    int swapError = mEglHelper.swap();
                    switch (swapError) {
                        case EGL10.EGL_SUCCESS:
                            break;
                        case EGL11.EGL_CONTEXT_LOST:
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "egl context lost tid=" + getId());
                            }
                            lostEglContext = true;
                            break;
                        default:
                            EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", swapError);

                            synchronized(sGLThreadManager) {
                                mSurfaceIsBad = true;
                                sGLThreadManager.notifyAll();
                            }
                            break;
                    }

                    if (wantRenderNotification) {
                        doRenderNotification = true;
                    }
                }

            } finally {
                synchronized (sGLThreadManager) {
                    stopEglSurfaceLocked();
                    stopEglContextLocked();
                }
            }
        }

        public boolean ableToDraw() {
            return mHaveEglContext && mHaveEglSurface && readyToDraw();
        }

        private boolean readyToDraw() {
            return (!mPaused) && mHasSurface && (!mSurfaceIsBad)
                && (mWidth > 0) && (mHeight > 0)
                && (mRequestRender || (mRenderMode == RENDERMODE_CONTINUOUSLY));
        }

        public void setRenderMode(int renderMode) {
            if ( !((RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= RENDERMODE_CONTINUOUSLY)) ) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized(sGLThreadManager) {
                mRenderMode = renderMode;
                sGLThreadManager.notifyAll();
            }
        }

        public int getRenderMode() {
            synchronized(sGLThreadManager) {
                return mRenderMode;
            }
        }

        public void requestRender(int param1, long param2) {
            synchronized (sGLThreadManager) {
                mRenderParam1 = param1;
                mRenderParam2 = param2;

                mRequestRender = true;
                mRenderRequested = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void requestGLForEyeDraw() {
            Log.v(TAG, "requestGLForEyeDraw()");
            synchronized (sGLThreadManager) {
                mGLForEyeDrawRequested = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void requestGLForEyeShow() {
            Log.v(TAG, "requestGLForEyeShow()");
            // do it on the current thread
            mEglHelper.makeGLForEyeShow();
        }

        public void requestSwapShow() {
            if (LOG_RENDERER_DRAW_FRAME) {
                Log.w("GLThread", "requestSwapShow ");
            }
            
            // do it on the current thread
            mEglHelper.swapShow();
        }

        public void surfaceCreated() {
            synchronized(sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceCreated tid=" + getId());
                }
                mHasSurface = true;
                mFinishedCreatingEglSurface = false;
                sGLThreadManager.notifyAll();
                while (mWaitingForSurface
                       && !mFinishedCreatingEglSurface
                       && !mExited) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized(sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceDestroyed tid=" + getId());
                }
                mHasSurface = false;
                sGLThreadManager.notifyAll();
                while((!mWaitingForSurface) && (!mExited)) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (sGLThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onPause tid=" + getId());
                }
                mRequestPaused = true;
                sGLThreadManager.notifyAll();
                while ((! mExited) && (! mPaused)) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onPause waiting for mPaused.");
                    }
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onResume() {
            synchronized (sGLThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onResume tid=" + getId());
                }
                mRequestPaused = false;
                mRequestRender = true;
                mRenderComplete = false;
                sGLThreadManager.notifyAll();
                while ((! mExited) && mPaused && (!mRenderComplete)) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onResume waiting for !mPaused.");
                    }
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onWindowResize(int w, int h) {
            synchronized (sGLThreadManager) {
                mWidth = w;
                mHeight = h;
                mSizeChanged = true;
                mRequestRender = true;
                mRenderComplete = false;
                sGLThreadManager.notifyAll();

                // Wait for thread to react to resize and render a frame
                while (! mExited && !mPaused && !mRenderComplete
                        && ableToDraw()) {
                    if (LOG_SURFACE) {
                        Log.i("Main thread", "onWindowResize waiting for render complete from tid=" + getId());
                    }
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestExitAndWait() {
            // don't call this from GLThread thread or it is a guaranteed
            // deadlock!
            synchronized(sGLThreadManager) {
                mShouldExit = true;
                sGLThreadManager.notifyAll();
                while (! mExited) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestReleaseEglContextLocked() {
            mShouldReleaseEglContext = true;
            sGLThreadManager.notifyAll();
        }

        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            synchronized(sGLThreadManager) {
                mEventQueue.add(r);
                sGLThreadManager.notifyAll();
            }
        }

        // Once the thread is started, all accesses to the following member
        // variables are protected by the sGLThreadManager monitor
        private boolean mShouldExit;
        private boolean mExited;
        private boolean mRequestPaused;
        private boolean mPaused;
        private boolean mHasSurface;
        private boolean mSurfaceIsBad;
        private boolean mWaitingForSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private boolean mFinishedCreatingEglSurface;
        private boolean mShouldReleaseEglContext;
        private int mWidth;
        private int mHeight;
        private int mRenderMode;
        private boolean mRequestRender;
        private boolean mRenderComplete;
        private ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();
        private boolean mSizeChanged = true;
        
        private int mRenderParam1;
        private long mRenderParam2;
        private boolean mRenderRequested;
        private boolean mGLForEyeDrawRequested;

        // End of member variables protected by the sGLThreadManager monitor.

        /*
         * The instance of EglHelper: we don't create multiple instances on
         * purpose, but keep tracks the individual handles when release and so
         * on if still active.
         */
        protected EglHelper mEglHelper;
        private WeakReference<GVRGLSurfaceView> mGVRGLSurfaceViewWeakRef;
    }

    private void checkRenderThreadState() {
        if (mGLThread != null) {
            throw new IllegalStateException(
                    "setRenderer has already been called for this instance.");
        }
    }

    private static class GLThreadManager {
        private static String TAG = "GLThreadManager";

        public synchronized void threadExiting(GLThread thread) {
            if (LOG_THREADS) {
                Log.i("GLThread", "exiting tid=" +  thread.getId());
            }
            thread.mExited = true;
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        public boolean tryAcquireEglContextLocked(GLThread thread) {
            if (mEglOwner == thread || mEglOwner == null) {
                mEglOwner = thread;
                notifyAll();
                return true;
            }
            checkGLESVersion();
            if (mMultipleGLESContextsAllowed) {
                return true;
            }
            if (mEglOwner != null) {
                mEglOwner.requestReleaseEglContextLocked();
            }
            return false;
        }

        public void releaseEglContextLocked(GLThread thread) {
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        public synchronized boolean shouldReleaseEGLContextWhenPausing() {
            return mLimitedGLESContexts;
        }

        public synchronized boolean shouldTerminateEGLWhenPausing() {
            checkGLESVersion();
            return !mMultipleGLESContextsAllowed;
        }

        public synchronized void checkGLDriver(GL10 gl) {
            if (! mGLESDriverCheckComplete) {
                checkGLESVersion();
                String renderer = gl.glGetString(GL10.GL_RENDERER);
                if (mGLESVersion < kGLES_20) {
                    mMultipleGLESContextsAllowed =
                        ! renderer.startsWith(kMSM7K_RENDERER_PREFIX);
                    notifyAll();
                }
                mLimitedGLESContexts = !mMultipleGLESContextsAllowed;
                if (LOG_SURFACE) {
                    Log.w(TAG, "checkGLDriver renderer = \"" + renderer + "\" multipleContextsAllowed = "
                        + mMultipleGLESContextsAllowed
                        + " mLimitedGLESContexts = " + mLimitedGLESContexts);
                }
                mGLESDriverCheckComplete = true;
            }
        }

        private void checkGLESVersion() {
        }

        private boolean mGLESVersionCheckComplete;
        private int mGLESVersion;
        private boolean mGLESDriverCheckComplete;
        private boolean mMultipleGLESContextsAllowed;
        private boolean mLimitedGLESContexts;
        private static final int kGLES_20 = 0x20000;
        private static final String kMSM7K_RENDERER_PREFIX =
            "Q3Dimension MSM7500 ";
        private GLThread mEglOwner;
    }

    private static final GLThreadManager sGLThreadManager = new GLThreadManager();

    private final WeakReference<GVRGLSurfaceView> mThisWeakRef =
            new WeakReference<GVRGLSurfaceView>(this);
    private GLThread mGLThread;
    private Renderer mRenderer;
    private boolean mDetached;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLWrapper mGLWrapper;
    private int mDebugFlags;
    private int mEGLContextClientVersion;
    private boolean mPreserveEGLContextOnPause;
}

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

