package org.gearvrf.mixedreality;

import org.joml.Matrix4f;

/**
 * Created by diego.a on 10/04/18.
 */

public class GVRHitResult {
    private float[] pose;
    private float distance;
    private GVRPlane plane;


    public float[] getPose() {
        return pose;
    }

    public void setPose(float[] pose) {
        this.pose = pose;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setPlane(GVRPlane plane) {
        this.plane = plane;
    }

    public GVRPlane getPlane() {
        return plane;
    }
}
