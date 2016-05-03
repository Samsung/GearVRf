package org.gearvrf.scene_objects.view;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class represents a {@link TextView} that is rendered
 * into the attached {@link GVRViewSceneObject}.
 * Internally this view is added to a {@link LinearLayout}.
 * See {@link GVRView} and {@link GVRViewSceneObject}
 */
public class GVRTextView extends TextView implements GVRView {
    private GVRViewSceneObject mSceneObject = null;
    private ViewGroup mTextViewContainer;

    public GVRTextView(GVRActivity context) {
        super(context);

        mTextViewContainer = new LinearLayout(context);
        mTextViewContainer.addView(this);

        mTextViewContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        context.registerView(mTextViewContainer);
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
    public GVRTextView(GVRActivity context, int viewWidth, int viewHeight) {
        this(context, viewWidth, viewHeight, null);
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
    public GVRTextView(GVRActivity context, int viewWidth, int viewHeight, String text) {
        this(context);

        setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
        mTextViewContainer.measure(viewWidth, viewHeight);
        mTextViewContainer.layout(0, 0, viewWidth, viewHeight);
        setText(text);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSceneObject == null)
            return;

        // Canvas attached to GVRViewSceneObject to draw on
        Canvas attachedCanvas = mSceneObject.lockCanvas();
        // Clear the canvas to avoid overlapping text when
        // TextView's background is transparent.
        attachedCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        // draw the view to provided canvas
        super.draw(attachedCanvas);

        mSceneObject.unlockCanvasAndPost(attachedCanvas);
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
