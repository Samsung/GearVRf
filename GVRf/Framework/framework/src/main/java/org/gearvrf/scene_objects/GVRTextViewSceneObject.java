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

package org.gearvrf.scene_objects;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;

import java.lang.ref.WeakReference;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.LinearLayout;
import android.widget.TextView;

public class GVRTextViewSceneObject extends GVRSceneObject {
    private static final int REALTIME_REFRESH_INTERVAL = 1;
    private static final int HIGH_REFRESH_INTERVAL = 10; // frames
    private static final int MEDIUM_REFRESH_INTERVAL = 20;
    private static final int LOW_REFRESH_INTERVAL = 30;
    private static final int NONE_REFRESH_INTERVAL = 0;

    private static final float DEFAULT_QUAD_WIDTH = 2.0f;
    private static final float DEFAULT_QUAD_HEIGHT = 1.0f;
    private static final String DEFAULT_TEXT = "";
    //@todo the following two probably should be derived from the display metrics
    private static final int FACTOR_IMAGE_SIZE = 128;
    private static final int MAX_IMAGE_SIZE = 4*FACTOR_IMAGE_SIZE;

    /**
     * The refresh frequency of this sceneobject.
     */
    public static enum IntervalFrequency {
        /*
         * Frequency REALTIME, means will refresh as soon as it changes.
         */
        REALTIME,
        /*
         * Frequency HIGH, means will do refresh every 10 frames (if it changes)
         */
        HIGH,
        /*
         * Frequency MEDIUM, means will do refresh every 20 frames (if it changes)
         */
        MEDIUM,
        /*
         * Frequency LOW, means will do refresh every 30 frames (if it changes)
         */
        LOW,
        /*
         * No periodic refresh, even if it changes.
         */
        NONE
    }

    private static int sReferenceCounter = 0;// This is for load balancing.
    private boolean mFirstFrame;
    private boolean mIsChanged;
    private volatile int mRefreshInterval = REALTIME_REFRESH_INTERVAL;

    private final Surface mSurface;
    private final SurfaceTexture mSurfaceTexture;
    private final LinearLayout mTextViewContainer;
    private final TextView mTextView;

    private int mCount;
    private final GVRDrawFrameListenerImpl mFrameListener;

    /**
     * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}
     * with view's default height and width.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param gvrActivity
     *            a {@link GVRActivity}
     * @param width
     *            Scene object height, in GVRF scene graph units.
     * 
     * @param height
     *            Scene object's width, in GVRF scene graph units.
     * @param text
     *            {@link CharSequence} to show on the textView
     */
    public GVRTextViewSceneObject(GVRContext gvrContext, float width, float height, CharSequence text) {
        super(gvrContext, gvrContext.createQuad(width, height));

        //cap the canvas dimensions
        final float factor = width/height;
        int canvasWidth = (int)(width*FACTOR_IMAGE_SIZE);
        int canvasHeight = (int)(height*FACTOR_IMAGE_SIZE);

        if (canvasWidth > canvasHeight && canvasWidth > MAX_IMAGE_SIZE) {
            canvasWidth = MAX_IMAGE_SIZE;
            canvasHeight = (int)(MAX_IMAGE_SIZE/factor);
        } else if (canvasHeight > canvasWidth && canvasHeight > MAX_IMAGE_SIZE) {
            canvasWidth = (int)(MAX_IMAGE_SIZE*factor);
            canvasHeight = MAX_IMAGE_SIZE;
        }

        final GVRActivity activity = gvrContext.getActivity();
        mTextView = new TextView(activity);
        mTextView.setBackgroundColor(Color.TRANSPARENT);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setText(text);
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setLayoutParams(new LayoutParams(canvasWidth, canvasHeight));

        mTextViewContainer = new LinearLayout(activity);
        mTextViewContainer.addView(mTextView);
        mTextViewContainer.setVisibility(View.VISIBLE);

        mTextViewContainer.measure(canvasWidth, canvasHeight);
        mTextViewContainer.layout(0, 0, canvasWidth, canvasHeight);

        mFrameListener = new GVRDrawFrameListenerImpl(this);
        gvrContext.registerDrawFrameListener(mFrameListener);

        GVRTexture texture = new GVRExternalTexture(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurfaceTexture.setDefaultBufferSize(canvasWidth, canvasHeight);
        mSurface = new Surface(mSurfaceTexture);

        sReferenceCounter++;
        mCount = sReferenceCounter;
        mFirstFrame = true;
    }

    /**
     * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}
     * with both view's default height and width and quad's default height and
     * width.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param text
     *            {@link CharSequence} to show on the textView
     */
    public GVRTextViewSceneObject(GVRContext gvrContext, CharSequence text) {
        this(gvrContext, DEFAULT_QUAD_WIDTH, DEFAULT_QUAD_HEIGHT, text);
    }

    /**
     * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}
     * with both view's default height and width and quad's default height and
     * width. The initial text will be the private {@code DEFAULT_TEXT}
     * constant, or {@code ""}.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRTextViewSceneObject(GVRContext gvrContext) {
        this(gvrContext, DEFAULT_TEXT);
    }

    /**
     * Set the text size.
     * 
     * @param newSize
     *            The new text size.
     */
    public void setTextSize(float size) {
        mTextView.setTextSize(size);
        mIsChanged = true;
    }

    /**
     * Get the current text size.
     * 
     * @return The current text size.
     */
    public float getTextSize() {
        return mTextView.getTextSize();
    }

    /**
     * Set the text color.
     * 
     * @param color
     *            The text color, in Android {@link Color} format. The
     *            {@linkplain Color#alpha(int) alpha component} is ignored.
     */
    public void setTextColor(int color) {
        mTextView.setTextColor(color);
        mIsChanged = true;
    }

    /**
     * Set the text to be displayed.
     * 
     * @param text
     *            The new text to be displayed.
     */
    public void setText(CharSequence text) {
        mTextView.setText(text);
        mIsChanged = true;
    }

    /**
     * Get the current text.
     * 
     * @return The text that is currently displayed.
     */
    public CharSequence getText() {
        return mTextView.getText();
    }

    /**
     * Get the current text, as a {@code String}.
     * 
     * This is a convenience function, 100% equivalent to {@link #getText()}
     * {@code .toString()}
     * 
     * @return The text that is currently displayed.
     */
    public String getTextString() {
        return getText().toString();
    }

    /**
     * Set the view's background color.
     * 
     * @param color
     *            The view's background color, in Android {@link Color} format.
     *            The {@linkplain Color#alpha(int) alpha component} is ignored.
     */
    public void setBackgroundColor(int color) {
        mTextViewContainer.setBackgroundColor(color);
        mIsChanged = true;
    }

    /**
     * Set the view's background {@code Drawable}.
     * 
     * @param drawable
     *            The view's background. {@code null} will clear any current
     *            background {@code Drawable}.
     */
    public void setBackGround(Drawable drawable) {
        mTextViewContainer.setBackground(drawable);
        mIsChanged = true;
    }

    /**
     * Get the view's background {@code Drawable}, if any.
     * 
     * @param drawable
     *            The view's background; may be {@code null}.
     */
    public Drawable getBackGround() {
        return mTextViewContainer.getBackground();
    }

    /**
     * Set the view's gravity.
     * 
     * @param gravity
     *            The gravity of the internal TextView
     */
    public void setGravity(int gravity) {
        mTextView.setGravity(gravity);
        mIsChanged = true;
    }

    /**
     * Get the view's gravity.
     * 
     * @return The gravity of the internal TextView
     */
    public int getGravity() {
        return mTextView.getGravity();
    }

    /**
     * Set the refresh frequency of this scene object.
     * Use NONE for improved performance when the text is set initially and never
     * changed.
     * 
     * @param frequency
     *            The refresh frequency of this TextViewSceneObject.
     */
    public void setRefreshFrequency(IntervalFrequency frequency) {
        if (NONE_REFRESH_INTERVAL == mRefreshInterval && IntervalFrequency.NONE != frequency) {
            // Install draw-frame listener if frequency is no longer NONE
            getGVRContext().unregisterDrawFrameListener(mFrameListener);
            getGVRContext().registerDrawFrameListener(mFrameListener);
        }
        switch (frequency) {
        case REALTIME:
            mRefreshInterval = REALTIME_REFRESH_INTERVAL;
            break;
        case HIGH:
            mRefreshInterval = HIGH_REFRESH_INTERVAL;
            break;
        case MEDIUM:
            mRefreshInterval = MEDIUM_REFRESH_INTERVAL;
            break;
        case LOW:
            mRefreshInterval = LOW_REFRESH_INTERVAL;
            break;
        case NONE:
            mRefreshInterval = NONE_REFRESH_INTERVAL;
            break;
        default:
            break;
        }
    }

    /**
     * Get the refresh frequency of this scene object.
     * 
     * @return The refresh frequency of this TextViewSceneObject.
     */

    public IntervalFrequency getRefreshFrequency() {
        switch (mRefreshInterval) {
        case REALTIME_REFRESH_INTERVAL:
            return IntervalFrequency.REALTIME;
        case HIGH_REFRESH_INTERVAL:
            return IntervalFrequency.HIGH;
        case LOW_REFRESH_INTERVAL:
            return IntervalFrequency.LOW;
        case MEDIUM_REFRESH_INTERVAL:
            return IntervalFrequency.MEDIUM;
        default:
            return IntervalFrequency.NONE;
        }
    }

    private static final class GVRDrawFrameListenerImpl implements GVRDrawFrameListener {
        GVRDrawFrameListenerImpl(final GVRTextViewSceneObject sceneObject) {
            mRef = new WeakReference<GVRTextViewSceneObject>(sceneObject);
            mContext = sceneObject.getGVRContext();
        }

        @Override
        public void onDrawFrame(float frameTime) {
            final GVRTextViewSceneObject sceneObject = mRef.get();
            if (null != sceneObject) {
                int refreshInterval = sceneObject.mRefreshInterval;
                if ((sceneObject.mFirstFrame || sceneObject.mIsChanged) &&
                    (REALTIME_REFRESH_INTERVAL == refreshInterval ||
                     (NONE_REFRESH_INTERVAL != refreshInterval
                            && (++sceneObject.mCount % refreshInterval == 0)))) {

                    sceneObject.refresh();
                    if (!sceneObject.mFirstFrame) {
                        sceneObject.mCount = 0;
                    } else {
                        sceneObject.mFirstFrame = false;
                    }
                    sceneObject.mIsChanged = false;
                }
                if (NONE_REFRESH_INTERVAL == refreshInterval) {
                    mContext.unregisterDrawFrameListener(this);
                }
            } else {
                mContext.unregisterDrawFrameListener(this);
            }
        }

        private final WeakReference<GVRTextViewSceneObject> mRef;
        private final GVRContext mContext;
    };

    private void refresh() {
        try {
            Canvas canvas = mSurface.lockCanvas(null);
            canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
            mTextViewContainer.draw(canvas);
            mSurface.unlockCanvasAndPost(canvas);
        } catch (Surface.OutOfResourcesException t) {
            Log.e("GVRTextViewObject", "lockCanvas failed");
        }
        mSurfaceTexture.updateTexImage();
    }
}
