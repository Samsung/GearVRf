package org.gearvrf.animation.keyframe;

import org.joml.Vector3f;

public class GVRPositionKey implements GVRKeyFrame<Vector3f> {
    public GVRPositionKey(float time, float x, float y, float z) {
        mTime = time;
        mX = x;
        mY = y;
        mZ = z;
    }

    @Override
    public float getTime() {
        return mTime;
    }

    public Vector3f getValue() {
        return new Vector3f(mX, mY, mZ);
    }

    public void setValue(Vector3f pos) {
        mX = pos.x;
        mY = pos.y;
        mZ = pos.z;
    }

    private float mTime;
    private float mX, mY, mZ;
}
