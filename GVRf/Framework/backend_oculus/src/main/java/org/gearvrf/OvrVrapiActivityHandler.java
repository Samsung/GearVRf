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

import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.EGLContextFactory;
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;

import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Keep Oculus-specifics here
 */
final class OvrVrapiActivityHandler implements OvrActivityHandler {

    private final GVRApplication mApplication;
    private long mPtr;
    private GLSurfaceView mSurfaceView;
    private EGLSurface mPixelBuffer;

    // warning: writable static state; used to determine when vrapi can be safely uninitialized
    private static WeakReference<OvrVrapiActivityHandler> sVrapiOwner = new WeakReference<>(null);
    private OvrViewManager mViewManager;
    private int mCurrentSurfaceWidth, mCurrentSurfaceHeight;

    OvrVrapiActivityHandler(final GVRApplication application, final OvrActivityNative activityNative) throws VrapiNotAvailableException {
        if (null == application) {
            throw new IllegalArgumentException();
        }
        try {
            application.getActivity().getPackageManager().getPackageInfo("com.oculus.systemdriver", PackageManager.GET_SIGNATURES);
        } catch (final PackageManager.NameNotFoundException e) {
            try {
                application.getActivity().getPackageManager().getPackageInfo("com.oculus.systemactivities", PackageManager.GET_SIGNATURES);
            } catch (PackageManager.NameNotFoundException e1) {
                Log.e(TAG, "oculus packages missing, assuming vrapi will not work");
                throw new VrapiNotAvailableException();
            }
        }
        mApplication = application;
        mPtr = activityNative.getNative();

        if (null != sVrapiOwner.get()) {
            nativeUninitializeVrApi();
        }
        if (VRAPI_INITIALIZE_UNKNOWN_ERROR == nativeInitializeVrApi(mPtr)) {
            throw new VrapiNotAvailableException();
        }
        sVrapiOwner = new WeakReference<>(this);
    }

    @Override
    public void onPause() {
        if (null != mSurfaceView) {
            final CountDownLatch cdl = new CountDownLatch(1);
            mSurfaceView.onPause();
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    //these two must happen on the gl thread
                    nativeLeaveVrMode(mPtr);
                    destroySurfaceForTimeWarp();
                    cdl.countDown();
                }
            });
            try {
                cdl.await();
            } catch (final InterruptedException e) {
            }
        }
        mCurrentSurfaceWidth = mCurrentSurfaceHeight = 0;
    }

    @Override
    public void onResume() {
        final OvrVrapiActivityHandler currentOwner = sVrapiOwner.get();
        if (this != currentOwner) {
            nativeUninitializeVrApi();
            nativeInitializeVrApi(mPtr);
            sVrapiOwner = new WeakReference<>(this);
        }

        if (null != mSurfaceView) {
            mSurfaceView.onResume();
        }
    }

    @Override
    public boolean onBack() {
        if (null != mSurfaceView) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    nativeShowConfirmQuit(mPtr);
                }
            });
        }
        return true;
    }

    @Override
    public void onDestroy() {
        if (this == sVrapiOwner.get()) {
            nativeUninitializeVrApi();
            sVrapiOwner.clear();
        }
    }

    @Override
    public void setViewManager(GVRViewManager viewManager) {
        mViewManager = (OvrViewManager)viewManager;
    }

    @Override
    public void onSetScript() {
        mSurfaceView = new GLSurfaceView(mApplication.getActivity());
        mSurfaceView.setZOrderOnTop(true);

        final DisplayMetrics metrics = new DisplayMetrics();
        mApplication.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final VrAppSettings appSettings = mApplication.getAppSettings();
        int defaultWidthPixels = Math.max(metrics.widthPixels, metrics.heightPixels);
        int defaultHeightPixels = Math.min(metrics.widthPixels, metrics.heightPixels);
        final int frameBufferWidth = appSettings.getFramebufferPixelsWide();
        final int frameBufferHeight = appSettings.getFramebufferPixelsHigh();
        final SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSLUCENT);

        if ((-1 != frameBufferHeight) && (-1 != frameBufferWidth)) {
            if ((defaultWidthPixels != frameBufferWidth) && (defaultHeightPixels != frameBufferHeight)) {
                Log.v(TAG, "--- window configuration ---");
                Log.v(TAG, "--- width: %d", frameBufferWidth);
                Log.v(TAG, "--- height: %d", frameBufferHeight);
                //a different resolution of the native window requested
                defaultWidthPixels = frameBufferWidth;
                defaultHeightPixels = frameBufferHeight;
                Log.v(TAG, "----------------------------");
            }
        }
        holder.setFixedSize(defaultWidthPixels, defaultHeightPixels);

        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(3);
        mSurfaceView.setEGLContextFactory(mContextFactory);
        mSurfaceView.setEGLConfigChooser(mConfigChooser);
        mSurfaceView.setEGLWindowSurfaceFactory(mWindowSurfaceFactory);
        mSurfaceView.setRenderer(mRenderer);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mApplication.getActivity().setContentView(mSurfaceView);
    }

    private final EGLContextFactory mContextFactory = new EGLContextFactory() {
        @Override
        public void destroyContext(final EGL10 egl, final EGLDisplay display, final EGLContext context) {
            Log.v(TAG, "EGLContextFactory.destroyContext 0x%X", context.hashCode());
            egl.eglDestroyContext(display, context);
        }

        @Override
        public EGLContext createContext(final EGL10 egl, final EGLDisplay display, final EGLConfig eglConfig) {
            final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
            final int[] contextAttribs = {
                    EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL10.EGL_NONE
            };

            final EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, contextAttribs);
            if (context == EGL10.EGL_NO_CONTEXT) {
                throw new IllegalStateException("eglCreateContext failed; egl error 0x"
                        + Integer.toHexString(egl.eglGetError()));
            }
            Log.v(TAG, "EGLContextFactory.createContext 0x%X", context.hashCode());
            return context;
        }
    };

    private final EGLWindowSurfaceFactory mWindowSurfaceFactory = new EGLWindowSurfaceFactory() {
        @Override
        public void destroySurface(final EGL10 egl, final EGLDisplay display, final EGLSurface surface) {
            Log.v(TAG, "EGLWindowSurfaceFactory.destroySurface 0x%X, mPixelBuffer 0x%X", surface.hashCode(), mPixelBuffer.hashCode());
            boolean result = egl.eglDestroySurface(display, mPixelBuffer);
            Log.v(TAG, "EGLWindowSurfaceFactory.destroySurface successful %b, egl error 0x%x", result, egl.eglGetError());
            mPixelBuffer = null;
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
            Log.v(TAG, "EGLWindowSurfaceFactory.eglCreatePbufferSurface 0x%X", mPixelBuffer.hashCode());
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

            final int[] configAttribs = new int[16];
            int counter = 0;

            configAttribs[counter++] = EGL10.EGL_ALPHA_SIZE;
            configAttribs[counter++] = 8;
            configAttribs[counter++] = EGL10.EGL_BLUE_SIZE;
            configAttribs[counter++] = 8;
            configAttribs[counter++] = EGL10.EGL_GREEN_SIZE;
            configAttribs[counter++] = 8;
            configAttribs[counter++] = EGL10.EGL_RED_SIZE;
            configAttribs[counter++] = 8;
            configAttribs[counter++] = EGL10.EGL_DEPTH_SIZE;
            configAttribs[counter++] = 0;
            configAttribs[counter++] = EGL10.EGL_SAMPLES;
            configAttribs[counter++] = 0;

            Log.v(TAG, "--- window surface configuration ---");
            final VrAppSettings appSettings = mApplication.getAppSettings();
            if (appSettings.useSrgbFramebuffer) {
                final int EGL_GL_COLORSPACE_KHR = 0x309D;
                final int EGL_GL_COLORSPACE_SRGB_KHR = 0x3089;

                configAttribs[counter++] = EGL_GL_COLORSPACE_KHR;
                configAttribs[counter++] = EGL_GL_COLORSPACE_SRGB_KHR;
            }
            Log.v(TAG, "--- srgb framebuffer: %b", appSettings.useSrgbFramebuffer);

            if (appSettings.useProtectedFramebuffer) {
                final int EGL_PROTECTED_CONTENT_EXT = 0x32c0;

                configAttribs[counter++] = EGL_PROTECTED_CONTENT_EXT;
                configAttribs[counter++] = EGL14.EGL_TRUE;
            }
            Log.v(TAG, "--- protected framebuffer: %b", appSettings.useProtectedFramebuffer);

            configAttribs[counter++] = EGL10.EGL_NONE;
            Log.v(TAG, "------------------------------------");

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

    private void destroySurfaceForTimeWarp() {
        final EGL10 egl = (EGL10) EGLContext.getEGL();
        final EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        final EGLContext context = egl.eglGetCurrentContext();

        if (!egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, context)) {
            Log.v(TAG, "destroySurfaceForTimeWarp makeCurrent NO_SURFACE failed, egl error 0x%x", egl.eglGetError());
        }
    }

    private final Renderer mRenderer = new Renderer() {
        @Override
        public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
            Log.i(TAG, "onSurfaceCreated");
            nativeOnSurfaceCreated(mPtr);
            mViewManager.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
            Log.i(TAG, "onSurfaceChanged; %d x %d", width, height);

            if (width < height) {
                Log.v(TAG, "short-circuiting onSurfaceChanged; surface in portrait");
                return;
            }
            if (mCurrentSurfaceWidth == width && mCurrentSurfaceHeight == height) {
                return;
            }

            mCurrentSurfaceWidth = width; mCurrentSurfaceHeight = height;
            nativeLeaveVrMode(mPtr);
            destroySurfaceForTimeWarp();

            final EGL10 egl = (EGL10) EGLContext.getEGL();
            final EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            final EGLContext context = egl.eglGetCurrentContext();

            // necessary to explicitly make the pbuffer current for the rendering thread;
            // TimeWarp took over the window surface
            if (!egl.eglMakeCurrent(display, mPixelBuffer, mPixelBuffer, context)) {
                throw new IllegalStateException("Failed to make context current ; egl error 0x"
                        + Integer.toHexString(egl.eglGetError()));
            }

            nativeOnSurfaceChanged(mPtr, mSurfaceView.getHolder().getSurface());

            mViewManager.onSurfaceChanged(width, height);
            mViewManager.createSwapChain();
        }

        @Override
        public void onDrawFrame(final GL10 gl) {
            mViewManager.onDrawFrame();
        }
    };


    @SuppressWarnings("serial")
    static final class VrapiNotAvailableException extends RuntimeException {
    }

    private static native void nativeOnSurfaceCreated(long ptr);

    private static native void nativeOnSurfaceChanged(long ptr, Surface surface);

    private static native void nativeLeaveVrMode(long ptr);

    private static native void nativeShowConfirmQuit(long appPtr);

    private static native int nativeInitializeVrApi(long ptr);

    static native int nativeUninitializeVrApi();

    private static final int VRAPI_INITIALIZE_UNKNOWN_ERROR = -1;

    private static final String TAG = "OvrVrapiActivityHandler";
}
