package org.gearvrf.animation.keyframe;

import org.joml.Vector3f;

public class GVRScaleKey implements GVRKeyFrame<Vector3f> {
    public GVRScaleKey(float time, float x, float y, float z) {
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

    public void setValue(Vector3f scale) {
        mX = scale.x;
        mY = scale.y;
        mZ = scale.z;
    }

    private float mTime;
    private float mX, mY, mZ;
}
