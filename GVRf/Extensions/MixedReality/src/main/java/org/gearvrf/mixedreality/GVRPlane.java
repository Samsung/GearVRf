package org.gearvrf.mixedreality;

import org.joml.Matrix4f;

/**
 * Created by diego.a on 27/03/18.
 */

public class GVRPlane{
    private int type;
    private Matrix4f center;
    private int trackingState;

    public GVRPlane() {
        center = new Matrix4f();
    }

    public void setPlaneType() {

    }

    public void setCenter(float[] poseMatrix) {
        this.center = this.center.set(poseMatrix);
    }

    public void setTrackingState() {

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
}
