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
import java.util.Locale;

import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.LinearLayout;
import android.widget.TextView;

public class GVRTextViewSceneObject extends GVRSceneObject {
    private static final String TAG = GVRTextViewSceneObject.class.getSimpleName();

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

    public static final String DEFAULT_FONT = "default";

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

    /**
     * Enumerated types justifyTypes and fontStyleTypes values
     * based on X3D's <FontStyle> justify and style settings.
     */
    public enum justifyTypes { BEGIN, END, FIRST, MIDDLE };
    public enum fontStyleTypes { PLAIN, BOLD, ITALIC, BOLDITALIC };


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

    // individual lines of text.  Assists in determining lenghts of
    // strings once the fonts are set.
    private String linesOfText[] = null;
    private GVRMaterial gvrMaterialX3D = null;

    /**
     * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}
     * with view's default height and width.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            Scene object height, in GVRF scene graph units.
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
     * Special constructor called by X3D parser
     * setTextPlaneSize() called once parameters such as font and size are set.
     *
     * @param gvrContext
     * @param strings
     */
    public GVRTextViewSceneObject(GVRContext gvrContext, String[] strings) {
        super(gvrContext);

        final GVRActivity activity = gvrContext.getActivity();
        mTextView = new TextView(activity);
        mTextView.setBackgroundColor(Color.TRANSPARENT);
        mTextView.setTextColor(Color.WHITE);
        setNumberLines( strings.length );
        String text = "";
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) text += "\n";
            text += strings[i];
        }
        mTextView.setText(text);

        mTextViewContainer = new LinearLayout(activity);
        mFrameListener = new GVRDrawFrameListenerImpl(this);


        GVRTexture texture = new GVRExternalTexture(gvrContext);

        gvrMaterialX3D = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
        gvrMaterialX3D.setMainTexture(texture);

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurface = new Surface(mSurfaceTexture);

        setTextStrings(strings);

        sReferenceCounter++;
        mCount = sReferenceCounter;
        mFirstFrame = true;
    }

    /**
     * Once the font family, size and style are set, one can build the Quad
     * based on the width and height of the desired Quad.
     * @param canvasWidth
     * @param canvasHeight
     */
    public void setTextPlaneSize(float canvasWidth, float canvasHeight) {
        int canvasWidthInt = (int)Math.ceil(canvasWidth);
        int canvasHeightInt = (int)Math.ceil(canvasHeight);
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setLayoutParams(new LayoutParams( canvasWidthInt,  canvasHeightInt) );

        float width = canvasWidth / FACTOR_IMAGE_SIZE;
        float height = canvasHeight / FACTOR_IMAGE_SIZE;

        GVRContext gvrContext = getGVRContext();
        GVRMesh gvrMesh = gvrContext.createQuad(width, height);

        GVRRenderData gvrRenderData = new GVRRenderData(gvrContext);
        gvrRenderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        gvrRenderData.setMesh(gvrMesh);
        gvrRenderData.setMaterial(gvrMaterialX3D);
        this.attachRenderData( gvrRenderData );

        mTextViewContainer.addView(mTextView);
        mTextViewContainer.setVisibility(View.VISIBLE);

        mTextViewContainer.measure(canvasWidthInt, canvasHeightInt);
        mTextViewContainer.layout(0, 0, canvasWidthInt, canvasHeightInt);

        gvrContext.registerDrawFrameListener(mFrameListener);

        mSurfaceTexture.setDefaultBufferSize(canvasWidthInt, canvasHeightInt);

        sReferenceCounter++;
        mCount = sReferenceCounter;
        mFirstFrame = true;
    }

    /**
     * Set the text size.
     * 
     * @param size
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

    public void setText(String text) {
        mTextView.setText(text);
        mIsChanged = true;
    }
    /**
     * Appends the text to be displayed.
     *
     * @param text
     *            The new text to be appended to the end of the current text.
     */
    public void append(String text) {
        mTextView.append(text);
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
     * set the number of lines for this text
     * @param lines
     */
    public void setNumberLines(int lines) {
        mTextView.setLines(lines);
        mIsChanged = true;
    }
    /**
     * Min Lines for this text
     * @return
     */
    public int getMinLines() {
        return mTextView.getMinLines();
    }

    /**
     * max lines for this text
     * @return
     */
    public int getMaxLines() {
        return mTextView.getMaxLines();
    }

    /**
     * Saves the lines of text and used later to calculate the
     * length in pixels based on font-family, size, etc.
     * @param textString
     */
    public void setTextStrings(String[] textString) {
        linesOfText = new String[textString.length];
        for (int i = 0; i < textString.length; i++) {
            linesOfText[i] = textString[i];
        }
    }

    public String getTextLine(int lineNumber) {
        String returnString = "";
        if ( (lineNumber >= 0) && (lineNumber < linesOfText.length) ) {
            returnString = linesOfText[lineNumber];
        }
        return returnString;
    }

    /**
     * get the length of this line based in pixels and dependent on
     * the font and size of the characters.
     * @param text
     * @return
     */
    public float lineLength(String text) {
        return mTextView.getPaint().measureText(text);
    }

    /**
     * set the justification to left, center/middle or right.  The values from
     * the enumerated type are from X3D's <FontStyle> justify setting.
     * @param justify
     */
    public void setJustification(justifyTypes justify) {
        if (justify == justifyTypes.BEGIN) mTextView.setGravity(Gravity.START);
        else if (justify == justifyTypes.MIDDLE) mTextView.setGravity(Gravity.CENTER);
        else if (justify == justifyTypes.END) mTextView.setGravity(Gravity.RIGHT);
        else if (justify == justifyTypes.FIRST) mTextView.setGravity(Gravity.LEFT);
        else mTextView.setGravity(Gravity.NO_GRAVITY);

    }

    /**
     * Returns the justification type defined the enumerated type.
     * If none is set, it returns null
     * @return
     */
    public justifyTypes getJustification() {
        justifyTypes justify = null;
        if ( mTextView.getGravity()== Gravity.START) justify = justifyTypes.BEGIN;
        else if ( mTextView.getGravity()== Gravity.CENTER) justify = justifyTypes.MIDDLE;
        else if ( mTextView.getGravity()== Gravity.RIGHT) justify = justifyTypes.END;
        else if ( mTextView.getGravity()== Gravity.LEFT) justify = justifyTypes.FIRST;
        return justify;
    }

    /**
     * Sets the typeface (font)
     * @param gvrContext
     * @param font
     *          a string that matches the font name saved in the assets directory
     *          Must include the file ending such as "myFont.ttf"
     *
     * @return
     */
    public boolean setTypeface(GVRContext gvrContext, String font) {
        return setTypeface(gvrContext, font, fontStyleTypes.PLAIN);
    }
    /**
     * Sets the typeface (font)
     * @param gvrContext
     * @param font
     *          a string that matches the font name saved in the assets directory
     *          Must include the file ending such as "myFont.ttf"
     * @param style
     *          value for style from the enumerated list that matches values from X3D
     *          <FontStyle> style setting
     * @return
     */
    public boolean setTypeface(GVRContext gvrContext, String font, fontStyleTypes style) {
        if ( !font.equals(DEFAULT_FONT) ) {
            try {
                Context context = gvrContext.getContext();
                int styleType = Typeface.NORMAL;
                if (style == fontStyleTypes.BOLD) styleType = Typeface.BOLD;
                else if  (style == fontStyleTypes.ITALIC) styleType = Typeface.ITALIC;
                else if  (style == fontStyleTypes.BOLDITALIC) styleType = Typeface.BOLD_ITALIC;

                Typeface typeface = Typeface.createFromAsset(context.getAssets(), font);
                if (typeface != null) {
                    mTextView.setTypeface(typeface, styleType);
                    mIsChanged = true;
                }
            }
            catch (java.lang.RuntimeException e) {
                org.gearvrf.utility.Log.e(TAG, "Runtime error: " + font + "; " + e);
                return false;
            }
            catch (Exception e) {
                org.gearvrf.utility.Log.e(TAG, "Exception: " + e);
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the current Typeface
     * @return
     */
    public Typeface getTypeface () {
        return mTextView.getTypeface();
    }

    /**
     * The amount to add to the line spacing
     * @param lineSpacing
     */
    public void setLineSpacing(float lineSpacing) {
        mTextView.setLineSpacing(lineSpacing, 1);
    }

    public float getLineHeight() {
        return mTextView.getLineHeight();
    }


    /**
     * Get the amount of extra spacing between lines.
     * @return
     */
    public float getLineSpacing() {
        return mTextView.getLineSpacingExtra();
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
