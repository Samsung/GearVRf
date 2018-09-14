package org.gearvrf.widgetlib.widget.basic;

import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * Interface for managing text settings, basically set of setters and getters for text properties
 */
public interface TextContainer {

    /**
     * Specific properties for Text Container
     */
    enum Properties {
        background,
        background_color,
        gravity,
        refresh_freq,
        text,
        text_color,
        text_size,
        typeface
    }

    /**
     * Gets the text container background
     * @return drawable background
     */
    Drawable getBackGround();

    /**
     * Gets the text container background color
     * @return background color
     */
    int getBackgroundColor();

    /**
     * Gets the text gravity . The default Gravity is {@link android.view.Gravity#CENTER}
     * @return text gravity, see {@link android.view.Gravity} for possible values.
     */
    int getGravity();

    /**
     * Gets IntervalFrequency for text refreshment. See {@link IntervalFrequency} for possible values
     * @return text refreshing interval
     */
    IntervalFrequency getRefreshFrequency();

    /**
     * Gets text
     * @return text
     */
    CharSequence getText();

    /**
     * Gets text color
     * @return text color
     */
    int getTextColor();

    /**
     * Gets text size
     * @return text size
     */
    float getTextSize();

    /**
     * Gets text Typeface
     * @return text Typeface
     */
    Typeface getTypeface();

    /**
     * Gets text as a String
     * @return text as a String
     */
    String getTextString();

    /**
     * Sets the text container background
     * @param drawable background
     */
    void setBackGround(Drawable drawable);

    /**
     * Sets the text container background color
     * @param color background color
     */
    void setBackgroundColor(int color);

    /**
     * Sets the text gravity . The default Gravity is {@link android.view.Gravity#CENTER}
     * @param gravity text gravity, see {@link android.view.Gravity} for possible values.
     */
    void setGravity(int gravity);

    /**
     * Sets IntervalFrequency for text refreshment. See {@link IntervalFrequency} for possible values
     * @param frequency text refreshing interval
     */
    void setRefreshFrequency(IntervalFrequency frequency);

    /**
     * Sets text
     * @param text
     */
    void setText(CharSequence text);

    /**
     * Sets text color
     * @param color text color
     */
    void setTextColor(int color);

    /**
     * Sets text size
     * @param size text size
     */
    void setTextSize(float size);

    /**
     * Sets text typeface
     * @param typeface text typeface
     */
    void setTypeface(Typeface typeface);
}
