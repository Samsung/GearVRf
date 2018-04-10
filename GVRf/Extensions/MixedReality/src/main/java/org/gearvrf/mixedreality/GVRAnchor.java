package org.gearvrf.mixedreality;

import android.renderscript.Matrix4f;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

public abstract class GVRAnchor extends GVRBehavior {
    static private long TYPE_ANCHOR = newComponentType(GVRAnchor.class);
    protected GVRSceneObject mOwner;
    protected float[] modelMatrix = new float[16];
    private GVRTrackingState mTrackingState;

    protected GVRAnchor(GVRContext gvrContext) { super(gvrContext); }

    static public long getComponentType() { return TYPE_ANCHOR; }

    public void setModelMatrix(float[] modelMatrix) { this.modelMatrix = modelMatrix; }

    public void setTrackingState(GVRTrackingState state) { this.mTrackingState = state; }

    public GVRTrackingState getTrackingState() { return this.mTrackingState; }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);
        mOwner = newOwner;
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        mOwner = null;
        onDetachAnchor();
    }

    @Override
    public void onDrawFrame(float frameTime) {
        super.onDrawFrame(frameTime);
        if (mOwner != null) {
            mOwner.getTransform().setModelMatrix(modelMatrix);
        }
    }

    protected abstract void onDetachAnchor();
}
