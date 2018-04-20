package org.gearvrf.mixedreality;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;


public abstract class GVRPlane extends GVRSceneObject{

    protected int mType;
    private GVRTrackingState mTrackingState;
    protected float mWidth;
    protected float mHeight;
    protected float[] mCenterPose = new float[16];


    public GVRPlane(GVRContext gvrContext) { super(gvrContext); }

    public void setTrackingState(GVRTrackingState state) { this.mTrackingState = state; }

    public GVRTrackingState getTrackingState() { return this.mTrackingState; }

    protected abstract void setCenter(float[] pose);

    protected abstract void setType();

    protected abstract void setWidth(float width);

    protected abstract void setHeight(float height);

    public float[] getCenterPose() {
        return this.mCenterPose;
    }

    public int getPlaneType() {
        return this.mType;
    }

    public float getWidth() {
        return this.mWidth;
    }

    public float getHeight() {
        return this.mHeight;
    }

    public void update(float[] viewmtx, float[] gvrmatrix, float scale) {
        onUpdate(viewmtx, gvrmatrix, scale);
    }

    protected abstract void onUpdate(float[] viewmtx, float[] gvrmatrix, float scale);
}
