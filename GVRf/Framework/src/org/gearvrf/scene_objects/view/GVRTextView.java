package org.gearvrf.scene_objects.view;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import android.graphics.Canvas;
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
        this(context);

        setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
        mTextViewContainer.measure(viewWidth, viewHeight);
        mTextViewContainer.layout(0, 0, viewWidth, viewHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSceneObject == null)
            return;

        // Canvas attached to GVRViewSceneObject to draw on
        Canvas attachedCanvas = mSceneObject.lockCanvas();
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
