package org.gearvrf.mixedreality;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;


public abstract class GVRPlane extends GVRSceneObject{
    protected int mType;
    protected int mTrackingState;
    protected float mWidth;
    protected float mHeight;
    protected float[] mCenterPose = new float[16];


    public GVRPlane(GVRContext gvrContext) {
        super(gvrContext);
    }

    protected abstract void setCenter(float[] pose);

    protected abstract void setType();

    protected abstract void setTrackingState();

    protected abstract void setWidth(float width);

    protected abstract void setHeight(float height);

    public float[] getCenterPose() {
        return this.mCenterPose;
    }

    public int getPlaneType() {
        return this.mType;
    }

    public int getTrackingState() {
        return this.mTrackingState;
    }

    public float getWidth() {
        return this.mWidth;
    }

    public float getHeight() {
        return this.mHeight;
    }
}
