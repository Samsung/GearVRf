package org.gearvrf.scene_objects.view;

import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import org.gearvrf.GVRApplication;
import org.gearvrf.scene_objects.GVRViewSceneObject;

/**
 * This class represents a {@link FrameLayout} that is rendered
 * into the attached {@link GVRViewSceneObject}.
 * Every {@link View Android view} added to this layout will be rendered
 * into attached {@link GVRViewSceneObject}.
 * See {@link GVRView} and {@link GVRViewSceneObject}
 */
@Deprecated
public class GVRFrameLayout extends FrameLayout implements GVRView {
    private GVRViewSceneObject mSceneObject = null;

    @Deprecated
    public GVRFrameLayout(GVRApplication application) {
        super(application.getActivity());

        /* Setting background color to avoid complex logic,
         * otherwise android will call just drawChild
         * for each child. Setting some background color
         * it calls draw(Canvas) keeping our default logic.
         */
        setBackgroundColor(Color.TRANSPARENT);

        application.registerView(this);
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
