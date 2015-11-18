package org.gearvrf;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.utility.ImageUtils;
import org.gearvrf.utility.Log;

import android.graphics.Bitmap;

public class GVRTextureCapturer extends GVRHybridObject {
    private static final String TAG = GVRTextureCapturer.class.getSimpleName();
    private static final int TCCB_NEW_CAPTURE = 1;

    public static interface TextureCapturerListener {
        void onTextureCaptured(Bitmap capturedTexture);
    }

    protected int width;
    protected int height;
    protected GVRRenderTexture captureTexture;
    protected int[] readBackBuffer;
    protected boolean processingCapturedTexture;

    protected List<TextureCapturerListener> mListeners;

    public GVRTextureCapturer(GVRContext gvrContext, int width, int height) {
        this(gvrContext, width, height, 0);
    }

    public GVRTextureCapturer(GVRContext gvrContext, int width, int height, int sampleCount) {
        super(gvrContext, NativeTextureCapturer.ctor(
                gvrContext.getRenderBundle().getMaterialShaderManager().getNative()));
        NativeTextureCapturer.setCapturerObject(getNative(), this);

        mListeners = new ArrayList<TextureCapturerListener>();

        if (sampleCount > 1) {
            int maxSampleCount = GVRMSAA.getMaxSampleCount();
            if (sampleCount > maxSampleCount) {
                sampleCount = maxSampleCount;
            }
        }

        update(width, height, sampleCount);
    }

    public void update(int width, int height, int sampleCount) {
        this.width = width;
        this.height = height;

        if (sampleCount == 0)
            captureTexture = new GVRRenderTexture(getGVRContext(), width, height);
        else
            captureTexture = new GVRRenderTexture(getGVRContext(), width, height, sampleCount);

        setRenderTexture(captureTexture);
        readBackBuffer = new int[width * height];
    }

    public void setRenderTexture(GVRRenderTexture screenshot) {
        NativeTextureCapturer.setRenderTexture(getNative(), screenshot.getNative());
    }

    public void setCapture(boolean capture, float fps) {
        NativeTextureCapturer.setCapture(getNative(), capture, fps);
    }

    public void addListener(TextureCapturerListener l) {
        synchronized (mListeners) {
            mListeners.add(l);
        }
    }

    public void removeListener(TextureCapturerListener l) {
        synchronized (mListeners) {
            mListeners.remove(l);
        }
    }

    public void callbackFromNative(int index, String info) {
        switch (index) {
            case TCCB_NEW_CAPTURE:
                getGVRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        Bitmap capturedBitmap = null;

                        synchronized (readBackBuffer) {
                            if (processingCapturedTexture) {
                                Log.w(TAG, "Busy processing a captured frame. Reduce capture FPS?");
                                return;
                            }

                            try {
                                processingCapturedTexture = true;
                                boolean readOk = captureTexture.readRenderResult(readBackBuffer);
                                if (!readOk)
                                    return;

                                capturedBitmap = ImageUtils.generateBitmap(readBackBuffer,
                                                                           width, height);
                            } finally {
                                processingCapturedTexture = false;
                            }

                            for (TextureCapturerListener l : mListeners) {
                                l.onTextureCaptured(capturedBitmap);
                            }
                        }
                    }
                });
                break;
        }
    }
}

class NativeTextureCapturer {
    static native long ctor(long shaderManagerPtr);
    static native void setCapturerObject(long capturer, GVRTextureCapturer capturerObject);
    static native void setRenderTexture(long capturer, long ptr);
    static native void setCapture(long capturer, boolean capture, float fps);
}
