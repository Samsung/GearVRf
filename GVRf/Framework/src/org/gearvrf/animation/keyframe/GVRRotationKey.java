package org.gearvrf.animation.keyframe;

import org.joml.Quaternionf;

public class GVRRotationKey implements GVRKeyFrame<Quaternionf> {
    public GVRRotationKey(float time, float x, float y, float z, float w) {
        mTime = time;
        mX = x;
        mY = y;
        mZ = z;
        mW = w;
    }

    @Override
    public float getTime() {
        return mTime;
    }

    public Quaternionf getValue() {
        return new Quaternionf(mX, mY, mZ, mW);
    }

    public void setValue(Quaternionf rot) {
        mX = rot.x;
        mY = rot.y;
        mZ = rot.z;
        mW = rot.w;
    }

    private float mTime;
    private float mX, mY, mZ, mW; // w is the real part
}
