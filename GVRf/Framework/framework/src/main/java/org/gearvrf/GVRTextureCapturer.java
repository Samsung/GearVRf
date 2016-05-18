package org.gearvrf;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.utility.ImageUtils;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.Threads;

import android.graphics.Bitmap;

/**
 * Captures rendered contents from a shader into a {@code GVRRenderTexture} while
 * normal rendering is in progress.
 *
 * A {@code GVRTextureCapturer} object can be set into a {@code GVRRenderData}, which
 * is passed to the corresponding shader that renders a scene object. The shader's native
 * class can check if capturing is requested, and perform a capture while rendering to
 * screen.
 *
 * Automatic capturing can be set up with a specified FPS value, and after rendering
 * the callback function is called with the captured {@code Bitmap}.
 */
public class GVRTextureCapturer extends GVRHybridObject {
    private static final String TAG = GVRTextureCapturer.class.getSimpleName();
    private static final int TCCB_NEW_CAPTURE = 1;

    /**
     * An interface to receive captured {@code Bitmap}s.
     */
    public static interface TextureCapturerListener {
        void onTextureCaptured(Bitmap capturedTexture);
    }

    protected int width;
    protected int height;
    protected GVRRenderTexture captureTexture;
    protected int[] readBackBuffer;
    protected Object processingLock = new Object();
    protected boolean processingCapturedTexture;
    protected boolean capturing;

    protected List<TextureCapturerListener> mListeners;

    /**
     * Constructs a texture capturer with a backing texture with width * height pixels.
     *
     * @param gvrContext The {@code GVRContext}.
     * @param width The width of the backing texture in pixels.
     * @param height The height of the backing texture in pixels.
     */
    public GVRTextureCapturer(GVRContext gvrContext, int width, int height) {
        this(gvrContext, width, height, 0);
    }

    /**
     * Constructs a texture capturer with a backing texture with width * height pixels,
     * a specified MSAA sample count.
     *
     * @param gvrContext The {@code GVRContext}.
     * @param width The width of the backing texture in pixels.
     * @param height The height of the backing texture in pixels.
     * @param sampleCount The MSAA sample count.
     */
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


    static public long getComponentType() {
        return NativeTextureCapturer.getComponentType();
    }
    
    /**
     * Updates the backing render texture. This method should not
     * be called when capturing is in progress.
     *
     * @param width The width of the backing texture in pixels.
     * @param height The height of the backing texture in pixels.
     * @param sampleCount The MSAA sample count.
     */
    public void update(int width, int height, int sampleCount) {
        if (capturing) {
            throw new IllegalStateException("Cannot update backing texture while capturing");
        }

        this.width = width;
        this.height = height;

        if (sampleCount == 0)
            captureTexture = new GVRRenderTexture(getGVRContext(), width, height);
        else
            captureTexture = new GVRRenderTexture(getGVRContext(), width, height, sampleCount);

        setRenderTexture(captureTexture);
        readBackBuffer = new int[width * height];
    }

    private void setRenderTexture(GVRRenderTexture screenshot) {
        NativeTextureCapturer.setRenderTexture(getNative(), screenshot.getNative());
    }

    /**
     * Starts or stops capturing.
     *
     * @param capture If true, capturing is started. If false, it is stopped.
     * @param fps Capturing FPS (frames per second).
     */
    public void setCapture(boolean capture, float fps) {
        capturing = capture;
        NativeTextureCapturer.setCapture(getNative(), capture, fps);
    }

    /**
     * Adds a listener of the capturer.
     *
     * @param l The listener.
     */
    public void addListener(TextureCapturerListener l) {
        synchronized (mListeners) {
            mListeners.add(l);
        }
    }

    /**
     * Removes a listener of the capturer.
     *
     * @param l The listener.
     */
    public void removeListener(TextureCapturerListener l) {
        synchronized (mListeners) {
            mListeners.remove(l);
        }
    }

    protected void callbackFromNative(int index, String info) {
        switch (index) {
            case TCCB_NEW_CAPTURE:
                synchronized (processingLock) {
                    // Busy processing, drop frame without queuing
                    if (processingCapturedTexture) {
                        Log.w(TAG, "Busy processing a captured frame. Reduce capture FPS?");
                        return;
                    }
                    processingCapturedTexture = true;
                }

                getGVRContext().runOnGlThreadPostRender(1 /* delay 1 frame */, new Runnable() {
                    @Override
                    public void run()
                    {
                        boolean readOk = captureTexture.readRenderResult(readBackBuffer);
                        if (!readOk) {
                            synchronized (processingLock) {
                                processingCapturedTexture = false;
                            }
                            return;
                        }

                        Threads.spawn(new Runnable() {
                            @Override
                            public void run()
                            {
                                Bitmap capturedBitmap = ImageUtils.generateBitmap(readBackBuffer,
                                                                                  width, height);
                                // Wait for all listeners before processing another frame
                                for (TextureCapturerListener l : mListeners) {
                                    l.onTextureCaptured(capturedBitmap);
                                }

                                synchronized (processingLock) {
                                    processingCapturedTexture = false;
                                }
                            }
                        });
                    }
                });
                break;
        }
    }
}

class NativeTextureCapturer {
    static native long ctor(long shaderManagerPtr);
    static native long getComponentType();
    static native void setCapturerObject(long capturer, GVRTextureCapturer capturerObject);
    static native void setRenderTexture(long capturer, long ptr);
    static native void setCapture(long capturer, boolean capture, float fps);
}
