package org.gearvrf.animation.keyframe;

import org.gearvrf.PrettyPrint;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;

/**
 * Describes the animation of a set of floating point values.
 */
public final class GVRQuatAnimation extends GVRFloatAnimation
{
    private static final String TAG = GVRQuatAnimation.class.getSimpleName();

    public static class SphericalInterpolator extends LinearInterpolator
    {
        private Quaternionf mTempQuatA = new Quaternionf();
        private Quaternionf mTempQuatB = new Quaternionf();

        public SphericalInterpolator(float[] keyData, int keySize)
        {
            super(keyData, keySize);
        }

        public boolean interpolateValues(int keyIndex, float[] values, float factor)
        {
            int firstOfs = getKeyOffset(keyIndex);
            int lastOfs = getKeyOffset(keyIndex + 1);

            if ((firstOfs < 0) || (lastOfs < 0))
            {
                return false;
            }
            ++firstOfs;
            ++lastOfs;
            mTempQuatA.x = mKeyData[firstOfs + 0];
            mTempQuatA.y = mKeyData[firstOfs + 1];
            mTempQuatA.z = mKeyData[firstOfs + 2];
            mTempQuatA.w = mKeyData[firstOfs + 3];
            mTempQuatB.x = mKeyData[lastOfs + 0];
            mTempQuatB.y = mKeyData[lastOfs + 1];
            mTempQuatB.z = mKeyData[lastOfs + 2];
            mTempQuatB.w = mKeyData[lastOfs + 3];
            mTempQuatA.slerp(mTempQuatB, factor, mTempQuatA);
            values[0] = mTempQuatA.x;
            values[1] = mTempQuatA.y;
            values[2] = mTempQuatA.z;
            values[3] = mTempQuatA.w;
            return true;
        }
    };

    /**
     * Constructor.
     *
     * @param keyData animation key data, must be x,y,z,w (Quaterions)
     */
    public GVRQuatAnimation(float[] keyData)
    {
        super(keyData, 5);
        mFloatInterpolator =  new SphericalInterpolator(mKeys, 5);
    }

    /**
     * Constructor.
     *
     * @param numKeys expected number of animation keys
     */
    public GVRQuatAnimation(int numKeys)
    {
        super(numKeys, 5);
        mFloatInterpolator =  new SphericalInterpolator(mKeys, 5);
    }

    /**
     * Returns the scaling factor as vector.<p>
     *
     * @param keyIndex the index of the scale key
     *
     * @return the scaling factor as vector
     */
    public void getKey(int keyIndex, Quaternionf q)
    {
        int index = keyIndex * mFloatsPerKey;
        q.x = mKeys[index + 1];
        q.y = mKeys[index + 2];
        q.z = mKeys[index + 3];
        q.w = mKeys[index + 4];
    }

    public void setKey(int keyIndex, float time, final Quaternionf q)
    {
        int index = keyIndex * mFloatsPerKey;

        mKeys[index] = time;
        mKeys[index + 1] = q.x;
        mKeys[index + 2] = q.y;
        mKeys[index + 3] = q.z;
        mKeys[index + 4] = q.w;
    }


}

