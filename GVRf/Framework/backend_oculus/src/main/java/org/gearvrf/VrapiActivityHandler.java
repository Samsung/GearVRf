/* Copyright 2016 Samsung Electronics Co., LTD
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

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;
import org.joml.Vector2f;

import android.graphics.PixelFormat;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.EGLContextFactory;
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import android.view.SurfaceHolder;

/**
 * Keep Oculus-specifics here
 */
class VrapiActivityHandler implements ActivityHandler {

    private final GVRActivity mActivity;
    private long mPtr;
    private GLSurfaceView mSurfaceView;
    private final ActivityHandlerRenderingCallbacks mCallbacks;
    private EGLSurface mPixelBuffer;
    private EGLSurface mMainSurface;
    boolean mVrApiInitialized;

    VrapiActivityHandler(final GVRActivity activity,
            final ActivityHandlerRenderingCallbacks callbacks) throws VrapiNotAvailableException {
        if (null == callbacks || null == activity) {
            throw new IllegalArgumentException();
        }
        mActivity = activity;
        mCallbacks = callbacks;
        mPtr = activity.getNative();

        if (VRAPI_INITIALIZE_UNKNOWN_ERROR == nativeInitializeVrApi(mPtr)) {
            throw new VrapiNotAvailableException();
        }
        mVrApiInitialized = true;
    }

    @Override
    public void onPause() {
        stopChoreographerThread();

        final CountDownLatch cdl;
        if (null != mSurfaceView) {
            mSurfaceView.onPause();
            cdl = new CountDownLatch(1);
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    //these two must happen on the gl thread
                    nativeLeaveVrMode(mPtr);
                    destroySurfaceForTimeWarp();
                    cdl.countDown();
                }
            });
        } else {
            cdl = null;
        }

        if (mVrApiInitialized) {
            if (null != cdl) {
                try {
                    cdl.await();
                } catch (final InterruptedException ignored) {
                }
            }
            nativeUninitializeVrApi(mPtr);
            mVrApiInitialized = false;
        }
    }

    @Override
    public void onResume() {
        if (!mVrApiInitialized) {
            nativeInitializeVrApi(mPtr);
            mVrApiInitialized = true;
        }

        startChoreographerThreadIfNotStarted();
        if (null != mSurfaceView) {
            mSurfaceView.onResume();
        }
    }

    @Override
    public boolean onBack() {
        nativeShowConfirmQuit(mPtr);
        return true;
    }

    @Override
    public boolean onBackLongPress() {
        nativeShowGlobalMenu(mPtr);
        return true;
    }

    @Override
    public void onSetScript() {
        mSurfaceView = new GLSurfaceView(mActivity);

        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(3);
        mSurfaceView.setEGLContextFactory(mContextFactory);
        mSurfaceView.setEGLConfigChooser(mConfigChooser);
        mSurfaceView.setEGLWindowSurfaceFactory(mWindowSurfaceFactory);
        mSurfaceView.setRenderer(mRenderer);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mActivity.setContentView(mSurfaceView);

        final DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final VrAppSettings appSettings = mActivity.getAppSettings();
        final int screenWidthPixels = Math.max(metrics.widthPixels, metrics.heightPixels);
        final int screenHeightPixels = Math.min(metrics.widthPixels, metrics.heightPixels);
        final int frameBufferWidth = appSettings.getFramebufferPixelsWide();
        final int frameBufferHeight = appSettings.getFramebufferPixelsHigh();
        final SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSLUCENT);

        if ((-1 != frameBufferHeight) && (-1 != frameBufferWidth)) {
            if ((screenWidthPixels != frameBufferWidth) && (screenHeightPixels != frameBufferHeight)) {
                Log.v(TAG, "--- window configuration ---");
                Log.v(TAG, "--- width: %d", frameBufferWidth);
                Log.v(TAG, "--- height: %d", frameBufferHeight);
                //a different resolution of the native window requested
                holder.setFixedSize((int) frameBufferWidth, (int) frameBufferHeight);
                Log.v(TAG, "----------------------------");
            }
        }
    }

    private final EGLContextFactory mContextFactory = new EGLContextFactory() {
        @Override
        public void destroyContext(final EGL10 egl, final EGLDisplay display, final EGLContext context) {
            Log.v(TAG, "EGLContextFactory.destroyContext");
            egl.eglDestroyContext(display, context);
        }

        @Override
        public EGLContext createContext(final EGL10 egl, final EGLDisplay display, final EGLConfig eglConfig) {
            final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
            final int[] contextAttribs = {
                    EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL10.EGL_NONE
            };

            final EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT,
                    contextAttribs);
            if (context == EGL10.EGL_NO_CONTEXT) {
                throw new IllegalStateException("eglCreateContext failed; egl error 0x"
                        + Integer.toHexString(egl.eglGetError()));
            }
            return context;
        }
    };

    private final EGLWindowSurfaceFactory mWindowSurfaceFactory = new EGLWindowSurfaceFactory() {
        @Override
        public void destroySurface(final EGL10 egl, final EGLDisplay display, final EGLSurface surface) {
            Log.v(TAG, "EGLWindowSurfaceFactory.destroySurface " + Integer.toHexString(surface.hashCode()));
            egl.eglDestroySurface(display, surface);
        }

        @Override
        public EGLSurface createWindowSurface(final EGL10 egl, final EGLDisplay display, final EGLConfig config,
                final Object ignoredNativeWindow) {
            final int[] surfaceAttribs = {
                    EGL10.EGL_WIDTH, 16,
                    EGL10.EGL_HEIGHT, 16,
                    EGL10.EGL_NONE
            };
            mPixelBuffer = egl.eglCreatePbufferSurface(display, config, surfaceAttribs);
            if (EGL10.EGL_NO_SURFACE == mPixelBuffer) {
                throw new IllegalStateException("Pixel buffer surface not created; egl error 0x"
                        + Integer.toHexString(egl.eglGetError()));
            }
            Log.v(TAG, "EGLWindowSurfaceFactory.createWindowSurface : " + Integer.toHexString(mPixelBuffer.hashCode()));
            return mPixelBuffer;
        }
    };

    private final EGLConfigChooser mConfigChooser = new EGLConfigChooser() {
        @Override
        public EGLConfig chooseConfig(final EGL10 egl, final EGLDisplay display) {
            final int[] numberConfigs = new int[1];
            if (!egl.eglGetConfigs(display, null, 0, numberConfigs)) {
                throw new IllegalStateException("Unable to retrieve number of egl configs available.");
            }
            final EGLConfig[] configs = new EGLConfig[numberConfigs[0]];
            if (!egl.eglGetConfigs(display, configs, configs.length, numberConfigs)) {
                throw new IllegalStateException("Unable to retrieve egl configs available.");
            }

            final int[] configAttribs = {
                    EGL10.EGL_ALPHA_SIZE, 8, // need alpha for the multi-pass
                                             // timewarp compositor
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 0,
                    EGL10.EGL_SAMPLES, 0,
                    EGL10.EGL_NONE
            };

            EGLConfig config = null;
            for (int i = 0; i < numberConfigs[0]; ++i) {
                final int[] value = new int[1];

                final int EGL_OPENGL_ES3_BIT_KHR = 0x0040;
                if (!egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_RENDERABLE_TYPE,
                        value)) {
                    Log.v(TAG, "eglGetConfigAttrib for EGL_RENDERABLE_TYPE failed");
                    continue;
                }
                if ((value[0] & EGL_OPENGL_ES3_BIT_KHR) != EGL_OPENGL_ES3_BIT_KHR) {
                    continue;
                }

                if (!egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_SURFACE_TYPE, value)) {
                    Log.v(TAG, "eglGetConfigAttrib for EGL_SURFACE_TYPE failed");
                    continue;
                }
                if ((value[0]
                        & (EGL10.EGL_WINDOW_BIT | EGL10.EGL_PBUFFER_BIT)) != (EGL10.EGL_WINDOW_BIT
                                | EGL10.EGL_PBUFFER_BIT)) {
                    continue;
                }

                int j = 0;
                for (; configAttribs[j] != EGL10.EGL_NONE; j += 2) {
                    if (!egl.eglGetConfigAttrib(display, configs[i], configAttribs[j], value)) {
                        Log.v(TAG, "eglGetConfigAttrib for " + configAttribs[j] + " failed");
                        continue;
                    }
                    if (value[0] != configAttribs[j + 1]) {
                        break;
                    }
                }
                if (configAttribs[j] == EGL10.EGL_NONE) {
                    config = configs[i];
                    break;
                }
            }
            return config;
        }
    };

    private final FrameCallback mFrameCallback = new FrameCallback() {
        @Override
        public void doFrame(final long frameTimeNanos) {
            mSurfaceView.requestRender();
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    private HandlerThread mChoreographerThread;

    private final void startChoreographerThreadIfNotStarted() {
        if (null == mChoreographerThread) {
            mChoreographerThread = new HandlerThread(
                    "gvrf-choreographer-" + Integer.toHexString(hashCode())) {
                @Override
                protected void onLooperPrepared() {
                    // Force callbacks from the Choreographer to happen off the
                    // UI thread.
                    Choreographer.getInstance().removeFrameCallback(mFrameCallback);
                    Choreographer.getInstance().postFrameCallback(mFrameCallback);
                }
            };
            mChoreographerThread.start();
        }
    }

    private final void stopChoreographerThread() {
        if (null != mChoreographerThread) {
            Choreographer.getInstance().removeFrameCallback(mFrameCallback);
            mChoreographerThread.quitSafely();
            try {
                mChoreographerThread.join();
            } catch (final Exception ignored) {
            } finally {
                mChoreographerThread = null;
            }
        }
    }

    private void destroySurfaceForTimeWarp() {
        if (null != mMainSurface) {
            Log.v(TAG, "destroying mMainSurface: 0x%x", mMainSurface.hashCode());
            final EGL10 egl = (EGL10) EGLContext.getEGL();
            final EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            egl.eglDestroySurface(display, mMainSurface);
            mMainSurface = null;
        }
    }

    private final Renderer mRenderer = new Renderer() {
        private EGLConfig mConfig;

        @Override
        public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
            Log.i(TAG, "onSurfaceCreated");

            mConfig = config;
            nativeOnSurfaceCreated(mPtr);
            mCallbacks.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
            Log.i(TAG, "onSurfaceChanged; %d x %d", width, height);

            if (null != mMainSurface) {
                Log.v(TAG, "short-circuiting onSurfaceChanged");
                return;
            }

            final EGL10 egl = (EGL10) EGLContext.getEGL();
            final EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            final EGLContext context = egl.eglGetCurrentContext();

            int numAttribs = 0;
            final int[] configAttribs = new int[16];
            Arrays.fill(configAttribs, EGL10.EGL_NONE);

            Log.v(TAG, "--- window surface configuration ---");
            final VrAppSettings appSettings = mActivity.getAppSettings();
            if (appSettings.useSrgbFramebuffer) {
                final int EGL_GL_COLORSPACE_KHR = 0x309D;
                final int EGL_GL_COLORSPACE_SRGB_KHR = 0x3089;

                configAttribs[numAttribs++] = EGL_GL_COLORSPACE_KHR;
                configAttribs[numAttribs++] = EGL_GL_COLORSPACE_SRGB_KHR;
            }
            Log.v(TAG, "--- srgb framebuffer: %b", appSettings.useSrgbFramebuffer);

            if (appSettings.useProtectedFramebuffer) {
                final int EGL_PROTECTED_CONTENT_EXT = 0x32c0;

                configAttribs[numAttribs++] = EGL_PROTECTED_CONTENT_EXT;
                configAttribs[numAttribs++] = EGL14.EGL_TRUE;
            }
            Log.v(TAG, "--- protected framebuffer: %b", appSettings.useProtectedFramebuffer);

            configAttribs[numAttribs++] = EGL10.EGL_NONE;
            Log.v(TAG, "------------------------------------");

            // this is the display surface timewarp will hijack
            mMainSurface = egl.eglCreateWindowSurface(display, mConfig, mSurfaceView.getHolder(), configAttribs);
            Log.v(TAG, "mMainSurface: 0x%x", mMainSurface.hashCode());

            if (mMainSurface == EGL10.EGL_NO_SURFACE) {
                throw new IllegalStateException(
                        "eglCreateWindowSurface() failed: 0x" + Integer.toHexString(egl.eglGetError()));
            }
            if (!egl.eglMakeCurrent(display, mMainSurface, mMainSurface, context)) {
                throw new IllegalStateException(
                        "eglMakeCurrent() failed: 0x " + Integer.toHexString(egl.eglGetError()));
            }
            nativeOnSurfaceChanged(mPtr);

            // necessary to explicitly make the pbuffer current for the rendering thread;
            // TimeWarp took over the window surface
            if (!egl.eglMakeCurrent(display, mPixelBuffer, mPixelBuffer, context)) {
                throw new IllegalStateException("Failed to make context current ; egl error 0x"
                        + Integer.toHexString(egl.eglGetError()));
            }

            startChoreographerThreadIfNotStarted();
            mCallbacks.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(final GL10 gl) {
            mCallbacks.onBeforeDrawEyes();
            nativeOnDrawFrame(mPtr);
            mCallbacks.onAfterDrawEyes();
        }
    };


    @SuppressWarnings("serial")
    static final class VrapiNotAvailableException extends Exception {
    }

    private static native void nativeOnSurfaceCreated(long ptr);

    private static native void nativeOnSurfaceChanged(long ptr);

    private static native void nativeOnDrawFrame(long ptr);

    private static native void nativeLeaveVrMode(long ptr);

    private static native void nativeShowGlobalMenu(long appPtr);

    private static native void nativeShowConfirmQuit(long appPtr);

    private static native int nativeInitializeVrApi(long ptr);

    private static native int nativeUninitializeVrApi(long ptr);

    private static final int VRAPI_INITIALIZE_SUCCESS = 0;
    private static final int VRAPI_INITIALIZE_UNKNOWN_ERROR = -1;
    private static final int VRAPI_INITIALIZE_PERMISSIONS_ERROR = -2;

    private static final String TAG = "VrapiActivityHandler";
}
