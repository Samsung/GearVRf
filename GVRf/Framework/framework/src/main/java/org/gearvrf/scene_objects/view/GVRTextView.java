package org.gearvrf.scene_objects.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRApplication;
import org.gearvrf.scene_objects.GVRViewSceneObject;

/**
 * This class represents a {@link TextView} that is rendered
 * into the attached {@link GVRViewSceneObject}.
 * Internally this view is added to a {@link LinearLayout}.
 * See {@link GVRView} and {@link GVRViewSceneObject}
 */
@Deprecated
public class GVRTextView extends TextView implements GVRView {
    private GVRViewSceneObject mSceneObject = null;
    private ViewGroup mTextViewContainer;

    @Deprecated
    public GVRTextView(GVRActivity activity) {
        this(activity.getGVRApplication());
    }

    /**
     * Creates a new instance and sets its internal {@linkplain LinearLayout Layout container}
     * with the specified width and height.
     *
     * @param viewWidth
     *            Width of the {@linkplain LinearLayout Layout container}
     * @param viewHeight
     *            Height of the {@linkplain LinearLayout Layout container}
     */
    public GVRTextView(GVRActivity activity, int viewWidth, int viewHeight) {
        this(activity.getGVRApplication(), viewWidth, viewHeight, null);
    }

    public GVRTextView(GVRApplication application) {
        super(application.getActivity());

        mTextViewContainer = new LinearLayout(application.getActivity());
        mTextViewContainer.addView(this);

        mTextViewContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        application.registerView(mTextViewContainer);
    }

    /**
     * Creates a new instance and sets its internal {@linkplain LinearLayout Layout container}
     * with the specified width and height.
     *
     * @param viewWidth
     *            Width of the {@linkplain LinearLayout Layout container}
     * @param viewHeight
     *            Height of the {@linkplain LinearLayout Layout container}
     */
    public GVRTextView(GVRApplication application, int viewWidth, int viewHeight) {
        this(application, viewWidth, viewHeight, null);
    }

    /**
     * Creates a new instance and sets its internal {@linkplain LinearLayout Layout container}
     * with the specified width and height, and text with specified text.
     *
     * @param viewWidth
     *            Width of the {@linkplain LinearLayout Layout container}
     * @param viewHeight
     *            Height of the {@linkplain LinearLayout Layout container}
     * @param text
     *            Text to set to the text view.
     */
    public GVRTextView(GVRApplication application, int viewWidth, int viewHeight, String text) {
        this(application);

        setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
        mTextViewContainer.measure(viewWidth, viewHeight);
        mTextViewContainer.layout(0, 0, viewWidth, viewHeight);
        setText(text);
    }

    @Override
    public void setSceneObject(GVRViewSceneObject sceneObject) {
        mSceneObject = sceneObject;
    }

    @Override
    public View getView() {
        return this;
    }
}
