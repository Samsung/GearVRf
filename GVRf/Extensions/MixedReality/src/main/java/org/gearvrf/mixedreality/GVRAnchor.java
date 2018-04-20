package org.gearvrf.mixedreality;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

public abstract class GVRAnchor extends GVRSceneObject{
    private GVRTrackingState mTrackingState;
    protected GVRSceneObject mSceneObject = null;

    protected GVRAnchor(GVRContext gvrContext, GVRSceneObject sceneObject) {
        super(gvrContext);
        mSceneObject = sceneObject;
        addChildObject(sceneObject);
    }

    public void setTrackingState(GVRTrackingState state) { this.mTrackingState = state; }

    public GVRTrackingState getTrackingState() { return this.mTrackingState; }

    public void update(float[] viewmtx, float[] gvrmatrix, float scale) {
        onUpdate(viewmtx, gvrmatrix, scale);
    }

    protected abstract void onUpdate(float[] viewmtx, float[] gvrmatrix, float scale);
}
