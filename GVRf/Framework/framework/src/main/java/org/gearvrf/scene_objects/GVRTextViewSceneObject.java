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

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import java.lang.ref.WeakReference;

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

    // use when no font family is set
    public static final String DEFAULT_FONT = "default";
    private String fontFamily = DEFAULT_FONT;
    // Android's TextView.setGravity() RIGHT and LEFT return a value added by 0x30
    private final int GRAVITY_RIGHT = Gravity.RIGHT + 0x30;
    private final int GRAVITY_LEFT  = Gravity.LEFT + 0x30;

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
     * Enumerated types for justification and style
     */
    public enum justifyTypes { BEGIN, MIDDLE, END, FIRST };
    public enum fontStyleTypes { PLAIN, BOLD, ITALIC, BOLDITALIC };

    private static int sReferenceCounter = 0;// This is for load balancing.
    private boolean mFirstFrame;
    private boolean mIsChanged;
    private volatile int mRefreshInterval = REALTIME_REFRESH_INTERVAL;

    private final Surface mSurface;
    private final SurfaceTexture mSurfaceTexture;
    private final LinearLayout mTextViewContainer;
    private final TextView mTextView;

    // the font size value is a raw value.
    // textSize value is change by Android textView
    private float size; //

    private int mCount;
    private final GVRDrawFrameListenerImpl mFrameListener;

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
        super(gvrContext, GVRMesh.createQuad(gvrContext, "float3 a_position float2 a_texcoord", width, height));

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

        final Activity activity = gvrContext.getActivity();
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
     * Constructs a GVRTextViewSceneObject that will be on a plane contoured to the size
     * of the longest line of text and the number of lines of text.
     * @param gvrContext
     *            current {@link GVRContext}
     * @param name
     *          In X3D, it's the name of the FontStyle, not the Text node
     * @param string
     *          Text to be displayed, with lines separated by '\n' newline character
     * @param font
     *          Font Family for this text.  If not the default font, the ttf font file
     *          should be in the assets folder.
     * @param justify
     *          Will either be left, right or centered, based on the enumerated type file
     * @param spacing
     *          Additional spacing between the lines of text
     * @param size
     *          Font character size, default is 1.
     * @param style
     *          Specifies plain, bold, italic or bolditalic based on enumerated type value
     */
    public GVRTextViewSceneObject(GVRContext gvrContext, String name, String string, String font,
                                  justifyTypes justify, float spacing, float size, fontStyleTypes style) {
        super(gvrContext);

        setName(name);
        final Activity activity = gvrContext.getActivity();
        mTextView = new TextView(activity);
        mTextView.setBackgroundColor(Color.TRANSPARENT);
        mTextView.setTextColor(Color.WHITE);

        mTextView.setText(string);
        mTextViewContainer = new LinearLayout(activity);
        mFrameListener = new GVRDrawFrameListenerImpl(this);

        GVRTexture texture = new GVRExternalTexture(gvrContext);

        GVRMaterial gvrMaterial = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
        gvrMaterial.setMainTexture(texture);

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurface = new Surface(mSurfaceTexture);

        int numberOfLines = 1;
        String text = string;
        while ( text.indexOf("\n") != -1) {
            numberOfLines++;
            text = text.substring(text.indexOf("\n")+1, text.length() );
        }
        String[] lines = new String[numberOfLines];
        int lineNumber = 0;
        while ( string.indexOf("\n") != -1) {
            lines[lineNumber] = string.substring(0, string.indexOf("\n") );
            lineNumber++;
            string = string.substring(string.indexOf("\n")+1, string.length() );
        }
        lines[lineNumber] = string.substring(string.indexOf("\n")+1, string.length() );

        setJustification( justify );
        //setLineSpacing((spacing - 1) * 10.0f);
        setLineSpacing(spacing);
        this.size = size;
        setTextSize( getSize() );
        fontFamily = font;
        setTypeface(gvrContext, fontFamily, style);

        // Get the length of the longest line of text in order to set the
        //   width of the plane that the text will be 'pasted' on to.
        float maxLineLength = 0;
        for (int i = 0; i < lines.length; i++) {
            float lineLength = getLineLength(lines[i]);
            if ( maxLineLength < lineLength ) maxLineLength = lineLength;
        }

        int canvasWidthInt = (int)Math.ceil(maxLineLength);
        int canvasHeightInt = (int)Math.ceil( numberOfLines*getLineHeight() );
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setLayoutParams(new LayoutParams( canvasWidthInt,  canvasHeightInt) );

        float width = maxLineLength / FACTOR_IMAGE_SIZE;
        float height = (numberOfLines*getLineHeight()) / FACTOR_IMAGE_SIZE;

        GVRMesh gvrMesh = gvrContext.createQuad(width, height);

        GVRRenderData gvrRenderData = new GVRRenderData(gvrContext);
        gvrRenderData.setMesh(gvrMesh);
        gvrRenderData.setMaterial(gvrMaterial);
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
     * Note that Android's TextView.setTextSize modifies the size value
     * Thus size, and getTextSize() will not be the same results
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
     * @return The current text size modified and saved within Android's TextView class.
     */
    public float getTextSize() {
        return mTextView.getTextSize();
    }

    /**
     * getSize is the size saved within GVRTextViewSceneObject and differs
     * from the textSize modified within Android's TextView class.
     * @return
     */
    public float getSize() {
        return this.size;
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
     * returns the length of this line based in pixels and dependent on
     * the font family and size of the characters.
     * @param text
     * @return
     */
    public float getLineLength(String text) {
        return mTextView.getPaint().measureText(text);
    }

    /**
     * set the justification to left, center/middle or right.  The values from
     * the enumerated type are from X3D's <FontStyle> justify setting.
     * @param justify
     */
    public void setJustification(justifyTypes justify) {
        if (justify == justifyTypes.BEGIN) mTextView.setGravity(Gravity.LEFT);
        else if (justify == justifyTypes.MIDDLE) mTextView.setGravity(Gravity.CENTER);
        else if (justify == justifyTypes.END) mTextView.setGravity(Gravity.RIGHT);
            else if (justify == justifyTypes.FIRST) mTextView.setGravity(Gravity.START);
                else mTextView.setGravity(Gravity.NO_GRAVITY);
    }

    /**
     * Returns the justification type defined the enumerated type.
     * If none is set, it returns null.
     * Note that the value saved is not the value returned for
     * setGravity(LEFT) and setGravity(RIGHT).  0x30 is added to those values by
     * Android's TextView.setGravity()
     * @return
     */
    public justifyTypes getJustification() {
        justifyTypes justify = null;
        if ( mTextView.getGravity()== Gravity.LEFT) justify = justifyTypes.BEGIN;
        else if ( mTextView.getGravity()== GRAVITY_LEFT) justify = justifyTypes.BEGIN;
        else if ( mTextView.getGravity()== Gravity.CENTER) justify = justifyTypes.MIDDLE;
            else if ( mTextView.getGravity()== Gravity.RIGHT) justify = justifyTypes.END;
                else if ( mTextView.getGravity()== GRAVITY_RIGHT) justify = justifyTypes.END;
                    else if ( mTextView.getGravity()== Gravity.START) justify = justifyTypes.FIRST;
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
     * Returns the style type: plain, bold, italic or boldItalic
     * @return
     */
    public fontStyleTypes getStyleType() {
        fontStyleTypes styleType = fontStyleTypes.PLAIN;
        if ( mTextView.getTypeface().getStyle() == Typeface.BOLD) styleType = fontStyleTypes.BOLD;
        else if ( mTextView.getTypeface().getStyle() == Typeface.ITALIC) styleType = fontStyleTypes.ITALIC;
        else if ( mTextView.getTypeface().getStyle() == Typeface.BOLD_ITALIC) styleType = fontStyleTypes.BOLDITALIC;
        return styleType;
    }

    /**
     * Returns the font family file name
     * @return
     */
    public String getFontFamily() {
        return fontFamily;
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