package org.gearvrf.animation.keyframe;

import org.joml.Vector3f;

/**
 * Represents a scale keyframe.
 */
public class GVRScaleKey implements GVRKeyFrame<Vector3f> {
    /**
     * Constructor.
     *
     * @param time The time of the key frame.
     * @param x The X component of the scaling vector.
     * @param y The Y component of the scaling vector.
     * @param z The Z component of the scaling vector.
     */
    public GVRScaleKey(float time, float x, float y, float z) {
        mTime = time;
        mX = x;
        mY = y;
        mZ = z;
    }

    /**
     * Gets the time of the keyframe.
     */
    @Override
    public float getTime() {
        return mTime;
    }

    /**
     * Gets the scale vector of the keyframe.
     */
    public Vector3f getValue() {
        return new Vector3f(mX, mY, mZ);
    }

    /**
     * Sets the scale vector of the keyframe.
     */
    public void setValue(Vector3f scale) {
        mX = scale.x;
        mY = scale.y;
        mZ = scale.z;
    }

    private float mTime;
    private float mX, mY, mZ;
}
