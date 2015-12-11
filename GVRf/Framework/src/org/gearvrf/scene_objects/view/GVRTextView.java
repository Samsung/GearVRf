package org.gearvrf.scene_objects.view;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import android.graphics.Canvas;
import android.view.View;
import android.widget.TextView;

/**
 * This class represents a {@link TextView} that is rendered
 * into the attached {@link GVRViewSceneObject}
 * See {@link GVRView} and {@link GVRViewSceneObject}
 */
public class GVRTextView extends TextView implements GVRView {
    private GVRViewSceneObject mSceneObject = null;
    
    public GVRTextView(GVRActivity context) {
        super(context);

        context.registerView(this);
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
