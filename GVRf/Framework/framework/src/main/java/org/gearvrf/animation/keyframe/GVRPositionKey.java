package org.gearvrf.animation.keyframe;

import org.joml.Vector3f;

/**
 * Represents a position keyframe.
 */
public class GVRPositionKey implements GVRKeyFrame<Vector3f> {
    /**
     * Constructor.
     *
     * @param time The time of the key frame.
     * @param x The X component of the position.
     * @param y The Y component of the position.
     * @param z The Z component of the position.
     */
    public GVRPositionKey(float time, float x, float y, float z) {
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
     * Gets the position vector of the keyframe.
     */
    public Vector3f getValue() {
        return new Vector3f(mX, mY, mZ);
    }

    /**
     * Sets the position vector of the keyframe.
     */
    public void setValue(Vector3f pos) {
        mX = pos.x;
        mY = pos.y;
        mZ = pos.z;
    }

    private float mTime;
    private float mX, mY, mZ;
}
