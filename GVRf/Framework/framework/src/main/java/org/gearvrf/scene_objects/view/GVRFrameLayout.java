package org.gearvrf.scene_objects.view;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.FrameLayout;

/**
 * This class represents a {@link FrameLayout} that is rendered
 * into the attached {@link GVRViewSceneObject}.
 * Every {@link View Android view} added to this layout will be rendered
 * into attached {@link GVRViewSceneObject}.
 * See {@link GVRView} and {@link GVRViewSceneObject}
 */
public class GVRFrameLayout extends FrameLayout implements GVRView {
    private GVRViewSceneObject mSceneObject = null;

    public GVRFrameLayout(GVRActivity  context) {
        super(context);

        /* Setting background color to avoid complex logic,
         * otherwise android will call just drawChild
         * for each child. Setting some background color
         * it calls draw(Canvas) keeping our default logic.
         */
        setBackgroundColor(Color.TRANSPARENT);

        context.registerView(this);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSceneObject == null)
            return;

        // Canvas attached to GVRViewSceneObject to draw on
        Canvas attachedCanvas = mSceneObject.lockCanvas();
        // Clear the canvas
        attachedCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
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
