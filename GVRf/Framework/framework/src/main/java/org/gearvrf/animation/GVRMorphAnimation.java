package org.gearvrf.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMeshMorph;
import org.gearvrf.PrettyPrint;
import org.gearvrf.animation.keyframe.GVRFloatAnimation;
import org.gearvrf.utility.Log;

/** 
 * Describes the animation of a set of floating point values.
 */
public final class GVRMorphAnimation extends GVRAnimation implements PrettyPrint
{
    private static final String TAG = GVRMorphAnimation.class.getSimpleName();
    protected float[] mKeys;
    protected GVRFloatAnimation mKeyInterpolator;
    protected float[] mCurrentValues;

    /**
     * Constructor.
     *
     * @param keyData blend weight keys
     * @param keySize number of floats per key
     */
    public GVRMorphAnimation(GVRMeshMorph target, float[] keyData, int keySize)
    {
        super(target, keyData[keyData.length - keySize] - keyData[0]);
        mKeys = keyData;
        mKeyInterpolator = new GVRFloatAnimation(keyData, keySize);
        mCurrentValues = new float[keySize - 1];
    }

    public void animate(GVRHybridObject object, float animationTime)
    {
        GVRMeshMorph morph  = (GVRMeshMorph) mTarget;
        mKeyInterpolator.animate(animationTime, mCurrentValues);
        morph.setWeights(mCurrentValues);
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent)
    {
        sb.append(Log.getSpaces(indent));
        sb.append(GVRMorphAnimation.class.getSimpleName());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }
}

