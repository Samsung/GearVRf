package org.gearvrf.animation.keyframe;

import org.joml.Quaternionf;

/**
 * Represents a rotation keyframe.
 */
public class GVRRotationKey implements GVRKeyFrame<Quaternionf> {
    /**
     * Constructor.
     *
     * @param time The time of the keyframe.
     * @param x The X component of the quaternion.
     * @param y The Y component of the quaternion.
     * @param z The Z component of the quaternion.
     * @param w The W component of the quaternion.
     */
    public GVRRotationKey(float time, float x, float y, float z, float w) {
        mTime = time;
        mX = x;
        mY = y;
        mZ = z;
        mW = w;
    }

    /**
     * Gets the time of the keyframe.
     */
    @Override
    public float getTime() {
        return mTime;
    }

    /**
     * Gets the quaternion of the keyframe.
     */
    public Quaternionf getValue() {
        return new Quaternionf(mX, mY, mZ, mW);
    }

    /**
     * Sets the quaternion of the keyframe.
     */
    public void setValue(Quaternionf rot) {
        mX = rot.x;
        mY = rot.y;
        mZ = rot.z;
        mW = rot.w;
    }

    private float mTime;
    private float mX, mY, mZ, mW; // w is the real part
}
