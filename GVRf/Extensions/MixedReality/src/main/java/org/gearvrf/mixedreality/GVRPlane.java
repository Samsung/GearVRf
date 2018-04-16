package org.gearvrf.mixedreality;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.joml.Matrix4f;


public abstract class GVRPlane extends GVRSceneObject{
    private int type;
    private Matrix4f center;
    private int trackingState;
    private float width;
    private float height;

    public GVRPlane(GVRContext gvrContext) {
        super(gvrContext);
        center = new Matrix4f();
    }

    public void setPlaneType() {

    }

    public void setCenter(float[] poseMatrix) {
        this.center = this.center.set(poseMatrix);
    }

    public void setTrackingState() {

    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getPlaneType() {
        return this.type;
    }

    public Matrix4f getCenter() {
        return this.center;
    }

    public int getTrackingState() {
        return this.trackingState;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }
}
