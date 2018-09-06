package org.gearvrf.widgetlib.widget.basic;

import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.NodeEntry;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.layout.LayoutHelpers;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;
import org.json.JSONObject;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.copy;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optPointF;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optString;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.put;

/**
 * A user interface element that displays text to the user. {@link GVRTextViewSceneObject} is used
 * to represent the text. {@link GVRTextViewSceneObject} is actually using standard Android
 * {@link TextView} for text UI.
 *
 */
@SuppressWarnings("deprecation")
public class TextWidget extends Widget implements TextContainer {

    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    public TextWidget(final GVRContext context, final GVRSceneObject sceneObject) {
        super(context, sceneObject);
        mTextViewSceneObject = maybeWrap(getSceneObject());
        init();
    }

    /**
     * A constructor for wrapping existing {@link GVRSceneObject} instances.
     * Deriving classes should override and do whatever processing is
     * appropriate.
     *
     * @param context
     *            The current {@link GVRContext}
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     * @param attributes
     *            A set of class-specific attributes.
     * @throws InstantiationException
     */
    @Deprecated
    public TextWidget(GVRContext context, GVRSceneObject sceneObject,
            NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);
        mTextViewSceneObject = maybeWrap(sceneObject);
        init();
    }

    /**
     * Core {@link TextWidget} constructor.
     *
     * @param context A valid {@link GVRContext}.
     * @param properties A structured set of properties for the {@code TextWidget} instance. See
     *                       {@code textcontainer.json} for schema.
     */
    public TextWidget(GVRContext context, JSONObject properties) {
        super(context, createPackagedTextView(context, properties));
        mTextViewSceneObject = (GVRTextViewSceneObject) getSceneObject();
        init();
    }

    /**
     * Shows a {@link TextView} on a {@linkplain Widget widget} with view's
     * default height and width.
     *
     * @param context
     *            current {@link GVRContext}
     * @param width
     *            Widget height, in GVRF scene graph units.
     *
     *            Please note that your widget's size is independent of the size
     *            of the internal {@code TextView}: a large mismatch between the
     *            scene object's size and the view's size will result in
     *            'spidery' or 'blocky' text.
     *
     * @param height
     *            Widget width, in GVRF scene graph units.
     */
    public TextWidget(GVRContext context, float width, float height) {
        this(context, width, height, null);
    }

    /**
     * Shows a {@link TextView} on a {@linkplain Widget widget} with view's
     * default height and width.
     *
     * @param context
     *            current {@link GVRContext}
     * @param width
     *            Widget height, in GVRF scene graph units.
     *
     *            Please note that your widget's size is independent of the size
     *            of the internal {@code TextView}: a large mismatch between the
     *            scene object's size and the view's size will result in
     *            'spidery' or 'blocky' text.
     *
     * @param height
     *            Widget width, in GVRF scene graph units.
     * @param text
     *            {@link CharSequence} to show on the textView
     */
    public TextWidget(GVRContext context, float width, float height,
            CharSequence text) {
        super(context, new GVRTextViewSceneObject(context, width, height, text));
        mTextViewSceneObject = (GVRTextViewSceneObject) getSceneObject();
    }

    /**
     * Gets the text parameters for the TextWidget
     * @return the copy of {@link TextParams}. Changing this instance does not actually affect
     * TextWidget. To change the parameters of TextWidget, {@link #setTextParams} should be used.
     */
    public TextParams getTextParams() {
        return (TextParams) TextParams.copy(this, new TextParams());
    }

    /**
     * Sets the text parameters for the TextWidget
     * @return the copy of {@link TextParams}. Changing this instance does not actually effect
     * TextWidget. To change the parameters of TextWidget, {@link #setTextParams} should be used.
     */
    public void setTextParams(final TextContainer textInfo) {
        TextParams.copy(textInfo, this);
    }

    @Override
    public Drawable getBackGround() {
        return mTextViewSceneObject.getBackGround();
    }

    @Override
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    @Override
    public int getGravity() {
        return mTextViewSceneObject.getGravity();
    }

    @Override
    public IntervalFrequency getRefreshFrequency() {
        return mTextViewSceneObject.getRefreshFrequency();
    }

    @Override
    public CharSequence getText() {
        return mTextViewSceneObject.getText();
    }

    @Override
    public int getTextColor() {
        return mTextColor;
    }

    @Override
    public float getTextSize() {
        return mTextViewSceneObject.getTextSize();
    }

    @Override
    public String getTextString() {
        return mTextViewSceneObject.getTextString();
    }

    @Override
    public void setBackGround(Drawable drawable) {
        mTextViewSceneObject.setBackGround(drawable);
    }

    @Override
    public void setBackgroundColor(int color) {
        mTextViewSceneObject.setBackgroundColor(color);
    }

    @Override
    public void setGravity(int gravity) {
        mTextViewSceneObject.setGravity(gravity);
    }

    @Override
    public void setRefreshFrequency(IntervalFrequency frequency) {
        mTextViewSceneObject.setRefreshFrequency(frequency);
    }

    @Override
    public void setText(CharSequence text) {
        mTextViewSceneObject.setText(text);
    }

    @Override
    public void setTextColor(int color) {
        mTextViewSceneObject.setTextColor(color);
    }

    @Override
    public void setTextSize(float size) {
        mTextViewSceneObject.setTextSize(size);
    }

    @Override
    public void setTypeface(Typeface typeface) {
        Log.w(TAG, "setTypeface() is not supported by TextWidget.TextParams; use LightTextWidget instead");
    }

    @Override
    public Typeface getTypeface() {
        return null;
    }

    private void init() {
        JSONObject properties = getObjectMetadata();

        TextParams params = new TextParams();
        params.setText(getText());
        params.setFromJSON(getGVRContext().getContext(), properties);
        setTextParams(params);
    }

    private static JSONObject createPackagedTextView(GVRContext context, JSONObject properties) {
        properties = copy(properties);
        PointF size = optPointF(properties, Widget.Properties.size, new PointF(0, 0));
        String text = optString(properties, TextContainer.Properties.text);
        GVRTextViewSceneObject textViewSceneObject =
                new GVRTextViewSceneObject(context, size.x, size.y, text);
        put(properties, Widget.Properties.scene_object, textViewSceneObject);
        return properties;
    }

    private GVRTextViewSceneObject maybeWrap(GVRSceneObject sceneObject) {
        if (sceneObject instanceof GVRTextViewSceneObject) {
            return (GVRTextViewSceneObject) sceneObject;
        } else {
            final float sizes[] = LayoutHelpers
                    .calculateGeometricDimensions(sceneObject);
            final GVRSceneObject temp = new GVRTextViewSceneObject(
                    sceneObject.getGVRContext(), sizes[0], sizes[1], "");
            sceneObject.addChildObject(temp);
            return (GVRTextViewSceneObject) temp;
        }
    }

    private final GVRTextViewSceneObject mTextViewSceneObject;
    private int mBackgroundColor;
    private int mTextColor;

    private static final String TAG = TextWidget.class.getSimpleName();
}
